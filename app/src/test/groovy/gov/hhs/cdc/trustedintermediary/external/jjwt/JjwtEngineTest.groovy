package gov.hhs.cdc.trustedintermediary.external.jjwt

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class JjwtEngineTest extends Specification {

    def "readPrivateaKey works"() {
        given:
        def expected = """SunRsaSign RSA private CRT key, 2048 bits
  params: null
  modulus: 27897662671237412614645585949757597903240726523917871693547600527580531037615927714357198420605835518634671965372032548422942203717229673323463225343709924634208255237053619724692723573411363211484198357569100148771742283580632631306619990273312871157974345578004704347658040442733544316016870094273123316267308144041089779925186312275870592286737178499633095847230908792065102806909557602796883595871117990184080094342104723065331441619179002838364572851853143344620773544234941860382864764351610571703794571553025384855153288701039759404323155524596814265271809151127402777298274741051512777127381287892775603620877
  private exponent: 6135084963443691491959954033612789413315485466621394415112910151574750195761776678565895940262471648264678116192752477289861521141867535388779067324812232613493545304240891317608890832686745995170911223102440696006419818760484425571916026211363378787010471270320472426425095867691490416933841039927237893032909032803580731575157486299449884306264507680961825930444072391195248779956131000125529322499011873765170247835498332429269555457443449344492245708187941705566103485536344264029763219575769322352195792242439276975260648301693718104261371053389186933547575290557842271013884989713398541057956598536593873481023"""

        def key = new String(
                Files.readAllBytes(
                Path.of("..", "mock_credentials", "my-rsa-local-private-key.pem")
                ))
        when:
        def actual = JjwtEngine.getInstance().readPrivateKey(key)
        then:
        actual.toString() == expected
    }
}
