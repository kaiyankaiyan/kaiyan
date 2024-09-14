package com.haoyong.sales.test.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import net.sf.mily.support.tools.DesUtil;
import net.sf.mily.types.DateTimeType;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.TextFieldBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.form.BaseImportForm;
import com.haoyong.sales.base.form.UserForm;
import com.haoyong.sales.base.logic.UserLogic;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.sale.form.GlobalSearchForm;
import com.haoyong.sales.util.SSaleUtil;

public class UserTest extends AbstractTest<UserForm> {
	
	private static HashMap<String, User> loadList = new HashMap<String, User>();
	
	public UserTest() {
		this.setForm(new UserForm());
	}
	
	private void check用户__1新增(String... propValues) {
		this.loadView("DeptUserList");
		this.onMenu("新增");
		List<String> propList = new ArrayList<String>();
		for (Iterator<String> iter=Arrays.asList(propValues).iterator(); iter.hasNext();) {
			String fieldName = iter.next();
			String value = iter.next();
			if (value==null)
				continue;
			propList.add(fieldName);
			propList.add(value);
			this.setFieldText(fieldName, value);
		}
		User user = this.getForm().getDomain();
		this.onMenu("提交");
		Assert.assertTrue("初始密码为用户名+登录名", user.getId()>0 && StringUtils.equals(new DesUtil().getDecrypt(user.getPassword()), user.getUserName().concat(user.getUserId())));
		this.loadView("DeptUserList", propList.toArray(new Object[0]));
		Assert.assertTrue("新增1个用户", this.getListViewValue().size()==1);
	}
	
	private void check用户__1编辑address_2删除_3改密码为userid_4重置密码(char type, Object... filters) {
		Date checkTime = new Date();
		if ("1、改联系地址".length()>0 && type=='1') {
			this.loadView("DeptUserList", filters);
			this.setSqlAllSelect(1);
			this.onMenu("编辑");
			String address = "地址".concat(new DateTimeType().format(new Date()));
			this.setFieldText("address", address);
			User user=this.getForm().getDomain(), suser=user.getSnapShot();
			this.onMenu("提交");
			Assert.assertTrue("编辑用户名", user.getModifytime().after(checkTime) && StringUtils.equals(user.getAddress(), suser.getAddress())==false);
		}
		if ("2、删除用户".length()>0 && type=='2') {
			this.loadView("DeptUserList", filters);
			this.setSqlAllSelect(1);
			this.onMenu("删除");
			User user = this.getForm().getSelectFormer4User().getFirst();
			Assert.assertTrue("删除用户", user.getId()<0);
		}
		if ("3、改自己的密码".length()>0 && type=='3') {
			GlobalSearchForm gform = new GlobalSearchForm();
			gform.setFormProperty("attrMap.UserForm", this.getForm());
			this.loadFormView(gform, "UserPsw");
			User user=this.getForm().getDomain();
			this.setFieldText("password", user.getUserId());
			this.setFieldText("voParamMap.password", user.getUserId());
			this.onMenu("提交");
			Assert.assertTrue("编辑密码", user.getModifytime().after(checkTime) && StringUtils.equals(user.getPassword(), new DesUtil().getEncrypt(user.getUserId())));
		}
		if ("4、重置用户密码".length()>0 && type=='4') {
			this.loadView("DeptUserList", filters);
			this.setSqlAllSelect(1);
			this.onMenu("重置密码");
			User user = this.getForm().getSelectFormer4User().getFirst();
			Assert.assertTrue("重置密码为用户名+登录名", user.getModifytime().after(checkTime) && StringUtils.equals(user.getPassword(), new DesUtil().getEncrypt(user.getUserName().concat(user.getUserId()))));
		}
	}
	
	private void check导入用户(String sDomains, String sIndexs) {
		this.getForm().prepareImport();
		this.loadView("ImportUser");
		if (sIndexs != null) {
			ViewBuilder viewBuilder = this.getEditListView().getViewBuilder();
			LinkedHashMap<String, String> mapFieldIndex = new LinkedHashMap<String, String>(); 
			for (Iterator fiter=this.getEditListView().getViewBuilder().getFieldBuilderLeafs().iterator(), citer=Arrays.asList(sIndexs.split("\\t")).iterator(); fiter.hasNext() && citer.hasNext();) {
				FieldBuilder builder = (FieldBuilder)fiter.next();
				if (builder.getClass() != TextFieldBuilder.class)
					continue;
				String index = (String)citer.next();
				StringBuffer name = new StringBuffer();
				for (FieldBuilder cur=builder; StringUtils.equals(cur.getName(),viewBuilder.getName())==false; cur=cur.getViewBuilder()) {
					name.insert(0, cur.getName().concat("."));
				}
				if (StringUtils.isBlank(index)==false)
					mapFieldIndex.put(name.deleteCharAt(name.length()-1).toString(), index);
			}
			BaseImportForm imform = this.getForm().getFormProperty("attrMap.BaseImportForm");
			SellerViewInputs inputs = imform.getFormProperty("attrMap.SellerViewInputs");
			inputs.getInputs().clear();
			inputs.getInputs().putAll(mapFieldIndex);
			imform.getSellerIndexes(this.getEditListView().getComponent());
			this.getForm().setFormProperty("attrMap.FieldIndex", mapFieldIndex);
		}
		this.getForm().getDomain().getVoParamMap().put("Remark", sDomains);
		this.onMenu("导入格式化");
		this.onMenu("导入提交");
		if (sIndexs!=null) {
			this.loadView("ImportUser");
			SellerViewInputs inputs = this.getForm().getFormProperty("attrMap.BaseImportForm.attrMap.SellerViewInputs");
			LinkedHashMap<String, String> mapFieldIndex = (LinkedHashMap<String, String>)this.getForm().getFormProperty("attrMap.FieldIndex");
			Assert.assertTrue("有保存列序号，能加载出来", inputs.getId()>0 && inputs.getInputs().keySet().containsAll(mapFieldIndex.keySet()) && inputs.getInputs().values().containsAll(mapFieldIndex.values()));
		}
		if (true) {
			List<User> importList = this.getForm().getImportList();
			User user=importList.get(importList.size()-1), suser=user.getSnapShot();
			Assert.assertTrue("用户密码加密保存", user.getId()>0 && StringUtils.equals(new DesUtil().getEncrypt(suser.getPassword()), user.getPassword())==false);
		}
	}
	
	public void test用户() {
if (1==1) {
		if ("新增错误验证".length()>0) {
			if ("新增界面".length()>0) {
				this.loadView("DeptUser");
				Assert.assertTrue("不用写密码", this.hasField("password")==false);
			}
			if ("不写部门".length()>0) {
				try {
					this.check用户__1新增("deptName", null, "userId", "userId", "userName", "userName");
					Assert.fail("没有部门，应不成功");
				}catch(Exception e) {
				}
			}
			if ("不写登录名".length()>0) {
				try {
					this.check用户__1新增("deptName", "deptName", "userId", null, "userName", "userName");
					Assert.fail("没有登录名，应不成功");
				}catch(Exception e) {
				}
			}
			if ("不写姓名".length()>0) {
				try {
					this.check用户__1新增("deptName", "deptName", "userId", "userId", "userName", null);
					Assert.fail("没有姓名，应不成功");
				}catch(Exception e) {
				}
			}
			if ("新增用户1，不能新增同userId".length()>0) {
				String userId = ""+Calendar.getInstance().getTimeInMillis();
				this.check用户__1新增("deptName", "deptName", "userId", userId, "userName", "姓名".concat(userId));
				try {
					this.check用户__1新增("deptName", "deptName", "userId", userId, "userName", "姓名".concat(userId));
					Assert.fail("新增同登录名，应不成功");
				}catch(Exception e) {
				}
			}
		}
}
		if ("新增用户，编辑，改密码，重置密码，删除".length()>0) {
			String userId = ""+Calendar.getInstance().getTimeInMillis();
			this.check用户__1新增("deptName", "deptName", "userId", userId, "userName", "姓名".concat(userId));
			this.check用户__1编辑address_2删除_3改密码为userid_4重置密码('1', "userId", userId);
			this.setTransUser(new UserLogic().getUser(userId));
			this.check用户__1编辑address_2删除_3改密码为userid_4重置密码('3');
			this.setTestStart();
			this.check用户__1编辑address_2删除_3改密码为userid_4重置密码('4', "userId", userId);
			this.check用户__1编辑address_2删除_3改密码为userid_4重置密码('2', "userId", userId);
		}
	}
	
	public void test导入用户() {
		this.setQ清空();
		if ("列序号记忆保存".length()>0) {
			StringBuffer sIndexs = new StringBuffer();
			for (int i=0; i<10; i++, sIndexs.append(i).append("\t"));
			this.check导入用户(sIndexs.toString(), sIndexs.deleteCharAt(sIndexs.length()-1).toString());
		}
	}

	public User getUser安装师傅01() {
		String name="安装师傅01";
		if (loadList.containsKey(name))
			return (User)loadList.get(name);
		String sql = "select t.* from bs_user t where t.linkType=0 and t.deptName='安装师傅们' and t.userName=? and t.sellerId=?";
		User user = new BaseDAO().nativeQueryFirstResult(sql, User.class, name);
		if (user == null) {
			user = new User();
			user.setLinkType(0);
			user.setDeptName("安装师傅们");
			user.setUserId("anzh01");
			user.setUserName(name);
			user.setPassword(new DesUtil().getEncrypt("123"));
			user.setLinkerCall("18012341201");
			user.setAddress("安装师傅01的地址");
			SSaleUtil.saveOrUpdate(user);
			loadList.put(name, user);
		}
		return user;
	}
	public User getUser安装师傅02() {
		String name="安装师傅02";
		if (loadList.containsKey(name))
			return (User)loadList.get(name);
		String sql = "select t.* from bs_user t where t.linkType=0 and t.deptName='安装师傅们' and t.userName=? and t.sellerId=?";
		User user = new BaseDAO().nativeQueryFirstResult(sql, User.class, name);
		if (user == null) {
			user = new User();
			user.setLinkType(0);
			user.setDeptName("安装师傅们");
			user.setUserId("anzh02");
			user.setUserName(name);
			user.setPassword(new DesUtil().getEncrypt("123"));
			user.setLinkerCall("18012341202");
			user.setAddress("安装师傅02的地址");
			SSaleUtil.saveOrUpdate(user);
			loadList.put(name, user);
		}
		return user;
	}
	public User getUser管理员() {
		String name="管理员";
		if (loadList.containsKey(name))
			return (User)loadList.get(name);
		String sql = "select t.* from bs_user t where t.linkType=0 and t.userName=? and t.sellerId=?";
		User uadmin = new BaseDAO().nativeQueryFirstResult(sql, User.class, name);
		if (uadmin == null) {
			uadmin = new User();
			uadmin.setLinkType(0);
			uadmin.setUserId("admin");
			uadmin.setUserName(name);
			uadmin.setPassword(new DesUtil().getEncrypt("123"));
			uadmin.setLinkerCall("18012341200");
			uadmin.setAddress("管理员的地址0000");
			SSaleUtil.saveOrUpdate(uadmin);
			loadList.put(name, uadmin);
		}
		return uadmin;
	}
	public User getUser临时(String userName) {
		User user = new User();
		user.setUserName(userName);
		return user;
	}
	
	private User getRole领班人员() {
		String name="领班人员";
		if (loadList.containsKey(name))
			return loadList.get(name);
		String sql = "select t.* from bs_user t where t.linkType=1 and t.deptName='领班人员' and t.sellerId=?";
		User role = new BaseDAO().nativeQueryFirstResult(sql, User.class);
		if (role == null) {
			role = new User();
			role.setLinkType(1);
			role.setDeptName("领班人员");
			SSaleUtil.saveOrUpdate(role);
			loadList.put(name, role);
		}
		sql = "select t.* from bs_user t where t.linkType=21 and t.deptName='领班人员' and t.userName='安装师傅们' and t.sellerId=?";
		User rdept = new BaseDAO().nativeQueryFirstResult(sql, User.class);
		if (rdept == null) {
			rdept = new User();
			rdept.setLinkType(21);
			rdept.setDeptName("领班人员");
			rdept.setUserName("安装师傅们");
			SSaleUtil.saveOrUpdate(rdept);
		}
		return role;
	}
	
	public void setQ清空() {
		List<String> sqlList = new ArrayList<String>();
		if ("删除岗位,权限分享".length()>0) { 
			StringBuffer sql = new StringBuffer("delete from bs_user where (1=0")
			.append(" or (linkType=0 and deptName is not null)")
			.append(" or (linkType=1)")
			.append(" or (linkType=21)")
			.append(") and sellerId=?");
			sqlList.add(sql.toString());
		}
		if ("重置密码123".length()>0)
			sqlList.add("update bs_user set password='iGHAjOHDJvo=' where linkType=0 and (sellerId=? or sellerId=17);");
		SSaleUtil.executeSqlUpdate(sqlList.toArray(new String[0]));
		loadList.clear();
		this.getRole领班人员();
	}
}
