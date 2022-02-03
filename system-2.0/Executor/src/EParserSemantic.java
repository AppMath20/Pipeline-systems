import com.java_polytech.pipeline_interfaces.RC;

public class EParserSemantic {

    private int index = 0;
    protected int SIGN;
    private int SIZE_LONG;

    public EParserSemantic(){

    }

    public RC analyze(String configFile){
        SyntaxAnalyzer parser = new SyntaxAnalyzer(new EGrammar());
        String[] parameters = parser.readConfig(configFile);

        if(parameters == null) {
            return new RC(RC.RCWho.EXECUTOR,
                          RC.RCType.CODE_INVALID_ARGUMENT,
                     "The input data is not valid in Executor");
        }

        if (parser.getError() != RC.RC_SUCCESS) {
            return parser.getError();
        }

        SIZE_LONG   = Integer.parseInt(parameters[index]);
        SIGN        = Integer.parseInt(parameters[++index]);

        if (SIZE_LONG <= 0 ) {

            return new RC(RC.RCWho.EXECUTOR,
                          RC.RCType.CODE_CONFIG_SEMANTIC_ERROR,
                     "Invalid argument in executor's config");

        }else{
            return RC.RC_SUCCESS;
        }
    }

    public int getData(){
        return SIZE_LONG;
    }
}