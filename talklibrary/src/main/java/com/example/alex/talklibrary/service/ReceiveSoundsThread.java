package com.example.alex.talklibrary.service;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

import com.example.alex.talklibrary.Utils.SpeexCoder;

import java.util.LinkedList;

/**
 * Created by alex on 16/10/5.
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
    private LinkedList<byte[]> dataList = null;


    public ReceiveSoundsThread() {
        codec = new SpeexCoder();
        codec.InitSpeexDecode(frequency);
        // 播放器
        playBufSize = AudioTrack.getMinBufferSize(frequency, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, playBufSize, AudioTrack.MODE_STREAM);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioTrack.setVolume(1.0f);
        }else {
            audioTrack.setStereoVolume(1.0f,1.0f);
        }

        audioTrack.play();  //播放
    }

    @Override
    public  void run()
    {
        super.run();

        //初始化参数
        dataList =  new LinkedList<>();
        isRunning = true;

        //后台播音处理
        while (true){

            if(!isRunning){
                return;
            }

            if( !dataList.isEmpty()){
                    //播放音频
                    playAudio();
            } else{
                //非运行状态 或者数据包为空
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }




     public   void playAudio(){

             byte [] data = dataList.removeFirst();

             try {
                 //解码  audio - >pcm
                 Log.e(TAG, "playAudio: 音频数据解码"+data.length);

                 short[] decoderData = new short[pcmLen];
                  int  decoderDataLen = codec.SpeexDecodeAudioData(data,data.length,decoderData,decoderData.length);
                 Log.e(TAG, "playAudio: 音频数据解码完毕"+data.length);
                 // 播放解码后的数据  把数据写到数据流中
                 audioTrack.write(decoderData, 0, decoderDataLen);
                 Log.i(TAG, "run: 音频数据写到播放器中");

             } catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
                 Log.i(TAG, "run: "+e.toString());
             }



    }




    /*****
     * 设置是否运行播放线程
     * @param isRunning
     */
    public void setRunning(boolean isRunning)
    {
        this.isRunning = isRunning;
    }


    /***
     * 待播放列表
     * 添加ARM语音数据包到播放队列中
     * @param recordBytes
     */
    public  void addRecordBytes(byte[] recordBytes){

        synchronized (dataList) {
            dataList.add(recordBytes);
            dataList.notifyAll();
        }
        Log.e(TAG, "addRecordBytes: 添加语音数据到队列中");
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
        isRunning = false;
        codec.ReleaseSpeexDecode();
        dataList.clear();
        audioTrack.stop();
        audioTrack.release();
    }






}
