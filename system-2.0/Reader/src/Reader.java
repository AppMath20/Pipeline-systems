import com.java_polytech.pipeline_interfaces.IConsumer;
import com.java_polytech.pipeline_interfaces.IReader;
import com.java_polytech.pipeline_interfaces.RC;

import java.io.IOException;
import java.io.InputStream;

public class Reader implements IReader {

    private InputStream m_fileInput;
    private IConsumer m_consumer;
    private RC m_error;
    private byte[] m_bytes;
    private int m_bufferSize;
    private final RParserSemantic m_parser;

    public Reader() {
        m_parser = new RParserSemantic();
    }

    @Override
    public RC setConfig(String s) {
        if (s != null) {
            return m_parser.analyze(s);
        }
        else {

            return new RC(RC.RCWho.READER,
                          RC.RCType.CODE_INVALID_ARGUMENT,
                     "The name of the configuration file is missing");
        }
    }

    @Override
    public RC setConsumer(IConsumer consumer) {
        if(consumer == null) {
            return new RC(RC.RCWho.READER,
                          RC.RCType.CODE_FAILED_PIPELINE_CONSTRUCTION,
                     "Reader's consumer is null");
        }
        else {
            m_consumer = consumer;
            return RC.RC_SUCCESS;
        }
    }

    @Override
    public RC setInputStream(InputStream fileInputStream) {
        if(fileInputStream == null) {

            return new RC(RC.RCWho.READER,
                          RC.RCType.CODE_INVALID_INPUT_FILE,
                     "Invalid input stream Reader`s");
        } else {
            m_fileInput = fileInputStream;
            return RC.RC_SUCCESS;
        }
    }

    private RC readData(){

        int temp;
        m_bytes = new byte[m_bufferSize];
        try {
            temp = m_fileInput.read(m_bytes);
            if (temp != m_bufferSize) {
                return new RC(RC.RCWho.READER,
                              RC.RCType.CODE_INVALID_INPUT_FILE,
                         "Error in reading data");
            }
        } catch (IOException exception) {
            return new RC(RC.RCWho.READER,
                          RC.RCType.CODE_FAILED_TO_READ,
                     "Error reading output file");
        }
        return RC.RC_SUCCESS;
    }

    @Override
    public RC run() {

        m_bufferSize = m_parser.getData();
        try {
            while (m_fileInput.available() > 0)
            {
                if (m_bufferSize > m_fileInput.available()) {
                    m_bufferSize = m_fileInput.available();
                }
                m_error = readData();
                if (m_error != RC.RC_SUCCESS) {
                    return m_error;
                }
                m_error = m_consumer.consume(m_bytes);
                if (m_error != RC.RC_SUCCESS) {
                    return m_error;
                }
            }
        }
        catch (IOException exception) {
            return new RC(RC.RCWho.READER,
                          RC.RCType.CODE_FAILED_TO_READ,
                     "Error reading the input file");
        }
        return RC.RC_SUCCESS;
    }
}