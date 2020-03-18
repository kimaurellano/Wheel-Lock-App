package com.cdtekk.WheelLockApp.Util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.widget.Toast;

import com.cdtekk.WheelLockApp.Interface.AsyncResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ConnectBT extends AsyncTask<Void, Void, Void> {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private static BluetoothSocket mSocket;
    private String address;
    private BluetoothAdapter adapter;
    private BluetoothDevice device;
    public AsyncResponse delegate = null;

    public ConnectBT(Context context, BluetoothAdapter adapter, String address) {
        this.context = context;
        this.address = address;
        this.adapter = adapter;
    }

    public static BluetoothSocket getmSocket(){
        return mSocket;
    }

    private void setSocket(BluetoothSocket socket){
        mSocket = socket;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try{
            device = adapter.getRemoteDevice(address);
            Method method = device.getClass().getMethod("getUuids");
            ParcelUuid[] parcelUuids = (ParcelUuid[]) method.invoke(device);

            mSocket = device.createInsecureRfcommSocketToServiceRecord(parcelUuids[0].getUuid());

            setSocket(mSocket);

            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            mSocket.connect();
        }catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result){
        super.onPostExecute(result);

        if(!mSocket.isConnected()){
            Toast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Connected to " + device.getName() + "\n" + device.getAddress(), Toast.LENGTH_SHORT).show();
        }

        delegate.processingFinish(mSocket.isConnected());
    }
}
