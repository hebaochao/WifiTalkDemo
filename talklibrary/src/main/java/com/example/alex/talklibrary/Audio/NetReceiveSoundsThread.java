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
public class NetReceiveSoundsThread extends BaseSoundsThread {


    //播放器
    private AudioTrack audioTrack;
    //音频文件大小
    private  int playBufSize;
    private boolean isRunning = false;


    /***
     * 接收到的包数据大小
     */
    private byte[] recordBytes = new byte[190];


    /***
     * 初始化
     */
    public NetReceiveSoundsThread(Codec mycode)
    {
        // 播放器
//        int playerBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT);
//        player = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT, playerBufferSize, AudioTrack.MODE_STREAM);
        playBufSize = AudioTrack.getMinBufferSize(frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, playBufSize, AudioTrack.MODE_STREAM);

        audioTrack.setStereoVolume(0.8f, 0.8f);// 设置当前音量大小


    }


    @Override
    public synchronized void run()
    {
        super.run();

        try
        {
            @SuppressWarnings("resource")
            DatagramSocket serverSocket = new DatagramSocket(AppConfig.Port);
            while (true)
            {
                if (isRunning)
                {
                    //接收到包
                    DatagramPacket receivePacket = new DatagramPacket(recordBytes, recordBytes.length);
                    serverSocket.receive(receivePacket);

                    byte[] data = receivePacket.getData();

                    byte[] head = new byte[30];
                    byte[] body = new byte[160];

                    // 获得包头
                    for (int i = 0; i < head.length; i++)
                    {
                        head[i] = data[i];
                    }

                    // 获得包体
                    for (int i = 0; i < body.length; i++)
                    {
                        body[i] = data[i + 30];
                    }

                    // 获得头信息 通过头信息判断是否是自己发出的语音
                    String thisDevInfo = new String(head).trim();
                    System.out.println(thisDevInfo);

                    if (!thisDevInfo.equals(DevInfo))
                    {
                        short[] out = new short[160];
                        int  len = 0;
                        try {
                            len = codec.decode(body, out, body.length);
                            calc1(out,0,160);//消除噪音
                            len = audioTrack.write(out, 0, len);  // 播放解码后的数据
                            Log.e("UDPVoiceListener", "**************" + len);
                            audioTrack.play();  //播放
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setRunning(boolean isRunning)
    {
        this.isRunning = isRunning;
    }

    //消除噪音
    private void calc1(short[] lin,int off,int len) {
        int i,j;
        for (i = 0; i < len; i++) {
            j = lin[i+off];
            lin[i+off] = (short)(j>>2);
        }
    }


}
