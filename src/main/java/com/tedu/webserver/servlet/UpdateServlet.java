package com.tedu.webserver.servlet;

import java.io.RandomAccessFile;
import java.util.Arrays;

import com.tedu.webserver.http.HttpRequest;
import com.tedu.webserver.http.HttpResponse;
/**
 * 修改用户密码
 * @author adminitartor
 *
 */
public class UpdateServlet extends HttpServlet{

	public void service(HttpRequest request, HttpResponse response) {
		String username = request.getParameter("username");
		String oldPwd = request.getParameter("oldpassword");
		String newPwd = request.getParameter("newpassword");
		try (
			RandomAccessFile raf = new RandomAccessFile("user.dat","rw");	
		){
			//是否找到该用户
			boolean check = false;
			for(int i=0;i<raf.length()/100;i++){
				raf.seek(i*100);
				byte[] data = new byte[32];
				raf.read(data);
				String name = new String(data,"UTF-8").trim();
				//查看是否为该用户
				if(name.equals(username)){
					//读取原密码
					raf.read(data);
					String pwd = new String(data,"UTF-8").trim();
					//比对密码
					if(pwd.equals(oldPwd)){
						//将指针移动到密码位置
						raf.seek(i*100+32);
						//修改密码
						data = newPwd.getBytes("UTF-8");
						data = Arrays.copyOf(data, 32);
						raf.write(data);
						forward("/myweb/update_success.html", request, response);
					}else{
						//旧密码不对
						forward("/myweb/update_fail.html", request, response);
					}
					check = true;
					break;
				}
			}
			//查无此人
			if(!check){
				forward("/myweb/no_user.html", request, response);
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}

}








