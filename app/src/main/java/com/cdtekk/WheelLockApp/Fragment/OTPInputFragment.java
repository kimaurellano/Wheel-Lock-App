package com.cdtekk.WheelLockApp.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cdtekk.WheelLockApp.Interface.IFragmentChange;
import com.cdtekk.WheelLockApp.R;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OTPInputFragment extends Fragment {

    private IFragmentChange fragmentChangeListener;
    private String _message;
    private EditText editTextOtpInput;

    public void setOtp(String message){
        _message = message;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_on_otp_input, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button buttonEnterOTP = Objects.requireNonNull(getView()).findViewById(R.id.buttonEnterOTP);
        editTextOtpInput = Objects.requireNonNull(getActivity()).findViewById(R.id.editTextOTPInput);
        editTextOtpInput.setLetterSpacing(0.5f);

        buttonEnterOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_message == null){
                    Toast.makeText(getContext(), "Wait for OTP message", Toast.LENGTH_SHORT).show();
                    return;
                }

                int inputOtpi = Integer.parseInt(editTextOtpInput.getText().toString());
                int otpi = Integer.parseInt(_message.replace('\"', ' ').trim());

                if(inputOtpi != otpi){
                    Toast.makeText(getContext(), "Invalid OTP", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verified
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
