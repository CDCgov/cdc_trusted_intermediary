package gov.hhs.cdc.trustedintermediary.rse2e;

import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Represents a HAPI HL7 message that implements the HealthData interface. This class provides a
 * wrapper around the HAPI Message object.
 */
public class HL7Message implements HealthData<HL7Message> {

    private final Map<String, List<String>> segments;
    private final Map<String, Character> encodingCharacters;

    public HL7Message(
            Map<String, List<String>> segments, Map<String, Character> encodingCharacters) {
        this.segments = segments;
        this.encodingCharacters = encodingCharacters;
    }

    public List<String> getSegment(String segment) {
        return segments.get(segment);
    }

    public String getValue(String segmentName, int... indices) {
        List<String> fields = segments.get(segmentName);
        if (fields == null || indices[0] > fields.size()) {
            return null;
        }

        char[] levelDelimiters = this.getOrderedLevelDelimiters();
        String value = fields.get(indices[0] - 1);
        for (int i = 1; i < indices.length; i++) {
            if (i >= levelDelimiters.length) {
                return null;
            }
            char levelDelimiter = levelDelimiters[i];
            int index = indices[i] - 1;
            String[] parts = value.split(Pattern.quote(String.valueOf(levelDelimiter)));
            if (index < 0 || index >= parts.length) {
                return null;
            }
            value = parts[index];
        }
        return value;
    }

    public char getEscapeCharacter() {
        return this.encodingCharacters.get("escape");
    }

    public char getDelimiter(String type) {
        return this.encodingCharacters.get(type);
    }

    public char[] getOrderedLevelDelimiters() {
        return new char[] {
            getDelimiter("field"),
            getDelimiter("component"),
            getDelimiter("repetition"),
            getDelimiter("subcomponent")
        };
    }

    @Override
    public HL7Message getUnderlyingData() {
        return this;
    }

    @Override
    public String getIdentifier() {
        return getValue("MSH", 10);
    }
}
