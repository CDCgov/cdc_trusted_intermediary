package gov.hhs.cdc.trustedintermediary.rse2e.external.azure


import spock.lang.Specification

import java.time.LocalDate

class AzureBlobHelperTest extends Specification {

    def "buildDatePathPrefix should create correct path format"() {
        given:
        def date = LocalDate.of(2024, 3, 15)

        when:
        def result = AzureBlobHelper.buildDatePathPrefix(date)

        then:
        result == "2024/03/15/"
    }

    def "createDateBasedPath should combine date prefix with filename"() {
        given:
        def date = LocalDate.of(2024, 3, 15)
        def fileName = "test.hl7"

        when:
        def result = AzureBlobHelper.createDateBasedPath(date, fileName)

        then:
        result == "2024/03/15/test.hl7"
    }

    def "isInDateFolder should return true for matching date folder"() {
        given:
        def date = LocalDate.of(2024, 3, 15)
        def path = "2024/03/15/Assertion/test.hl7"
        def testType = "Assertion/"

        expect:
        AzureBlobHelper.isInDateFolder(testType, path, date)
    }
}
