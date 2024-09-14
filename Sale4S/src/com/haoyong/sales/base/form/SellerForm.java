package com.haoyong.sales.base.form;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.ui.WindowMonitor;

import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.common.derby.DerbyDAO;
import com.haoyong.sales.common.derby.DerbyUpdateListener;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.listener.RemindCountListener;
import com.haoyong.sales.common.listener.RemindInstallorListener;
import com.haoyong.sales.common.listener.SchemaUpdateListener;
import com.haoyong.sales.common.listener.TestCaseListener;
import com.haoyong.sales.common.schedule.ExecutorService;

/**
 * 界面管理——供应商
 */
public class SellerForm extends AbstractForm<Supplier> {
	
	private void testCase() {
		WindowMonitor.getMonitor();
		TestCaseListener listener = new TestCaseListener();
		listener.setManual(true);
		listener.run();
	}
	
	private void testRemind() {
		RemindCountListener count = new RemindCountListener();
		count.setManual(true);
		count.run();
		RemindInstallorListener remind = new RemindInstallorListener();
		remind.setManual(true);
		remind.run();
	}
	
	private void testTemp() throws Exception {
	}
	
	private void fireSchemaUpdate() throws Exception {
		new SchemaUpdateListener().run();
	}
	
	private void fireDerbyUpdate() {
		new DerbyUpdateListener().run();
	}
	
	private void setDerbyExecute() {
		String k="DerbyExecute", r="DerbySelectList", sql=this.getAttr(k);
		DerbyDAO dao = new DerbyDAO(0);
		int rtn = dao.getSession().createSQLQuery(sql).executeUpdate();
		dao.getSession().flush();
		StringBuffer sb = new StringBuffer().append(new Date()).append("\n").append(rtn);
		this.setAttr(r, sb.toString());
	}
	private void setDerbySelect() {
		String k="DerbySelect", r="DerbySelectList", sql=this.getAttr(k);
		DerbyDAO dao = new DerbyDAO(0);
		StringBuffer sb = new StringBuffer().append(new Date()).append("\n");
		int irow=0;
		for (Iterator<Object> iter=dao.getSession().createSQLQuery(sql).list().iterator(); iter.hasNext();) {
			Object[] row = (Object[])iter.next();
			sb.append(++irow).append("：\t").append(Arrays.toString(row)).append("\n");
		}
		this.setAttr(r, sb.toString());
	}
	private void setDerbySellerSelect() {
		String k="DerbySellerSelect", r="DerbySelectList", sql=this.getAttr(k);
		DerbyDAO dao = new DerbyDAO(0);
		StringBuffer sb = new StringBuffer().append(new Date()).append("\n");
		int irow=0;
		for (Iterator<Object> iter=dao.nativeSqlQuery(sql).iterator(); iter.hasNext();) {
			Object[] row = (Object[])iter.next();
			sb.append(++irow).append("：\t").append(Arrays.toString(row)).append("\n");
		}
		this.setAttr(r, sb.toString());
	}
	private void setDerbySellerUpdate() {
		String k="DerbySellerUpdate", r="DerbySelectList", sql=this.getAttr(k);
		DerbyDAO dao = new DerbyDAO(0);
		int rtn = dao.getSQLQuery(sql).executeUpdate();
		dao.getSession().flush();
		StringBuffer sb = new StringBuffer().append(new Date()).append("\n").append(rtn);
		this.setAttr(r, sb.toString());
	}

	public void setDeleteTickets4Service() {
		new TestCaseListener().clearSellerData();
	}

	@Override
	public void setSelectedList(List<Supplier> selected) {
	}
}
