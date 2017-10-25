package com.talk.newtalklib.service;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;

import com.talk.TalkAudioCallBack;
import com.talk.newtalklib.code.SpeexCoder;

import java.nio.ByteBuffer;


/**
 * Created by alex on 16/10/5.
 * 发送语音线程  ，录音的数据包通过接口回调
 */
public class SendSoundsThread extends BaseSoundsThread {

    //录音器
    private AudioRecord audioRecord;
    private boolean isRunning = false;

    /**
     * Number of bytes per frame
     */
    private int frame_size;


    /** Number of frame per second */

    /****
     * 分贝值
     */
    private double volume = 0;
    /***
     * 接口回调
     * 用于回调当前录音组成的数据包
     */
    public TalkAudioCallBack myCallBack;

    /***
     * 是否开启
     */
    public boolean isStart;


    public double getVolume() {
        if (!isRunning) {  //录音完毕
            volume = 0;
        }
        return volume;
    }


    public AudioRecord getAudioRecord() {
        return audioRecord;
    }

    public SendSoundsThread(int bufferSize) {
        super();
        codec = new SpeexCoder();
        //启动编码器
        codec.InitSpeexEncode(frequency);

        frame_size = AudioRecord.getMinBufferSize(frequency, AudioFormat.CHANNEL_IN_MONO, audioFormat);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, AudioFormat.CHANNEL_IN_MONO, audioFormat, frame_size);

        aec(audioRecord);

    }


    @Override
    public void run() {
        super.run();
        audioRecord.startRecording();
        isStart = true;

        //音频数据
        short[] pcmFrame = new short[pcmLen];
        //silk 格式
        byte[] encodeAudioData = new byte[frame_size];
        //call back data
        byte[] data = new byte[frame_size];

        while (true) {
            if (!isStart) {
                return;
            }
            if (isRunning()) {
                try {
                    //获取录音到的音频流数据（PCM）
                    int recordedSize = audioRecord.read(pcmFrame, 0, pcmLen);
                    //音频编码  pcm ->silk 格式
                    int encodeSize = frame_size;
                    //编码音频数据
                    encodeSize = codec.SpeexEncodeAudioData(pcmFrame, recordedSize, encodeAudioData, encodeSize);
                    if (encodeSize > 0) {
                        //回调录音数据包
//                        data = new byte[encodeSize];
                        ByteBuffer.wrap(encodeAudioData).get(data, 0, encodeSize);
                        if (myCallBack != null && isRunning()) {
                            myCallBack.RecordAudioData(data);
                        }
                        //获取分贝值
                        volume = SendSoundsThread.this.countDb(pcmFrame);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    sleep(1000*3);
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
    public void releaseSendSoundsThread() {
        isStart = false;
        codec.ReleaseSpeexEncode();

        myCallBack = null;
        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            }catch (Throwable e){
                e.printStackTrace();
            }
            audioRecord = null;
        }

    }

    /****
     * 设置录音线程是否运行
     * @param isRunning
     */
    public synchronized void setRunning(boolean isRunning) {

        this.isRunning = isRunning;
    }

    /***
     * 获取录音线程是否在工作
     * @return
     */
    public synchronized boolean isRunning() {
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
    public double countDb(short[] data) {
        float BASE = 32768f;
        float maxAmplitude = 0;

        for (int i = 0; i < data.length; i++) {
            maxAmplitude += data[i] * data[i];
        }
        maxAmplitude = (float) Math.sqrt(maxAmplitude / data.length);
        float ratio = maxAmplitude / BASE;
        float db = 0;
        if (ratio > 0) {
            db = (float) (20 * Math.log10(ratio)) + 100;
        }

        return db;
    }


}

