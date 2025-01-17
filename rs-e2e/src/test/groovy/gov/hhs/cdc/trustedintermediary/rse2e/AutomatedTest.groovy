package gov.hhs.cdc.trustedintermediary.rse2e

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.rse2e.external.azure.AzureBlobFileFetcher
import gov.hhs.cdc.trustedintermediary.rse2e.hl7.HL7FileStream
import gov.hhs.cdc.trustedintermediary.rse2e.hl7.HL7FileMatcher
import gov.hhs.cdc.trustedintermediary.rse2e.hl7.HL7ExpressionEvaluator
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.rse2e.external.localfile.LocalFileFetcher
import gov.hhs.cdc.trustedintermediary.wrappers.HealthDataExpressionEvaluator
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.ruleengine.RuleLoader
import gov.hhs.cdc.trustedintermediary.rse2e.ruleengine.AssertionRuleEngine
import spock.lang.Specification

class AutomatedTest extends Specification  {

    List<HL7FileStream> azureFiles
    List<HL7FileStream> localFiles
    AssertionRuleEngine engine
    HL7FileMatcher fileMatcher
    Logger mockLogger = Mock(Logger)
    List<String> loggedErrorsAndWarnings = []
    List<String> failedFiles = []
    FileFetcher azureFileFetcher
    FileFetcher localFileFetcher

    def setup() {
        engine = AssertionRuleEngine.getInstance()
        fileMatcher = HL7FileMatcher.getInstance()

        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(AssertionRuleEngine, engine)
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HL7FileMatcher, fileMatcher)
        TestApplicationContext.register(HealthDataExpressionEvaluator, HL7ExpressionEvaluator.getInstance())
        TestApplicationContext.register(LocalFileFetcher, LocalFileFetcher.getInstance())
        TestApplicationContext.register(AzureBlobFileFetcher, AzureBlobFileFetcher.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        mockLogger.logWarning(_ as String, _ as Object) >> { String msg, Object args ->
            loggedErrorsAndWarnings << msg
        }
        mockLogger.logError(_ as String, _ as Exception) >> { String msg, Exception e ->
            loggedErrorsAndWarnings << msg
        }

        azureFileFetcher = AzureBlobFileFetcher.getInstance()
        localFileFetcher = LocalFileFetcher.getInstance()


        engine.ensureRulesLoaded()
    }

    def cleanup() {
        for (HL7FileStream fileStream : localFiles + azureFiles) {
            fileStream.inputStream().close()
        }
    }

    def "test defined assertions on relevant messages"() {
        given:
        azureFiles = azureFileFetcher.fetchFiles(FileFetcherEnum.ASSERTION)
        localFiles = localFileFetcher.fetchFiles(FileFetcherEnum.ASSERTION)
        def rulesToEvaluate = engine.getRules()
        def matchedFiles = fileMatcher.matchFiles(azureFiles, localFiles)

        when:
        for (messagePair in matchedFiles) {
            def inputMessage = messagePair.getKey()
            def outputMessage = messagePair.getValue()
            def evaluatedRules = engine.runRules(outputMessage, inputMessage)
            rulesToEvaluate.removeAll(evaluatedRules)
        }

        then:
        rulesToEvaluate.collect { it.name }.isEmpty() //Check whether all the rules in the assertions file have been run
        if (!loggedErrorsAndWarnings.isEmpty()) {
            throw new AssertionError("Unexpected errors and/or warnings were logged:\n- ${loggedErrorsAndWarnings.join('\n- ')}")
        }
    }

    def "test golden copy files on actual files"() {
        given:
        azureFiles = azureFileFetcher.fetchFiles(FileFetcherEnum.GOLDEN_COPY)
        localFiles = localFileFetcher.fetchFiles(FileFetcherEnum.GOLDEN_COPY)
        def matchedFiles = fileMatcher.matchFiles(azureFiles, localFiles)

        when:
        for (filePair in matchedFiles) {
            def actualFile = filePair.getKey()
            def expectedFile = filePair.getValue()
            if (actualFile.toString() != expectedFile.toString()) {
                failedFiles.add(expectedFile.getIdentifier())
            }
        }

        then:
        assert failedFiles.isEmpty()
        if (!loggedErrorsAndWarnings.isEmpty()) {
            throw new AssertionError("Unexpected errors and/or warnings were logged:\n- ${loggedErrorsAndWarnings.join('\n- ')}")
        }
    }
}
