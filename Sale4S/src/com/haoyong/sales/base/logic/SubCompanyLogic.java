package com.haoyong.sales.base.logic;

import java.util.List;

import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.builder.EqualsBuilder;

import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.base.domain.SubCompanyT;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.form.SubCompanyForm;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;

public class SubCompanyLogic {
	
	public PropertyChoosableLogic.Choose12<SubCompanyForm, SubCompany, SubCompanyT> getPropertyChoosableLogic() {
		return new PropertyChoosableLogic.Choose12<SubCompanyForm, SubCompany, SubCompanyT>(new SubCompanyForm(), new SubCompany(), new SubCompanyT());
	}
	
	public PropertyChoosableLogic.TicketDetail<SubCompanyForm, SubCompany, SubCompanyT> getLinkChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<SubCompanyForm, SubCompany, SubCompanyT>(new SubCompanyForm(), new SubCompany(), new SubCompanyT());
	}
	
	public void fromTrunk(SubCompany to, SubCompany from) {
		getPropertyChoosableLogic().fromTrunk(to, from);
	}
	
	public SellerViewSetting getViewSetting() {
		return getPropertyChoosableLogic().getChooseSetting( this.getPropertyChoosableLogic().getChooseBuilder() );
	}
	
	public ViewBuilder getViewBuilder() {
		return getPropertyChoosableLogic().getChooseBuilder();
	}
	
	public SubCompany getSubCompany(Supplier link) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_company t where t.dtype=3 and t.subNameH like '%").append(link.getSubmitNumber()).append("%' and t.sellerId=?");
		SubCompany subcompany = new BaseDAO().nativeQuerySingleResult(sql.toString(), SubCompany.class);
		if (subcompany != null) {
			SubCompany accept = subcompany;
			EqualsBuilder builder = new EqualsBuilder();
			builder.append(accept.getSubmitNumber(), link.getSubmitNumber());
			builder.append(accept.getSubmitType(), link.getSubmitType());
			builder.append(subcompany.getSellerId(), link.getToSellerId());
			builder.append(accept.getUaccept(), link.getUaccept());
			if (builder.isEquals())
				return subcompany;
		}
		return null;
	}
	
	public SubCompany getSubCompanyByNumber(String number) {
		String sql = "select t.* from bs_company t where t.dtype=3 and t.number=? and t.sellerId=?";
		SubCompany company = new BaseDAO().nativeQuerySingleResult(sql, SubCompany.class, number);
		return company;
	}
	
	public boolean hasRepeat(SubCompany domain, String column, Object value) {
		String sql = new StringBuffer("select t.* from bs_company t where t.dtype=3")
		.append(" and t.").append(column).append("=?").append(" and t.id!=?")
		.append(" and t.sellerId=?").toString();
		List list = new BaseDAO().nativeSqlQuery(sql, value, domain.getId());
		return list.size()>0;
	}
}