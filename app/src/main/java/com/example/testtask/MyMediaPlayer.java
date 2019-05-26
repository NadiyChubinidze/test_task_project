package com.example.testtask;




import android.media.AudioManager;
import android.media.MediaPlayer;
import android.content.Context;
import android.widget.Toast;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class MyMediaPlayer {

    private Context context;
    private int seekBar;
    private MediaPlayer mediaPlayerFirst;
    private MediaPlayer mediaPlayerSecond;
    private Timer timerFirst;
    private Timer timerSecond;


    public MyMediaPlayer(Context cntxt, String firstSong, String secondSong, int seekBar){

        context=cntxt;
        this.seekBar = seekBar*1000;
        timerFirst = new Timer(true);
        timerSecond = new Timer(true);
        mediaPlayerFirst = new MediaPlayer();
        mediaPlayerSecond = new MediaPlayer();

        FileInputStream fis=null;

        try {
            fis = new FileInputStream(firstSong);
            mediaPlayerFirst.setDataSource(fis.getFD());
            mediaPlayerFirst.prepare();

            fis.close();

            fis = new FileInputStream(secondSong);
            mediaPlayerSecond.setDataSource(fis.getFD());
            mediaPlayerSecond.prepare();

        }
        catch (IOException e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(context,
                    "Attention! File is not found", Toast.LENGTH_SHORT);
            toast.show();
        } finally {
            if(fis !=null)
                try {
                    fis.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    Toast toast = Toast.makeText(context,
                            "Attention! File is not found", Toast.LENGTH_SHORT);
                    toast.show();
                }
        }

        play();

    }

    public void play() {

        final long durationFirst = mediaPlayerFirst.getDuration();
        long durationSecond = mediaPlayerSecond.getDuration();


        timerFirst.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
               mediaPlayerFirst.start();
               crossFade(mediaPlayerFirst);

            }
        }, 0,durationSecond+durationFirst-seekBar*2);

        timerSecond.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mediaPlayerSecond.start();
                crossFade(mediaPlayerSecond);

            }
        }, durationFirst-seekBar,durationSecond+durationFirst-seekBar*2);

    }

    public void stop(){

        timerFirst.cancel();
        timerFirst.purge();

        timerSecond.cancel();
        timerSecond.purge();

        mediaPlayerFirst.stop();
        mediaPlayerSecond.stop();

    }

    private void crossFade(MediaPlayer mediaPlayer) {

        fadeIn(mediaPlayer, seekBar);
        fadeOut(mediaPlayer, seekBar);

    }

    public void fadeOut(final MediaPlayer _player, final int duration) {

        final float deviceVolume = getDeviceVolume();

        final Timer timer = new Timer(true);

        timer.schedule(new TimerTask(){

            private float time = duration;
            private float volume = 0.0f;

            @Override
            public void run() {
                    time -= 100;
                    volume = (deviceVolume * time) / duration;
                    _player.setVolume(volume, volume);
                    if (time <= 0) {
                        timer.cancel();
                        timer.purge();
                    }
            }

        },_player.getDuration()-duration,100);

    }

    public void fadeIn(final MediaPlayer _player, final int duration) {

        final float deviceVolume = getDeviceVolume();

        final Timer timer = new Timer(true);

        timer.schedule(new TimerTask(){

            private float time = 0.0f;
            private float volume = 0.0f;

            @Override
            public void run() {
                time+=100;
                volume = (deviceVolume * time) / duration;
                _player.setVolume(volume, volume);
                if(time>=duration) {
                    timer.cancel();
                    timer.purge();
                }
            }

        },0,100);

    }


    public float getDeviceVolume() {

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        int volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        return (float) volumeLevel / maxVolume;
    }

}
