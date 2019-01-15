package cn.videoworks.gearman.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

import cn.videoworks.gearman.common.ParameterMap;
import cn.videoworks.gearman.util.PropertiesUtil;

/**
 * ClassName:GZAWSS3Client
 * Function: TODO ADD FUNCTION
 * Reason:	 TODO ADD REASON
 * 贵州aws s3 客户端
 * @author   meishen
 * @version  
 * @since    Ver 1.1
 * @Date	 2018	2018年6月28日		下午3:06:06
 *
 * @see 	 
 */
public class GZAWSS3Client {
	
	private static AmazonS3 s3 = null;
	
	private static TransferManager tm = null;
	
	private static GZAWSS3Client gZAWSS3Client = null;
	
	private static Logger logger = LoggerFactory.getLogger(AWSS3Client.class);

	
	@SuppressWarnings("static-access")
	private GZAWSS3Client() {
		try {
		 AWSCredentials credentials = new BasicAWSCredentials(PropertiesUtil.getPropertiesUtil().get("aws_access_key_id"), PropertiesUtil.getPropertiesUtil().get("aws_secret_access_key"));

	        ClientConfiguration clientConfiguration = new ClientConfiguration();
	        clientConfiguration.setSignerOverride("S3SignerType");

	        s3 = AmazonS3ClientBuilder.standard()
	                .withCredentials(new AWSStaticCredentialsProvider(credentials))
	                .withClientConfiguration(clientConfiguration)
	                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(PropertiesUtil.getPropertiesUtil().get("aws_endpoint"),"us-east-1"))
	                .build();
	        
	        tm = TransferManagerBuilder.standard()
                    .withS3Client(s3)
                    .build();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("读取配置文件【gearman.properties】失败");
		}
	}
	
	public static synchronized GZAWSS3Client getAmazonS3Client() {
		if (gZAWSS3Client == null) {    
			gZAWSS3Client = new GZAWSS3Client();  
        }    
       return gZAWSS3Client;  
	}
	
	public static void createBucket(String bucketName) {
		if(!s3.doesBucketExistV2(bucketName)) {
			s3.createBucket(bucketName);
			logger.info("bucket【"+bucketName+"】创建成功");
		}
	}
	
	public static boolean deleteObject(String bucketName,String key) {
		boolean flag = true;
		try {
			if(s3.doesBucketExistV2(bucketName)) {
				if(s3.doesObjectExist(bucketName, key)) {
					s3.deleteObject(bucketName, key);
				}else {
					logger.info("objectKey【"+key+"】不存在！");
				}
			}else {
				logger.info("bucketName【"+bucketName+"】不存在！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("bucketName【"+bucketName+"】不存在！");
			flag =false;
		}
		return flag;
	}
	
	public static void putObject(String bucketName,String key,File file,boolean isPublic) {
		createBucket(bucketName);
		if(s3.doesObjectExist(bucketName, key)) {
			logger.info("bucket【"+bucketName+"】+object【"+key+"】已经存在");
		}else {
			PutObjectRequest request = new PutObjectRequest(bucketName, key, file);
			if(isPublic)
				request.setCannedAcl(CannedAccessControlList.PublicRead);
			s3.putObject(request);
		}
	}
	
	public static void putObjectWithTransferManager(String bucketName,String key,File file,boolean isPublic) {
		createBucket(bucketName);
		if(s3.doesObjectExist(bucketName, key)) {
			logger.info("bucket【"+bucketName+"】+object【"+key+"】已经存在");
		}else {
			PutObjectRequest request = new PutObjectRequest(bucketName, key, file);
			if(isPublic)
				request.setCannedAcl(CannedAccessControlList.PublicRead);
			Upload up = tm.upload(request);
			logger.debug("bucketName【"+bucketName+"】-objectKey【"+key+"】开始上传对象存储！");
			try {
				up.waitForCompletion();
				logger.debug("bucketName【"+bucketName+"】-objectKey【"+key+"】上传对象存储成功！");
			} catch (AmazonServiceException e) {
				e.printStackTrace();
				logger.error("bucketName【"+bucketName+"】-objectKey【"+key+"】上传对象存储失败，原因【"+e.getMessage()+"】！");
			} catch (AmazonClientException e) {
				e.printStackTrace();
				logger.error("bucketName【"+bucketName+"】-objectKey【"+key+"】上传对象存储失败，原因【"+e.getMessage()+"】！");
			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.error("bucketName【"+bucketName+"】-objectKey【"+key+"】上传对象存储失败，原因【"+e.getMessage()+"】！");
			}
		}
	}
	
	public static List<S3ObjectSummary> getObjects(String bucketName) {
		List<S3ObjectSummary> objects = new ArrayList<S3ObjectSummary>();
		if(s3.doesBucketExistV2(bucketName)) {
			ListObjectsV2Result lov2s = s3.listObjectsV2(bucketName);
			objects =  lov2s.getObjectSummaries();
			for (S3ObjectSummary os: objects) {  
	            //调用其 getKey 方法以检索对象名称  
	            logger.info("*对象名称： " + os.getKey());
	        }  
		}
		return objects;
	}
	
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		ParameterMap.getParameterMap("D:\\gearman.properties"); 
		GZAWSS3Client.getAmazonS3Client().putObject("000000", "key2", new File("Z:\\lyy\\testimglyy2.png"), true);
		GZAWSS3Client.getAmazonS3Client().getObjects("000000");
	}
}
