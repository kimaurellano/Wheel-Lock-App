package com.cdtekk.WheelLockApp.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.cdtekk.WheelLockApp.R;
import com.cdtekk.WheelLockApp.Util.ConnectBT;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class UserControlFragment extends Fragment {

    private TextView textViewVerified;
    private TextView textViewInstruction;
    private ImageView imageViewLockState;
    private boolean locked = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_on_user_control, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        textViewVerified = Objects.requireNonNull(getActivity()).findViewById(R.id.textViewVerified);
        textViewInstruction = getActivity().findViewById(R.id.textViewInstruction);
        imageViewLockState = getActivity().findViewById(R.id.imageViewLock);

        imageViewLockState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locked = !locked;
                imageViewLockState.setImageResource(locked ? R.drawable.ic_locked : R.drawable.ic_unlocked);
                try {
                    ConnectBT.getmSocket().getOutputStream().write(locked ? "LOCK".getBytes() : "UNLOCK".getBytes());

                    // Avoid flooding
                    Thread.sleep(500);
                } catch (IOException | InterruptedException e) {
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
                imageViewLockState.setImageResource(R.drawable.ic_unlocked);
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
}
