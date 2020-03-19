package com.cdtekk.WheelLockApp.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cdtekk.WheelLockApp.Interface.IBLEConnection;
import com.cdtekk.WheelLockApp.Interface.IFragmentChange;
import com.cdtekk.WheelLockApp.R;
import com.cdtekk.WheelLockApp.Util.ConnectBT;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class UserControlFragment extends Fragment {

    private IBLEConnection connectionChangeListener;
    private IFragmentChange fragmentChangeListener;
    private TextView textViewVerified;
    private TextView textViewInstruction;
    private ImageView imageViewLockState;
    private ViewGroup viewGroupOption;
    private SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_on_user_control, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        preferences = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);

        textViewVerified = Objects.requireNonNull(getActivity()).findViewById(R.id.textViewVerified);
        textViewInstruction = getActivity().findViewById(R.id.textViewInstruction);
        imageViewLockState = getActivity().findViewById(R.id.imageViewLock);
        viewGroupOption = getActivity().findViewById(R.id.options);
        Button buttonYes = getActivity().findViewById(R.id.buttonYes);
        Button buttonNo = getActivity().findViewById(R.id.buttonNo);

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("state", false);
                editor.apply();

                imageViewLockState.setImageResource(R.drawable.ic_unlocked);
                try {
                    ConnectBT.getmSocket().getOutputStream().write("UNLOCK".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Disconnect
                connectionChangeListener.onConnectionChange(false);
                fragmentChangeListener.OnFragmentChange(new OTPInputFragment());
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Animation anim = new AlphaAnimation(1f, 0f);
                anim.setDuration(300);

                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        viewGroupOption.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                viewGroupOption.startAnimation(anim);
            }
        });

        imageViewLockState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isLocked = preferences.getBoolean("state", false);
                if(isLocked){
                    final Animation in = new AlphaAnimation(0.0f, 1.0f);
                    in.setDuration(300);

                    in.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            viewGroupOption.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    viewGroupOption.startAnimation(in);

                    return;
                }

                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("state", true);
                editor.apply();

                imageViewLockState.setImageResource(R.drawable.ic_locked);
                try {
                    ConnectBT.getmSocket().getOutputStream().write("LOCK".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        final Animation animSlide = AnimationUtils.loadAnimation(getContext(), R.anim.slide_right);

        final Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(250);

        animSlide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                textViewVerified.setText("");
                boolean isLocked = preferences.getBoolean("state", false);
                imageViewLockState.setImageResource(isLocked ? R.drawable.ic_locked : R.drawable.ic_unlocked);
                imageViewLockState.startAnimation(in);
                textViewInstruction.setText("Tap to lock/unlock");
                textViewInstruction.startAnimation(in);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        textViewVerified.startAnimation(animSlide);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        fragmentChangeListener = null;
        connectionChangeListener = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof IFragmentChange){
            fragmentChangeListener = (IFragmentChange)context;
        } else {
            throw new RuntimeException(context.toString() + " must implement IFragmentChange");
        }

        if(context instanceof IBLEConnection){
            connectionChangeListener = (IBLEConnection)context;
        } else {
            throw new RuntimeException(context.toString() + " must implement IBLEConnection");
        }
    }
}
