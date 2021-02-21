package com.example.wechat.methods;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.wechat.javaBean.ChatBean;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.LoginBean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static java.lang.Thread.sleep;

public class getMysql {
	private static String url = "jdbc:mysql://159.75.27.108:3306/wechat?" + "useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8&allowPublicKeyRetrieval=true";
	private static String user = "root";
	private static String password = "a5550205A!";
	/*private static String url = "jdbc:mysql://192.168.1.2:3306/wechat?" + "useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8&allowPublicKeyRetrieval=true";
	private static String user = "root";
	private static String password = "123456";*/

	public static Connection connectionMysql() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			return DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void register(String email, String password, String user_name, Context context) {
		Connection con = connectionMysql();
		PreparedStatement sql = null;
		try {
			con = getMysql.connectionMysql();
			String insertCondition = "insert into users(email,password,head,username) values (?,?,?,?)";
			sql = con.prepareStatement(insertCondition);
			sql.setString(1, email);
			password = Encrypt.encrypt(password, "java");//加密
			sql.setString(2, password);
			sql.setString(3, "");
			sql.setString(4, user_name);
			if (sql.executeUpdate() != 0) {
				Looper.prepare();
				Toast.makeText(context, "注册成功", Toast.LENGTH_SHORT).show();
			}
		} catch (SQLException exp) {
			Looper.prepare();
			exp.printStackTrace();
			Toast.makeText(context, "该用户名已经注册过了", Toast.LENGTH_SHORT).show();
		} finally {
			try {
				sql.close();
				con.close();
				Looper.loop();
			} catch (Exception ee) {

			}
		}
	}

	public static void login(String email, String password, LoginBean loginBean, Context context) {
		Connection con = connectionMysql();
		Statement sql = null;
		try {
			con = getMysql.connectionMysql();
			String condition = "select * from users where email = '" + email +
					"' and password ='" + password + "'";
			sql = con.createStatement();
			ResultSet rs = sql.executeQuery(condition);
			boolean m = rs.next();
			if (m == true) {
				loginBean.setEmail(email);
				loginBean.setPassword(password);
				loginBean.setLogin(true);
				loginBean.setHead(rs.getString(4));
				loginBean.setMyName(rs.getString(5));

				Looper.prepare();
				Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
				//写在loop()后面的代码不会被执行。
			} else {
				Looper.prepare();
				Toast.makeText(context, "账号或密码错误", Toast.LENGTH_SHORT).show();
			}
		} catch (SQLException exp) {
			exp.printStackTrace();
		} finally {
			try {
				con.close();
				sql.close();
				Looper.loop();
			} catch (Exception ee) {
			}
		}
	}

	public static void getContacts(String email, LoginBean loginBean, List<ContactBean> pythonList, Context context) {
		Connection con = connectionMysql();
		Statement sql = null;
		ContactBean contactbean;
		try {
			con = getMysql.connectionMysql();
			String condition = "select * from userscontacts where ownEmail = '" + email + "'";
			sql = con.createStatement();
			ResultSet rs = sql.executeQuery(condition);
			while (rs.next()) {
				contactbean = new ContactBean(loginBean.getHead());
				contactbean.setContact_email(rs.getString(2));
				contactbean.setMy_name(loginBean.getMyName());
				contactbean.setHead(rs.getString(3));
				contactbean.setContact_name(rs.getString(4));
				contactbean.setMy_email(loginBean.getEmail());
				pythonList.add(contactbean);
			}
		} catch (SQLException exp) {
			exp.printStackTrace();
		} finally {
			try {
				con.close();
				sql.close();
			} catch (Exception ee) {
			}
		}
	}

	/*public static void getMessages(String ownEmail, String contactEmail, List<ChatBean> chatBeanList, String ourHead, String contactHead, Context context) {
		Connection con = connectionMysql();
		Statement sql = null;
		ChatBean chatBean;
		while (true) {
			try {
				con = getMysql.connectionMysql();
				String condition = "select * from usersmessage where (sendUser = '" + ownEmail + "' and receiveUser = '" + contactEmail + "') or (sendUser = '" + contactEmail + "' and receiveUser = '" + ownEmail + "') ";
				//Log.i("???",condition);
				sql = con.createStatement();
				ResultSet rs = sql.executeQuery(condition);
				chatBeanList.clear();
				while (rs.next()) {
					chatBean = new ChatBean();
					if (rs.getString(2).equals(ownEmail)) {
						chatBean.setState(chatBean.SEND); //SEND表示自己发送的信息
						chatBean.setHeadDetail(ourHead);
					} else if (rs.getString(3).equals(ownEmail)) {
						chatBean.setState(chatBean.RECEIVE);
						chatBean.setHeadDetail(contactHead);
					}
					chatBean.setMessage(rs.getString(4));
					chatBeanList.add(chatBean);
				}
				sleep(3000);
			} catch (SQLException | InterruptedException exp) {
				exp.printStackTrace();
			} finally {
				try {
					con.close();
					sql.close();
				} catch (Exception ee) {
				}
			}
		}
	}*/

	/*public static void sendMessage(String sendUser, String receiveUser, String message) {
		Connection con = connectionMysql();
		PreparedStatement sql = null;
		try {
			Log.i("???",sendUser+receiveUser+message);
			con = getMysql.connectionMysql();
			String condition = "insert into usersmessage(sendUser,receiveUser,message) values (?,?,?)";
			sql = con.prepareStatement(condition);
			sql.setString(1, sendUser);
			sql.setString(2, receiveUser);
			sql.setString(3, message);
			sql.executeUpdate();
		} catch (SQLException exp) {
			exp.printStackTrace();
		} finally {
			try {
				con.close();
				sql.close();
			} catch (Exception ee) {
			}
		}
	}*/

	public static void addContact(String email, String contactEmail) {
		Connection con = connectionMysql();
		Statement sql = null;
		PreparedStatement sq = null;
		String headDetail;
		String contactName;
		boolean flag = true;

		try {
			con = getMysql.connectionMysql();
			String condition = "select * from users where email = '" + contactEmail + "'";
			sql = con.createStatement();
			ResultSet rs = sql.executeQuery(condition);
			flag = rs.next();
			headDetail = rs.getString(4);
			contactName = rs.getString(5);
			if (flag == true) {
				condition = "insert into userscontacts(ownEmail,contactEmail,contactHead,contactName) values (?,?,?,?)";
				sq = con.prepareStatement(condition);
				sq.setString(1, email);
				sq.setString(2, contactEmail);
				sq.setString(3, headDetail);
				sq.setString(4, contactName);
				sq.executeUpdate();
			}
			condition = "select * from users where email = '" + email + "'";
			sql = con.createStatement();
			rs = sql.executeQuery(condition);
			flag = rs.next();
			headDetail = rs.getString(4);
			contactName = rs.getString(5);
			if (flag == true) {
				condition = "insert into userscontacts(ownEmail,contactEmail,contactHead,contactName) values (?,?,?,?)";
				sq = con.prepareStatement(condition);
				sq.setString(1, contactEmail);
				sq.setString(2, email);
				sq.setString(3, headDetail);
				sq.setString(4, contactName);
				sq.executeUpdate();
			}

		} catch (SQLException exp) {
			exp.printStackTrace();
		} finally {
			try {
				con.close();
				sql.close();
			} catch (Exception ee) {
			}
		}
	}
	public static void updateHead(String email, String head) {
		Connection con = connectionMysql();
		Statement sql = null;
		try {
			con = getMysql.connectionMysql();
			String condition = "update users set head='"+head+"' where email='"+email+"'";
			sql = con.createStatement();
			sql.executeUpdate(condition);
			condition = "update userscontacts set contactHead='"+head+"' where contactEmail='"+email+"'";
			sql.executeUpdate(condition);
		} catch (SQLException exp) {
			exp.printStackTrace();
		} finally {
			try {
				con.close();
				sql.close();
			} catch (Exception ee) {
			}
		}
	}

}
