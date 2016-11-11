package com.hundsun.gmudemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.hundsun.quotewidget.item.Realtime;
import com.hundsun.quotewidget.item.Stock;
import com.hundsun.quotewidget.item.StockTrend;
import com.hundsun.quotewidget.kline.StockKline;
import com.hundsun.quotewidget.utils.QWQuoteBase;
import com.hundsun.quotewidget.viewmodel.KlineViewModel;
import com.hundsun.quotewidget.viewmodel.RealtimeViewModel;
import com.hundsun.quotewidget.viewmodel.TrendViewModel;
import com.hundsun.quotewidget.viewmodel.ViewModel;
import com.hundsun.quotewidget.widget.QiiTrendWidget;
import com.hundsun.quotewidget.widget.QwKlineView;
import com.hundsun.quotewidget.widget.QwTrendView;

import java.util.ArrayList;

public class QuoteDemoActivity extends Activity {
	private Stock mStock ;
	private LinearLayout   mFrameContainer;
	private KlineViewModel mKlineViewModel;
	private TrendViewModel mTrendViewModel;
	private QiiTrendWidget mTrendView;
	private QwTrendView mQwTrendView;
	private QwKlineView mKlineView;
	private FrameLayout    mChartContainer;
	private ButtonGroup mQuoteBar;
	private ViewModel mRealtimeViewModel;

	private boolean mStopRefreshing;
	IDataCenter mDataCenter;
	private boolean mIsKlineDataLoading;
	private int mPeroid;
	
	protected OnClickListener mOnClickListener= new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			default:
				onViewClick(view);
				break;
			}
		}
	};


	@Override
	public void onCreate(Bundle saveStateBundle) {
		super.onCreate(saveStateBundle);
		setContentView(R.layout.activity_quote_demo);

		mPeroid = -1;
		mStock = new Stock("600570" , "SS");
		mKlineViewModel = new KlineViewModel( mStock );

		mFrameContainer = (LinearLayout) findViewById(R.id.frame_container);
		mChartContainer = (FrameLayout) findViewById(R.id.quote_widget);
		mChartContainer.setOnClickListener(mOnClickListener	);
		mQuoteBar = (ButtonGroup) findViewById(R.id.qii_quote_bar);
		mQuoteBar.setOnButtonClick(mOnClickListener);
		mDataCenter = H5DataCenter.getInstance();
		loadRealtime();

		mQuoteBar.selectTabItem(0);
	}

	protected void onViewClick(View view) {

		int id = view.getId();
		if ( id == R.id.quote_widget) {
			Intent intent = new Intent();
//			intent.putExtra(Keys.KEY_KLINE_PERIOD_TYPE, mPeroid);
//			intent.putExtra(Keys.STOCK_KEY, mStock);
			//			intent.putExtra(Keys.KEY_REALTIME_MODEL, mRealtimeViewModel);

		}else if (R.id.qii_bar_kline_period_day_button == id) {
			mPeroid = QWQuoteBase.KLinePeroid.PEROID_DAY;
			showKline( );
		}else if (R.id.qii_bar_kline_period_week_button == id) {
			mPeroid = QWQuoteBase.KLinePeroid.PEROID_WEEK;
			showKline( );
		}else if (R.id.qii_bar_kline_period_month_button == id) {
			mPeroid = QWQuoteBase.KLinePeroid.PEROID_MONTH;
			showKline( );
		}else if (R.id.qii_bar_trend_button == id) {
			showTrend( );
			mPeroid = -1;
		}
	}

	private void loadRealtime(){
		mDataCenter.loadRealtime(mStock, mHandler , null);
	}

	private void showTrend() {
		if (mKlineView != null && mKlineView.isShown()) {
			mKlineView.setVisibility(View.GONE);
		}
		if (QWQuoteBase.isUsStock(mStock) || QWQuoteBase.isIndex(mStock) ) {
			if ( null == mQwTrendView) {
				mQwTrendView = new QwTrendView(this);
				mChartContainer.addView(mQwTrendView);
				mQwTrendView.setIsDrawAxisInside(true);
			}
			mQwTrendView.setVisibility(View.VISIBLE);
			loadTrendData();
			mQwTrendView.setTrendViewModel(mTrendViewModel);
			mQwTrendView.invalidate();
		}else{
			if (null == mTrendView) {
				mTrendView = new QiiTrendWidget(this);
				mTrendView.setIsDrawAxisInside(true);
				mChartContainer.addView(mTrendView);
			}
			mTrendView.setVisibility(View.VISIBLE);
			loadTrendData();
			mTrendView.setTrendViewModel(mTrendViewModel);
			mTrendView.invalidate();
		}
	}

	private void showKline() {
		if ( -1 == mPeroid) {
			mPeroid = QWQuoteBase.KLinePeroid.PEROID_DAY;
		}
		int peroid = QWQuoteBase.getKlinePeroid(mStock, mPeroid);

		if (null == mKlineView) {
			mKlineView = new QwKlineView( this );
			mChartContainer.addView(mKlineView);
			mKlineView.setData( mKlineViewModel );
		}
		if (mTrendView != null && mTrendView.isShown()) {
			mTrendView.setVisibility(View.GONE);
		}
		if (mQwTrendView != null && mQwTrendView.isShown()) {
			mQwTrendView.setVisibility(View.GONE);
		}
		mKlineViewModel.setStockDatas(null);
		mKlineView.setVisibility(View.VISIBLE);
		loadKlineData(  peroid  );
		mIsKlineDataLoading = true;
		//		mKlineView.invalidate();
	}

	private void loadTrendData(){
		mTrendViewModel = new TrendViewModel();
		mDataCenter.loadTrends(mStock, mHandler , null);
	}

	private void loadKlineData(int period){
		if (null == mStock) {
			return;
		}

		mDataCenter.loadKline( mStock,0,0, period, 120, mHandler , null);
	}

	protected Realtime mStockRealtime;
	Handler mHandler = new Handler( new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			if (isFinishing() || mStopRefreshing) {
				return false;
			}
			if (msg != null && msg.obj != null) {
				DataCenterMessage dataMessage = (DataCenterMessage)msg.obj;

				if (msg.what == HsMessageContants.H5SDK_MSG_TREND) {
					StockTrend.Data data = null;
					data = dataMessage.getMessageData(data);
					mTrendViewModel.setTrends(data);
					mTrendViewModel.setRealtime(mStockRealtime);

					if (QWQuoteBase.isUsStock(mStock) || QWQuoteBase.isIndex(mStock)) {
						mQwTrendView.setTrendViewModel(mTrendViewModel);
						mQwTrendView.invalidate();
					} else {
						mTrendView.setTrendViewModel(mTrendViewModel);
						mTrendView.invalidate();
					}
				}else if (msg.what == HsMessageContants.H5SDK_MSG_SNAPSHOT) {
					Realtime realtimeObj = null;
					realtimeObj = dataMessage.getMessageData(realtimeObj);
					mStockRealtime = realtimeObj;

					String stockName = mStockRealtime.getName();
					if (!TextUtils.isEmpty(stockName)) {
						mStock.setStockName(stockName);
					}

					mStock.setPreClosePrice(realtimeObj.getPreClosePrice());
					if( QWQuoteBase.isIndex(mStock) ){
						mRealtimeViewModel = new RealtimeViewModel();
						mRealtimeViewModel.setRealtime(realtimeObj);
						mQwTrendView.invalidate();
					}else{
						mRealtimeViewModel = new RealtimeViewModel();
						mRealtimeViewModel.setRealtime(realtimeObj);
						mTrendView.setRealtimeViewModel(mRealtimeViewModel);
						mTrendView.invalidate();
					}
				}else if (msg.what == HsMessageContants.H5SDK_MSG_CANDLE_BY_OFFSET) {
					ArrayList<StockKline.Item> klineItems = null;
					klineItems = dataMessage.getMessageData(klineItems);

					ArrayList<StockKline.Item> datas = mKlineViewModel.getStockDatas();
					int dataAdded = 0;
					if (null == datas) {
						datas = klineItems;
					} else {
						dataAdded = klineItems.size();
						datas.addAll(0 , klineItems);
					}
					mKlineViewModel.setStockDatas(datas) ;

					if ( null != mKlineView) {
						mKlineView.dataAdded( dataAdded );
						mKlineView.invalidate();
					}
					mIsKlineDataLoading = false;
				}
			}
			return false;
		}
	});

	@Override
	public void onResume() {
		super.onResume();
		mStopRefreshing = false;
		loadRealtime();
	}

}
