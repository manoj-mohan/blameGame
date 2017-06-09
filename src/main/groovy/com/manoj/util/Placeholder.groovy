package com.manoj.util

class Placeholder {
    static final String MODULE_NAME = "[MODULE_NAME]"
    static final String COMMITTER_LIST = "[COMMITTER_LIST]"
    static final String CURRENT_TEST_RESULT_URL = "[CURRENT_TEST_RESULT_URL]"
    static final String COMMON_TEST_RESULT_URL = "[COMMON_TEST_RESULT_URL]"
    static final String EMPTY_PLACEHOLDER_REPLACEMENT_REGEX = "(\\[[A-Z]*_?[A-Z]*\\])"

    static String getPopulatedContent(Map parameters, String bodyTemplate, Boolean replaceEmptyPlaceHolders = true) {
        for (Object key : parameters.keySet()) {
            if (bodyTemplate.contains(key)) {
                String value = (String) parameters.get(key)
                if (value != null) {
                    bodyTemplate = bodyTemplate.replace(key, value ?: "N/A")
                }
            }
        }
        return (replaceEmptyPlaceHolders ? bodyTemplate.replaceAll(EMPTY_PLACEHOLDER_REPLACEMENT_REGEX, "N/A") : bodyTemplate)
    }

}
