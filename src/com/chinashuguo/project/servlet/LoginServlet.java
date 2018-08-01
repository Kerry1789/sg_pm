/**
 * 
 */
package com.chinashuguo.project.servlet;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chinashuguo.project.db.DBUtils;
import com.chinashuguo.project.db.UserInfo;
import com.chinashuguo.project.ldap.LDAPControl;
import com.chinashuguo.project.utils.StringUtils;

/**
 * @author kui.li
 *
 */
public class LoginServlet extends BaseServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub 
		
		String username = request.getParameter("username");
		String password = request.getParameter("password");
	
		if(StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
			boolean rtnVal = LDAPControl.connect("ragentekxian\\" + username, password);
			System.out.println("rtnVal=" + rtnVal);
			if(rtnVal) { // login success !
				System.out.println("login success !");

				UserInfo userInfoBean = new UserInfo();
				
				// check weather in the database, if upgrade the password, else insert the username & password to database
				List<UserInfo> listUser = DBUtils.query("select * from user_info where username = ?", new String[]{username}, UserInfo.class);
				if(listUser != null && listUser.size()>0) { // exists in the databases, need upgrade password
					userInfoBean = listUser.get(0);
					userInfoBean.setPassword(password);
					userInfoBean.setUpdateBy("auto login");
					userInfoBean.setUpdateDate(new Timestamp(System.currentTimeMillis()));
					if(DBUtils.update(userInfoBean)>0) {
						System.out.println("password upgrade success !");
					}
				} else { // not exists , need insert
					userInfoBean = new UserInfo();
					userInfoBean.setUsername(username);
					userInfoBean.setPassword(password);
					userInfoBean.setCreateBy("auto login");
					userInfoBean.setCreateDate(new Timestamp(System.currentTimeMillis()));
					if(DBUtils.insert(userInfoBean)>0) {
						System.out.println("user info insert success !");
					}				
				}
				
				request.getSession().setAttribute("userinfo", userInfoBean);
				request.setAttribute("username", username);
				request.getRequestDispatcher("index.jsp").forward(request, response);
			} else { // login failed !
				System.out.println("login failed !");
				request.setAttribute("error_message", "账号或密码错误 ，请重新登陆！");
				request.getRequestDispatcher("login.jsp").forward(request, response);
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
}
