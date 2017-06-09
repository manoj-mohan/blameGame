package com.manoj.entity

import grails.gorm.annotation.Entity

@Entity
class Committer {
    String email
    Double points = 0
}
