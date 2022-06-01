package com.makepe.blackout.GettingStarted.Fragments;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.makepe.blackout.R;

public class PodcastFragment extends Fragment {

    private ImageView podcastBTN;
    private EditText podcastName, podcastDescription;

    public PodcastFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_podcast, container, false);

        podcastBTN = view.findViewById(R.id.podcastBTN);
        podcastName = view.findViewById(R.id.podcastNameET);
        podcastDescription = view.findViewById(R.id.podcastDescriptionET);

        podcastBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(podcastName.getText().toString().trim())){
                    podcastName.setError("Enter Your Podcast Name here");
                    podcastName.requestFocus();
                }else if (TextUtils.isEmpty(podcastDescription.getText().toString().trim())){
                    podcastDescription.setError("Enter Your Podcast Description here");
                    podcastDescription.requestFocus();
                }else
                    Toast.makeText(getActivity(), "You will be able to start your podcast", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

}