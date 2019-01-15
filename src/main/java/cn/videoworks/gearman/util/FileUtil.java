/**
 * FileUtil.java
 * cn.videoworks.ldapadmin.util
 * 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2014年7月30日 		meishen
 *
 * Copyright (c) 2014, TNT All Rights Reserved.
*/

package cn.videoworks.gearman.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;




/**
 * ClassName:FileUtil
 * @author   meishen
 * @version  Ver 1.0.0    
 * @Date	 2014年7月30日		下午6:11:27 
 */
public class FileUtil {
	private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
	private static SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
	/**
     * 把一个文件转化为字节
     * @param file
     * @return   byte[]
     * @throws Exception
     */
    @SuppressWarnings("resource")
	public static byte[] getByte(File file) throws Exception
    {
        byte[] bytes = null;
        if(file!=null)
        {
            InputStream is = new FileInputStream(file);
            int length = (int) file.length();
            if(length>Integer.MAX_VALUE)   //当文件的长度超过了int的最大值
            {
                System.out.println("this file is max ");
                return null;
            }
            bytes = new byte[length];
            int offset = 0;
            int numRead = 0;
            while(offset<bytes.length&&(numRead=is.read(bytes,offset,bytes.length-offset))>=0)
            {
                offset+=numRead;
            }
            //如果得到的字节长度和file实际的长度不一致就可能出错了
            if(offset<bytes.length)
            {
                System.out.println("file length is error");
                return null;
            }
            is.close();
        }
        return bytes;
    }
    /** 
     * 把字节数组保存为一个文件 
     *  
     * @param b 
     * @param outputFile 
     * @return 
     */
    public static File getFileFromBytes(byte[] b, String outputFile) {  
        File ret = null;  
        BufferedOutputStream stream = null;  
        try {  
            ret = new File(outputFile); 
            FileOutputStream fstream = new FileOutputStream(ret);  
            stream = new BufferedOutputStream(fstream);  
            stream.write(b);  
        } catch (Exception e) {  
            // log.error("helper:get file from byte process error!");  
            e.printStackTrace();  
        } finally {  
            if (stream != null) {  
                try {  
                    stream.close();  
                } catch (IOException e) {  
                    // log.error("helper:get file from byte process error!");  
                    e.printStackTrace();  
                }  
            }  
        }  
        return ret;  
    } 
    
    public static void byteToFile(String path,byte[] b){
        try  
        {  
            File apple = new File(path);// 把字节数组的图片写到另一个地方  
            FileOutputStream fos = new FileOutputStream(apple);  
            fos.write(b);  
            fos.flush();  
            fos.close();  
        }  
        catch (Exception e)  
        {  
            e.printStackTrace();  
        }  
    }
    
   public static void removeFile(String path){
	   File file=new File(path);
	   if(file.exists()){
		   file.delete();
	   }
   }
    
    public static void removeFile1(String path){
    	try {
    		File file=new File(path);
    		String[] allFile=file.list();
    		File temp=null;
    		for(String f : allFile){
    			 if (path.endsWith(File.separator)) {
    	             temp = new File(path +f);
    	          } else {
    	              temp = new File(path + File.separator + f);
    	          }
    	          if (temp.isFile()) {
    	             temp.delete();
    	          }
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static boolean checkImage(String path){
    	boolean flag=false;
    	try {
			File file=new File(path);
			if(file.exists()){
				flag=true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return flag;
    }
	/** 
     * 根据byte数组，生成文件 
     */  
    public static void buildFile(byte[] bfile, String filePath,String fileName) {  
        BufferedOutputStream bos = null;  
        FileOutputStream fos = null;  
        File file = null;  
        try {  
            File dir = new File(filePath);  
            if(!dir.exists() && !dir.isDirectory()){//判断文件目录是否存在  
                dir.mkdirs();  
            }  
            file = new File(filePath+"/"+fileName);  
            fos = new FileOutputStream(file);  
            bos = new BufferedOutputStream(fos);  
            bos.write(bfile);  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (bos != null) {  
                try {  
                    bos.close();  
                } catch (IOException e1) {  
                    e1.printStackTrace();  
                }  
            }  
            if (fos != null) {  
                try {  
                    fos.close();  
                } catch (IOException e1) {  
                    e1.printStackTrace();  
                }  
            }  
        }  
    } 
    
    /**
     * buildFile:(写入到文件)
     * @author   meishen
     * @Date	 2015	2015年4月29日		下午4:03:58
     * @return void    
     * @throws 
     * @since  CodingExample　Ver 1.0.0
     */
    public static void buildFile(String path,byte[] byt){
 	   BufferedOutputStream bos = null;  
        FileOutputStream fos = null;  
        try {  
        	 File file  = new File(path);  
            fos = new FileOutputStream(file);  
            bos = new BufferedOutputStream(fos);  
            bos.write(byt);  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (bos != null) {  
                try {  
                    bos.close();  
                } catch (IOException e1) {  
                    e1.printStackTrace();  
                }  
            }  
            if (fos != null) {  
                try {  
                    fos.close();  
                } catch (IOException e1) {  
                    e1.printStackTrace();  
                }  
            }  
        }  
    }
	
	public static String execGenerateFile(MultipartFile multipartFile,String storagePath,String httpUrl) {
		// TODO Auto-generated method stub.
		String uuid = UUID.randomUUID().toString();
		String url = "";
		String dateStr = df.format(new Date());
		String imgPath = storagePath+dateStr+"/"+uuid+multipartFile.getOriginalFilename();
		boolean result = generateFile(imgPath,multipartFile);
		if(result){
			url = httpUrl+storagePath+"upload/avator/"+dateStr+"/"+uuid+multipartFile.getOriginalFilename();
		}else{
			log.error("generate poster faild,imgPath is "+imgPath);
		}
		return url;
	}
	
	/**
	 * 
	 * 生成图片
	 *
	 * @param path
	 * @param mf
	 * @return
	 */
	public static boolean generateFile(String path,MultipartFile mf)  {
		log.warn("make dirs starting,the file path is "+path);
		if(path==null){
			return false;
		}
		File file = new File(path);		
		if(!file.getParentFile().exists()){ 
			boolean reslut = file.getParentFile().mkdirs();
			if(!reslut)
			{
				log.error("make dirs faild,the file path is "+path);
			}
			
		} 
		FileOutputStream out = null;
		InputStream in = null;
		try {
			file.createNewFile();
			//将字符串转换为byte数组
			byte[] buffer = new byte[1024];
			out = new FileOutputStream(path);
			int byteread = 0;
			in = mf.getInputStream();
			while ((byteread = in.read(buffer)) != -1) {
				out.write(buffer, 0, byteread); //文件写操作
			}
			return true;
		} catch (IOException exception) {
			// TODO Auto-generated catch-block stub.
			exception.printStackTrace();
			return false;
		}  finally {   
			try {   
				out.close();  
				in.close();
			} catch (Exception e) {   
				e.printStackTrace();  
				return false;
			}   
		}   
		
	}
	public static boolean generateFile(String path,byte[] fileByte)  {
		log.warn("make dirs starting,the file path is "+path);
		if(path==null){
			return false;
		}
		File file = new File(path); 
		if(!file.getParentFile().exists()){ 
			boolean reslut = file.getParentFile().mkdirs();
			if(!reslut)
			{
				log.error("make dirs faild,the file path is "+path);
			}
			
		} 
		BufferedOutputStream bos = null;  
        FileOutputStream fos = null;  
        try {  
            fos = new FileOutputStream(file);  
            bos = new BufferedOutputStream(fos);  
            bos.write(fileByte);  
        } catch (Exception e) {  
            e.printStackTrace(); 
            return false;
        } finally {  
            if (bos != null) {  
                try {  
                    bos.close();  
                } catch (IOException e1) {  
                    e1.printStackTrace();  
                    return false;
                }  
            }  
            if (fos != null) {  
                try {  
                    fos.close();  
                } catch (IOException e1) {  
                    e1.printStackTrace();
                    return false;
                }  
            }  
            
        } 
        return true;
		
	}
	
	/**
	 * input2byte:(流转字节)
	 *
	 * @author   meishen
	 * @Date	 2018	2018年7月4日		下午5:07:04
	 * @param inStream
	 * @return
	 * @throws IOException   
	 * @return byte[]    
	 * @throws 
	 * @since  Videoworks　Ver 1.1
	 */
	public static byte[] input2byte(InputStream inStream){  
		byte[] in2b = null;
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();  
		try {
		    byte[] buff = new byte[100];  
		    int rc = 0;  
		    while ((rc = inStream.read(buff, 0, 100)) > 0) {  
		    	swapStream.write(buff, 0, rc);  
		    }  
		    in2b = swapStream.toByteArray();  
		    swapStream.close();
		    return in2b;  
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				swapStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	    return in2b;
	}  
	
	/**
	 * deleteFile:(删除单个文件)
	 *
	 * @author   meishen
	 * @Date	 2018	2018年7月4日		下午8:50:53
	 * @param fileName
	 * @return   
	 * @return boolean    
	 * @throws 
	 * @since  Videoworks　Ver 1.1
	 */
	public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
            	log.info("删除单个文件" + fileName + "成功！");
                return true;
            } else {
            	log.error("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
        	log.error("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }
	
	/**
	 * 通过url获取输入流. s3l路径
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
			System.out.println(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return inputStream;
	}
	
	 /**
     * exists:(验证文件是否存在)
     * @author   meishen
     * @Date	 2018	2018年11月21日		下午5:51:29 
     * @return boolean    
     * @throws
     */
    public static boolean exists(String path){
    	boolean flag=false;
    	try {
			File file=new File(path);
			if(file.exists()){
				flag=true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			flag=false;
		}
    	return flag;
    }
    
	public static String suffix(String fileName) {
		String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
		return suffix;
	}
}

