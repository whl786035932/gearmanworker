package cn.videoworks.gearman.function;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MoveAction;

import org.gearman.GearmanFunction;
import org.gearman.GearmanFunctionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.videoworks.commons.util.json.JsonConverter;
import cn.videoworks.gearman.client.SXFTPUtilClient;
import cn.videoworks.gearman.common.ParameterMap;
import cn.videoworks.gearman.constant.ResponseDictionary;
import cn.videoworks.gearman.dto.MovieDto;
import cn.videoworks.gearman.dto.WorkerDto;
import cn.videoworks.gearman.util.ApiResponse;
import cn.videoworks.gearman.util.FileUtil;
import cn.videoworks.gearman.util.PropertiesUtil;

/**
 * https://www.cnblogs.com/javaLin/p/8024977.html 陕西CDN的注入，
 * 主要功能就是将ts文件和对应的xml文件放到规定目录结构的ftp中，
 * 然后轮询等CIP发布完成后是否生成了xml文件，从生成的cip系统生成的xml文件中读取cdnkey就是rtsp地址
 * 
 * @author whl
 * 
 * 
 *         接收到的任务参数格式：
 *         {
		"isUploadCdn": 1,
		"taskId": 11111,
	
		"asset_id": "5b18f09sadddsddd1c7e051a21d1471dc555683",
		"title": "少帅2",
		"title_abbr": "G",
		"type": 1,
		"description": "789",
		"publish_time": "2018-06-07 15:12:09",
		"duration": 22,
		"cp": "视频工厂",
		"source": "视频工厂",
		"movies": [{
			"id": 323,
			"url": "http://10.2.16.99:7480/my-new-bucket/01whl2.mp4",
			"type": 1,
			"size": 3511088,
			"check_sum": "a2fb929150dc414142d53bd0b0ff73a9",
			"filename": "whl2.mp4"
		}],
		"images": [{
			"id": 324,
			"width": 720,
			"height": 576,
			"url": "http://10.2.16.99:7480/my-new-bucket/testimgwhl2.png",
			"size": 20,
			"check_sum": "b394d952db8dd16e76858fa3d04838ec",
			"filename": "testimgwhl2.png"
		}]
	}
 *
 *
 *         worker返回的任务参数格式是：
 *
 *
	 *         {
		"data": {
			"taskId": 111,
			"msgid": "111",
			"contentId": "内容的id",
			"cdns": [{
				"id": 572,
				"cdn_key": "http://10.2.16.99:7480/videoworks-c/b394d952db8dd16e76858fa3d04838ec",
				"type": 1
	
	
			}, {
				"id": 571,
				"cdn_key":
	
					"http://10.2.16.99:7480/videoworks-9/a2fb929150dc414142d53bd0b0ffddd73a9",
				"type": 2
			}]
		},
		"message": "注入成功",
		"statusCode": 100000
	}
 *
 * 
 */
public class SXCDNInject implements GearmanFunction {

	private static final Logger log = LoggerFactory.getLogger(SXCDNInject.class);

	@Override
	public byte[] work(String arg0, byte[] data, GearmanFunctionCallback arg2) throws Exception {

		ApiResponse response = new ApiResponse();
		String wokerJson = new String(data, "UTF-8");
		log.info("陕西CDN Worker接收到的任务参数：【" + wokerJson + "】");

		WorkerDto cdnWorker = JsonConverter.parse(wokerJson, WorkerDto.class);
		if (cdnWorker != null) {
			Long taskId = cdnWorker.getTaskId(); // 也充当msgid
			Long contentId = cdnWorker.getContentId();
			List<MovieDto> movies = cdnWorker.getMovies();
			// 组织返回的数据
			try {

				response = copyVideoAndWriteXml2FTP(movies, cdnWorker);
			} catch (Exception e) {
				HashMap<String, Object> response_data = new HashMap<>();
				response_data.put("taskId", taskId);
				response_data.put("msgid", contentId);
				response = buildResponse(ResponseDictionary.SERVEREXCEPTION,
						"删词CDN worker注入失败：【" + e.getMessage() + "】", response_data, response);
			}

		} else {
			response = buildResponse(ResponseDictionary.SERVEREXCEPTION, "陕西CDN worker注入失败：解析任务参数失败", null, response);
		}

		String format = JsonConverter.format(response);
		return format.getBytes();
	}

	/***
	 * 将视频文件上传到ftp目录，并将对应的xml文件写到ftp目录中
	 * 
	 * @param movies
	 * @param cdnWorker
	 * @return
	 */
	private ApiResponse copyVideoAndWriteXml2FTP(List<MovieDto> movies, WorkerDto cdnWorker) {
		ApiResponse apiResponse = new ApiResponse();
		Long taskId = cdnWorker.getTaskId();
		Long contentId = cdnWorker.getContentId();

		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("taskId", taskId);
		hashMap.put("msgid", taskId);
		hashMap.put("contentId", contentId);

		try {

			for (MovieDto movieDto : movies) {
				String fileName= movieDto.getFilename();
				String url = movieDto.getUrl();
				String ftp_filename = "sx-"+contentId+"."+FileUtil.suffix(fileName);
				realUpload2Ftp(url, ftp_filename);
				writeXML2Ftp(movieDto, cdnWorker);
			}

		} catch (Exception e) {
			apiResponse.setStatusCode(ResponseDictionary.SERVEREXCEPTION);
			apiResponse.setMessage("陕西CDN worker注入失败：【" + e.getMessage() + "】");
			apiResponse.setData(hashMap);
		}
		return apiResponse;
	}

	/**
	 * 组织视频的xml文件---------------TO DO
	 * 
	 * @param movieDto
	 * @param cdnWorker
	 * @throws Exception 
	 */
	private void writeXML2Ftp(MovieDto movieDto, WorkerDto cdnWorker) throws Exception {
		String str =createXml(movieDto, cdnWorker);
		Long contentId = cdnWorker.getContentId();
		// 获取的ftp的上传目录
		String ftpRootDir = PropertiesUtil.getPropertiesUtil().get("sx.ftp.rootDir");
		String  ftpXMLaDir = PropertiesUtil.getPropertiesUtil().get("sx.ftp.xml.rootDir"); 
		String filePath = ftpRootDir + "/"+ftpXMLaDir;
		InputStream input =new ByteArrayInputStream(str.getBytes("UTF-8"));
		SXFTPUtilClient ftpClient = SXFTPUtilClient.getInstance();
		if (ftpClient.connect()) {
			String xml_filename = "sx-"+contentId+".xml";
			boolean upload = ftpClient.upload(xml_filename, filePath, input);
			log.info("陕西CDN worker 写文件="+upload);
		}
	}
	
	/**
	 * 生成xml
	 * @param movieDto
	 * @param cdnWorker
	 * @return
	 */
	public  String createXml(MovieDto movieDto, WorkerDto cdnWorker) {
		StringBuffer sb = new StringBuffer();
		//内容提供商
		String  proviederID ="";
		String  VODRelease_assetID="";
		String  VODRelase_seriaNo="";
		String  AddMetadataAsset_groupAssetID="";
		String  startDateTime="";
		String  endDateTime="";
		String showType ="Video";
		String fileName = movieDto.getFilename();
		String fileSize = movieDto.getSize();
		String check_sum = movieDto.getCheck_sum();
		
		
		String category ="栏目";  //????------------TO DO
		String  classification ="分类"; //???------------ TO  DO
		
		sb.append("<?xml  version='1.0' encoding='GBK' ?>");
		sb.append("<adi:ADI2 xmlns='http://www.cablelabs.com/VODSchema/default' xmlns:adi='http://www.cablelabs.com/VODSchema/adi'  xmlns:vod='http://www.cablelabs.com/VODSchema/vod'>");
			
			//GroupAsset定义
			sb.append("<adi:OpenGroupAsset type='VODRelease' product='VOD'>");
				//内容提供商
				sb.append("<vod:VODRelease providerID='"+proviederID+"' providerType='2' assetID='"+VODRelease_assetID+"' updateNum='' groupAsset='Y' serialNo='"+VODRelase_seriaNo+"'>"); //内容提供商的ID----------TODO 
					sb.append("<adi:AssetLifetime startDateTime='"+startDateTime+"' endDateTime='"+endDateTime+"' /> ");   //开始时间和结束时间  ----TODO 
				sb.append("</vod:VODRelease>");
			sb.append("</adi:OpenGroupAsset>");
			//MetadatAsset定义 --begin
			sb.append("<adi:AddMetadataAsset groupProviderID='"+proviederID+"' groupAssetID='"+AddMetadataAsset_groupAssetID+"' type='Title' product='VOD'>");
				//vod-title ---begin-------------------------新闻的元数据----------------未确定用哪个模板？？？？？？？？？？？
				sb.append("<vod:Title providerID='"+proviederID+"' assetID='"+AddMetadataAsset_groupAssetID+"' updateNum='1'>");
					//adi:AssetLifetime
					sb.append( "<adi:AssetLifetime startDateTime='"+startDateTime+"' endDateTime='"+endDateTime+"' />" );
					sb.append(" <vod:TitleFull>"+cdnWorker.getTitle()+"</vod:TitleFull> ");   ///title
					sb.append("<vod:ShowType>"+showType+"/vod:ShowType> ");     //showType  Video/Movie/Column
					sb.append("<vod:SummaryMedium>"+cdnWorker.getDescription()+"</vod:SummaryMedium> ");  //中文描述
					sb.append("<vod:SummaryShort></vod:SummaryShort> ");  //看点
					
				//vod-title ---end
				sb.append("</vod:Title>");
			sb.append(" </adi:AddMetadataAsset>");   //《！--Title-->
			
			
			sb.append("<adi:AddMetadataAsset  groupProviderID='"+proviederID+"' groupAssetID='"+AddMetadataAsset_groupAssetID+"' type='CategoryPath'  product='VOD'>");
				sb.append("<vod:CategoryPath providerID='"+proviederID+"' assetID='"+AddMetadataAsset_groupAssetID+"' updateNum='1'>");
					sb.append("<vod:Category>"+category+"</vod:Category> ");
					sb.append("<vod:Classification>"+classification+"</vod:Classification> ");
				sb.append("</vod:CategoryPath>");
			sb.append("</adi:AddMetadataAsset>");  //<!--CategoryPath-->
			
			
			sb.append("<adi:AddMetadataAsset  groupProviderID='"+proviederID+"' groupAssetID='"+AddMetadataAsset_groupAssetID+"' type='Copyright' product='VOD'>");
				sb.append("<vod:Copyright providerID='"+proviederID+"' assetID='"+AddMetadataAsset_groupAssetID+"' updateNum='1'>");
					sb.append("<adi:AssetLifetime startDateTime='"+startDateTime+"' endDateTime='"+endDateTime+"' /> ");
				sb.append("</vod:Copyright>");
			sb.append("</adi:AddMetadataAsset>");
			//MetadatAsset定义 --end
			
			//<!-- Adi    AcceptContentAsset  begin-->
			sb.append("<adi:AcceptContentAsset type='Video' metadataOnly='N' fileName='"+fileName+"' fileSize='"+fileSize+"' mD5CheckSum='"+check_sum+"'>");
				
				sb.append("<vod:Video providerID='"+proviederID+"' assetID='"+AddMetadataAsset_groupAssetID+"' updateNum='1' fileName='"+fileName+"' fileSize='"+fileSize+"' mD5CheckSum='"+check_sum+"' ");
					sb.append("<adi:AssetLifetime startDateTime='"+startDateTime+"' endDateTime='"+endDateTime+"' /> ");
				sb.append("</vod:Video>");
			sb.append(" </adi:AcceptContentAsset>");
			//<!-- Adi    AcceptContentAsset  end-->
			
			//<!--adi  AssociateContent-->  ---begin
			sb.append("<adi:AssociateContent type='Video' effectiveDate='' groupProviderID='"+proviederID+"' groupAssetID='"+AddMetadataAsset_groupAssetID+"' providerID='"+proviederID+"' assetID='"+AddMetadataAsset_groupAssetID+"' /> ")	;
			//<!--adi  AssociateContent-->  ---end  
		sb.append("</adi:ADI2>");
		return sb.toString();
	}

	/**
	 * 将视频文件传入到ftp ---------------------TO DO
	 * 
	 * @param url 视频的百度云的存储地址
	 * @param filename   视频的名称如test.mp4
	 * @throws Exception
	 */
	private void realUpload2Ftp(String url, String filename) throws Exception {

		// 获取的ftp的上传目录
		String ftpRootDir = PropertiesUtil.getPropertiesUtil().get("sx.ftp.rootDir");
		String  ftpMeidaDir = PropertiesUtil.getPropertiesUtil().get("sx.ftp.media.rootDir"); 
		String filePath = ftpRootDir + "/"+ftpMeidaDir;
		// 判读url
		InputStream input = null;
		if (url.contains("http://")) {
			input = FileUtil.getFileStream(url);

		} else {
			// 本地路径
			File file = new File(url);
			boolean exists = FileUtil.exists(url);
			if (!exists) {
				throw new FileNotFoundException("文件不存在：" + url);
			}
			input = new FileInputStream(file);
		}
		log.info("获取到input`````````=" + input.available());
		SXFTPUtilClient ftpClient = SXFTPUtilClient.getInstance();
		if (ftpClient.connect()) {
			boolean upload = ftpClient.upload(filename, filePath, input);
			log.info("陕西CDN worker 是否上传="+upload);
		}
	}

	public ApiResponse buildResponse(Integer statusCode, String message, Map<String, Object> data,
			ApiResponse returnApiResponse) {
		returnApiResponse.setStatusCode(statusCode);
		returnApiResponse.setMessage(message);
		returnApiResponse.setData(data);
		return returnApiResponse;
	}
	
	private void writeXML2FtpTest(MovieDto movieDto, WorkerDto cdnWorker) throws Exception {
		// 获取的ftp的上传目录
		String ftpRootDir = PropertiesUtil.getPropertiesUtil().get("sx.ftp.rootDir");
		String  ftpXMLaDir = PropertiesUtil.getPropertiesUtil().get("sx.ftp.result.rootDir"); 
		String filePath = ftpRootDir + "/"+ftpXMLaDir;
		String str="<MetaDataContentsNotify><content  OriginalContentCode='8320ddddeedddddd'  ContentId='sx-111'   PlayURL='http://10.2555555//dsa.mp3' /></MetaDataContentsNotify>";
		InputStream input =new ByteArrayInputStream(str.getBytes("UTF-8"));
		SXFTPUtilClient ftpClient = SXFTPUtilClient.getInstance();
		if (ftpClient.connect()) {
			String xml_filename = "sx-"+"test2"+".xml";
			boolean upload = ftpClient.upload(xml_filename, filePath, input);
			log.info("陕西CDN worker 写文件="+upload);
		}
	}
	public static void main(String[] args) throws Exception {
		ParameterMap.getParameterMap("D:\\gearman.properties"); //初始化配置文件
		SXCDNInject sxcdnInject = new  SXCDNInject();
//		sxcdnInject.realUpload2Ftp("http://bj.bcebos.com/videoworks-0/e0cad3b74af072f487bb9ce0d5552ec0.mp4", "645252.mp4");
		sxcdnInject.writeXML2FtpTest(null, null);
	}
}
