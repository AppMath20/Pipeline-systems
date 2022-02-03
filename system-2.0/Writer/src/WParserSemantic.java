import com.java_polytech.pipeline_interfaces.RC;

public class WParserSemantic {

    private final int index = 0;
    private int BUFFER_SIZE;

    public WParserSemantic(){

    }

    public RC analyze(String configFile){
        SyntaxAnalyzer parser = new SyntaxAnalyzer(new WGrammar());
        String[] parameters = parser.readConfig(configFile);

        if(parameters == null) {
            return new RC(RC.RCWho.WRITER,
                          RC.RCType.CODE_INVALID_ARGUMENT,
                     "The input data is not valid in Writer");
        }

        if (parser.getError() != RC.RC_SUCCESS) {
            return parser.getError();
        }

        BUFFER_SIZE = Integer.parseInt(parameters[index]);

        if (BUFFER_SIZE <= 0) {

            return new RC(RC.RCWho.WRITER,
                          RC.RCType.CODE_CONFIG_SEMANTIC_ERROR,
                     "Invalid argument in writer's config");
        }else {
            return RC.RC_SUCCESS;
        }
    }

    public int getData(){
        return BUFFER_SIZE;
    }
}
