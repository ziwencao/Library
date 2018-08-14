package com.tedu.webserver.http;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求类
 * 该类的每一个实例用于表示客户端发送过来的一个具体请求
 * 内容。
 * @author adminitartor
 *
 */
public class HttpRequest {
	/*
	 * 请求行相关信息
	 */
	//请求方式
	private String method;
	//请求资源路径
	private String url;
	//请求的协议版本
	private String protocol;
	/*
	 * 由于url可能会出现两种情况，是否带参数
	 * 例如:
	 * /myweb/index.html   不带参数
	 * /myweb/reg?name=xxx&pwd=xxx...   带参数
	 * 所以对于url内容而言，我们再定义两个属性，分别
	 * 保存url中的请求部分与参数部分。
	 */
	//请求部分,url中"?"左侧的内容,若没有"?"则与url内容一致
	private String requestURI;
	//参数部分,url中"?"右侧的内容
	private String queryString;
	
	//用于保存每个参数的Map
	private Map<String,String> parameters = new HashMap<String,String>();
	
	
	
	/*
	 * 消息头相关信息
	 */
	//key:消息头名字     value:消息头对应的值
	private Map<String,String> headers = new HashMap<String,String>();
	
	
	
	//对应客户端的Socket
	private Socket socket;
	//通过Socket获取的输入流，用于读取客户端发送的请求内容
	private InputStream in;
	/**
	 * 创建HttpRequest的同时传入对应客户端的Socket。
	 * 要根据该Socket获取输入流读取客户端发送的请求。
	 * @param socket
	 * @throws EmptyRequestException 
	 */
	public HttpRequest(Socket socket) throws EmptyRequestException{
		try {
			this.socket = socket;
			this.in = socket.getInputStream();
			//1解析请求行
			parseRequestLine();
			//2解析消息头
			parseHeaders();
			//3解析消息正文
			parseContent();
			
		} catch(EmptyRequestException e){
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 解析请求行
	 * @throws EmptyRequestException 
	 */
	private void parseRequestLine() throws EmptyRequestException{
		System.out.println("开始解析请求行...");
		/*
		 * 1:先通过输入流读取第一行字符串(请求行内容)
		 * 2:按照空格拆分字符串
		 * 3:将拆分的三项内容设置到请求行对应的属性上
		 */
		String line = readLine();
		System.out.println("请求行内容:"+line);
		
		/*
		 * 解析过程中可能会出现数组下标越界，这是由于
		 * HTTP协议允许客户端发送一个空请求，即:客户端
		 * 连接后没有按照HTTP的request格式发送内容。
		 * 后期解决。
		 */
		String[] data = line.split("\\s");
		if(data.length<3){
			//空请求
			throw new EmptyRequestException();
		}
		this.method = data[0];
		this.url = data[1];
		//进一步解析url
		parseURL();
		this.protocol = data[2];
		System.out.println("method:"+method);
		System.out.println("url:"+url);
		System.out.println("protocol:"+protocol);
		
		System.out.println("解析请求行完毕!");
	}
	/**
	 * 解析url
	 * 由于url中可能含有参数，所以要对url进行进一步解析
	 */
	private void parseURL(){
		/*
		 * url会存在两种情况，是否带参数
		 * 1:判断当前url是否含有"?"，若则表示该url是
		 *   含有参数部分的。
		 * 2:若含有"?",则首先按照"?"将url拆分为两部分
		 *   将"?"左侧内容设置到requestURI属性上，将
		 *   "?"右侧内容设置到queryString属性上。
		 * 3:进一步解析参数部分，首先按照"&"拆分出每个
		 *   参数，再将每个参数按照"="拆分为参数名与
		 *   参数值，并将参数名作为key,参数的值作为value
		 *   存入到parameters这个map中。
		 *   
		 * 4:若url不含有"?",则直接将url的值设置到属性
		 *   requestURI即可。
		 *   
		 * /myweb/reg?username=123&password=123&nickname=123456&age=123456
		 * 
		 */
		//1
		if(this.url.indexOf("?")!=-1){
			//2
			//按照?拆分
			String data[] = this.url.split("\\?");
			this.requestURI = data[0];
			if(data.length>1){
				this.queryString = data[1];	
				parseParameters(this.queryString);
			}	
		}else{
			this.requestURI = this.url;
		}
		
		System.out.println("requestURI:"+requestURI);
		System.out.println("queryString:"+queryString);
		
	}
	/**
	 * 解析参数
	 * 该格式应当为:
	 * name1=value1&name2=value2&.....
	 */
	private void parseParameters(String line){
		try {
			/*
			 * 将line进行转码，将所有%XX内容转换为对应字符
			 * 
			 * URLDecoder用来对URL中%XX内容进行解码
			 * 其提供了静态方法:
			 * static String decode(String str,String csn)
			 * 对给定的字符串str解码，将其中所有%XX内容按照给定
			 * 的字符集转换为对应字符并替换%XX,将替换好的字符串
			 * 返回
			 */
			line = URLDecoder.decode(line, "UTF-8");		

			String[] paraArray = line.split("&");
			//paraStr:username=123
			for(String paraStr : paraArray){
				String []paras = paraStr.split("=");
				if(paras.length>1){
					this.parameters.put(paras[0], paras[1]);
				}else{
					this.parameters.put(paras[0], null);
				}
			}
			
			System.out.println("parameters:"+parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 解析消息头
	 */
	private void parseHeaders(){
		System.out.println("开始解析消息头...");
		/*
		 * 1:循环读取一行字符串,若读取到的是空字符串
		 *   则表示单独读取了CRLF，那么就停止循环，因
		 *   为所有消息头均读取完毕。
		 * 2:每当读取一行字符串后，要按照": "拆分为两
		 *   项，第一项为消息头名字，第二项为消息头的值
		 * 3:将该消息头信息存入到属性headers这个map中
		 *   完成消息头解析工作    
		 * 
		 */
//		String line = null;
//		while(!"".equals(line = readLine())){	
		
		while(true){
			String line = readLine();
			if("".equals(line)){
				break;
			}
			String[] data = line.split(":\\s");
			headers.put(data[0], data[1]);
		}
		System.out.println("headers:"+headers);		
		System.out.println("解析消息头完毕!");
	}
	/**
	 * 解析消息正文
	 */
	private void parseContent(){
		System.out.println("开始解析消息正文...");
		/*
		 * 首先判断当前消息头中是否含有:Content-Length
		 */		
		if(headers.containsKey("Content-Length")){
			//获取消息正文长度
			int contentLength = Integer.parseInt(
				headers.get("Content-Length")	
			);
			try {
				byte[] data = new byte[contentLength];
				//读取消息正文内容
				in.read(data);
				/*
				 * 再根据消息头中:Content-Type来判断消息
				 * 正文内容是什么
				 */
				String contentType = headers.get("Content-Type");
				//判断是否为form表单
				System.out.println(contentType);
				if("application/x-www-form-urlencoded".equals(contentType)){
					//将读取到的字节还原为字符串
					String form = new String(data,"ISO8859-1");
					//解析表单内容
					parseParameters(form);
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
		}
		
		System.out.println("解析消息正文完毕!");
	}
	
	
	/**
	 * 读取一行字符串,以CRLF结尾则认为一行字符串结束,
	 * 并将之前内容以一个字符串形式返回。返回的字符串
	 * 中不包含最后的CRLF符
	 * @param in
	 * @return
	 */
	private String readLine(){
		StringBuilder builder = new StringBuilder();
		try {
			int d = -1;
			//c1表示上次读取到的字符，c2表示本次读取到的字符
			char c1='a',c2='a';
			while((d = in.read())!=-1){
				c2 = (char)d;
				//判断是否连续读取到了CRLF
				if(c1==13&&c2==10){
					break;
				}
				builder.append(c2);
				c1 = c2;
			}
			return builder.toString().trim();		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	
	public String getMethod() {
		return method;
	}
	public String getUrl() {
		return url;
	}
	public String getProtocol() {
		return protocol;
	}
	/**
	 * 根据给定的消息头名字获取对应的值
	 * @param name
	 * @return
	 */
	public String getHeader(String name){
		return headers.get(name);
	}
	public String getRequestURI() {
		return requestURI;
	}
	public String getQueryString() {
		return queryString;
	}
	/**
	 * 获取给定参数名对应的参数值
	 * @param name
	 * @return
	 */
	public String getParameter(String name){
		return this.parameters.get(name);
	}

}













