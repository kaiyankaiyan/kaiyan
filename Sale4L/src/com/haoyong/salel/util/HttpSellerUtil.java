package com.haoyong.salel.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.ReflectHelper;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haoyong.salel.base.domain.Seller;
import com.haoyong.salel.base.domain.User;
import com.haoyong.salel.common.form.ActionEnum;

/**
 * 访问商家站点
 */
public class HttpSellerUtil {

	private static String server = SLoginUtil.getProperties().getString(
			"seller.server");

	public void login(Seller seller, User user, String sessionId) {
		StringBuffer sb = new StringBuffer(server).append("CheckInForM.jsp?");
		NameValuePair[] params = new NameValuePair[] {
				new NameValuePair("m", "login"),
				new NameValuePair("s", getSellerJson(seller)),
				new NameValuePair("u", getUserJson(user)),
				new NameValuePair("sid", sessionId) };
		getURL(sb.toString(), params);
	}

	public void action(ActionEnum action, Seller seller, User user) {
		Gson gson = new Gson();
		StringBuffer sb = new StringBuffer(server).append("CheckInForM.jsp?");
		NameValuePair[] params = new NameValuePair[] {
				new NameValuePair("m", "action"),
				new NameValuePair("action", action.name()),
				new NameValuePair("u", getUserJson(user)),
				new NameValuePair("s", getSellerJson(seller)) };
		getURL(sb.toString(), params);
	}

	public void listener(String listenerName, Seller seller) {
		User user = new User();
		user.setUserId("admin");
		user.setUserName("管理员");
		StringBuffer sb = new StringBuffer(server).append("CheckInForM.jsp?");
		NameValuePair[] params = new NameValuePair[] {
				new NameValuePair("m", "listener"),
				new NameValuePair("listener", listenerName),
				new NameValuePair("u", getUserJson(user)),
				new NameValuePair("s", getSellerJson(seller)) };
		getURL(sb.toString(), params);
	}

	private void getURL(String url, NameValuePair[] params) {
		HttpClient httpClient = new HttpClient(new HttpClientParams(),
				new SimpleHttpConnectionManager(true));
		GetMethod httpGet = new GetMethod(url);
		httpGet.setQueryString(params);
		httpGet.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
		httpGet.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		try {
			httpClient.executeMethod(httpGet);
			String encode = httpGet.getResponseCharSet();
			BufferedReader bis = new BufferedReader(new InputStreamReader(httpGet.getResponseBodyAsStream(), encode));
			StringBuffer sb = new StringBuffer();
			String line = null;
			while ((line = bis.readLine()) != null) {
				sb.append(line);
			}
			bis.close();
			Type type = new TypeToken<HttpSellerStatus>() {}.getType();
			HttpSellerStatus status = new Gson().fromJson(sb.toString(), type);
			if (status.getStatus() == 0)
				throw new LogicException(2, status.getMsg()+"");
		} catch (Exception e) {
			StringBuffer sb = new StringBuffer(url).append(e.getMessage());
			for (NameValuePair p: params) {
				sb.append(p.getName()).append(",").append(p.getValue());
			}
			LogUtil.error(sb.toString());
			throw LogUtil.getRuntimeException("访问S站点失败", e);
		} finally {
			httpGet.releaseConnection();
		}
	}

	private String getUserJson(User user0) {
		User user = TicketPropertyUtil.deepClone(user0);
		user.setModifytime(null);
		ReflectHelper.setPropertyValue(user, "state", null);
		ReflectHelper.setPropertyValue(user, "voParamMap", null);
		Gson gson = new Gson();
		Type gtype = new TypeToken<User>() {
		}.getType();
		return gson.toJson(user, gtype);
	}

	private String getSellerJson(Seller seller0) {
		Seller seller = TicketPropertyUtil.deepClone(seller0);
		seller.setModifytime(null);
		ReflectHelper.setPropertyValue(seller, "state", null);
		ReflectHelper.setPropertyValue(seller, "voParamMap", null);
		Gson gson = new Gson();
		Type gtype = new TypeToken<Seller>() {
		}.getType();
		return gson.toJson(seller, gtype);
	}

	private static class HttpSellerStatus {

		private int status;

		private String msg;

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}
	}
}
