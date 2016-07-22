package com.ryanpotsander.androidcv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int AUDIO_IN = 0;
    public static final int VIDEO_OUT = 1;

    RecyclerView mRecyclerView;
    CustomRecyclerViewAdapter adapter;

    ImageButton micButton;

    String audioIn;
    String videoOut;

    boolean recording = false;
    boolean processing = false;

    MediaRecorder audioRecorder;
    FFmpegFrameRecorder recorder;

    private AudioRecord audioRecord;
    private AudioRecordRunnable audioRecordRunnable;
    Thread audioThread;
    volatile boolean runAudioThread = true;

    long recordLength;
    Frame[] images;
    long[] timestamps;
    ShortBuffer[] samples;
    int imagesIndex, samplesIndex;
    Frame img;

    private int sampleAudioRateInHz = 44100;
    private int imageWidth = 320;
    private int imageHeight = 240;
    private int frameRate = 30;

    long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRecyclerView();
        initControls();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public static String createFilePath(int type){

        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        long timeStamp = System.currentTimeMillis();
        String fileExtension;

        //TODO remove switch if unnecessary
        switch (type){
            case AUDIO_IN:
                fileExtension = ".mp4";
                break;
            case VIDEO_OUT:
                fileExtension = ".mp4";
                break;
            default:
                fileExtension = ".mp4";
        }

        String newPath = dir + "/" + timeStamp + fileExtension;
        Log.d("new path created ", newPath);

        return newPath;

    }


    private void play(String path){

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), "video/mp4");
        startActivity(intent);
    }

    public void addItem(PreviewObject item){

        List<PreviewObject> current = adapter.getItemList();
        current.add(item);


        changeAdapter(createAdapter(current));

        Log.d("adapter", "adapter.addItem() called");
        Log.d("adapter", "itemList length " + current.size());
        Log.d("adapter", "item label " + item.getLabel());
        Log.d("adapter", "item path " + item.getPath());

    }

    private void changeAdapter(CustomRecyclerViewAdapter adapter){
        this.adapter = adapter;

        mRecyclerView.swapAdapter(adapter, true);
    }

    private PreviewObject createTestItem(){ //TODO remove
        PreviewObject preview = new PreviewObject("dummy/path");
        Bitmap previewImg = getLogoBitmap();
        preview.setPreview(previewImg);

        return preview;
    }

    public Bitmap getLogoBitmap() {
        return BitmapFactory.decodeResource(getResources(), R.drawable.my_voice);
    }

    private CustomRecyclerViewAdapter createAdapter(List<PreviewObject> data){

        return new CustomRecyclerViewAdapter(new CustomRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String path, View v) {
                play(path);
            }

            @Override
            public Bitmap getLogoImg() {
                return getLogoBitmap();
            }
        }, data);
    }

    private void initRecyclerView(){

        List<PreviewObject> data = new ArrayList<>(); // TODO remove
        data.add(createTestItem()); //TODO remove

        final float scale = getResources().getDisplayMetrics().density;
        int dips = 8;
        int space = (int) (dips * scale + 0.5f);

        mRecyclerView = (RecyclerView)findViewById(R.id.file_list_fullscreen);
        mRecyclerView.addItemDecoration(new GreyItemDecoration(this, R.drawable.divider));
        adapter = createAdapter(data); //adapter with empty list
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));

    }

    private void initControls(){
        micButton = (ImageButton)findViewById(R.id.fullscreen_content);
        if (micButton != null) micButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (!recording) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private Frame convertToFrame(Bitmap source){
        AndroidFrameConverter converter = new AndroidFrameConverter();
        return converter.convert(source);
    }

    public void createVideo(){

        /**
        final String path = MainActivity.createFilePath(MainActivity.VIDEO_OUT);
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected void onPreExecute() {
                Log.d("async", "preexecute ran");
            }

            @Override
            protected Void doInBackground(Void... params) {
                FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(path, 320, 240);
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mp4");
                recorder.setFrameRate(30);
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

                try {
                    recorder.start();

                    startTime = System.currentTimeMillis();
                    long imgTime = 50000000;

                    Frame frame = convertToFrame(getLogoBitmap());
                    recorder.record(frame);

                } catch (FFmpegFrameRecorder.Exception e) {
                    e.printStackTrace();
                }

                try {
                    recorder.stop();
                    recorder.release();
                } catch (FFmpegFrameRecorder.Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                addItem(new PreviewObject(path));

            }
        }.execute();
         */
    }

    private void initRecorder() {

        Log.w("MainActivity","init recorder");

        img = convertToFrame(getLogoBitmap());
        Log.i("MainActivity", "create img");

        videoOut = createFilePath(VIDEO_OUT);

        recorder = new FFmpegFrameRecorder(videoOut, 320, 240, 1);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("mp4");
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        recorder.setSampleRate(sampleAudioRateInHz);
        recorder.setFrameRate(frameRate);

        Log.i("MainActivity", "recorder initialize success");

        audioRecordRunnable = new AudioRecordRunnable();
        audioThread = new Thread(audioRecordRunnable);
        runAudioThread = true;
    }

    public void startRecording() {
        recording = true;
        processing = true;

        initRecorder();

        try {
            recorder.start();
            startTime = System.currentTimeMillis();
            Log.d("MainActivity", "timeStamp" + recorder.getTimestamp());
            audioThread.start();

        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        recording = false;
        recordLength = System.currentTimeMillis() - startTime;
        runAudioThread = false;

        try {
            audioThread.join();
        } catch (InterruptedException e) {
            // reset interrupt to be nice
            Thread.currentThread().interrupt();
            return;
        }
        audioRecordRunnable = null;
        audioThread = null;

        try {
            //recorder.setTimestamp(2); this worked
            recorder.setTimestamp(recordLength);
            Log.d("MainActivity", "timeStamp" + recorder.getTimestamp());
            recorder.record(img);
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }

        Log.v("MainActivity","Finishing recording, calling stop and release on recorder");
        try {
            recorder.stop();
            recorder.release();
        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        }
        recorder = null;
        processing = false;

        addItem(new PreviewObject(videoOut));
        Log.d("MainActivity", "addItem called");
    }


    class AudioRecordRunnable implements Runnable {

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            // Audio
            int bufferSize;
            ShortBuffer audioData;

            bufferSize = AudioRecord.getMinBufferSize(sampleAudioRateInHz,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleAudioRateInHz,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize * 10);

            audioRecord.startRecording();

            audioData = ShortBuffer.allocate(bufferSize * 10);

            while (recording){

                int result = audioRecord.read(audioData.array(), 0, audioData.capacity());

                Log.d("recording loop", "audioRecord.read" + result);
                Log.d("recording loop", "timeStamp" + recorder.getTimestamp());

                //audioData.limit(result);


            }

            try {
                recorder.recordSamples(audioData);
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }

            Log.v("AudioThread","AudioThread Finished, release audioRecord" + recorder.getTimestamp());

            /* encoding finish, release recorder */
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
                Log.v("AudioThread","audioRecord released");
            }
        }
    }
}
