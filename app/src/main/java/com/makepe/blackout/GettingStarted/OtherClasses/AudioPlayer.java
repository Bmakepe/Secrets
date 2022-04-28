package com.makepe.blackout.GettingStarted.OtherClasses;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.makepe.blackout.R;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class AudioPlayer {

    public Context context;
    public MediaPlayer mediaPlayer = new MediaPlayer();;
    public final CircleImageView playBTN;
    public boolean isPlaying = false;
    public Handler handler;
    public final TextView seekTimer, postTotalTime;
    public int seconds = 0, dummySeconds = 0, playableSeconds = 0;
    public LottieAnimationView lavPlaying;
    public AudioManager manager;

    private int current = 0, duration = 0;

    public AudioPlayer(Context context, CircleImageView playBTN,
                       TextView seekTimer, TextView postTotalTime, LottieAnimationView lavPlaying) {
        this.context = context;
        this.playBTN = playBTN;
        this.seekTimer = seekTimer;
        this.postTotalTime = postTotalTime;
        this.lavPlaying = lavPlaying;
    }

    public void startPlayingAudio(String audioURL){
        manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (manager.isMusicActive()){
            manager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }

        if (audioURL != null){
            try {
                mediaPlayer.setDataSource(audioURL);
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    playBTN.setImageResource(R.drawable.ic_baseline_stop_circle_24);
                    isPlaying = true;
                    lavPlaying.playAnimation();

                    //runTimer();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void runTimer() {
        handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                String time = String.format(String.valueOf(mediaPlayer.getDuration()), "%02d:%02d", minutes, secs);
                seekTimer.setText(time);

                if (isPlaying && playableSeconds != -1){
                    seconds ++;
                    playableSeconds --;

                    if (playableSeconds == -1 && isPlaying){
                        stopPlayingAudio();
                        playableSeconds = dummySeconds;
                        return;
                    }
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    public void stopPlayingAudio(){

        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        isPlaying = false;
        seconds = 0;
        //handler.removeCallbacksAndMessages(null);
        playBTN.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
        lavPlaying.pauseAnimation();
        manager.abandonAudioFocus(null);
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setDuration(){
        mediaPlayer.setOnPreparedListener(mp -> {

            duration = mp.getDuration() / 1000;
            String durationString = String.format("%02d:%02d", duration / 60, duration % 60);
            postTotalTime.setText(durationString);
        });
    }

}
