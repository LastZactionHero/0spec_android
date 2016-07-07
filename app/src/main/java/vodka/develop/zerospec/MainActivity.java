package vodka.develop.zerospec;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ZeroSpecActivity";
    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
//    private OutputSteam mOutputStream;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");

            sendNotification(title + " " + text);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));


        connectToBluetooth();
        sendTime();
        sendNotification("Connected to phone");
    }

    private void sendTime() {
        Calendar c = Calendar.getInstance();
        String notification = String.format("C%2d%2d%2d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));

        try {
            Log.v(TAG, "Sending Time");
            mOutputStream.write(notification.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Failed to get write");
            e.printStackTrace();
        }

    }

    private void sendNotification(String message) {
        String notification = "N" + message + "\n";

        try {
            Log.v(TAG, "Sending String");
            mOutputStream.write(notification.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Failed to get write");
            e.printStackTrace();
        }
    };

    private void connectToBluetooth() {
        Log.v(TAG, "CONNECTING");

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                Log.v(TAG, device.getName());
                if(device.getName().equals("DFU")){
                    Log.v(TAG, "FOUND DEVICE!");
                    mDevice = device;
                    break;
                }
            }
        }

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to create RF Comm Socket");
        }
        try {
            mSocket.connect();
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect");
            e.printStackTrace();
        }

        try {
            mOutputStream = mSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Failed to get output stream");
            e.printStackTrace();
        }
        try {
            mInputStream = mSocket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Failed to get input stream");
            e.printStackTrace();
        }
    }
}
