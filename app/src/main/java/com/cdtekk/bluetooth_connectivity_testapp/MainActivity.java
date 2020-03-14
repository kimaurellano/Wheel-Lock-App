package com.cdtekk.bluetooth_connectivity_testapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AsyncResponse{

    private ArrayAdapter<String> arrayAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private ConnectBT connectBT;
    private AsyncResponse asyncResponse;

    private ImageButton imageButtonSwitch;
    private Button buttonEnterOTP;
    private EditText editTextOTPInput;
    private TextView textViewRecievedText;

    private boolean deviceConnected = false;
    private Thread thread;
    private byte buffer[];
    private int bufferPosition;
    private boolean stopThread;
    private String recievedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make user enable bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);

        asyncResponse = this;

        textViewRecievedText = findViewById(R.id.textViewRecievedText);
        imageButtonSwitch = findViewById(R.id.switchLock);
        editTextOTPInput = findViewById(R.id.editTextOTP);
        buttonEnterOTP = findViewById(R.id.buttonEnterOTP);

        // Disable switch lock on start
        imageButtonSwitch.setEnabled(false);

        bluetoothStart();

        buttonEnterOTP.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // OTP sent by the gsm to a specific mobile device
                String otp = editTextOTPInput.getText().toString();
                try {
                    if(!recievedMessage.equals(otp)){
                        Toast.makeText(MainActivity.this, "OTP Invalid!", Toast.LENGTH_SHORT).show();
                    }
                    // Send back the provided OTP via bluetooth(HC-05).
                    // The microcontroller would then verify if the sent
                    // back OTP matched the recently sent OTP by GSM
                    ConnectBT.getmSocket().getOutputStream().write(otp.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(), "Verifying OTP...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Attempt to initialize bluetooth or find bonded devices
    private void bluetoothStart(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
        }else{
            // Bluetooth off
            if(!bluetoothAdapter.isEnabled()){
                // Make user enable bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if(pairedDevices.size() > 0){
                arrayAdapter =
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                for (BluetoothDevice bluetoothDevice : pairedDevices){
                    arrayAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
                }
            }

            // Show all bonded devices only
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
            builderSingle.setTitle("Connect to");

            final String[] strName = new String[1];
            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    strName[0] = arrayAdapter.getItem(which);

                    connectBT = new ConnectBT(
                            getApplicationContext(),
                            bluetoothAdapter,
                            Objects.requireNonNull(strName[0]).split("\n")[1]);
                    connectBT.delegate = asyncResponse;
                    connectBT.execute();
                }
            });

            // Show listed devices
            builderSingle.show();
        }
    }

    @Override
    public void processingFinish(Boolean isConnected) {
        if(!isConnected){
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        beginListenForData();
    }

    private void beginListenForData() {
        final Handler handler = new Handler();

        stopThread = false;

        buffer = new byte[1024];

        Thread thread  = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        InputStream inputStream = ConnectBT.getmSocket().getInputStream();

                        int byteCount = inputStream.available();

                        if(byteCount > 0) {
                            byte[] rawBytes = new byte[byteCount];

                            inputStream.read(rawBytes);

                            // Messages from HC-05
                            recievedMessage = new String(rawBytes, StandardCharsets.US_ASCII);

                            handler.post(new Runnable() {
                                public void run() {
                                    textViewRecievedText.setText(recievedMessage);
                                }
                            });
                        }
                    }
                    catch (IOException ex) {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }
}
