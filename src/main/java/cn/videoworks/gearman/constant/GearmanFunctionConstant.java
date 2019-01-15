package cn.videoworks.gearman.constant;

import java.util.ArrayList;
import java.util.List;

public class GearmanFunctionConstant {
	
	/**
	 * work function 根据function启动对应的work
	 */
	public final static String WRITE_STORAGE = "WRITE_STORAGE"; //存储work，对应AWSS3，BAIDUBOS,GZAWSS3三种
	
	/**
	 * 移除对象存储文件
	 */
	public final static String REMOVE_STORAGE = "REMOVE_STORAGE";
	/**
	 *  WRITE_STORAGE 存储启动关键字，确认是哪种存储
	 */
	public final static String STORAGE_AWSS3 = "STORAGE_AWSS3";//公司本地存储
	public final static String STORAGE_BAIDUBOS = "STORAGE_BAIDUBOS";//百度云bos
	public final static String STORAGE_GZAWSS3 = "STORAGE_GZAWSS3"; //贵州s3
	public final static String STORAGE_MINIOAWSS3 = "STORAGE_MINIOAWSS3"; //贵州轻量级minio储存
	public final static String STORAGE_TENCENTCOS = "STORAGE_TENCENTCOS"; //腾讯云cos
	
	/**
	 * work functtion 根据function启动对应的cdn  work
	 */
	public final static String WRITE_CDN = "WRITE_CDN";
	/**
	 * WRITE_CDN 启动对应的cdn
	 */
	public final static String CDN_BAIDUBOS = "CDN_BAIDUBOS";//百度cdn  此处没使用
	public final static String CDN_GZ = "CDN_GZ";//贵州cdn
	public final static String CDN_TENCENTCOS = "CDN_TENCENTCOS"; //腾讯cos
	public final static String CDN_SX = "CDN_SX"; //陕西的cdn
	
	/**
	 * 陕西的监控ftp返回的xml文件
	 */
	public final static String SXMONITOR_XML ="SXMONITOR_XML";

	public static List<String> GEARMANFUNCTIONS = null;  //work列表
	public static List<String> STORAGECLIENTS = null;  //存储客户端列表
	public static List<String> CDNCLIENTS = null;  //CDN客户端列表
	
	static {
		GEARMANFUNCTIONS = new ArrayList<String>();
		GEARMANFUNCTIONS.add(WRITE_STORAGE);
		GEARMANFUNCTIONS.add(WRITE_CDN);
		GEARMANFUNCTIONS.add(REMOVE_STORAGE);
		
		STORAGECLIENTS = new ArrayList<String>();
		STORAGECLIENTS.add(STORAGE_AWSS3);
		STORAGECLIENTS.add(STORAGE_BAIDUBOS);
		STORAGECLIENTS.add(STORAGE_GZAWSS3);
		STORAGECLIENTS.add(STORAGE_MINIOAWSS3);
		STORAGECLIENTS.add(STORAGE_TENCENTCOS);
		
		CDNCLIENTS = new ArrayList<String>();
		CDNCLIENTS.add(CDN_BAIDUBOS);
		CDNCLIENTS.add(CDN_GZ);
		CDNCLIENTS.add(CDN_TENCENTCOS);
		CDNCLIENTS.add(CDN_SX);
		CDNCLIENTS.add(SXMONITOR_XML);
	}
}
