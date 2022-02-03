import com.java_polytech.pipeline_interfaces.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Reader implements IReader {

    private InputStream m_fileInput;
    private IConsumer m_consumer;
    private IMediator m_mediator;
    private RC m_error;
    private byte[] m_bytes;
    private int m_bufferSize;
    private final TYPE[] m_outputTypes;
    private final RParserSemantic m_parser;

    public Reader() {
        m_parser = new RParserSemantic();
        m_outputTypes = new TYPE[] {TYPE.INT_ARRAY, TYPE.CHAR_ARRAY, TYPE.BYTE_ARRAY};
    }

    @Override
    public RC setConfig(String s) {
        if (s == null) {
            return new RC(RC.RCWho.READER,
                    RC.RCType.CODE_INVALID_ARGUMENT,
                    "The name of the configuration file is missing");
        }
        else {
            return m_parser.analyze(s);
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
                m_error = m_consumer.consume();
                if (m_error != RC.RC_SUCCESS) {
                    return m_error;
                }
            }
        }
        catch (IOException exception)
        {
            return new RC(RC.RCWho.READER,
                          RC.RCType.CODE_FAILED_TO_READ,
                    "Error reading the input file");
        }
        return RC.RC_SUCCESS;
    }

    private class ByteMediator implements IMediator{

        @Override
        public Object getData() {
            byte []data = new byte[m_bytes.length];

            System.arraycopy(m_bytes, 0, data, 0, m_bytes.length);
            return data;
        }
    }

    private class IntMediator implements IMediator{

        @Override
        public Object getData() {
            int[] data = new int[m_bytes.length / 4];

            ByteBuffer byteBuffer = ByteBuffer.wrap(m_bytes);
            for (int i = 0; i < data.length; i++) {
                data[i] = byteBuffer.getInt();
            }
            return data;
        }
    }

    private class CharMediator implements IMediator{
        @Override
        public Object getData() {
            char[] data = new char[m_bytes.length];

            for(int i = 0; i < m_bytes.length; ++i) {
                data[i] = (char)m_bytes[i];
            }

            return data;
        }
    }

    @Override
    public TYPE[] getOutputTypes() {
        return m_outputTypes;
    }

    @Override
    public IMediator getMediator(TYPE type) {
        if (type == null){
            return null;
        }
        switch (type){
            case BYTE_ARRAY: m_mediator = new ByteMediator();
                break;
            case CHAR_ARRAY: m_mediator = new CharMediator();
                break;
            case INT_ARRAY:  m_mediator = new IntMediator();
                break;
            default:         m_mediator = null;
                break;
        }
        return m_mediator;
    }
}