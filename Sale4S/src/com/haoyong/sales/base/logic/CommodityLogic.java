package com.haoyong.sales.base.logic;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.form.BOMForm;
import com.haoyong.sales.base.form.CommodityForm;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;

/**
 * 查询支持类——商品
 */
public class CommodityLogic {
	
	public PropertyChoosableLogic.Choose12<CommodityForm, Commodity, CommodityT> getPropertyChoosableLogic() {
		return new PropertyChoosableLogic.Choose12<CommodityForm, Commodity, CommodityT>(new CommodityForm(), new Commodity(), new CommodityT());
	}
	
	public PropertyChoosableLogic.Choose12<BOMForm, Commodity, CommodityT> getMaterialChoosableLogic() {
		return new PropertyChoosableLogic.Choose12<BOMForm, Commodity, CommodityT>(new BOMForm(), new Commodity(), new CommodityT());
	}
	
	public void fromTrunk(Commodity to, Commodity from) {
		getPropertyChoosableLogic().fromTrunk(to, from);
	}
	
	public SellerViewSetting getViewSetting() {
		return getPropertyChoosableLogic().getChooseSetting( this.getPropertyChoosableLogic().getChooseBuilder() );
	}
	
	public SellerViewSetting getViewSetting_Material() {
		return getMaterialChoosableLogic().getChooseSetting( this.getMaterialChoosableLogic().getChooseBuilder() );
	}
	
	public Commodity getCommodityByNumber(String commNumber) {
		return new BaseDAO().nativeQuerySingleResult("select t.* from bs_commodity t where t.commNumber=? and t.sellerId=?", Commodity.class, commNumber);
	}
}
