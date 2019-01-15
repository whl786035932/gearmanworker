package cn.videoworks.gearman;

import cn.videoworks.gearman.common.ParameterMap;
import cn.videoworks.gearman.constant.GearmanFunctionConstant;
import cn.videoworks.gearman.work.CNDWork;
import cn.videoworks.gearman.work.StorageWork;

public class DebugMain {

	/**
	 * main:(程序测试入口)
	 *
	 * @author   meishen
	 * @Date	 2018	2018年6月17日		下午6:52:27
	 * @param args   
	 * @return void    
	 * @throws 
	 * @since  Videoworks　Ver 1.1
	 */
	public static void main(String[] args) {
		ParameterMap.getParameterMap("D:\\gearman.properties"); //初始化配置文件
		
		StorageWork.registWorker(GearmanFunctionConstant.STORAGE_BAIDUBOS); 
		
		CNDWork.registWorker(GearmanFunctionConstant.CDN_SX);
		
		//TestWork.registWorker();
	}
}
