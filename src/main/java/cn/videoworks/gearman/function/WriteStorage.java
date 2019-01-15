package cn.videoworks.gearman.function;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gearman.GearmanFunction;
import org.gearman.GearmanFunctionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.videoworks.commons.util.json.JsonConverter;
import cn.videoworks.gearman.client.AWSS3Client;
import cn.videoworks.gearman.client.BaiDuBosClient;
import cn.videoworks.gearman.client.GZAWSS3Client;
import cn.videoworks.gearman.client.MinioAWSS3Client;
import cn.videoworks.gearman.client.TencentCOSClient;
import cn.videoworks.gearman.constant.GearmanFunctionConstant;
import cn.videoworks.gearman.constant.ResponseDictionary;
import cn.videoworks.gearman.dto.AwsStorageDto;
import cn.videoworks.gearman.util.FileUtil;
import cn.videoworks.gearman.util.PropertiesUtil;

/**
 * ClassName:StorageWork Function: 存储work Reason: TODO ADD REASON
 *
 * @author meishen
 * @version
 * @since Ver 1.1
 * @Date 2018 2018年6月17日 下午5:40:55
 *
 * @see
 */
public class WriteStorage implements GearmanFunction {
	
	public String storageClient;
	
	public WriteStorage() {
		
	}
	
	public WriteStorage(String storageClient) {
		this.storageClient = storageClient;
	}

	private static final Logger log = LoggerFactory.getLogger(WriteStorage.class);
	
	@SuppressWarnings("static-access")
	@Override
	public byte[] work(String arg0, byte[] data, GearmanFunctionCallback callback) {
		ArrayList<AwsStorageDto> returnDtos = new ArrayList<AwsStorageDto>();
		String aws_endpoint;
		try {
			aws_endpoint = PropertiesUtil.getPropertiesUtil().get("aws_endpoint");
			String string = new String(data, "UTF-8");
			log.info("写入对象存储Work，收到参数："+string);
			List<AwsStorageDto> asList = JsonConverter.asList(string, AwsStorageDto.class);

			for (AwsStorageDto awsStorageDto : asList) {
				// 获取校验码
				String check_sum = awsStorageDto.getCheck_sum();
				Long id = awsStorageDto.getId();
				String url = awsStorageDto.getUrl();
				Integer type = awsStorageDto.getType();

				String bucketName = "";

				if (check_sum != null) {
					if(!storageClient.equals(GearmanFunctionConstant.STORAGE_TENCENTCOS))
						bucketName = "videoworks-"+check_sum.substring(check_sum.length() - 1);
					else
						bucketName = "videoworks-"+check_sum.substring(check_sum.length() - 1)+"-" +PropertiesUtil.getPropertiesUtil().get("TENCENT_APP_ID");
				}
				File file = new File(url);
				
				log.info("Work启动存储client为【"+storageClient+"】！");
				String suffix = url.substring(url.lastIndexOf("."));//腾讯云必需知道文件后缀,此处得到.mp4或者.ts等，包含"."
				check_sum = check_sum+suffix;
				switch (storageClient) {
					case GearmanFunctionConstant.STORAGE_AWSS3: //本地对象存储
//						AWSS3Client.getAmazonS3Client().putObject(bucketName, check_sum, file, true);
						AWSS3Client.getAmazonS3Client().putObjectWithTransferManager(bucketName, check_sum, file, true);
						break;
					case GearmanFunctionConstant.STORAGE_BAIDUBOS: //百度云bos对象存储
						BaiDuBosClient.getBaiDuBosClient().putObject(bucketName, check_sum, file);
						aws_endpoint = PropertiesUtil.getPropertiesUtil().get("BD_ENDPOINT");
						//删除ftp文件
						FileUtil.deleteFile(url);
						break;
					case GearmanFunctionConstant.STORAGE_GZAWSS3: //贵州对象存储
//						GZAWSS3Client.getAmazonS3Client().putObject(bucketName, check_sum, file, true);
						GZAWSS3Client.getAmazonS3Client().putObjectWithTransferManager(bucketName, check_sum, file, true);
						break;
					case GearmanFunctionConstant.STORAGE_MINIOAWSS3: //minio轻量级对象存储
//						MinioAWSS3Client.getMinioAWSS3Client().putObject(bucketName, check_sum, file, true);
						MinioAWSS3Client.getMinioAWSS3Client().putObjectWithTransferManager(bucketName, check_sum, file, true);
						break;	
					case GearmanFunctionConstant.STORAGE_TENCENTCOS: //腾讯云对象存储
//						String suffix = url.substring(url.lastIndexOf("."));//腾讯云必需知道文件后缀,此处得到.mp4或者.ts等，包含"."
//						check_sum = check_sum+suffix;
						TencentCOSClient.getTencentCOSClient().putObject(bucketName, check_sum, file, true);
						//删除ftp文件
						FileUtil.deleteFile(url);
						break;	
					default:
						break;
				}
				
				AwsStorageDto returnDto = new AwsStorageDto();
				returnDto.setId(id);
				returnDto.setType(type);
				if(!storageClient.equals(GearmanFunctionConstant.STORAGE_TENCENTCOS))//http://ip:port/bucketName/key
					if(aws_endpoint.endsWith("/"))
						returnDto.setUrl(aws_endpoint + bucketName + "/" + check_sum);
					else
						returnDto.setUrl(aws_endpoint + "/" + bucketName + "/" + check_sum);
				else {//https://<bucketname-APPID>.cos.ap-beijing-1.myqcloud.com
					returnDto.setUrl(PropertiesUtil.getPropertiesUtil().get("PROTOCOL")+"://" + bucketName + ".cos." + PropertiesUtil.getPropertiesUtil().get("TENCENT_REGION")+"."+PropertiesUtil.getPropertiesUtil().get("TENCENT_ENDPOINT")+ "/" + check_sum);
				}
				returnDtos.add(returnDto);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			log.error("存储注入失败：" + e1.getMessage());
			return buildResponse(ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION,"注入失败:"+e1.getMessage(),returnDtos);
		}catch (Exception e) {
			e.printStackTrace();
			log.error("存储注入失败："+e.getMessage());
			return buildResponse(ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION,"注入失败:"+e.getMessage(),returnDtos);
		}
		return buildResponse(ResponseDictionary.SUCCESS,"注入成功",returnDtos);
	}
	
	public byte[] buildResponse(Integer statusCode, String message,List<AwsStorageDto> returnDtos) {
		HashMap<String, Object> response = new HashMap<String, Object>();
		response.put("statusCode", statusCode);
		response.put("message", message);
		response.put("data", returnDtos);
		String returnStr = JsonConverter.format(response);
		byte[] bytes = null;
		try {
			bytes = returnStr.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		}
		return bytes;
	}

}
