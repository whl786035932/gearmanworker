package cn.videoworks.gearman.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.videoworks.gearman.dto.WorkerDto;

public class WorkerUtil {
	private static final Logger log = LoggerFactory.getLogger(WorkerUtil.class);

	/**
	 * Description: 向FTP服务器上传文件
	 * 
	 * @param ip
	 *            FTP服务器hostname
	 * @param username
	 *            FTP登录账号
	 * @param password
	 *            FTP登录密码
	 * @param path
	 *            FTP服务器保存目录
	 * @param filename
	 *            上传到FTP服务器上的文件名
	 * @param input
	 *            输入流
	 * @return 成功返回true，否则返回false
	 */
	public static boolean uploadFile(String ip, String username,
			String password, String path, String filename, InputStream input) {
		boolean success = false;
		log.info("ftp ip=" + ip);
		log.info("ftp username=" + username);
		log.info("ftp password=" + password);
		FTPClient ftp = new FTPClient();
		try {
			int reply;
			ftp.connect(ip);// 连接FTP服务器
			log.info("ftp 连接成功！");
			// 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
			ftp.login(username, password);// 登录
			log.info("ftp 登录成功！");
			ftp.setFileType(FTP.BINARY_FILE_TYPE);// 登陆后设置文件类型为二进制否则可能导致乱码文件无法打开
			ftp.setControlEncoding("UTF-8"); // 设置格式
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return success;
			}
			if (!ftp.changeWorkingDirectory(path)) {
				// 如果不能进入dir下，说明此目录不存在！
				StringTokenizer s = new StringTokenizer(path, "/"); // sign
				String pathName = "";
				while (s.hasMoreElements()) {
					pathName = pathName + "/" + (String) s.nextElement();
					ftp.mkd(pathName);
				}
			}
			ftp.changeWorkingDirectory(path);
			success = ftp.storeFile(filename, input);
			input.close();
			ftp.logout();
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (Exception ioe) {
					ioe.printStackTrace();
				}
			}
		}
		// 上传文件
		return success;
	}

	/**
	 * 判断url是否为视频文件.
	 * 
	 * @param httpPath
	 * @return
	 */
	public static boolean existHttpPath(String httpPath) {
		URL httpurl = null;
		try {
			httpurl = new URL(httpPath);
			URLConnection rulConnection = httpurl.openConnection();
			rulConnection.getInputStream();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 通过url获取输入流.
	 * 
	 * @param url
	 *            文件 http url
	 * @return inputStream 输入流数据
	 */
	public static InputStream getFileStream(String url) {
		// 得到输入流
		InputStream inputStream = null;
		HttpURLConnection conn = null;
		try {
			// 测试数据
			// url =
			URL urls = new URL(url);
			conn = (HttpURLConnection) urls.openConnection();
			log.debug("远程连接成功");
			// 设置超时间为3秒
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);// 使用 URL 连接进行输入
			conn.setUseCaches(false);// 忽略缓存
			// 防止屏蔽程序抓取而返回403错误
			conn.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			inputStream = conn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return inputStream;
	}
	
	/**
	 * 工单生成
	 * @param worker
	 * @return
	 */
	public static String getXml(WorkerDto worker) {
	StringBuffer xmlStr = new StringBuffer();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmSSS");
	String seri = "QNES";
	String prog ="QNES";
	String mo = "QNES";
	String pi = "QNES"+sdf.format(new Date());
	if (worker != null) {
		xmlStr.append("<?xml version='1.0' encoding='UTF-8'?><ADI xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><Objects>");
		// Series Object
		xmlStr.append("<Object ElementType='Series' ID='");
		// ID
		seri += sdf.format(new Date()) + 0;
//		xmlStr.append("CMSSERI");
//		xmlStr.append(worker.getContentId());
		xmlStr.append(seri);
		xmlStr.append("'");
		// action
		xmlStr.append(" Action='");
		xmlStr.append("REGIST");
		xmlStr.append("'");
		// code
		xmlStr.append(" Code='");
//		xmlStr.append("CMSSERI");
//		xmlStr.append(worker.getContentId());
		xmlStr.append(seri);
		xmlStr.append("'>");

		if (worker.getTitle() != null) {
			xmlStr.append("<Property Name='Name'>");
			xmlStr.append(worker.getTitle());
			xmlStr.append("</Property>");
		} else {
			xmlStr.append("<Property Name='Name'></Property>");
		}
		xmlStr.append("<Property Name='OrderNumber'/>");
		xmlStr.append("<Property Name='OriginalName'/>");
		xmlStr.append("<Property Name='Sort'/>");
		xmlStr.append("<Property Name='KeyWords'>");
//		if (worker.getTags() != null && worker.getTags().size() > 0) {
//			for (String tag : worker.getTags()) {
//				xmlStr.append(tag);
//				xmlStr.append(" ");
//			}
//		}
		xmlStr.append("</Property>");
		
		//组织智能推荐需要的数据 kind 以/分隔， ViewType = 13  Type='新闻'
		xmlStr.append("<Property Name='Kind'>");
		if (worker.getTags() != null && worker.getTags().size() > 0) {
			xmlStr.append(StringUtils.join(worker.getTags(), "/"));
		}
		xmlStr.append("</Property>");
		
		xmlStr.append("<Property Name='Type'>新闻</Property>");
		xmlStr.append("<Property Name='ViewType'>13</Property>");
		
		
		xmlStr.append("<Property Name='ActorDisplay'/>");
		xmlStr.append("<Property Name='WriterDisplay'/>");
		xmlStr.append("<Property Name='OriginalCountry'/>");
		xmlStr.append("<Property Name='ReleaseYear'/>");
		xmlStr.append("<Property Name='OrgAirDate'/>");
		xmlStr.append("<Property Name='LicensingWindowStart'/>");
		xmlStr.append("<Property Name='LicensingWindowEnd'/>");
		xmlStr.append("<Property Name='Macrovision'/>");
		xmlStr.append("<Property Name='Description'>");
		xmlStr.append(worker.getDescription());
		xmlStr.append("</Property>");
		xmlStr.append("<Property Name='PriceTaxIn'/>");
		xmlStr.append("<Property Name='Status'>1</Property>");
		xmlStr.append("<Property Name='SourceType'>1</Property>");
		String type = "";
		switch (worker.getType()) {
		case 1:
			type = "视频";
			break;
		case 2:
			type = "广告";
			break;
		default:
			break;
		}// 类型
		xmlStr.append("<Property Name='Type'>");
		xmlStr.append(type);
		xmlStr.append("</Property>");
		xmlStr.append("<Property Name='Tags'>");
//		if (worker.getTags() != null && worker.getTags().size() > 0) {
//			for (String tag : worker.getTags()) {
//				xmlStr.append(tag);
//				xmlStr.append(" ");
//			}
//		}
		xmlStr.append("</Property>");
		xmlStr.append("<Property Name='ContentProvider'>100590</Property>");
		xmlStr.append("<Property Name='Duration'>");
		xmlStr.append(worker.getDuration());
		xmlStr.append("</Property>");
		xmlStr.append("<Property Name='PlayCount'/>");
		xmlStr.append("<Property Name='ShowType'/>");
		xmlStr.append("<Property Name='NewCount'/>");
		xmlStr.append("<Property Name='AliasName'/>");
		xmlStr.append("<Property Name='EnglishName'/>");
		xmlStr.append("<Property Name='ParentCategoryName'/>");
		xmlStr.append("<Property Name='ParentCategoryId'/>");
		xmlStr.append("<Property Name='TypeId'/>");
		xmlStr.append("<Property Name='Language'>");
		xmlStr.append("Chinese");
		xmlStr.append("</Property>");
		xmlStr.append("<Property Name='OnlineIdentify'>");
		xmlStr.append("网博视界贵州");
		xmlStr.append("</Property>");
		xmlStr.append("</Object>");
		/*** Program Object **/
		xmlStr.append("<Object ElementType='Program' ID='");
		// ID
		prog += sdf.format(new Date()) + 1;
//		xmlStr.append("CMSPROG");
//		xmlStr.append(worker.getContentId());
		xmlStr.append(prog);
		xmlStr.append("'");
		// action
		xmlStr.append(" Action='");
		xmlStr.append("REGIST");
		xmlStr.append("'");
		// code
		xmlStr.append(" Code='");
//		xmlStr.append("CMSPROG");
//		xmlStr.append(worker.getContentId());
		xmlStr.append(prog);
		xmlStr.append("'>");
		xmlStr.append("<Property Name='Name'>");
		if (worker.getTitle() != null) {
			xmlStr.append(worker.getTitle());
		}
		xmlStr.append("</Property>");
		xmlStr.append("<Property Name='OrderNumber'/>");
		xmlStr.append("<Property Name='OriginalName'/>");
		xmlStr.append("<Property Name='SortName'/>");
		xmlStr.append("<Property Name='KeyWords'/>");
		xmlStr.append("<Property Name='ActorDisplay'/>");
		xmlStr.append("<Property Name='WriterDisplay'/>");
		xmlStr.append("<Property Name='OriginalCountry'/>");
		xmlStr.append("<Property Name='Language'/>");
		xmlStr.append("<Property Name='ReleaseYear'/>");
		xmlStr.append("<Property Name='OrgAirDate'/>");
		xmlStr.append("<Property Name='LicensingWindowStart'/>");
		xmlStr.append("<Property Name='LicensingWindowEnd'/>");
		xmlStr.append("<Property Name='Macrovision'>");
		xmlStr.append("0");
		xmlStr.append("</Property>");
		xmlStr.append("<Property Name='Description'>");
		xmlStr.append(worker.getDescription());
		xmlStr.append("</Property>");
		xmlStr.append("<Property Name='Status'>");
		xmlStr.append("1");
		xmlStr.append("</Property>");
		xmlStr.append("<Property Name='SeriesFlag'>");
		xmlStr.append("0");
		xmlStr.append("</Property>");
		xmlStr.append("<Property Name='Type'>");
		xmlStr.append("新闻");
		xmlStr.append("</Property>");
		xmlStr.append("<Property Name='Tags'>");
//		if (worker.getTags() != null && worker.getTags().size() > 0) {
//			for (String tag : worker.getTags()) {
//				xmlStr.append(tag);
//				xmlStr.append(" ");
//			}
//		}
		xmlStr.append("</Property>");
		xmlStr.append("<Property Name='ContentProvider'>");
		xmlStr.append("100590");
		xmlStr.append("</Property>");
		xmlStr.append("<Property Name='Duration'>");
		xmlStr.append(worker.getDuration());
		xmlStr.append("</Property>");
		xmlStr.append("<Property Name='PlayCount'/>");
		xmlStr.append("<Property Name='Score'/>");
		xmlStr.append("<Property Name='WatchFocus'/>");
		xmlStr.append("<Property Name='CPContentID'>");
		xmlStr.append("100590");
		xmlStr.append("</Property>");
		xmlStr.append("</Object>");
		/*** Program Movie **/
		if (worker.getMovies() != null) {
			xmlStr.append("<Object ElementType='Movie' ID='");
			// ID
			mo += sdf.format(new Date())+1;
//			xmlStr.append("CMSMO00");
//			xmlStr.append(String.valueOf(worker.getMovies().get(0).getId()));
			xmlStr.append(mo);
			xmlStr.append("'");
			// action
			xmlStr.append(" Action='");
			xmlStr.append("REGIST");
			xmlStr.append("'");
			// code
			xmlStr.append(" Code='");
//			xmlStr.append("CMSMO00");
//			xmlStr.append(String.valueOf(worker.getMovies().get(0).getId()));
			xmlStr.append(mo);
			xmlStr.append("'>");
			xmlStr.append("<Property Name='Type'>");
			xmlStr.append("1");
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='FileURL'>");
			xmlStr.append(worker.getMovies().get(0).getFtpUlr());
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='Name'>");
			xmlStr.append(worker.getMovies().get(0).getFilename());
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='SourceDRMType'>");
			xmlStr.append("0");
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='DestDRMType'>");
			xmlStr.append("0");
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='AudioType'>");
			xmlStr.append("1");
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='ScreenFormat'>");
			xmlStr.append("2");
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='ClosedCaptioning'>");
			xmlStr.append("0");
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='Tag'/>");
			xmlStr.append("<Property Name='Duration'>");
			xmlStr.append(worker.getDuration());
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='FileSize'/>");
			xmlStr.append("<Property Name='BitRateType'>");
			xmlStr.append(worker.getMovies().get(0).getBitrate());
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='MediaType'/>");
			xmlStr.append("<Property Name='Resolution'>");
			xmlStr.append("720x576");
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='MediaMode'>");
			xmlStr.append("2");
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='SystemLayer'>");
			xmlStr.append("1");
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='ServiceType'>");
			xmlStr.append("1");
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='ShowType'>");
			xmlStr.append("1");
			xmlStr.append("</Property>");
			xmlStr.append("<Property Name='mediaProto'/>");
			xmlStr.append("<Property Name='LiveID'/>");
			xmlStr.append("<Property Name='BeginTime'/>");
			xmlStr.append("<Property Name='CPContentID'>");
			xmlStr.append("100590");
			xmlStr.append("</Property>");
			xmlStr.append("</Object>");
		}
		if (worker.getImages() != null) {
			for (int i = 0; i < worker.getImages().size(); i++) {

				/*** Program Picture **/
				xmlStr.append("<Object ElementType='Picture' ID='");
				// ID
//				xmlStr.append("CMSPI00");
//				xmlStr.append(String.valueOf(worker.getImages().get(i).getId()));
				xmlStr.append(pi+i);
				xmlStr.append("'");
				// action
				xmlStr.append(" Action='");
				xmlStr.append("REGIST");
				xmlStr.append("'");
				// code
				xmlStr.append(" Code='");
//				xmlStr.append("CMSPI00");
//				xmlStr.append(String.valueOf(worker.getImages().get(i).getId()));
				xmlStr.append(pi+1);
				xmlStr.append("'>");

				xmlStr.append("<Property Name='FileURL'>");
				xmlStr.append(worker.getImages().get(i).getFtpUrl());
				xmlStr.append("</Property>");
				xmlStr.append("<Property Name='Type'>");
				xmlStr.append(4);
				xmlStr.append("</Property>");
				xmlStr.append("</Object>");
				// </Objects>结束
			}
		}
		xmlStr.append("</Objects>");
		/** mappings* **/
		xmlStr.append(" <Mappings>");
		// Program与Series关系对象
		xmlStr.append(" <Mapping ParentType='Series' ParentID='");
//		xmlStr.append("CMSSERI");
//		xmlStr.append(worker.getContentId());
		xmlStr.append(seri);
		xmlStr.append("'");
		xmlStr.append(" ParentCode='");
//		xmlStr.append("CMSSERI");
//		xmlStr.append(worker.getContentId());
		xmlStr.append(seri);
		xmlStr.append("'");
		xmlStr.append(" ElementType='");
		xmlStr.append("Program");
		xmlStr.append("'");
		xmlStr.append(" ElementCode='");
//		xmlStr.append("CMSPROG");
//		xmlStr.append(worker.getContentId());
		xmlStr.append(prog);
		xmlStr.append("'");
		xmlStr.append(" ElementID='");
//		xmlStr.append("CMSPROG");
//		xmlStr.append(worker.getContentId());
		xmlStr.append(prog);
		xmlStr.append("'");
		xmlStr.append(" Action='REGIST'>");
		xmlStr.append("<Property Name='Sequence'>0</Property>");
		xmlStr.append("</Mapping>");
		// Movie与Program关系对象
		xmlStr.append(" <Mapping ParentType='Program' ParentID='");
//		xmlStr.append("CMSPROG");
//		xmlStr.append(worker.getContentId());
		xmlStr.append(prog);
		xmlStr.append("'");
		xmlStr.append(" ParentCode='");
//		xmlStr.append("CMSPROG");
//		xmlStr.append(worker.getContentId());
		xmlStr.append(prog);
		xmlStr.append("'");
		xmlStr.append(" ElementType='");
		xmlStr.append("Movie");
		xmlStr.append("'");
		xmlStr.append(" ElementID='");
//		xmlStr.append("CMSMO00");
//		xmlStr.append(String.valueOf(worker.getMovies().get(0).getId()));
		xmlStr.append(mo);
		xmlStr.append("'");
		xmlStr.append(" ElementCode='");
//		xmlStr.append("CMSMO00");
//		xmlStr.append(String.valueOf(worker.getMovies().get(0).getId()));
		xmlStr.append(mo);
		xmlStr.append("'");
		xmlStr.append(" Action='REGIST' />");
		if (worker.getImages() != null) {
			for (int i = 1; i < worker.getImages().size(); i++) {
				// Pictuer与Series关系对象
				xmlStr.append(" <Mapping ParentType='Picture' ParentID='");
//				xmlStr.append("CMSPI00");
//				xmlStr.append(String.valueOf(worker.getImages().get(i)
//						.getId()));
				xmlStr.append(pi+i);
				xmlStr.append("'");
				xmlStr.append(" ElementType='Series'");
				xmlStr.append(" ElementID='");
//				xmlStr.append("CMSSERI");
//				xmlStr.append(worker.getContentId());
				xmlStr.append(seri);
				xmlStr.append("'");
				xmlStr.append(" ParentCode='");
//				xmlStr.append("CMSPI00");
//				xmlStr.append(String.valueOf(worker.getImages().get(i).getId()));
				xmlStr.append(pi+i);
				xmlStr.append("'");
				xmlStr.append(" ElementCode='");
//				xmlStr.append("CMSSERI");
//				xmlStr.append(worker.getContentId());
				xmlStr.append(seri);
				xmlStr.append("'");
				xmlStr.append(" Action='REGIST'>");
				if (i == 0) {
					xmlStr.append("<Property Name='Type'>0</Property>");
				} else {
					xmlStr.append("<Property Name='Type'>4</Property>");
				}
				xmlStr.append("</Mapping>");
			}
		}
		xmlStr.append("</Mappings>");
		xmlStr.append(" </ADI>");
	}
	String xml = xmlStr.toString();
	Document doc = null;
	try {
		doc = DocumentHelper.parseText(xml);
	} catch (DocumentException e) {
		e.printStackTrace();
	}
	String XML = doc.asXML();
	return XML;
}

	/**
	 * 向cdn注入请求.
	 */
	@SuppressWarnings({ "static-access" })
	public static Map<String, Object> recnd(String fileName, String xml) {
		Map<String, Object> resultp = null;
		// 拼接参数
		try {
			String cdnIp = PropertiesUtil.getPropertiesUtil().get("cdn.host");
			//String cdnIp = "10.21.16.42";
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("msgid", fileName);
			param.put("cpid", "100590");
			param.put("xml", xml);
			String result = HttpUtil.httpPost("http://" + cdnIp
					+ "/nn_gzgdbk/api_v2/import/http_import_v2.php", param);
			log.info("cdn返回结果：" + result);
			System.out.println("cdn同步返回结果：" + result);
			if (result != null) {
				Document docs = DocumentHelper.parseText(result.toString());
				System.out.println("xml=" + docs.asXML());
				Element root = docs.getRootElement();
				String ret = root.attributeValue("ret");
				String reason = root.attributeValue("reason");
				if (resultp == null) {
					resultp = new HashMap<String, Object>();
				}
				resultp.put("ret", ret);
				resultp.put("reason", reason);
				System.out.println("cdn同步返回结果错误原因：" + reason);
				return resultp;
			} else {
				if (resultp == null) {
					resultp = new HashMap<String, Object>();
				}
				resultp.put("ret", -1);
				resultp.put("reason", "cdn返回结果为空！");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultp;
	}
	/**
	 * 上传文件到阿帕奇.
	 * @param urlPath 文件路径
	 * @param downloadDir 上传路径
	 * @param fileName 文件名称
	 * @throws Exception
	 */
	public static void downloadFile(String urlPath, String downloadDir,String fileName) throws Exception{
        // 统一资源
        URL url = new URL(urlPath);
        // 连接类的父类，抽象类
        URLConnection urlConnection = url.openConnection();
        // http的连接类
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        // 设定请求的方法，默认是GET
        httpURLConnection.setRequestMethod("GET");
        // 设置字符编码
        httpURLConnection.setRequestProperty("Charset", "UTF-8");
        // 打开到此 URL 引用的资源的通信链接（如果尚未建立这样的连接）。
        httpURLConnection.connect();

        BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());

        String path = downloadDir + File.separatorChar + fileName;
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        OutputStream out = new FileOutputStream(file);
        int size = 0;
        byte[] buf = new byte[1024];
        while ((size = bin.read(buf)) != -1) {
            out.write(buf, 0, size);
        }
        bin.close();
        out.close();
    }
}
