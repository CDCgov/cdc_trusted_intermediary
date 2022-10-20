package gov.hhs.cdc.trustedintermediary.external.javalin

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse
import io.javalin.http.Context
import spock.lang.Specification

class AppTest extends Specification {
    def "convert Javalin Context to DomainRequest correctly"() {
        given:

        def bodyString = "DogCow"
        def urlString = "Moof"
        def headerMap = [
            "Clarus": "is a DogCow"
        ]

        def javalinContext = Mock(Context)
        javalinContext.body() >> bodyString
        javalinContext.url() >> urlString
        javalinContext.headerMap() >> headerMap

        when:
        def domainRequest = App.javalinContextToDomainRequest(javalinContext)

        then:
        domainRequest.getBody() == bodyString
        domainRequest.getUrl() == urlString
        domainRequest.getHeaders() == headerMap
    }

    def "Inject values from DomainResponse into a Javalin Context correctly"() {
        given:

        def statusCode = 418
        def bodyString = "Moof"
        def headerMap = [
            "Clarus": "DogCow",
            "Two": "Another time"
        ]

        def response = new DomainResponse(statusCode)
        response.setBody(bodyString)
        response.setHeaders(headerMap)

        def javalinContext = Mock(Context)
        def savedResult = null
        def savedStatusCode = null
        def savedHeaders = [:]
        javalinContext.result(_ as String) >> { String result ->
            savedResult = result
            return
        }
        javalinContext.status(_ as Integer) >> { int status ->
            savedStatusCode = status
            return
        }
        javalinContext.header(_ as String, _ as String) >> { String key, String value -> savedHeaders.put(key, value) }

        when:
        App.domainResponseFillsInJavalinContext(response, javalinContext)

        then:
        savedResult == bodyString
        savedStatusCode == statusCode
        savedHeaders == headerMap
    }
}
