package com.hundsun.gmudemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.hundsun.quotewidget.item.DealDetails;
import com.hundsun.quotewidget.item.Realtime;
import com.hundsun.quotewidget.item.Stock;
import com.hundsun.quotewidget.item.StockTrend;
import com.hundsun.quotewidget.kline.StockKline;
import com.hundsun.quotewidget.utils.QWColorUtils;
import com.hundsun.quotewidget.utils.QWFormatUtils;
import com.hundsun.quotewidget.utils.QWQuoteBase;
import com.hundsun.quotewidget.viewmodel.KlineViewModel;
import com.hundsun.quotewidget.viewmodel.RealtimeViewModel;
import com.hundsun.quotewidget.viewmodel.TickViewModel;
import com.hundsun.quotewidget.viewmodel.TrendViewModel;
import com.hundsun.quotewidget.widget.QiiFocusInfoWidget;
import com.hundsun.quotewidget.widget.QiiKlineLandscapeWidget;
import com.hundsun.quotewidget.widget.QiiTrendLandscapeWidget;
import com.hundsun.quotewidget.widget.QwKlineView;
import com.hundsun.quotewidget.widget.QwTrendViewTouchable;

import java.util.ArrayList;

public class QuoteLandsacpeDemoActivity extends Activity implements QwKlineView.IKlineEvent {
	public String activityId = "QuoteLandsacpeDemoActivity";

	private Stock mStock;
	private KlineViewModel mKlineViewModel;
	private TrendViewModel mTrendViewModel;
	private QiiTrendLandscapeWidget mTrendWidget;
	private QwTrendViewTouchable mTrendView;
	private QiiKlineLandscapeWidget mKlineView;
	private FrameLayout    mChartContainer;
	private ButtonGroup mQuoteBar;
	private IDataCenter mDataCenter;
	private boolean     mIsKlineDataLoading;
	private int mPeroid;
	private QiiFocusInfoWidget mFocusInfoWiddget;
	private float mHand;

	protected OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {

			default:
				onViewClick(view);
				break;
			}
		}
	};

	protected Realtime mStockRealtime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
		setContentView(R.layout.activity_quote_landsacpe_demo);

		mStock = getIntentStock();

		mKlineViewModel = new KlineViewModel(mStock);
		mTrendViewModel = new TrendViewModel();
		mChartContainer = (FrameLayout) findViewById(R.id.quote_widget);
		mChartContainer.setOnClickListener(mOnClickListener);
		mQuoteBar = (ButtonGroup) findViewById(R.id.qii_quote_bar);
		mQuoteBar.setOnButtonClick(mOnClickListener);
		
		mFocusInfoWiddget = (QiiFocusInfoWidget) findViewById(R.id.qii_quote_focus_info);
		mHand = 1;
		mDataCenter = H5DataCenter.getInstance();
//		mQuoteBar.selectTabItem(1);
//		showTrend();
		loadRealtime();
		loadDealDetail();

		mQuoteBar.selectTabItem(0);
	}

	private void loadDealDetail() {
		mDataCenter.loadStockTick(mStock, 0, 50, mHandler , "dealdetail");
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	protected void onViewClick(View view) {
		int id = view.getId();
		if (R.id.qii_bar_kline_period_day_button == id) {
			showKline( QWQuoteBase.KLinePeroid.PEROID_DAY );
		}else if (R.id.qii_bar_kline_period_week_button == id) {
			showKline( QWQuoteBase.KLinePeroid.PEROID_WEEK );
		}else if (R.id.qii_bar_kline_period_month_button == id) {
			showKline( QWQuoteBase.KLinePeroid.PEROID_MONTH );
		}else if (R.id.qii_bar_kline_period_minute_5_button == id) {
			showKline( QWQuoteBase.KLinePeroid.PEROID_5MIN );
		}else if (R.id.qii_bar_kline_period_minute_15_button == id) {
			showKline( QWQuoteBase.KLinePeroid.PEROID_15MIN );
		}else if (R.id.qii_bar_kline_period_minute_30_button == id) {
			showKline( QWQuoteBase.KLinePeroid.PEROID_30MIN );
		}else if (R.id.qii_bar_kline_period_minute_60_button == id) {
			showKline( QWQuoteBase.KLinePeroid.PEROID_60MIN );
		}else if (R.id.qii_bar_trend_button == id) {
			showTrend();
		}
		/*else if (R.id.qii_bar_level2_button == id) {
			showLevel2Widget();
		}*/
	}
	
	private void loadRealtime(){
		mDataCenter.loadRealtime(mStock, mHandler , null);
	}

	private void showTrend() {
		setKlineViewVisibility(View.GONE);
		if (QWQuoteBase.isUsStock(mStock) || QWQuoteBase.isIndex(mStock)) {
			if (null == mTrendView) {
				mTrendView = new QwTrendViewTouchable( this );
				mChartContainer.addView(mTrendView);
				mTrendView.setTrendEvent(mTrendEvent);
			}
			loadTrendData();
			mTrendView.setTrendViewModel(mTrendViewModel);
			mTrendView.invalidate();
		} else {
			if (null == mTrendWidget) {
				mTrendWidget = new QiiTrendLandscapeWidget(this);
				mChartContainer.addView(mTrendWidget);
				mTrendWidget.setTrendEvent(mTrendEvent);
			}
			loadTrendData();
			mTrendWidget.setTrendViewModel(mTrendViewModel);
			mTrendWidget.invalidate();
		}
		setTrendViewVisibility(View.VISIBLE);
	}

	public void setKlineViewVisibility( int visibility ) {
		if (mKlineView != null ) {
			mKlineView.setVisibility(visibility);
		}
	}
	public void setTrendViewVisibility( int visibility ) {
		if (mTrendWidget != null ) {
			mTrendWidget.setVisibility(visibility);
		}
		if (mTrendView != null ) {
			mTrendView.setVisibility(visibility);
		}
	}

	private void showKline( int peroid ) {
		mPeroid = QWQuoteBase.getKlinePeroid(mStock,  peroid);
		
		setTrendViewVisibility(View.GONE);
		
		if (null == mKlineView) {
			mKlineView = new QiiKlineLandscapeWidget(this, null);
			mChartContainer.addView(mKlineView);
			mKlineView.setKlineEvent( this );
			
		}

		mKlineViewModel.setStockDatas(null);
		mKlineView.setVisibility(View.VISIBLE);
		loadKlineData();
		mKlineView.setViewModel(mKlineViewModel);
		mKlineView.invalidateKlineView();
	}

	private Stock getIntentStock() {
		Stock stock = new Stock("600570" , "SS");
		return stock;
	}

	private void loadTrendData() {
//		StockTrend.Data data = TestDataGenerator.getTrendData();
//		mTrendViewModel.setTrends(data);
		
//		mDataCenter.loadStockTick( mStock, 0, 100, mHandler);
		mDataCenter.loadTrends(mStock, mHandler , null);
	}

	private void loadKlineData( ) {
		if (null == mStock) {
			return;
		}
		mDataCenter.loadKline( mStock,0,0, mPeroid, 200, mHandler , null);
		// dataCenter.loadTrends(mStock, mHandler);

//		mKlineViewModel.setStockDatas(TestDataGenerator.getKlineData());
	}

	Handler mHandler = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (msg != null && msg.obj != null) {
				DataCenterMessage dataMessage = (DataCenterMessage)msg.obj;
				
				if (msg.what == HsMessageContants.H5SDK_MSG_TREND) {
					StockTrend.Data data = null;
					data = dataMessage.getMessageData(data);
					mTrendViewModel.setTrends(data);

					if ( QWQuoteBase.isUsStock(mStock) || QWQuoteBase.isIndex(mStock)) {
						mTrendView.setTrendViewModel(mTrendViewModel);
						mTrendView.invalidate();
					}else if(mTrendWidget != null){
						mTrendWidget.setTrendViewModel(mTrendViewModel);
						mTrendWidget.invalidate();
					}
				}else if (msg.what == HsMessageContants.H5SDK_MSG_SNAPSHOT) {
					Realtime realtime = null;
					realtime = dataMessage.getMessageData(realtime);
					if (null != mStock) {
						mStock.setPreClosePrice(realtime.getPreClosePrice());
					}
					mStockRealtime = realtime;
					mKlineViewModel.setRealtime(realtime);
					mTrendViewModel.setRealtime(realtime);
					RealtimeViewModel viewModel = new RealtimeViewModel();
					viewModel.setRealtime( realtime );
					mHand = viewModel.getStocksPerHand();
					if (null != mTrendWidget) {
						mTrendWidget.setRealtimeViewModel(viewModel);
					}
					if (mHand <= 0) {
						mHand = 1;
					}
				}else if (msg.what == HsMessageContants.H5SDK_MSG_CANDLE_BY_OFFSET) {
					ArrayList<StockKline.Item> klineItems = null;
					klineItems = dataMessage.getMessageData(klineItems);
					String refreshString = "refreshKline"+mPeroid;
					
					ArrayList<StockKline.Item> datas = mKlineViewModel.getStockDatas();
					int dataAdded = 0;
					if (null == datas || datas.size() == 0) {
						datas = klineItems;
					} else {
//						if( klineItems == null || klineItems.size() > 0 || 
//								klineItems.get(klineItems.size()-1).date == datas.get(0).date) {
//							return true;
//						}
						if (refreshString.equals(dataMessage.getUserInfo())) {
							datas.remove(datas.size()-1);
							datas.addAll(  klineItems);
						}else{
							dataAdded = klineItems.size();
							datas.addAll(0 , klineItems);
						}
					}
					mKlineViewModel.setStockDatas(datas) ;
					
					if ( null != mKlineView) {
						mKlineView.dataAdded( dataAdded );
						mKlineView.invalidateKlineView();
					}
					mIsKlineDataLoading = false;
				}else if( msg.what == HsMessageContants.H5SDK_MSG_TICK_DIRECTION){
					DealDetails dealDetails = null;
					dealDetails = dataMessage.getMessageData(dealDetails);
					TickViewModel viewModel = new TickViewModel();
					viewModel.setDealDetailsData(dealDetails);
					if (null != mTrendWidget) {
						mTrendWidget.setTickViewModel( viewModel );
					}
				}
			}
			return false;
		}
	});

	@Override
	public void onFocus(int focusIndex, KlineViewModel viewModel, QwKlineView klineView) {
		if (focusIndex >= viewModel.getDataSize() || focusIndex < 0 ) {
			return;
		}
		if (!mFocusInfoWiddget.isShown()) {
			mFocusInfoWiddget.setVisibility(View.VISIBLE);
		}
		
		
		String date = viewModel.getDateTimeStr(focusIndex) ;
		String[] labels = { date, "开"  ,"高" , "低" ,"收" , "涨跌" , "量"};
		mFocusInfoWiddget.setLabels(labels);
		
		viewModel.setIndex(focusIndex);
		String[] values = {
				QWFormatUtils.formatPrice(mStock, viewModel.getOpenPrice()),
				QWFormatUtils.formatPrice(mStock, viewModel.getMaxPrice()),
				QWFormatUtils.formatPrice(mStock, viewModel.getMinPrice()),
				QWFormatUtils.formatPrice(mStock, viewModel.getClosePrice()),
				viewModel.getUpDownPercentStr(),
				QWFormatUtils.formatStockVolume( mStock,viewModel.getTotalDealAmount()/mHand)
		};
		
		double preClosePrice = viewModel.getClosePrice();
		if ( focusIndex > 1) {
			viewModel.setIndex(focusIndex - 1);
			preClosePrice = viewModel.getClosePrice();
		}
		viewModel.setIndex(focusIndex);
		
		int[] colors = {
				QWColorUtils.getColor(viewModel.getOpenPrice(), preClosePrice),
				QWColorUtils.getColor(viewModel.getMaxPrice(), preClosePrice),
				QWColorUtils.getColor(viewModel.getMinPrice(), preClosePrice),
				QWColorUtils.getColor(viewModel.getClosePrice(), preClosePrice),
				QWColorUtils.getColor(viewModel.getClosePrice(), preClosePrice),
				QWColorUtils.CHAR_COLOR
		};
		mFocusInfoWiddget.setValues(values, colors);
	}
	
	private QwTrendViewTouchable.ITrendEvent mTrendEvent = new  QwTrendViewTouchable.ITrendEvent(){

		@Override
		public void onUnFocus(TrendViewModel viewModel, QwTrendViewTouchable trendView) {
			if (mFocusInfoWiddget.isShown()) {
				mFocusInfoWiddget.setVisibility(View.GONE);
			}
		}

		@Override
		public void onFocus(int focusIndex, TrendViewModel viewModel, QwTrendViewTouchable trendView) {
			
			StockTrend.Item focusItem = viewModel.getTrendItem(focusIndex);
			if (null == focusItem ){
				return;
			}
			if (!mFocusInfoWiddget.isShown()) {
				mFocusInfoWiddget.setVisibility(View.VISIBLE);
			}
			
			double preClose = mStock.getPreClosePrice();
			double price = focusItem.getPrice();
			double inval = price - preClose;
			String percent = QWFormatUtils.formatPercent(inval/preClose) ;
			double wavg = focusItem.getWavg();
			double volume = focusItem.getVol()/mHand;
			String volumeStr = QWFormatUtils.formatStockVolume( mStock,volume );
			int wavgColor = QWColorUtils.getColor( wavg, preClose);
					
			String date = viewModel.getTime((focusIndex));
			String[] labels = { date ,"价格" , "涨跌" ,"涨跌幅" , "成交量" ,"均价" };
			if ("1A0001".equalsIgnoreCase(mStock.getStockcode()) || "2A01".equalsIgnoreCase(mStock.getStockcode())) {
				labels[5] = "领先";
				wavgColor = 0xffff8f2d;
			}
			
			mFocusInfoWiddget.setLabels(labels);
			
			String[] values = {
					QWFormatUtils.formatPrice(mStock, price ),
					QWFormatUtils.formatPrice(mStock, inval ),
					percent,
					volumeStr,
					QWFormatUtils.formatPrice(mStock, wavg )
			};
			
			int priceColor = QWColorUtils.getColor( price, preClose);
			int[] colors = {
					priceColor,
					priceColor,
					priceColor,
					0xffff8f2d,
					wavgColor,
			};
			mFocusInfoWiddget.setValues(values, colors);
		}
		
	};

	@Override
	public void onLeftDataChanged(int left, KlineViewModel viewModel, QwKlineView klineView) {
		if (left< 20 && !mIsKlineDataLoading) {
			mIsKlineDataLoading = true;
			ArrayList<StockKline.Item> klineItems = mKlineViewModel.getStockDatas();
			if (null == klineItems || klineItems.size() == 0) {
				return;
			}
			StockKline.Item item = klineItems.get(0);
			mDataCenter.loadKline( mStock, item.getDate(),item.getTime(), mPeroid, 100, mHandler , null);
		}
	}

	@Override
	public void onUnFocus(KlineViewModel viewModel, QwKlineView klineView) {
		if (mFocusInfoWiddget.isShown()) {
			mFocusInfoWiddget.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 刷新K线
	 */
	public void refreshKline(){
		if (null == mKlineViewModel) {
			return;
		}
		ArrayList<StockKline.Item> klineItems = mKlineViewModel.getStockDatas();
		if (null == klineItems || klineItems.size() == 0) {
			return;
		}
		int index = klineItems.size() -1;
		if (index > 0) {
			index -= 1;
		}
		StockKline.Item item = klineItems.get(index);
		mDataCenter.loadKline( mStock, item.getDate(),item.getTime(), mPeroid, 100, mHandler , "refreshKline"+mPeroid , HsMessageContants.H5SDK_ENUM_BACKWARD);
	}

	@Override
	public void onKlineModeChanged(int mode) {
		mKlineViewModel.setStockDatas(null);
		loadKlineData();
	}
}
