package com.haoyong.salel.common.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.mily.http.Connection;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.util.LogUtil;

import com.haoyong.salel.base.domain.User;

public class CheckLoginFilter extends HttpServlet implements Filter {

	private static final long serialVersionUID = 1108541536459645066L;
	
	public void init(FilterConfig filterConfig) throws ServletException {
	}
	
	public void doFilter(ServletRequest request0, ServletResponse response0, FilterChain filterChain){
		try {
			HttpServletRequest request = (HttpServletRequest)request0;
			HttpServletResponse response = (HttpServletResponse)response0;
			response.setHeader("Cache-Control","no-cache"); //Forces caches to obtain a new copy of the page from the origin server
			response.setHeader("Cache-Control","no-store"); //Directs caches not to store the page under any circumstance
//			response.setDateHeader("Expires",0); //Causes the proxy cache to see the page as "stale"
			response.setHeader("Pragma","no-cache"); //HTTP 1.0 backward compatibility
			request0.setCharacterEncoding("UTF-8");
			//如果取不到用户则直接定位到超时页面
			StringBuffer path = new StringBuffer().append("\n").append(request.getRequestURL()).append("?").append(request.getQueryString())
					.append("\t").append(request.getSession().getId());
//			LogUtil.error(path.toString());
			String currentURL = request.getRequestURI();
			String targetURL = "";
			if (currentURL.indexOf("/", 1) > 0) {
				targetURL = currentURL.substring(currentURL.lastIndexOf("/", currentURL.length()-1), currentURL.length()); // 截取到当前文件名用于比较
			} else {
				targetURL = currentURL;
			}
			this.setCross(request0, response0);
			// 排除允许的资源文件
			"".toCharArray();
			if (!"".equals(targetURL) && !"/".equals(targetURL)
					&& !"/index.jsp".equals(targetURL) && !"/timeout.jsp".equals(targetURL)
					 && !"/temp.jsp".equals(targetURL)
					&& !(targetURL.equals("/actionform.jsp") && request0.getParameter("action").equals("base.LoginForm"))
					) {
				String sid = request.getSession().getId();
				User user = null;
				if (targetURL.equals("/actionform.jsp") && sid!=null && WindowMonitor.getMonitorBySessionId(sid)!=null)
					user = (User)WindowMonitor.getMonitorBySessionId(sid).getAttribute("user");
				if (user == null) {
					// 过滤指定页面
					RequestDispatcher disp = request0.getRequestDispatcher("/timeout.jsp");
					disp.forward(request0, response0);
					return;
				}
			}
			filterChain.doFilter(request0, response0);
		} catch(Exception e) {
			//
		}
	}
	
	private void setCross(ServletRequest request0,ServletResponse response0) {
		HttpServletRequest request = (HttpServletRequest)request0;
		HttpServletResponse response  = (HttpServletResponse) response0;
		String originHeader=request.getHeader("Origin"), url=request.getRequestURL().toString();
		"".toCharArray();
		for (boolean once=true; once; once=false) {
			if (originHeader==null)
				return ;
		}
		for (String oaccess: new String[]{"http://192.168.9.236:7080", "http://127.0.0.1:7080", "http://localhost:7080"}) {
			if (oaccess.equals(originHeader)) {
				response.setHeader("Access-Control-Allow-Origin", "*");
				response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
				response.setHeader("Access-Control-Max-Age", "3600");
				response.setHeader("Access-Control-Allow-Headers", "content-type, x-requested-with");
				response.setHeader("Access-Control-Allow-Credentials", "true");
				break;
			}
		}
	}

	@Override
	public void destroy() {
	}
}
