package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;

/** Removes the Message Structure (MSH-9.3) from the Message Type (MSH-9). */
public class RemoveMessageTypeStructure implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args)
            throws RuleExecutionException {
        try {
            Bundle bundle = (Bundle) resource.getUnderlyingResource();
            Coding coding = HapiHelper.getMessageTypeCoding(bundle);
            String display = coding.getDisplay();
            String delimiter = "^";
            List<String> displayList =
                    new ArrayList<>(Arrays.asList(display.split("\\" + delimiter)));
            if (displayList.size() < 3) {
                return;
            }
            // Remove the third element from the list
            displayList.remove(2);
            String strippedString = String.join(delimiter, displayList);
            coding.setDisplay(strippedString);
        } catch (Exception e) {
            throw new RuleExecutionException("Failed to remove message type structure", e);
        }
    }
}
