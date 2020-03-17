package com.cdtekk.bluetooth_connectivity_testapp.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cdtekk.bluetooth_connectivity_testapp.Interface.IFragmentChange;
import com.cdtekk.bluetooth_connectivity_testapp.R;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OTPStatusFragment extends Fragment {

    private IFragmentChange fragmentChangeListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_on_otp_recieve, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button buttonEnterOTP = Objects.requireNonNull(getView()).findViewById(R.id.buttonEnterOTP);

        buttonEnterOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // What fragment should be displayed
                fragmentChangeListener.OnFragmentChange(new UserControlFragment());
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof IFragmentChange){
            fragmentChangeListener = (IFragmentChange) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement IFragmentChange");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        fragmentChangeListener = null;
    }
}
