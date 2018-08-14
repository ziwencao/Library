package com.tedu.webserver.servlet;

import java.io.RandomAccessFile;

import com.tedu.webserver.http.HttpRequest;
import com.tedu.webserver.http.HttpResponse;

/**
 * 生成修改指定用户信息页面
 * @author adminitartor
 *
 */
public class ToEditUserServlet extends HttpServlet{
	public void service(HttpRequest request, HttpResponse response) {
		//获取要修改的用户的名字
		String username = request.getParameter("username");
		
		//找到该用户信息，并生成页面
		try (
			RandomAccessFile raf 
				= new RandomAccessFile("user.dat","r");
		){
			for(int i=0;i<raf.length()/100;i++){
				raf.seek(i*100);
				byte[] data = new byte[32];
				raf.read(data);
				String name = new String(data,"UTF-8").trim();
				if(name.equals(username)){
					raf.read(data);
					String password = new String(data,"UTF-8").trim();
					raf.read(data);
					String nickname = new String(data,"UTF-8").trim();
					int age = raf.readInt();
					
					StringBuilder builder = new StringBuilder();
					builder.append("<html>");
					builder.append("<head>");
					builder.append("<title>用户列表</title>");
					builder.append("<meta charset='UTF-8'>");
					builder.append("</head>");
					builder.append("<body>");
					builder.append("<center>");
					builder.append("<h1>用户列表</h1>");
					builder.append("<form action='updateUser' method='get'>");
					builder.append("<table border='1'>");
					builder.append("<tr>");
					builder.append("<td>用户名</td>");
					builder.append("<td>"+username+"<input type='hidden' name='username' value='"+username+"'></td>");
					builder.append("</tr>");
					builder.append("<tr>");
					builder.append("<td>密码</td>");
					builder.append("<td><input type='password' name='password' value='"+password+"'></td>");
					builder.append("</tr>");
					builder.append("<tr>");
					builder.append("<td>昵称</td>");
					builder.append("<td><input type='text' name='nickname' value='"+nickname+"'></td>");
					builder.append("</tr>");
					builder.append("<tr>");
					builder.append("<td>年龄</td>");
					builder.append("<td><input type='text' name='age' value='"+age+"'></td>");
					builder.append("</tr>");
					builder.append("<tr>");
					builder.append("<td colspan='2' align='center'><input type='submit' value='修改'></td>");
					builder.append("</tr>");
					builder.append("</table>");
					builder.append("</form>");
					builder.append("</center>");
					builder.append("</body>");
					builder.append("</html>");
					
					byte[] arr = builder.toString().getBytes("UTF-8");
					response.putHeader("Content-Type", "text/html");
					response.putHeader("Content-Length", arr.length+"");
					response.setData(arr);
					break;
				}				
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}





