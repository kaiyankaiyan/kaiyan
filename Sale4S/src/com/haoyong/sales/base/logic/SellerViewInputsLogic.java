package com.haoyong.sales.base.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.common.derby.DerbyDAO;

public class SellerViewInputsLogic {

	public SellerViewInputs get(String builderName, String userName) {
		String sql = "select t.* from bs_SellerViewInputs t where t.builderName=? and t.userName=? and t.sellerId=?";
		SellerViewInputs inputs = new DerbyDAO().nativeQueryFirstResult(sql, SellerViewInputs.class, builderName, userName);
		if (inputs!=null)
			inputs.getSnapShot();
		return inputs;
	}

	public SellerViewInputs get(String builderName) {
		String sql = "select t.* from bs_SellerViewInputs t where t.builderName=? and t.sellerId=?";
		SellerViewInputs inputs = new DerbyDAO().nativeQueryFirstResult(sql, SellerViewInputs.class, builderName);
		if (inputs != null)
			inputs.getSnapShot();
		return inputs;
	}
	
	public void saveOrUpdate(SellerViewInputs inputs) {
		SellerViewInputs source = inputs.getSnapShot();
		HashMap<String, String> sm=source.getInputs(), nm=inputs.getInputs();
		boolean change = true;
		if (true && inputs.getId()>0) {
			List<String> nl=new ArrayList<String>(nm.keySet()), sl=new ArrayList<String>(sm.keySet());
			nl.removeAll(sm.keySet());
			sl.removeAll(nm.keySet());
			if (nl.size()==0 && sl.size()==0) {
				for (Iterator<String> iter=nm.keySet().iterator(); iter.hasNext();) {
					String k = iter.next();
					if (StringUtils.equals(nm.get(k), sm.get(k))==false) {
						break;
					} else if (iter.hasNext()==false) {
						change = false;
					}
				}
			}
		}
		if (change) {
			DerbyDAO dao = new DerbyDAO();
			dao.saveOrUpdate(inputs);
			dao.getSession().flush();
		}
	}
}
