package com.talk.oldtalklib.Audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;

import com.talk.TalkAudioCallBack;


/**
 * Created by alex on 16/10/5.
 * 发送语音线程  ，录音的数据包通过接口回调
 */
public class SendSoundsThread extends BaseSoundsThread {

    //录音器
    private AudioRecord audioRecord;
    private boolean isRunning = false;
    //        private byte[] recordBytes = new byte[640];
    //接收、播放音频数据大小
    private int recBufSize;


    private  int num = 0 ;
    /** Number of bytes per frame */
    private int frame_size;


    /** Number of frame per second */
    int frame_rate;
    /****
     * 分贝值
     */
    private      double volume =  0 ;
    /***
     * 接口回调
     * 用于回调当前录音组成的数据包
     */
    public TalkAudioCallBack myCallBack;
    /***
     * 缓冲区大小
     */
    public  int bufferSize;
    /***
     * 是否开启
     */
    public boolean isStart;


    public double getVolume() {
        if(!isRunning){  //录音完毕
            volume = 0;
        }
        return  volume;
    }

    public AudioRecord getAudioRecord() {
        return audioRecord;
    }

    public SendSoundsThread(int bufferSize)
    {
        super();
        int frame_size = codec.frame_size();
        this.frame_size = frame_size;
//        recBufSize = AudioRecord.getMinBufferSize(frequency, audioFormat, AudioFormat.ENCODING_PCM_16BIT);
//        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, audioFormat, AudioFormat.ENCODING_PCM_16BIT, recBufSize);

        recBufSize = AudioRecord.getMinBufferSize(frequency, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, recBufSize);

        aec(audioRecord);


        if (recBufSize == 640) {
            if (frame_size == 960) frame_size = 320;
            if (frame_size == 1024) frame_size = 160;
            recBufSize = 4096*3/2;
        } else if (recBufSize < 4096) {
            if (recBufSize <= 2048 && frame_size == 1024) frame_size /= 2;
            recBufSize = 4096*3/2;
        } else if (recBufSize == 4096) {
            recBufSize *= 3/2;
            if (frame_size == 960) frame_size = 320;
        } else {
            if (frame_size == 960) frame_size = 320;
            if (frame_size == 1024) frame_size = 160; // frame_size *= 2;
        }
        frame_rate = codec.samp_rate()/frame_size;
        frame_rate *= 1.5;

        this.bufferSize = bufferSize;

    }



    @Override
    public  void run()
    {
        super.run();
        audioRecord.startRecording();
        isStart = true;
        while (true)
        {

            if(!isStart){
                return;
            }

            if (isRunning)
            {
                try
                {
                    //音频数据
                    byte[] buffer = new byte[bufferSize];
                    short[] pcmFrame = new short[frame_size];
                    //获取录音到的音频流数据（PCM）
                    num = audioRecord.read(pcmFrame, 0, frame_size);
                    //使用FFmpeg编码  PCM->AAC
                    num = codec.encode(pcmFrame, 0, buffer, num);
                    //获取分贝值
                    volume = SendSoundsThread.this.countDb(pcmFrame);
                    //回调录音数据包
                    if(myCallBack != null){
                        myCallBack.RecordAudioData(buffer);
                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }else {
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    /***
     * 设置是否开始录音线程
     * 用于停止该线程
     * @param start
     */
    public void setStart(boolean start) {
        isStart = start;
    }

    /***
     * 停止线程 释放资源
     */
    public void  releaseSendSoundsThread(){
        isStart = false;
        if(codec != null){
            codec.close();
            codec =null;
        }

        myCallBack = null;
        if(audioRecord != null){
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }

    }

    /****
     * 设置录音线程是否运行
     * @param isRunning
     */
    public void setRunning(boolean isRunning)
    {

        this.isRunning = isRunning;

    }

    /***
     * 获取录音线程是否在工作
     * @return
     */
    public boolean isRunning() {
        return isRunning;
    }

    public static void aec(AudioRecord ar) {

        if (AcousticEchoCanceler.isAvailable()) {
            AcousticEchoCanceler aec = AcousticEchoCanceler.create(ar.getAudioSessionId());
            if (aec != null && !aec.getEnabled()) {
                aec.setEnabled(true);
            }
        }
        if (NoiseSuppressor.isAvailable()) {
            NoiseSuppressor noise = NoiseSuppressor.create(ar.getAudioSessionId());
            if (noise != null && !noise.getEnabled()) {
                noise.setEnabled(true);
            }
        }
    }

    /****
     * 计算分贝值
     * @param data
     * @return
     */
    public double countDb (short[] data)
    {
        float BASE=32768f;
        float maxAmplitude = 0;

        for (int i = 0; i < data.length; i++)
        {
            maxAmplitude += data[i] * data[i];
        }
        maxAmplitude=(float)Math.sqrt(maxAmplitude/data.length);
        float ratio=maxAmplitude / BASE;
        float db =0;
        if(ratio>0)
        {
            db = (float) (20 * Math.log10(ratio))+100;
        }

        return db;
    }

}

