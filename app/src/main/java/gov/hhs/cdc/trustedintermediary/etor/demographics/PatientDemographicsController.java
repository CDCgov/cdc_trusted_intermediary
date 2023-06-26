package gov.hhs.cdc.trustedintermediary.etor.demographics;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponseHelper;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiDemographics;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import java.util.Optional;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;

/**
 * Creates an in-memory representation of patient demographics to be ingested by the system, and
 * return response information back to the client.
 */
public class PatientDemographicsController {

    private static final PatientDemographicsController PATIENT_DEMOGRAPHICS_CONTROLLER =
            new PatientDemographicsController();
    static final String CONTENT_TYPE_LITERAL = "Content-Type";
    static final String APPLICATION_JSON_LITERAL = "application/json";

    @Inject HapiFhir fhir;
    @Inject Formatter formatter;
    @Inject Logger logger;
    @Inject DomainResponseHelper domainResponseHelper;

    private PatientDemographicsController() {}

    public static PatientDemographicsController getInstance() {
        return PATIENT_DEMOGRAPHICS_CONTROLLER;
    }

    public Demographics<?> parseDemographics(DomainRequest request) {
        logger.logInfo("Parsing patient demographics");
        var fhirBundle = fhir.parseResource(request.getBody(), Bundle.class);
        return new HapiDemographics(fhirBundle);
    }

    public DomainResponse constructResponse(
            PatientDemographicsResponse patientDemographicsResponse) {
        return domainResponseHelper.constructResponse(patientDemographicsResponse);
    }

    public DomainResponse constructResponse(int httpStatus, String errorString) {
        return domainResponseHelper.constructErrorResponse(httpStatus, errorString);
    }

    public DomainResponse constructResponse(int httpStatus, Exception exception) {
        var errorMessage =
                Optional.ofNullable(exception.getMessage()).orElse(exception.getClass().toString());
        return domainResponseHelper.constructErrorResponse(httpStatus, errorMessage);
    }
}
