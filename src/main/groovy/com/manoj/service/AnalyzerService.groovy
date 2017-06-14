package com.manoj.service

import com.manoj.dto.BuildAnalysis
import com.manoj.entity.BuildState
import com.manoj.entity.Committer
import com.manoj.entity.Rule
import com.manoj.util.ConfigUtil
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil

import static com.manoj.enums.RuleType.*

@Slf4j
@Singleton
class AnalyzerService {

    ConfigUtil configUtil = ConfigUtil.instance

    Boolean doesFileExistForAnalysis(String filePath) {
        File file = new File(filePath)
        file.exists() && file.text.trim()
    }

    BuildAnalysis analyzeTestCases(String moduleName, String coverageFilePath) {
        BuildAnalysis buildAnalysis = new BuildAnalysis(moduleName: moduleName)
        buildAnalysis.with {
            currentlyBrokenTests = getCurrentlyBrokenTests(new File(coverageFilePath))
            previouslyBrokenTests = getPreviouslyBrokenTests(moduleName)
            isBroken = areAnyNewTestsBroken(currentlyBrokenTests, previouslyBrokenTests)
        }
        log.info("Currently Broken Tests: ${buildAnalysis.currentlyBrokenTests.size()}, Previously Broken Tests: ${buildAnalysis.previouslyBrokenTests.size()}, Is Newly Broken: ${buildAnalysis.isBroken}")
        buildAnalysis
    }

    @Transactional
    BuildState storeCurrentAnalysis(BuildAnalysis buildAnalysis) {
        BuildState.withNewSession {
            BuildState state = BuildState.findByModule(buildAnalysis.moduleName) ?: new BuildState(module: buildAnalysis.moduleName, committers: [])
            state.with {
                if (buildAnalysis.currentlyBrokenTests.size()) {
                    if (buildAnalysis.isBroken) {
                        isBrokenOnLastCommit = true
                        lastBrokenBy = configUtil.currentCommitter
                        lastBrokenCommitHash = configUtil.currentCommitHash
                        committers.add(configUtil.currentCommitter)
                        log.info("Updated blame list")
                    } else {
                        isBrokenOnLastCommit = false
                    }
                } else {
                    isBrokenOnLastCommit = false
                    committers.clear()
                }
                analyzedData = getXMLStringToSave(buildAnalysis)
            }
            log.debug("Saving current analysis...")
            state.save(failOnError: true, flush: true)
        }
    }

    private String getXMLStringToSave(BuildAnalysis buildAnalysis) {
        GPathResult root = new XmlSlurper().parseText("<testsuites></testsuites>")
        buildAnalysis.currentlyBrokenTests?.each { GPathResult pathResult ->
            root.appendNode(pathResult)
        }
        new XmlUtil().serialize(root)
    }

    List<GPathResult> getCurrentlyBrokenTests(File xmlResult) {
        GPathResult pathResult = new XmlSlurper().parse(xmlResult)
        pathResult.children().findResults { GPathResult childPath ->
            if (childPath.@errors != "0" || childPath.@failures != "0") {
                childPath
            }
        } ?: []
    }

    List<GPathResult> getPreviouslyBrokenTests(String module) {
        BuildState.withNewSession {
            BuildState.findByModule(module)?.getXMLTree()?.children()?.collect { it } ?: []
        }
    }

    Boolean areAnyNewTestsBroken(List<GPathResult> currentlyBroken = [], List<GPathResult> previouslyBroken = []) {
        Boolean isBroken = false
        currentlyBroken?.any { GPathResult newResult ->
            GPathResult oldResult = previouslyBroken.find {
                it.@name == newResult.@name && it.@package == newResult.@package
            }
            if (oldResult) {
                Integer oldErrors = oldResult.@errors?.toInteger() ?: 0
                Integer oldFailures = oldResult.@failures?.toInteger() ?: 0
                if (newResult.@errors?.toInteger() > oldErrors || newResult.@failures?.toInteger() > oldFailures) {
                    isBroken = true
                } else {
                    List<String> oldTestNames = oldResult.children()*.@name
                    isBroken = (newResult.children()*.@name - oldTestNames) as Boolean
                }
                log.info("Found Old Result for ${oldResult.@package}, ${oldResult.@name}, Broken: ${isBroken}")
            } else {
                isBroken = true
            }
            isBroken
        }
        log.info("Is Broken: ${isBroken}")
        isBroken
    }

    @Transactional
    void updateScoreForBuild(BuildAnalysis buildAnalysis) {
        Integer totalScore = 0
        Rule.withNewSession {
            Integer totalErrorsAdded = buildAnalysis.getCurrentErrorCount() - buildAnalysis.previousErrorCount
            Integer errorScore = totalErrorsAdded * (Rule.findByRuleType(totalErrorsAdded > 0 ? ADDED_BROKEN_TESTS : FIXED_BROKEN_TESTS).points)
            Integer totalFailuresAdded = buildAnalysis.currentFailureCount - buildAnalysis.previousFailureCount
            Integer failureScore = totalFailuresAdded * (Rule.findByRuleType(totalFailuresAdded > 0 ? ADDED_BROKEN_TESTS : FIXED_BROKEN_TESTS).points)
            Integer buildScore = Rule.findByRuleType(buildAnalysis.isBroken ? BROKEN_BUILD : SAFE_BUILD).points
            totalScore = (totalErrorsAdded > 0 ? errorScore : errorScore * -1) + (totalFailuresAdded > 0 ? failureScore : failureScore * -1) + buildScore
            log.info("Total Errors: ${totalErrorsAdded}, Failures: ${totalFailuresAdded}, Build Score: ${buildScore}, Final Score: ${totalScore}")
        }
        Committer.withNewSession {
            Committer committer = Committer.findByEmail(configUtil.currentCommitter) ?: new Committer(email: configUtil.currentCommitter)
            committer.points += totalScore
            committer.save(flush: true, failOnError: true)
        }
    }

}
