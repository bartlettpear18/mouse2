package com.example.joelbartlett.test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        Integer i = 5;
        Integer j = 6;
        Integer k = 7;
        byte[] packet = {i.byteValue(),j.byteValue(), k.byteValue()};
        outputStream.write(packet);
        Log.d(TAG, "Packet sent: " + packet.toString());

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

}
