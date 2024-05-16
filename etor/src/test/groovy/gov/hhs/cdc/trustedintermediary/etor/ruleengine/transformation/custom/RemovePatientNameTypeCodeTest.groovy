package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import spock.lang.Specification

class RemovePatientNameTypeCodeTest extends Specification {
    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new RemovePatientNameTypeCode()
    }

    def "remove PID.5-7 from Bundle"() {
        given:
        def fhirResource = ExamplesHelper.getExampleFhirResource("../CA/002_CA_ORU_R01_initial_translation.fhir")
        def bundle = fhirResource.getUnderlyingResource() as Bundle
        def patient = HapiHelper.resourceInBundle(bundle, Patient) as Patient
        def pid5_7 = getPid5_7(bundle)

        expect:
        pid5_7 != null

        when:
        transformClass.transform(fhirResource, null)
        def removedPid5_7 = getPid5_7(bundle)

        then:
        removedPid5_7 == null
    }

    def "throw RuleExecutionException if patient resource not present"() {
        given:
        def bundle = new Bundle()
        HapiHelper.createMessageHeader(bundle)

        when:
        transformClass.transform(new HapiFhirResource(bundle), null)

        then:
        thrown(RuleExecutionException)
    }

    def getPid5_7(Bundle bundle) {
        def patient = HapiHelper.resourceInBundle(bundle, Patient) as Patient
        def patientName = patient.getName()
        def extension = patientName.get(0).getExtensionByUrl("https://reportstream.cdc.gov/fhir/StructureDefinition/xpn-human-name")
        if (!extension.hasExtension("XPN.7"))
            return null
        return patientName.get(0).getExtensionByUrl("https://reportstream.cdc.gov/fhir/StructureDefinition/xpn-human-name").getExtensionByUrl("XPN.7").getValue()
    }
}
