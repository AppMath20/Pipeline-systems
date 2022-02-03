import com.java_polytech.pipeline_interfaces.*;
import java.nio.ByteBuffer;

public class Executor implements IExecutor,INotifiable {

    private TYPE[] m_producerTypes;
    private IProvider m_provider;
    private INotifier m_notifier;
    private IMediator m_producerMediator;
    private IMediator m_executorMediator;
    private Object m_data;
    private TYPE m_typeProducer;
    private byte[] m_bytes;
    private final TYPE[] m_outputTypes;
    private volatile int m_waitedChunks;
    private final EParserSemantic m_parser;

    public Executor() {
        m_parser = new EParserSemantic();
        m_outputTypes = new TYPE[] {TYPE.BYTE_ARRAY};
    }

    public byte[] cyclicShift(int m_sizeLong) {

        switch (m_parser.SIGN){
            case 1:
                reverse(0, m_sizeLong);
                reverse(m_sizeLong + 1, m_bytes.length - 1);
                reverse(0, m_bytes.length - 1);
                break;

            case 0:
                reverse(0, m_bytes.length - 1);
                reverse(0, m_sizeLong);
                reverse(m_sizeLong + 1, m_bytes.length - 1);
                break;

            default:
                return null;
        }
        return m_bytes;
    }

    private void reverse(int start, int end) {
        for (int i = start; i < (start + end) / 2; i++) {

            byte temp = m_bytes[start + end - i];
            m_bytes[start + end - i] = m_bytes[i];
            m_bytes[i] = temp;
        }
    }

    @Override
    public RC setConfig(String s) {
        if (s == null) {

            return new RC(RC.RCWho.EXECUTOR,
                          RC.RCType.CODE_INVALID_ARGUMENT,
                    "The name of the configuration file is missing");

        } else {
            return m_parser.analyze(s);
        }
    }

    private class Notifier implements INotifier {
        @Override
        public RC notify(int chunkId) {
            synchronized (Executor.this) {
                m_waitedChunks++;
            }
            return RC.RC_SUCCESS;
        }
    }

    @Override
    public INotifier getNotifier() {
        return new Notifier();
    }

    @Override
    public RC addNotifier(INotifier iNotifier) {
        if (iNotifier == null){

            return new RC(RC.RCWho.READER,
                    RC.RCType.CODE_INVALID_ARGUMENT,
                    "Value is not valid");
        }
        m_notifier = iNotifier;
        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer iConsumer) {

        return RC.RC_SUCCESS;
    }

    @Override
    public RC setProvider(IProvider iProvider) {
        if (iProvider == null) {

            return new RC(RC.RCWho.EXECUTOR,
                          RC.RCType.CODE_FAILED_PIPELINE_CONSTRUCTION,
                    "Executor's producer is null");
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
    public RC consume() {

        m_data = m_producerMediator.getData();
        if (m_data == null){
            return RC.RC_SUCCESS;
        }

        m_bytes = translateToByte(m_typeProducer);
        if (m_bytes == null) {

            return new RC(RC.RCWho.EXECUTOR,
                          RC.RCType.CODE_INVALID_ARGUMENT,
                     "The executor`s do not received data");
        }

        m_bytes = cyclicShift(m_parser.SIZE_LONG);

        m_notifier.notify(0);

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
            case BYTE_ARRAY: m_executorMediator = new ByteMediator();
                break;
            case CHAR_ARRAY: m_executorMediator = new CharMediator();
                break;
            case INT_ARRAY:  m_executorMediator = new IntMediator();
                break;
            default:         m_executorMediator = null;
                break;
        }
        return m_executorMediator;
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