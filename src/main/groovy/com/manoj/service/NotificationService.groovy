package com.manoj.service

import com.manoj.entity.BuildState
import com.manoj.util.ConfigUtil
import com.manoj.util.MailerUtil
import com.manoj.util.Placeholder
import groovy.util.logging.Slf4j

@Slf4j
@Singleton
class NotificationService {

    ConfigUtil configUtil = ConfigUtil.instance
    MailerUtil mailerUtil = MailerUtil.instance

    void sendBrokenBuildEmail(BuildState state) {
        Map<String, String> contentMap = [
                (Placeholder.COMMITTER_LIST)         : state.committers.join(System.getProperty("line.separator")),
                (Placeholder.MODULE_NAME)            : state.module,
                (Placeholder.COMMIT_HASH)            : configUtil.getCurrentCommitHash(),
                (Placeholder.CURRENT_TEST_RESULT_URL): configUtil.getJenkinsBuildURL(state.module),
                (Placeholder.COMMON_TEST_RESULT_URL) : configUtil.getJenkinsJobURL(),
        ]
        log.debug("Sending Failure Mail for params: ${contentMap}")
        String body = Placeholder.getPopulatedContent(contentMap, configUtil.getBrokenBuildMailBody())
        String subject = Placeholder.getPopulatedContent(contentMap, configUtil.brokenBuildMailSubject)
        mailerUtil.instance.sendMail(state.lastBrokenBy, subject, body)
    }

    void sendErrorMail(Throwable throwable) {
        Map<String, String> contentMap = [
                (Placeholder.ERROR)      : throwable.getMessage(),
                (Placeholder.STACKTRACE) : throwable.getStackTrace().toString(),
                (Placeholder.COMMITTER)  : configUtil.currentCommitter,
                (Placeholder.MODULE_NAME): configUtil.currentModule
        ]
        log.debug("Sending ERROR Mail for params: ${contentMap}")
        String body = Placeholder.getPopulatedContent(contentMap, configUtil.errorAlertBody)
        String subject = Placeholder.getPopulatedContent(contentMap, configUtil.errorAlertSubject)
        mailerUtil.instance.sendMail(configUtil.errorAlerter, subject, body)
    }
}
