import com.java_polytech.pipeline_interfaces.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Writer implements IWriter {

    private OutputStream m_fileOutput;
    private TYPE[] m_producerTypes;
    private IProvider m_provider;
    private IMediator m_producerMediator;
    private Object m_data;
    private TYPE m_typeProducer;
    private RC m_error;
    private int m_bufferSize;
    private final TYPE[] m_outputTypes;
    private final WParserSemantic m_parser;

    public Writer() {
        m_parser = new WParserSemantic();
        m_outputTypes = new TYPE[] {TYPE.BYTE_ARRAY, TYPE.CHAR_ARRAY, TYPE.INT_ARRAY};
    }

    @Override
    public RC setConfig(String s) {
        if (s == null) {
            return new RC(RC.RCWho.WRITER,
                          RC.RCType.CODE_INVALID_ARGUMENT,
                     "The name of the configuration file is missing");

        }else {
            return m_parser.analyze(s);
        }
    }

    @Override
    public RC setProvider(IProvider iProvider) {
        if (iProvider == null) {

            return new RC(RC.RCWho.EXECUTOR,
                          RC.RCType.CODE_FAILED_PIPELINE_CONSTRUCTION,
                     "Writer's producer is null");

        } else {
            m_provider = iProvider;
            m_producerTypes = m_provider.getOutputTypes();
            for(int i = 0; i < m_outputTypes.length; i++ ){
                for(int j = 0; j < m_producerTypes.length; j++){
                    if(m_producerTypes[j] == m_outputTypes[i]){
                        m_typeProducer = m_producerTypes[j];

                        m_producerMediator = m_provider.getMediator(m_typeProducer);
                        if (m_producerMediator == null){

                            return new RC(RC.RCWho.EXECUTOR,
                                          RC.RCType.CODE_FAILED_PIPELINE_CONSTRUCTION,
                                     "Mediator's producer is null");
                        }
                        return RC.RC_SUCCESS;
                    }
                }
            }
        }
        return new RC(RC.RCWho.EXECUTOR,
                      RC.RCType.CODE_FAILED_PIPELINE_CONSTRUCTION,
                 "The intersection of types is zero");
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
    public RC consume() {
        m_bufferSize = m_parser.getData();

        m_data = m_producerMediator.getData();
        if (m_data == null){
            return new RC(RC.RCWho.WRITER,
                          RC.RCType.CODE_INVALID_ARGUMENT,
                     "The input data is not valid");
        }

        byte[] bytes = translateToByte(m_typeProducer);
        if (bytes == null) {
            return new RC(RC.RCWho.WRITER,
                          RC.RCType.CODE_INVALID_ARGUMENT,
                     "Writer no receive data");
        }

        if (bytes.length <= m_bufferSize) {

            m_error = writeData(bytes);
            if (m_error != RC.RC_SUCCESS) {
                return m_error;
            }
        } else {
            return new RC(RC.RCWho.WRITER,
                          RC.RCType.CODE_INVALID_ARGUMENT,
                     "Exceeding the size of the input data");
        }
        return RC.RC_SUCCESS;
    }

    private byte[] translateToByte(TYPE type){

        byte[] bytes;
        if (type == null){
            return null;
        }
        switch (type){
            case BYTE_ARRAY: bytes = fromByte();
                break;
            case CHAR_ARRAY: bytes = fromChar();
                break;
            case INT_ARRAY:  bytes = fromInt();
                break;
            default:         bytes = null;
                break;
        }
        return bytes;
    }

    private byte[] fromByte(){
        byte []bytes = new byte[((byte[])m_data).length];
        System.arraycopy(((byte[])m_data), 0, bytes, 0, bytes.length);
        return bytes;
    }

    private byte[] fromChar(){

        byte[] bytes = new byte[((char[])m_data).length];

        for(int i = 0; i < bytes.length; ++i) {
            bytes[i] = (byte)((char[])m_data)[i];
        }

        return bytes;
    }

    private byte[] fromInt(){
        byte[] bytes = new byte[((int[])m_data).length * 4];
        ByteBuffer.wrap(bytes).asIntBuffer().put((int[]) m_data);
        return bytes;
    }
}