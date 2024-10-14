package gov.hhs.cdc.trustedintermediary.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.HealthDataExpressionEvaluator
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class RuleLoaderTest extends Specification {

    String fileContents
    Path tempFile

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(HealthDataExpressionEvaluator, Mock(HealthDataExpressionEvaluator))
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        tempFile = Files.createTempFile("test_validation_definition", ".json")
    }

    def cleanup(){
        Files.deleteIfExists(tempFile)
    }

    def "load rules from file"() {
        given:
        fileContents = """
        {
            "definitions": [
                {
                    "name": "patientName",
                    "description": "a test rule",
                    "message": "testing the message",
                    "conditions": ["Patient.name.exists()"],
                    "rules": ["Patient.name.where(use='usual').given.exists()"]
                }
            ]
        }
        """
        Files.writeString(tempFile, fileContents)

        when:
        List<Rule> rules = RuleLoader.getInstance().loadRules(Files.newInputStream(tempFile), new TypeReference<Map<String, List<Rule>>>() {})

        then:
        rules.size() == 1
        Rule rule = rules.get(0) as Rule
        rule.getName() == "patientName"
        rule.getDescription() == "a test rule"
        rule.getMessage() == "testing the message"
        rule.getConditions() == ["Patient.name.exists()"]
        rule.getRules() == [
            "Patient.name.where(use='usual').given.exists()"
        ]
    }

    def "handle FormatterProcessingException when loading rules from a non existent file"() {
        given:
        Files.writeString(tempFile, "!K@WJ#8uhy")

        when:
        RuleLoader.getInstance().loadRules(Files.newInputStream(tempFile), new TypeReference<Map<String, List<Rule>>>() {})

        then:
        thrown(RuleLoaderException)
    }
}
