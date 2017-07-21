import com.manoj.enums.RuleType
import com.manoj.util.Placeholder

modules = [
//        <moduleName> : <absolute_path_of_coverage.xml_file>
"nimbus4-core"   : "/home/manoj/Projects/Westcon/nimbus4/nimbus4-core/target/test-reports/TESTS-TestSuites.xml",
"nimbus4-admin"  : "/home/manoj/Projects/Westcon/nimbus4/nimbus4-admin/target/test-reports/TESTS-TestSuites.xml",
"nimbus4-api"    : "/home/manoj/Projects/Westcon/nimbus4/nimbus4-api/target/test-reports/TESTS-TestSuites.xml",
"nimbus4-backend": "/home/manoj/Projects/Westcon/nimbus4/nimbus4-backend/target/test-reports/TESTS-TestSuites.xml",
"nimbus-shell"   : "/home/manoj/Projects/Westcon/nimbus4/nimbus-shell/target/test-reports/TESTS-TestSuites.xml"
]

scoring {
    rules = [
            (RuleType.SAFE_BUILD)             : 1,
            (RuleType.BROKEN_BUILD)           : -10,
            (RuleType.INCREASED_TEST_COVERAGE): 1,
            (RuleType.REDUCED_TEST_COVERAGE)  : -1,
            (RuleType.ADDED_BROKEN_TESTS)     : -3,
            (RuleType.FIXED_BROKEN_TESTS)     : 3
    ]
    rebuildPointsOnChange = false
}


dataSourceProperties = [
        'dataSource.username'       : "root",
        'dataSource.password'       : "admin",
        'dataSource.dbCreate'       : "update", // one of 'create', 'create-drop', 'update', 'validate',''
        'dataSource.url'            : "jdbc:mysql://localhost:3306/blameGame?autoReconnect=true",
        'dataSource.logSql'         : false,
        'dataSource.pooled'         : true,
        'dataSource.jmxExport'      : true,
        'dataSource.driverClassName': "com.mysql.jdbc.Driver",
        'dataSource.dialect'        : "org.hibernate.dialect.MySQL5InnoDBDialect",
        'logSql'                    : false,
        'dataSource.properties'     : [
                'jmxEnabled'                   : true,
                'initialSize'                  : 5,
                'minIdle'                      : 10,
                'maxActive'                    : 100,
                'maxWait'                      : 15000, // (15 Seconds),
                'maxAge'                       : 60000, // (60 Seconds) Previously it was set to 10 * 60000,
                'minEvictableIdleTimeMillis'   : 120000, // (2 Minutes) Old Settings (30 Minutes),
                'timeBetweenEvictionRunsMillis': 10000, //(10 Seconds) Old Settings (30 Minutes),
                'numTestsPerEvictionRun'       : 3,
                'testOnBorrow'                 : true,
                'testWhileIdle'                : true,
                'testOnReturn'                 : true,
                'validationQuery'              : "SELECT 1",
                '///validationQueryTimeout'    : 3, //TODO : Enabling it cause MySQL Cancellation Timer : WAITING THREAD Unsafe.park,
                'validationInterval'           : 15000,
                'abandonWhenPercentageFull'    : 100, // settings are active only when pool is full,
                'removeAbandonedTimeout'       : 120,
                'removeAbandoned'              : true,
                'logAbandoned'                 : true //Only for testing purpose remove on Production
        ]
]

/***
 *  MAIL SETTINGS
 ***/
mail {
    configuation {
        from = "intelligrape@gmail.com"
        ccList = ["manoj.mohan@tothenew.com"]
        smtpHost = "10.0.1.165"
    }
    templates {
        brokenBuild {
            subject = "Unit Test Cases Broken for [${Placeholder.MODULE_NAME}]"
            body = """
                  
 Hey,
 
    You are receiving this auto-generated mail as Unit Test Cases for the module have been broken by your last commit (${Placeholder.COMMIT_HASH}). 

    Please fix the testcases ASAP as this commit drastically reduces our quality index.
    
    Here is a list of people who have "contributed" in making this build fail
    ${Placeholder.COMMITTER_LIST}                       
    
    To find all breaking cases currently, please visit:
    ${Placeholder.CURRENT_TEST_RESULT_URL}


    PS: If the above URL doesn't work, the report has been deleted by Jenkins as part of housekeeping. Please visit the below URL and pull out the report from the latest build.
    ${Placeholder.COMMON_TEST_RESULT_URL}
"""
        }

        exception {
            to = "manoj.mohan@tothenew.com"
            subject = "Unable to complete Analysis for Module []"
            body = """
                    Hi,
                        Unfortunately we were not able to finish analysis of [COMMITTER] 's commits for the module [MODULE_NAME].
                    Please contact manoj.mohan@tothenew.com to get it resolved ASAP.
                    
                    
                    ERROR DETAILS
                    ----------------------------------
                    Error: [ERROR]

                    Details
                    -------------
                    [STACKTRACE]

                    """
        }
    }
}

jenkins.jobURL = "https://jenkins2.verecloud.com/job/test_unit/job/Branch_Integration_Unit_Test/"


rawResultDirectory = "/Users/manoj/Projects/POC/BlameGame/rawResults"