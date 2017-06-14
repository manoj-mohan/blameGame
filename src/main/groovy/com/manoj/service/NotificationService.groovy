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

    public void sendBrokenBuildEmail(BuildState state) {
        Map<String, String> contentMap = [
                ("${Placeholder.COMMITTER_LIST}")         : state.committers,
                ("${Placeholder.MODULE_NAME}")            : state.module,
                ("${Placeholder.CURRENT_TEST_RESULT_URL}"): "",
                ("${Placeholder.COMMON_TEST_RESULT_URL}") : "",
        ]
        log.debug("Sending Failure Mail for params: ${contentMap}")
        String body = Placeholder.getPopulatedContent(contentMap, configUtil.getBrokenBuildMailBody())
        mailerUtil.instance.sendMail(state.lastBrokenBy, configUtil.brokenBuildMailSubject, body)
    }


}
