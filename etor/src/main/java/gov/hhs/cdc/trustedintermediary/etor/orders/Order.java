package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.messages.Message;

/**
 * Interface to wrap a third-party lab order class (Ex: Hapi FHIR Bundle)
 *
 * @param <T> The underlying FHIR lab order type.
 */
public interface Order<T> extends Message<T> {
    String getPatientId();
}
