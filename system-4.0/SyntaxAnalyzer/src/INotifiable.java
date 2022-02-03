import com.java_polytech.pipeline_interfaces.RC;

public interface INotifiable {

    RC addNotifier(INotifier iNotifier);
    INotifier getNotifier();
}
