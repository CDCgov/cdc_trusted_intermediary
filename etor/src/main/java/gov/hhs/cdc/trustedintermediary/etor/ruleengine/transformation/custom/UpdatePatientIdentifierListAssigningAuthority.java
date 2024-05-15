package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;

public class UpdatePatientIdentifierListAssigningAuthority implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args)
            throws RuleExecutionException {

        if (!(resource.getUnderlyingResource() instanceof Bundle)) {
            throw new RuleExecutionException("Resource provided is not a Bundle");
        }

        try {
            Bundle bundle = (Bundle) resource.getUnderlyingResource();
            String newValue = args.get("newValue");

            HapiHelper.updateOrganizationIdentifierValue(bundle, newValue);

        } catch (Exception e) {
            throw new RuleExecutionException("Unexpected error during transformation", e);
        }
    }
}
