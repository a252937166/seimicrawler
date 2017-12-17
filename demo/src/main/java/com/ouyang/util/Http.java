package com.ouyang.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.util.SerializationUtils;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author yue han jiang
 * @date 2015年3月12日
 * @file_name HttpClient.java
 */
public class Http {
	
	private static Log log = LogFactory.getLog(Http.class);
	
	private final String url;
	
	private HttpRequestBase requestBase;
	
	private RequestConfig config;

	private final Map<String, String> headers = new LinkedHashMap<String, String>();
	
	private final Map<String, Object> params = new LinkedHashMap<String, Object>();
	
	private StatusLine statusLine;
	
	private Header[] headerAll;
			
	private CloseableHttpClient httpClient;

	public Http(String url) {
		if(url.startsWith("https")){
			SSLContext sslContext = null;
			try {
				sslContext = new SSLContextBuilder().loadTrustMaterial(null,new TrustStrategy() {
					@Override
					public boolean isTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
						return true;
					}
				}).
				build();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			this.httpClient = HttpClients.custom().setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)).build();
		}else{
			this.httpClient = HttpClients.createDefault();
		}
		this.url = url;
	}
	
	
	public Map<String, Object> getParams(){
		return this.params;
	}
	
	public Http addParam(String name,Object value){
		this.params.put(name, value);
		return this;
	}
	
	public Http addHeader(String name,String value){
		this.headers.put(name, value);
		return this;
	}

	public void setConfig(RequestConfig config) {
		this.config = config;
	}
	
	private HttpResult doExecute(){
		try {
			setConfig();
			addHeader();
			HttpResponse httpResponse = httpClient.execute(this.requestBase);
			this.headerAll = httpResponse.getAllHeaders();
			this.statusLine = httpResponse.getStatusLine();
			return new HttpResult(EntityUtils.toByteArray(httpResponse.getEntity()));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}finally{
			doClose();
		}
		return null;
	}
	private void setConfig() {
		if(config != null)
			requestBase.setConfig(config);
	}
	
	public HttpResult doGet(){
		try {
			requestBase = new HttpGet(getParamsURI());
			return doExecute();
		} catch (URISyntaxException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public HttpResult doPost(HttpEntity entity){
		try {
			requestBase = new HttpPost(url);
			HttpPost post = (HttpPost) this.requestBase;
			post.setEntity(entity);
			return doExecute();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public HttpResult doPost(){
		try {
			return doPost(new UrlEncodedFormEntity(addNameValuePairs(), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public HttpResult doPut(){
		try {
			return doPut(new UrlEncodedFormEntity(addNameValuePairs(),"utf-8"));
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	private List<NameValuePair> addNameValuePairs(){
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		for (Entry<String, Object> en : this.params.entrySet())
			list.add(new BasicNameValuePair(en.getKey(), en.getValue().toString()));
		return list;
	}
	
	public HttpResult doPut(HttpEntity entity){
		try {
			requestBase = new HttpPut(url);
			HttpPut httpPut = (HttpPut) this.requestBase;
			httpPut.setEntity(entity);
			return doExecute();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public HttpResult doDelete(){
		try {
			requestBase = new HttpDelete(getParamsURI());
			return doExecute();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	private void addHeader(){
		for (Entry<String, String> header : headers.entrySet())
			requestBase.addHeader(header.getKey(), header.getValue());
	}
	
	private void doClose(){
		try {
			if(this.requestBase != null)
				this.requestBase.abort();
			if(this.httpClient != null)
				this.httpClient.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private URI getParamsURI() throws URISyntaxException {
		final StringBuilder urls = new StringBuilder(this.url);
		if(!this.params.isEmpty()){
			if(url.indexOf("?") == -1)
				urls.append("?");
			else
				urls.append("&");
			int size = 0;
			for (Entry<String, Object> en : this.params.entrySet()){
			    if(size == 0)
			    	urls.append(en.getKey()+"="+en.getValue());
			    else
			    	urls.append("&"+en.getKey()+"="+en.getValue());
			    size++;
			}
		}
		return new URI(urls.toString());
	}
	


	public StatusLine getStatusLine() {
		return statusLine;
	}

	public Header[] getHeaderAll() {
		return headerAll;
	}
	
	public int getStatusCode(){
		return getStatusLine().getStatusCode();
	}

	public static byte[] getImageBytes(String imgUrl) {
		ByteArrayOutputStream baos = null;
		try {
			URL u = new URL(imgUrl);
			BufferedImage image = ImageIO.read(u);

			//convert BufferedImage to byte array
			baos = new ByteArrayOutputStream();
			ImageIO.write( image, "jpg", baos);
			baos.flush();

			return baos.toByteArray();
		}
		catch (Exception e)
		{
			return null;
		}
		finally
		{
			if(baos != null)
			{
				try {
					baos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public class HttpResult{
		
		private Integer resultCode;
		
		private byte[] resultBytes;
		
		
		/**
		 * 把结果集装换成 map 对象
		 * @auther Y.hj
		 * @return
		 */
		public Map<String, Object> toMap(){
			return toJsonObject(Map.class);
		}
		
		
		/**
		 * 结果返回 bytes
		 * @return
		 */
		public byte[] toBytes(){
			return this.resultBytes;
		}
		
		/**
		 * 返回 String
		 * @return
		 */
		public String toString(){
			try {
				return new String(this.resultBytes, "utf-8");
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage(), e);
			}
			return null;
		}
		
		public JSONObject toJsonObject(){
			return JSON.parseObject(toString());
		}
		
		@SuppressWarnings("unchecked")
		public <T> T toJsonObject(Class<?> clazz){
			try {
				return (T) JSON.parseObject(toString(), clazz);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		public <T> T toObject(){
			try {
				return (T) SerializationUtils.deserialize(toBytes());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			return null;
		}

		public HttpResult(byte[] resultBytes) {
			super();
			this.resultBytes = resultBytes;
		}

		public Integer getResultCode() {
			return resultCode;
		}

		public void setResultCode(Integer resultCode) {
			this.resultCode = resultCode;
		}
		
		
		
	}
}
