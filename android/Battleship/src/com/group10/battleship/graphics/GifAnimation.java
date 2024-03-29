package com.group10.battleship.graphics;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.RelativeLayout;

public class GifAnimation {
	private WebView mView;
	private RelativeLayout.LayoutParams params;
	private String fileName;

	float mScreenWidth;
	float mScreenHeight;
	float mScreenCurrentHeight;

	public GifAnimation(Context context, String name, float w, float h) {

		mView = new WebView(context);

		params = new RelativeLayout.LayoutParams((int) w, (int) h);
		params.setMargins(0, 0, 0, 0);
		mView.setLayoutParams(params);
		mView.requestLayout();

		mView.setBackgroundColor(Color.TRANSPARENT);

		mView.setVerticalScrollBarEnabled(false);
		mView.setHorizontalScrollBarEnabled(false);

		// disable scroll on touch
		mView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return (event.getAction() == MotionEvent.ACTION_MOVE);
			}
		});

		fileName = name;
	}

	public WebView getView() {
		return mView;
	}

	/**
	 * Sets the top left corner of the view
	 * 
	 * @param x
	 * @param y
	 */
	public void setPosition(int x, int y) {
		mView.setX(x);
		mView.setY(y);
	}

	/**
	 * load the gif into the webview
	 * 
	 * @param x
	 * @param y
	 */
	public void show(int x, int y, int size) {
		setPosition(x, y);
		String data = "<html><body><table width=\"" + size
				+ "px\"><tr><right><img width=\"100%\" src=\"" + fileName
				+ "\" /><right></tr><table></body></html>";
		mView.loadDataWithBaseURL("file:///android_asset/", data, "text/html",
				"UTF-8", null);
	}

	public void clear() {
		mView.loadUrl("about:blank");
		mView.clearCache(true);
	}
}
