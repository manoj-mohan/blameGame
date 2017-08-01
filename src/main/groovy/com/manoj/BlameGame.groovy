package com.manoj

import com.manoj.dto.BuildAnalysis
import com.manoj.entity.BuildState
import com.manoj.entity.Committer
import com.manoj.entity.Rule
import com.manoj.enums.RuleType
import com.manoj.service.AnalyzerService
import com.manoj.service.NotificationService
import com.manoj.util.ConfigUtil
import grails.orm.bootstrap.HibernateDatastoreSpringInitializer
import grails.persistence.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BlameGame {

    static Logger log = LoggerFactory.getLogger(this);
    static ConfigUtil configUtil = ConfigUtil.instance
    static AnalyzerService analyzerService = AnalyzerService.instance
    static NotificationService notificationService = NotificationService.instance

    static void main(String[] args) {
        if (args?.size() < 4) {
            throw new RuntimeException("PROGRAM EXECUTION INTERRUPTED !! MISSING CERTAIN ARGUMENTS  (Size is ${args?.size()} as opposed to minimum 4. Passed arguments are ${args})")
        }
        try {
            initializeApplication(args);
            log.info "****************** Starting Blame Game *******************"
            goBlame(args[2])
            log.info "***************** Blame Game Completed ********************"
        } catch (Exception e) {
            e.printStackTrace()
            notificationService.sendErrorMail(e)
        }
    }

    private static void initializeApplication(String[] arguments) {
        setupConfigAndLogging(arguments[0])
        configureDataSource()
        bootstrapConfigData()
        setExecutionParameters(arguments as List)
        sanitizeRawResults()
    }

    private static void setupConfigAndLogging(String configDirectory) {
        File file = new File("${configDirectory}")
        if (file.exists()) {
            configUtil.initialize("${configDirectory}/ApplicationConfig.groovy")
            System.setProperty("logback.configurationFile", "${configDirectory}/logback.groovy")
        } else {
            throw new FileNotFoundException("Config directory not specified correctly. Please check !!")
        }
    }

    private static void configureDataSource() {
        log.info("Setup DataSource")
        def init = new HibernateDatastoreSpringInitializer(configUtil.getDataSourceConfig(),
                Rule, BuildState, Committer
        )
        init.configure()
    }

    private static void sanitizeRawResults() {
        File file = new File(configUtil.getRawResultsDirectoryPath())
        if (file.exists() && file.isDirectory()) {
            //clean up older files later on
        } else {
            file.mkdirs()
        }
    }

    private static void setExecutionParameters(List<String> arguments) {
        configUtil.currentCommitter = arguments.getAt(1)
        configUtil.currentCommitHash = arguments.getAt(2)
        configUtil.jenkinsBuildBaseURL = arguments.getAt(3)
        configUtil.basePathForFilesToAnalyze = arguments.getAt(4)
    }

    private static void bootstrapConfigData() {
        log.info("Bootstrapping module(s)")
        configUtil.modulesToExecute().each { String module, String coverageFilePath ->
            BuildState.withNewSession {
                BuildState.countByModule(module) ?: new BuildState(module: module).save(flush: true)
            }
        }
        log.info("Loading rules")
        configUtil.getScoreRules().each { RuleType ruleType, Double points ->
            Rule.withNewSession {
                Rule rule = Rule.findByRuleType(ruleType) ?: new Rule(ruleType: ruleType)
                rule.points = points
                rule.save(flush: true)
            }
        }
    }

    static void goBlame(String commitHash) {
        configUtil.modulesToExecute().each { String moduleName, String coverageFilePath ->
            configUtil.currentModule = moduleName
            coverageFilePath = "${configUtil.basePathForFilesToAnalyze}${coverageFilePath}"
            log.info("Running analysis for ${moduleName}, Path: ${coverageFilePath}")
            if (analyzerService.doesFileExistForAnalysis(coverageFilePath)) {
                BuildAnalysis buildAnalysis = analyzerService.analyzeTestCases(moduleName, coverageFilePath)
                BuildState state = analyzerService.storeCurrentAnalysis(buildAnalysis)
                analyzerService.updateScoreForBuild(buildAnalysis)
                if (buildAnalysis.isBroken) {
                    notificationService.sendBrokenBuildEmail(state)
                }
                saveRawResults(moduleName, coverageFilePath, commitHash)
            } else {
                log.warn("Test Suite for ${moduleName} does not exist ... Skipping BuildState!!!")
            }
        }
        log.info("Completed Blaming everybody but me !!")
    }

    static void saveRawResults(String moduleName, String coverageFilePath, String commitHash) {
        log.info("Saving raw results for hash ${commitHash}, ${coverageFilePath}")
        try {
            String parentDirectory = "${configUtil.getRawResultsDirectoryPath()}/${commitHash}"
            new File("${parentDirectory}").mkdirs()
            File file = new File("${parentDirectory}/${moduleName}")
            file.text = new File(coverageFilePath).text
        } catch (Exception e) {
            log.error("Error while saving RAW results..")
            e.printStackTrace()
        }
    }


}
