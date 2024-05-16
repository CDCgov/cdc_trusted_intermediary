package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;

/** Removes Name Type Code (PID-5.7) from Patient Name (PID-5). */
public class RemovePatientNameTypeCode implements CustomFhirTransformation {

    @Override
    public void transform(final FhirResource<?> resource, final Map<String, String> args)
            throws RuleExecutionException {
        try {
            Bundle bundle = (Bundle) resource.getUnderlyingResource();
            Patient patient = (Patient) HapiHelper.resourceInBundle(bundle, Patient.class);
            List<HumanName> names = patient.getName();
            names.stream()
                    .map(
                            name ->
                                    name.getExtensionByUrl(
                                            "https://reportstream.cdc.gov/fhir/StructureDefinition/xpn-human-name"))
                    .findFirst()
                    .ifPresent(extension -> extension.removeExtension("XPN.7"));
        } catch (Exception e) {
            throw new RuleExecutionException("Failed to remove patient name type code", e);
        }
    }
}
