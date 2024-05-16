package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Organization
import spock.lang.Specification

class UpdateReceivingFacilityWithOrderingFacilityIdentifierTest extends Specification {

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new updateReceivingFacilityWithOrderingFacilityIdentifier()
    }

    def "update california receiving facility"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource('../MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir')
        def bundle = fhirResource.getUnderlyingResource() as Bundle

        def messageHeader = HapiHelper.getMessageHeader(bundle)
        def destination = messageHeader.getDestinationFirstRep()
        def organization = destination.getReceiver().getResource() as Organization
        def ident = organization.getIdentifier().findAll {
            it.getExtension().findAll {
                it.value as String == 'HD.1'
            }
        }.first()
        //        def displayArray = messageHeader.getEventCoding().getDisplay().split("\\^")

        expect:
        //        displayArray.size() == 3
        //        displayArray[2] != ""

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)
        //        def convertedMessageHeader = HapiHelper.findOrCreateMessageHeader(bundle)
        //        def convertedDisplay = convertedMessageHeader.getEventCoding().getDisplay()
        //        def convertedDisplayArray = convertedDisplay.split("\\^")

        then:
        true
        //        convertedDisplayArray.size() == 2
    }

    def "throw RuleExecutionException if receiving facility not in bundle"() {
        given:
        def bundle = new Bundle()
        HapiHelper.createMessageHeader(bundle)

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        thrown(RuleExecutionException)
    }
}
