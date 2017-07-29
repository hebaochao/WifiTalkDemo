package com.talk.newtalklib.code;

/**
 * Created by baochaoh on 2017/7/29.
 * speex音频编解码器
 */

public class SpeexCoder {


    static {
        System.loadLibrary("speex");
    }


    public native  void  InitSpeexEncode(int frameHZ);

    /***
     * 销毁编码器
     */
    public native  void  ReleaseSpeexEncode();

    /***
     *数据编码
     * @param pInBuf
     * @param len
     * @return
     */
    public native  int   SpeexEncodeAudioData(short[] pInBuf,int len,byte []outFrame,int outFrameSize);


    /***
     * 初始化解码器
     */
    public native  void  InitSpeexDecode(int frameHZ);

    /***
     * 销毁解码器
     */
    public native  void  ReleaseSpeexDecode();

    /***
     * 数据解码
     * @param pInBuf
     * @param len
     * @return
     */
    public native  int  SpeexDecodeAudioData(byte[] pInBuf, int len,short []outFrame);


}
