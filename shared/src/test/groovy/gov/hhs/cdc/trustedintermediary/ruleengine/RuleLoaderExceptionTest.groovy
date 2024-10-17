package gov.hhs.cdc.trustedintermediary.ruleengine

import gov.hhs.cdc.trustedintermediary.wrappers.HttpClientException
import spock.lang.Specification

class RuleLoaderExceptionTest extends Specification {

    def "constructor works"() {
        given:
        def message = "rules loaded wrong!"
        def cause = new HttpClientException(message, new IOException())

        when:
        def exceptionWithCause = new RuleLoaderException(message, cause)
        def exceptionWithout = new RuleLoaderException(message, new Exception())

        then:
        exceptionWithCause.getMessage() == message
        exceptionWithCause.getCause() == cause
        exceptionWithout.getMessage() == message
    }
}
