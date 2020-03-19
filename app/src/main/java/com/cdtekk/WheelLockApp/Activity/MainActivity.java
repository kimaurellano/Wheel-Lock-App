package com.cdtekk.WheelLockApp.Activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.cdtekk.WheelLockApp.Fragment.OTPStatusFragment;
import com.cdtekk.WheelLockApp.Fragment.UserControlFragment;
import com.cdtekk.WheelLockApp.Interface.AsyncResponse;
import com.cdtekk.WheelLockApp.Interface.IFragmentChange;
import com.cdtekk.WheelLockApp.R;
import com.cdtekk.WheelLockApp.Util.ConnectBT;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements IFragmentChange, AsyncResponse {

    private BluetoothAdapter bluetoothAdapter;
    private ConnectBT connectBT;
    private AsyncResponse asyncResponse;
    private ProgressBar progressBarBLEConn;
    private Spinner spinnerBLEList;
    private ImageView imageViewBLEIcon;
    private Button buttonDisconnect;

    private boolean stopThread;
    private String recievedMessage;

    // For handling fragments currently attached to the container fragment
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBarBLEConn = findViewById(R.id.progressbarBLEConn);
        spinnerBLEList = findViewById(R.id.spinnerBleList);
        imageViewBLEIcon = findViewById(R.id.imageViewBLEIcon);
        buttonDisconnect = findViewById(R.id.buttonDisconnect);

        imageViewBLEIcon.setVisibility(View.INVISIBLE);

        currentFragment = new OTPStatusFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container_root_view , currentFragment);
        fragmentTransaction.commit();

        asyncResponse = this;

        bluetoothStart();

        imageViewBLEIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(buttonDisconnect.getVisibility() == View.VISIBLE){
                    updateIcon(
                            buttonDisconnect,
                            0,
                            R.id.buttonDisconnect,
                            1f,
                            0f,
                            500,
                            View.INVISIBLE
                    );
                } else {
                    updateIcon(
                            buttonDisconnect,
                            0,
                            R.id.buttonDisconnect,
                            0f,
                            1f,
                            500,
                            View.VISIBLE
                    );
                }
            }
        });

        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connectBT != null){
                    try {
                        Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                        ConnectBT.getmSocket().close();

                        updateIcon(
                                imageViewBLEIcon,
                                R.drawable.ic_bluetooth,
                                R.id.imageViewBLEIcon,
                                1f,
                                0f,
                                250,
                                View.INVISIBLE);

                        updateIcon(
                                buttonDisconnect,
                                0,
                                R.id.buttonDisconnect,
                                1f,
                                0f,
                                500,
                                View.INVISIBLE
                        );

                        for (Fragment frg: Objects.requireNonNull(currentFragment.getActivity()).getSupportFragmentManager().getFragments()) {
                            if(frg instanceof UserControlFragment){
                                // Back to home page upon disconnect on Usercontrol
                                OnFragmentChange(new OTPStatusFragment());
                            }
                        }

                        // Reset dropdown
                        int i = spinnerBLEList.getSelectedItemPosition() - spinnerBLEList.getSelectedItemPosition();
                        spinnerBLEList.setSelection(i, false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void OnFragmentChange(Fragment fragmentObject) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment_container_root_view, fragmentObject);
        fragmentTransaction.commit();

        currentFragment = fragmentObject;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void processingFinish(Boolean isConnected) {
        progressBarBLEConn.setVisibility(View.INVISIBLE);
        if(!isConnected){
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
            return;
        }

        updateIcon(
                imageViewBLEIcon,
                R.drawable.ic_bluetooth,
                R.id.imageViewBLEIcon,
                0f,
                1f,
                250,
                View.VISIBLE);

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

            final List<Pair<String, String>> pairList = new ArrayList<>();
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if(pairedDevices.size() > 0){
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
                adapter.add("----");
                for (BluetoothDevice bluetoothDevice : pairedDevices){
                    String deviceName = bluetoothDevice.getName();
                    String deviceAddress = bluetoothDevice.getAddress();

                    adapter.add(deviceName);
                    pairList.add(new Pair<>(deviceName, deviceAddress));
                }

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerBLEList.setAdapter(adapter);

                // This way you set your selection with no animation which
                // causes the on item selected listener to be called.
                // But the listener is null so nothing is run. Then your listener is assigned.
                spinnerBLEList.setSelection(0, false);

                spinnerBLEList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if(connectBT != null){
                            try {
                                ConnectBT.getmSocket().close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        if(i == 0){
                            return;
                        }

                        String address = pairList.get(i - 1).second;
                        connectBT = new ConnectBT(
                                getApplicationContext(),
                                bluetoothAdapter,
                                address);
                        connectBT.delegate = asyncResponse;
                        connectBT.execute();

                        if(imageViewBLEIcon.getVisibility() == View.VISIBLE){
                            updateIcon(
                                    imageViewBLEIcon,
                                    R.drawable.ic_bluetooth,
                                    R.id.imageViewBLEIcon,
                                    1f,
                                    0f,
                                    250,
                                    View.INVISIBLE);
                        }

                        progressBarBLEConn.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }
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

    private void updateIcon(View view, int imageResource, int resourceId, float alphaStart, float alphaEnd, int duration, int visibility){
        final Animation animation = new AlphaAnimation(alphaStart, alphaEnd);
        animation.setDuration(duration);

        view.findViewById(resourceId);
        if(view instanceof ImageView){
            ((ImageView)view).setImageResource(imageResource);
        }
        view.startAnimation(animation);
        view.setVisibility(visibility);
    }
}
