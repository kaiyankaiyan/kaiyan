package com.haoyong.sales.base.logic;

import java.util.List;

import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.builder.EqualsBuilder;

import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.ClientT;
import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.form.ClientForm;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;

public class ClientLogic {
	
	public PropertyChoosableLogic.Choose12<ClientForm, Client, ClientT> getPropertyChoosableLogic() {
		return new PropertyChoosableLogic.Choose12<ClientForm, Client, ClientT>(new ClientForm(), new Client(), new ClientT());
	}
	
	public PropertyChoosableLogic.TicketDetail<ClientForm, Client, ClientT> getLinkChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<ClientForm, Client, ClientT>(new ClientForm(), new Client(), new ClientT());
	}
	
	public void fromTrunk(Client to, Client from) {
		getPropertyChoosableLogic().fromTrunk(to, from);
	}
	
	public SellerViewSetting getViewSetting() {
		return getPropertyChoosableLogic().getChooseSetting( this.getPropertyChoosableLogic().getChooseBuilder() );
	}
	
	public ViewBuilder getViewBuilder() {
		return getPropertyChoosableLogic().getChooseBuilder();
	}
	
	public Client getClient(Supplier link) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_company t where t.dtype=2 and t.clientNameH like '%").append(link.getSubmitNumber()).append("%' and t.sellerId=?");
		Client client = new BaseDAO().nativeQuerySingleResult(sql.toString(), Client.class);
		if (client != null) {
			Client accept = client;
			EqualsBuilder builder = new EqualsBuilder();
			builder.append(accept.getSubmitNumber(), link.getSubmitNumber());
			builder.append(accept.getSubmitType(), link.getSubmitType());
			builder.append(client.getSellerId(), link.getToSellerId());
			builder.append(accept.getUaccept(), link.getUaccept());
			if (builder.isEquals())
				return client;
		}
		return null;
	}
	
	public Client getClientByNumber(String number) {
		String sql = "select t.* from bs_company t where t.dtype=2 and t.number=? and t.sellerId=?";
		Client client = new BaseDAO().nativeQueryFirstResult(sql, Client.class, number);
		return client;
	}
	public Client getClientByName(String name) {
		String sql = "select t.* from bs_company t where t.dtype=2 and t.name=? and t.sellerId=?";
		Client client = new BaseDAO().nativeQueryFirstResult(sql, Client.class, name);
		return client;
	}
	
	public boolean hasRepeat(Client domain, String column, Object value) {
		String sql = new StringBuffer("select t.* from bs_company t where t.dtype=2")
		.append(" and t.").append(column).append("=?").append(" and t.id!=?")
		.append(" and t.sellerId=?").toString();
		List list = new BaseDAO().nativeSqlQuery(sql, value, domain.getId());
		return list.size()>0;
	}
}