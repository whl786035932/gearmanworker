package cn.videoworks.gearman.work;

import org.apache.commons.lang3.StringUtils;
import org.gearman.Gearman;
import org.gearman.GearmanServer;
import org.gearman.GearmanWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.videoworks.gearman.constant.GearmanFunctionConstant;
import cn.videoworks.gearman.function.BaiduStorage;
import cn.videoworks.gearman.function.GZCDNInject;
import cn.videoworks.gearman.function.SXCDNInject;
import cn.videoworks.gearman.function.TencentCOS;
import cn.videoworks.gearman.util.PropertiesUtil;

public class CNDWork {

	private static Logger log = LoggerFactory.getLogger(CNDWork.class);
	
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
	public static void registWorker(String cdnClient){
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
	        
	        switch (cdnClient) {
				case GearmanFunctionConstant.CDN_BAIDUBOS:
					worker.addFunction(GearmanFunctionConstant.WRITE_CDN,new BaiduStorage());
					break;
				case GearmanFunctionConstant.CDN_GZ:
					worker.addFunction(GearmanFunctionConstant.WRITE_CDN, new GZCDNInject());
					break;
				case GearmanFunctionConstant.CDN_TENCENTCOS:
					worker.addFunction(GearmanFunctionConstant.WRITE_CDN, new TencentCOS());
					break;
				case GearmanFunctionConstant.CDN_SX:
					worker.addFunction(GearmanFunctionConstant.CDN_SX, new SXCDNInject());
					SXMonitorXMLWorker.registWorker();
				default:
					break;
			}
	       
	        boolean b = worker.addServer(server);
	        worker.getGearman();
	        if(b)
	        	log.info("启动【"+cdnClient+"】CDN work成功！");
	        else
	        	log.error("启动【"+cdnClient+"】CDN work失败！");
    
        }else {
        	log.error("读取gearman配置信息失败");
        }
    }
}
