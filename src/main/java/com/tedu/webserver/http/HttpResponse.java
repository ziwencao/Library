package com.tedu.webserver.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 响应对象
 * 该类的每个实例表示一个服务端要发送给客户端的具体响应
 * 内容。
 * 一个响应有三部分:
 * 1:状态行
 * 2:响应头
 * 3:响应正文
 * @author adminitartor
 *
 */
public class HttpResponse {
	/*
	 * 状态行相关信息定义
	 */
	//状态代码
	private int statusCode = 200;
	
	/*
	 * 响应头相关信息定义
	 * key:响应头名字
	 * value:响应头对应的值
	 * 
	 */
	private Map<String,String> headers = new HashMap<String,String>();
	
	
	/*
	 * 响应正文相关信息定义
	 */
	
	//响应的实体文件
	private File entity;
	
	//响应正文数据
	private byte[] data;
	
	
	private Socket socket;
	private OutputStream out;
	
	
	public HttpResponse(Socket socket){
		try {
			this.socket = socket;
			this.out = socket.getOutputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 响应客户端
	 */
	public void flush(){
		/*
		 * 1:发送状态行
		 * 2:发送响应头
		 * 3:发送响应正文
		 */
		sendStatusLine();
		sendHeaders();
		sendContent();
	}
	/**
	 * 发送状态行
	 */
	private void sendStatusLine(){
		try {
			String line = "HTTP/1.1"+" "+statusCode+" "+HttpContext.getStatusReason(statusCode);
			println(line);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 发送响应头
	 */
	private void sendHeaders(){
		try {
			/*
			 * 遍历headers，将所有响应头发送给客户端
			 */
			Set<Entry<String,String>> entrySet 
										= headers.entrySet();
			for(Entry<String,String> header : entrySet){
				String name = header.getKey();
				String value = header.getValue();
				String line = name+": "+value;
				System.out.println("发送响应头"+line);
				println(line);
			}
			
			//单独发送CRLF
			println("");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 发送响应正文
	 */
	private void sendContent(){
		if(data != null){
			try {
				out.write(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(entity!=null){
			try (
				FileInputStream fis 
					= new FileInputStream(entity);
			){			
				byte[] data = new byte[1024*10];
				int len = -1;
				while((len = fis.read(data))!=-1){
					out.write(data, 0, len);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 向客户端发送一行字符串，自动以CRLF结尾
	 * @param line
	 */
	private void println(String line){
		try {
			out.write(line.getBytes("ISO8859-1"));
			out.write(13);//written CR
			out.write(10);//written LF
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public File getEntity() {
		return entity;
	}
	/**
	 * 设置响应实体文件，在设置的同时会自动根据该文件
	 * 长度及类型设置对应的响应头:
	 * Content-Type,Content-Length
	 * @param entity
	 */
	public void setEntity(File entity) {
		this.entity = entity;
		//设置Content-Type,根据实体文件后缀找到对应的介质类型
		String name = entity.getName();
		String ext = name.substring(name.lastIndexOf(".")+1);
		String contentType = HttpContext.getMimeType(ext);
		headers.put("Content-Type", contentType);
		
		
		//设置Content-Length
		headers.put("Content-Length", entity.length()+"");
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	/**
	 * 设置响应头
	 * @param name
	 * @param value
	 */
	public void putHeader(String name,String value){
		this.headers.put(name, value);
	}
	
	/**
	 * 重定向到指定路径
	 * @param url
	 */
	public void sendRedirect(String url){
		//1设置状态代码
		this.setStatusCode(302);
		
		//2设置响应头
		this.putHeader("Location", url);
	}
	
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
	
	
}















