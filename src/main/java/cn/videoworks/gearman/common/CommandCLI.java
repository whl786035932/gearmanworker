package cn.videoworks.gearman.common;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.videoworks.commons.util.json.JsonConverter;
import cn.videoworks.gearman.constant.GearmanFunctionConstant;

/**
 * ClassName:CommandCLI
 * Function: TODO ADD FUNCTION
 * Reason:	 TODO ADD REASON
 * apache CommandCLI工具类
 * @author   meishen
 * @version  
 * @since    Ver 1.1
 * @Date	 2018	2018年6月24日		下午3:05:55
 *
 * @see 	 
 */
public class CommandCLI {
	
	private static Logger logger = LoggerFactory.getLogger(CommandCLI.class);

	/**
	 * buildOptions:(定义启动参数)
	 *
	 * @author   meishen
	 * @Date	 2018	2018年6月24日		下午3:06:20
	 * @return   
	 * @return Options    
	 * @throws 
	 * @since  Videoworks　Ver 1.1
	 */
	private static Options buildOptions() {
		Options options = new Options();
		Option config = new Option("config", true, "配置文件路径");
		Option work = new Option("work", true, "启动work参数");
		Option storageClient = new Option("sc", true, "存储对应的client(哪种存储)");
		Option cdnClient = new Option("cc", true, "CDN对应的client(哪种cdn)");
		options.addOption(config);
		options.addOption(work);
		options.addOption(storageClient);
		options.addOption(cdnClient);
		return options;
	}
	
	/**
	 * getCommandLine:(获取commandLine命令行)
	 *
	 * @author   meishen
	 * @Date	 2018	2018年6月24日		下午3:06:36
	 * @param args
	 * @return   
	 * @return CommandLine    
	 * @throws 
	 * @since  Videoworks　Ver 1.1
	 */
	public static CommandLine getCommandLine(String[] args) {
		Options ops = buildOptions();
		CommandLine comm = null;
		try {
		    comm = new DefaultParser().parse(ops, args);
		} catch (ParseException e) {
		    e.printStackTrace();
		    logger.error("解析参数失败，请输入正确的指令，指令包含：[-config,-work,-sc,-cc]");
		}
		return comm;
	}
	
	/**
	 * valid:(验证命令行参数)
	 *
	 * @author   meishen
	 * @Date	 2018	2018年6月24日		下午3:06:53
	 * @param args
	 * @return   
	 * @return boolean    
	 * @throws 
	 * @since  Videoworks　Ver 1.1
	 */
	public static boolean valid(String[] args) {
		CommandLine comm =  getCommandLine(args);
		if (comm.getOptions().length == 0) {
			logger.info("请指定参数...");
			return false;
		}
		
		String workName = null;
		if (comm.hasOption("work")) {
			workName = comm.getOptionValue("work");
        }else{
        	logger.error("请通过-work选项输入work名称");
        	return false;
        }
        if (StringUtils.isBlank(workName)) {
        	logger.error("请输入需要启动的work名称");
        	return false;
        }
        if(!GearmanFunctionConstant.GEARMANFUNCTIONS.contains(workName)) {
        	logger.error("work不存在，目前支持的work："+JsonConverter.format(GearmanFunctionConstant.GEARMANFUNCTIONS));
        	return false;
        }
		
		String configFilePath = null;
		if (comm.hasOption("config")) {
            configFilePath = comm.getOptionValue("config");
            try {
				File file = new File(configFilePath);
				if(!file.exists()) {
					logger.error("配置文件【"+configFilePath+"】，不存在！");
					return false;
				}
			} catch (Exception e) {
				logger.error("无法读取配置文件【"+configFilePath+"】，请确保路径是否正确");
				return false;
			}
        }else{
        	logger.error("请通过-config选项输入配置文件路径");
        	return false;
        }
        if (StringUtils.isBlank(configFilePath)) {
        	logger.error("请输入配置文件路径");
        	return false;
        }
        
        /**
         * 只有启动存储work时，适配存储client
         */
        if(workName.equals(GearmanFunctionConstant.WRITE_STORAGE)) {
			String storageClient = null;
			if (comm.hasOption("sc")) {
				storageClient = comm.getOptionValue("sc");
	        }else{
	        	logger.error("请通过-sc选项输入启动哪种存储");
	        	return false;
	        }
	        if (StringUtils.isBlank(storageClient)) {
	        	logger.error("请输入启动存储类型");
	        	return false;
	        }
	        if(!GearmanFunctionConstant.STORAGECLIENTS.contains(storageClient)) {
	        	logger.error("存储Client不存在，目前支持的存储client："+JsonConverter.format(GearmanFunctionConstant.STORAGECLIENTS));
	        	return false;
	        }
        }
        
        /**
         * 启动cdn work时，适配cdn client
         */
        if(workName.equals(GearmanFunctionConstant.WRITE_CDN)) {
			String cdnClient = null;
			if (comm.hasOption("cc")) {
				cdnClient = comm.getOptionValue("cc");
	        }else{
	        	logger.error("请通过-cc选项输入启动哪种存储");
	        	return false;
	        }
	        if (StringUtils.isBlank(cdnClient)) {
	        	logger.error("请输入启动cdn类型");
	        	return false;
	        }
	        if(!GearmanFunctionConstant.CDNCLIENTS.contains(cdnClient)) {
	        	logger.error("CDN Client不存在，目前支持的cdn client："+JsonConverter.format(GearmanFunctionConstant.CDNCLIENTS));
	        	return false;
	        }
        }
        
        /**
         * 启动cdn work时，适配cdn client
         */
        if(workName.equals(GearmanFunctionConstant.REMOVE_STORAGE)) {
			String storageClient = null;
			if (comm.hasOption("sc")) {
				storageClient = comm.getOptionValue("sc");
	        }else{
	        	logger.error("请通过-sc选项输入启动哪种存储");
	        	return false;
	        }
	        if (StringUtils.isBlank(storageClient)) {
	        	logger.error("请输入启动存储类型");
	        	return false;
	        }
	        if(!GearmanFunctionConstant.STORAGECLIENTS.contains(storageClient)) {
	        	logger.error("存储Client不存在，目前支持的存储client："+JsonConverter.format(GearmanFunctionConstant.STORAGECLIENTS));
	        	return false;
	        }
        }
		return true;
	}
}
