// package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;
//
//
/// ** Manages the loading of rules from a definitions file. */
// public class RuleLoader {
//
//    private static final RuleLoader INSTANCE = new RuleLoader();
//
//    @Inject Formatter formatter;
//    @Inject Logger logger;
//
//    private RuleLoader() {}
//
//    public static RuleLoader getInstance() {
//        return INSTANCE;
//    }
//
//    public <T> List<T> loadRules(
//            InputStream ruleDefinitionStream, TypeReference<Map<String, List<T>>> typeReference)
//            throws RuleLoaderException {
//        try {
//            var rulesString =
//                    new String(ruleDefinitionStream.readAllBytes(), StandardCharsets.UTF_8);
//            Map<String, List<T>> jsonObj =
//                    formatter.convertJsonToObject(rulesString, typeReference);
//            return jsonObj.getOrDefault("definitions", Collections.emptyList());
//        } catch (IOException | FormatterProcessingException e) {
//            throw new RuleLoaderException("Failed to load rules definitions", e);
//        }
//    }
// }
