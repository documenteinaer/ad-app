package upb.airdocs;
import android.media.MediaRecorder;

import java.io.IOException;

public class AudioScan {
    MediaRecorder recorder = new MediaRecorder();
    String fileName = "audio";
    int index = 0;

    public void startScan(){
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName+index);
        index++;
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopScan(){
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }
}
