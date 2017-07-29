package com.talk.newtalklib.service;

import android.media.AudioFormat;

import com.talk.newtalklib.code.SpeexCoder;


/**
 * Created by alex on 16/10/5.
 */
public class BaseSoundsThread extends  Thread {

    //编解码
    public SpeexCoder codec ;
    //采样率
    public int frequency = 8000;
    /***
     * 声道
     */
    public int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
    /***
     * 音频格式
     */
    public int audioFormat = AudioFormat.ENCODING_PCM_16BIT;


    public final   int pcmLen = frequency * 20/1000;




}
