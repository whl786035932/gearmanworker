package cn.videoworks.gearman.work;

import org.apache.commons.lang3.StringUtils;
import org.gearman.Gearman;
import org.gearman.GearmanServer;
import org.gearman.GearmanWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.videoworks.gearman.constant.GearmanFunctionConstant;
import cn.videoworks.gearman.function.WriteStorage;
import cn.videoworks.gearman.util.PropertiesUtil;

/**
 * ClassName:StorageWork
 * Function: TODO ADD FUNCTION
 * Reason:	 TODO ADD REASON
 *  存储work
 * @author   meishen
 * @version  
 * @since    Ver 1.1
 * @Date	 2018	2018年6月17日		下午6:50:06
 *
 * @see 	 
 */
public class StorageWork {
	
	private static Logger log = LoggerFactory.getLogger(StorageWork.class);
	
    /**
     * registWorker:(注册work)
     *
     * @author   meishen
     * @Date	 2018	2018年6月17日		下午6:50:22   
     * @return void    
     * @throws 
     * @since  Videoworks　Ver 1.1
     */
    @SuppressWarnings("static-access")
	public static void registWorker(String storageClient){
        Gearman gearman = Gearman.createGearman();
 
        String gearmanIp = "";
        int gearmanPort = -1;
		try {
			gearmanIp = PropertiesUtil.getPropertiesUtil().get("gearman.ip");
			gearmanPort = Integer.valueOf(PropertiesUtil.getPropertiesUtil().get("gearman.port"));
		} catch (Exception e) {
			e.printStackTrace();
			log.error("系统读取gearman配置信息失败！");
			System.exit(-1);;
		}
        if(StringUtils.isNotBlank(gearmanIp) && -1 != gearmanPort) {
	        GearmanServer server = gearman.createGearmanServer(gearmanIp, gearmanPort);
	        log.info("服务地址："+server.toString());
	        GearmanWorker worker = gearman.createGearmanWorker();
	        
	        worker.addFunction(GearmanFunctionConstant.WRITE_STORAGE, new WriteStorage(storageClient));
	       
	        boolean b = worker.addServer(server);
	        worker.getGearman();
	        if(b)
	        	log.info("创建【"+storageClient+"】存储work成功！");
	        else
	        	log.error("创建【"+storageClient+"】存储work失败！");
    
        }else {
        	log.error("读取gearman配置信息失败");
        }
    }
}
