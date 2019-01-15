/**
 * DateUtil.java
 * cn.videoworks.despotui.util
 * 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2014年2月10日 		meishen
 *
 * Copyright (c) 2014, TNT All Rights Reserved.
*/

package cn.videoworks.gearman.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * ClassName:DateUtil
 * @author   meishen
 * @version  Ver 1.0.0    
 * @Date	 2014年2月10日		下午3:49:58 
 */
public class DateUtil {
	
	public static final SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat formatYMD=new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat formatHMS=new SimpleDateFormat("HH:mm:ss");

	/**
	 * getDate:(把字符串转换成date)
	 * @author   meishen
	 * @Date	 2014年2月10日		下午3:51:17
	 * @return Date    
	 * @throws
	 */
	public static Date getDate(String str){
	       Date date=null;
        try {
        		date= format.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
	}
	
	/**
	 * getDate:(把字符串转换成date)
	 * @author   meishen
	 * @Date	 2014年2月10日		下午3:51:17
	 * @return Date    
	 * @throws
	 */
	public static Date getDateYMD(String str){
	       Date date=null;
        try {
        		date= formatYMD.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
	}
	
	public static String getDateStr(String str){
	       String date="";
     try {
     		date=format.format(format.parse(str));
     } catch (Exception e) {
         e.printStackTrace();
     }
     return date;
	}
	
	public static final Timestamp getNowTimeStamp(){
		Date now=new Date();
		return Timestamp.valueOf(format.format(now));
	}
	
	public static final String getNowTime(){
		Date now=new Date();
		return format.format(now);
	}
	
	public static final String getNowTimeYMD(){
		Date now=new Date();
		return formatYMD.format(now);
	}
	
	public static final long getNowTimeYMDLong(){
		Date now=new Date();
		try {
			return formatYMD.parse(formatYMD.format(now)).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static final long getNowTimeLong(){
		Date now=new Date();
		try {
			return format.parse(format.format(now)).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static final long geTimeYMDLong(String date){
		try {
			return formatYMD.parse(date).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static final long geTimeHMSLong(String time){
		try {
			return formatHMS.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	
	public static Timestamp getTimeStamp(String time){
		Date date;
		try {
			date = format.parse(time);
			Timestamp valueOf = Timestamp.valueOf(format.format(date));
			return valueOf;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("init " + time +" error!");
		}	
		return null;
	}
	
	/**将utc的long值转为utc时间*/
	public static GregorianCalendar getUTCDate(long millionSeconds){
		long sd=millionSeconds;  
        Date dat=new Date(sd);  
        GregorianCalendar gc = new GregorianCalendar();   
        gc.setTime(dat);  
      
        return gc;
	}
	/**将utc时间转本地时间*/
	
	public static String  convertUTCtoLocal(GregorianCalendar gc){
		 String sb=format.format(gc.getTime());
		 return sb;
	}
	
	public static long localLongFromUTClLong(long millionSeconds) {
		GregorianCalendar gcDate = getUTCDate(millionSeconds);
		String localDate = convertUTCtoLocal(gcDate);
		try {
			Date parse = format.parse(localDate);
			return parse.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
		
	}
	/**
	 * 获取当前utc时间的long值
	 */
	public  static long getNowUTC(){
		Calendar calendar = Calendar.getInstance();
		int offset = calendar.get(Calendar.ZONE_OFFSET); 
		calendar.add(Calendar.MILLISECOND, -offset);
		Date date = calendar.getTime(); 
		String nowTime=formatHMS.format(date);
		System.out.println("当前时间为nowTime="+nowTime);
		long nowTimeLong = DateUtil.geTimeHMSLong(nowTime);
		return nowTimeLong;
	}

	/**
	 * 日期格式化 yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String dateTime(Date date){
		return format.format(date);

	}
	/**
	 * convert:(这里用一句话描述这个方法的作用)
	 * 
	 * @author meishen
	 * @Date 2018 2018年4月18日 下午5:24:04
	 * @param timestamp
	 * @return
	 * @return String
	 * @throws
	 * @since Videoworks　Ver 1.1
	 */
	public static String timeStampConvertStr(Timestamp timestamp) {
		return format.format(timestamp);
	}
	
	public static final String getNowTimeOnlyYMD(){
		Date now=new Date();
		return formatYMD.format(now);
	}

	
}

