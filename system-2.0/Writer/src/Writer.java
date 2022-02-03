import com.java_polytech.pipeline_interfaces.IWriter;
import com.java_polytech.pipeline_interfaces.RC;

import java.io.IOException;
import java.io.OutputStream;

public class Writer implements IWriter {

    private OutputStream m_fileOutput;
    private RC m_error;
    private int m_bufferSize;
    private final WParserSemantic m_parser;

    public Writer() {
        m_parser = new WParserSemantic();
    }

    @Override
    public RC setConfig(String s) {
        if (s != null) {
            return m_parser.analyze(s);

        }else {
            return new RC(RC.RCWho.WRITER,
                          RC.RCType.CODE_INVALID_ARGUMENT,
                     "The name of the configuration file is missing");
        }
    }

    @Override
    public RC setOutputStream(OutputStream fileOutputStream) {
        if(fileOutputStream == null) {
            return new RC(RC.RCWho.WRITER,
                          RC.RCType.CODE_INVALID_INPUT_FILE,
                     "Invalid output stream Writer`s");
        } else {
            m_fileOutput = fileOutputStream;
            return RC.RC_SUCCESS;
        }
    }

    private RC writeData(byte[] data){
        try {
            m_fileOutput.write(data);
        }
        catch (IOException exception) {

            return new RC(RC.RCWho.WRITER,
                          RC.RCType.CODE_FAILED_TO_WRITE,
                     "Error writing output file");
        }
        return RC.RC_SUCCESS;
    }

    @Override
    public RC consume(byte[] data) {

        m_bufferSize = m_parser.getData();
        if (data != null)
        {
            if(data.length > m_bufferSize) {
                return new RC(RC.RCWho.WRITER,
                              RC.RCType.CODE_INVALID_ARGUMENT,
                         "Exceeding the size of the input data");
            }

            m_error = writeData(data);
            if (m_error != RC.RC_SUCCESS) {
                return m_error;
            }
            return RC.RC_SUCCESS;
        }
        else {
            return new RC(RC.RCWho.WRITER,
                          RC.RCType.CODE_INVALID_ARGUMENT,
                     "Writer no receive data");
        }
    }
}