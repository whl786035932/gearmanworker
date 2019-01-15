package cn.videoworks.gearman.client;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.BucketSummary;
import com.baidubce.services.bos.model.CannedAccessControlList;
import com.baidubce.services.bos.model.PutObjectRequest;

import cn.videoworks.gearman.util.PropertiesUtil;

public class BaiDuBosClient {
	private static Logger logger = LoggerFactory.getLogger(BaiDuBosClient.class);
	
	private static BosClient client = null;
	private static BaiDuBosClient bdbosClient;
	
	
	@SuppressWarnings("static-access")
	private BaiDuBosClient() {
		try {
			BosClientConfiguration config = new BosClientConfiguration();
			config.setCredentials(new DefaultBceCredentials(PropertiesUtil.getPropertiesUtil().get("BD_ACCESS_KEY_ID"), PropertiesUtil.getPropertiesUtil().get("BD_SECRET_ACCESS_KEY")));
			config.setEndpoint(PropertiesUtil.getPropertiesUtil().get("BD_ENDPOINT"));
			client = new BosClient(config);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("读取配置文件【gearman.properties】失败");
		}
	}
	
	public static synchronized BaiDuBosClient getBaiDuBosClient() {
		if(null == bdbosClient) {
			bdbosClient = new BaiDuBosClient();
		}
		return bdbosClient;
	}

	
	
	/**
	 * createBucket:(创建bucket)
	 *
	 * @author   meishen
	 * @Date	 2018	2018年6月21日		下午5:58:51
	 * @param bucketName   
	 * @return void    
	 * @throws 
	 * @since  Videoworks　Ver 1.1
	 */
	public void createBucket(String bucketName) {
		if(!client.doesBucketExist(bucketName)) {
			client.createBucket(bucketName);
			client.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
			logger.info("bucket【"+bucketName+"】创建成功");
		}
	}
	
	public void putObject(String bucketName,String key,File file) {
		createBucket(bucketName);
		PutObjectRequest request = new PutObjectRequest(bucketName, key, file);
		client.putObject(request);
	}
	
	public boolean deleteObject(String bucketName,String key) {
		boolean flag = true;
		try {
			if(client.doesBucketExist(bucketName)) {
				client.deleteObject(bucketName, key);
			}else {
				logger.info("bucketName【"+bucketName+"】不存在！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}
	
	public void listBuckets () {
	    // 获取用户的Bucket列表
	    List<BucketSummary> buckets = client.listBuckets().getBuckets();
	    for (BucketSummary bucket : buckets) {
	        System.out.println(bucket.getName());
	    }
	}
	
	public static void main(String[] args) {
		File file = new File("Z:\\lyy\\testimglyy2.png");
		BaiDuBosClient.getBaiDuBosClient().putObject("88888888888888", "key", file);
		BaiDuBosClient.getBaiDuBosClient().listBuckets();
	}
}
