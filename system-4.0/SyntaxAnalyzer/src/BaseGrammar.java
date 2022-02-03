public class BaseGrammar {

    private final String[] tokens;
    private static final String delimiter = "=";

    protected BaseGrammar(String[] tokens) {
        assert tokens != null;
        this.tokens = tokens;
    }

    public final int numberTokens() {
        return tokens.length;
    }

    public final String token(int index) {
        assert index >= 0;

        return tokens[index];
    }

    public String delimiter() {
        return delimiter;
    }
}