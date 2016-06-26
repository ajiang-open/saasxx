package com.saasxx.framework.testcase.lang;

import java.io.File;
import java.net.HttpCookie;
import java.util.List;
import java.util.UUID;

import com.saasxx.framework.lang.Https;

public class HttpsTest {
	public static void main(String[] args) {
		try {
			// 创建请求对象
			Https.HttpRequest httpRequest = Https
					.create("http://www.baidu.com")
					// 可选，设置Cookies
					.addCookies(
							new HttpCookie("JSESSIONID", UUID.randomUUID()
									.toString()))
					// 可选，设置参数
					.addParameters("wd", "测试关键字")
					// 可选，设置同名的多个参数
					.addParameters("name", "1111", "2222")
					// 可选，设置上传的文件，可以上传很大的文件
					.addUploads("file1",
							new File("/Users/lujijiang/Desktop/upload.txt"))
					// 可选，设置请求编码，默认UTF-8
					.setEncoding("UTF-8")
					// 可选，设置代理访问
					.setProxyHostname("localhost")
					.setProxyPort(8181)
					.setProxyUsername("user")
					// 可选，设置Basic认证用户密码
					.setUsername("basicUser1")
					.setPassword("basicuser1xxxxx")
					// 可选，设置SSL自定义证书文件流
					.setCertStream(
							Thread.currentThread().getContextClassLoader()
									.getResourceAsStream("/ssl/aaaa.cer"))
					.setCertPassword("这是SSL证书密码")
					// 设置不同浏览器的userAgent（这里是模拟谷歌浏览器）
					.setHeaderUserAgent(
							"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36")
					.setHeader("other header", "设置其它属性");
			// 以Post方式提交并获取应答对象
			Https.HttpResponse httpResponse = httpRequest.post();
			// 以字节数组形式获取响应内容
			byte[] content = httpResponse.getContent();
			// 以字符串形式获取响应内容
			String responseContent = httpResponse.getContentAsString("UTF-8");
			// 获取响应的Cookies
			List<HttpCookie> cookies = httpResponse.getCookies();
			// 获取响应状态码，200表示服务器成功响应，500是服务器错误等等
			int status = httpResponse.getResponseCode();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
