public class WGrammar extends BaseGrammar {

    private static final String[] writerToken;

    static{
        writerToken = new String[]{"BUFFER_SIZE"};
    }

    public WGrammar() {
        super(writerToken);
    }
}
