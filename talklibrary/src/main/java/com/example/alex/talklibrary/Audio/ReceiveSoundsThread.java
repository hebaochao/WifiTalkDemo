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
    private boolean isRunning = false;


    /***
     * 接收到的包数据大小
     */
    private byte[] recordBytes = null;


    /***
     * 初始化
     */
    public ReceiveSoundsThread(Codec mycode)
    {
        // 播放器
         super(mycode);
        playBufSize = AudioTrack.getMinBufferSize(frequency, audioFormat, AudioFormat.ENCODING_PCM_16BIT);
//        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, playBufSize, AudioTrack.MODE_STREAM);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, audioFormat, AudioFormat.ENCODING_PCM_16BIT, playBufSize, AudioTrack.MODE_STREAM);
        audioTrack.setStereoVolume(0.8f, 0.8f);// 设置当前音量大小
        audioTrack.play();  //播放
    }


    @Override
    public synchronized void run()
    {
        super.run();
            while (true)
            {
//                playAudio();
            }
    }


    public  void playAudio(){
        if (isRunning)
        {
            if(recordBytes != null) {
                //接收到数据
                short[] out = new short[recordBytes.length];
                int  len = 0;
                try {
                    //解码  ARM - >pcm
                    len = codec.decode(recordBytes, out, recordBytes.length);
                    Log.i(TAG, "run: len"+len);
                    //消除噪音
                    calc1(out,0,len);
                    Log.i(TAG, "run: 消除噪音");
                    // 播放解码后的数据  把数据写到数据流中
                    len = audioTrack.write(out, 0, len);
                    Log.i(TAG, "run: 音频数据写到播放器中");
                    recordBytes = null; //恢复临时数据
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.i(TAG, "run: "+e.toString());
                }
            }
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

    /*****
     * 添加ARM语音数据包到播放队列中
     * @param recordBytes
     */
    public void setRecordBytes(byte[] recordBytes) {
        this.recordBytes = recordBytes;
        playAudio();
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


    /****
     *释放音频数据流中的数据
     */
    public void ReleaseReceiveSoundsData(){
        audioTrack.stop();
        audioTrack.release();
        audioTrack.play();
    }




}
