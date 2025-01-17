package gov.hhs.cdc.trustedintermediary.rse2e.external.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import gov.hhs.cdc.trustedintermediary.rse2e.FileFetcher;
import gov.hhs.cdc.trustedintermediary.rse2e.FileFetcherEnum;
import gov.hhs.cdc.trustedintermediary.rse2e.hl7.HL7FileStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * The AzureBlobFileFetcher class implements the {@link FileFetcher FileFetcher} interface and
 * retrieves files from an Azure Blob Storage container.
 */
public class AzureBlobFileFetcher implements FileFetcher {

    // Using Eastern Standard Time as all or most contributors are in the US
    private static final ZoneId TIME_ZONE = ZoneOffset.of("-05:00");
    private static final int RETENTION_DAYS = 90;
    private static final String CONTAINER_NAME = "automated";

    private final BlobContainerClient blobContainerClient;

    private static final FileFetcher INSTANCE = new AzureBlobFileFetcher();

    private AzureBlobFileFetcher() {
        String azureStorageConnectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");

        if (azureStorageConnectionString == null || azureStorageConnectionString.isEmpty()) {
            throw new IllegalArgumentException(
                    "Environment variable AZURE_STORAGE_CONNECTION_STRING is not set");
        }
        this.blobContainerClient =
                new BlobContainerClientBuilder()
                        .connectionString(azureStorageConnectionString)
                        .containerName(CONTAINER_NAME)
                        .buildClient();

        AzureBlobOrganizer blobOrganizer = new AzureBlobOrganizer(blobContainerClient);
        blobOrganizer.organizeAndCleanupBlobsByDate(RETENTION_DAYS, TIME_ZONE);
    }

    public static FileFetcher getInstance() {
        return INSTANCE;
    }

    @Override
    public List<HL7FileStream> fetchFiles(FileFetcherEnum fileFetcherEnum) {
        List<HL7FileStream> relevantFiles = new ArrayList<>();

        LocalDate today = LocalDate.now(TIME_ZONE);

        String pathPrefix = AzureBlobHelper.buildDatePathPrefix(today);

        if (FileFetcherEnum.ASSERTION == fileFetcherEnum) {
            pathPrefix += "Assertion/";
        } else {
            pathPrefix += "GoldenCopy/";
        }

        ListBlobsOptions options = new ListBlobsOptions().setPrefix(pathPrefix);
        for (BlobItem blobItem : blobContainerClient.listBlobs(options, null)) {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobItem.getName());
            relevantFiles.add(
                    new HL7FileStream(blobClient.getBlobName(), blobClient.openInputStream()));
        }

        return relevantFiles;
    }
}
