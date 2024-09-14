package com.haoyong.salel.common.form;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.mily.attributes.AttrFunctionName;
import net.sf.mily.attributes.AttrScriptName;
import net.sf.mily.attributes.AttributeName;
import net.sf.mily.attributes.ClientEventName;
import net.sf.mily.attributes.StyleName;
import net.sf.mily.http.Connection;
import net.sf.mily.server.ActionForm;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.DesUtil;
import net.sf.mily.ui.BlockGrid;
import net.sf.mily.ui.BlockGrid.BlockCell;
import net.sf.mily.ui.Button;
import net.sf.mily.ui.CheckBox;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.HtmlText;
import net.sf.mily.ui.PCenter;
import net.sf.mily.ui.PasswordField;
import net.sf.mily.ui.Text;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.ui.enumeration.BlockGridMode;
import net.sf.mily.ui.event.ActionListener;
import net.sf.mily.ui.event.ChangeListener;
import net.sf.mily.ui.event.EventObject;
import net.sf.mily.ui.facable.Former;
import net.sf.mily.util.LogUtil;
import net.sf.mily.webObject.query.SqlListBuilderSetting;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Test;

import com.haoyong.salel.base.domain.Seller;
import com.haoyong.salel.base.domain.User;
import com.haoyong.salel.base.logic.SellerLogic;
import com.haoyong.salel.base.logic.UserLogic;
import com.haoyong.salel.common.dao.User4sLogic;
import com.haoyong.salel.util.HttpSellerUtil;
import com.haoyong.salel.util.SLoginUtil;

public class LoginForm extends AbstractForm implements Former {
	
	protected void beforeWindow(Window window) {
	}
	
	public Component getComponent() {
		PCenter pcenter = new PCenter();
		pcenter.setIdentifier("login_bg").addAttribute(ClientEventName.InitScript1, "this.style.height=document.body.clientHeight;");
		pcenter.addAttribute(AttrFunctionName.FShow, new StringBuffer()
			.append("_userSave.checked=eval(hexDecode(window.localStorage.userSave));")
			.append("if(_userSave.checked) {")
			.append("  _sellerName.value=hexDecode(window.localStorage.sellerName);")
			.append("  _userId.value=hexDecode(window.localStorage.userId);")
			.append("  _userPsw.value=hexDecode(window.localStorage.userPsw);")
			.append("}")
			.append("if(_userPsw.value.length>1)	_submit.focus();")
			.toString());
		pcenter.addAttribute(AttrFunctionName.FHide, new StringBuffer()
			.append("var tinput=null;")
			.append("L_propertychange(tinput=_sellerName,tinput.value);")
			.append("L_propertychange(tinput=_userId,tinput.value);")
			.append("L_propertychange(tinput=_userPsw,tinput.value);")
			.append("if(window.localStorage.setItem) {")
			.append("  window.localStorage.setItem('userSave',hexEncode(_userSave.checked+''));")
			.append("  if(_userSave.checked) {")
			.append("    window.localStorage.setItem('sellerName',hexEncode(_sellerName.svalue));")
			.append("    window.localStorage.setItem('userId',hexEncode(_userId.svalue));")
			.append("    window.localStorage.setItem('userPsw',hexEncode(_userPsw.svalue));")
			.append("  }")
			.append("}").toString());
		pcenter.addAttribute(ClientEventName.InitScript2, "this.fshow();");
		pcenter.addCSS("css/login.css");
		BlockGrid gout = new BlockGrid().createGrid(3);
		Text otd11font = new Text(SLoginUtil.getProperties().getString("project.name"));
		otd11font.addAttribute(ClientEventName.InitScript0, new StringBuffer("window.document.title='").append(otd11font.getText()).append("'").toString());
		otd11font.addStyle(StyleName.COLOR, "white").addStyle(StyleName.FONT_WEIGHT, "900").addStyle(StyleName.FONT_SIZE, "28px");
		BlockCell otd11 = gout.append(otd11font, 3, 1).addStyle(StyleName.BACKGROUND, "#2878B0").addStyle(StyleName.TEXT_ALIGN, "center").addStyle(StyleName.Padding, "5 0 8 0");
		BlockCell otd21 = gout.append(new Text("　　　　　")).addStyle(StyleName.BACKGROUND, "#CED8E1").addStyle(StyleName.Border_Right, "1px solid #9BA4AB");
		BlockGrid ingrid = new BlockGrid().createGrid(2, BlockGridMode.Independent);
		Text tr1font = new Text("商　家：");
		tr1font.addStyle(StyleName.FONT_SIZE, "16");
		TextField tr1input = new TextField();
		tr1input.setIdentifier("_sellerName").addAttribute(ClientEventName.ONKEYUP, "if (event.keyCode==13) {_userId.focus();}");
		tr1input.addAttribute(AttributeName.PLACEHOLDER, "请输入商家").addStyle(StyleName.Margin, "0").addStyle(StyleName.Padding, "0").addStyle(StyleName.WIDTH, "135px").addStyle(StyleName.HEIGHT, "22px");
		ingrid.append(tr1font).getBlockRow().addStyle(StyleName.HEIGHT, "30");
		ingrid.append(tr1input);
		Text tr2font = new Text("用户名：");
		tr2font.addStyle(StyleName.FONT_SIZE, "16");
		TextField tr2input = new TextField();
		tr2input.setIdentifier("_userId").addAttribute(ClientEventName.ONKEYUP, "if (event.keyCode==13) {_userPsw.focus();}");
		tr2input.addAttribute(AttributeName.PLACEHOLDER, "请输入用户名").addStyle(StyleName.Margin, "0").addStyle(StyleName.Padding, "0").addStyle(StyleName.WIDTH, "135px").addStyle(StyleName.HEIGHT, "22px");
		ingrid.append(tr2font).getBlockRow().addStyle(StyleName.HEIGHT, "30");
		ingrid.append(tr2input);
		Text tr3font = new Text("密　码：");
		tr3font.addStyle(StyleName.FONT_SIZE, "16");
		PasswordField tr3input = new PasswordField();
		tr3input.setIdentifier("_userPsw").addAttribute(ClientEventName.ONKEYUP, "if (event.keyCode==13) {_submit.onclick();}");
		tr3input.addStyle(StyleName.Margin, "0").addStyle(StyleName.Padding, "0").addStyle(StyleName.WIDTH, "135px").addStyle(StyleName.HEIGHT, "22px");
		ingrid.append(tr3font).getBlockRow().addStyle(StyleName.HEIGHT, "30");
		ingrid.append(tr3input);
		BlockGrid g = new BlockGrid().createGrid(2, BlockGridMode.Independent);
		CheckBox check = new CheckBox();
		check.setIdentifier("_userSave");
		check.setSelected(true);
		g.append(new Component[]{check, new HtmlText("记住密码　　　")}, 1, 1);
		Button submit = new Button("确认登录");
		submit.setIdentifier("_submit").addAttribute(ClientEventName.ONCLICK, new StringBuffer().append("login_bg.fhide();").toString());
		submit.getEventListenerList().addActionListener(OnSubmitListener.listener);
		g.append(submit);
		ingrid.append(g, 2, 1).addStyle(StyleName.TEXT_ALIGN, "center");
		BlockCell otd22 = gout.append(ingrid).addStyle(StyleName.BACKGROUND,	"#DDE8E9").addStyle(StyleName.Padding, "15 20").addStyle(StyleName.TEXT_ALIGN, "center");
		BlockCell otd23 = gout.append(new Text("　　　　　")).addStyle(StyleName.BACKGROUND, "#CED8E1").addStyle(StyleName.Border_Left, "1px solid #9BA4AB");
		Text font = new Text();
		font.addStyle(StyleName.COLOR, "red").addStyle(StyleName.FONT_SIZE, "16");
		BlockCell otd31 = gout.append(font.setIdentifier("_error"), 3, 1).addStyle(StyleName.TEXT_ALIGN, "center");
		otd31.getBlockRow().addStyle(StyleName.BACKGROUND, "#2878B0").addStyle(StyleName.HEIGHT, "40px");
		pcenter.add(gout);
		return pcenter;
	}
	
	@Test
	public String getLoginView(String sessionId, String url) {
//		Connection conn=(Connection)this.getAttribute("conn");
//		conn.getRequest().getParameterMap();
		Window win = new Window();
		String slist[]=url.split("\\=|\\&"), saction=slist[1];
		win.addAttribute(AttrScriptName.Url, "'"+url+"'");
		Object action = new ActionForm().getAction(saction, win);
		win.setFormer(action);
		this.beforeWindow(win);
		WindowMonitor.getMonitor(sessionId).addWindow(win);
		StringBuffer writer = new StringBuffer();
		String wid=win.getIdentifier();
		if ("script".length()>0) {
			writer.append("<div id='").append(wid).append("'></div>\n")
			.append("<script type='text/javascript'>\n")
			.append("var f=").append(wid).append(", w=f.clientWidth, h=document.body.clientHeight, p=window.location.search.substring(1);\n")
			.append("var req=['").append(url).append("', ['_action=updator', '&win',f.id,'=inited', ")
			.append("'&attr_width='+w,'&attr_height='+h,p, ")
			.append("'&attr_expltype='+(isFirefox?'isFirefox':'')+(isIE?'isIE':'')+(isOpera?'isOpera':'')+(isSafari?'isSafari':'')+(isMobile?'isMobile':''),")
			.deleteCharAt(writer.length()-1).append("].join('')];\n")
			.append("compList.requests.unshift(req);\n")
			.append("</script>");
		}
		return writer.toString();
	}
	
	private static class OnSubmitListener implements ActionListener, ChangeListener {
		
		private final String coname = SLoginUtil.getProperties().getString("seller.co");
		private final String svname = SLoginUtil.getProperties().getString("seller.server");
		
		public static OnSubmitListener listener = new OnSubmitListener();

		@Override
		public void perform(EventObject event) {
			PCenter pcenter = event.getSource().searchParentByClass(PCenter.class);
			Text error = pcenter.getInnerId("_error");
			CheckBox check = pcenter.getInnerComponentList(CheckBox.class).get(0);
			TextField ftext = null;
			try {
				String sseller=(ftext=pcenter.getInnerId("_sellerName")).getText(), suser=(ftext=pcenter.getInnerId("_userId")).getText(), spsw=(ftext=pcenter.getInnerId("_userPsw")).getText();
				if (StringUtils.isEmpty(sseller))
					throw new LogicException(2, "请输入商家名称");
				User user = isValidUser(sseller, suser, spsw, check.isSelected());
				Connection conn = (Connection)WindowMonitor.getMonitor().getAttribute("conn");
				if (sseller.equals(coname)) {
					WindowMonitor.getMonitor().addAttribute("user", user);
					StringBuffer script = new StringBuffer();
					script.append("let _0fw=_0fwp.querySelector('div');");
					script.append("_0fw.parentNode.url='").append("actionform.jsp?action=base.MainForm';");
					script.append("compList.requests.unshift([_0fw.parentNode.url,['_action=initor','&_fw=',_0fw.id].join('')]);");
					event.getSource().addAttribute(ClientEventName.InitScript0, script.toString());
				} else {
					String sessionid = conn.getRequest().getSession().getId();
					new HttpSellerUtil().login((Seller)WindowMonitor.getMonitor().getAttribute("seller"), user, sessionid);
					StringBuffer script = new StringBuffer("window.location='").append(svname).append("index.jsp?sid=").append(sessionid).append("';");
					event.getSource().addAttribute(ClientEventName.InitScript0, script.toString());
				}
			} catch(Exception e) {
				LogUtil.error("登录失败", e);
				error.setText(LogUtil.getCauseMessage(e));
			}
		}
		
		public User isValidUser(String sellerName, String userId, String psw, boolean remember) {
			User user = null;
			if (sellerName.equals(coname)) {
				user = new UserLogic().getUser(userId);
			} else {
				Seller seller = new SellerLogic().getSeller(sellerName);
				if (seller != null) {
					WindowMonitor.getMonitor().addAttribute("seller", seller);
					user = new User4sLogic().getUser(userId);
				}
			}
			if (!(user!=null && new EqualsBuilder().append(user.getPassword(), new DesUtil().getEncrypt(psw)).isEquals())) {
				throw new LogicException(2, "用户名密码错误");
			}
			return user;
		}
	}
	
	@Override
	public String getActionMark() {
		return null;
	}
	
	protected SqlListBuilderSetting genSqlListBuilderSetting() {
		return null;
	}

	@Override
	public ConcurrentLinkedQueue<SqlListBuilderSetting> loadSqlListBuilderSettings() {
		return null;
	}

	@Override
	public void saveSqlListBuilderSetting(SqlListBuilderSetting sqlListBuilderSetting) {
		
	}

	@Override
	public void setSelectedList(List selected) {
	}
}
