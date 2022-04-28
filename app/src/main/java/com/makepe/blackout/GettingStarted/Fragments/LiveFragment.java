package com.makepe.blackout.GettingStarted.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.YuvImage;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.makepe.blackout.GettingStarted.InAppActivities.PostActivity;
import com.makepe.blackout.R;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraListener;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;

public class LiveFragment extends Fragment {

    private RadioGroup liveAudioVideoBTN;
    private ImageView goLiveBTN;

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
                        goLiveBTN.setImageResource(R.drawable.ic_baseline_live_tv_24);
                        break;
                    case R.id.liveAudioMediaBTN:
                        goLiveBTN.setTag("liveAudio");
                        goLiveBTN.setImageResource(R.drawable.ic_mic_black_24dp);
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

}