package com.tedu.webserver.servlet;

import java.io.File;
import java.io.RandomAccessFile;

import com.tedu.webserver.http.HttpRequest;
import com.tedu.webserver.http.HttpResponse;

/**
 * 处理登录业务
 * @author adminitartor
 *
 */
public class LoginServlet extends HttpServlet{
	public void service(HttpRequest request,HttpResponse response){
		//获取用户登录信息
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		//读取user.dat比对信息
		try (
			RandomAccessFile raf 
				= new RandomAccessFile("user.dat","r");
		){
			//默认为登录不成功
			boolean check = false;
			for(int i=0;i<raf.length()/100;i++){
				raf.seek(i*100);		
				byte[] data = new byte[32];
				raf.read(data);
				String name = new String(data,"UTF-8").trim();
				//找到此用户
				if(name.equals(username)){
					//比对密码
					raf.read(data);
					String pwd = new String(data,"UTF-8").trim();
					if(pwd.equals(password)){
						check = true;
					}			
					break;
				}
			}
			if(check){
				/*
				 * 需要注意，内部跳转页面时使用的相对路径是服务端这边
				 * 指定的相对路径
				 * 而重定向是将路径发送给客户端，让其根据该地址发起请求，
				 * 所以指定的相对路径是相对浏览器上次请求的路径。
				 * 这里要注意区分
				 */
//				forward("/myweb/login_success.html",request,response);
				response.sendRedirect("login_success.html");
			}else{
//				forward("/myweb/login_fail.html",request,response);
				response.sendRedirect("login_fail.html");
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}










