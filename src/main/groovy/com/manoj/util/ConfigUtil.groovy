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

    Map<RuleType, Double> getScoreRules() {
        getConfig().scoring.rules
    }

    List<String> getCCList() {
        getConfig().mail.ccList
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

}
