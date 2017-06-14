package com.manoj.entity

import grails.gorm.annotation.Entity
import groovy.transform.ToString

@ToString
@Entity
class Committer {
    String email
    Double points = 0
    Date dateCreated
    Date lastUpdated
}
