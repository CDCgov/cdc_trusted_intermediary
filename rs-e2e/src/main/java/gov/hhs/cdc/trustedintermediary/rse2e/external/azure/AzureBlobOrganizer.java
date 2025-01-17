package gov.hhs.cdc.trustedintermediary.rse2e.external.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.CopyStatusType;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/* AzureBlobOrganizer is responsible for organizing and cleaning up blobs in an Azure container */
public class AzureBlobOrganizer {

    public static final String GOLDEN_COPY = "GoldenCopy/";
    public static final String ASSERTION = "Assertion/";
    private final BlobContainerClient blobContainerClient;

    protected final Logger logger = ApplicationContext.getImplementation(Logger.class);

    public AzureBlobOrganizer(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }

    //    Delete the current date folder's blobs to prevent buildup when ran manually
    private void deleteOldBlobsFromToday(String testType, ZoneId timeZone) {
        // Check folder structures: YEAR/MONTH/DAY/Assertion_OR_GoldenCopy/
        String prefix =
                LocalDate.now(timeZone).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                        + "/"
                        + testType;
        logger.logInfo("Checking folder: " + prefix);

        for (BlobItem blobItem : blobContainerClient.listBlobsByHierarchy(prefix)) {
            if (blobItem.getName().endsWith(".hl7")) {
                logger.logInfo("Deleting blob: " + blobItem.getName());
                blobContainerClient.getBlobClient(blobItem.getName()).delete();
            }
        }
        logger.logInfo("End of checking folder: " + prefix);
    }

    // Organize blob into folder structure: YEAR/MONTH/DAY/Assertion_OR_GoldenCopy/SOURCE_NAME
    public void organizeAndCleanupBlobsByDate(int retentionDays, ZoneId timeZone) {
        deleteOldBlobsFromToday(GOLDEN_COPY, timeZone);
        deleteOldBlobsFromToday(ASSERTION, timeZone);
        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            String sourceName = blobItem.getName();
            try {
                BlobClient sourceBlob = blobContainerClient.getBlobClient(sourceName);
                BlobProperties sourceProperties = sourceBlob.getProperties();
                LocalDate sourceCreationDate =
                        sourceProperties
                                .getCreationTime()
                                .toInstant()
                                .atZone(timeZone)
                                .toLocalDate();

                LocalDate retentionDate = LocalDate.now(timeZone).minusDays(retentionDays);
                if (sourceCreationDate.isBefore(retentionDate)) {
                    sourceBlob.delete();
                    logger.logInfo("Deleted old blob: {}", sourceName);
                    continue;
                }

                String testType = ASSERTION;
                String testTypeAndSourceName = testType + sourceName;
                if (sourceBlob.getBlobName().contains("GOLDEN-COPY")) {
                    testType = GOLDEN_COPY;
                    testTypeAndSourceName = GOLDEN_COPY + sourceName;
                }

                if (AzureBlobHelper.isInDateFolder(testType, sourceName, sourceCreationDate)) {
                    continue;
                }

                System.out.println("Organizing blob: " + testTypeAndSourceName);

                String destinationName =
                        AzureBlobHelper.createDateBasedPath(
                                sourceCreationDate, testTypeAndSourceName);

                BlobClient destinationBlob = blobContainerClient.getBlobClient(destinationName);
                destinationBlob
                        .beginCopy(sourceBlob.getBlobUrl(), null)
                        .waitForCompletion(Duration.ofSeconds(30));
                CopyStatusType copyStatus = destinationBlob.getProperties().getCopyStatus();
                if (copyStatus == CopyStatusType.SUCCESS) {
                    sourceBlob.delete();
                    logger.logInfo("Moved blob {} to {}", sourceName, destinationName);
                } else {
                    destinationBlob.delete();
                    logger.logError("Failed to copy blob: " + sourceName);
                }
            } catch (Exception e) {
                logger.logError("Error processing blob: " + sourceName, e);
            }
        }
    }
}
