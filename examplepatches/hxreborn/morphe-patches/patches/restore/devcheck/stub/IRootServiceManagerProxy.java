package stub;

import android.os.IBinder;

public class IRootServiceManagerProxy implements IRootServiceManager {

    public IBinder remote;

    @Override
    public void connect(IBinder binder) {
    }

    @Override
    public IBinder asBinder() {
        return null;
    }

}
