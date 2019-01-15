package cn.videoworks.gearman.function;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gearman.GearmanFunction;
import org.gearman.GearmanFunctionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author meishen
 * @version
 * @since Ver 1.1
 * @Date 2018 2018年6月17日 下午5:40:55
 * 
 * @see
 */
public class GZCDNInject implements GearmanFunction {
	private static final Logger log = LoggerFactory
			.getLogger(GZCDNInject.class);

	@SuppressWarnings("static-access")
	@Override
	public byte[] work(String arg0, byte[] data, GearmanFunctionCallback callback) throws Exception {
		
			log.info("publishworker 开始获取ftp参数!");
			String port = PropertiesUtil.getPropertiesUtil().get("ftp.port");// ftp
			String userName = PropertiesUtil.getPropertiesUtil().get("ftp.username");// ftp 用户名
			String password = PropertiesUtil.getPropertiesUtil().get("ftp.password");// 密码
			String xmlMkir = PropertiesUtil.getPropertiesUtil().get("ftp.xmlmkir");// xml存放路径
			log.info("publishworker 开始获取ftp参数:xmlMkir=" + xmlMkir);
			String moviePath = PropertiesUtil.getPropertiesUtil().get("ftp.moviepath");// 视频路径
			log.info("publishworker 开始获取ftp参数:moviePath=" + moviePath);
			String imagePath = PropertiesUtil.getPropertiesUtil().get("ftp.imagepath");// 图片路径
			log.info("publishworker 开始获取ftp参数:imagePath=" + imagePath);
			String host = PropertiesUtil.getPropertiesUtil().get("ftp.host");// ftp
			log.info("publishworker 开始获取ftp参数:host=" + host); // 路径
			String ip = PropertiesUtil.getPropertiesUtil().get("ftp.ip");// ftp
			log.info("publishworker 开始获取ftp参数:ip=" + ip); // 路径
			String jsonData = new String(data);
			log.info("publishworker 收到的任务=" + jsonData); // byte[]
			// 获取ftp相关参数
			log.info("publishworker 获取ftp参数结束!");
			Map<String, Object> result = new HashMap<String, Object>();
			List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
			// 上传的路径
			String datePath = String.valueOf(new Date().getTime());
			// 解析参数
			WorkerDto worker = JsonConverter.parse(jsonData, WorkerDto.class);
		try {
			log.info("传入数据转成class成功!");
			// 将视频/海报注入ftp
			boolean success = false;
			List<MovieDto> movies = null;
			List<ImageDto> images = null;
			if (worker != null) {
				try {
					if(worker.getTaskId()==null||worker.getTaskId().equals("")){
						log.info("传输的任务id----taskId为空!");
					}
					// 视频
					movies = worker.getMovies();
					// 海报
					images = worker.getImages();
					boolean isCdn = true;
					if (worker.getIsUploadCdn() != null) {
						if (worker.getIsUploadCdn().intValue() == 0) {
							isCdn = false;// 不上传cdn
							log.info("視頻不需要传入cdn!");
						}
					}
					if (isCdn) {
						log.info("需要上传cdn");
						if (movies != null && movies.size() > 0) {
							for (MovieDto movie : movies) {
								if (movie.getUrl() != null) {
									// String image =
									boolean isFile = WorkerUtil.existHttpPath(movie.getUrl());
									log.info("视频路径是否是文件：" + isFile);
									// 文件存在
									if (isFile) {
										InputStream input = WorkerUtil.getFileStream(movie.getUrl());
										// 拼接ftp上传路径
										String moviePaths = moviePath;
										success = WorkerUtil.uploadFile(ip, userName, password, moviePaths, movie.getFilename(), input);
										if (success) {
											log.info("视频名[" + movie.getFilename() + "]上传成功!");
											String filepath = host + "/" + moviePaths + "/" + movie.getFilename();
											log.info("视频 ftp地址:" + filepath);
											movie.setFtpUlr(filepath);
											Map<String, Object> re = new HashMap<String, Object>();
											re.put("id", movie.getId());
											re.put("cdn_key", filepath);
											re.put("type", 1);
											datas.add(re);
										} else {
											Map<String, Object> param = new HashMap<String, Object>();
											param.put("msgid", datePath);
											param.put("cdns", datas);
											param.put("contentId",String.valueOf(worker.getContentId()));
											param.put("taskId", String.valueOf(worker.getTaskId()));
											result.put("statusCode", ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION);
											result.put("message", "視頻ftp上传失败");
											result.put("data", param);
											String returnStr = JsonConverter.format(result);
											log.info("返回结果为:" + returnStr);
											byte[] returnData = returnStr.getBytes();
											return returnData;
										}
									} else {
										Map<String, Object> param = new HashMap<String, Object>();
										param.put("msgid", datePath);
										param.put("cdns", datas);
										param.put("contentId",String.valueOf(worker.getContentId()));
										param.put("taskId", String.valueOf(worker.getTaskId()));
										result.put("statusCode",ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION);
										result.put("message", "视频地址错误");
										result.put("data", param);
										String returnStr = JsonConverter.format(result);
										log.info("返回结果为:" + returnStr);
										byte[] returnData = returnStr.getBytes();
										return returnData;
									}
								} else {
									Map<String, Object> param = new HashMap<String, Object>();
									param.put("msgid", datePath);
									param.put("cdns", datas);
									param.put("contentId",String.valueOf(worker.getContentId()));
									param.put("taskId", String.valueOf(worker.getTaskId()));
									result.put("statusCode",ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION);
									result.put("message", "视频地址为空");
									result.put("data", param);
									String returnStr = JsonConverter.format(result);
									log.info("返回结果为:" + returnStr);
									byte[] returnData = returnStr.getBytes();
									return returnData;
								}
							}
						} else {
							Map<String, Object> param = new HashMap<String, Object>();
							param.put("msgid", datePath);
							param.put("cdns", datas);
							param.put("contentId",String.valueOf(worker.getContentId()));
							param.put("taskId", String.valueOf(worker.getTaskId()));
							result.put("statusCode",ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION);
							result.put("message", "视频不存在");
							result.put("data", param);
							String returnStr = JsonConverter.format(result);
							log.info("返回结果为:" + returnStr);
							byte[] returnData = returnStr.getBytes();
							return returnData;
						}
						log.info("publishworker 视频上传end");
						if (success) {
							if (images != null && images.size() > 0) {
								for (ImageDto image : images) {
									if (image.getUrl() != null) {
										boolean isFile = WorkerUtil.existHttpPath(image.getUrl());
										log.info("是否是文件：" + isFile);
										// 文件存在
										if (isFile) {
											InputStream input = WorkerUtil.getFileStream(image.getUrl());
											String imagePaths = imagePath;
											success = WorkerUtil.uploadFile(ip, userName, password, imagePaths, image.getFilename(), input);
											Map<String, Object> re = new HashMap<String, Object>();
											// 图片上传阿帕奇
											String apaUrl = readlUplod(image.getUrl(), image.getFilename(), String.valueOf(worker.getContentId()));
											log.info("图片阿帕奇的地址为:" + apaUrl);
											if (success) {
												log.info("海报名["+ image.getFilename()+ "]上传成功!");
												String filepath = host + "/"+ imagePaths + "/"+ image.getFilename();
												log.info("图片ftp地址:" + filepath);
												image.setFtpUrl(filepath);
												re.put("id", image.getId());
												re.put("cdn_key", apaUrl);
												re.put("file_name",image.getFilename());
												re.put("type", 2);
												datas.add(re);
											} else {
												Map<String, Object> param = new HashMap<String, Object>();
												param.put("msgid", datePath);
												param.put("cdns", datas);
												param.put("contentId",String.valueOf(worker.getContentId()));
												param.put("taskId", String.valueOf(worker.getTaskId()));
												result.put("statusCode",ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION);
												result.put("message", "海報ftp上传失败");
												result.put("data", param);
												String returnStr = JsonConverter.format(result);
												log.info("返回结果为:" + returnStr);
												byte[] returnData = returnStr.getBytes();
												return returnData;
											}
										} else {
											Map<String, Object> param = new HashMap<String, Object>();
											param.put("msgid", datePath);
											param.put("cdns", datas);
											param.put("contentId",String.valueOf(worker.getContentId()));
											param.put("taskId", String.valueOf(worker.getTaskId()));
											result.put("statusCode",ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION);
											result.put("message", "图片地址错误");
											result.put("data", param);
											String returnStr = JsonConverter.format(result);
											log.info("返回结果为:" + returnStr);
											byte[] returnData = returnStr.getBytes();
											return returnData;
										}
									} else {
										Map<String, Object> param = new HashMap<String, Object>();
										param.put("msgid", datePath);
										param.put("cdns", datas);
										param.put("contentId",String.valueOf(worker.getContentId()));
										param.put("taskId", String.valueOf(worker.getTaskId()));
										result.put("statusCode",ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION);
										result.put("message", "图片地址为空");
										result.put("data", param);
										String returnStr = JsonConverter.format(result);
										log.info("返回结果为:" + returnStr);
										byte[] returnData = returnStr.getBytes();
										return returnData;
									}
								}
							}
							/*else {
								Map<String, Object> param = new HashMap<String, Object>();
								param.put("msgid", datePath);
								param.put("cdns", datas);
								param.put("contentId",String.valueOf(worker.getContentId()));
								param.put("taskId", String.valueOf(worker.getTaskId()));
								result.put("statusCode",ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION);
								result.put("message", "图片不存在");
								result.put("data", param);
								String returnStr = JsonConverter.format(result);
								log.info("返回结果为:" + returnStr);
								byte[] returnData = returnStr.getBytes();
								return returnData;
							}*/
						}
						log.info("publishworker 海报上传end");
						log.info("ftp上传数据end");
						// 想cdn注入请求
						if (success) {
							String xml = WorkerUtil.getXml(worker);
//							log.debug("**************工单组装完成，开始注入cdn,工单格式："+xml);
							Map<String, Object> cdnResult = WorkerUtil.recnd(datePath, xml);
							if (cdnResult.containsKey("ret")) {
								String code = String.valueOf(cdnResult.get("ret"));
								if (code.equals("0")) {
									result.put("statusCode", 100009);
									result.put("message", "注入成功!");
									// 工单id
									Map<String, Object> param = new HashMap<String, Object>();
									param.put("msgid", datePath);
									param.put("cdns", datas);
									param.put("contentId", String.valueOf(worker.getContentId()));
									param.put("taskId", String.valueOf(worker.getTaskId()));
									result.put("data", param);
								} else {
									Map<String, Object> param = new HashMap<String, Object>();
									param.put("msgid", datePath);
									param.put("cdns", datas);
									param.put("contentId",String.valueOf(worker.getContentId()));
									param.put("taskId", String.valueOf(worker.getTaskId()));
									result.put("statusCode",ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION);
									result.put("data", param);
									result.put("message", result.get("reason"));
								}
							}
						}
						String returnStr = JsonConverter.format(result);
						log.info("返回结果为:" + returnStr);
						byte[] returnData = returnStr.getBytes();
						return returnData;
					} else {
						if (images != null && images.size() > 0) {
							for (ImageDto image : images) {
								if (image.getUrl() != null) {
									boolean isFile = WorkerUtil.existHttpPath(image.getUrl());
									log.info("是否是文件：" + isFile);
									// 文件存在
									if (isFile) {
										Map<String, Object> re = new HashMap<String, Object>();
										// 图片上传阿帕奇
										String apaUrl = readlUplod(image.getUrl(),image.getFilename(),String.valueOf(worker.getContentId()));
										log.info("图片阿帕奇的地址为:" + apaUrl);
										re.put("id", image.getId());
										re.put("cdn_key", apaUrl);
										re.put("file_name", image.getFilename());
										re.put("type", 2);
										datas.add(re);
									} else {
										result.put("statusCode",ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION);
										result.put("message", "图片地址错误");
										Map<String, Object> param = new HashMap<String, Object>();
										param.put("msgid", datePath);
										param.put("cdns", datas);
										param.put("contentId",String.valueOf(worker.getContentId()));
										param.put("taskId", String.valueOf(worker.getTaskId()));
										result.put("data", param);
										String returnStr = JsonConverter.format(result);
										log.info("返回结果为:" + returnStr);
										byte[] returnData = returnStr.getBytes();
										return returnData;
									}
								}
							}
							result.put("statusCode", 100000);
							result.put("message", "注入成功!");
							// 工单id
							Map<String, Object> param = new HashMap<String, Object>();
							param.put("msgid", datePath);
							param.put("cdns", datas);
							param.put("contentId",String.valueOf(worker.getContentId()));
							param.put("taskId", String.valueOf(worker.getTaskId()));
							result.put("data", param);
							String returnStr = JsonConverter.format(result);
							log.info("返回结果为:" + returnStr);
							byte[] returnData = returnStr.getBytes();
							return returnData;
						}
					}
				} catch (Exception e) {
					Map<String, Object> param = new HashMap<String, Object>();
					param.put("msgid", datePath);
					param.put("cdns", datas);
					param.put("contentId",String.valueOf(worker.getContentId()));
					param.put("taskId", String.valueOf(worker.getTaskId()));
					result.put("data", param);
					result.put("statusCode",ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION);
					result.put("message","错误信息为:【"+ e.getMessage()+"】获得的参数为:"+jsonData);
					String returnStr = JsonConverter.format(result);
					log.info("返回结果为:" + returnStr);
					byte[] returnData = returnStr.getBytes();
					return returnData;
				}
			}
		} catch (Exception e) {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("msgid", datePath);
			param.put("cdns", datas);
			if (worker != null) {
				param.put("contentId", String.valueOf(worker.getContentId()));
				param.put("taskId", String.valueOf(worker.getTaskId()));
			}
			result.put("data", param);
			result.put("statusCode",ResponseDictionary.EXTERNALINTERFACECALLSEXCEPTION);
			result.put("message","错误信息为:【"+ e.getMessage()+"】获得的参数为:"+jsonData);
			String returnStr = JsonConverter.format(result);
			log.info("返回结果为:" + returnStr);
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
