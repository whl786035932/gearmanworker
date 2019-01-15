package cn.videoworks.gearman.common;

import java.util.HashMap;
import java.util.Map;

public class ParameterMap {

	private static ParameterMap parameterMap = null;
	public static Map<String,String> params;
	
	private  ParameterMap(String file) {
		if(null == params) {
			params = new HashMap<String,String>();
			params.put("file", file);
		}
	}
	
	public static synchronized ParameterMap getParameterMap(String file) {
		if(null == parameterMap) {
			parameterMap = new ParameterMap(file);
		}
		return parameterMap;
	}
	
}
