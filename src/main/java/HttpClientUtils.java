import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @author sfit0254
 * @date 2014-2-27
 */
// 直接抄
public class HttpClientUtils {

	private static final String HTTPS_PROTOCOL = "https://";
	private static final int HTTPS_PROTOCOL_DEFAULT_PORT = 443;

	/**
	 * 默认编码格式
	 */
	private static final String DEFAULT_CHARSET = "UTF-8";

	private static Logger logger = Logger.getLogger(HttpClientUtils.class);

	private static int getPort(String url) {
		int startIndex = url.indexOf("://") + "://".length();
		String host = url.substring(startIndex);
		if (host.indexOf("/") != -1) {
			host = host.substring(0, host.indexOf("/"));
		}
		int port = HTTPS_PROTOCOL_DEFAULT_PORT;
		if (host.contains(":")) {
			int i = host.indexOf(":");
			port = new Integer(host.substring(i + 1));
		}
		return port;
	}

	private static List<NameValuePair> geneNameValPairs(Map<String, ?> params) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		if (params == null) {
			return pairs;
		}
		for (String name : params.keySet()) {
			if (params.get(name) == null) {
				continue;
			}
			pairs.add(new BasicNameValuePair(name, params.get(name).toString()));
		}
		return pairs;
	}

	/**
	 * 发送GET请求
	 * @param url 请求地址
	 * @param charset 返回数据编码
	 * @return 返回数据
	 */
	public static String sendGetReq(final String url, String charset) {
		if (null == charset)
			charset = DEFAULT_CHARSET;
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet get = new HttpGet(url);
		if (url.toLowerCase().startsWith(HTTPS_PROTOCOL)) {
			initSSL(httpClient, getPort(url));
		}
		try {
			// 提交请求并以指定编码获取返回数据
			HttpResponse httpResponse = httpClient.execute(get);
			int statuscode = httpResponse.getStatusLine().getStatusCode();
	        if ((statuscode == HttpStatus.SC_MOVED_TEMPORARILY) ||
	            (statuscode == HttpStatus.SC_MOVED_PERMANENTLY) ||
	            (statuscode == HttpStatus.SC_SEE_OTHER) ||
	            (statuscode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
	        	Header header = httpResponse.getFirstHeader("location");
	            if (header != null) {
	                String newuri = header.getValue();
	                if ((newuri == null) || (newuri.equals("")))
	                    newuri = "/";
	                try {
	    				httpClient.close();
	    			} catch (Exception e) {
	    				e.printStackTrace();
	    				httpClient = null;
	    			}
	                logger.info("重定向地址：" + newuri);
	                return sendGetReq(newuri, null);
	            }
	        }
			logger.info("请求地址：" + url + "；响应状态：" + httpResponse.getStatusLine());
			HttpEntity entity = httpResponse.getEntity();
			return EntityUtils.toString(entity, charset);
		} catch (ClientProtocolException e) {
			logger.error("协议异常,堆栈信息如下", e);
		} catch (IOException e) {
			logger.error("网络异常,堆栈信息如下", e);
		} finally {
			// 关闭连接，释放资源
			try {
				httpClient.close();
			} catch (Exception e) {
				e.printStackTrace();
				httpClient = null;
			}
		}
		return null;
	}

	/**
	 * 发送put请求
	 * @param url		请求地址
	 * @param params	请求参数
	 * @param charset	返回数据编码
	 * @return
	 */
	public static String sendPutReq(String url, Map<String, Object> params, String charset) {
		if (null == charset)
			charset = DEFAULT_CHARSET;
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPut put = new HttpPut(url);

		// 封装请求参数
		List<NameValuePair> pairs = geneNameValPairs(params);
		try {
			put.setEntity(new UrlEncodedFormEntity(pairs, charset));
			if (url.startsWith(HTTPS_PROTOCOL)) {
				initSSL(httpClient, getPort(url));
			}
			// 提交请求并以指定编码获取返回数据
			HttpResponse httpResponse = httpClient.execute(put);

			logger.info("请求地址：" + url + "；响应状态：" + httpResponse.getStatusLine());
			HttpEntity entity = httpResponse.getEntity();
			return EntityUtils.toString(entity, charset);
		} catch (ClientProtocolException e) {
			logger.error("协议异常,堆栈信息如下", e);
		} catch (IOException e) {
			logger.error("网络异常,堆栈信息如下", e);
		} finally {
			// 关闭连接，释放资源
			try {
				httpClient.close();
			} catch (Exception e) {
				e.printStackTrace();
				httpClient = null;
			}
		}
		return null;
	}

	/**
	 * 发送delete请求
	 * @param url		请求地址
	 * @param charset	返回数据编码
	 * @return
	 */
	public static String sendDelReq(String url, String charset) {
		if (null == charset)
			charset = DEFAULT_CHARSET;
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpDelete del = new HttpDelete(url);
		if (url.toLowerCase().startsWith(HTTPS_PROTOCOL)) {
			initSSL(httpClient, getPort(url));
		}
		try {
			// 提交请求并以指定编码获取返回数据
			HttpResponse httpResponse = httpClient.execute(del);
			logger.info("请求地址：" + url + "；响应状态：" + httpResponse.getStatusLine());
			HttpEntity entity = httpResponse.getEntity();
			return EntityUtils.toString(entity, charset);
		} catch (ClientProtocolException e) {
			logger.error("协议异常,堆栈信息如下", e);
		} catch (IOException e) {
			logger.error("网络异常,堆栈信息如下", e);
		} finally {
			// 关闭连接，释放资源
			try {
				httpClient.close();
			} catch (Exception e) {
				e.printStackTrace();
				httpClient = null;
			}
		}
		return null;
	}

	/**
	 * 发送POST请求（请求参数处理，content-type为application/json），支持HTTP与HTTPS
	 * @param url
	 * @param params
	 * @param charset
	 * @return
	 */
	public static String sendPostRequest(String url, Map<String, Object> params, String charset) {
	    if (null == charset)
            charset = DEFAULT_CHARSET;
	    String response  = null;
	    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {
            RequestConfig reqConf = RequestConfig.DEFAULT;
            HttpPost httpPost = new HttpPost(url);
            JSONObject json = new JSONObject(params);
            StringEntity stringEntity = new StringEntity(JSON.toJSONString(json));
            stringEntity.setContentType("application/json; charset=" + charset);
            httpPost.setEntity(stringEntity);
            // 对HTTPS请求进行处理
            if (url.toLowerCase().startsWith(HTTPS_PROTOCOL)) {
                initSSL(httpClient, getPort(url));
            }
            // 提交请求并以指定编码获取返回数据
            httpPost.setConfig(reqConf);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            int statuscode = httpResponse.getStatusLine().getStatusCode();
            if ((statuscode == HttpStatus.SC_MOVED_TEMPORARILY) ||
                (statuscode == HttpStatus.SC_MOVED_PERMANENTLY) ||
                (statuscode == HttpStatus.SC_SEE_OTHER) ||
                (statuscode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
                Header header = httpResponse.getFirstHeader("location");
                if (header != null) {
                    String newuri = header.getValue();
                    if ((newuri == null) || (newuri.equals("")))
                        newuri = "/";
                    try {
                        httpClient.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        httpClient = null;
                    }
                    return sendGetReq(newuri, null);
                }
            }
            logger.info("请求地址：" + url + "；响应状态：" + httpResponse.getStatusLine());
            HttpEntity entity = httpResponse.getEntity();
            return EntityUtils.toString(entity, charset);
        
        } catch (UnsupportedEncodingException e) {
            logger.error("不支持当前参数编码格式[" + charset + "],堆栈信息如下", e);
        } catch (ClientProtocolException e) {
            logger.error("协议异常,堆栈信息如下", e);
        } catch (IOException e) {
            logger.error("网络异常,堆栈信息如下", e);
        } finally {
            // 关闭连接，释放资源
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
                httpClient = null;
            }
        }
	    return response;
	}
	
	/**
	 * 发送POST请求，支持HTTP与HTTPS
	 * @param url 请求地址
	 * @param params 请求参数
	 * @param charset 返回数据编码
	 * @return 返回数据
	 */
	public static String sendPostReq(String url, Map<String, ?> params, String charset) {
		if (null == charset)
			charset = DEFAULT_CHARSET;
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		RequestConfig reqConf = RequestConfig.DEFAULT;
		HttpPost httpPost = new HttpPost(url);
		// 封装请求参数
		List<NameValuePair> pairs = geneNameValPairs(params);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(pairs, charset));
			// 对HTTPS请求进行处理
			if (url.toLowerCase().startsWith(HTTPS_PROTOCOL)) {
				initSSL(httpClient, getPort(url));
			}
			// 提交请求并以指定编码获取返回数据
			httpPost.setConfig(reqConf);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			int statuscode = httpResponse.getStatusLine().getStatusCode();
	        if ((statuscode == HttpStatus.SC_MOVED_TEMPORARILY) ||
	            (statuscode == HttpStatus.SC_MOVED_PERMANENTLY) ||
	            (statuscode == HttpStatus.SC_SEE_OTHER) ||
	            (statuscode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
	        	Header header = httpResponse.getFirstHeader("location");
	            if (header != null) {
	                String newuri = header.getValue();
	                if ((newuri == null) || (newuri.equals("")))
	                    newuri = "/";
	                try {
	    				httpClient.close();
	    			} catch (Exception e) {
	    				e.printStackTrace();
	    				httpClient = null;
	    			}
	                return sendGetReq(newuri, null);
	            }
	        }

			logger.info("请求地址：" + url + "；响应状态：" + httpResponse.getStatusLine());
			HttpEntity entity = httpResponse.getEntity();
			return EntityUtils.toString(entity, charset);
		} catch (UnsupportedEncodingException e) {
			logger.error("不支持当前参数编码格式[" + charset + "],堆栈信息如下", e);
		} catch (ClientProtocolException e) {
			logger.error("协议异常,堆栈信息如下", e);
		} catch (IOException e) {
			logger.error("网络异常,堆栈信息如下", e);
		} finally {
			// 关闭连接，释放资源
			try {
				httpClient.close();
			} catch (Exception e) {
				e.printStackTrace();
				httpClient = null;
			}
		}
		return null;
	}

	
	/**
	 * 发送POST请求，支持HTTP与HTTPS, 参数放入请求体方式
	 * @param url 请求地址
	 * @param params 请求参数
	 * @param charset 返回数据编码
	 * @return 返回数据
	 */
	public static void sendPostFile(String bar_id, String url, String content, String charset) {
		OutputStream out = null;
		InputStream in = null;
		 
		if (null == charset)
			charset = DEFAULT_CHARSET;
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		RequestConfig reqConf = RequestConfig.DEFAULT;
		HttpPost httpPost = new HttpPost(url);
		try {
			httpPost.setEntity(new StringEntity(content, charset));
			// 对HTTPS请求进行处理
			if (url.toLowerCase().startsWith(HTTPS_PROTOCOL)) {
				initSSL(httpClient, getPort(url));
			}
			// 提交请求并以指定编码获取返回数据
			httpPost.setConfig(reqConf);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			for(Header header : httpResponse.getAllHeaders()){
				logger.info("[ " + header.getName() + " : " + header.getValue() + " ]");
			}
			
            HttpEntity entity = httpResponse.getEntity();
            in = entity.getContent();

            File file = new File("/Users/lilei/Documents/mimiLive/" + bar_id + ".jpeg");
            if(!file.exists()){
                file.createNewFile();
            }
            
            out = new FileOutputStream(file);  
            byte[] buffer = new byte[4096];
            int readLength = 0;
            while ((readLength=in.read(buffer)) > 0) {
                byte[] bytes = new byte[readLength];
                System.arraycopy(buffer, 0, bytes, 0, readLength);
                out.write(bytes);
            }
            
            out.flush();
			
		} catch (UnsupportedEncodingException e) {
			logger.error("不支持当前参数编码格式[" + charset + "],堆栈信息如下", e);
		} catch (ClientProtocolException e) {
			logger.error("协议异常,堆栈信息如下", e);
		} catch (IOException e) {
			logger.error("网络异常,堆栈信息如下", e);
		} finally {
			// 关闭连接，释放资源
			try {
				httpClient.close();
			} catch (Exception e) {
				e.printStackTrace();
				httpClient = null;
			}
		}
	}
	
	
	
	
	/**
	 * 发送POST请求，支持HTTP与HTTPS, 参数放入请求体方式
	 * @param url 请求地址
	 * @param content 请求参数
	 * @param charset 返回数据编码
	 * @return 返回数据
	 */
	public static String sendPost(String url, String content, String charset) {
		if (null == charset)
			charset = DEFAULT_CHARSET;
		RequestConfig reqConf = RequestConfig.DEFAULT;
		HttpPost httpPost = new HttpPost(url);
		
		try (CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build()){
			httpPost.setEntity(new StringEntity(content, charset));
			// 对HTTPS请求进行处理
			if (url.toLowerCase().startsWith(HTTPS_PROTOCOL)) {
				initSSL(httpClient, getPort(url));
			}
			// 提交请求并以指定编码获取返回数据
			httpPost.setConfig(reqConf);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			int statuscode = httpResponse.getStatusLine().getStatusCode();
			if ((statuscode == HttpStatus.SC_MOVED_TEMPORARILY) ||
					(statuscode == HttpStatus.SC_MOVED_PERMANENTLY) ||
					(statuscode == HttpStatus.SC_SEE_OTHER) ||
					(statuscode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
				Header header = httpResponse.getFirstHeader("location");
				if (header != null) {
					String newuri = header.getValue();
					if ((newuri == null) || (newuri.equals("")))
						newuri = "/";
					try {
						httpClient.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return sendGetReq(newuri, null);
				}
			}
			logger.info("请求地址：" + url + "；响应状态：" + httpResponse.getStatusLine());
			HttpEntity entity = httpResponse.getEntity();
			return EntityUtils.toString(entity, charset);
		} catch (UnsupportedEncodingException e) {
			logger.error("不支持当前参数编码格式[" + charset + "],堆栈信息如下", e);
		} catch (ClientProtocolException e) {
			logger.error("协议异常,堆栈信息如下", e);
		} catch (IOException e) {
			logger.error("网络异常,堆栈信息如下", e);
		}
		return null;
	}

	/**
	 * 初始化HTTPS请求服务
	 * @param httpClient HTTP客户端
	 */
	public static void initSSL(CloseableHttpClient httpClient, int port) {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("SSL");
			final X509TrustManager trustManager = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			// 使用TrustManager来初始化该上下文,TrustManager只是被SSL的Socket所使用
			sslContext.init(null, new TrustManager[] { trustManager }, null);
			ConnectionSocketFactory ssf = new SSLConnectionSocketFactory(sslContext);
			Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create().register("https", ssf).build();
			BasicHttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(r);
			HttpClients.custom().setConnectionManager(ccm).build();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}
