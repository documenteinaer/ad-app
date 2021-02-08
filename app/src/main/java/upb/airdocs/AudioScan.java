package upb.airdocs;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

public class AudioScan {
    private static final String LOG_TAG = "AudioScan";
    MediaRecorder recorder = new MediaRecorder();
    String fileName = null;
    int index = 0;
    Context mContext;
    Visualizer visualizer = null;
    MediaPlayer player = new MediaPlayer();

    public AudioScan(Context context) {
        mContext = context;
    }


    public void startScan() {
        recordAudio();
    }


    public void recordAudio() {
        // Record to the external cache directory for visibility
        fileName = mContext.getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.wav";
        Log.d(LOG_TAG, "file: " + fileName);

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(fileName);
        index++;
        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                recorder.stop();
                playAndProcessAudio();
            }
        }, 1000);   //1 second
    }

    private void playAndProcessAudio() {
        Log.d(LOG_TAG, "Play audio: " + fileName);
        try {
            player.setDataSource(fileName);
            player.prepare();
            //player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        visualizer = new Visualizer(player.getAudioSessionId());
        visualizer.setEnabled(true);
        //player.setVolume(0, 0);
        player.start();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                calculateFft();
                player.stop();
            }
        }, 1000);   //1 second
    }

    private void calculateFft() {
        if (visualizer.getEnabled()) {
            byte[] fft = new byte[visualizer.getCaptureSize()];
            byte[] waveForm = new byte[visualizer.getCaptureSize()];
            visualizer.getFft(fft);
            visualizer.getWaveForm(waveForm);

            Log.d(LOG_TAG,"FFT: " + Arrays.toString(fft));
            Log.d(LOG_TAG, "Waveform: " + Arrays.toString(waveForm));

            int n = fft.length;
            float[] magnitudes = new float[n / 2 + 1];
            float[] phases = new float[n / 2 + 1];
            magnitudes[0] = (float)Math.abs(fft[0]);      // DC
            magnitudes[n / 2] = (float)Math.abs(fft[1]);  // Nyquist
            phases[0] = phases[n / 2] = 0;
            for (int k = 1; k < n / 2; k++) {
                int i = k * 2;
                magnitudes[k] = (float)Math.hypot(fft[i], fft[i + 1]);
                phases[k] = (float)Math.atan2(fft[i + 1], fft[i]);
            }
            Log.d(LOG_TAG, "Magnitudes: " + Arrays.toString(magnitudes));
            Log.d(LOG_TAG, "Phases: " + Arrays.toString(phases));

            visualizer.release();
        }
    }

    public void stopScan() {
        //save audio to collection/fingerprint
    }
}
