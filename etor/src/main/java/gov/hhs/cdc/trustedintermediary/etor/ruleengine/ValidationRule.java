package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class ValidationRule implements Rule {
    private String name;
    private String description;
    private String warningMessage;
    private List<String> conditions;
    private List<String> validations;

    HapiFhir fhirEngine = HapiFhirImplementation.getInstance();

    public ValidationRule() {}

    public ValidationRule(
            String ruleName,
            String ruleDescription,
            String ruleWarningMessage,
            List<String> ruleConditions,
            List<String> ruleValidations) {
        name = ruleName;
        description = ruleDescription;
        warningMessage = ruleWarningMessage;
        conditions = ruleConditions;
        validations = ruleValidations;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getWarningMessage() {
        return warningMessage;
    }

    @Override
    public List<String> getConditions() {
        return conditions;
    }

    @Override
    public List<String> getValidations() {
        return validations;
    }

    @Override
    public boolean isValid(IBaseResource resource) {
        return validations.stream()
                .allMatch(
                        validation -> {
                            try {
                                return fhirEngine.evaluateCondition(resource, validation);
                            } catch (Exception e) {
                                throw new RuntimeException(e); // @todo use more accurate exception
                            }
                        });
    }

    @Override
    public boolean appliesTo(IBaseResource resource) {
        return conditions.stream()
                .allMatch(
                        condition -> {
                            try {
                                return fhirEngine.evaluateCondition(resource, condition);
                            } catch (Exception e) {
                                throw new RuntimeException(e); // @todo use more accurate exception
                            }
                        });
    }
}
