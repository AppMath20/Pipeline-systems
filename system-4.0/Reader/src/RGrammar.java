public class RGrammar extends BaseGrammar {

    private static final String[] readerToken;

    static{
        readerToken = new String[]{"BUFFER_SIZE"};
    }

    public RGrammar() {
        super(readerToken);
    }
}