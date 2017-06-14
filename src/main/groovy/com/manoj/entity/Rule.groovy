package com.manoj.entity

import com.manoj.enums.RuleType
import grails.gorm.annotation.Entity
import groovy.transform.ToString

@ToString
@Entity
class Rule {
    RuleType ruleType
    Double points = 0
    Date dateCreated
    Date lastUpdated
}
