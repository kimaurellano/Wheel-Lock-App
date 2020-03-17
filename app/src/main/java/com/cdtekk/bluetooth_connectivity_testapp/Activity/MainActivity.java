package com.cdtekk.bluetooth_connectivity_testapp.Activity;

import android.os.Bundle;

import com.cdtekk.bluetooth_connectivity_testapp.Fragment.OTPStatusFragment;
import com.cdtekk.bluetooth_connectivity_testapp.Interface.IFragmentChange;
import com.cdtekk.bluetooth_connectivity_testapp.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements IFragmentChange {

    OTPStatusFragment otpStatusFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        otpStatusFragment = new OTPStatusFragment();
        fragmentTransaction.add(R.id.fragment_container_root_view , otpStatusFragment);

        fragmentTransaction.commit();
    }

    @Override
    public void OnFragmentChange(Fragment fragmentObject) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(otpStatusFragment);
        fragmentTransaction.add(R.id.fragment_container_root_view, fragmentObject);

        fragmentTransaction.commit();
    }
}
