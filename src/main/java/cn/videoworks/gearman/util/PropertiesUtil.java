package cn.videoworks.gearman.util;

import java.io.FileInputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.videoworks.gearman.common.ParameterMap;

/**
 * ClassName:PropertiesUtil
 * Function: TODO ADD FUNCTION
 * Reason:	 TODO ADD REASON
 *  读取配置文件
 * @author   meishen
 * @version  
 * @since    Ver 1.1
 * @Date	 2018	2018年6月17日		上午10:45:51
 *
 * @see 	 
 */
public class PropertiesUtil {

    private static Properties properties = null;
    
    private static PropertiesUtil propertiesUtil = null;
    
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
    
    /**
     * 单例模式读取文件
     * Creates a new instance of PropertiesUtil.
     *
     */
    private PropertiesUtil(){
    	try {
            properties = new Properties();
            String file = ParameterMap.params.get("file");
//            properties.load(PropertiesUtil.class.getResourceAsStream("/gearman.properties"));
            properties.load(new FileInputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("读取配置文件【gearman.properties】失败");
        }
    }
    
    
    /**
     * getPropertiesUtil:(单例)
     *
     * @author   meishen
     * @Date	 2018	2018年6月17日		上午10:54:54
     * @return   
     * @return PropertiesUtil    
     * @throws 
     * @since  Videoworks　Ver 1.1
     */
    public static synchronized PropertiesUtil getPropertiesUtil() {
    	if(propertiesUtil == null) {
    		propertiesUtil = new PropertiesUtil();
    	}
    	return propertiesUtil;
    }

    /**
     * get:(通过key获取值)
     *
     * @author   meishen
     * @Date	 2018	2018年6月17日		上午10:48:38
     * @param key
     * @return
     * @throws Exception   
     * @return String    
     * @throws 
     * @since  Videoworks　Ver 1.1
     */
    public static String get(String key) throws Exception {
        String value = "";
        if (properties.containsKey(key)) {
            value = String.valueOf(properties.get(key));
        } else {
            throw new Exception("配置文件参数[" + key + "]异常");
        }
        return value;
    }
   
    /**
     * get:(获取key值，否则默认)
     *
     * @author   meishen
     * @Date	 2018	2018年6月17日		上午10:55:44
     * @param key
     * @param defaultValue
     * @return
     * @throws Exception   
     * @return String    
     * @throws 
     * @since  Videoworks　Ver 1.1
     */
    public static String get(String key,String defaultValue) throws Exception {
        String value = "";
        if (properties.containsKey(key)) {
            value = String.valueOf(properties.get(key));
        } else {
            return defaultValue;
        }
        return value;
    }

}
