import com.java_polytech.pipeline_interfaces.RC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SyntaxAnalyzer {

    private BaseGrammar grammar;
    private static RC m_error;

    public SyntaxAnalyzer(BaseGrammar grammar){
        if(grammar != null){
            this.grammar = grammar;

        }else{
            m_error = new RC(RC.RCWho.UNKNOWN,
                           RC.RCType.CODE_INVALID_ARGUMENT,
                      "Invalid argument in syntax analyzer config");
        }
    }

    public RC getError() {
        return m_error;
    }

    public String[] readConfig(String configurationFile){
        try {
            ArrayList<String> parameters = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(configurationFile));
            int index = 0;

            for (String str = reader.readLine(); str != null; str = reader.readLine()) {
                String[] signs = str.split(grammar.delimiter());

                if (index >= grammar.numberTokens()) {
                    break;
                }

                for (int i = 0; i < signs.length; i++) {
                    String line = signs[i].trim();

                    if (line.equals(grammar.token(index))) {
                        parameters.add(index, signs[++i].trim());
                        index++;
                    }
                }
            }
            if(parameters.size() == 0) {

                m_error = new RC(RC.RCWho.UNKNOWN,
                               RC.RCType.CODE_INVALID_ARGUMENT,
                          "There are no arguments in the configuration file");
            }
            String[] param = new String[parameters.size()];
            parameters.toArray(param);

            m_error = RC.RC_SUCCESS;
            return param;

        }catch (IOException ex) {

            m_error = new RC(RC.RCWho.UNKNOWN,
                           RC.RCType.CODE_INVALID_INPUT_FILE,
                      " Unable to open the config's input stream");
            return null;
        }
    }
}