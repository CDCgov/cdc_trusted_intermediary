package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;

/**
 * Removes Assigning Authority (PID-3.4) and Identifier Type Code (PID-3.5) from Patient Identifier
 * List (PID-3).
 */
public class RemovePatientIdentifiers implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args)
            throws RuleExecutionException {
        try {
            Bundle bundle = (Bundle) resource.getUnderlyingResource();
            Identifier patientIdentifier = HapiHelper.getPatientIdentifierList(bundle).get(0);
            Organization organization =
                    (Organization) patientIdentifier.getAssigner().getResource();
            organization.getIdentifierFirstRep().setValue(""); // remove PID.3-4
            patientIdentifier.getType().getCodingFirstRep().setCode(""); // remove PID.3-5
        } catch (Exception e) {
            throw new RuleExecutionException("Failed to remove patient identifiers", e);
        }
    }
}