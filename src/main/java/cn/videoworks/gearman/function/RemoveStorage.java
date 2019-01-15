package cn.videoworks.gearman.function;

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
import cn.videoworks.gearman.dto.StorageRemoveDto;
import cn.videoworks.gearman.dto.StorageRequestDto;
import cn.videoworks.gearman.enumeration.ContentType;
import cn.videoworks.gearman.util.PropertiesUtil;

/**
 * @author   meishen
 * @Date	 2018	2018年9月10日		下午4:29:05
 * @Description 方法描述: 删除对象存储数据
 */
public class RemoveStorage implements GearmanFunction {

	public String storageClient;
	
	public RemoveStorage() {
		
	}
	
	public RemoveStorage(String storageClient) {
		this.storageClient = storageClient;
	}

	private static final Logger log = LoggerFactory.getLogger(WriteStorage.class);
	
	/**
	 * 数据格式：data List<Map<String,Object>>
	 * (non-Javadoc)
	 * @see org.gearman.GearmanFunction#work(java.lang.String, byte[], org.gearman.GearmanFunctionCallback)
	 */
	@SuppressWarnings("static-access")
	@Override
	public byte[] work(String arg0, byte[] data, GearmanFunctionCallback callback) {
		List<StorageRemoveDto> responseDtos = new ArrayList<StorageRemoveDto>();
		try {
			String string = new String(data, "UTF-8");
			System.out.println(string);
			List<StorageRemoveDto> asList = JsonConverter.asList(string, StorageRemoveDto.class);
			log.info("Work启动存储client为【"+storageClient+"】！");
			for (StorageRemoveDto dto : asList) {
				StorageRemoveDto removeDto = new StorageRemoveDto();
				// 获取校验码
				List<StorageRequestDto> storageRequestDtos = dto.getStorageRequestDto();
				 List<StorageRequestDto> storageRequestDto = new ArrayList<StorageRequestDto>();
				for(StorageRequestDto reqDto : storageRequestDtos) {
					String bucketName = "";
					String check_sum = reqDto.getCheck_sum();
					if (check_sum != null) {
						if(!storageClient.equals(GearmanFunctionConstant.STORAGE_TENCENTCOS))
							bucketName = "videoworks-"+check_sum.substring(check_sum.length() - 1);
						else
							bucketName = "videoworks-"+check_sum.substring(check_sum.length() - 1)+"-" +PropertiesUtil.getPropertiesUtil().get("TENCENT_APP_ID");
						
						StorageRequestDto rDto = new StorageRequestDto();
						rDto.setCheck_sum(check_sum);
						rDto.setId(reqDto.getId());
						rDto.setType(reqDto.getType());
						rDto.setUrl(reqDto.getUrl());
						int type = reqDto.getType();
						if(type == ContentType.VIDEO.getValue()) {
							switch (storageClient) {
								case GearmanFunctionConstant.STORAGE_AWSS3: //本地对象存储
									boolean flag = AWSS3Client.getAmazonS3Client().deleteObject(bucketName, check_sum);
									removeDto.setFlag(flag);
									break;
								case GearmanFunctionConstant.STORAGE_BAIDUBOS: //百度云bos对象存储
									boolean baiduFlag = BaiDuBosClient.getBaiDuBosClient().deleteObject(bucketName, check_sum);
									removeDto.setFlag(baiduFlag);
									break;
								case GearmanFunctionConstant.STORAGE_GZAWSS3: //贵州对象存储
									boolean gzFlag = GZAWSS3Client.getAmazonS3Client().deleteObject(bucketName, check_sum);
									removeDto.setFlag(gzFlag);
									break;
								case GearmanFunctionConstant.STORAGE_MINIOAWSS3: //minio轻量级对象存储
									boolean minioFlag  = MinioAWSS3Client.getMinioAWSS3Client().deleteObject(bucketName, check_sum);
									removeDto.setFlag(minioFlag);
									break;	
								case GearmanFunctionConstant.STORAGE_TENCENTCOS: //腾讯云对象存储
									boolean tencentFlag = TencentCOSClient.getTencentCOSClient().deleteObject(bucketName, check_sum);
									removeDto.setFlag(tencentFlag);
									break;	
								default:
									break;
							}
						}
						storageRequestDto.add(rDto);
						removeDto.setContentId(dto.getContentId());
						removeDto.setStorageRequestDto(storageRequestDto);
					}
				}
				responseDtos.add(removeDto);
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.error("存储删除失败："+e.getMessage());
			return buildResponse(ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION,"删除失败:"+e.getMessage(),responseDtos);
		}
		return buildResponse(ResponseDictionary.SUCCESS,"删除成功",responseDtos);
	}
	
	public byte[] buildResponse(Integer statusCode, String message,List<StorageRemoveDto> returnDtos) {
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
