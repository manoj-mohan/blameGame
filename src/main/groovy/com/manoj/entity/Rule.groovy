package com.manoj.entity

import com.manoj.enums.RuleType
import grails.gorm.annotation.Entity

@Entity
class Rule {
    RuleType ruleType
    Double points = 0
}
