package com.hundsun.gmudemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ButtonGroup extends LinearLayout{

	private LayoutParams mLayoutParams;
	private OnClickListener mButtonOnClickListener;
	private OnClickListener mOnClickListener;
	private View mCurrentSelectedView;
	private int  mButtonBackgroundResId;
	private int  mButtonTopDrawableResId;
	private int  mButtonBottomDrawableResId;
	private RelativeLayout.LayoutParams mWrapItemLayoutParams;

	public int getButtonBackgroundResId() {
		return mButtonBackgroundResId;
	}

	public void setButtonBackgroundResId(int buttonBackgroundResId) {
		this.mButtonBackgroundResId = buttonBackgroundResId;
		changeButtonBackground();
	}
	
	public int getButtonTopDrawableResId() {
		return mButtonTopDrawableResId;
	}

	public void setButtonTopDrawableResId(int buttonTopDrawableResId) {
		this.mButtonTopDrawableResId = buttonTopDrawableResId;
		changeButtonBackground();
	}
	
	public int getButtonBottomDrawableResId() {
		return mButtonBottomDrawableResId;
	}

	public void setButtonBottomDrawableResId(int buttonBottomDrawableResId) {
		this.mButtonBottomDrawableResId = buttonBottomDrawableResId;
		changeButtonBackground();
	}

	public ButtonGroup( Context context ) {
		super(context);
		init(context);
	}
	
	public ButtonGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init( Context context ){
		mButtonBackgroundResId = R.drawable.selector_tabbar_btn;
		mLayoutParams = new LayoutParams( LayoutParams.FILL_PARENT , LayoutParams.FILL_PARENT , 1);
		mWrapItemLayoutParams = new RelativeLayout.LayoutParams( LayoutParams.WRAP_CONTENT , LayoutParams.FILL_PARENT );
		mWrapItemLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		mLayoutParams.gravity = Gravity.CENTER;
		setBackgroundResource(R.color.app_accordion_background);
		
		mOnClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				if (null != mButtonOnClickListener) {
					mButtonOnClickListener.onClick(view);
				}
				changeTab(view);
			}
		};
	}
	
	private void changeTab( View view  ){
		if (null != mCurrentSelectedView) {
			mCurrentSelectedView.setSelected(false);
		}
		view.setSelected(true);
		mCurrentSelectedView = view;
	}
	
	
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setProxyClickListenrer();
		ArrayList<View> childs = new ArrayList<View>();
		if (getChildCount() > 0) {
			changeTab( getChildAt(0) );
		}
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			childs.add(getChildAt(i));
		}
		removeAllViews();
		for (int i = 0; i < count; i++) {
//			childs.add(getChildAt(i));
			addView(wrapItem(childs.get(i)));
		}
		changeButtonBackground();
	}

	public void addButton( int textRes , int buttonId ){
		addButton( getContext().getString(textRes) , buttonId , 0);
	}
	
	public void addButton( String text , int buttonId ){
		addButton( text , buttonId , 0);
	}
	public void addButton( String text , int buttonId , int style ){
		TextView btn = new TextView(  getContext()  );
		btn.setText(text);
		btn.setId(buttonId);
		btn.setOnClickListener(mOnClickListener);
		
		if (0 != style) {
			btn.setTextAppearance(getContext(), style);
		}
		btn.setGravity(Gravity.CENTER);
		btn.setBackgroundResource(mButtonBackgroundResId);
		btn.setPadding(10, 0, 10, 0);
		addView( wrapItem(btn) );
		
		if (null == mCurrentSelectedView) {
			changeTab(btn);
		}
	}
	
	public void setOnButtonClick( OnClickListener onClickListener ){
		mButtonOnClickListener = onClickListener;
	}
	
	private void setProxyClickListenrer(){
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).setOnClickListener(mOnClickListener);
		}
	}
	
	private void changeButtonBackground(){
		int count = getChildCount();
		
		for (int i = 0; i < count; i++) {
			View btn = getChildAt(i);
			btn.setOnClickListener(mOnClickListener);
//			btn.setBackgroundResource(mButtonBackgroundResId);
		}
	}
	
	private View wrapItem( View tv){
		RelativeLayout layout = new RelativeLayout( getContext() );
		layout.setLayoutParams(mLayoutParams);
		tv.setLayoutParams(mWrapItemLayoutParams);
		layout.addView(tv);
		return layout;
	}
	
	public void selectTabItem( int idx ){
		try {
			RelativeLayout layout = (RelativeLayout) getChildAt(idx);
			layout.getChildAt(0).performClick();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}