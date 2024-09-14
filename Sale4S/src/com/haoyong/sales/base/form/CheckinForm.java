package com.haoyong.sales.base.form;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.http.Connection;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.ReflectHelper;

import org.junit.Assert;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.logic.UserLogic;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.listener.AttrRunnableListener;
import com.haoyong.sales.common.listener.RunnableListener;
import com.haoyong.sales.common.listener.TransRunnableListener;
import com.haoyong.sales.common.schedule.ExecutorService;
import com.haoyong.sales.util.SSaleUtil;

/**
 * 处理login端的请求
 */
public class CheckinForm extends HttpServlet {
	
	private String sessionId;

	public final void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String mname=request.getParameter("m");
		Map<String, String> map = getParameterMap(request);
		this.sessionId = new Connection(null, request, response).getSessionId();
		int status = 1;
		String msg = null;
		try {
			ReflectHelper.invokeMethod(this, mname, map);
			msg = "ok";
		} catch(Exception e) {
			status = 0;
			LogUtil.error("S站点访问失败", e);
			msg = e.getMessage();
		}
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");    
		PrintWriter out = response.getWriter();
		JsonObject json = new JsonObject();
		json.addProperty("status", status);
		json.addProperty("msg", msg);
		out.print(json.toString());
		out.flush();
		new SessionProvider().clear();
	}
	
	private Map<String, String> getParameterMap(HttpServletRequest request) throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
		String decodedQueryString = URLDecoder.decode(new StringBuffer("&").append(request.getQueryString()).append("&").toString(), "UTF-8");
		Map<String, String> map = new HashMap<String, String>();
		int f=0, t=0;
		for (Enumeration<String> keys = request.getParameterNames(); keys.hasMoreElements();) {
			String k=keys.nextElement(), ks="&"+k+"=";
			f=decodedQueryString.indexOf(ks);
			t=decodedQueryString.indexOf("&", f+1);
			String v = decodedQueryString.substring(f+ks.length(), t);
			map.put(k, v);
		}
		return map;
	}

	private void login(HashMap<String, String> map) throws Exception {
		Gson gson = new Gson();
		Seller seller = gson.fromJson(map.get("s"), getSellerType());
		User user = gson.fromJson(map.get("u"), getUserType());
		this.sessionId = map.get("sid");
		if (seller!=null && user!=null && sessionId!=null) {
			WindowMonitor monitor = WindowMonitor.getMonitor(sessionId);
			monitor.addAttribute("seller", seller);
			user = new UserLogic().getUser(user.getUserId());
			Assert.assertTrue("找不到此用户", user!=null);
			monitor.addAttribute("user", user);
//			LogUtil.error(new StringBuffer().append("Seller Login").append(sessionId).append(seller).append(user).toString());
		}
	}
	
	private void action(HashMap<String, String> map) {
		Gson gson = new Gson();
		Seller seller = gson.fromJson(map.get("s"), getSellerType());
		User user = gson.fromJson(map.get("u"), getUserType());
		if (seller!=null && user!=null && sessionId!=null) {
			WindowMonitor monitor = WindowMonitor.getMonitor(sessionId);
			monitor.addAttribute("seller", seller);
			monitor.addAttribute("user", user);
		}
		ActionEnum action = ActionEnum.valueOf(map.get("action"));
		ViewData<Seller> viewData = new ViewData<Seller>();
		viewData.setTicketDetails(seller);
		SSaleUtil.runServiceChannel(action, viewData);
	}
	
	private void listener(HashMap<String, String> map) {
		Gson gson = new Gson();
		Seller seller = gson.fromJson(map.get("s"), getSellerType());
		User user = gson.fromJson(map.get("u"), getUserType());
		String listenerName = map.get("listener");
		Runnable runnable = null;
		try {
			runnable = (Runnable)ReflectHelper.invokeConstructor(ReflectHelper.classForName(listenerName), new Object[0]);
		}catch(Exception e) {
			throw new LogicException(2, new StringBuffer("加载失败").append(listenerName).toString());
		}
		String sessionName = new StringBuffer(runnable.getClass().getSimpleName()).append(runnable.hashCode()).toString();
		if (runnable instanceof TransRunnableListener) {
			TransRunnableListener listener = (TransRunnableListener)runnable;
			if (listener.isRunnable()==false)
				return;
			listener.setSessionName(sessionName);
			WindowMonitor monitor = WindowMonitor.getMonitor(sessionName);
			monitor.addAttribute("seller", seller);
			monitor.addAttribute("user", user);
			monitor.addAttribute("width", 1280);
			ExecutorService.single(listener);
		} else if (runnable instanceof AttrRunnableListener) {
			AttrRunnableListener listener = (AttrRunnableListener)runnable;
			if (listener.isRunnable()==false)
				return;
			listener.setSessionName(sessionName);
			WindowMonitor monitor = WindowMonitor.getMonitor(sessionName);
			monitor.addAttribute("seller", seller);
			monitor.addAttribute("user", user);
			monitor.addAttribute("width", 1280);
			ExecutorService.single(listener);
		} else if (runnable instanceof RunnableListener) {
			RunnableListener listener = (RunnableListener)runnable;
			if (listener.isRunnable()==false)
				return;
			ExecutorService.single(listener);
		}
	}
	
	private Type getUserType() {
		Type gtype = new TypeToken<User>(){}.getType();
		return gtype;
	}
	
	private Type getSellerType() {
		Type gtype = new TypeToken<Seller>(){}.getType();
		return gtype;
	}
}
