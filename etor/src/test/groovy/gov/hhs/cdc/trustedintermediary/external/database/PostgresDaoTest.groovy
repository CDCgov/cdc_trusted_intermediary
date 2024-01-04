package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.external.azure.AzureClient
import gov.hhs.cdc.trustedintermediary.wrappers.SqlDriverManager
import spock.lang.Specification

import java.sql.Timestamp
import java.time.Instant

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class PostgresDaoTest extends Specification {

    private def mockDriver
    private def mockConn

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        mockDriver = Mock(SqlDriverManager)
        mockConn = Mock(Connection)
        def mockAzureClient = Mock(AzureClient)
        mockAzureClient.getScopedToken(_ as String) >> "DogCow password"
        TestApplicationContext.register(AzureClient, mockAzureClient)

        TestApplicationContext.register(PostgresDao, PostgresDao.getInstance())
    }

    def "connect happy path works"(){
        given:
        mockDriver.getConnection(_ as String, _ as Properties) >> {mockConn}

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def conn = PostgresDao.getInstance().connect()

        then:
        conn == mockConn
    }

    def "connect unhappy path throws exception"() {
        given:
        mockDriver.getConnection(_ as String, _ as Properties) >> {throw new SQLException()}
        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().connect()

        then:
        thrown(SQLException)
    }

    def "upsertMetadata works"() {
        given:
        PreparedStatement upsertMockStatement = Mock(PreparedStatement)

        mockDriver.getConnection(_ as String, _ as Properties) >>  mockConn
        mockConn.prepareStatement(_ as String) >> upsertMockStatement

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().upsertMetadata("mock_id", "mock_sender", "mock_receiver", "mock_hash", Instant.now())

        then:
        1 * upsertMockStatement.executeUpdate()
    }


    def "upsertMetadata unhappy path throws exception"() {
        given:
        mockDriver.getConnection(_ as String, _ as Properties) >> mockConn
        mockConn.prepareStatement(_ as String) >> { throw new SQLException() }

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().upsertMetadata("mock_id", "mock_sender", "mock_receiver", "mock_hash", Instant.now())

        then:
        thrown(SQLException)
    }

    def "select metadata retrieves data"(){
        given:
        PreparedStatement selectPreparedStatement = Mock(PreparedStatement)
        ResultSet selectResultSet = Mock(ResultSet)

        mockDriver.getConnection(_ as String, _ as Properties) >> mockConn
        mockConn.prepareStatement(_ as String) >> selectPreparedStatement
        selectPreparedStatement.executeQuery() >> selectResultSet
        selectResultSet.next() >> true
        selectResultSet.getTimestamp(_ as String) >> Timestamp.from(Instant.now())

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PartnerMetadata result = (PartnerMetadata) PostgresDao.getInstance().fetchMetadata("mock_sender")

        then:
        result != null
    }

    def "fetchMetadata unhappy path throws exception"() {
        given:
        mockDriver.getConnection(_ as String, _ as Properties) >> mockConn
        mockConn.prepareStatement(_ as String) >> { throw new SQLException() }

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().fetchMetadata("mock_lookup")

        then:
        thrown(SQLException)
    }

    def "fetchMetadata returns null when rows  do not exist"() {
        given:
        def mockPreparedStatement = Mock(PreparedStatement)
        def mockResultSet = Mock(ResultSet)
        def expected = null

        mockDriver.getConnection(_ as String, _ as Properties) >> mockConn
        mockConn.prepareStatement(_ as String) >>  mockPreparedStatement
        mockResultSet.next() >> false
        mockPreparedStatement.executeQuery() >> mockResultSet

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = PostgresDao.getInstance().fetchMetadata("mock_lookup")

        then:
        actual == expected
    }

    def "fetchMetadata returns partnermetadata when rows exist"() {
        given:
        def mockPreparedStatement = Mock(PreparedStatement)
        def mockResultSet = Mock(ResultSet)
        def messageId = "12345"
        def receiver = "DogCow"
        Timestamp timestampForMock = Timestamp.from(Instant.parse("2024-01-03T15:45:33.30Z"))
        Instant timeReceived = timestampForMock.toInstant()
        def hash = receiver.hashCode().toString()
        def expected = new PartnerMetadata(messageId, receiver, timeReceived, hash)

        mockDriver.getConnection(_ as String, _ as Properties) >> mockConn
        mockConn.prepareStatement(_ as String) >>  mockPreparedStatement
        mockResultSet.next() >> true
        mockResultSet.getString("message_id") >> messageId
        mockResultSet.getString("receiver") >> receiver
        mockResultSet.getTimestamp("time_received") >> timestampForMock
        mockResultSet.getString("hash_of_order") >> hash
        mockPreparedStatement.executeQuery() >> mockResultSet

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = PostgresDao.getInstance().fetchMetadata("mock_lookup")

        then:
        actual == expected
    }
}
