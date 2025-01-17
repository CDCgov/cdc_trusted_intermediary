package gov.hhs.cdc.trustedintermediary.rse2e.external.localfile;

import gov.hhs.cdc.trustedintermediary.rse2e.FileFetcher;
import gov.hhs.cdc.trustedintermediary.rse2e.FileFetcherEnum;
import gov.hhs.cdc.trustedintermediary.rse2e.hl7.HL7FileStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * The LocalFileFetcher class implements the {@link FileFetcher FileFetcher} interface and
 * represents a file fetcher that fetches files from the local file system.
 */
public class LocalFileFetcher implements FileFetcher {
    private static final String EXTENSION = "hl7";

    private static final FileFetcher INSTANCE = new LocalFileFetcher();

    private LocalFileFetcher() {}

    public static FileFetcher getInstance() {
        return INSTANCE;
    }

    @Override
    public List<HL7FileStream> fetchFiles(FileFetcherEnum fileFetcherEnum) {
        String rse2ELocalInputFilePath = "../examples/Test/Automated/";
        if (FileFetcherEnum.ASSERTION == fileFetcherEnum) {
            rse2ELocalInputFilePath += "Assertion/";
        } else {
            rse2ELocalInputFilePath += "GoldenCopy/Expected/";
        }
        try (Stream<Path> stream = Files.walk(Paths.get(rse2ELocalInputFilePath))) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(
                            p -> {
                                try {
                                    // Need to keep the input stream open until the test is done
                                    // Must make sure to close the input stream after use
                                    InputStream inputStream = Files.newInputStream(p);
                                    return new HL7FileStream(
                                            p.getFileName().toString(), inputStream);
                                } catch (IOException e) {
                                    throw new RuntimeException("Error opening file: " + p, e);
                                }
                            })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
