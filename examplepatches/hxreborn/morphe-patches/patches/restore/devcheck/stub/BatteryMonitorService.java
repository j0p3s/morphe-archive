package stub;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BatteryMonitorService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
