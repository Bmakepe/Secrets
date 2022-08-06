package com.makepe.blackout.GettingStarted.OtherClasses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.makepe.blackout.R;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class AudioPlayer implements SensorEventListener{

    public Context context;
    public MediaPlayer mediaPlayer = new MediaPlayer();
    public final CircleImageView playBTN;
    public final TextView currentTime, postTotalTime;

    public AudioManager audioManager;
    public SensorManager sensorManager;
    public Sensor proximitySensor;

    public SeekBar seekbar;
    public Handler audioHandler = new Handler();

    public AudioPlayer(Context context, CircleImageView playBTN, TextView currentTime,
                       TextView postTotalTime, SeekBar seekbar){
        this.context = context;
        this.playBTN = playBTN;
        this.currentTime = currentTime;
        this.postTotalTime = postTotalTime;
        this.seekbar = seekbar;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void init(){
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        seekbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SeekBar seekBar = (SeekBar) view;
                int playPosition = (mediaPlayer.getDuration() / 100) * seekBar.getProgress();
                mediaPlayer.seekTo(playPosition);
                currentTime.setText(milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
                return false;
            }
        });

        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                seekbar.setSecondaryProgress(i);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                seekbar.setProgress(0);
                playBTN.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                currentTime.setText("00:00");
                audioManager.abandonAudioFocus(null);
                mediaPlayer.reset();
            }
        });

    }

    public void startPlayingAudio(String audioURL){

        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

            if (proximitySensor != null){
                sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        if (audioManager.isMusicActive()){
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }

        seekbar.setMax(100);

        if (audioURL != null){
            try {
                mediaPlayer.setDataSource(audioURL);
                mediaPlayer.prepare();
                postTotalTime.setText(milliSecondsToTimer(mediaPlayer.getDuration()));

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                        playBTN.setImageResource(R.drawable.ic_baseline_stop_circle_24);
                        updateSeeker();

                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void stopPlayingAudio(){

        audioManager.setMode(AudioManager.MODE_NORMAL);

        try{
            sensorManager.unregisterListener(this);
        }catch (Exception e) {
            e.printStackTrace();
        }

        if (mediaPlayer != null){
            audioHandler.removeCallbacks(updater);
            mediaPlayer.pause();
            playBTN.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
            audioManager.abandonAudioFocus(null);
        }

    }

    public Runnable updater = new Runnable(){
        @Override
        public void run() {
            updateSeeker();
            long currentDuration = mediaPlayer.getCurrentPosition();
            currentTime.setText(milliSecondsToTimer(currentDuration));
        }
    };

    public void updateSeeker(){
        if (mediaPlayer.isPlaying()){
            seekbar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100));
            audioHandler.postDelayed(updater, 1000);
        }
    }

    public String milliSecondsToTimer(long milliSeconds){
        String timerString = "";
        String secondsString;

        int hours = (int) (milliSeconds / (1000 * 60 *60));
        int minutes = (int) (milliSeconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliSeconds % (1000 * 60 * 60)) % (1000 * 60) /1000);

        if (hours > 0)
            timerString = hours + ":";

        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        timerString = timerString + minutes + ":" + secondsString;

        return timerString;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        
        if(sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY){
            Toast.makeText(context, "Values: " + sensorEvent.values[0], Toast.LENGTH_SHORT).show();
            
            if (sensorEvent.values[0] > 0){
                Toast.makeText(context, "Object is far from sensor", Toast.LENGTH_SHORT).show();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                audioManager.setMode(AudioManager.MODE_NORMAL);
                audioManager.setSpeakerphoneOn(true);
            }else{
                Toast.makeText(context, "Object is close to sensor", Toast.LENGTH_SHORT).show();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setSpeakerphoneOn(false);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
