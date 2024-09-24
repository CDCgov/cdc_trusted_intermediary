package gov.hhs.cdc.trustedintermediary.external.hapi;

import ca.uhn.hl7v2.model.Message;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;

public class HapiHL7Message implements HealthData<Message> {

    protected final Message underlyingData;

    public HapiHL7Message(Message innerResource) {
        this.underlyingData = innerResource;
    }

    @Override
    public Message getUnderlyingData() {
        return underlyingData;
    }

    @Override
    public String getName() {
        return underlyingData.getName();
    }
}
