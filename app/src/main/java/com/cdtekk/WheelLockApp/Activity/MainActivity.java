package com.cdtekk.WheelLockApp.Activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.cdtekk.WheelLockApp.Fragment.OTPStatusFragment;
import com.cdtekk.WheelLockApp.Interface.AsyncResponse;
import com.cdtekk.WheelLockApp.Interface.IFragmentChange;
import com.cdtekk.WheelLockApp.R;
import com.cdtekk.WheelLockApp.Util.ConnectBT;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements IFragmentChange, AsyncResponse {

    private ArrayAdapter<String> arrayAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private ConnectBT connectBT;
    private AsyncResponse asyncResponse;

    private boolean stopThread;
    private String recievedMessage;

    // For handling fragments currently attached to the container fragment
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        currentFragment = new OTPStatusFragment();

        fragmentTransaction.add(R.id.fragment_container_root_view , currentFragment);

        fragmentTransaction.commit();

        asyncResponse = this;

        bluetoothStart();
    }

    @Override
    public void OnFragmentChange(Fragment fragmentObject) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(currentFragment);
        fragmentTransaction.add(R.id.fragment_container_root_view, fragmentObject);

        fragmentTransaction.commit();
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

    private void beginListenForData() {
        final Handler handler = new Handler();

        stopThread = false;

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

                            ((OTPStatusFragment) currentFragment).setOtp(recievedMessage);

                            handler.post(new Runnable() {
                                @SuppressLint("SetTextI18n")
                                public void run() {
                                    // TODO
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
