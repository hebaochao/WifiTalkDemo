package com.example.alex.wifitalkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.alex.talklibrary.Audio.NetReceiveSoundsThread;
import com.example.alex.talklibrary.Audio.NetSendSoundsThread;
import com.example.alex.talklibrary.Utils.Codec;
import com.example.alex.talklibrary.Utils.Speex;


import java.util.Timer;
import java.util.TimerTask;


public class MainActivity_test extends Activity
{
	private Button speakButton;// 按住说话
	private TextView message;
	private NetSendSoundsThread sendSoundsThread ;
	private NetReceiveSoundsThread receiveSoundsThread;
	private boolean isFirst = true;


	// 设备信息：手机名+Android版本
	private String DevInfo = android.os.Build.MODEL + " Android " + android.os.Build.VERSION.RELEASE;

	private Codec mycodec; //编解码
	/***
	 * 绘制音频波浪线
	 */
    private VolumeWaveView myVolumeWaveView ;
	private SurfaceView surfaceView;
	/***
	 * 定时器
	 */
	private Timer sfvtimer;

	/***
	 * 初始化录音以及播放线程
	 */
    private void initSoundThread(){
		mycodec = new Speex();
		mycodec.init();
		//初始化录音发送线程
		sendSoundsThread = new NetSendSoundsThread(mycodec.frame_size(),mycodec);
		receiveSoundsThread = new NetReceiveSoundsThread(mycodec);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        this.initSoundThread();

		message = (TextView) findViewById(R.id.Message);

	    surfaceView = (SurfaceView) this.findViewById(R.id.myVolumeWaveView);
//
		sfvtimer = new Timer();
		myVolumeWaveView = new VolumeWaveView(surfaceView,this);


//

		speakButton = (Button) findViewById(R.id.speakButton);
		speakButton.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					message.setText("松开结束");

					if (isFirst)
					{

						sendSoundsThread.start();
						receiveSoundsThread.start();
						isFirst = false;
						myVolumeWaveView.baseLine = surfaceView.getHeight()/2;
//						myVolumeWaveView.drawWidth = 200;
//						myVolumeWaveView.drawHight = surfaceView.getHeight();
						sfvtimer.schedule(new TimerTask() {
							@Override
							public void run() {
								Message message = new Message();
								message.what = 1;
								mHandler.sendMessage(message);
							}
						}, 0, 200);
					}

					sendSoundsThread.setRunning(true);
					receiveSoundsThread.setRunning(false);
				}
				else if (event.getAction() == MotionEvent.ACTION_UP)
				{
					message.setText("按住说话");
					sendSoundsThread.setRunning(false);
					receiveSoundsThread.setRunning(true);
				}
				return false;
			}
		});
	}


	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.i("分贝", "handleMessage: "+sendSoundsThread.getVolume());
			// TODO Auto-generated method stub
			myVolumeWaveView.updateMicStatus((int)sendSoundsThread.getVolume());

		}
	};
}
