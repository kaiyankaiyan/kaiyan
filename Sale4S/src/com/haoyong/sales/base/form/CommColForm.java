package com.haoyong.sales.base.form;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.webObject.query.SqlColumnSetting;

import com.haoyong.sales.base.domain.CommCol;
import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.base.logic.CommColLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.Infomation;
import com.haoyong.sales.common.form.ViewData;

/**
 * 商品库存统计列标题，从商品Trunk中选择
 */
public class CommColForm extends AbstractForm<CommCol> {
	
	public void asCol() {
		this.getRowList().removeAll(this.getSelectedList());
		this.getColList().addAll(this.getSelectedList());
	}
	
	public void asRow() {
		this.getColList().removeAll(this.getSelectedList());
		this.getRowList().addAll(this.getSelectedList());
	}
	
	public void validateSetting() {
		StringBuffer sb = new StringBuffer();
		if (this.getRowList().isEmpty()) {
			sb.append("商品数量统计行标题不能为空，");
		}
		if (sb.length()>0) {
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
		}
	}
	
	public void infoSetting() {
		throw new Infomation("保存成功！");
	}
	
	public List<CommCol> getColList() {
		String k = "ColList";
		List<CommCol> colList = this.getAttr(k);
		if (colList==null) {
			TypeInfos domain = new CommColLogic().getDomain();
			colList = new ArrayList(domain.getInfoList());
			for (Iterator<CommCol> iter=colList.iterator(); iter.hasNext();) {
				CommCol col = iter.next();
				if (getTrunkMap().keySet().contains(col.getName())==false) {
					iter.remove();
				} else {
					col.setLabel(getTrunkMap().get(col.getName()));
				}
			}
			this.setAttr(k, colList);
		} else {
			Map<String, Object> map = new LinkedHashMap<String, Object>(this.getTrunkMap());
			for (CommCol col: colList) {
				map.put(col.getName(), col);
			}
			List<CommCol> list = new ArrayList<CommCol>();
			for (Object item: map.values()) {
				if (item instanceof CommCol) {
					list.add((CommCol) item);
				}
			}
			colList.clear();
			colList.addAll(list);
		}
		return colList;
	}
	
	public List<CommCol> getRowList() {
		String k = "RowList";
		List<CommCol> rowList = this.getAttr(k);
		if (rowList==null) {
			List<String> colList = new ArrayList<String>();
			for (CommCol item: this.getColList()) {
				colList.add(item.getName());
			}
			rowList = new ArrayList<CommCol>();
			Map<String, String> trunkMap = new LinkedHashMap<String, String>(getTrunkMap());
			for (Iterator<Map.Entry<String, String>> iter=trunkMap.entrySet().iterator(); iter.hasNext();) {
				Map.Entry<String, String> entry = iter.next();
				if (colList.contains(entry.getKey())==false) {
					CommCol row = new CommCol();
					row.setName(entry.getKey());
					row.setLabel(entry.getValue());
					rowList.add(row);
				}
			}
			this.setAttr(k, rowList);
		} else {
			Map<String, Object> map = new LinkedHashMap<String, Object>(this.getTrunkMap());
			for (CommCol row: rowList) {
				map.put(row.getName(), row);
			}
			List<CommCol> list = new ArrayList<CommCol>();
			for (Object item: map.values()) {
				if (item instanceof CommCol) {
					list.add((CommCol) item);
				}
			}
			rowList.clear();
			rowList.addAll(list);
		}
		return rowList;
	}
	
	private Map<String, String> getTrunkMap() {
		Map<String, String> trunkMap = this.getAttr(LinkedHashMap.class);
		if (trunkMap==null) {
			ChooseFormer former = new ChooseFormer();
			CommodityLogic logic = new CommodityLogic();
			former.setViewBuilder(logic.getPropertyChoosableLogic().getChooseBuilder());
			former.setSellerViewSetting(logic.getViewSetting());
			trunkMap = new LinkedHashMap<String, String>();
			for (SqlColumnSetting item: former.getTrunkListFromChoose()) {
				if ("true".equals( String.valueOf(item.getVoParamMap().get("choose"))) ) {
					trunkMap.put(item.getFieldId(), item.getFieldLabel());
				}
			}
			this.setAttr(trunkMap);
		}
		return trunkMap;
	}
	
	private List getSelectedList() {
		String k = "SelectedList";
		List list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList();
			this.setAttr(k, list);
		}
		return list;
	}

	@Override
	public void setSelectedList(List<CommCol> selected) {
		this.getSelectedList().clear();
		this.getSelectedList().addAll(selected);
	}
	
	public void setSetting4Service(ViewData<TypeInfos> viewData) {
		TypeInfos domain = new CommColLogic().getDomain();
		domain.setInfoList(this.getColList());
		viewData.setTicketDetails(domain);
	}
}
