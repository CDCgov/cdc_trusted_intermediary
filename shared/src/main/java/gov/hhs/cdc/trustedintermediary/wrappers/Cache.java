package gov.hhs.cdc.trustedintermediary.wrappers;

/** Interface that provides a structure to follow for Caching */
public interface Cache {

    void put(String key, String value);

    String get(String key);

    void remove(String key);
}
