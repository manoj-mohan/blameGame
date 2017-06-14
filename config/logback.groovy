import ch.qos.logback.classic.*
import ch.qos.logback.classic.*

import static ch.qos.logback.classic.Level.*

scan("60 seconds")
def logFile = "/home/manoj/Projects/POC/BlameGame/logs/BlameGame.log"

appender("Console-Appender", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%msg%n"
    }
}
appender("File-Appender", FileAppender) {
    file = "${logFile}"
    encoder(PatternLayoutEncoder) {
        pattern = "%msg%n"
        outputPatternAsHeader = true
    }
}

logger("com.manoj", INFO, ["File-Appender"])
root(INFO, ["Console-Appender"])