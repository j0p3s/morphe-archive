package stub;

import android.content.Intent;
import android.content.ServiceConnection;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class RootServiceManager {

    public RemoteProcess remote;

    public RemoteProcess daemon;

    public int flags;

    public ArrayList pendingTasks;

    public Object bind(Intent intent, Executor executor, ServiceConnection connection) {
        return null;
    }

}
