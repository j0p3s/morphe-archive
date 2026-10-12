import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.RemoteException;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import stub.ProviderPrefs;
import stub.BatteryActivity;
import stub.BatteryMonitorReceiver;
import stub.BatteryMonitorWindow;
import stub.BatteryMonitorService;
import stub.BatteryActivityTask;
import stub.BinderContainer;
import stub.BluetoothActivity;
import stub.BluetoothTask;
import stub.BootReceiver;
import stub.CpuTimeActivity;
import stub.MonitorActivity;
import stub.MonitorPause;
import stub.MonitorReceiver;
import stub.DashboardFragment;
import stub.NetworkDialogTask;
import stub.DeviceNetwork;
import stub.Telephony;
import stub.CpuFrequency;
import stub.DeviceFiles;
import stub.TimeInStateRow;
import stub.UpdateReceiver;
import stub.PauseResumeReceiver;
import stub.BindTask;
import stub.EncryptedPrefs;
import stub.SensorActivity;
import stub.PublicIpTask;
import stub.RemoteProcess;
import stub.IRootServiceManager;
import stub.IRootServiceManagerProxy;
import stub.RootServiceKeys;
import stub.RootServiceManager;
import stub.RootServiceReceiver;
import stub.HardwareFragment;
import stub.HardwareReceiver;
import stub.InfoRow;
import stub.Shizuku;
import stub.ShizukuReceiver;
import stub.StringRes;
import stub.CpuTimeSensorTask;
import stub.TempMonitorReceiver;
import stub.TempMonitorWindow;
import stub.TimeInState;

public final class DevCheckRestored {

    private DevCheckRestored() {
    }

    public static Object cpuTimeSensorDoInBackground(CpuTimeSensorTask task, Object[] params) {
        Object owner = task.owner;
        if (owner instanceof CpuTimeActivity) {
            return cpuTimes((CpuTimeActivity) owner);
        }
        return sensorDetails((SensorActivity) owner);
    }

    public static Object batteryActivityDoInBackground(BatteryActivityTask task, Object[] params) {
        if (task.mode == 0) {
            restartBatteryMonitor(task.activity);
        }
        else {
            sleep(750);
        }
        return Boolean.TRUE;
    }

    private static void restartBatteryMonitor(BatteryActivity activity) {
        if (activity.entries.size() > 2) {
            activity.entries.remove(2);
        }
        activity.stopService(new Intent(activity.getApplicationContext(), BatteryMonitorService.class));
        sleep(500);
        if (EncryptedPrefs.get().getBoolean("prefBMEnable", false)) {
            Intent intent = new Intent(activity.getApplicationContext(), BatteryMonitorService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                activity.startForegroundService(intent);
            }
            else {
                activity.startService(intent);
            }
        }
        sleep(500);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public static Object bluetoothDoInBackground(BluetoothTask task, Object[] params) {
        BluetoothActivity activity = task.activity;
        ArrayList rows = new ArrayList();
        try {
            Thread.sleep(task.delay);
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        try {
            if (Build.VERSION.SDK_INT >= 31
                    && activity.checkSelfPermission("android.permission.BLUETOOTH_SCAN") != 0) {
                activity.runOnUiThread(new BluetoothMessage(activity, activity.getString(StringRes.permission_denied)
                        + "\nandroid.permission.BLUETOOTH_SCAN"));
            }
            Set devices = activity.adapter.getBondedDevices();
            if (devices.size() > 0) {
                for (Object device : devices) {
                    BluetoothDevice bluetoothDevice = (BluetoothDevice) device;
                    rows.add(new InfoRow(37, bluetoothDevice.getName(), bluetoothDevice.getAddress()));
                }
            }
            else {
                activity.runOnUiThread(new BluetoothMessage(activity, activity.getString(StringRes.none)));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return rows;
    }

    public static Object publicIpDoInBackground(PublicIpTask task, Object[] params) {
        StringBuilder text = new StringBuilder();
        Context context = task.resultText.getContext();
        try {
            text.append("\n");
            text.append(publicIp());
            return text.toString();
        }
        catch (Exception ex) {
            try {
                return (text.toString().length() > 6) ? text.toString() : context.getString(StringRes.not_avail);
            }
            catch (Exception nested) {
                return context.getString(StringRes.not_avail);
            }
        }
    }

    private static String publicIp() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL("https://api.ipify.org").openConnection().getInputStream()))) {
            return reader.readLine();
        }
        catch (Exception ex) {
            return "";
        }
    }

    public static Object networkDialogDoInBackground(NetworkDialogTask task, Object[] params) {
        ArrayList rows = new ArrayList();
        try {
            Context context = task.list.getContext();
            Context application = context.getApplicationContext();
            TelephonyManager telephony = (TelephonyManager) application.getSystemService("phone");
            ConnectivityManager connectivity = (ConnectivityManager) application.getSystemService("connectivity");
            NetworkInfo active = connectivity.getActiveNetworkInfo();
            WifiManager wifiManager = (WifiManager) application.getSystemService("wifi");
            WifiInfo wifiInfo = null;
            boolean wifi = false;
            if (active != null && active.getType() == ConnectivityManager.TYPE_WIFI) {
                wifiInfo = wifiManager.getConnectionInfo();
                wifi = true;
            }
            boolean ethernetActive = active != null && active.getType() == ConnectivityManager.TYPE_ETHERNET;
            boolean ethernet = false;
            try {
                ArrayList interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (int i = 0; i < interfaces.size(); i++) {
                    if (((NetworkInterface) interfaces.get(i)).getName().contains("eth")) {
                        ethernet = true;
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            if (ethernet) {
                rows.add(new InfoRow(17, context.getString(StringRes.ethernet), null));
                if (ethernetActive) {
                    addEthernetRows(context, connectivity, rows);
                }
                else {
                    rows.add(new InfoRow(1, context.getString(StringRes.status),
                            context.getString(StringRes.disconnected)));
                }
            }
            rows.add(new InfoRow(17, context.getString(StringRes.wifi), null));
            if (!wifi) {
                rows.add(new InfoRow(14, context.getString(StringRes.status), context.getString(StringRes.disconnected)));
            }
            else if (wifiInfo != null) {
                addWifiRows(context, wifiInfo, rows);
            }
            if (!Telephony.phoneType(context, telephony).equals("None")) {
                addMobileRows(context, telephony, active, task.mobileConnected, rows);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return rows;
    }

    private static void addEthernetRows(Context context, ConnectivityManager connectivity, ArrayList rows) {
        rows.add(new InfoRow(14, context.getString(StringRes.status), context.getString(StringRes.connected)));
        rows.add(new InfoRow(14, context.getString(StringRes.ip_address), DeviceNetwork.ipAddress()));
        String proxyHost = System.getProperty("http.proxyHost");
        if (proxyHost != null) {
            rows.add(new InfoRow(14, "HTTP Proxy", proxyHost + ":" + System.getProperty("http.proxyPort")));
        }
        try {
            for (Network network : connectivity.getAllNetworks()) {
                LinkProperties link = connectivity.getLinkProperties(network);
                rows.add(new InfoRow(14, context.getString(StringRes.network_interface), link.getInterfaceName()));
                try {
                    String routes = link.getRoutes().toString();
                    String afterArrow = routes.substring(routes.lastIndexOf("-> ") + 3);
                    String gateway = afterArrow.substring(0, afterArrow.indexOf(" "));
                    if (gateway.length() > 6) {
                        rows.add(new InfoRow(14, context.getString(StringRes.gateway), gateway));
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
                try {
                    rows.add(new InfoRow(14, context.getString(StringRes.domain), link.getDomains()));
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
                rows.add(new InfoRow(14, "DNS1", link.getDnsServers().get(0).toString().substring(1)));
                rows.add(new InfoRow(14, "DNS2", link.getDnsServers().get(1).toString().substring(1)));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void addWifiRows(Context context, WifiInfo wifiInfo, ArrayList rows) {
        rows.add(new InfoRow(14, context.getString(StringRes.status), context.getString(StringRes.connected)));
        if (context.checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == 0
                && !TextUtils.isEmpty(wifiInfo.getSSID()) && !wifiInfo.getHiddenSSID()) {
            rows.add(new InfoRow(14, context.getString(StringRes.network), wifiInfo.getSSID().replace("\"", "")));
        }
        rows.add(new InfoRow(14, context.getString(StringRes.ip_address), DeviceNetwork.ipAddress()));
        rows.add(new InfoRow(14, context.getString(StringRes.link_speed), wifiInfo.getLinkSpeed() + " Mbps"));
        rows.add(new InfoRow(14, context.getString(StringRes.signal_strength), wifiInfo.getRssi() + " dBm"));
    }

    private static void addMobileRows(Context context, TelephonyManager telephony, NetworkInfo active,
            boolean connected, ArrayList rows) {
        rows.add(new InfoRow(17, context.getString(StringRes.mobile), null));
        rows.add(new InfoRow(14, context.getString(StringRes.status),
                context.getString(connected ? StringRes.connected : StringRes.disconnected)));
        rows.add(new InfoRow(14, context.getString(StringRes.phone_type), Telephony.phoneType(context, telephony)));
        if (connected) {
            rows.add(new InfoRow(14, "APN", active.getExtraInfo()));
            rows.add(new InfoRow(14, context.getString(StringRes.ip_address), DeviceNetwork.ipAddress()));
        }
        rows.add(new InfoRow(17, context.getString(StringRes.sim), null));
        int simState = telephony.getSimState();
        rows.add(new InfoRow(14, context.getString(StringRes.state), Telephony.simState(context, simState)));
        if (simState == TelephonyManager.SIM_STATE_ABSENT || simState == TelephonyManager.SIM_STATE_UNKNOWN) {
            return;
        }
        rows.add(new InfoRow(14, context.getString(StringRes.operator), Telephony.operator(context)));
        boolean phoneState = context.checkSelfPermission("android.permission.READ_PHONE_STATE") == 0;
        if (phoneState) {
            try {
                String networkType = DashboardFragment.networkTypeReader.read();
                rows.add(new InfoRow(14, context.getString(StringRes.network_type), networkType));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            String number = telephony.getLine1Number();
            String shown = (number == null || number.equals("")) ? context.getString(StringRes.not_avail)
                    : PhoneNumberUtils.formatNumber(number);
            rows.add(new InfoRow(14, context.getString(StringRes.phone_number), shown));
        }
    }

    public static void bootReceive(BootReceiver receiver, Context context, Intent intent) {
        if (!"android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            return;
        }
        EncryptedPrefs.putBoolean("prefCPUTimeSaveOffsets", false);
        EncryptedPrefs.putBoolean("prefBootReceiver", true);
        try {
            BootReceiver.startBatteryMonitor(context);
        }
        catch (Exception ex) {
            BootReceiver.startBatteryMonitor(context);
        }
        try {
            receiver.startMonitorWindows(context);
        }
        catch (Exception ex) {
            receiver.startMonitorWindows(context);
        }
    }

    public static void monitorReceive(MonitorReceiver receiver, Context context, Intent intent) {
        context.startActivity(new Intent(context, MonitorActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public static void pauseResumeReceive(PauseResumeReceiver receiver, Context context, Intent intent) {
        if (!"flar2.devcheck.ACTION_MASTER_MONITOR_TOGGLE".equals(intent.getAction())) {
            return;
        }
        SharedPreferences monitors = context.getSharedPreferences("monitors", Context.MODE_PRIVATE);
        boolean paused = monitors.getBoolean("prefMonitorPaused", false);
        monitors.edit().putBoolean("prefMonitorPaused", !paused).commit();
        Intent toggle = new Intent("flar2.devcheck.ACTION_MONITOR_TOGGLE");
        toggle.setPackage("flar2.devcheck");
        context.sendBroadcast(toggle);
    }

    public static void tempMonitorReceive(TempMonitorReceiver receiver, Context context, Intent intent) {
        TempMonitorWindow window = receiver.window;
        if (receiver.mode == 0) {
            window.showTemperature(intent.getIntExtra("temperature", 0));
            return;
        }
        String action = intent.getAction();
        if ("android.intent.action.SCREEN_ON".equals(action)) {
            if (window.usesBatteryTemperature) {
                window.getApplicationContext().registerReceiver(window.batteryReceiver,
                        new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            }
            else {
                window.handler.post(window.poller);
            }
        }
        else if ("android.intent.action.SCREEN_OFF".equals(action)) {
            if (window.usesBatteryTemperature) {
                window.getApplicationContext().unregisterReceiver(window.batteryReceiver);
            }
            else {
                window.handler.removeCallbacks(window.poller);
            }
        }
        else if ("flar2.devcheck.ACTION_MONITOR_TOGGLE".equals(action)) {
            MonitorPause.offsetView(window.view);
        }
    }

    public static void batteryMonitorReceive(BatteryMonitorReceiver receiver, Context context, Intent intent) {
        BatteryMonitorWindow window = receiver.window;
        if (receiver.mode == 0) {
            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", -1);
            int temperature = intent.getIntExtra("temperature", 0) / 10;
            window.levelText.setText(((int) (level * 100.0f / scale)) + "%");
            try {
                if (ProviderPrefs.instance.getBoolean("prefFahrenheit")) {
                    window.temperatureText.setText(((int) (temperature * 1.8 + 32.0)) + "\u00b0F");
                }
                else {
                    window.temperatureText.setText(temperature + "\u00b0C");
                }
            }
            catch (Exception ex) {
                window.temperatureText.setText(temperature + "\u00b0C");
            }
            return;
        }
        String action = intent.getAction();
        if ("android.intent.action.SCREEN_ON".equals(action)) {
            window.getApplicationContext().registerReceiver(window.batteryReceiver,
                    new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        }
        else if ("android.intent.action.SCREEN_OFF".equals(action)) {
            window.getApplicationContext().unregisterReceiver(window.batteryReceiver);
        }
        else if ("flar2.devcheck.ACTION_MONITOR_TOGGLE".equals(action)) {
            MonitorPause.offsetView(window.view);
        }
    }

    public static void shizukuBinderReceive(ShizukuReceiver receiver, Context context, Intent intent) {
        BinderContainer container = (BinderContainer) intent
            .getParcelableExtra("moe.shizuku.privileged.api.intent.extra.BINDER");
        if (container != null && container.binder != null) {
            Shizuku.onBinderReceived(container.binder, context.getPackageName());
        }
    }

    public static void rootServiceReceive(RootServiceReceiver receiver, Context context, Intent intent) {
        if (receiver.mode != 0) {
            return;
        }
        RootServiceManager manager = (RootServiceManager) receiver.owner;
        Bundle bundle = intent.getBundleExtra(RootServiceKeys.intentBundle);
        if (bundle == null) {
            return;
        }
        IBinder binder = bundle.getBinder("binder");
        if (binder == null) {
            return;
        }
        IInterface local = binder.queryLocalInterface("com.topjohnwu.superuser.internal.IRootServiceManager");
        IRootServiceManager rootManager;
        if (local instanceof IRootServiceManager) {
            rootManager = (IRootServiceManager) local;
        }
        else {
            IRootServiceManagerProxy proxy = new IRootServiceManagerProxy();
            proxy.remote = binder;
            rootManager = proxy;
        }
        try {
            rootManager.connect(((Messenger) receiver.messenger).getBinder());
            RemoteProcess process = new RemoteProcess(manager, rootManager);
            if (intent.getBooleanExtra("extra.daemon", false)) {
                manager.daemon = process;
                manager.flags &= ~2;
            }
            else {
                manager.remote = process;
                manager.flags &= ~1;
            }
            ArrayList tasks = manager.pendingTasks;
            for (int i = tasks.size() - 1; i >= 0; i--) {
                BindTask task = (BindTask) tasks.get(i);
                if (manager.bind(task.intent, task.executor, task.connection) == null) {
                    tasks.remove(i);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void updateReceive(UpdateReceiver receiver, Context context, Intent intent) {
        if (intent == null || intent.getAction() == null || context == null) {
            return;
        }
        try {
            String action = intent.getAction();
            boolean replaced = "android.intent.action.MY_PACKAGE_REPLACED".equals(action)
                    || ("android.intent.action.PACKAGE_REPLACED".equals(action) && intent.getData() != null
                            && intent.getData().getSchemeSpecificPart().equals(context.getPackageName()));
            if (replaced) {
                EncryptedPrefs.putBoolean("pbc", true);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void hardwareReceive(HardwareReceiver receiver, Context context, Intent intent) {
        HardwareFragment fragment = receiver.fragment;
        if (receiver.mode != 0) {
            if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(intent.getAction())) {
                new Handler().postDelayed(new HardwareReload(fragment), 1000);
            }
            return;
        }
        if (!BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
            return;
        }
        ArrayList rows = fragment.nearbyRows;
        if (rows.size() == 1 && ((InfoRow) rows.get(0)).type == 1) {
            rows.remove(0);
        }
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String address = device.getAddress();
        float rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
        int strength = (int) (167.0f - Math.abs(rssi) * 1.6666666f);
        for (Object row : rows) {
            if (address.equals(((InfoRow) row).address)) {
                return;
            }
        }
        String connectPermission = "android.permission.BLUETOOTH_CONNECT";
        if (Build.VERSION.SDK_INT >= 31
                && fragment.requireContext().checkSelfPermission(connectPermission) != 0) {
            fragment.requestPermissions(new String[] { connectPermission }, 134);
        }
        else {
            String name = device.getName();
            if (TextUtils.isEmpty(name)) {
                name = "<" + fragment.getString(StringRes.unknown) + ">";
            }
            rows.add(new InfoRow(name, "", address, rssi + "dBm", strength, 19));
        }
        fragment.nearbyAdapter.notifyDataSetChanged();
    }

    private static List sensorDetails(SensorActivity activity) {
        ArrayList rows = new ArrayList();
        Sensor sensor = activity.sensor;
        if (sensor == null) {
            return rows;
        }
        Intent intent = activity.getIntent();
        rows.add(new InfoRow(0, activity.getString(StringRes.information), null));
        if (intent.getIntExtra("sensorType", 1) == Sensor.TYPE_STEP_COUNTER) {
            String steps = (activity.steps == 0) ? activity.getString(StringRes.not_avail) : String.valueOf(activity.steps);
            rows.add(new InfoRow(1, activity.getString(StringRes.total_steps), steps));
        }
        rows.add(new InfoRow(1, activity.getString(StringRes.manufacturer), intent.getStringExtra("sensorMfg")));
        rows.add(new InfoRow(1, activity.getString(StringRes.model), intent.getStringExtra("sensorModel")));
        rows.add(new InfoRow(1, activity.getString(StringRes.resolution), SensorActivity.formatRange(sensor, false)));
        rows.add(new InfoRow(1, activity.getString(StringRes.max_range), SensorActivity.formatRange(sensor, true)));
        String power = new DecimalFormat("0.000#######").format(sensor.getPower()) + " mA";
        rows.add(new InfoRow(1, activity.getString(StringRes.power), power));
        String wakeUp = activity.getString(sensor.isWakeUpSensor() ? StringRes.yes : StringRes.no);
        rows.add(new InfoRow(1, activity.getString(StringRes.wakeup_sensor), wakeUp));
        return rows;
    }

    private static List cpuTimes(CpuTimeActivity activity) {
        ArrayList rows = new ArrayList();
        DecimalFormat format = new DecimalFormat("0.0");
        readTimeInState(activity);
        try {
            if (DeviceFiles.exists(activity.timeInStatePath)) {
                TimeInStateRow header = new TimeInStateRow();
                header.type = 0;
                header.title = activity.getString(StringRes.cpu_time_in_state);
                rows.add(header);
                ArrayList states = activity.states;
                for (int i = states.size() - 1; i >= 0; i--) {
                    TimeInState state = (TimeInState) states.get(i);
                    TimeInStateRow row = new TimeInStateRow();
                    row.type = 2;
                    row.title = state.name;
                    row.duration = state.duration;
                    row.percent = format.format(Double.parseDouble(state.percent)) + "%";
                    row.progress = state.progress;
                    rows.add(row);
                }
            }
            else {
                rows.add(notAvailable(activity));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            rows.add(notAvailable(activity));
        }
        TimeInStateRow footer = new TimeInStateRow();
        footer.type = 3;
        rows.add(footer);
        return rows;
    }

    private static TimeInStateRow notAvailable(CpuTimeActivity activity) {
        TimeInStateRow row = new TimeInStateRow();
        row.type = 0;
        row.title = activity.getString(StringRes.not_available);
        return row;
    }

    private static String formatDuration(long ticks) {
        long millis = ticks * 10;
        long hours = millis / 3600000;
        long minutes = millis / 60000;
        long seconds = millis / 1000 - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d:%02d", hours, minutes - TimeUnit.HOURS.toMinutes(hours), seconds);
    }

    private static String[] readLines(String path) {
        ArrayList lines = new ArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader(path), 512)) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                lines.add(line);
            }
        }
        catch (Exception ex) {
            return new String[0];
        }
        return (String[]) lines.toArray(new String[lines.size()]);
    }

    private static void readTimeInState(CpuTimeActivity activity) {
        if (!DeviceFiles.exists(activity.timeInStatePath)) {
            return;
        }
        SharedPreferences prefs = EncryptedPrefs.get();
        try {
            String[] lines = readLines(activity.timeInStatePath);
            ArrayList states = activity.states;
            states.clear();
            double total = 0;
            for (String line : lines) {
                if (line.contains("freq") || line.contains("N/A")) {
                    continue;
                }
                total += Long.parseLong(line.split("\\s+")[1]);
                if (total < 0) {
                    total = 0;
                }
            }
            long deepSleep = 0;
            if (!prefs.getBoolean("prefCPUTimeDeepSleep", false)) {
                deepSleep = Math.max((SystemClock.elapsedRealtime() - SystemClock.uptimeMillis()) / 10, 0);
                total += deepSleep;
            }
            ArrayList offsets = null;
            if (prefs.getBoolean("prefCPUTimeSaveOffsets", false)) {
                String saved;
                if (prefs.getBoolean("prefCPUTimecluster2", false)) {
                    saved = prefs.getString("prefCPUTimeOffsetsBig", null);
                }
                else if (prefs.getBoolean("prefCPUTimecluster3", false)) {
                    saved = prefs.getString("prefCPUTimeOffsetsPerf", null);
                }
                else {
                    saved = prefs.getString("prefCPUTimeOffsetsLittle", null);
                }
                offsets = new ArrayList(Arrays.asList(saved.split(",")));
                long offsetSum = 0;
                for (int i = 0; i < offsets.size(); i++) {
                    offsetSum += Long.parseLong((String) offsets.get(i));
                }
                total -= offsetSum;
                if (!prefs.getBoolean("prefCPUTimeDeepSleep", false)) {
                    long deepSleepOffset;
                    try {
                        deepSleepOffset = Long.parseLong(prefs.getString("prefCPUTimeDeepSleepOffset", null));
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                        EncryptedPrefs.putBoolean("prefCPUTimeSaveOffsets", false);
                        deepSleepOffset = 0;
                    }
                    total = Math.max(total - deepSleepOffset, 0);
                    deepSleep = Math.max(deepSleep - deepSleepOffset, 0);
                }
            }
            long totalTime = (long) total;
            states.add(new TimeInState(activity.getString(StringRes.total), totalTime, formatDuration(totalTime), "100",
                    100));
            if (!prefs.getBoolean("prefCPUTimeDeepSleep", false)) {
                double percent = Math.min(Math.round(deepSleep / total * 1000.0) / 10.0, 100.0);
                states.add(new TimeInState(activity.getString(StringRes.deep_sleep), deepSleep, formatDuration(deepSleep),
                        Double.toString(percent), (int) Math.round(percent)));
            }
            int offsetIndex = 0;
            for (String line : lines) {
                if (line.contains("freq") || line.contains("N/A")) {
                    continue;
                }
                String frequency = line.split("\\s+")[0];
                long time = Long.parseLong(line.split("\\s+")[1]);
                if (prefs.getBoolean("prefCPUTimeSaveOffsets", false) && offsets != null) {
                    time -= Long.parseLong((String) offsets.get(offsetIndex));
                }
                double permille = Math.min((double) Math.round(time / total * 1000.0), 1000.0);
                double percent = Math.min(permille / 10.0, 100.0);
                if (time > 0) {
                    states.add(new TimeInState(CpuFrequency.label(frequency), time, formatDuration(time),
                            Double.toString(percent), (int) Math.round(percent)));
                }
                if (prefs.getBoolean("prefCPUTimeSort", false)) {
                    Collections.sort(states);
                }
                offsetIndex++;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static final class BluetoothMessage implements Runnable {

        private final BluetoothActivity activity;

        private final String message;

        BluetoothMessage(BluetoothActivity activity, String message) {
            this.activity = activity;
            this.message = message;
        }

        @Override
        public void run() {
            this.activity.emptyView.setVisibility(0);
            this.activity.emptyText.setText(this.message);
        }

    }

    private static final class HardwareReload implements Runnable {

        private final HardwareFragment fragment;

        HardwareReload(HardwareFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void run() {
            if (this.fragment.isAdded()) {
                this.fragment.viewModel.reload();
            }
        }

    }

}
