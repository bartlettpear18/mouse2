package com.example.joelbartlett.test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    //Constants
    private String TAG = "debug";
    private String UUID_STR ="0b938f75-7fe7-4a04-9079-579d78ad64b7";
    private UUID UUID_CODE = UUID.fromString(UUID_STR);

    //Bluetooth objects
    private BluetoothAdapter adapter;
    private BluetoothDevice serverDevice;
    private BluetoothSocket socket;

    //Stream Objects
    private InputStream inputStream;
    private OutputStream outputStream;

    //Mouse hardware
    private boolean running = false;
    private final int SCALAR = 10;
    private final int DELTA_TIME = 10; //milliseconds
    private final int X_LIMIT = 1;
    private final int Y_LIMIT = 1;

    public Integer mouseX = 0;
    public Integer mouseY = 0;


    private int prevPosX = 0;
    private int prevPosY = 0;

    private int currentPosX;
    private int currentPosY;

    private int xVel0 = 0;
    private int yVel0 = 0;

    //Sensor setup
    private SensorManager sensors;
    private final double Z_LIMIT = 0.1;
    private float xAccel;
    private float yAccel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensors = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensors.registerListener(this, sensors.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
        running = true;

        adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.enable();

        if(!adapter.isEnabled()) adapter.enable(); Log.d(TAG, "Adapter enabled");

        findDevice("5C:5F:67:30:C4:CD");
        try {
            setupConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            running = false;
            terminate();
            Log.d(TAG, "Connection terminated");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Find BluetoothDevice of Server
     * @param serverMAC
     */
    private void findDevice(String serverMAC) {
        for(BluetoothDevice bt : adapter.getBondedDevices())
            if(bt.getAddress().equals(serverMAC)) serverDevice = bt;

        Log.d(TAG,"Server Device Found: " + serverDevice.getName());
    }

    /**
     * Create and connect Socket
     * @throws IOException
     */
    private void setupConnection() throws IOException {
        socket = serverDevice.createRfcommSocketToServiceRecord(UUID_CODE);
        Log.d(TAG, "Socket created");

        adapter.cancelDiscovery();
        socket.connect();
        Log.d(TAG, "Socket connected");

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        Log.d(TAG, "Streams connected");

        sendPackets.start();
    }

    /**
     * Close connection
     * @throws IOException
     */
    private void terminate() throws IOException {
        socket.close();
        inputStream.close();
        outputStream.close();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            if(event.values[2] < Z_LIMIT) {
                xAccel = event.values[0];
                yAccel = event.values[1];
//                Log.d(TAG, xAccel + ", " + yAccel);

            } else {
                xAccel = 0;
                yAccel = 0;
//                Log.d(TAG, "0, 0");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    Thread sendPackets = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true) {


                if(xAccel > X_LIMIT) {
                    currentPosX = (int) (xAccel * DELTA_TIME * DELTA_TIME)/2 + (xVel0 * DELTA_TIME);
                    xVel0 = (int) (xVel0 + xAccel * DELTA_TIME);
                    mouseX = (currentPosX - prevPosX)/10;
                    prevPosX = currentPosX;
                } else {
                    mouseX = 0;
                }

                if(yAccel > Y_LIMIT) {
                    currentPosY = (int) (yAccel * DELTA_TIME * DELTA_TIME)/2 + (yVel0 * DELTA_TIME);
                    yVel0 = (int) (yVel0 + yAccel * DELTA_TIME);
                    mouseY = (currentPosY - prevPosY)/10;
                    prevPosY = currentPosY;
                } else {
                    mouseY = 0;
                }

                byte[] packet = {mouseX.byteValue(), mouseY.byteValue()};
                try {
                    outputStream.write(packet);
                    Log.d(TAG, "Sent packet: " + packet[0] + ", " + packet[1]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });
}
