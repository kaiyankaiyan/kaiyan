package com.haoyong.sales.base.logic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import net.sf.mily.types.DoubleType;
import net.sf.mily.ui.BlockGrid;
import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.enumeration.BlockGridMode;
import net.sf.mily.util.LogUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.domain.BomT;
import com.haoyong.sales.sale.domain.BomTicket;
import com.haoyong.sales.sale.form.PPurchaseTicketForm;

public class BomTicketLogic {
	
	public PropertyChoosableLogic.TicketDetail<PPurchaseTicketForm, BomTicket, BomT> getTicketChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<PPurchaseTicketForm, BomTicket, BomT>(new PPurchaseTicketForm(), new BomTicket(), new BomT());
	}
	
	public List<BomDetail> getDetailInIDs(String inIDs) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from sa_BomDetail t where t.id in (").append(inIDs).append(") and t.sellerId=?");
		BaseDAO dao = new BaseDAO();
		List<BomDetail> list = dao.nativeQuery(sql.toString(), BomDetail.class);
		return list;
	}

	/**
	 * BOM文本查看
	 */
	public void setBDetailsLink(Hyperlink link, List<BomDetail> dlist) {
		BlockGrid grid = new BlockGrid().createGrid(1, BlockGridMode.NotOccupySizable);
		DoubleType type = new DoubleType();
		for (BomDetail d: dlist) {
			StringBuffer sb = new StringBuffer();
			for (int i=d.getLevel(); i-->1; sb.append("\t\t"));
			sb.append(d.getVoparam(CommodityT.class).getCommName()).append("\t");
			sb.append(type.format(d.getBomTicket().getAunit()));
			grid.append(new Hyperlink(null, sb.toString()));
		}
		link.add(grid);
	}
	
	public BomDetail getParent(List<BomDetail> sourceList, BomDetail cur) {
		for (int i=sourceList.indexOf(cur), prtLevel=cur.getLevel()-1; i-->0;) {
			BomDetail d = sourceList.get(i);
			if (d.getLevel()==prtLevel)
				return d;
		}
		return null;
	}
	
	/**
	 * 取全部一级物料
	 */
	public List<BomDetail> getChildrenRoot(List<BomDetail> sourceList) {
		List<BomDetail> list = new ArrayList<BomDetail>();
		for (BomDetail d: sourceList) {
			if (d.getLevel()==1)
				list.add(d);
		}
		return list;
	}
	
	public BomDetail getFollowBrother(List<BomDetail> sourceList, BomDetail cur) {
		List<BomDetail> childs = this.getChildrenFold(sourceList, cur);
		BomDetail last = childs.size()>0? childs.get(childs.size()-1): cur;
		int idx = sourceList.indexOf(last)+1;
		if (idx < sourceList.size())
			return sourceList.get(idx);
		return null;
	}

	public List<BomDetail> getChildrenFold(List<BomDetail> sourceList, BomDetail cur) {
		List<BomDetail> list = new ArrayList<BomDetail>();
		for (int ni=sourceList.indexOf(cur)+1, nsize=sourceList.size(); ni<nsize; ni++) {
			BomDetail next = sourceList.get(ni);
			if (next.getLevel() > cur.getLevel())
				list.add(next);
			else
				break;
		}
		return list;
	}

	public List<BomDetail> getChildrenBrother(List<BomDetail> sourceList, BomDetail cur) {
		List<BomDetail> list = new ArrayList<BomDetail>();
		for (int ni=sourceList.indexOf(cur)+1, nsize=sourceList.size(); ni<nsize; ni++) {
			BomDetail next = sourceList.get(ni);
			if (next.getLevel() == cur.getLevel()+1)
				list.add(next);
			else if (next.getLevel() <= cur.getLevel())
				break;
		}
		return list;
	}
	
	public int getLevalMax(List<BomDetail> sourceList) {
		int mx = 0;
		for (BomDetail d: sourceList) {
			if (d.getLevel() > mx)
				mx = d.getLevel();
		}
		return mx;
	}
	
	public String getToJson(List<BomDetail> bomMaterials) {
		if (bomMaterials==null || bomMaterials.isEmpty())
			return null;
		List<BomDetailS> dList = new ArrayList<BomDetailS>();
		for (BomDetail detail: bomMaterials) {
			BomDetailS d = BomDetailS.getBomDetail(detail);
			dList.add(d);
		}
		Type gtype = new TypeToken<ArrayList<BomDetailS>>(){}.getType();
		String json =  new Gson().toJson(dList, gtype);
		return json;
	}

	public List<BomDetail> getToMaterials(String json) {
		if (json == null) {
			return new ArrayList<BomDetail>(0);
		}
		Gson gson = new Gson();
		Type gtype = new TypeToken<ArrayList<BomDetailS>>(){}.getType();
		List<BomDetailS> bomDetails = gson.fromJson(json, gtype);
		List<BomDetail> bomMaterials = new ArrayList<BomDetail>();
		for (BomDetailS d: bomDetails) {
			bomMaterials.add(d.getMaterialDetail());
		}
		return bomMaterials;
	}
	
	private static class BomDetailS {
		
		private CommodityT tcomm;
		private double aunit;
		private String arrange;
		private int level;
		
		public static BomDetailS getBomDetail(BomDetail detail) {
			BomDetailS d = new BomDetailS();
			d.tcomm = new CommodityLogic().getPropertyChoosableLogic().toTrunk(detail.getCommodity());
			d.aunit = detail.getBomTicket().getAunit();
			d.arrange = detail.getArrange();
			d.level = detail.getLevel();
			return d;
		}
		
		public BomDetail getMaterialDetail() {
			BomDetail detail = new BomDetail();
			if (this.tcomm == null)
				this.tcomm = new CommodityT();
			try {
				new CommodityLogic().getPropertyChoosableLogic().fromTrunk(detail.getCommodity(), this.tcomm);
			} catch (Exception e) {
				throw LogUtil.getRuntimeException(e);
			}
			detail.setVoparam(this.tcomm);
			detail.getBomTicket().setAunit(this.aunit);
			detail.setArrange(this.arrange);
			detail.setLevel(this.level);
			return detail;
		}
	}
}
