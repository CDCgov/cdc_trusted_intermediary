package gov.hhs.cdc.trustedintermediary.external.inmemory


import spock.lang.Specification

class KeyCacheTest extends Specification {

    def "keyCache works"() {
        given:
        def cache = KeyCache.getInstance()
        def value = "fake_key"
        def key = "report_stream"
        def expected = value
        when:
        cache.put(key, value)
        def actual = cache.get(key)
        then:
        actual == expected
    }

    def "keyCache synchronization works"() {
        given:
        def cache = KeyCache.getInstance()
        def threadsNum = 5
        def iterations = 25

        when:
        List<Thread> threads = []
        (1..threadsNum).each { threadId ->
            threads.add(new Thread({
                for (int i = 0; i < iterations; i++) {
                    cache.put("Thread-" + threadId, "${i}")
                }
            }))
        }

        threads*.start()
        threads*.join()
        def keys = cache.getProperties().get("keys").collect().collectEntries()

        then:
        keys.size() == threadsNum // one key per thread
        keys.values().toSet().size() == 1 // all entries have same value, threads had to wait on the lock

    }

    def "keyCache removal works"() {
        given:
        def cache = KeyCache.getInstance()
        def value = "fake_key"
        def key = "report_stream"
        def expected = null
        when:
        cache.put(key, value)
        cache.remove(key)
        def actual = cache.get(key)
        then:
        actual == expected
    }
}
