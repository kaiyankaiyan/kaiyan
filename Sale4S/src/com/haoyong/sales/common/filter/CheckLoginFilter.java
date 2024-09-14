package com.haoyong.sales.common.filter;

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

import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.util.LogUtil;

import com.haoyong.sales.base.domain.User;

public class CheckLoginFilter extends HttpServlet implements Filter {

	private static final long serialVersionUID = 1108541536459645066L;
	
	public void init(FilterConfig filterConfig) throws ServletException {
	}
	
	public void doFilter(ServletRequest request0,ServletResponse response0,FilterChain filterChain){
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
					.append("\t sid_param").append(request.getParameter("sid")).append("_request").append(request.getSession().getId())
					;
//			LogUtil.error(path.toString());
			String currentURL = request.getRequestURI();
			String targetURL = "";
			if (currentURL.indexOf("/", 1) > 0) {
				targetURL = currentURL.substring(currentURL.lastIndexOf("/", currentURL.length()-1), currentURL.length()); // 截取到当前文件名用于比较
			} else {
				targetURL = currentURL;
			}
			this.setCross(request, response);
			
			// 排除允许的资源文件
			if (!"".equals(targetURL) && !"/".equals(targetURL)
					&& !"/temp.jsp".equals(targetURL) && !"/timeout.jsp".equals(targetURL) && !"/success.jsp".equals(targetURL)
					&& !"/CheckInForM.jsp".equals(targetURL)
					) {
				if (this.isLogined(targetURL, request)==false) {
					// 过滤指定页面
					RequestDispatcher disp = request0.getRequestDispatcher("/timeout.jsp");
					disp.forward(request0, response0);
					return;
				}
			}
			filterChain.doFilter(request0, response0);
		} catch (Throwable e) {
			LogUtil.error(e);
		}
	}
	
	private boolean isLogined(String targetURL, HttpServletRequest request) {
		String from2Sid=request.getParameter("sid"), to2Sid=request.getSession().getId();
		if ("/index.jsp".equals(targetURL) && from2Sid!=null && "进网站".length()>0) {
			WindowMonitor from=WindowMonitor.getMonitorBySessionId(from2Sid), to=WindowMonitor.getMonitor(to2Sid);
			if (from!=null) {
				Object seller=from.getAttribute("seller"), user=from.getAttribute("user");
				to.addAttribute("seller", seller);
				to.addAttribute("user", user);
				from.close();
			}
			User user = (User)to.getAttribute("user");
			return user!=null;
		} else if ("/actionform.jsp".equals(targetURL)) {
			User user = (User)WindowMonitor.getMonitor(to2Sid).getAttribute("user");
			return user!=null;
		} else {
			return true;
		}
	}
	
	private void setCross(HttpServletRequest request, HttpServletResponse response) {
		String originHeader=request.getHeader("Origin"), url=request.getRequestURL().toString();
		for (boolean once=1==0; once; once=false) {
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
