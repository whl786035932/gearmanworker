package cn.videoworks.gearman;

import org.apache.commons.cli.CommandLine;

import cn.videoworks.gearman.common.CommandCLI;
import cn.videoworks.gearman.common.ParameterMap;
import cn.videoworks.gearman.constant.GearmanFunctionConstant;
import cn.videoworks.gearman.work.CNDWork;
import cn.videoworks.gearman.work.RemoveStorageWork;
import cn.videoworks.gearman.work.SXMonitorXMLWorker;
import cn.videoworks.gearman.work.StorageWork;

/**
 * ClassName:Main Function: TODO ADD FUNCTION Reason: TODO ADD REASON gearman主程序
 * 
 * @author meishen
 * @version
 * @since Ver 1.1
 * @Date 2018 2018年6月17日 下午6:51:44
 * 
 * @see
 */
public class Main {

	/**
	 * main:(程序主入口)
	 * 
	 * @author meishen
	 * @Date 2018 2018年6月17日 下午6:52:27
	 * @param args
	 * @return void
	 * @throws
	 * @since Videoworks　Ver 1.1
	 */
	public static void main(String[] args) {
		if (CommandCLI.valid(args)) {
			CommandLine comm = CommandCLI.getCommandLine(args);
			String workName = comm.getOptionValue("work");// work名称
			String configFilePath = comm.getOptionValue("config"); // 获取配置文件地址
			String storageClient = "";
			String cdnClient = "";
			if (comm.hasOption("sc")) {
				storageClient = comm.getOptionValue("sc"); // storate client  sc
			}
			if (comm.hasOption("cc")) {
				cdnClient = comm.getOptionValue("cc"); // cdn client cc
			}
			ParameterMap.getParameterMap(configFilePath); // 初始化配置文件

			switch (workName) {
				case GearmanFunctionConstant.WRITE_CDN:
					CNDWork.registWorker(cdnClient);
					break;
				case GearmanFunctionConstant.WRITE_STORAGE:
					StorageWork.registWorker(storageClient);
					break;
				case GearmanFunctionConstant.REMOVE_STORAGE:
					RemoveStorageWork.registWorker(storageClient);
					break;
				case GearmanFunctionConstant.SXMONITOR_XML:
					SXMonitorXMLWorker.registWorker();
					break;
				default:
					break;
			}
		}
	}
}
