package com.example.alex.talklibrary.Audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;

import com.example.alex.talklibrary.Config.AppConfig;
import com.example.alex.talklibrary.Utils.Codec;
import com.example.alex.talklibrary.Utils.dataPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by alex on 16/10/5.
 */
public class NetSendSoundsThread extends BaseSoundsThread {

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


    public double getVolume() {
        if(!isRunning){  //录音完毕
            volume = 0;
        }
        return  volume;
    }

    public AudioRecord getAudioRecord() {
        return audioRecord;
    }

    public NetSendSoundsThread(int frame_size, Codec codec)
        {
            super(codec);
            this.frame_size = frame_size;
            recBufSize = AudioRecord.getMinBufferSize(frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, recBufSize);
            aec(audioRecord);

            // 录音机
//            int recordBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO,
//                    AudioFormat.ENCODING_PCM_16BIT);
//            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO,
//                    AudioFormat.ENCODING_PCM_16BIT, recordBufferSize);


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


        }

        @Override
        public synchronized void run()
        {
            super.run();
            audioRecord.startRecording();

            while (true)
            {
                if (isRunning)
                {
                    try
                    {
                        DatagramSocket clientSocket = new DatagramSocket();
                        InetAddress IP = InetAddress.getByName(AppConfig.IPAddress);// 向这个网络广播

                        // 获取音频数据
                        /***
                         * 从音频硬件录制缓冲区读取数据。
                         　                            audioData        写入的音频录制数据。
                         offsetInShorts           目标数组 audioData 的起始偏移量。
                         sizeInShorts              请求读取的数据大小。
                         　返回值
                         返回short型数据，表示读取到的数据，如果对象属性没有初始化，
                         则返回ERROR_INVALID_OPERATION，如果参数不能解析成有效的数据或索引，
                         则返回ERROR_BAD_VALUE。 返回数值不会超过sizeInShorts。
                         */
//                        audioRecord.read(recordBytes, 0, recordBytes.length);

                        byte[] buffer = new byte[160];  //音频数据
                        short[] pcmFrame = new short[frame_size];
                        num = audioRecord.read(pcmFrame, 0, frame_size);
                        num = codec.encode(pcmFrame, 0, buffer, num);



                        //获取分贝值
                         volume = NetSendSoundsThread.this.countDb(pcmFrame);


                        // 构建数据包 头+体
                        dataPacket dataPacket = new dataPacket(DevInfo.getBytes(), buffer);

                        // 构建数据报
                        DatagramPacket sendPacket = new DatagramPacket(dataPacket.getAllData(),
                                dataPacket.getAllData().length, IP, AppConfig.Port);

                        // 发送
                        clientSocket.send(sendPacket);
                        clientSocket.close();
                    }
                    catch (SocketException e)
                    {
                        e.printStackTrace();
                    }
                    catch (UnknownHostException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

    public void setRunning(boolean isRunning)
        {

            this.isRunning = isRunning;

        }

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
