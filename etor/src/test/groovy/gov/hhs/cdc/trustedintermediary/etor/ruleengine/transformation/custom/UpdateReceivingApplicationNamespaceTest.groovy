package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.StringType
import spock.lang.Specification

class UpdateReceivingApplicationNamespaceTest extends Specification {

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new UpdateReceivingApplicationNamespace()
    }

    def "update receiving application namespace to given name and remove Universal Id and Universal Id Type "() {
        given:
        def name = "EPIC"
        def bundle = new Bundle()
        HapiHelper.createMessageHeader(bundle)
        def receivingApplication = HapiHelper.createMessageDestinationComponent()
        receivingApplication.addExtension(HapiHelper.EXTENSION_UNIVERSAL_ID_URL, new StringType("universal-id"))
        receivingApplication.addExtension(HapiHelper.EXTENSION_UNIVERSAL_ID_TYPE_URL, new StringType("universal-id-type"))
        HapiHelper.setReceivingApplication(bundle, receivingApplication)

        expect:
        def existingReceivingApplication = HapiHelper.getReceivingApplication(bundle)
        existingReceivingApplication.name != name
        existingReceivingApplication.getExtensionByUrl(HapiHelper.EXTENSION_UNIVERSAL_ID_URL) != null
        existingReceivingApplication.getExtensionByUrl(HapiHelper.EXTENSION_UNIVERSAL_ID_TYPE_URL) != null

        when:
        transformClass.transform(new HapiFhirResource(bundle), Map.of("name", name))
        def transformedReceivingApplication = HapiHelper.getReceivingApplication(bundle)

        then:
        transformedReceivingApplication.name == name
        transformedReceivingApplication.getExtensionByUrl(HapiHelper.EXTENSION_UNIVERSAL_ID_URL) == null
        transformedReceivingApplication.getExtensionByUrl(HapiHelper.EXTENSION_UNIVERSAL_ID_TYPE_URL) == null
    }

    def "don't throw exception if receiving application not in bundle"() {
        given:
        def bundle = new Bundle()
        HapiHelper.createMessageHeader(bundle)

        when:
        transformClass.transform(new HapiFhirResource(bundle), Map.of("name", ""))

        then:
        noExceptionThrown()
    }
}
