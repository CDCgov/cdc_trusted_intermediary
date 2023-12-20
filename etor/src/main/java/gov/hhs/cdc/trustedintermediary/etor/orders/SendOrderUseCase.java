package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataOrchestrator;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import javax.inject.Inject;

/** The overall logic to receive, convert to OML, and subsequently send a lab order. */
public class SendOrderUseCase {
    private static final SendOrderUseCase INSTANCE = new SendOrderUseCase();
    @Inject OrderConverter converter;
    @Inject OrderSender sender;
    @Inject MetricMetadata metadata;
    @Inject PartnerMetadataOrchestrator partnerMetadataOrchestrator;
    @Inject Logger logger;

    private SendOrderUseCase() {}

    public static SendOrderUseCase getInstance() {
        return INSTANCE;
    }

    public void convertAndSend(final Order<?> order, String submissionId)
            throws UnableToSendOrderException {

        savePartnerMetadata(submissionId, order);

        var omlOrder = converter.convertMetadataToOmlOrder(order);
        metadata.put(order.getFhirResourceId(), EtorMetadataStep.ORDER_CONVERTED_TO_OML);
        omlOrder = converter.addContactSectionToPatientResource(omlOrder);
        metadata.put(order.getFhirResourceId(), EtorMetadataStep.CONTACT_SECTION_ADDED_TO_PATIENT);
        sender.sendOrder(omlOrder);
    }

    private void savePartnerMetadata(String submissionId, final Order<?> order) {
        if (submissionId == null) {
            return;
        }

        try {
            partnerMetadataOrchestrator.updateMetadataForReceivedOrder(submissionId, order);
        } catch (PartnerMetadataException e) {
            logger.logError("Unable to save metadata for submissionId " + submissionId, e);
        }
    }
}
