/**
 * ResponseDictionary.java
 * cn.videoworks.edge.constant
 *
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2017年6月8日 		meishen
 *
 * Copyright (c) 2017, TNT All Rights Reserved.
*/

package cn.videoworks.gearman.constant;
/**
 * ClassName:ResponseDictionary
 *
 * @author   meishen
 * @version  
 * @since    Ver 1.0.0
 * @Date	 2017年6月8日		上午11:23:12
 *
 */
public class ResponseDictionary {
	
	/**
	 * 操作成功
	 */
	public static final int SUCCESS = 100000;
	
	/**
	 * 服务端异常
	 */
	public static final int SERVEREXCEPTION = 100001;
	
	/**
	 * 连接超时
	 */
	public static final int TIMEOUT = 100002;
	
	/**
	 * 拒绝请求
	 */
	public static final int REFUSED = 100003;
	
	/**
	 * 请求失败
	 */
	public static final int ERROR = 100004;
	
	/**
	 * 参数验证异常
	 */
	public static final int UNAUTHORIZED = 100005;
	
	/**
	 * 外部接口调用异常
	 */
	public static final int EXTERNALINTERFACECALLSEXCEPTION = 100006;
	
	/**
	 * 外部接口请求异常
	 */
	public static final int EXTERNALINTERFACEREQUESTSEXCEPTION = 100007;
	
	/**
	 * 下载异常
	 */
	public static final int OWNLOADEXCEPTION = 100008;
	 	
}

