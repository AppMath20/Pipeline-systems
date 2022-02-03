public class EGrammar extends BaseGrammar {

    private static final  String[] executorTokens;

    static {
        executorTokens = new String[]{"SIZE_LONG",
                                      "SIGN"};
    }

    public EGrammar() {
        super(executorTokens);
    }
}
