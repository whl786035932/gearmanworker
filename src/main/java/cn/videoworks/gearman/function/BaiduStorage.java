package cn.videoworks.gearman.function;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gearman.GearmanFunction;
import org.gearman.GearmanFunctionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.baidubce.auth.DefaultBceCredentials;
//import com.baidubce.services.bos.BosClient;
//import com.baidubce.services.bos.BosClientConfiguration;

import cn.videoworks.commons.util.json.JsonConverter;
import cn.videoworks.gearman.constant.ResponseDictionary;
import cn.videoworks.gearman.dto.ImageDto;
import cn.videoworks.gearman.dto.MovieDto;
import cn.videoworks.gearman.dto.WorkerDto;
import cn.videoworks.gearman.util.DateUtil;
import cn.videoworks.gearman.util.PropertiesUtil;
import cn.videoworks.gearman.util.WorkerUtil;

/**
 * ClassName:StorageWork Function: 存储work Reason: TODO ADD REASON
 * 
 * @author peijunhe
 * @version
 * @since Ver 1.1
 * @Date 2018 2018年6月17日 下午5:40:55
 * 
 * @see
 */
public class BaiduStorage implements GearmanFunction {
	private static final Logger log = LoggerFactory
			.getLogger(BaiduStorage.class);

	@Override
	public byte[] work(String arg0, byte[] data,
			GearmanFunctionCallback callback) throws Exception {
//		String jsonData = new String(data);
		String jsonData = new String(data, "UTF-8");
		log.info("publishworker 收到的任务=" + jsonData); // byte[]
		// 返回参数
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
		// 解析参数
		WorkerDto worker = JsonConverter.parse(jsonData, WorkerDto.class);
		try {
			// 将视频/海报注入ftp
			List<MovieDto> movies = null;
			List<ImageDto> images = null;
			if (worker != null) {
				try {
					// 视频
					movies = worker.getMovies();
					// 海报
					images = worker.getImages();
					if (movies != null) {
						for (MovieDto movie : movies) {
							if (movie.getUrl() != null) {
								Map<String, Object> re = new HashMap<String, Object>();
								re.put("id", movie.getId());
								re.put("cdn_key", String.valueOf(worker.getContentId()));
								re.put("type", 1);
								datas.add(re);
							}
						}
					}
					if (images != null) {
						for (ImageDto image : images) {
							if (image.getUrl() != null) {
								Map<String, Object> re = new HashMap<String, Object>();
								// 图片上传阿帕奇
								String apaUrl = readlUplod(image.getUrl(),image.getFilename(),String.valueOf(worker.getContentId()));
								re.put("id", image.getId());
								re.put("cdn_key", apaUrl);
								re.put("type", 2);
								datas.add(re);
							}
						}
					}
					result.put("statusCode", ResponseDictionary.SUCCESS);
					Map<String,Object>param = new HashMap<String,Object>();
					param.put("cdns", datas);
					param.put("contentId",String.valueOf(worker.getContentId()));
					param.put("taskId", String.valueOf(worker.getTaskId()));
					param.put("cdns", datas);
					param.put("msgid", null);
					result.put("data", param);
					String returnStr = JsonConverter.format(result);
					byte[] returnData = returnStr.getBytes();
					return returnData;
				} catch (Exception e) {
					Map<String,Object>param = new HashMap<String,Object>();
					param.put("cdns", datas);
					param.put("contentId",String.valueOf(worker.getContentId()));
					param.put("taskId", String.valueOf(worker.getTaskId()));
					param.put("cdns", datas);
					param.put("msgid", null);
					result.put("data", param);
					result.put("statusCode",ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION);
					result.put("message","错误信息为:【"+ e.toString()+"】获得的参数为:"+jsonData);
					String returnStr = JsonConverter.format(result);
					log.debug("返回结果为:" + returnStr);
					byte[] returnData = returnStr.getBytes();
					return returnData;
				}
			}
		} catch (Exception e) {
			Map<String,Object>param = new HashMap<String,Object>();
			param.put("cdns", datas);
			param.put("contentId",String.valueOf(worker.getContentId()));
			param.put("taskId", String.valueOf(worker.getTaskId()));
			param.put("cdns", datas);
			param.put("msgid", null);
			result.put("data", param);
			result.put("statusCode",ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION);
			result.put("message","错误信息为:【"+ e.toString()+"】获得的参数为:"+jsonData);
			String returnStr = JsonConverter.format(result);
			log.debug("返回结果为:" + returnStr);
			byte[] returnData = returnStr.getBytes();
			return returnData;
		}
		return null;
	}
	@SuppressWarnings("static-access")
	public static String readlUplod(String sourceUrl, String fileName, String contentId) throws Exception {
		boolean apacheSwitch = Boolean.valueOf(PropertiesUtil.getPropertiesUtil().get("apache.switch"));
		if (apacheSwitch) {
			String apachePath = "";
			String apacheHost = PropertiesUtil.getPropertiesUtil().get("apache.host");
			String apacheImgurl = PropertiesUtil.getPropertiesUtil().get("apache.imgurl");
			String nowTime = DateUtil.getNowTimeOnlyYMD();
			String toPath = "";
			if (apacheImgurl.startsWith(File.separator)) {
				apachePath = apacheHost + apacheImgurl + File.separator + nowTime + File.separator + contentId;
				toPath = apacheImgurl + File.separator + nowTime + File.separator + contentId;
			} else {
				apachePath = apacheHost + File.separator + apacheImgurl + File.separator + nowTime + File.separator + contentId;
				toPath = apacheImgurl + File.separator + nowTime + File.separator + contentId;
			}
			WorkerUtil.downloadFile(sourceUrl, toPath, fileName);
			String absoluteImgUrl = apachePath + File.separator + fileName;
			return absoluteImgUrl;
		} else {
			return sourceUrl;
		}
	}
}
