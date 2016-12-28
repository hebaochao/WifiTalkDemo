package com.example.alex.talklibrary.Audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.example.alex.talklibrary.Config.AppConfig;
import com.example.alex.talklibrary.Utils.Codec;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

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
        // 播放器
        playBufSize = AudioTrack.getMinBufferSize(frequency, audioFormat, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, audioFormat, AudioFormat.ENCODING_PCM_16BIT, playBufSize, AudioTrack.MODE_STREAM);
        audioTrack.setStereoVolume(0.8f, 0.8f);// 设置当前音量大小
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




    synchronized public  void playAudio(){

        byte [] data = dataList.removeFirst();
        //接收到数据
        short[] out = new short[data.length];

        int  len = 0;
        try {
            //解码  ARM - >pcm
            len = codec.decode(data, out, data.length);
            Log.i(TAG, "run: len"+len);
            data = null;

            //消除噪音
            calc1(out,0,len);
//            Log.i(TAG, "run: 消除噪音");
            // 播放解码后的数据  把数据写到数据流中
            len = audioTrack.write(out, 0, len);
//            Log.i(TAG, "run: 音频数据写到播放器中");
//            data = null; //恢复临时数据

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
    public synchronized void addRecordBytes(byte[] recordBytes){
        dataList.addLast(recordBytes);
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
        dataList.clear();
        audioTrack.stop();
        audioTrack.release();
    }






}
