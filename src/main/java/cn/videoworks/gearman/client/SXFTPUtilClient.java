package cn.videoworks.gearman.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.StringTokenizer;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.videoworks.gearman.util.PropertiesUtil;


public class SXFTPUtilClient {
	private static FTPClient ftpClient = null;
	private static SXFTPUtilClient instance = null;

	private static Logger logger = LoggerFactory.getLogger(FTPUtilClient.class);

	private SXFTPUtilClient() {
		ftpClient = new FTPClient();
		logger.info("创建ftp客户端成功!");
	}

	public static SXFTPUtilClient getInstance() {
		synchronized (SXFTPUtilClient.class) {
			if (instance == null) {
				instance = new SXFTPUtilClient();
			}
		}
		return instance;
	}

	public FTPClient getFtpClient() {
		if (instance != null) {
			return instance.ftpClient;
		}
		return null;
	}

	/**
	 * connect:(连接ftp)
	 * 
	 * @author meishen
	 * @Date 2018 2018年6月29日 下午6:48:58
	 * @return
	 * @return boolean
	 * @throws Exception 
	 * @throws @since
	 *             Videoworks Ver 1.1
	 */
	@SuppressWarnings("static-access")
	public boolean connect() throws Exception {
		boolean flag = false;
		if (null != ftpClient && ftpClient.isConnected()) {
			logger.info("ftp已连接");
			flag = true;
		} else {
			try {
				String host = "";
				int port = 0;
				String username = "";
				String password = "";
				try {
					host = PropertiesUtil.getPropertiesUtil().get("sx.ftp.ip");
					logger.info("ftp.ip=" + host);
					port = Integer.valueOf(PropertiesUtil.getPropertiesUtil().get("sx.ftp.port"));
					logger.info("ftp.port=" + port);
					username = PropertiesUtil.getPropertiesUtil().get("sx.ftp.username");
					logger.info("ftp.username=" + username);
					password = PropertiesUtil.getPropertiesUtil().get("sx.ftp.password");
					logger.info("ftp.password=" + password);
					System.out.println("ftpclient====================" + ftpClient);
					ftpClient.connect(host,port);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("FTP连接失败" + e.getMessage());
					throw new Exception("FTP连接失败" + e.getMessage());
				}
				ftpClient.login(username, password);
				logger.info("FTP登录成功!");
//				ftpClient.enterLocalActiveMode();
				ftpClient.enterLocalPassiveMode();
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

	public void close() {
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
	 * @throws @since
	 *             Videoworks Ver 1.1
	 */
	public void makeDirectory(String remote) {
		// 如果不能进入dir下，说明此目录不存在！
		
		//String dirs[] = remote.split("/");
		try {
//			ftpClient.changeToParentDirectory();
//			for (String dir : dirs) {
//				if (!ftpClient.changeWorkingDirectory(dir)) {
//					ftpClient.makeDirectory(dir);
//					ftpClient.changeWorkingDirectory(dir);
//				}
//			}
			if (!ftpClient.changeWorkingDirectory(remote)) {
			StringTokenizer s = new StringTokenizer(remote, "/"); // sign
			String pathName = "";
			while (s.hasMoreElements()) {
				pathName = pathName + "/" + (String) s.nextElement();
				ftpClient.mkd(pathName);
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
	 * @throws @since
	 *             Videoworks Ver 1.1
	 */
	public boolean upload(String fileName, String filePath, InputStream input) {
		boolean flag = false;
		try {
			makeDirectory(filePath);
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			ftpClient.changeWorkingDirectory(filePath);
			ftpClient.storeFile(fileName, input);
			logger.info("上传成功!");
			flag = true;
		} catch (IOException e) {
			e.printStackTrace();
			flag = false;
		} finally {
			try {
				input.close();
				ftpClient.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return flag;
	}

	/**
	 * readFtpFile:(读取ftp文件流)
	 *
	 * @author meishen
	 * @Date 2018 2018年7月4日 下午2:27:54
	 * @param ftpFile
	 * @return
	 * @return InputStream
	 * @throws @since
	 *             Videoworks Ver 1.1
	 */
	public InputStream readFtpFile(String ftpFileName, String ftpFilePath) {
		InputStream is = null;
		try {
			ftpClient.enterLocalPassiveMode();
			ftpClient.changeWorkingDirectory(ftpFilePath);
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
	 * @author meishen
	 * @Date 2018 2018年7月4日 下午5:26:01
	 * @param remotePath
	 * @param fileName
	 * @param localPath
	 * @return
	 * @throws Exception
	 * @return boolean
	 * @throws @since
	 *             Videoworks Ver 1.1
	 */
	public boolean downloadFile(String remotePath, String fileName, String localPath) throws Exception {
		FileOutputStream fos = null;
		try {
			File localFile = new File(localPath, fileName);
			fos = new FileOutputStream(localFile);

			ftpClient.enterLocalPassiveMode();
			ftpClient.changeWorkingDirectory(remotePath);
			boolean bok = ftpClient.retrieveFile(fileName, fos);

			fos.close();
			fos = null;

			return bok;
		} catch (Exception e) {
			throw e;
		} finally {
			if (fos != null) {
				try {
					fos.close();
					fos = null;
				} catch (Exception e2) {
				}
			}
		}

	}

	/**
	 * 往ftp 的filePath目录下写入文件
	 * 
	 * @param fileName
	 * @param filePath
	 * @param content
	 * @throws IOException
	 */
	public void writeFileToFtp(String fileName, String filePath, String content) throws IOException {
		InputStream is = null;
		try {
			
		boolean changeDir = ftpClient.changeWorkingDirectory(filePath);
		// 创建文件夹
		if (changeDir) {
			ftpClient.enterLocalPassiveMode();
			ftpClient.setControlEncoding("utf-8");
 			// 1.输入流
 			is = new ByteArrayInputStream(content.getBytes());
 			ftpClient.storeFile(new String(fileName.getBytes("utf-8"),
 					"iso-8859-1"), is);

// 			
//			// // 向指定文件写入内容，如果没有该文件，则先创建文件再写入。写入的方式是追加。
//			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ftpClient.appendFileStream(fileName),
//					"utf-8");
//			PrintWriter pw = new PrintWriter(outputStreamWriter, true); // 写入的文件名
//			pw.write(content);
//			pw.flush();
//			pw.close();
//			outputStreamWriter.close();
		} else {
			// 如果不能进入dir下，说明此目录不存在！
			StringTokenizer s = new StringTokenizer(filePath, "/"); // sign
			String pathName = "";
			while (s.hasMoreElements()) {
				pathName = pathName + "/" + (String) s.nextElement();
				ftpClient.mkd(pathName);
			}
			boolean changeWorkingDirectory = ftpClient.changeWorkingDirectory(filePath);
			is = new ByteArrayInputStream(content.getBytes());
 			ftpClient.storeFile(new String(fileName.getBytes("utf-8"),
 					"iso-8859-1"), is);
//			
//			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ftpClient.appendFileStream(fileName), "utf-8");
//			PrintWriter pw = new PrintWriter(outputStreamWriter, true); // 写入的文件名
//			pw.write(content);
//			pw.flush();
//			pw.close();
//			outputStreamWriter.close();
		}
		}finally {
			is.close();
			ftpClient.disconnect();
		}

		
	}

	public static void main(String[] args) throws Exception {
		SXFTPUtilClient client = SXFTPUtilClient.getInstance();
		boolean connect = client.connect();
		if (connect) {
			client.writeFileToFtp("517668.md5", "/capital/2018-12-04", "nihao");
		} else {
			System.out.println("ftp连接失败");
		}

	}
}
