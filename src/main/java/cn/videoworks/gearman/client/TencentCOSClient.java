package cn.videoworks.gearman.client;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.Bucket;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.CannedAccessControlList;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;

import cn.videoworks.gearman.common.ParameterMap;
import cn.videoworks.gearman.util.PropertiesUtil;

/**
 * @author   meishen
 * @Date	 2018	2018年9月6日		下午5:31:41
 * @Description 方法描述: 腾讯云cos
 */
public class TencentCOSClient {
	
	private static TencentCOSClient tencentCOSClient = null;
	
	private static COSClient cosClient = null;
	
	
	private static Logger logger = LoggerFactory.getLogger(TencentCOSClient.class);
	
	@SuppressWarnings("static-access")
	private TencentCOSClient() {
		try {
			// 1 初始化用户身份信息(secretId, secretKey)
			COSCredentials cred = new BasicCOSCredentials(PropertiesUtil.getPropertiesUtil().get("TENCENT_ACCESS_KEY_ID"),PropertiesUtil.getPropertiesUtil().get("TENCENT_SECRET_ACCESS_KEY"));
			// 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
			ClientConfig clientConfig = new ClientConfig(new Region(PropertiesUtil.getPropertiesUtil().get("TENCENT_REGION")));
			// 3 生成cos客户端
			cosClient = new COSClient(cred, clientConfig);
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("读取配置文件【gearman.properties】失败");
		}
	}
	
	public static synchronized TencentCOSClient getTencentCOSClient() {
		if (tencentCOSClient == null) {    
			tencentCOSClient = new TencentCOSClient();  
        }    
       return tencentCOSClient;  
	}
	
	public void createBucket(String bucketName) {
		if(!cosClient.doesBucketExist(bucketName)) {
			cosClient.createBucket(bucketName);
			cosClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
			logger.info("bucket【"+bucketName+"】创建成功");
		}
	}
	
	public void deleteBucket(String bucketName) {
		if(cosClient.doesBucketExist(bucketName)) {
			cosClient.deleteBucket(bucketName);
			logger.info("bucket【"+bucketName+"】删除成功");
		}
	}
	
	public void putObject(String bucketName,String key,File file,boolean isPublic) {
		createBucket(bucketName);
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file);
//		if(isPublic)
//			putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);//腾讯云只支持1000个acl，这里默认继承bucket权限
		
		if(!cosClient.doesObjectExist(bucketName, key)) {//已经存在就不传了
			cosClient.putObject(putObjectRequest);
			logger.info("bucket【"+bucketName+"】下key【"+key+"】添加成功");
		}
		cosClient.shutdown();
	}
	
	public boolean deleteObject(String bucketName,String key) {
		boolean flag = true;
		try {
			if(cosClient.doesBucketExist(bucketName)) {
				if(cosClient.doesObjectExist(bucketName, key)) {
					cosClient.deleteObject(bucketName, key);
					logger.info("bucket【"+bucketName+"】下key【"+key+"】删除成功");
				}else {
					logger.info("objectKey【"+key+"】不存在！");
				}
			}else {
				logger.info("bucketName【"+bucketName+"】不存在！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}
	
	public void putObject(String bucketName, String key, InputStream input,ObjectMetadata metadata,boolean isPublic) {
		createBucket(bucketName);
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, input, metadata);
		if(isPublic)
			putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
		cosClient.putObject(putObjectRequest);
		cosClient.shutdown();
	}
	
	public List<String> listBuckets () {
	    List<Bucket> buckets = cosClient.listBuckets();
	    List<String> bucketNames = new ArrayList<String>();
	    for (Bucket bucket : buckets) {
	    	String bucketName = bucket.getName();
	        System.out.println(bucketName);
	        bucketNames.add(bucketName);
	    }
	    cosClient.shutdown();
	    return bucketNames;
	}
	
	public  List<String> listObjects (String bucketName) {
		ObjectListing objectListing = cosClient.listObjects(bucketName);
		  List<String> keys = new ArrayList<String>();
		List<COSObjectSummary> objectSummaries = objectListing.getObjectSummaries();
		for (COSObjectSummary cosObjectSummary : objectSummaries) {
			// 文件路径
			String key = cosObjectSummary.getKey();
			// 获取文件长度
			long fileSize = cosObjectSummary.getSize();
			// 获取文件ETag
			String eTag = cosObjectSummary.getETag();
			// 获取最后修改时间
			Date lastModified = cosObjectSummary.getLastModified();
			// 获取文件的存储类型
			String StorageClassStr = cosObjectSummary.getStorageClass();
			logger.info(key+","+fileSize+","+eTag+","+lastModified+","+StorageClassStr);
			keys.add(key);
		}
		cosClient.shutdown();
		return keys;
	}
	
	public static void main(String[] args) {
		ParameterMap.getParameterMap("D:\\gearman.properties"); // 初始化配置文件
		File file = new File("D:\\test1.mp4");
		String bucketName = "videoworks-test-1256204773";
		
//		TencentCOSClient.getTencentCOSClient().deleteObject(bucketName, "test");
//		TencentCOSClient.getTencentCOSClient().deleteBucket(bucketName);
		
		TencentCOSClient.getTencentCOSClient().putObject(bucketName, "test1.mp4", file,true);
		TencentCOSClient.getTencentCOSClient().listObjects(bucketName);
		
//		List<String> listBuckets = TencentCOSClient.getTencentCOSClient().listBuckets();
//		for(String bucket : listBuckets) {
//			List<String> listObjects = TencentCOSClient.getTencentCOSClient().listObjects(bucket);
//			for(String key : listObjects) {
//				TencentCOSClient.getTencentCOSClient().deleteObject(bucket, key);
//			}
//		}
		
	}

}
