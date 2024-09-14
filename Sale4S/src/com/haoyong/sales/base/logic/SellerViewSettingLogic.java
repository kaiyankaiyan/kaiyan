package com.haoyong.sales.base.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.mily.common.JdbcQueryExecutor;
import net.sf.mily.common.SessionProvider;
import net.sf.mily.types.DateTimeType;
import net.sf.mily.types.StringType;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.dao.DomainChangeTracing;
import com.haoyong.sales.common.dao.SerialNumberFactory;

public class SellerViewSettingLogic {
	
	private static ConcurrentLinkedQueue<SellerViewSetting> sellerSettings = new ConcurrentLinkedQueue<SellerViewSetting>();
	
	public SellerViewSetting getViewSetting(ViewBuilder viewBuilder, List<String> trunkDefault) {
		String builderName = viewBuilder.getFullViewName();
		if (builderName.endsWith("ArrangeTicket.Ticket"))
			"".toCharArray();
		long sellerId = SellerLogic.getSellerId();
		for (SellerViewSetting st: new ArrayList<SellerViewSetting>(sellerSettings)) {
			if (StringUtils.equals(builderName, st.getBuilderName()) && st.getSellerId()==sellerId)
				return st;
		}
		for (boolean once=true; once; once=false) {
			SellerViewSetting d = null;
			JdbcQueryExecutor query = new JdbcQueryExecutor();
			try {
				query.setConnection(SessionProvider.getSession2().connection());
				String sql = new BaseDAO().getParametedSql("select t.* from bs_SellerViewSetting t where t.builderName=? and t.sellerId=?", builderName);
				List<SellerViewSetting> dList = query.queryEntity(sql, SellerViewSetting.class);
				if (dList.size()>0)
					d = dList.get(0);
			} catch (Exception e) {
				throw LogUtil.getRuntimeException(e);
			} finally {
				query.close();
			}
			if ("Load".length()>0 && d!=null) {
				sellerSettings.add(d);
				return d;
			}
			if ("Create".length()>0) {
				SellerViewSetting d1 = new SellerViewSetting();
				d1.setBuilderName(builderName);
				d1.setSellerId(sellerId);
				sellerSettings.add(d1);
				if ("Default Fields".length()>0) {
					d1.setTrunkList(trunkDefault);
					if (d1.getRequireList().isEmpty())
						d1.setCfgRequire(viewBuilder);
					if (d1.getTitleList().isEmpty())
						d1.getTitleList().addAll(d1.getRequireList());
				}
				return d1;
			}
		}
		return null;
	}
	
	public void saveViewSetting(SellerViewSetting vs) {
		vs.setModifytime(new Date());
		if ("保存".length()>0) {
			StringBuffer sb = new StringBuffer("\nDirectSaveToDB").append(vs.getId()==0? "新增，": "编辑，");
			JdbcQueryExecutor query = new JdbcQueryExecutor();
			try {
				query.setConnection(SessionProvider.getSession2().connection());
				StringBuffer sql = null;
				if (vs.getId()==0) {
					if ("set GlobalID".length()>0) {
						SerialNumberFactory serialf = new SerialNumberFactory();
						ReflectHelper.setPropertyValue(serialf, "sellerId", 11);
						vs.setId(serialf.serialLen2("SViewSetting", null, 3).getNextIndex10());
					}
					sql = new StringBuffer("insert into bs_SellerViewSetting(id,builderName,modifytime,sellerId,MInput,MRename,MSelect,choose,rquire,title,trunk,version) values(")
							.append(vs.getId()).append(",")
							.append(new StringType().sqlFormat(vs.getBuilderName())).append(",").append(new DateTimeType().sqlFormat(vs.getModifytime()))
							.append(",").append(vs.getSellerId()).append(",").append(new StringType().sqlFormat(vs.getMInput()))
							.append(",").append(new StringType().sqlFormat(vs.getMRename())).append(",").append(new StringType().sqlFormat(vs.getMSelect()))
							.append(",").append(new StringType().sqlFormat(vs.getChoose())).append(",").append(new StringType().sqlFormat(vs.getRquire()))
							.append(",").append(new StringType().sqlFormat(vs.getTitle())).append(",").append(new StringType().sqlFormat(vs.getTrunk()))
							.append(",").append(vs.getVersion()).append(");commit;");
					query.executeUpdate(sql.toString());
				} else {
					sql = new StringBuffer("update bs_SellerViewSetting set builderName=").append(new StringType().sqlFormat(vs.getBuilderName()))
							.append(",modifytime=").append(new DateTimeType().sqlFormat(vs.getModifytime()))
							.append(",sellerId=").append(vs.getSellerId()).append(",MInput=").append(new StringType().sqlFormat(vs.getMInput()))
							.append(",MRename=").append(new StringType().sqlFormat(vs.getMRename())).append(",MSelect=").append(new StringType().sqlFormat(vs.getMSelect()))
							.append(",choose=").append(new StringType().sqlFormat(vs.getChoose())).append(",rquire=").append(new StringType().sqlFormat(vs.getRquire()))
							.append(",title=").append(new StringType().sqlFormat(vs.getTitle())).append(",trunk=").append(new StringType().sqlFormat(vs.getTrunk()))
							.append(",version=").append(vs.getVersion()).append(" where id=").append(vs.getId()).append(";commit;");
					query.executeUpdate(sql.toString());
				}
				DomainChangeTracing.getLog().info(sb.append(sql).toString());
			} finally {
				query.close();
			}
		}
	}
}
