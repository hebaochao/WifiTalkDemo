package com.talk.newtalklib.service;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Build;
import android.util.Log;

import com.talk.newtalklib.code.SpeexCoder;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by baochaoh on 16/10/5.
 */
public class ReceiveSoundsThread extends BaseSoundsThread {

   private final static String  TAG = "ReceiveSoundsThread";
    //播放器
    private AudioTrack audioTrack;
    //音频文件大小
    private  int playBufSize;
    /****
     * 播放语音线程是否运行
     */
    private boolean isRunning ;
    /***
     * 接收到的包数据缓冲池
     */
    private LinkedBlockingDeque<byte[]> dataList = null;
    // 定义音频响度处理类
    private LoudnessEnhancer loudnessEnhancer;

    public ReceiveSoundsThread() {
        codec = new SpeexCoder();
        codec.InitSpeexDecode(frequency);
        // 播放器
        playBufSize = AudioTrack.getMinBufferSize(frequency, AudioFormat.CHANNEL_OUT_MONO,audioFormat );
        Log.e(TAG, "ReceiveSoundsThread: playBufSize"+playBufSize);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, AudioFormat.CHANNEL_OUT_MONO,audioFormat, playBufSize, AudioTrack.MODE_STREAM);

        //设置音量
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioTrack.setVolume(1.0f);
        }else {
            audioTrack.setStereoVolume(1.0f,1.0f);
        }
        //启动扬声器播放
        audioTrack.play();
        //音效增益  增大音量 增大响度
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            loudnessEnhancer = new LoudnessEnhancer(audioTrack.getAudioSessionId());
            //设置用于音频效果的目标响度
            loudnessEnhancer.setTargetGain(5000);
        }
    }

    @Override
    public  void run()
    {
        super.run();
        //初始化参数
        dataList =  new LinkedBlockingDeque<>();
        isRunning = true;
        //后台播音处理
        while (isRunning()){
            byte[]   data = null;
            try {
                //阻塞等待获取音频数据
               data  =  dataList.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
             //play audio data
            if (isRunning()){
                //播放音频
                playAudio(data);
            }

        }

    }

    /***
     * 音频解码后原生数据PCM
     */
    private   short[] decoderData = new short[pcmLen];

     public   void playAudio(byte [] data){
             try {
                 //解码  audio - >pcm
//                  Log.e(TAG, "playAudio: 音频数据解码"+data.length);
                  int  decoderDataLen = codec.SpeexDecodeAudioData(data,data.length,decoderData);
//                  Log.e(TAG, "playAudio: 音频数据解码完毕"+data);
                 if (decoderDataLen == 0 ){ //解码成功
                     // 播放解码后的数据  把数据写到数据流中
                     audioTrack.write(decoderData, 0, decoderData.length);
//                     Log.i(TAG, "run: 音频数据写到播放器中");
                 }
             } catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
                 Log.i(TAG, "run: "+e.toString());
             }
    }


    public synchronized boolean isRunning() {
        return isRunning;
    }

    /***
     * 待播放列表
     * 添加ARM语音数据包到播放队列中
     * @param recordBytes
     */
    public  void addRecordBytes(byte[] recordBytes){
        if (isRunning()){
            dataList.offer(recordBytes);
        }
//        Log.e(TAG, "addRecordBytes: 添加语音数据到队列中");
    }

    /***
     * 消除噪音
     * @param lin
     * @param off
     * @param len
     */
    private void calc1(short[] lin,int off,int len) {
        int i,j;
        for (i = 0; i < len; i++) {
            j = lin[i+off];
            lin[i+off] = (short)(j>>2);
        }
    }




    /***
     * 停止播放音频线程
     */
    public void stopMyReceiveSoundsThread(){
        if (loudnessEnhancer != null){
            // 释放所有对象
            loudnessEnhancer.release();
            loudnessEnhancer = null;
        }
        isRunning = false;
        codec.ReleaseSpeexDecode();
        dataList.clear();
        audioTrack.stop();
        audioTrack.release();
    }






}
