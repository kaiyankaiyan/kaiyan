package com.haoyong.sales.sale.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.webObject.AuditListBuilder;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.ViewBuilder;
import net.sf.mily.webObject.query.Footer;

import org.hibernate.util.JoinedIterator;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.sale.domain.PrintModel;

/**
 * 打印模板Logic
 */
public class PrintModelLogic {

	public PrintModel getPrintModel(ViewBuilder builder) {
		String sql = "select t.* from sa_PrintModel t where t.builder=? and t.sellerId=?";
		PrintModel model = new BaseDAO().nativeQuerySingleResult(sql, PrintModel.class, builder.getFullViewName());
		if (model==null) {
			model = new PrintModel();
			model.setBuilder(builder.getFullViewName());
		}
		this.genPrintModel(model, builder);
		return model;
	}
	
	private PrintModel genPrintModel(PrintModel model, ViewBuilder builder) {
		AuditListBuilder listBuilder = null;
		List<FieldBuilder> ticketList = new ArrayList<FieldBuilder>();
		List<FieldBuilder> detailList = new ArrayList<FieldBuilder>();
		List<Footer> footerList = new ArrayList<Footer>(); 
		for (Iterator<FieldBuilder> iter=builder.getFieldBuilderIterator(); iter.hasNext();) {
			FieldBuilder b = iter.next();
			if (b instanceof AuditListBuilder) {
				listBuilder = (AuditListBuilder)b;
			} else if (b instanceof ViewBuilder) {
				iter = new JoinedIterator(((ViewBuilder)b).getFieldBuilderIterator(), iter);
			} else {
				ticketList.add(b);
			}
		}
		if (listBuilder != null)
		for (Iterator<FieldBuilder> iter=listBuilder.getFieldBuilderIterator(); iter.hasNext();) {
			FieldBuilder b = iter.next();
			if (b instanceof ViewBuilder) {
				iter = new JoinedIterator(((ViewBuilder)b).getFieldBuilderIterator(), iter);
			} else {
				footerList.addAll(b.getFooterList());
				detailList.add(b);
			}
		}
		model.getVoParamMap().put("TicketList", ticketList);
		model.getVoParamMap().put("DetailList", detailList);
		model.getVoParamMap().put("FooterList", footerList);
		return model;
	}
}
