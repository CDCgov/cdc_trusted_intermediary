package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;

/**
 * This transformation removes all OBRs from an ORU message except for the OBR with value "54089-8"
 * in OBR-4.1. All OBXs are attached to the sole remaining OBR
 */
public class RemoveObservationRequests implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();

        Stream<DiagnosticReport> diagnosticReports =
                HapiHelper.resourcesInBundle(bundle, DiagnosticReport.class);
        List<Observation> observations =
                HapiHelper.resourcesInBundle(bundle, Observation.class).toList();

        for (DiagnosticReport report : diagnosticReports.toList()) {
            ServiceRequest serviceRequest = HapiHelper.getServiceRequest(report);
            if (serviceRequest != null) {
                String ob4_1 = HapiHelper.getOBR4_1Value(serviceRequest);
                if ("54089-8".equals(ob4_1)) {
                    List<Reference> references =
                            observations.stream()
                                    .map(observation -> new Reference(observation.getId()))
                                    .collect(Collectors.toList());
                    report.setResult(references);
                    continue;
                }
                bundle.getEntry().removeIf(entry -> entry.getResource().equalsDeep(serviceRequest));
            }
            bundle.getEntry().removeIf(entry -> entry.getResource().equalsDeep(report));
        }
    }
}