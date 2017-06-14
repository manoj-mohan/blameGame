package com.manoj.entity

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import groovy.util.slurpersupport.GPathResult

@ToString
@Entity
class BuildState {
    String module
    String analyzedData
    String lastBrokenBy
    String lastBrokenCommitHash
    Boolean isBrokenOnLastCommit = false
    Date dateCreated
    Date lastUpdated

    static hasMany = [committers: String]


    static mapping = {
        analyzedData type: 'text'
    }

    static constraints = {
        analyzedData nullable: true
        lastBrokenBy nullable: true, email: true
        lastBrokenCommitHash nullable: true
    }

    GPathResult getXMLTree() {
        analyzedData ? new XmlSlurper().parseText(analyzedData) : null
    }

    Integer getErrorCount() {
        analyzedData*.@errors*.toInteger()?.sum { it } ?: 0
    }

    Integer getFailureCount() {
        analyzedData*.@failures*.toInteger()?.sum { it } ?: 0
    }


}

