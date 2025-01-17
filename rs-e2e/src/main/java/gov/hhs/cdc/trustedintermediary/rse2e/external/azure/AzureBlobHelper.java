package gov.hhs.cdc.trustedintermediary.rse2e.external.azure;

import java.time.LocalDate;

/* The AzureBlobHelper is a utility class that provides helper methods for working with Azure Blob Storage. */
public class AzureBlobHelper {

    private AzureBlobHelper() {}

    // Builds a path prefix for a given date in the format "YYYY/MM/DD/". This is meant to make it
    // easier for people in the team to find files in the Azure Blob Storage
    public static String buildDatePathPrefix(LocalDate date) {
        return String.format(
                "%d/%02d/%02d/", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    public static String createDateBasedPath(LocalDate date, String originalName) {
        return buildDatePathPrefix(date) + originalName;
    }

    public static boolean isInDateFolder(String testType, String blobPath, LocalDate creationDate) {
        String expectedPath = buildDatePathPrefix(creationDate) + testType;
        return blobPath.startsWith(expectedPath);
    }
}
