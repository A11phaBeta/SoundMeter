package com.syslab.soundmeter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileOutputStream;
import java.io.IOException;

import fft.RealDoubleFFT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";


    private static int mAudioSource = MediaRecorder.AudioSource.MIC;
    private static int mSampleRate = 44100;
    private static int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private static int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private static int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);

    private int mFrequency = 8000;
    private int mChannelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int mAudioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private RealDoubleFFT transformer;
    private int blockSize = 256;

    public static AudioRecord mAudioRecord = null;
    public static boolean isRecording = false;
    public String mFilePath = null;
    private RecordAudio mRecordTask;

    private Button startBtn, endBtn;
    private ImageView imageView;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;

    private static int nowTime = 0;
    private static int[][][] colorSaveArray = null;

    private void doComponentInit(){
        startBtn = (Button)findViewById(R.id.btn_start);
        endBtn = (Button)findViewById(R.id.btn_end);
        mRecordTask = new RecordAudio();
        transformer = new RealDoubleFFT(blockSize);
        imageView = (ImageView)findViewById(R.id.ImageView01);

        bitmap = Bitmap.createBitmap((int)256, (int)100, Bitmap.Config.ARGB_8888);

        canvas = new Canvas(bitmap);

        canvas.drawColor(Color.BLACK);

        paint = new Paint(Paint.FILTER_BITMAP_FLAG);

        paint.setColor(Color.GREEN);

        colorSaveArray = new int[257][257][3];

        imageView.setImageBitmap(bitmap);
        startBtn.setOnClickListener(this);
        endBtn.setOnClickListener(this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        Permissions.getRecordAudioPermission(this);
        doComponentInit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case Permissions.REQ_RECORD_AUDIO :
                if(!Permissions.checkRecordAudioPermissionResponse(grantResults[0])){ finish(); }
                else{ Permissions.getWriteExternalStoragePermission(this); }
                break;
            case Permissions.REQ_WRITE_EXTENAL_STORAGE:
                if(!Permissions.checkWriteExternalStoragePermissionResponse(grantResults[0])){ finish(); }
                break;
        }
    }

    public void onRecord() {
        if(isRecording == true) {
            isRecording = false;
            mRecordTask.cancel(true);
        }
        else {
            isRecording = true;

            if(mAudioRecord == null) {
                mAudioRecord =  new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
                mAudioRecord.startRecording();
            }

            mRecordTask.execute();
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_start :
                onRecord();
                break;
            case R.id.btn_end :
                onRecord();
                break;
        }
    }


    private class RecordAudio extends AsyncTask<Void, double[], Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            short[] readData = new short[blockSize];
            double[] toTransform = new double[blockSize];

            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/record.pcm";
            FileOutputStream fos = null;
            /*try {
                fos = new FileOutputStream(mFilePath);
            } catch(FileNotFoundException e) {
                e.printStackTrace();
            }*/

            while(isRecording){
                int ret = mAudioRecord.read(readData, 0, blockSize);
                double maxVal = -9999999.0, maxIdx = 0;
                for(int i = 0; i < blockSize && i < ret; i++){
                    toTransform[i] = (double)readData[i] / Short.MAX_VALUE; // 부호 있는 16비트
                }

                nowTime = (nowTime + 1 <= 256) ? nowTime + 1 : 0;

                transformer.ft(toTransform);
                try
                {
                    Thread.sleep(100);//Your Interval after which you want to refresh the screen
                }
                catch (InterruptedException e)
                {
                }
                publishProgress(toTransform);



                /*try {
                    fos.write(readData, 0, mBufferSize);
                }catch (IOException e){
                    e.printStackTrace();
                }*/
            }
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;

            publishProgress();

            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override

        protected void onProgressUpdate(double[]... toTransform) {
            double maxVal = -99999.0, maxIdx = 0;
            for(int i = 0; i < toTransform[0].length; i++){

                int[] rgb = colorMap(toTransform[0][i]);
                int x = i;

                int downy = (int) (100 - (toTransform[0][i] * 10));

                int upy = 1;

                colorSaveArray[i][nowTime] = rgb;
                //paint.setColor(Color.argb(255, rgb[0], rgb[1], rgb[2]));
                //canvas.drawLine(nowTime, x, nowTime+1, x, paint);

            }

            paint.setAntiAlias(true);
            paint.setDither(true);

            for(int i=0;i<256;++i){

                for(int j=0;j<100;++j){
                    int[] rgb = colorSaveArray[j][i];

                    paint.setColor(Color.argb(255, rgb[0], rgb[1], rgb[2]));
                    canvas.drawCircle(i, 100-j, 0.5f, paint);
                }
            }



            imageView.invalidate();

        }

    }

    public int[] colorMap(double value) {
        // implements a simple linear RYGCB colormap
        if(value <= 0.25) {
            return new int[]{255, (int)(value*255), (int)255};
        } else if(value <= 0.5) {
            return new int[]{255, (int)255, (int)((1-4*(value-0.25))*255)};
        } else if(value <= 0.75) {
            return new int[]{(int)(4*(value-0.5)*255), (int)255, 0};
        } else {
            //return new int[]{(int)255, (int)((1-4*(value-0.75))*255), 0};
            return new int[]{(int)255, 255, 0};
        }
    }



}