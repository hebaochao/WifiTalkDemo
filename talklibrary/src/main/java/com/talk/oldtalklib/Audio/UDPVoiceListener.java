package com.talk.oldtalklib.Audio;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.util.Log;

import com.talk.oldtalklib.Config.AppConfig;
import com.talk.oldtalklib.Utils.Codec;
import com.talk.oldtalklib.Utils.Speex;


/**
 * 语音收发器
 *
 * @author wj
 * @creation 2013-5-16
 */
public class UDPVoiceListener extends UDPListener {

	// 文本消息监听端口
	private final int port = AppConfig.Port; //;
	private final int BUFFER_SIZE = 1024 * 6;// 6k的数据缓冲区

	int frequency = 8000;
	int recBufSize, playBufSize;
	private AudioRecord audioRecord;
	private AudioTrack audioTrack;
	private InetAddress address;

	Codec codec;

	private static UDPVoiceListener instance;

	private boolean go = true;
	private boolean isplay = false;
	/** Number of frame per second */
	int frame_rate;

	/** Number of bytes per frame */
	int frame_size;

	public static UDPVoiceListener getInstance(InetAddress address) {
		if (instance == null) {
//			instance = new UDPVoiceListener(address);
			instance = new UDPVoiceListener(address);
		}
		return instance;
	}

	private UDPVoiceListener(InetAddress address) {
		this.address = address;
	}

	@Override
	void init() {
		codec = new Speex();
		codec.init();
		frequency = codec.samp_rate();
		setPort(port);
		setBufferSize(BUFFER_SIZE);
		frame_size = codec.frame_size();
		recBufSize = AudioRecord.getMinBufferSize(frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
		playBufSize = AudioTrack.getMinBufferSize(frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);

		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, recBufSize);
		aec(audioRecord);
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, playBufSize, AudioTrack.MODE_STREAM);

		audioTrack.setStereoVolume(0.8f, 0.8f);// 设置当前音量大小

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

	public void startTalk() {
		audioRecord.startRecording();// 开始录制
		go = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (go) {
					send();
				}
			}
		}).start();
	}

	public void stopTalk() {
		go = false;
		audioRecord.stop();// 开始录制
	}
	int num,ring = 0,pos;
	/**
	 * 发送语音流
	 */
	void send() {
		byte[] buffer = new byte[160];
		short[] pcmFrame = new short[frame_size];
		num = audioRecord.read(pcmFrame, 0, frame_size);
		num = codec.encode(pcmFrame, 0, buffer, num);

		if(num>0){
			send(buffer,num, address, port);
		}

	}
	int len = 0;
	@Override
	public void onReceive(byte[] data, DatagramPacket packet) {
		if (!isplay) {
			isplay = true;
			audioTrack.play();// 开始播放
		}
		short[] out = new short[160];
		try {
			len = codec.decode(data, out, 160);
			len = audioTrack.write(out, 0, len);  // 播放解码后的数据
			Log.e("UDPVoiceListener", "**************" + len);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	void noticeOffline() throws IOException {

	}

	@Override
	void noticeOnline() throws IOException {

	}

	@Override
	void sendMsgFailure() {

	}

	@Override
	public void open() throws IOException {
		super.open();
	}

	@Override
	public void close() throws IOException {
		super.close();
		go = false;
		if (audioTrack != null)
			audioTrack.stop();
		if (audioRecord != null)
			audioRecord.stop();
		instance = null;
		codec.close();
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



}
