package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;

/** Converts an ORM order to OML. */
public class ConvertToOmlOrder implements CustomFhirTransformation {

    private final MetricMetadata metadata =
            ApplicationContext.getImplementation(MetricMetadata.class);

    @Override
    public void transform(HealthData<?> resource, Map<String, Object> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingData();
        HapiHelper.setMSH9Coding(bundle, HapiHelper.OML_CODING);
        metadata.put(bundle.getId(), EtorMetadataStep.ORDER_CONVERTED_TO_OML);
    }
}
