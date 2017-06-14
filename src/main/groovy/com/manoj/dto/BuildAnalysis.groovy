package com.manoj.dto

import groovy.util.slurpersupport.GPathResult

class BuildAnalysis {
    String moduleName
    List<GPathResult> currentlyBrokenTests = []
    List<GPathResult> previouslyBrokenTests = []
    Boolean isBroken = false

    Integer getPreviousErrorCount() {
        previouslyBrokenTests*.@errors*.toInteger()?.sum { it } ?: 0
    }

    Integer getPreviousFailureCount() {
        previouslyBrokenTests*.@failures*.toInteger()?.sum { it } ?: 0
    }

    Integer getCurrentErrorCount() {
        currentlyBrokenTests*.@errors*.toInteger()?.sum { it } ?: 0
    }

    Integer getCurrentFailureCount() {
        currentlyBrokenTests*.@failures*.toInteger()?.sum { it } ?: 0
    }
}
