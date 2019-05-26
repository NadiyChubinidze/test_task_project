package com.example.testtask;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{

    private static final int  RQS_OPEN_AUDIO_MP3=1;
    private static final int REQUEST_CODE_PERMISSION_READ_MUSIC =2;

    private TextView mTextViewSeconds;
    private TextView mTitleFirstSong;
    private TextView mTitleSecongSong;
    private TableLayout table;
    private ImageButton imageButton;
    private SeekBar seekBar;
    private int chooser;
    private boolean clicker;
    private String pathFirstSong;
    private String pathSecondSong;
    private MyMediaPlayer mediaPlayer;
    private Button firstSongButton;
    private Button secondSongButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        mTextViewSeconds = (TextView)findViewById(R.id.textSeekBar);
        mTitleFirstSong = (TextView)findViewById(R.id.titleFirstSong);
        mTitleSecongSong = (TextView)findViewById(R.id.titleSecondSong);
        table = (TableLayout)findViewById(R.id.tablelayout);
        imageButton = (ImageButton)findViewById(R.id.buttonPlay);
        firstSongButton = (Button)findViewById(R.id.firstSong);
        secondSongButton=(Button)findViewById(R.id.secondSong);
        clicker=false;
        pathFirstSong=null;
        pathSecondSong=null;


    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mTextViewSeconds.setText(String.valueOf(seekBar.getProgress() + 2) + " s");

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mTextViewSeconds.setText(String.valueOf(seekBar.getProgress() + 2) + " s");
    }

    public void onClickSong(View v) {

        chooser = v.getId();

        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {

           readMusic();

        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION_READ_MUSIC);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_READ_MUSIC:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    readMusic();
                } else {
                    return;
                }

        }
    }

    public void readMusic(){

        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("audio/*");
        startActivityForResult(Intent.createChooser(chooseFile, "File Chooser"), RQS_OPEN_AUDIO_MP3);

    }


    public void setImageButton(String image){
        Uri uri = Uri.parse(image);
        imageButton.setImageURI(uri);
    }

    public void onClickPlay(View v) {

        System.out.println(pathSecondSong);

        if(pathFirstSong == null || pathSecondSong==null){
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Attention! Not all files uploaded", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }



        if(!clicker) {
            mediaPlayer = new MyMediaPlayer(getApplicationContext(),pathFirstSong, pathSecondSong,seekBar.getProgress()+2);
            setImageButton("android.resource://com.example.testtask/drawable/buttonstop");
            clicker=true;
            seekBar.setEnabled(false);
            firstSongButton.setEnabled(false);
            secondSongButton.setEnabled(false);
        }
        else {
            clicker=false;
            mediaPlayer.stop();
            setImageButton("android.resource://com.example.testtask/drawable/buttonplay");
            seekBar.setEnabled(true);
            firstSongButton.setEnabled(true);
            secondSongButton.setEnabled(true);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case  RQS_OPEN_AUDIO_MP3:
                if (resultCode == RESULT_OK) {

                    final Uri uri = data.getData();
                    String fileName = new File(getPath(uri)).getName();

                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(),uri);
                        if(mp.getDuration()<11000){
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Audio file length is too short", Toast.LENGTH_SHORT);
                            toast.show();
                            mp.release();
                            return;
                        }
                    mp.release();
                    if(chooser == findViewById(R.id.firstSong).getId()) {
                        int len = fileName.length();
                        if(len>15){
                            mTitleFirstSong.setText(fileName.substring(0,5) +" ... "
                                                        + fileName.substring(len-4,len));
                        }
                        else {
                            mTitleFirstSong.setText(fileName);
                        }
                        pathFirstSong=getPath(uri);
                    }
                    else {
                        int len = fileName.length();
                        if(len>15){
                            mTitleSecongSong.setText(fileName.substring(0,5) +" ... "
                                                         + fileName.substring(len-4,len));
                        }
                        else {
                            mTitleSecongSong.setText(fileName);
                        }
                        pathSecondSong = getPath(uri);
                    }
                }
                break;
        }
    }

    public String getPath(Uri uri) {
        String path = null;

            String[] projection = { MediaStore.Files.FileColumns.DATA };
            Cursor cursor = getContentResolver().query(uri, projection, null,
                    null, null);
            if(cursor == null)
                path = uri.getPath();
            else {
                cursor.moveToFirst();
                int column_index = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(column_index); cursor.close();
            }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }

}
