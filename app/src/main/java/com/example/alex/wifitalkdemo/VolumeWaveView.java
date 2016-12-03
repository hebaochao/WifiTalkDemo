package com.example.alex.wifitalkdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;





public class VolumeWaveView  {

	
 	public SurfaceView sfv;
	
	/**
	 * X轴缩小的比例
	 */
	public int rateX = 352;

	/**
	 * Y轴缩小的比例
	 */
	public int rateY = 0;

	/**
	 * Y轴基线
	 */
	public int baseLine = 0;
	/**
	 * 为了节约绘画时间，每三个像素画一个数据
	 */
	int divider = 2;
	
	Paint mPaint;// 画笔
	
	private ArrayList<Integer> x = new ArrayList<Integer>();// 保存音量数据
	/****
	 * 主画笔默认颜色
	 */
	private  int paintMainColor = R.color.maincolor;

	/**
	 * 画布背景颜色
	 */
	private  int drawBgCoRlor = R.color.acoustic_wave_bg;

	private  Context context;

	public VolumeWaveView(SurfaceView sfv,Context context) {
		this.sfv = sfv;
		this.initSubView(context);
		this.context = context;

	}






	/***
	 * 初始化子视图
	 * @param context
	 */
	public void initSubView(Context context) {

//	  View view =  LayoutInflater.from(context).inflate(R.layout.soundwave_layout,null);
//
//		//初始化SurfaceView
//		sfv = (SurfaceView) view.findViewById(R.id.mysurface);
		sfv.setZOrderOnTop(true);
		sfv.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		// 初始化画笔
		mPaint = new Paint();
		mPaint.setColor(context.getResources().getColor(paintMainColor));// 画笔为主色调
		mPaint.setStrokeWidth(4);// 设置画笔粗细

	}



	/**
	 * 绘制指定区域
	 *
	 * @param buf
	 *            缓冲区
	 * @param baseLine
	 *            Y轴基线
	 */
	void SimpleDraw(ArrayList<Integer> buf, int baseLine) {
		if (rateY == 0) {
			rateY = 200 / sfv.getHeight();
			baseLine = sfv.getHeight() / 2;
		}
		Canvas canvas = sfv.getHolder().lockCanvas(
				new Rect(0, 0, sfv.getWidth(), sfv.getHeight()));// 关键:获取画布

		canvas.drawColor(context.getResources().getColor(drawBgCoRlor));// 清除背景
		int start = sfv.getWidth() - buf.size() * divider;
		int py = baseLine;
		if (buf.size() > 0)
			py += buf.get(0) / rateY;
		int y;
		canvas.drawLine(0, baseLine, start - divider, baseLine, mPaint);
		for (int i = 0; i < buf.size(); i++) {
			y = buf.get(i) / rateY + baseLine;// 调节缩小比例，调节基准线
			canvas.drawLine(start + (i - 1) * divider, py, start + i * divider,
					y, mPaint);
			py = y;
		}
		sfv.getHolder().unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
	}

	/**
	 * 更新话筒状态
	 *
	 */
	private int BASE = 1;

	/***
	 * 传递分贝值
	 * @param db
     */
	public void updateMicStatus(int db) {

//			double ratio = (double) MaxAmplitude / BASE;
//			int db = 0;// 分贝
//			if (ratio > 1)
//				db = (int) (20 * Math.log10(ratio)) - 50;
//			if (db < 0)
//				db = 0;
			x.add(-db);
			if (x.size() > sfv.getWidth() / divider) {
				x.remove(0);
			}
			SimpleDraw(x, baseLine);

	}

	public void setDrawBgCoRlor(int drawBgCoRlor) {
		this.drawBgCoRlor = drawBgCoRlor;
	}

	public void setPaintMainColor(int paintMainColor) {
		this.paintMainColor = paintMainColor;
	}
}
