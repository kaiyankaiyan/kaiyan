package com.haoyong.sales.base.logic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.mappings.EntityClass;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.ViewBuilder;

import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.CommCol;
import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.base.form.CommodityForm;

/**
 * 商品库存统计列标题Logic，从商品Trunk中选择
 */
public class CommColLogic {
	
	public List<FieldBuilder> getRowColBuilders() {
		CommodityLogic logic = new CommodityLogic();
		List<FieldBuilder> sourceList = this.getCommodityTrunkBuilder().getFieldBuilders();
		SellerViewSetting viewSetting = logic.getViewSetting();
		List<FieldBuilder> chooseList=viewSetting.getChooseList(sourceList);
		List<FieldBuilder> trunkList=viewSetting.getTrunkList(chooseList);
		return trunkList;
	}
	
	public List<FieldBuilder> getRowBuilders() {
		CommodityLogic logic = new CommodityLogic();
		SellerViewSetting viewSetting = logic.getViewSetting();
		List<FieldBuilder> trunkList=viewSetting.getTrunkList(this.getCommodityTrunkBuilder().getFieldBuilders());
		List<CommCol> colList = this.getColList();
		for (Iterator<FieldBuilder> iter=trunkList.iterator(); iter.hasNext();) {
			FieldBuilder builder = iter.next();
			for (CommCol col: colList) {
				if (col.getName().equals(builder.getName())) {
					iter.remove();
					break;
				}
			}
		}
		return trunkList;
	}
	
	public List<FieldBuilder> getColBuilders() {
		List<FieldBuilder> list = new ArrayList<FieldBuilder>();
		List<FieldBuilder> sourceList = this.getCommodityTrunkBuilder().getFieldBuilders();
		for (CommCol col: getColList()) {
			for (FieldBuilder f: sourceList) {
				if (col.getName().equals(f.getName())) {
					list.add(f);
				}
			}
		}
		return list;
	}
	
	public List<CommCol> getColList() {
		return getDomain().getInfoList();
	}
	
	public TypeInfos getDomain() {
		TypeInfos infos = new TypeInfosLogic().getInfos(CommCol.class);
		Type gtype = new TypeToken<ArrayList<CommCol>>(){}.getType();
		infos.setType(gtype);
		return infos;
	}

	public List<CommCol> getTypeList() {
		TypeInfos infos = getDomain();
		List<CommCol> list = infos.getInfoList();
		return list;
	}
	
	private ViewBuilder getCommodityTrunkBuilder() {
		CommodityLogic logic = new CommodityLogic();
		ViewBuilder vbuilder = EntityClass.loadViewBuilder(CommodityForm.class, "Commodity");
		logic.getPropertyChoosableLogic().trunkViewBuilder(vbuilder);
		vbuilder = (ViewBuilder)vbuilder.getFieldBuilder("domain");
		return vbuilder;
	}
}
