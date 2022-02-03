import com.java_polytech.pipeline_interfaces.IConsumer;
import com.java_polytech.pipeline_interfaces.IExecutor;
import com.java_polytech.pipeline_interfaces.RC;

public class Executor implements IExecutor {

    private int m_longSize;
    private byte[] m_bytes;
    private IConsumer m_consumer;
    private final EParserSemantic m_parser;

    public Executor() {
        m_parser = new EParserSemantic();
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
        if (s != null) {
            return m_parser.analyze(s);

        } else {
            return new RC(RC.RCWho.EXECUTOR,
                          RC.RCType.CODE_INVALID_ARGUMENT,
                     "The name of the configuration file is missing");
        }
    }

    @Override
    public RC setConsumer(IConsumer iConsumer) {
        if (iConsumer == null) {

            return new RC(RC.RCWho.EXECUTOR,
                          RC.RCType.CODE_FAILED_PIPELINE_CONSTRUCTION,
                     "Executor's consumer is null");
        } else {
            m_consumer = iConsumer;
            return RC.RC_SUCCESS;
        }
    }

    @Override
    public RC consume(byte[] data) {
        if (data == null) {

            return new RC(RC.RCWho.EXECUTOR,
                          RC.RCType.CODE_INVALID_ARGUMENT,
                     "The executor`s do not received data");
        }else {
            m_bytes = data;
            m_longSize = m_parser.getData();
            m_bytes = cyclicShift(m_longSize);
            return m_consumer.consume(m_bytes);
        }
    }
}