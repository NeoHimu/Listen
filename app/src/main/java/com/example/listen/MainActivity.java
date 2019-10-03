package com.example.listen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    boolean isRecording = false;

    AudioManager am = null;
    AudioRecord record = null;
    AudioTrack track = null;
    int origionalVolume=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
        Button startButton = (Button) findViewById(R.id.start_button);
        Button stopButton = (Button) findViewById(R.id.stop_button);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    100);
        }

        if(permission_granted())
        {
            initRecordAndTrack();

            am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            am.setSpeakerphoneOn(true);
            origionalVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

            (new Thread() {
                @Override
                public void run() {
                    recordAndPlay();
                }
            }).start();

            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isRecording) {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                        startRecordAndPlay();
                    }
                }
            });

            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRecording) {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, origionalVolume, 0);
                        stopRecordAndPlay();
                    }
                }
            });
        }

    }

    private boolean permission_granted() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }

    private void initRecordAndTrack() {
        int min = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        record = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                min);
        if (AcousticEchoCanceler.isAvailable()) {
            AcousticEchoCanceler echoCancler = AcousticEchoCanceler.create(record.getAudioSessionId());
            echoCancler.setEnabled(true);
        }
        int maxJitter = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        track = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, maxJitter,
                AudioTrack.MODE_STREAM);
    }

    private void recordAndPlay() {
        short[] lin = new short[1024];
        int num = 0;
        am.setMode(AudioManager.MODE_IN_COMMUNICATION);
        while (true) {
            if (isRecording) {
                num = record.read(lin, 0, 1024);
                track.write(lin, 0, num);
            }
        }
    }

    private void startRecordAndPlay() {
        record.startRecording();
        track.play();
        isRecording = true;
    }

    private void stopRecordAndPlay() {
        record.stop();
        track.pause();
        isRecording = false;
    }
}


