package cn.videoworks.gearman.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcloud.vod.VodApi;
import com.qcloud.vod.response.VodUploadCommitResponse;

import cn.videoworks.commons.util.json.JsonConverter;
import cn.videoworks.gearman.common.ParameterMap;
import cn.videoworks.gearman.util.PropertiesUtil;

public class TencentVODClient {

	private static TencentVODClient tencentVODClient = null;
	
	private static VodApi vodApi = null;
	
	
	private static Logger log = LoggerFactory.getLogger(TencentCOSClient.class);
	
	@SuppressWarnings("static-access")
	private TencentVODClient() {
		try {
			vodApi = new VodApi(PropertiesUtil.getPropertiesUtil().get("TENCENT_ACCESS_KEY_ID"), PropertiesUtil.getPropertiesUtil().get("TENCENT_SECRET_ACCESS_KEY"));
		} catch (Exception e) {
			e.printStackTrace();
			log.error("读取配置文件【gearman.properties】失败");
		}
	}
	
	public VodUploadCommitResponse upload(String videoPath) {
		try {
			VodUploadCommitResponse response = vodApi.upload(videoPath);
			log.info(JsonConverter.format(response.getVideo()));
			log.info(JsonConverter.format(response.getCover()));
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public VodUploadCommitResponse upload(String videoPath,String coverPath) {
		try {
			VodUploadCommitResponse response =  vodApi.upload(videoPath, coverPath);
			log.info(JsonConverter.format(response.getVideo()));
			log.info(JsonConverter.format(response.getCover()));
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public VodUploadCommitResponse upload(String videoPath,String coverPath,String procedure) {
		try {
			VodUploadCommitResponse response =  vodApi.upload(videoPath, coverPath, procedure);
			log.info(JsonConverter.format(response));
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static synchronized TencentVODClient getTencentVODClient() {
		if (tencentVODClient == null) {    
			tencentVODClient = new TencentVODClient();  
        }    
       return tencentVODClient;  
	}
	
	public static void main(String[] args) {
		ParameterMap.getParameterMap("D:\\gearman.properties"); // 初始化配置文件
//		TencentVODClient.getTencentVODClient().upload("D:\\test.mp4","D:\\test.jpg");
		TencentVODClient.getTencentVODClient().upload("D:\\test.mp4","D:\\test.jpg","QCVB_SimpleProcessFile({210})");
	}
}
