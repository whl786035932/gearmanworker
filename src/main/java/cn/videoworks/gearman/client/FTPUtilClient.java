package cn.videoworks.gearman.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.videoworks.gearman.common.ParameterMap;
import cn.videoworks.gearman.util.FileUtil;

/**
 * ClassName:FTPClient Function: TODO ADD FUNCTION Reason: TODO ADD REASON
 * ftp客户端
 * 
 * @author meishen
 * @version
 * @since Ver 1.1
 * @Date 2018 2018年6月29日 下午4:15:50
 * 
 * @see
 */
public class FTPUtilClient {

	private static FTPClient ftpClient = null;
	private static FTPUtilClient instance = null;

	private static Logger logger = LoggerFactory.getLogger(FTPUtilClient.class);

	private FTPUtilClient() {
		ftpClient = new FTPClient();
		logger.info("创建ftp客户端成功!");
	}

	public static FTPUtilClient getInstance() {
		if (instance == null) {
			instance = new FTPUtilClient();
		}
		return instance;
	}

	/**
	 * connect:(连接ftp)
	 * 
	 * @author meishen
	 * @Date 2018 2018年6月29日 下午6:48:58
	 * @return
	 * @return boolean
	 * @throws
	 * @since Videoworks　Ver 1.1
	 */
	@SuppressWarnings("static-access")
	public boolean connect() {
		boolean flag = false;
		if (null != ftpClient && ftpClient.isConnected()) {
			logger.info("ftp已连接");
			flag = true;
		} else {
			try {
				String host = "";
				int port =0;
				String username = "";
				String password = "";
				try {
					//host = PropertiesUtil.getPropertiesUtil().get("ftp.ip");
					host ="ftp.am.jmxxb";
					logger.info("ftp.ip=" + host);
//					port = Integer.valueOf(PropertiesUtil.getPropertiesUtil()
//							.get("ftp.port"));
					logger.info("ftp.port=" + port);
//					username = PropertiesUtil.getPropertiesUtil().get(
//							"ftp.username");
					logger.info("ftp.username=" + username);
//					password = PropertiesUtil.getPropertiesUtil().get(
//							"ftp.password");
					logger.info("ftp.password=" + password);
					username = "wbsj";
					password = "wbsj12345";
					ftpClient.connect(host);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("FTP连接失败，读取ftp配置信息失败!");
				}
				ftpClient.login(username, password);
				logger.info("FTP登录成功!");
				if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
					ftpClient.setControlEncoding("UTF-8");
					ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
					logger.info("FTP连接成功。");
					flag = true;
				} else {
					logger.error("FTP连接失败，用户名或者密码错误!");
					close();
					flag = false;
				}
			} catch (SocketException e) {
				e.printStackTrace();
				logger.error("FTP连接失败，IP或者端口错误。");
				close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("FTP连接失败，IP或者端口错误。");
				close();
			}
		}
		return flag;
	}

	private void close() {
		try {
			if (ftpClient != null) {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
				ftpClient = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("FTP关闭失败！");
		}
	}

	/**
	 * makeDirectory:(如果目录不存在，则创建)
	 * 
	 * @author meishen
	 * @Date 2018 2018年6月29日 下午6:54:39
	 * @param remote
	 * @return void
	 * @throws
	 * @since Videoworks　Ver 1.1
	 */
	private void makeDirectory(String remote) {
		String dirs[] = remote.split("/");
		try {
			ftpClient.changeToParentDirectory();
			for (String dir : dirs) {
				if (!ftpClient.changeWorkingDirectory(dir)) {
					ftpClient.makeDirectory(dir);
					ftpClient.changeWorkingDirectory(dir);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("创建目录失败:【" + remote + "】");
		}
	}
	
	/**
	 * upload:(上传文件)
	 * 
	 * @author meishen
	 * @Date 2018 2018年6月29日 下午6:55:07
	 * @param fileName
	 * @param filePath
	 * @param file
	 * @return
	 * @return boolean
	 * @throws
	 * @since Videoworks　Ver 1.1
	 */
	public boolean upload(String fileName, String filePath, InputStream input) {
		boolean flag = false;
		try {
			makeDirectory(filePath);
			ftpClient.changeWorkingDirectory(filePath);
			ftpClient.storeFile(fileName, input);
			input.close();
			logger.info("上传成功!");
			flag = true;
		} catch (IOException e) {
			e.printStackTrace();
			flag = false;
		} finally {
			close();
		}
		return flag;
	}
	
	/**
	 * readFtpFile:(读取ftp文件流)
	 *
	 * @author   meishen
	 * @Date	 2018	2018年7月4日		下午2:27:54
	 * @param ftpFile
	 * @return   
	 * @return InputStream    
	 * @throws 
	 * @since  Videoworks　Ver 1.1
	 */
	public InputStream readFtpFile(String ftpFileName, String ftpFilePath) {
		InputStream is = null;
		try {
			ftpClient.enterLocalPassiveMode(); 
	        ftpClient.changeWorkingDirectory(ftpFilePath) ;
			is = ftpClient.retrieveFileStream(ftpFileName);
		} catch (IOException e) {
			e.printStackTrace();
			is = null;
		}
		return is;
	}

	/**
	 * downloadFile:(ftp下载文件)
	 *
	 * @author   meishen
	 * @Date	 2018	2018年7月4日		下午5:26:01
	 * @param remotePath
	 * @param fileName
	 * @param localPath
	 * @return
	 * @throws Exception   
	 * @return boolean    
	 * @throws 
	 * @since  Videoworks　Ver 1.1
	 */
	public boolean downloadFile(String remotePath, String fileName, String localPath) throws Exception {
	        FileOutputStream fos = null ; 
	        try {
	            File localFile = new File(localPath, fileName);
	            fos = new FileOutputStream(localFile);
	             
	            ftpClient.enterLocalPassiveMode(); 
	            ftpClient.changeWorkingDirectory(remotePath) ;
	            boolean bok = ftpClient.retrieveFile(fileName, fos);
	             
	            fos.close() ;
	            fos = null ;
	             
	            return bok ;
	        } catch (Exception e) {
	            throw e ;
	        }
	        finally {
	            if (fos!=null) {
	                try {
	                    fos.close() ;
	                    fos = null ;
	                } catch (Exception e2) { }
	            }
	        } 
	         
	    }
	
	public static void main(String[] args) throws Exception {
		ParameterMap.getParameterMap("D:\\gearman.properties");
		if (FTPUtilClient.getInstance().connect()) {
//			File file = new File("D:\\gearman.properties");
//			try {
//				FileInputStream stream = new FileInputStream(file);
//				FTPUtilClient.getInstance().upload("gearman.properties",
//						"gzsdk/1", stream);
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			InputStream is = FTPUtilClient.getInstance().readFtpFile("gearman.properties","gzsdk");
			byte[] buffer = FileUtil.input2byte(is);
			FileUtil.buildFile("D:\\1.properties", buffer);
			
//			FTPUtilClient.getInstance().downloadFile("gzsdk/1", "gearman.properties", "D:\\test");
		}
	}
}
