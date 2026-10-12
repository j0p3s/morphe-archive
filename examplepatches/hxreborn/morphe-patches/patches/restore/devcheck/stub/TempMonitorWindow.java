package stub;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;

public class TempMonitorWindow extends Service {

    public boolean usesBatteryTemperature;

    public BroadcastReceiver batteryReceiver;

    public Handler handler;

    public Runnable poller;

    public View view;

    public void showTemperature(int temperature) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
