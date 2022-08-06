package com.makepe.blackout.GettingStarted.OtherClasses;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.makepe.blackout.GettingStarted.Fragments.ExploreFragment;
import com.makepe.blackout.GettingStarted.Models.PostModel;

public class VideoPlayer {
    public Context context;
    public VideoView postVideoView;
    public ProgressBar bufferProgress;
    public SeekBar videoProgressLoader;
    public AudioManager audioManager;

    public VideoPlayer() {
    }

    public VideoPlayer(Context context, VideoView postVideoView,
                       ProgressBar bufferProgress, SeekBar videoProgressLoader) {
        this.context = context;
        this.postVideoView = postVideoView;
        this.bufferProgress = bufferProgress;
        this.videoProgressLoader = videoProgressLoader;
    }

    public void init(PostModel videoPost){
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager.isMusicActive())
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        postVideoView.setVideoURI(Uri.parse(videoPost.getVideoURL()));
        postVideoView.requestFocus();

        videoProgressLoader.setMax(100);

        postVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                mediaPlayer.start();

                //mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(100f, 100f);

                if (mediaPlayer.isPlaying())
                    videoProgressLoader.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100));

            }
        });

        postVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
                switch (what){
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        bufferProgress.setVisibility(View.VISIBLE);
                        videoProgressLoader.setSecondaryProgress(what);
                        return true;

                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        bufferProgress.setVisibility(View.GONE);
                        videoProgressLoader.setProgress(postVideoView.getCurrentPosition());
                        return true;

                }
                return false;
            }
        });

        postVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoProgressLoader.setProgress(0);
                mediaPlayer.start();
            }
        });
    }

    public VideoView getPostVideoView() {
        return postVideoView;
    }

    public void playVideo(){

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager.isMusicActive())
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        if (postVideoView != null){
            postVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    Toast.makeText(context, "Video Started playing", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(context, "Unable to play video", Toast.LENGTH_SHORT).show();
        }
    }

    public void pauseVideo(){

        if (postVideoView != null){
            postVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.pause();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        audioManager.abandonAudioFocusRequest(null);
                    }
                    Toast.makeText(context, "Video Paused", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(context, "Unable to pause video", Toast.LENGTH_SHORT).show();
        }
        
    }

    /*public class VideoProgress extends AsyncTask<Void, Integer, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            do{
                current = holderVideos.postVideoView.getCurrentPosition() / 1000;

                try{
                    int currentPercent = current * 100/duration;
                    publishProgress(currentPercent);
                }catch (Exception e) {
                    e.printStackTrace();
                }

            }while (holderVideos.videoProgressLoader.getProgress() <= 100);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            holderVideos.videoProgressLoader.setProgress(values[0]);
        }
    }*/

}
