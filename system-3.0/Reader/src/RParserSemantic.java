import com.java_polytech.pipeline_interfaces.RC;

public class RParserSemantic {

    private final int index = 0;
    private int BUFFER_SIZE;

    public RParserSemantic(){
    }

    public RC analyze(String configFile){
        SyntaxAnalyzer parser = new SyntaxAnalyzer(new RGrammar());
        String[] parameters = parser.readConfig(configFile);

        if(parameters == null) {

            return new RC(RC.RCWho.READER,
                          RC.RCType.CODE_INVALID_ARGUMENT,
                     "The input data is not valid in Reader");
        }

        if (parser.getError() != RC.RC_SUCCESS) {
            return parser.getError();
        }

        BUFFER_SIZE = Integer.parseInt(parameters[index]);

        if (BUFFER_SIZE <= 0) {

            return new RC(RC.RCWho.READER,
                          RC.RCType.CODE_CONFIG_SEMANTIC_ERROR,
                     "Invalid argument in reader's config");

        }else {
            return RC.RC_SUCCESS;
        }
    }

    public int getData(){
        return BUFFER_SIZE;
    }
}