package com.makepe.blackout.GettingStarted.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.makepe.blackout.R;

import java.io.IOException;

public class LiveFragment extends Fragment {

    private RadioGroup liveAudioVideoBTN;
    private ImageView goLiveBTN;

    private int[] colorIntArray = {R.color.colorBlack, R.color.colorBlack};
    private int[] iconIntArray = {R.drawable.ic_baseline_live_tv_24, R.drawable.ic_mic_black_24dp};


    public LiveFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_live, container, false);

        liveAudioVideoBTN = view.findViewById(R.id.liveMediaSelector);
        goLiveBTN = view.findViewById(R.id.goLiveBTN);
        liveAudioVideoBTN.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.liveVideoMediaBTN:
                        goLiveBTN.setTag("liveVideo");
                        animateLiveBTN(0);
                        break;
                    case R.id.liveAudioMediaBTN:
                        goLiveBTN.setTag("liveAudio");
                        animateLiveBTN(1);
                        break;

                    default:
                        Toast.makeText(getContext(), "Unknown media type selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        goLiveBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (goLiveBTN.getTag().equals("liveVideo")){
                    Toast.makeText(getContext(), "You will be able to start a video live broadcast", Toast.LENGTH_SHORT).show();
                }else if (goLiveBTN.getTag().equals("liveAudio")){
                    Toast.makeText(getContext(), "You will be able to start an audio live broadcast", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void animateLiveBTN(int position){
        goLiveBTN.clearAnimation();

        ScaleAnimation shrink = new ScaleAnimation(1f, 0.1f, 1f, 0.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(100);
        shrink.setInterpolator(new AccelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                goLiveBTN.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), colorIntArray[position]));
                goLiveBTN.setImageDrawable(ContextCompat.getDrawable(getActivity(), iconIntArray[position]));

                // Rotate Animation
                Animation rotate = new RotateAnimation(60.0f, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                        0.5f);
                rotate.setDuration(150);
                rotate.setInterpolator(new DecelerateInterpolator());

                // Scale up animation
                ScaleAnimation expand = new ScaleAnimation(0.1f, 1f, 0.1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                expand.setDuration(150);     // animation duration in milliseconds
                expand.setInterpolator(new DecelerateInterpolator());

                // Add both animations to animation state
                AnimationSet s = new AnimationSet(false); //false means don't share interpolators
                s.addAnimation(rotate);
                s.addAnimation(expand);
                goLiveBTN.startAnimation(s);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        goLiveBTN.startAnimation(shrink);
    }

}