package com.hundsun.gmudemo;

import android.os.Handler;

import com.hundsun.quotewidget.item.Stock;
import com.hundsun.quotewidget.utils.QWQuoteBase;


/**
 * 数据订阅中心接口滴定仪
 * @author huangcheng,LiangHao
 *
 */
public interface IDataCenter {
	/**
	 * 键盘精灵 :支持美股，兼容DTK
	 * @param stock
	 * @param type 0:代表沪深;1代表美股
	 * @param handler
	 */
	public void queryStocks( String stock  , int type,Handler handler , Object userInfo);
	/**
	 * 请求分时数据 
	 * @param stock
	 * @param handler
	 */
	public void loadTrends( Stock stock, Handler handler, Object userInfo);
	
	/**
	 * 请求K线数据
	 * @param stock
	 * @param time
	 * @param period
	 * @param count
	 * @param handler
	 * @param userInfo
	 * @param direction
	 */
	public void loadKline( Stock stock,long date ,long time,int period, int count, Handler handler, Object userInfo , int direction);
	/**
	 * 请求K线数据
	 * @param stock
	 * @param time
	 * @param period
	 * @param count
	 * @param handler
	 */
	public void loadKline( Stock stock,long date ,long time,int period, int count, Handler handler, Object userInfo);
	/**
	 * 请求快照数据
	 * @param stock
	 * @param handler
	 */
	public void loadRealtime( Stock stock  ,Handler handler, Object userInfo);
	/**
	 * 请求快照数据列表
	 * @param stock 股票列表
	 * @param handler
	 */
	public void loadListRealtime( Stock[] stock  ,Handler handler, Object userInfo);
	/**
	 * @param market
	 * @param handler
	 */
	public void loadMarketData( String market , Handler handler , Object userInfo);
	/**
	 * 请求成交明细
	 * @param stock
	 * @param begin
	 * @param count
	 * @param handler
	 */
	public void loadStockTick( Stock stock , int begin , int count , Handler handler, Object userInfo);
	/**
	 * 后取一级板块
	 * @param handler
	 */
	public void loadBlockData(Handler handler, Object userInfo);
	/**
	 * 获取板块下的子板块
	 * @param blockName
	 * @param handler
	 */
	public void loadSubBlockData(String blockName, Handler handler, Object userInfo);
	/**
	 * 获所属取板块下股票列表
	 * @param blockName
	 * @param begin
	 * @param count
	 * @param handler
	 */
	public void loadBlockStocksData(String blockName,int begin,int count, Handler handler, Object userInfo);
	
	/**
	 * 请求排名数据
	 * @param marketType 市场类型【枚举】
	 * @param begin      开始标记
	 * @param count      最大请求条数
	 * @param sortType 排序  【枚举类型】
	 * @param orderType  升降序
	 * @param handler
	 */
	public void loadRankingStocksData(String[] marketType,int begin,int count,QWQuoteBase.SORT sortType,int orderType,Handler handler , Object userInfo);
	void loadRankingStocksData(Stock[] stocks, QWQuoteBase.SORT sortType, int orderTpye, Handler handler, Object userInfo);
	
	// 主推接口
}
