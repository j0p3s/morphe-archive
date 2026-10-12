package stub;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;

public class BatteryMonitorWindow extends Service {

    public BroadcastReceiver batteryReceiver;

    public View view;

    public TextView levelText;

    public TextView temperatureText;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
