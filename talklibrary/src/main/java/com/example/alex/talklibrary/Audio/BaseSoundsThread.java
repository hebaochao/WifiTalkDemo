package com.example.alex.talklibrary.Audio;

import android.media.AudioFormat;

import com.example.alex.talklibrary.Utils.*;

/**
 * Created by alex on 16/10/5.
 */
public class BaseSoundsThread extends  Thread {

    //编解码
    public Codec codec;
    // 设备信息：手机名+Android版本
    public String DevInfo = android.os.Build.MODEL + " Android " + android.os.Build.VERSION.RELEASE;
    //采样率
    public int frequency = 8000;
    /***
     * 声道
     */
    public int audioFormat = AudioFormat.CHANNEL_CONFIGURATION_MONO;

    /****
     * 初始化加码器
     * @param codec
     */
    public BaseSoundsThread(Codec codec) {
        super();
        this.codec = codec;
    }



}
