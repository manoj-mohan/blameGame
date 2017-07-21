package com.manoj.util

import com.manoj.enums.RuleType


@Singleton
class ConfigUtil {

    private ConfigObject config

    void initialize(String configFilePath) {
        if (!config) {
            String filePath = configFilePath
            config = new ConfigSlurper().parse(new File(filePath).toURL());
        }
    }

    ConfigObject getConfig() {
        config
    }

    Map<String, String> modulesToExecute() {
        getConfig().modules
    }

    void setCurrentCommitter(String email) {
        getConfig().committer = email
    }

    String getCurrentCommitter() {
        getConfig().committer
    }

    void setCurrentModule(String module) {
        getConfig().currentModule = module
    }

    String getCurrentModule() {
        getConfig().currentModule
    }

    Map<RuleType, Double> getScoreRules() {
        getConfig().scoring.rules
    }

    List<String> getCCList() {
        getConfig().mail.configuation.ccList
    }

    String getMailSMTPHost() {
        getConfig().mail.configuation.smtpHost
    }

    String getBrokenBuildMailBody() {
        getConfig().mail.templates.brokenBuild.body
    }

    String getBrokenBuildMailSubject() {
        getConfig().mail.templates.brokenBuild.subject
    }

    Map<String, String> getDataSourceConfig() {
        getConfig().dataSourceProperties
    }

    String getRawResultsDirectoryPath() {
        getConfig().rawResultDirectory
    }

    String setCurrentCommitHash(String hash) {
        getConfig().currentCommitHash = hash
    }

    String getCurrentCommitHash() {
        getConfig().currentCommitHash
    }

    void setJenkinsBuildBaseURL(String basePath) {
        config.jenkins.build.baseURL = "${basePath}"
    }

    String getJenkinsBuildURL(String moduleName) {
        "${config.jenkins.build.baseURL}/Nimbus_${moduleName.tokenize("-").last()?.capitalize()}_Unit_Test_Report"
    }

    String getJenkinsJobURL() {
        config.jenkins.jobURL
    }

    String getErrorAlerter() {
        getConfig().mail.templates.exception.to
    }

    String getErrorAlertSubject() {
        getConfig().mail.templates.exception.subject
    }

    String getErrorAlertBody() {
        getConfig().mail.templates.exception.body
    }
}
