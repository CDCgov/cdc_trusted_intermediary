package gov.hhs.cdc.trustedintermediary.etor.metadata;

/** Interface to store and retrieve our partner-facing metadata. */
public interface PartnerMetadataStorage {
    PartnerMetadata readMetadata(String uniqueId) throws PartnerMetadataException;

    /**
     * This method will do "upserts". If the record doesn't exist, it is created. If the record
     * exists, it is updated.
     *
     * @param metadata The metadata to save.
     */
    void saveMetadata(PartnerMetadata metadata) throws PartnerMetadataException;
}
