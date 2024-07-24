package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import spock.lang.Specification

class ReportStreamSenderHelperTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(ReportStreamSenderHelper, ReportStreamSenderHelper.getInstance())
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
    }

    def "sendToReportStream works"() {
        given:
        def requestBody = "testBody"
        def bearerToken = "fake-token"
        def responseBody = """{"reportId": "fake-id"}"""
        def messageType = PartnerMetadataMessageType.ORDER

        def mockFormatter = Mock(Formatter)
        TestApplicationContext.register(Formatter, mockFormatter)

        def mockRsClient = Mock(RSEndpointClient)
        TestApplicationContext.register(RSEndpointClient, mockRsClient)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamSenderHelper.getInstance().sendToReportStream(requestBody, _ as String, messageType)

        then:
        1 * mockRsClient.getRsToken() >> "fake-token"
        1 * mockRsClient.requestWatersEndpoint(requestBody, bearerToken) >> responseBody
        1 * mockFormatter.convertJsonToObject(responseBody, _ as TypeReference) >> [reportId: "fake-id"]
        1 * ReportStreamSenderHelper.getInstance().metadata.put(_, EtorMetadataStep.SENT_TO_REPORT_STREAM)
    }

    def "sendOrderToReportStream works"() {
        setup:
        def body = "testBody"
        def fhirResourceId = "testId"
        def expected = Optional.of("result")
        def messageType = PartnerMetadataMessageType.ORDER

        def senderHelper = Spy(ReportStreamSenderHelper.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def order = senderHelper.sendOrderToReportStream(body, fhirResourceId)

        then:
        order.get() == expected.get()
        1 * senderHelper.sendToReportStream(body, fhirResourceId, messageType) >> expected
    }

    def "sendResultToReportStream works"() {
        setup:
        def body = "testBody"
        def fhirResourceId = "testId"
        def expected = Optional.of("result")
        def messageType = PartnerMetadataMessageType.RESULT

        def senderHelper = Spy(ReportStreamSenderHelper.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def result = senderHelper.sendResultToReportStream(body, fhirResourceId)

        then:
        result.get() == expected.get()
        1 * senderHelper.sendToReportStream(body, fhirResourceId, PartnerMetadataMessageType.RESULT) >> expected
    }

    def "sendToReportStream throws exception if RS client fails"() {
        setup:
        def mockEndpointClient = Mock(RSEndpointClient)
        mockEndpointClient.getRsToken() >> { throw new ReportStreamEndpointClientException("couldn't get token", new Exception())}
        TestApplicationContext.register(RSEndpointClient, mockEndpointClient)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamSenderHelper.getInstance().sendToReportStream("testBody", "testId", PartnerMetadataMessageType.ORDER)

        then:
        thrown(UnableToSendMessageException)
    }

    def "getReportId logs reportId if convertJsonToObject is successful"() {
        given:
        def mockReportId = "fake-id"
        def mockResponseBody = """{"reportId": "${mockReportId}", "key": "value"}"""

        TestApplicationContext.register(Formatter, Jackson.getInstance())

        def mockLogger = Mock(Logger)
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def reportId = ReportStreamSenderHelper.getInstance().getReportId(mockResponseBody)

        then:
        reportId.get() == mockReportId
    }

    def "getReportId logs error if convertJsonToObject fails"() {
        given:
        def mockResponseBody = '{"reportId": "fake-id", "key": "value"}'
        def exception = new FormatterProcessingException("couldn't convert json", new Exception())

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> { throw exception }
        TestApplicationContext.register(Formatter, mockFormatter)

        def mockLogger = Mock(Logger)
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamSenderHelper.getInstance().getReportId(mockResponseBody)

        then:
        1 * mockLogger.logError(_ as String, exception)
    }

    def "sendToReportStream correctly logs errors when it lacks a ReportId or it is blank"() {
        given:
        def requestBody = "testBody"
        def bearerToken = "fake-token"
        def responseBody = """{"reportId": ""}"""
        def messageType = PartnerMetadataMessageType.ORDER

        def mockFormatter = Mock(Formatter)
        TestApplicationContext.register(Formatter, mockFormatter)

        def mockRsClient = Mock(RSEndpointClient)
        TestApplicationContext.register(RSEndpointClient, mockRsClient)

        def mockLogger = Mock(Logger)
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()

        mockRsClient.getRsToken() >> "fake-token"
        mockRsClient.requestWatersEndpoint(requestBody, bearerToken) >> responseBody

        when:
        mockFormatter.convertJsonToObject(responseBody, _ as TypeReference) >> ["": ""]
        ReportStreamSenderHelper.getInstance().sendToReportStream(requestBody, _ as String, messageType)

        then:
        1 * mockLogger.logError("Unable to retrieve ReportId from ReportStream response")

        when:
        mockFormatter.convertJsonToObject(responseBody, _ as TypeReference) >> ["reportId": ""]
        ReportStreamSenderHelper.getInstance().sendToReportStream(requestBody, _ as String, messageType)

        then:
        1 * mockLogger.logError("Unable to retrieve ReportId from ReportStream response")
    }
}
