package cn.videoworks.gearman.work;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import cn.videoworks.gearman.client.SXFTPUtilClient;
import cn.videoworks.gearman.common.ParameterMap;
import cn.videoworks.gearman.util.FileUtil;
import cn.videoworks.gearman.util.PropertiesUtil;
import cn.videoworks.gearman.util.WxMappingJackson2HttpMessageConverter;

/**
 * shan'xi
 * @author whl
 *
 */
public class SXMonitorXMLWorker {
	private static Logger log = LoggerFactory.getLogger(SXMonitorXMLWorker.class);
   
    @SuppressWarnings("static-access")
	public static void registWorker() {
    	Timer timer = new Timer();
    	String ftpRootDir =null;
    	String  ftpResultaDir = null;
		try {
			ftpRootDir = PropertiesUtil.getPropertiesUtil().get("sx.ftp.rootDir");
			ftpResultaDir = PropertiesUtil.getPropertiesUtil().get("sx.ftp.result.rootDir");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		final String  monitorDir = ftpRootDir.trim()+"/"+ftpResultaDir.trim();
		final SXFTPUtilClient ftpClient_util = SXFTPUtilClient.getInstance();
		try {
			boolean connect = ftpClient_util.connect();
			if(connect) {
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
//				WatchKey key;
//				try {
//					WatchSe vice watchService  = FileSystems.getDefault().newWatchService();
//					Paths.get(monitorDir).register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
//					while(true) {
//						File file = new File(monitorDir);//monitor为监听的文件夹
//						File[] files = file.listFiles();
//						key = watchService.take(); //么有文件增加，阻塞在这里
//						for(WatchEvent<?> event: key.pollEvents()) {
//							String fileName = monitorDir+"/"+event.context();
//							File file1 = files[files.length-1];
//							byte[] byte1 = FileUtil.getByte(file1);
//							String resultXml = new String(byte1);
//							Map<String, String> xmlMap_result = parseXml(resultXml);
//							callbackCMS(xmlMap_result);
//							
//						}
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
					
				//检测ftp目录
						
						FTPClient ftpClient= 	ftpClient_util.getFtpClient();	
						try {
						ftpClient.changeWorkingDirectory(monitorDir);
							FTPFile[] listFiles = ftpClient.listFiles();
							for (FTPFile ftpFile : listFiles) {
								String fileName = ftpFile.getName();
								String suffix = FileUtil.suffix(fileName);
								if(suffix.equals("xml")) {
									log.info("陕西worker开始读取result的xml文件="+fileName);
									InputStream retrieveFileStream = ftpClient.retrieveFileStream(fileName);
									byte[] bytes = new byte[0];
									bytes = new byte[retrieveFileStream.available()];
									retrieveFileStream.read(bytes);
									String resultXml = new String(bytes,"UTF-8");
									
									String renameFileName= fileName+".finish";
									boolean rename = ftpClient.rename(fileName,renameFileName );
									log.info("陕西worker 是否重命名了文件【"+fileName+"-->"+";flag="+rename);
									if(retrieveFileStream!=null) {
										retrieveFileStream.close();
									}
									
									Map<String, String> xmlMap_result = parseXml(resultXml);
									callbackCMS(xmlMap_result);
								}
								
								
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}, 100, 1000*20);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    }
    
    public static String readFile(FTPFile ftpFile,FTPClient ftpClient)  {
        InputStream ins = null;
        StringBuilder builder = null;
        try {
            // 从服务器上读取指定的文件
            ins = ftpClient.retrieveFileStream(ftpFile.getName());
			BufferedReader reader = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
            String line;
            builder = new StringBuilder(150);
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
                builder.append(line);
            }
            reader.close();
            if (ins != null) {
                ins.close();
            }
            // 主动调用一次getReply()把接下来的226消费掉. 这样做是可以解决这个返回null问题
            ftpClient.getReply();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String string = builder.toString();
        System.out.println("--------读取的文件内容------"+string);
        return string;
    }

    /**
     * 解析cip生成的xml结果
     * @param xmlStrs
     * @return
     */
    public static Map<String,String> parseXml(String xmlStrs){
    	HashMap<String, String> hashMap = new HashMap<String,String>();
    	Document document = null;
    	if(StringUtils.isNotBlank(xmlStrs)) {
    		try {
				document = DocumentHelper.parseText(xmlStrs);
			} catch (DocumentException e) {
				log.info("陕西worker 解析生成的xml文件失败");
				e.printStackTrace();
			}
    		
    		Element rootElement = document.getRootElement();
    		for (Iterator i = rootElement.elementIterator(); i.hasNext();) {
    			Element element = (Element) i.next();
    			if(element.getName().equals("content")) {
    				String playUrl= element.attributeValue("PlayURL");
    				String contentId = element.attributeValue("ContentId");
    				hashMap.put("msgId", contentId);
    				hashMap.put("part_id", playUrl);
    			}
    		}
    		
    	}
    	
    	return hashMap;
    }
    /**
     * 回调cms的cdn回调，xml
     * <?xml version='1.0' encoding='utf-8' ?><xmlresult><msgid>111</msgid><state>1</state><action>1</action><msg>cdn  import ok</msg><info><asset_type>3</asset_type><asset_id>CMSSERI111</asset_id><part_id>222</part_id></info></xmlresult>
     * @param xmlMap_result
     */
    public static void callbackCMS(Map<String, String> xmlMap_result){
    	String  msgId = String.valueOf( xmlMap_result.get("msgId"));
    	String  part_id = String.valueOf( xmlMap_result.get("part_id"));
    	StringBuffer sb = new StringBuffer();
    	sb.append("<?xml version='1.0' encoding='utf-8' ?>");
    	sb.append("<xmlresult>");
    		sb.append("<msgid>"+msgId+"</msgid>");
    		sb.append("<state>1</state>");
    		sb.append("<action>1</action>");
    		sb.append("<msg>cdn  import ok</msg>");
    		sb.append("<info>");
    			sb.append("<asset_type>3</asset_type>");
    			sb.append("<asset_id>"+"CMSSERI"+msgId+"</asset_id>");
    			sb.append("<part_id>"+part_id+"</part_id>");
    		sb.append("</info>");
    	sb.append("</xmlresult>");
    	String cmsResult = sb.toString();
    	realCallbackCms(cmsResult);
    	
    }
    public static void realCallbackCms(String cmsResult) {
    	HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    	RestTemplate restTemplate = new RestTemplate();
    	restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());
    	
    	 MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
    	 params.add("cmsresult", cmsResult);
    	 HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
    			 params, headers);
          //发送请求，设置请求返回数据格式为String
    	try {
			String cms_callback_url=  PropertiesUtil.getPropertiesUtil().get("sx.cms.cdnCallbackUrl");
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(cms_callback_url, request, String.class);
			String body = responseEntity.getBody();
			log.info("陕西worker 回调cms 后的返回结果：【"+body+"】");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
                 
    }
    public static void main(String[] args) {
    	ParameterMap.getParameterMap("D:\\gearman.properties");
    	HashMap<String, String> hashMap = new HashMap<>();
    	hashMap.put("msgId", "111");
    	hashMap.put("part_id", "222");
    	callbackCMS(hashMap);
	}
}

