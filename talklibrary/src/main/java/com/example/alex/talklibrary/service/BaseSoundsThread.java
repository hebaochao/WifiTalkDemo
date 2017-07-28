package com.example.alex.talklibrary.service;

import android.media.AudioFormat;

import com.example.alex.talklibrary.Utils.SpeexCoder;


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
    public int audioFormat = AudioFormat.CHANNEL_CONFIGURATION_MONO;


    public final   int pcmLen = frequency * 20/1000;




}
