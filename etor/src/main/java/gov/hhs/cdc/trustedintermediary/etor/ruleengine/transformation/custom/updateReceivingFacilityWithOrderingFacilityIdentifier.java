package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Collections;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;

/**
 * Updates the receiving facility (MSH-6) to value in Ordering Facility Name's Organization
 * Identifier (ORC-21.10).
 */
public class updateReceivingFacilityWithOrderingFacilityIdentifier
        implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args)
            throws RuleExecutionException {
        try {
            Bundle bundle = (Bundle) resource.getUnderlyingResource();
            Organization receivingFacility = HapiHelper.getReceivingFacility(bundle);
            Identifier namespaceIdentifier =
                    HapiHelper.getHDNamespaceIdentifier(receivingFacility.getIdentifier());
            String orderingFacilityNameOrganizationIdentifier = "R797"; // Get it from ORC-21.10
            namespaceIdentifier.setValue(orderingFacilityNameOrganizationIdentifier);
            receivingFacility.setIdentifier(Collections.singletonList(namespaceIdentifier));
        } catch (Exception e) {
            throw new RuleExecutionException(
                    "Failed to update the receiving facility with the ordering facility identifier",
                    e);
        }
    }
}
