package com.hundsun.gmudemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hundsun.message.HsCommMessage;
import com.hundsun.message.HsCommRecord;
import com.hundsun.message.HsSessionException;
import com.hundsun.message.fields.HsCommSequenceItem;
import com.hundsun.message.fields.HsFieldItem;
import com.hundsun.message.fields.HsNoneItem;
import com.hundsun.message.interfaces.IH5Session;
import com.hundsun.message.interfaces.IH5SessionSettings;
import com.hundsun.message.interfaces.INetworkResponse;
import com.hundsun.message.interfaces.IOnSessionEvent;
import com.hundsun.message.interfaces.IUserOperationCallback;
import com.hundsun.message.net.HsH5Session;
import com.hundsun.message.net.HsSessionManager;
import com.hundsun.message.net.NetworkAddr;
import com.hundsun.message.net.SessionEvents;
import com.hundsun.quotewidget.item.DealDetails;
import com.hundsun.quotewidget.item.FinancialItem;
import com.hundsun.quotewidget.item.Market;
import com.hundsun.quotewidget.item.Realtime;
import com.hundsun.quotewidget.item.Realtime.PriceVolumeItem;
import com.hundsun.quotewidget.item.Stock;
import com.hundsun.quotewidget.item.StockTickItem;
import com.hundsun.quotewidget.item.StockTrend;
import com.hundsun.quotewidget.item.TradeTime;
import com.hundsun.quotewidget.kline.StockKline;
import com.hundsun.quotewidget.utils.QWFormatUtils;
import com.hundsun.quotewidget.utils.QWQuoteBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * 行情5的数据订阅中心
 * @author huangcheng LiangHao
 *
 */
public class H5DataCenter implements IDataCenter{
	static H5DataCenter sInstance;
	public static H5DataCenter getInstance(){
		if (null == sInstance) {
			sInstance = new H5DataCenter();
		}
		return sInstance;
	}
	
	private final static String TAG = H5DataCenter.class.getName();
	private static final String SESSION_KEY = "quote_session";
	private static String SERVER_HOST="112.124.211.5";
	private static int SERVER_PORT=9999;
	private static String USERNAME = "guest";
	private static String PASSWORD = "guest";
	private static String APP_SECRET="c1b00de0-32ac-40bb-9e70-02fe08bc608b";
	private static String APP_KEY="400d08fe-045d-42fc-8a6f-c6aa217c2abc";
	private static String TOKEN = "";
	private static String AUTHID = "";

	private static IH5Session sSession;

	private Handler mHandler;
	private int mLoginType;

	@Override
	public void queryStocks(String stock, int type,final Handler handler,final Object userInfo) {
		if(TextUtils.isEmpty(stock)){
			Log.v(TAG, "股票代码或股票名称为空!");
			return;
		}
		try {
			//获取键盘精灵快照消息
			HsCommMessage keyboardMsg = sSession.createMessage(HsMessageContants.BIZ_H5PROTO, HsMessageContants.H5SDK_KEYBOARD_WIZARD, HsMessageContants.REQUEST);	
			//构建消息{填充参数 <键盘精灵快照:股票代码,最大请求条数}
			keyboardMsg.getBodyRecord().setFieldValue(HsMessageContants.H5SDK_TAG_PROD_CODE, stock.getBytes());
			keyboardMsg.getBodyRecord().setFieldValue(HsMessageContants.H5SDK_TAG_MAX_VALUE, requestMaxRecord());
			//设置消息中带有重复数组参数
			HsCommSequenceItem msgItem_seq_types = keyboardMsg.getBodyRecord().newSequenceField(HsMessageContants.H5SDK_TAG_TYPE_GRP);
			if(type == 0){
				//沪深
				msgItem_seq_types.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, "XSHG.esa".getBytes());
				msgItem_seq_types.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, "XSHE.esa".getBytes());
				msgItem_seq_types.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, "XSHG.MRI".getBytes());
				msgItem_seq_types.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, "XSHE.MRI".getBytes());
				//			}else if(type == 1){
				//美股
				msgItem_seq_types.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, "XNAS.ES".getBytes());
				msgItem_seq_types.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, "XASE.ES".getBytes());
				msgItem_seq_types.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, "XNYS.ES".getBytes());
				msgItem_seq_types.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, "XNAS.MRI".getBytes());
				msgItem_seq_types.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, "XASE.MRI".getBytes());
				msgItem_seq_types.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, "XNYS.MRI".getBytes());
			}
			sSession.sendMessage(keyboardMsg, new INetworkResponse() {
				@Override
				public void handleMessage(IH5Session.MessageErrors result, HsCommMessage msg) {
					//返回股票列表 
					HsFieldItem item = msg.getBodyRecord().getItemByFieldId(HsMessageContants.H5SDK_TAG_PROD_GRP );
					if (item != HsNoneItem.NoneItem ) {
						HsCommSequenceItem sequenceItem = (HsCommSequenceItem) item;
						int size = sequenceItem.getRecordCount();
						final ArrayList<Stock> stockList = new ArrayList<Stock>();
						for (int i = 0; i < size; i++) {
							Stock stock = new Stock();
							HsCommRecord record = sequenceItem.getRecord(i);
							stock.setStockName(record.getStringValue(HsMessageContants.H5SDK_TAG_PROD_NAME));
							stock.setStockcode(record.getStringValue(HsMessageContants.H5SDK_TAG_PROD_CODE));
							stock.setCodeType(record.getStringValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE));
							stockList.add(stock);
							System.out.println(String.format("%s \t %s \t %s",stock.getStockName(), stock.getStockcode() , stock.getCodeType()));
						}
						sendMessage(stockList, handler , HsMessageContants.H5SDK_KEYBOARD_WIZARD , userInfo);
					}else{

					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loadTrends(final Stock pStock, final Handler handler,final Object userInfo) {
		if( pStock == null){
			return;
		}
		try {
			//分时 消息
			HsCommMessage thendsMsg = sSession.createMessage(HsMessageContants.BIZ_H5PROTO, HsMessageContants.H5SDK_MSG_TREND, HsMessageContants.REQUEST);	
			HsCommRecord thendsBody = thendsMsg.getBodyRecord();
			thendsBody.setFieldValue(HsMessageContants.H5SDK_TAG_HQ_LEVEL, HsMessageContants.H5SDK_ENUM_LEVEL_2); //美股行情
			thendsBody.setFieldValue(HsMessageContants.H5SDK_TAG_PROD_CODE, pStock.getStockcode().getBytes());
			thendsBody.setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, pStock.getCodeType().getBytes());
			sSession.sendMessage(thendsMsg, new INetworkResponse() {
				@Override
				public void handleMessage(IH5Session.MessageErrors result, HsCommMessage msg) {
					HsCommRecord thendsBodyRecord = msg.getBodyRecord();
					HsFieldItem thendsItem = thendsBodyRecord.getItemByFieldId(HsMessageContants.H5SDK_TAG_TREND_GRP);
					if (thendsItem != HsNoneItem.NoneItem) {
						HsCommSequenceItem thendsSequence = (HsCommSequenceItem) thendsItem;
						StockTrend.Data trendDatas = new StockTrend.Data();
						trendDatas.setDate(thendsBodyRecord.getIntValue(HsMessageContants.H5SDK_TAG_DATE)); //设定日期
						ArrayList<StockTrend.Item> trendDataList = new ArrayList<StockTrend.Item>();
						int len = thendsSequence.getRecordCount();
						long beforeVol = 0;
						long vol = 0;
						for (int k = 0; k < len; k++) {
							StockTrend.Item Item = new StockTrend.Item();
							HsCommRecord record = thendsSequence.getRecord(k);
							double priceUnit = QWQuoteBase.getPriceUnit(pStock);
							if(record != null){
								Item.setPrice(record.getInt64Value(HsMessageContants.H5SDK_TAG_HQ_PRICE)/priceUnit);
								Item.setAvg(record.getInt64Value(HsMessageContants.H5SDK_TAG_AVG_PX)/priceUnit);
								Item.setWavg(record.getInt64Value(HsMessageContants.H5SDK_TAG_WAVG_PX)/priceUnit);
								
								
								long currentVol = record.getInt64Value(HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT);
								if(k==0){
									vol = currentVol;
								}else{
									vol = currentVol - beforeVol;
								}
								beforeVol = currentVol;
								Item.setVol(vol);
								Item.setMoney(record.getInt64Value(HsMessageContants.H5SDK_TAG_BUSINESS_BALANCE));

//								System.out.println(String.format(
//										"%f \t %f \t %f \t %d \t %f ",
//										StockTrend.Item.getPrice(),
//										StockTrend.Item.getAvg(),
//										StockTrend.Item.getWavg(),
//										StockTrend.Item.getVol(),
//										StockTrend.Item.getMoney()));
							}else{
								Item.setPrice(0.00);
								Item.setAvg(0.00);
								Item.setWavg(0.00);
								Item.setVol(0);
								Item.setMoney(0);
							}
							trendDataList.add(Item);
						}
						trendDatas.setItems(trendDataList);
						sendMessage( trendDatas, handler  , HsMessageContants.H5SDK_MSG_TREND , userInfo);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void loadKline(Stock stock, long date, long time, int period, int count, Handler handler, Object userInfo) {
		loadKline( stock, date, time, period, count, handler, userInfo, HsMessageContants.H5SDK_ENUM_FORWARD);
	}

	@Override
	public void loadKline(final Stock pStock,long date , long time, int period, int count, final Handler handler,final Object userInfo, int direction) {
//		System.out.println(String.format("  code: %s \t time: %d \t count: %d", pStock.getStockcode() ,time , count ));
		if( pStock == null){
			return;
		}
		try {
			//k线 消息
			HsCommMessage klineMsg = sSession.createMessage(HsMessageContants.BIZ_H5PROTO, HsMessageContants.H5SDK_MSG_CANDLE_BY_OFFSET, HsMessageContants.REQUEST);	
			HsCommRecord  klineBody = klineMsg.getBodyRecord();
			//klineBody.setFieldValue(HsMessageContants.H5SDK_TAG_HQ_LEVEL,HsMessageContants.H5SDK_ENUM_LEVEL_2); //美股行情
			klineBody.setFieldValue(HsMessageContants.H5SDK_TAG_CANDLE_PEROID,period); //k线周期
			klineBody.setFieldValue(HsMessageContants.H5SDK_TAG_CANDLE_MODE, HsMessageContants.H5SDK_ENUM_CANDLE_ORIGINAL); //K线模式,是否复权
			klineBody.setFieldValue(HsMessageContants.H5SDK_TAG_DIRECTION, direction); //k线方向-1向前、【2：向后】
			klineBody.setFieldValue(HsMessageContants.H5SDK_TAG_DATA_COUNT,count); //k线个数
			klineBody.setFieldValue(HsMessageContants.H5SDK_TAG_PROD_CODE, pStock.getStockcode().getBytes());
			klineBody.setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, pStock.getCodeType().getBytes());
			if (0 != date) {
				klineBody.setFieldValue(HsMessageContants.H5SDK_TAG_DATE, date); //日期时间,YYYYMMDDHHmmss
				klineBody.setFieldValue(HsMessageContants.H5SDK_TAG_MIN_TIME, time); //日期时间,YYYYMMDDHHmmss
			}
			
			sSession.sendMessage(klineMsg, new INetworkResponse() {
				@Override
				public void handleMessage(IH5Session.MessageErrors result, HsCommMessage msg) {
					HsCommRecord klineBodyRecord = msg.getBodyRecord();
					HsFieldItem  klineBodyRecordItem = klineBodyRecord.getItemByFieldId(HsMessageContants.H5SDK_TAG_CANDLE_GRP);
					if (klineBodyRecordItem != HsNoneItem.NoneItem) {
						ArrayList<StockKline.Item> klineItemModeDatas = new ArrayList<StockKline.Item>();
						HsCommSequenceItem klineReqs = (HsCommSequenceItem) klineBodyRecord.getItemByFieldId(HsMessageContants.H5SDK_TAG_CANDLE_GRP);
						int klineSize = klineReqs.getRecordCount();
						for (int kline = 0; kline < klineSize; kline++) {
							HsCommRecord klineReqItem = klineReqs.getRecord(kline);
							StockKline.Item klineItemModeData = new StockKline.Item();
							fillKlineData(klineReqItem, klineItemModeData,pStock);
							klineItemModeDatas.add(klineItemModeData);
							//							System.out.println(String.format(
							//									"%f \t %f \t %f \t %f \t %d \t %d",
							//									klineItemModeData.getHighPrice(),
							//									klineItemModeData.getOpenPrice(),
							//									klineItemModeData.getLowPrice(),
							//									klineItemModeData.getClosePrice(),
							//									klineItemModeData.getVolume(),
							//									klineItemModeData.getDate()));
						}
						sendMessage(klineItemModeDatas, handler  , HsMessageContants.H5SDK_MSG_CANDLE_BY_OFFSET , userInfo);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loadRealtime(final Stock pStock, final Handler handler,final Object userInfo) {
		if( pStock == null){
			return;
		}
		int[] files =  getStockRealTimeFields( pStock ); //部分选择 
		try {
			//快照消息
			HsCommMessage realTimeMsg = sSession.createMessage(HsMessageContants.BIZ_H5PROTO, HsMessageContants.H5SDK_MSG_SNAPSHOT, HsMessageContants.REQUEST);	
			HsCommRecord realTimeMsgBody = realTimeMsg.getBodyRecord();
			realTimeMsgBody.setFieldValue(HsMessageContants.H5SDK_TAG_HQ_LEVEL,HsMessageContants.H5SDK_ENUM_LEVEL_2); //美股行情
			//股票集
			HsCommSequenceItem stocksSeq = realTimeMsgBody.newSequenceField(HsMessageContants.H5SDK_TAG_PROD_GRP); //股票集
			HsCommRecord stocksSeqRecord = stocksSeq.newRecord();
			stocksSeqRecord.setFieldValue(HsMessageContants.H5SDK_TAG_PROD_CODE, pStock.getStockcode().getBytes());
			stocksSeqRecord.setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, pStock.getCodeType().getBytes());
			//字段列表封装
			HsCommSequenceItem fieldsSeqs = realTimeMsgBody.newSequenceField( HsMessageContants.H5SDK_TAG_FIELDS); //字段集
			HsCommRecord field = null;
			for(int filed= 0; filed < files.length; filed++){
				field = fieldsSeqs.newRecord();
				field.setFieldValue(HsMessageContants.H5SDK_TAG_FIELD_ID , files[filed]);
			}
			sSession.sendMessage(realTimeMsg, new INetworkResponse() {
				@Override
				public void handleMessage(IH5Session.MessageErrors result, HsCommMessage msg) {
					HsCommRecord realTimeBodyRecord = msg.getBodyRecord();
					HsFieldItem realTimeItem = realTimeBodyRecord.getItemByFieldId(HsMessageContants.H5SDK_TAG_PROD_GRP);
					if (realTimeItem != HsNoneItem.NoneItem) {
						HsCommSequenceItem realtimeSequence = (HsCommSequenceItem) realTimeItem;
						HsCommRecord realtimData = realtimeSequence.getRecord(0);
						Realtime Realtime = new Realtime();
						fillH5QuoteRealtimeData(realtimData, Realtime,pStock);
						pStock.setPreClosePrice(Realtime.getPreClosePrice());
						sendMessage(Realtime, handler , HsMessageContants.H5SDK_MSG_SNAPSHOT , null);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public void loadListRealtime(final Stock[] pStock, final Handler handler,final Object userInfo) {
		if( pStock == null|| pStock.length == 0){
			return;
		}
		int[] files =  getStockRealTimeFields( null ); //部分选择 
		try {
			//快照消息
			HsCommMessage realTimeMsg = sSession.createMessage(HsMessageContants.BIZ_H5PROTO, HsMessageContants.H5SDK_MSG_SNAPSHOT, HsMessageContants.REQUEST);	
			HsCommRecord realTimeMsgBody = realTimeMsg.getBodyRecord();
			realTimeMsgBody.setFieldValue(HsMessageContants.H5SDK_TAG_HQ_LEVEL,HsMessageContants.H5SDK_ENUM_LEVEL_2); //美股行情
			//股票集
			HsCommSequenceItem stocksSeq = realTimeMsgBody.newSequenceField(HsMessageContants.H5SDK_TAG_PROD_GRP); //股票集
			int len = pStock.length;
			for(int k = 0;k < len; k++){
				Stock stock = pStock[k];
				HsCommRecord stocksSeqRecord = stocksSeq.newRecord();
				stocksSeqRecord.setFieldValue(HsMessageContants.H5SDK_TAG_PROD_CODE, stock.getStockcode().getBytes());
				stocksSeqRecord.setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, stock.getCodeType().getBytes());
			}
			//字段列表封装
			HsCommSequenceItem fieldsSeqs = realTimeMsgBody.newSequenceField( HsMessageContants.H5SDK_TAG_FIELDS); //字段集
			HsCommRecord field = null;
			for(int filed= 0; filed < files.length; filed++){
				field = fieldsSeqs.newRecord();
				field.setFieldValue(HsMessageContants.H5SDK_TAG_FIELD_ID , files[filed]);
			}
			sSession.sendMessage(realTimeMsg, new INetworkResponse() {
				@Override
				public void handleMessage(IH5Session.MessageErrors result, HsCommMessage msg) {
					HsCommRecord realTimeBodyRecord = msg.getBodyRecord();
					HsFieldItem realTimeItem = realTimeBodyRecord.getItemByFieldId(HsMessageContants.H5SDK_TAG_PROD_GRP);
					if (realTimeItem != HsNoneItem.NoneItem) {
						HsCommSequenceItem realtimeSequence = (HsCommSequenceItem) realTimeItem;
						int len = realtimeSequence.getRecordCount();
						ArrayList<Realtime> Realtime = new ArrayList<Realtime>();
						for(int k = 0;k < len; k++){
							HsCommRecord realtimData = realtimeSequence.getRecord(k);
							Realtime realTime = new Realtime();
							Realtime.add(realTime);
							Stock stock = new Stock();
							stock.setStockName(realtimData.getStringValue(HsMessageContants.H5SDK_TAG_PROD_NAME));
							stock.setStockcode(realtimData.getStringValue(HsMessageContants.H5SDK_TAG_PROD_CODE));
							stock.setCodeType(realtimData.getStringValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE));
							fillH5QuoteRealtimeData(realtimData, realTime, stock);
						}
						sendMessage(Realtime, handler,HsMessageContants.H5SDK_MSG_SNAPSHOT,userInfo);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void sendMessage(Object data, Handler handler, int msgId , Object userInfo ) {
		if (handler == null) {
			return;
		}
		DataCenterMessage messageData = new DataCenterMessage();
		messageData.setUserInfo(userInfo);
		messageData.setMessageData(data);
		
		Message message = handler.obtainMessage();
		message.what = msgId;
		message.obj = messageData;
		handler.sendMessage( message );
	}

	@Override
	public void loadMarketData(String market, Handler handler,final Object userInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadStockTick(final Stock stock, int begin, int count, final Handler handler,final Object userInfo) {
		HsCommMessage rankingMsg = sSession.createMessage(HsMessageContants.BIZ_H5PROTO, HsMessageContants.H5SDK_MSG_TICK_DIRECTION, HsMessageContants.REQUEST);
		HsCommRecord body = rankingMsg.getBodyRecord();
		body.setFieldValue(HsMessageContants.H5SDK_TAG_PROD_CODE, stock.getStockcode().getBytes());
		body.setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, stock.getCodeType().getBytes());
		body.setFieldValue(HsMessageContants.H5SDK_TAG_DATE, 0);
		body.setFieldValue(HsMessageContants.H5SDK_TAG_START_POS, begin);
		body.setFieldValue(HsMessageContants.H5SDK_TAG_DIRECTION, 1);
		body.setFieldValue(HsMessageContants.H5SDK_TAG_DATA_COUNT, count);

		sSession.sendMessage( rankingMsg, new INetworkResponse() {
			@Override
			public void handleMessage(IH5Session.MessageErrors result, HsCommMessage msg) {
				HsFieldItem item= msg.getBodyRecord().getItemByFieldId(HsMessageContants.H5SDK_TAG_TICK_GRP);
				if (HsNoneItem.NoneItem == item) {
					return;
				}
				DealDetails details = new DealDetails();
				HsCommSequenceItem stocks= (HsCommSequenceItem) msg.getBodyRecord().getItemByFieldId(HsMessageContants.H5SDK_TAG_TICK_GRP);
				ArrayList<StockTickItem> stockDetails = new ArrayList<StockTickItem>();
				int count = stocks.getRecordCount();
				double priceUnit = QWQuoteBase.getPriceUnit(stock);
				for (int i = 0; i < count; i++) {
					HsCommRecord realtimData = stocks.getRecord(i);
					StockTickItem tickitem = new StockTickItem();
					tickitem.setPrice(realtimData.getIntValue(HsMessageContants.H5SDK_TAG_HQ_PRICE)/priceUnit);
					tickitem.setTotalVolume(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT));
					tickitem.setMinutes(realtimData.getIntValue(HsMessageContants.H5SDK_TAG_BUSINESS_TIME));
					tickitem.setTradeFlag(realtimData.getIntValue(HsMessageContants.HSSDK_TAG_BUSINESS_DIRECTION));
					stockDetails.add(tickitem);
				}
				details.setDealDetails(stockDetails);
				sendMessage(details, handler , HsMessageContants.H5SDK_MSG_TICK_DIRECTION , null);
			}
		});
	}

	/**
	 * 最大请求条数
	 * @return
	 */
	private int requestMaxRecord(){
		return 10;
	}

	/**
	 * 动态修改参数列表
	 * @return
	 */
	private int[] getStockRealTimeFields( Stock stock ) {
		if ( null == stock ) {
			return REALTIME_FIELDS;
		}else if ( QWQuoteBase.isUsStock( stock ) ) {
			return US_STOCK_REALTIME_FIELDS;
		}else if ( QWQuoteBase.isIndex( stock ) ) {
			return INDEX_STOCK_REALTIME_FIELDS;
		}
		
		return CN_STOCK_REALTIME_FIELDS;
	}
	

	private int getTradeStatus(String tradeStatus){
		int status = 0;
		if (!TextUtils.isEmpty(tradeStatus)) {
			if("PRETR".equalsIgnoreCase(tradeStatus)){
				status = Realtime.TRADE_STATUS_PRETR;
			}else if("OCALL".equalsIgnoreCase(tradeStatus)){
				status = Realtime.TRADE_STATUS_OCALL;
			}else if("TRADE".equalsIgnoreCase(tradeStatus)){
				status = Realtime.TRADE_STATUS_TRADE;
			}else if("HALT".equalsIgnoreCase(tradeStatus)){
				status = Realtime.TRADE_STATUS_HALT;
			}else if("BREAK".equalsIgnoreCase(tradeStatus)){
				status = Realtime.TRADE_STATUS_BREAK;
			}else if("POSTR".equalsIgnoreCase(tradeStatus)){
				status = Realtime.TRADE_STATUS_POSTR;
			}else if("ENDTR".equalsIgnoreCase(tradeStatus)){
				status = Realtime.TRADE_STATUS_ENDTR;
			}else if("START".equalsIgnoreCase(tradeStatus)){
				status = Realtime.TRADE_STATUS_START;
			}
		}
		return status;
	}
	
	private void fillH5IndexQuoteRealtimeData(HsCommRecord realtimData,Realtime realtime,Stock pStock){
		if (realtime !=null) {
			int count = realtimData.getIntValue(HsMessageContants.HSSDK_TAG_RISE_COUNT);
			int fallCount = realtimData.getIntValue(HsMessageContants.HSSDK_TAG_FALL_COUNT);
			int totalCount = realtimData.getIntValue(HsMessageContants.HSSDK_TAG_MEMBER_COUNT);
			realtime.setFallCount(fallCount);
			realtime.setRiseCount(count);
			realtime.setTotalStocks(totalCount);
			realtime.setFallLeading(getLeadingStocks(realtimData, HsMessageContants.H5SDK_TAG_FALL_FIRST_GRP));
			realtime.setRiseLeading(getLeadingStocks(realtimData, HsMessageContants.H5SDK_TAG_RISE_FIRST_GRP));
		}
	}

	private ArrayList<Realtime> getLeadingStocks(HsCommRecord realtimData , int fieldId){
		ArrayList<Realtime> riseStock = new ArrayList<Realtime>();
		HsFieldItem se = realtimData.getItemByFieldId(fieldId);
		if (se != HsNoneItem.NoneItem) {
			HsCommSequenceItem seSequence = (HsCommSequenceItem) se;
			int recordCount = seSequence.getRecordCount();
			for (int i = 0; i < recordCount; i++) {
				HsCommRecord reccord = seSequence.getRecord(i);
				
				Realtime r = new Realtime();
				riseStock.add(r);
				String stockName = reccord.getStringValue(HsMessageContants.H5SDK_TAG_PROD_NAME);
				
				r.setCode(reccord.getStringValue(HsMessageContants.H5SDK_TAG_PROD_CODE));
				r.setCodeType( reccord.getStringValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE));
				r.setName(stockName);
				Stock stock = new Stock();
				
				stock.setStockName(reccord.getStringValue(HsMessageContants.H5SDK_TAG_PROD_NAME));
				stock.setStockcode(reccord.getStringValue(HsMessageContants.H5SDK_TAG_PROD_CODE));
				stock.setCodeType(reccord.getStringValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE));
				double priceUnit = QWQuoteBase.getPriceUnit(stock);
				
				double newPrice = reccord.getIntValue(HsMessageContants.H5SDK_TAG_LAST_PX)/priceUnit;
				r.setNewPrice( newPrice);         //最新成交价
				float value = reccord.getIntValue(HsMessageContants.H5SDK_TAG_PX_CHANGE_RATE)/10000f;
				String precent = QWFormatUtils.formatPercent( value);
				r.setPriceChangePrecent( precent ); //涨跌幅     float
				r.setPreClosePrice( newPrice - newPrice*value);
			}
		}
		
		return riseStock;
	}

	/**
	 * 获取行情快照数据封装到
	 * @param realtimData Realtime
	 * @param Realtime
	 */
	private void fillH5QuoteRealtimeData(HsCommRecord realtimData,Realtime Realtime,Stock pStock) {
		if(realtimData == null){
			setRealtimeDefaultValue(Realtime);
			return;
		}
		
		String stockName = realtimData.getStringValue(HsMessageContants.H5SDK_TAG_PROD_NAME);
		
		Realtime.setCode(realtimData.getStringValue(HsMessageContants.H5SDK_TAG_PROD_CODE));
		Realtime.setCodeType( realtimData.getStringValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE));
		pStock.setCodeType(Realtime.getCodeType());
		if (!TextUtils.isEmpty(stockName)) {
			Realtime.setName(stockName);
			pStock.setStockName(stockName);
		}else{
			Realtime.setName(pStock.getStockName());
		}
		

		double priceUnit = QWQuoteBase.getPriceUnit(pStock);
		
		Realtime.setIndustryCode(realtimData.getStringValue(HsMessageContants.H5SDK_TAG_INDUSTRY_CODE)); //行业代码
		Realtime.setCurrency(realtimData.getStringValue(HsMessageContants.H5SDK_TAG_MONEY_TYPE));  //货币代码
		Realtime.setTimestamp(realtimData.getIntValue(HsMessageContants.H5SDK_TAG_DATA_TIMESTAMP)); //时间戳
		Realtime.setTradeMinutes(realtimData.getIntValue(HsMessageContants.H5SDK_TAG_TRADE_MINS));//成交分钟数

		int tradeStatus = getTradeStatus(realtimData.getStringValue(HsMessageContants.H5SDK_TAG_TRADE_STATUS));
		Realtime.setTradeStatus(tradeStatus);  //交易状态

		Realtime.setPreClosePrice(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_PRECLOSE_PX)/priceUnit); //昨收价
		Realtime.setOpenPrice(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_OPEN_PX)/priceUnit);        //今开价
		Realtime.setNewPrice(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_LAST_PX)/priceUnit);         //最新成交价
		Realtime.setHighPrice(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_HIGH_PX)/priceUnit);        //最高价
		Realtime.setLowPrice(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_LOW_PX)/priceUnit);          //最低价	
		Realtime.setHighLimitPrice(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_UP_PRICE)/priceUnit);  //涨停价
		Realtime.setLowLimitPrice(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_DOWN_PRICE)/priceUnit); //跌停价
		Realtime.setAveragePrice(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_AVG_PX)/priceUnit) ;  //均价 
		Realtime.setWeightAveragePrice(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_WAVG_PX)/priceUnit);//加权均价
		Realtime.setClosePrice(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_CLOSE_PX)/priceUnit); //今日收盘价
		Realtime.setBeforeAfterPrice(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_POPC_PX)/priceUnit); //盘前/盘后价
		Realtime.set52WeekHighPrice(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_WEEK52_HIGH_PX)/priceUnit);//52周最高价
		Realtime.set52WeekLowPrice(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_WEEK52_LOW_PX)/priceUnit); //52周最低价

		Realtime.setOutside( realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT_OUT));
		Realtime.setInside(  realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT_IN));

		//总成交量
		Realtime.setTradeNumber(realtimData.getIntValue(HsMessageContants.H5SDK_TAG_BUSINESS_COUNT));   //成交笔数
		Realtime.setTotalVolume(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT));           
		Realtime.setTotalMoney(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_BUSINESS_BALANCE));    //总成交金额
		Realtime.setCurrent(realtimData.getIntValue(HsMessageContants.H5SDK_TAG_CURRENT_AMOUNT));      //现手 （最近成交量）
		Realtime.setInside(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT_IN));    //内盘成交量
		Realtime.setOutside(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT_OUT));
		//外盘成交量
		//财务数据
		FinancialItem financial = new FinancialItem();
		//总股本    Long - > String
		financial.setTotalShares(QWFormatUtils.formatBigNumber(realtimData.getInt64Value(HsMessageContants.H5SDK_TAG_TOTAL_SHARES)));
		//流通股
		financial.setCirculationShares(QWFormatUtils.formatBigNumber(realtimData.getInt64Value( HsMessageContants.HSSDK_TAG_CIRCULATION_AMOUNT )));
		//净资产(每股净资产)
		//financial.setNetAsset();
		//市盈  //市盈率 INT - > String
		financial.setPE(QWFormatUtils.format(2,realtimData.getIntValue( HsMessageContants.H5SDK_TAG_PE_RATE )/priceUnit));
		//每股收益
		financial.setEPS(QWFormatUtils.formatPrice( pStock, realtimData.getIntValue( HsMessageContants.HSSDK_TAG_EPS )/priceUnit));
		//市值
		financial.setMarketValue(QWFormatUtils.formatBigNumber( realtimData.getInt64Value( HsMessageContants.HSSDK_TAG_MARKET_VALUE ) ));
		financial.setCirculationValue(QWFormatUtils.formatBigNumber( realtimData.getInt64Value( HsMessageContants.HSSDK_TAG_CIRCULATION_VALUE ) ));
		//股东人数
		//financial.setStockHolders();
		Realtime.setFinancial(financial);
		Realtime.setHand(realtimData.getIntValue(HsMessageContants.HSSDK_TAG_SHARES_PER_HAND)); //每手 股数 
		Realtime.setTurnoverRatio(realtimData.getIntValue(HsMessageContants.H5SDK_TAG_TURNOVER_RATIO)/10000.0);
		if (0 == Realtime.getNewPrice()) {
			Realtime.setPriceChange( "--" );        //涨跌额
			Realtime.setPriceChangePrecent( "--" ); //涨跌幅     float 
		} else {
			//涨跌幅/额
			String priceChange = QWFormatUtils.formatPriceChange(pStock, realtimData.getIntValue(HsMessageContants.H5SDK_TAG_PX_CHANGE)/priceUnit);
			Realtime.setPriceChange( priceChange );        //涨跌额
			String precent = QWFormatUtils.formatPercent( realtimData.getIntValue(HsMessageContants.H5SDK_TAG_PX_CHANGE_RATE)/10000f);
			Realtime.setPriceChangePrecent( precent ); //涨跌幅     float 
		}
		
		
		//上涨、下跌家数
		if ( QWQuoteBase.isIndex( pStock ) ) {
			fillH5IndexQuoteRealtimeData(realtimData, Realtime, pStock);
		}
		Realtime.setBeforeAfterPrice(realtimData.getIntValue(HsMessageContants.H5SDK_TAG_POPC_PX) / priceUnit);

		//委买档位- H5 沪深使用
		HsFieldItem buyPriceVolumeRecord = realtimData.getItemByFieldId(HsMessageContants.H5SDK_TAG_BID_GRP); 
		if (buyPriceVolumeRecord != HsNoneItem.NoneItem) {
			HsCommSequenceItem buyPriVolSequence = (HsCommSequenceItem) buyPriceVolumeRecord;
			if(buyPriVolSequence != null){
				ArrayList<PriceVolumeItem> buyPriceList = new ArrayList<PriceVolumeItem>();
				int len = buyPriVolSequence.getRecordCount();
				for(int buyPriVol = 0;buyPriVol < len;buyPriVol++){
					HsCommRecord buyPriVol_Record = buyPriVolSequence.getRecord(buyPriVol);
					Realtime.PriceVolumeItem  buyPriVolItem = new Realtime.PriceVolumeItem();
					buyPriVolItem.price = (double)buyPriVol_Record.getInt64Value(HsMessageContants.H5SDK_TAG_ENTRUST_PX)/priceUnit; //委托价
					buyPriVolItem.volume = buyPriVol_Record.getIntValue(HsMessageContants.H5SDK_TAG_TOTAL_ENTRUST_AMOUNT); //委托量
					buyPriceList.add(buyPriVolItem);
				}
				Realtime.setBuyPriceList(buyPriceList);
			}
		}
		//委卖档位- H5 沪深使用{美股只有一档}
		HsFieldItem sellPriceVolumeRecord = realtimData.getItemByFieldId(HsMessageContants.H5SDK_TAG_OFFER_GRP); 
		if (sellPriceVolumeRecord != HsNoneItem.NoneItem) {
			HsCommSequenceItem sellPriVolSequence = (HsCommSequenceItem) sellPriceVolumeRecord;
			if(sellPriVolSequence != null){
				ArrayList<PriceVolumeItem> sellPriceList = new ArrayList<PriceVolumeItem>();
				int len = sellPriVolSequence.getRecordCount();
				for(int sellPriVol = 0;sellPriVol < len;sellPriVol++){
					HsCommRecord buyPriVol_Record = sellPriVolSequence.getRecord(sellPriVol);
					Realtime.PriceVolumeItem  sellPriVolItem = new Realtime.PriceVolumeItem();
					sellPriVolItem.price = (double)buyPriVol_Record.getInt64Value(HsMessageContants.H5SDK_TAG_ENTRUST_PX)/priceUnit; //委托价
					sellPriVolItem.volume = buyPriVol_Record.getIntValue(HsMessageContants.H5SDK_TAG_TOTAL_ENTRUST_AMOUNT); //委托量
					sellPriceList.add(sellPriVolItem);
				}
				Realtime.setSellPriceList(sellPriceList);
			}
		}
		
		pStock.setPreClosePrice(Realtime.getPreClosePrice());
	}

	private void setRealtimeDefaultValue(Realtime Realtime) {
		String DEFAULT_STR = "--";
		double DEFAULT_DOB = 0.00;
		Realtime.setIndustryCode(DEFAULT_STR); //行业代码
		Realtime.setCurrency(DEFAULT_STR);  //货币代码
		Realtime.setTimestamp(0); //时间戳
		Realtime.setTradeMinutes(0);//成交分钟数
		Realtime.setTradeStatus(0);  //交易状态
		Realtime.setPreClosePrice(DEFAULT_DOB); //昨收价
		Realtime.setOpenPrice(DEFAULT_DOB);        //今开价
		Realtime.setNewPrice(DEFAULT_DOB);         //最新成交价
		Realtime.setHighPrice(DEFAULT_DOB);        //最高价
		Realtime.setLowPrice(DEFAULT_DOB);          //最低价	
		Realtime.setHighLimitPrice(DEFAULT_DOB);  //涨停价
		Realtime.setLowLimitPrice(DEFAULT_DOB); //跌停价
		Realtime.setAveragePrice(DEFAULT_DOB) ;  //均价 
		Realtime.setWeightAveragePrice(DEFAULT_DOB);//加权均价
		Realtime.setClosePrice(DEFAULT_DOB); //今日收盘价
		Realtime.setBeforeAfterPrice(DEFAULT_DOB); //盘前/盘后价
		Realtime.set52WeekHighPrice(DEFAULT_DOB);//52周最高价
		Realtime.set52WeekLowPrice(DEFAULT_DOB); //52周最低价
		//总成交量
		Realtime.setTradeNumber(0);   //成交笔数
		Realtime.setTotalVolume(0);           
		Realtime.setTotalMoney(0);    //总成交金额
		Realtime.setCurrent(0);      //现手 （最近成交量）
		Realtime.setInside(0);    //内盘成交量
		Realtime.setOutside(0);
		//外盘成交量
		//财务数据
		FinancialItem financial = new FinancialItem();
		//总股本    Long - > String
		financial.setTotalShares("--");
		//流通股
		//financial.setCirculationShares();
		//净资产(每股净资产)
		//financial.setNetAsset();
		//市盈  //市盈率 INT - > String
		financial.setPE(DEFAULT_STR);
		//每股收益
		financial.setEPS(DEFAULT_STR);
		//市值
		financial.setMarketValue(DEFAULT_STR);
		//股东人数
		//financial.setStockHolders();
		Realtime.setFinancial(financial);
		Realtime.setHand(0); //每手 股数 
		//涨跌幅/额
		Realtime.setPriceChange(DEFAULT_STR);        //涨跌额
		Realtime.setPriceChangePrecent(DEFAULT_STR); //涨跌幅     float 
		//委买档位- H5 沪深使用
	}

	/**
	 * 构造分时数据
	 * @param klineReqItem
	 * @param klineItemModeData
	 * @param pStock 
	 */
	private void fillKlineData(HsCommRecord klineReqItem,StockKline.Item klineItemModeData, Stock pStock) {
		double priceUnit =  QWQuoteBase.getPriceUnit(pStock);
		if(klineReqItem == null){
			klineItemModeData.setClosePrice(0.00);
			klineItemModeData.setOpenPrice(0.00);
			klineItemModeData.setLowPrice(0.00);
			klineItemModeData.setHighPrice(0.00);
			klineItemModeData.setMoney(0);
			klineItemModeData.setVolume(0);
			klineItemModeData.setDate(116);
		}else{
			klineItemModeData.setClosePrice(klineReqItem.getInt64Value(HsMessageContants.H5SDK_TAG_CLOSE_PX)/priceUnit);
			klineItemModeData.setOpenPrice(klineReqItem.getInt64Value(HsMessageContants.H5SDK_TAG_OPEN_PX)/priceUnit);
			klineItemModeData.setLowPrice(klineReqItem.getInt64Value(HsMessageContants.H5SDK_TAG_LOW_PX)/priceUnit);
			klineItemModeData.setHighPrice(klineReqItem.getInt64Value(HsMessageContants.H5SDK_TAG_HIGH_PX)/priceUnit);
			klineItemModeData.setMoney(klineReqItem.getInt64Value(HsMessageContants.H5SDK_TAG_BUSINESS_BALANCE));
			klineItemModeData.setVolume(klineReqItem.getInt64Value(HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT));
			klineItemModeData.setDate(klineReqItem.getIntValue(HsMessageContants.H5SDK_TAG_DATE));
			klineItemModeData.setTime(klineReqItem.getIntValue(HsMessageContants.H5SDK_TAG_MIN_TIME));
		}
	}

	/* 
	 * 获取一级板块
	 * (non-Javadoc)
	 * @see com.hundsun.quote.net.IDataCenter#loadBlockData(android.os.Handler)
	 */
	@Override
	public void loadBlockData(Handler handler,final Object userInfo) {
		// TODO Auto-generated method stub

	}

	/* 
	 * 获取所属板块下的子版块
	 * (non-Javadoc)
	 * @see com.hundsun.quote.net.IDataCenter#loadSubBlockData(java.lang.String, android.os.Handler)
	 */
	@Override
	public void loadSubBlockData(String blockName, Handler handler,final Object userInfo) {
		// TODO Auto-generated method stub

	}

	/* 
	 * 获取所属板块下的股票列表
	 */
	@Override
	public void loadBlockStocksData(String blockName, int begin, int count,Handler handler,final Object userInfo) {
		// TODO Auto-generated method stub

	}

	/* 
	 * 请求排名数据【H5沪深行情、暂不支持美股】
	 */
	@Override
	public void loadRankingStocksData(String[] marketType, int begin, int count,QWQuoteBase.SORT sortType, int orderTpye,final Handler handler, final Object userInfo) {
		HsCommMessage rankingMsg = sSession.createMessage(HsMessageContants.BIZ_H5PROTO, HsMessageContants.H5SDK_MSG_SORT, HsMessageContants.REQUEST);
		HsCommRecord body = rankingMsg.getBodyRecord();
		int sortField = getSortField( sortType );
		body.setFieldValue( HsMessageContants.H5SDK_TAG_SORT_FIELD_ID, sortField );
		body.setFieldValue( HsMessageContants.H5SDK_TAG_START_POS,begin);
		body.setFieldValue( HsMessageContants.H5SDK_TAG_DATA_COUNT, count);
		body.setFieldValue( HsMessageContants.H5SDK_TAG_SORT_TYPE, orderTpye);
		HsCommSequenceItem types = body.newSequenceField( HsMessageContants.H5SDK_TAG_SORT_TYPE_GRP );
		for (String market : marketType) {
			types.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, market.getBytes());
		}
		
		int[] files =  getStockRealTimeFields( null ); //部分选择
		//字段列表封装
		HsCommSequenceItem fieldsSeqs = body.newSequenceField( HsMessageContants.H5SDK_TAG_FIELDS); //字段集
		HsCommRecord field = null;
		for(int filed= 0; filed < files.length; filed++){
			field = fieldsSeqs.newRecord();
			field.setFieldValue(HsMessageContants.H5SDK_TAG_FIELD_ID , files[filed]);
		}

		sSession.sendMessage( rankingMsg, new INetworkResponse() {
			@Override
			public void handleMessage(IH5Session.MessageErrors result, HsCommMessage msg) {
				HsFieldItem item= msg.getBodyRecord().getItemByFieldId( HsMessageContants.H5SDK_TAG_SORT_PROD_GRP );
				if (HsNoneItem.NoneItem == item) {
					return;
				}
				HsCommSequenceItem stocks= (HsCommSequenceItem) msg.getBodyRecord().getItemByFieldId( HsMessageContants.H5SDK_TAG_SORT_PROD_GRP );
				//				HsBytevectorItem name = (HsBytevectorItem) stocks.getRecord(0).getItemByFieldId(55);
				//				String str = new String(name.getRawData() , Charset.forName("gbk"));
				ArrayList<Realtime> stockRealtimes = new ArrayList<Realtime>();
				int count = stocks.getRecordCount();
				for (int i = 0; i < count; i++) {
					Realtime Realtime = new Realtime();
					HsCommRecord realtimData = stocks.getRecord(i);
					Stock stock = new Stock();
					stock.setStockName(realtimData.getStringValue(HsMessageContants.H5SDK_TAG_PROD_NAME));
					stock.setStockcode(realtimData.getStringValue(HsMessageContants.H5SDK_TAG_PROD_CODE));
					stock.setCodeType(realtimData.getStringValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE));
					fillH5QuoteRealtimeData(realtimData, Realtime, stock);
					stockRealtimes.add(Realtime);
				}
				sendMessage(stockRealtimes, handler , HsMessageContants.H5SDK_MSG_SORT  , userInfo);
			}
		});
	}
	
	/* 
	 * 请求排名数据【H5沪深行情、暂不支持美股】
	 */
	@Override
	public void loadRankingStocksData(Stock[] stocks, QWQuoteBase.SORT sortType, int orderTpye,final Handler handler, final Object userInfo) {
		if (null == stocks || stocks.length == 0) {
			return;
		}
		HsCommMessage rankingMsg = sSession.createMessage(HsMessageContants.BIZ_H5PROTO, HsMessageContants.H5SDK_MSG_SORT, HsMessageContants.REQUEST);
		HsCommRecord body = rankingMsg.getBodyRecord();
		
		int sortField = getSortField( sortType );
		body.setFieldValue( HsMessageContants.H5SDK_TAG_SORT_FIELD_ID, sortField );
		body.setFieldValue( HsMessageContants.H5SDK_TAG_SORT_TYPE, orderTpye);
		
		HsCommSequenceItem stockSequence = body.newSequenceField( HsMessageContants.H5SDK_TAG_SORT_PROD_GRP );
		HsCommRecord stockRecord = null;
		for (Stock stock : stocks) {
			stockRecord = stockSequence.newRecord();
			stockRecord.setFieldValue(HsMessageContants.H5SDK_TAG_PROD_CODE, stock.getStockcode().getBytes());
			stockRecord.setFieldValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE, stock.getCodeType().getBytes());
		}

		int[] files =  getStockRealTimeFields( null ); //部分选择
		//字段列表封装
		HsCommSequenceItem fieldsSeqs = body.newSequenceField( HsMessageContants.H5SDK_TAG_FIELDS); //字段集
		HsCommRecord field = null;
		for(int filed= 0; filed < files.length; filed++){
			field = fieldsSeqs.newRecord();
			field.setFieldValue(HsMessageContants.H5SDK_TAG_FIELD_ID , files[filed]);
		}

		sSession.sendMessage( rankingMsg, new INetworkResponse() {
			@Override
			public void handleMessage(IH5Session.MessageErrors result, HsCommMessage msg) {
				HsFieldItem item= msg.getBodyRecord().getItemByFieldId( HsMessageContants.H5SDK_TAG_SORT_PROD_GRP );
				if (HsNoneItem.NoneItem == item) {
					return;
				}
				HsCommSequenceItem stocks= (HsCommSequenceItem) msg.getBodyRecord().getItemByFieldId( HsMessageContants.H5SDK_TAG_SORT_PROD_GRP );
				ArrayList<Realtime> stockRealtimes = new ArrayList<Realtime>();
				int count = stocks.getRecordCount();
				for (int i = 0; i < count; i++) {
					Realtime Realtime = new Realtime();
					HsCommRecord realtimData = stocks.getRecord(i);
					Stock stock = new Stock();
					stock.setStockName(realtimData.getStringValue(HsMessageContants.H5SDK_TAG_PROD_NAME));
					stock.setStockcode(realtimData.getStringValue(HsMessageContants.H5SDK_TAG_PROD_CODE));
					stock.setCodeType(realtimData.getStringValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE));
					fillH5QuoteRealtimeData(realtimData, Realtime, stock);
					stockRealtimes.add(Realtime);
				}
				sendMessage(stockRealtimes, handler , HsMessageContants.H5SDK_MSG_SORT  , userInfo);
			}
		});
	}
	
	static int getSortField( QWQuoteBase.SORT sortField ){
		if (sortField == null) {
			sortField = QWQuoteBase.SORT.PRICE_CHANGE_PERCENT; 
		}
		int field = 0;
		switch (sortField) {
		case STOCK_CODE:   		  // 股票代码
			field = HsMessageContants.H5SDK_TAG_PROD_CODE;
			break;
		case STOCK_NAME:    		  // 股票名称
			field = HsMessageContants.H5SDK_TAG_PROD_NAME;
			break;
		case NEW_PRICE:             // 最新价 -成交价
			field = HsMessageContants.H5SDK_TAG_LAST_PX;
			break;
		case OPEN_PRICE:            // 开盘价
			field = HsMessageContants.H5SDK_TAG_OPEN_PX;
			break;
		case PRE_CLOSE_PRICE:       // 换手率
			field = HsMessageContants.H5SDK_TAG_PRECLOSE_PX;
			break;
		case AVERAGE_PRICE:         // 均价
			field = HsMessageContants.H5SDK_TAG_AVG_PX;
			break;
		case HIGH_PRICE:            // 最高价
			field = HsMessageContants.H5SDK_TAG_HIGH_PX;
			break;
		case LOW_PRICE:             // 最低价
			field = HsMessageContants.H5SDK_TAG_LOW_PX;
			break;
		case AMPLITUDE:             // 振幅
			field = HsMessageContants.H5SDK_TAG_AMPLITUDE;
			break;
		case ENTRUST_RADIO:         // 委比
			field = HsMessageContants.H5SDK_TAG_ENTRUST_RATE;
			break;
		case ENTRUST_DIFF:          // 委差
			field = HsMessageContants.H5SDK_TAG_ENTRUST_DIFF;
			break;
		case VOLUME_RADIO:          // 量比
			field = HsMessageContants.H5SDK_TAG_VOL_RATIO;
			break;
		case CHANGE_HAND_RADIO:     // 换手率
			field = HsMessageContants.H5SDK_TAG_TURNOVER_RATIO;
			break;
		case PRICE_CHANGE_PERCENT:  // 涨跌幅
			field = HsMessageContants.H5SDK_TAG_PX_CHANGE_RATE;
			break;
		case PRICE_CHANGE:          // 涨跌
			field = HsMessageContants.H5SDK_TAG_PX_CHANGE;
			break;
		case TOTAL_MONEY:           // 总成交金额
			field = HsMessageContants.H5SDK_TAG_BUSINESS_BALANCE;
			break;
		case TOTAL_VOLUME:          // 总成交量
			field = HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT;
			break;
		case CURRENT:               // 现手
			field = HsMessageContants.H5SDK_TAG_CURRENT_AMOUNT;
			break;
		case PE_RADIO:              // 市盈率
			field = HsMessageContants.H5SDK_TAG_PE_RATE;
			break;
		case RISE_SPEED:            // 涨速
			field = 0;
			break;
		case INSIDE:                // 内盘
			field = HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT_IN;
			break;
		case OUTSIDE:                // 外盘
			field = HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT_OUT;
			break;

		default:
			break;
		}
		
		return field;
	}
	
	
	public static final int[] CN_STOCK_REALTIME_FIELDS = new int[]{
			HsMessageContants.H5SDK_TAG_PROD_CODE,            //证券代码 
			HsMessageContants.H5SDK_TAG_PROD_NAME,        //证券名称
			HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE,      //类型代码
			HsMessageContants.H5SDK_TAG_INDUSTRY_CODE,      //行情代码
			HsMessageContants.H5SDK_TAG_MONEY_TYPE,	     //货币代码
			HsMessageContants.H5SDK_TAG_DATA_TIMESTAMP,     //时间戳
			HsMessageContants.H5SDK_TAG_TRADE_MINS,          //交易分钟数
			HsMessageContants.H5SDK_TAG_TRADE_STATUS,     //交易状态
			HsMessageContants.H5SDK_TAG_PRECLOSE_PX,      //昨收价
			HsMessageContants.H5SDK_TAG_OPEN_PX,          //今开盘
			HsMessageContants.H5SDK_TAG_LAST_PX,          //最新成交价
			HsMessageContants.H5SDK_TAG_HIGH_PX,          //最高价
			HsMessageContants.H5SDK_TAG_LOW_PX,           //最低价
			HsMessageContants.H5SDK_TAG_CLOSE_PX,         //今日收盘
			HsMessageContants.H5SDK_TAG_AVG_PX,           //均价
			HsMessageContants.H5SDK_TAG_WAVG_PX,           //加权均价
			HsMessageContants.H5SDK_TAG_BID_GRP,           //委买档位  --sequence
			HsMessageContants.H5SDK_TAG_OFFER_GRP,         //委卖档位 --sequence
			HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT,           //总成交量
			HsMessageContants.H5SDK_TAG_BUSINESS_BALANCE,      //总成交额
//			"uppx",          //涨停价格
//			"downpx",        //跌停价格
			HsMessageContants.H5SDK_TAG_CURRENT_AMOUNT,       //最近成交量(现手)
			HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT_IN,         //内盘成交量
			HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT_OUT,        //外盘成交量
			HsMessageContants.H5SDK_TAG_PX_CHANGE,           //涨跌额
			HsMessageContants.H5SDK_TAG_PX_CHANGE_RATE,       //涨跌幅
			HsMessageContants.H5SDK_TAG_TOTAL_SHARES,  //股数
			HsMessageContants.HSSDK_TAG_CIRCULATION_AMOUNT,  //股数
			HsMessageContants.H5SDK_TAG_PE_RATE        ,    //当前交易节成交量
			HsMessageContants.HSSDK_TAG_EPS,//每股收益
			HsMessageContants.H5SDK_TAG_TURNOVER_RATIO,//换手率
			HsMessageContants.HSSDK_TAG_MARKET_VALUE,//市值
			HsMessageContants.HSSDK_TAG_CIRCULATION_VALUE,//流通市值
			HsMessageContants.HSSDK_TAG_SHARES_PER_HAND //每手股数
	};
	
	public static final int[] US_STOCK_REALTIME_FIELDS = new int[]{
				HsMessageContants.H5SDK_TAG_PROD_CODE,            //证券代码 
				HsMessageContants.H5SDK_TAG_PROD_NAME,        //证券名称
				HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE,      //类型代码
//				HsMessageContants.H5SDK_TAG_INDUSTRY_CODE,      //行情代码
				HsMessageContants.H5SDK_TAG_MONEY_TYPE,	     //货币代码
				HsMessageContants.H5SDK_TAG_DATA_TIMESTAMP,     //时间戳
				HsMessageContants.H5SDK_TAG_TRADE_MINS,          //交易分钟数
				HsMessageContants.H5SDK_TAG_TRADE_STATUS,     //交易状态
				HsMessageContants.H5SDK_TAG_PRECLOSE_PX,      //昨收价
				HsMessageContants.H5SDK_TAG_OPEN_PX,          //今开盘
				HsMessageContants.H5SDK_TAG_LAST_PX,          //最新成交价
				HsMessageContants.H5SDK_TAG_HIGH_PX,          //最高价
				HsMessageContants.H5SDK_TAG_LOW_PX,           //最低价
				HsMessageContants.H5SDK_TAG_CLOSE_PX,         //今日收盘
				HsMessageContants.H5SDK_TAG_WAVG_PX,          //加权平均价
				HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT,           //总成交量
				HsMessageContants.H5SDK_TAG_BUSINESS_BALANCE,      //总成交额
				HsMessageContants.H5SDK_TAG_WEEK52_LOW_PX,       //52周最低价
				HsMessageContants.H5SDK_TAG_WEEK52_HIGH_PX,      //52周最高价
				HsMessageContants.H5SDK_TAG_PX_CHANGE,           //涨跌额
				HsMessageContants.H5SDK_TAG_PX_CHANGE_RATE,       //涨跌幅
				HsMessageContants.H5SDK_TAG_POPC_PX,          //盘前盘后价
//				"section",       //当前交易阶段:0:闭市(没有延迟数据) 1:盘前 2:盘中 3:盘后 4:实时盘后(没有延迟数据)
//				"svol",          //当前交易节成交量
				HsMessageContants.H5SDK_TAG_TOTAL_SHARES,  //股数
				HsMessageContants.HSSDK_TAG_CIRCULATION_AMOUNT,  //股数
				HsMessageContants.H5SDK_TAG_PE_RATE        ,    //当前交易节成交量
				HsMessageContants.HSSDK_TAG_EPS,//每股收益
				HsMessageContants.H5SDK_TAG_TURNOVER_RATIO,//换手率
				HsMessageContants.HSSDK_TAG_MARKET_VALUE,//市值
				HsMessageContants.HSSDK_TAG_CIRCULATION_VALUE,//流通市值
//				"popc_time",//盘前盘后时间
				HsMessageContants.HSSDK_TAG_SHARES_PER_HAND //每手股数
		};

	public static final int[] INDEX_STOCK_REALTIME_FIELDS = new int[]{
			HsMessageContants.H5SDK_TAG_PROD_CODE,            //证券代码 
			HsMessageContants.H5SDK_TAG_PROD_NAME,        //证券名称
			HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE,      //类型代码
//			HsMessageContants.H5SDK_TAG_INDUSTRY_CODE,      //行情代码
			HsMessageContants.H5SDK_TAG_MONEY_TYPE,	     //货币代码
			HsMessageContants.H5SDK_TAG_DATA_TIMESTAMP,     //时间戳
			HsMessageContants.H5SDK_TAG_TRADE_MINS,          //交易分钟数
			HsMessageContants.H5SDK_TAG_TRADE_STATUS,     //交易状态
			HsMessageContants.H5SDK_TAG_PRECLOSE_PX,      //昨收价
			HsMessageContants.H5SDK_TAG_OPEN_PX,          //今开盘
			HsMessageContants.H5SDK_TAG_LAST_PX,          //最新成交价
			HsMessageContants.H5SDK_TAG_HIGH_PX,          //最高价
			HsMessageContants.H5SDK_TAG_LOW_PX,           //最低价
			HsMessageContants.H5SDK_TAG_CLOSE_PX,         //今日收盘
			HsMessageContants.H5SDK_TAG_AVG_PX,           //均价
			HsMessageContants.H5SDK_TAG_WAVG_PX,           //加权均价
			HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT,           //总成交量
			HsMessageContants.H5SDK_TAG_BUSINESS_BALANCE,      //总成交额
			HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT_IN,         //内盘成交量
			HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT_OUT,        //外盘成交量
			HsMessageContants.H5SDK_TAG_PX_CHANGE,           //涨跌额
			HsMessageContants.H5SDK_TAG_PX_CHANGE_RATE,       //涨跌幅
			HsMessageContants.HSSDK_TAG_RISE_COUNT,//上涨家数
			HsMessageContants.HSSDK_TAG_FALL_COUNT,//下跌家数
			HsMessageContants.HSSDK_TAG_MEMBER_COUNT,//成员个数
			HsMessageContants.H5SDK_TAG_RISE_FIRST_GRP,//成员个数
			HsMessageContants.H5SDK_TAG_FALL_FIRST_GRP,//成员个数
			HsMessageContants.HSSDK_TAG_SHARES_PER_HAND //每手股数
	};
	
	public static final int[] REALTIME_FIELDS= new int[]{
			HsMessageContants.H5SDK_TAG_PROD_CODE,            //证券代码 
			HsMessageContants.H5SDK_TAG_PROD_NAME,        //证券名称
			HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE,      //类型代码
//			HsMessageContants.H5SDK_TAG_INDUSTRY_CODE,      //行情代码
			HsMessageContants.H5SDK_TAG_MONEY_TYPE,	     //货币代码
			HsMessageContants.H5SDK_TAG_DATA_TIMESTAMP,     //时间戳
			HsMessageContants.H5SDK_TAG_TRADE_MINS,          //交易分钟数
			HsMessageContants.H5SDK_TAG_TRADE_STATUS,     //交易状态
			HsMessageContants.H5SDK_TAG_PRECLOSE_PX,      //昨收价
			HsMessageContants.H5SDK_TAG_OPEN_PX,          //今开盘
			HsMessageContants.H5SDK_TAG_LAST_PX,          //最新成交价
			HsMessageContants.H5SDK_TAG_HIGH_PX,          //最高价
			HsMessageContants.H5SDK_TAG_LOW_PX,           //最低价
			HsMessageContants.H5SDK_TAG_CLOSE_PX,         //今日收盘
			HsMessageContants.H5SDK_TAG_AVG_PX,           //均价
			HsMessageContants.H5SDK_TAG_WAVG_PX,          //加权平均价
			HsMessageContants.H5SDK_TAG_BID_GRP,           //委买档位  --sequence
			HsMessageContants.H5SDK_TAG_OFFER_GRP,         //委卖档位 --sequence
			HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT,           //总成交量
			HsMessageContants.H5SDK_TAG_BUSINESS_BALANCE,      //总成交额
//			"uppx",          //涨停价格
//			"downpx",        //跌停价格
			HsMessageContants.H5SDK_TAG_CURRENT_AMOUNT,       //最近成交量(现手)
			HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT_IN,         //内盘成交量
			HsMessageContants.H5SDK_TAG_BUSINESS_AMOUNT_OUT,        //外盘成交量
//			"total_bidqty",  //总委买量
//			"total_offerqty",//总委卖量
//			"wavg_bidpx",    //加权平均委买价格
//			"wavg_offerpx",  //加权平均委卖价格
			HsMessageContants.H5SDK_TAG_WEEK52_LOW_PX,       //52周最低价
			HsMessageContants.H5SDK_TAG_WEEK52_HIGH_PX,      //52周最高价
			HsMessageContants.H5SDK_TAG_PX_CHANGE,           //涨跌额
			HsMessageContants.H5SDK_TAG_PX_CHANGE_RATE,       //涨跌幅
			HsMessageContants.H5SDK_TAG_POPC_PX,          //盘前盘后价
//			"section",       //当前交易阶段:0:闭市(没有延迟数据) 1:盘前 2:盘中 3:盘后 4:实时盘后(没有延迟数据)
//			"svol",          //当前交易节成交量
			HsMessageContants.H5SDK_TAG_TOTAL_SHARES,  //股数
			HsMessageContants.HSSDK_TAG_CIRCULATION_AMOUNT,  //股数
			HsMessageContants.H5SDK_TAG_PE_RATE        ,    //当前交易节成交量
			HsMessageContants.HSSDK_TAG_EPS,//每股收益
			HsMessageContants.H5SDK_TAG_TURNOVER_RATIO,//换手率
			HsMessageContants.HSSDK_TAG_MARKET_VALUE,//市值
			HsMessageContants.HSSDK_TAG_CIRCULATION_VALUE,//流通市值
//			"popc_time",//盘前盘后时间
			HsMessageContants.HSSDK_TAG_RISE_COUNT,//上涨家数
			HsMessageContants.HSSDK_TAG_FALL_COUNT,//下跌家数
			HsMessageContants.HSSDK_TAG_MEMBER_COUNT,//成员个数
			HsMessageContants.HSSDK_TAG_SHARES_PER_HAND //每手股数
	};
	
	private Context mContext;
	/**
	 * 初始化Session
	 */
	public void initSession(Context ctx,int logintype){
		mContext = ctx;
		mLoginType = logintype;
		try {
			sSession = HsSessionManager.createSession(SERVER_HOST+"_"+SERVER_PORT);
			IH5SessionSettings settings = sSession.getSessionSettings();
			NetworkAddr address = new NetworkAddr();
			address.setServerIP(SERVER_HOST);
			address.setServerPort(SERVER_PORT);
			settings.setNetworkAddr(address);
			settings.setTemplatePath(ctx.getFilesDir().getAbsolutePath() + "/");
			settings.setQueueSize(100);
			settings.setClientType(HsH5Session.CLIENT_TYPE_MOBILE);
			settings.setOSType("Android 5.1.0");
			settings.setAppKey(APP_KEY);
			settings.setAppSecret(APP_SECRET);
		} catch (HsSessionException e) {
			e.printStackTrace();
		}

		sSession.initiate(sSessionEvent);

		System.out.println("server is "+SERVER_HOST);
	}

	IOnSessionEvent sSessionEvent = new IOnSessionEvent() {
		@Override
		public void onEvent(SessionEvents event, String eventInfo, IH5Session session) {
			switch (event) {
				case LOGIN_SUCCESS:
					break;
				case TEMPLATE_SYNC_SUCCESS:
					if(mLoginType == 0){
						logcat("正在以用户名登录服务器=====>");
						sSession.loginByUser(USERNAME , PASSWORD, new IUserOperationCallback() {
							@Override
							public void onResponse(HashMap<String, String> userInfo, IH5Session session) {
								//System.out.println("get userinfo="+userInfo);
								AUTHID = userInfo.get("auth_id");
								TOKEN = userInfo.get("token");
								setLoginAuthidToken();
								logcat("获得userinfo::"+userInfo.toString());
								loadMarketInfo();
								logcat("<=====以用户名登录成功");
							}
						});
					}else if(mLoginType == 1){
						logcat("正在以authid服务器=====>");
						sSession.loginByAuthId(AUTHID, TOKEN,new IUserOperationCallback() {
							@Override
							public void onResponse(HashMap<String, String> userInfo, IH5Session session) {
								//System.out.println("get userinfo="+userInfo);
								logcat("获得userinfo::"+userInfo.toString());
								loadMarketInfo();
								logcat("<=====以authid登录成功");
							}
						});
					}
					break;
				default:
					break;
			}
		}
	};

	void loadMarketInfo(){
		
		try {
			HsCommMessage marketInfoMsg = sSession.createMessage(HsMessageContants.BIZ_H5PROTO, HsMessageContants.H5SDK_MSG_MARKET_TYPES, HsMessageContants.REQUEST);	
			//设置消息中带有重复数组参数
			HsCommSequenceItem marketInfoMsgSeqItem = marketInfoMsg.getBodyRecord().newSequenceField(HsMessageContants.H5SDK_FINANCE_MIC_GRP);
			//美股
			marketInfoMsgSeqItem.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_FINANCE_MIC, "XNAS".getBytes());
			marketInfoMsgSeqItem.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_FINANCE_MIC, "XASE".getBytes());
			marketInfoMsgSeqItem.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_FINANCE_MIC, "XNYS".getBytes());
			marketInfoMsgSeqItem.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_FINANCE_MIC, "XSHG".getBytes());
			marketInfoMsgSeqItem.newRecord().setFieldValue(HsMessageContants.H5SDK_TAG_FINANCE_MIC, "XSHE".getBytes());
			sSession.sendMessage(marketInfoMsg, new INetworkResponse() {
				@Override
				public void handleMessage(IH5Session.MessageErrors result, HsCommMessage msg) {
					parseMarketInfo(msg , true);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseMarketInfo(HsCommMessage msg , boolean needSave) {
		HsCommRecord marketInfoRecords = msg.getBodyRecord();
		HsFieldItem marketInfoItems = marketInfoRecords.getItemByFieldId(HsMessageContants.H5SDK_FINANCE_MIC_GRP );
		if (marketInfoItems != HsNoneItem.NoneItem ) {
			HsCommSequenceItem marketInfoSeq = (HsCommSequenceItem) marketInfoItems;
			int marketInfoSize = marketInfoSeq.getRecordCount();
			ArrayList<Market> marketList = new ArrayList<Market>();
			for(int m = 0;m < marketInfoSize; m++){
				HsCommRecord marketInfoRecord = marketInfoSeq.getRecord(m);
				Market market = new Market();
				marketList.add(market);
				market.setMarketCode(marketInfoRecord.getStringValue(HsMessageContants.H5SDK_TAG_FINANCE_MIC));
				market.setMarketName(marketInfoRecord.getStringValue(HsMessageContants.H5SDK_TAG_FINANCE_NAME));
				market.setMarketDate(marketInfoRecord.getIntValue(HsMessageContants.H5SDK_TAG_MARKET_DATE));
				market.setTradeDate(marketInfoRecord.getIntValue(HsMessageContants.H5SDK_TAG_INIT_DATE));
				market.setSummerTimeFlag(marketInfoRecord.getIntValue(HsMessageContants.H5SDK_TAG_DST_FLAG));
				market.setTimezone(marketInfoRecord.getIntValue(HsMessageContants.H5SDK_TAG_TIMEZONE));
				market.setPricePrecision(marketInfoRecord.getIntValue(HsMessageContants.HSSDK_TAG_PX_PRECISION));
				market.setTimeZoneCode(marketInfoRecord.getStringValue(HsMessageContants.H5SDK_TAG_TIMEZONE_CODE));
				
				QWQuoteBase.sMarketItem.put( market.getMarketCode(), market);
				HsFieldItem marketInfoTypeItem = marketInfoRecord.getItemByFieldId(HsMessageContants.H5SDK_TAG_TYPE_GRP );
				if(marketInfoTypeItem != HsNoneItem.NoneItem){
					HsCommSequenceItem marketInfoTypeSeq = (HsCommSequenceItem) marketInfoTypeItem;
					int marketTypeSize = marketInfoTypeSeq.getRecordCount();
					ArrayList<Market.TypeItem> marketTypes = new ArrayList<Market.TypeItem>();
					market.setTypeItems(marketTypes);
					for (int i = 0; i < marketTypeSize; i++) {
						HsCommRecord record = marketInfoTypeSeq.getRecord(i);
						String typeCode = market.getMarketCode()+"."+ record.getStringValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_CODE);
						Market.TypeItem marketTypeItem = new Market.TypeItem();
						marketTypeItem.setMarketTypeCode( typeCode );
						marketTypeItem.setMarketTypeName(record.getStringValue(HsMessageContants.H5SDK_TAG_HQ_TYPE_NAME));
						marketTypeItem.setPriceUnit( record.getIntValue(HsMessageContants.H5SDK_TAG_PRICE_SCALE));
						marketTypeItem.setPricePrecision( record.getIntValue(HsMessageContants.HSSDK_TAG_PX_PRECISION));
						marketTypeItem.setTradeDate(record.getIntValue(HsMessageContants.H5SDK_TAG_INIT_DATE));
						
						HsCommSequenceItem times = (HsCommSequenceItem)record.getItemByFieldId(HsMessageContants.H5SDK_TAG_TRADE_SECTION_GRP);
						ArrayList<TradeTime> tradeTimes = new  ArrayList<TradeTime>();
						for (int j = 0; j < times.getRecordCount(); j++) {
							HsCommRecord timeRecord = times.getRecord(j);
							tradeTimes.add( new TradeTime( timeRecord.getIntValue(HsMessageContants.H5SDK_TAG_OPEN_TIME), timeRecord.getIntValue(HsMessageContants.H5SDK_TAG_CLOSE_TIME)));
						}
						
						if (0 == marketTypeItem.getPriceUnit()) {
							System.out.println("Price Unit is Zero!!!!!");
						}
						
						typeCode = typeCode.toUpperCase(Locale.getDefault());
						Log.d("marketInfo", String.format(" %s \tNAME: %s \t unit:%d \t Precision:%d \t date:%d",typeCode , marketTypeItem.getMarketTypeName() , marketTypeItem.getPriceUnit() , marketTypeItem.getPricePrecision(),marketTypeItem.getTradeDate() ));
						marketTypeItem.setTradeTimes(tradeTimes);

						marketTypes.add(marketTypeItem);
						QWQuoteBase.sMarketTypeMap.put( typeCode, marketTypeItem);
					}
					
				}
				
			}
			int persision = QWQuoteBase.getPriceUnit(new Stock("600570","XSHG.esa.m") );
			if (QWQuoteBase.sMarketTypeMap.size() == 0 || persision != 1000 ) {
				Log.e( TAG, String.format(" marketInfo load fail! XSHG.esa price unit is %d", persision));
			}else{
				if (mContext instanceof Activity) {
					Activity act = (Activity) mContext;
					act.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText( mContext, "Session初始化成功！", Toast.LENGTH_SHORT).show();
						}
					});
				}
				
			}
			
			System.out.println("sMarketType Size :"+QWQuoteBase.sMarketTypeMap.size());
		}
	}

	public void setSERVER_HOST(String arge) {
		if(arge==null||arge.equals(""))return;
		SERVER_HOST = arge;
		logcat("set host:"+arge);
	}

	public void setSERVER_PORT(String arge) {
		if(arge==null||arge.equals(""))return;
		SERVER_PORT = Integer.valueOf(arge);
		logcat("set port:"+arge);
	}

	public void setUSERNAME(String arge) {
		if(arge==null||arge.equals(""))return;
		USERNAME = arge;
		logcat("set username:"+arge);
	}

	public void setPASSWORD(String arge) {
		if(arge==null||arge.equals(""))return;
		PASSWORD = arge;
		logcat("set password:"+arge);
	}

	public void setAPP_SECRET(String arge) {
		if(arge==null||arge.equals(""))return;
		APP_SECRET = arge;
		logcat("set secret:"+arge);
	}

	public void setAPP_KEY(String arge) {
		if(arge==null||arge.equals(""))return;
		APP_KEY = arge;
		logcat("set appkey:"+arge);
	}

	public void setTOKEN(String arge) {
		if(arge==null||arge.equals(""))return;
		TOKEN = arge;
		logcat("set token:"+arge);
	}

	public void setAUTHID(String arge) {
		if(arge==null||arge.equals(""))return;
		AUTHID = arge;
		logcat("set authid:"+arge);
	}

	public void logcat(String str){
		if(mHandler==null)return;
		Message msg = mHandler.obtainMessage();
		msg.what = 6002;
		Bundle bundle = new Bundle();
		bundle.putString("log", str);
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	public void setLoginAuthidToken(){
		if(mHandler==null)return;
		Message msg = mHandler.obtainMessage();
		msg.what = 126;
		Bundle bundle = new Bundle();
		bundle.putString("authid", AUTHID);
		bundle.putString("token", TOKEN);
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	public void setHandler(Handler handler){
		mHandler = handler;
	}
}
