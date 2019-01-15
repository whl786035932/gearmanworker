package cn.videoworks.gearman.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import cn.videoworks.commons.util.json.JsonConverter;

public class HttpUtil {

	private static final Logger logger = LoggerFactory
			.getLogger(HttpUtil.class);

	public static ResponseEntity<String> sentHttpRequest(HttpMethod method,
			Object data, String url, HttpHeaders httpHeaders) {
		if (httpHeaders == null) {
			httpHeaders = getHttpHearders();
		}
		HttpEntity<String> httpEntity = new HttpEntity<String>(
				JsonConverter.format(data), httpHeaders);
		RestTemplate restTemplate = getUTF8StringRestTemplate();
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				method, httpEntity, String.class);
		return responseEntity;
	}

	/**
	 * 得到utf8编码的restTemplate
	 * 
	 * @return
	 */
	private static RestTemplate getUTF8StringRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		List<HttpMessageConverter<?>> converters = new LinkedList<>();
		converters
				.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
		converters.add(new MappingJackson2HttpMessageConverter());
		restTemplate.setMessageConverters(converters);
		return restTemplate;
	}

	/**
	 * 得到http请求头
	 * 
	 * @return
	 */
	public static HttpHeaders getHttpHearders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Content-Type", "application/json");
		httpHeaders.set("Accept-Charset", "UTF-8");
		return httpHeaders;
	}

	public static String get(String url, Map<String, String> headersMap) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		if (headersMap != null && headersMap.size() > 0) {
			for (String key : headersMap.keySet()) {
				headers.add(key, headersMap.get(key));
			}
		}
		String result = restTemplate.getForObject(url, String.class);
		return result;
	}

	public static String post(String url, Map<String, Object> requestBody,
			Map<String, String> headersMap) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType
				.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		if (headersMap != null && headersMap.size() > 0) {
			for (String key : headersMap.keySet()) {
				headers.add(key, headersMap.get(key));
			}
		}
		HttpEntity<String> formEntity = new HttpEntity<String>(
				JsonConverter.format(requestBody), headers);
		String result = restTemplate.postForObject(url, formEntity,
				String.class);
		return result;
	}

	public static String httpPost(String url, Map<String, Object> params) {
		URL u = null;
		HttpURLConnection con = null;
		// 构建请求参数
		StringBuffer sb = new StringBuffer();
		if (params != null) {
			for (java.util.Map.Entry<String, Object> e : params.entrySet()) {
				sb.append(e.getKey());
				sb.append("=");
				sb.append(e.getValue());
				sb.append("&");
			}
			sb.substring(0, sb.length() - 1);
		}
		System.out.println("send_url:" + url);
		System.out.println("send_data:" + sb.toString());
		// 尝试发送请求
		try {
			u = new URL(url);
			con = (HttpURLConnection) u.openConnection();
			// // POST 只能为大写，严格限制，post会不识别
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			OutputStreamWriter osw = new OutputStreamWriter(
					con.getOutputStream(), "UTF-8");
			osw.write(sb.toString());
			osw.flush();
			osw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

		// 读取返回内容
		StringBuffer buffer = new StringBuffer();
		try {
			// 一定要有返回值，否则无法把请求发送给server端。
			BufferedReader br = new BufferedReader(new InputStreamReader(
					con.getInputStream(), "UTF-8"));
			String temp;
			while ((temp = br.readLine()) != null) {
				buffer.append(temp);
				buffer.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return buffer.toString();
	}
}
