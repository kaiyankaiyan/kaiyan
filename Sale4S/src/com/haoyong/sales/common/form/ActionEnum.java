package com.haoyong.sales.common.form;

import net.sf.mily.bus.service.Action;

import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.Storehouse;
import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.OrderTicket;
import com.haoyong.sales.sale.domain.PrintModel;
import com.haoyong.sales.sale.domain.Question;
import com.haoyong.sales.sale.domain.StoreEnough;
import com.haoyong.sales.sale.domain.StoreMonth;


/**
 * <pre>
 * Title: Action的枚举
 * Description:	每个枚举值需要指定 Ticket的类、中文名称、优先级权重（越小越优先）。

 * </pre>
 */
public enum ActionEnum implements Action {
	
	Question_Save(Question.class, "提问保存"),
	
// 基础模块------------------------------------------------------------------------
	Seller_Create(Seller.class, "商家新增"),
	Seller_Delete(Seller.class, "商家删除"),
	Seller_DeleteTickets(Seller.class, "商家单据删除"),

	User_Admin(User.class,"管理者"),
	User_Save(User.class,"用户保存"),
	User_Delete(User.class,"用户删除"),
	
	User_RolePrivilege(User.class, "保存角色授权"),
	User_RoleActor(User.class, "保存角色共享"),
	
	Client_Save(Client.class,"保存客户"),
	Client_Delete(Client.class,"删除客户"),
	
	Storehouse_Save(Storehouse.class,"保存仓库"),
	Storehouse_Delete(Storehouse.class,"删除仓库"),
	
	SubCompany_Save(SubCompany.class,"保存分公司"),
	SubCompany_Delete(SubCompany.class,"删除分公司"),
	
	Supplier_Save(Supplier.class,"供应商保存"),
	Supplier_Delete(Supplier.class,"供应商删除"),
	
	TypeInfos_ItemSave(TypeInfos.class, "分类项保存"),
	TypeInfos_ItemDelete(TypeInfos.class, "分类项删除"),
	TypeInfos_Save(TypeInfos.class, "分类集合保存"),
	
	Commodity_Save(Commodity.class,"商品新增"),
	Commodity_Delete(Commodity.class,"商品删除"),
	
// 业务模块------------------------------------------------------------------------
	OrderTicket_T(OrderTicket.class,"订单单头"),
	OrderTicket_Save(OrderDetail.class,"订单保存"),
	OrderTicket_Effect(OrderDetail.class,"订单生效"),
	OrderTicket_Delete(OrderDetail.class,"订单删除"),
	OrderTicket_Count(OrderDetail.class,"订单统计"),
	
	OrderTicket_ChangeEffect(OrderDetail.class,"改单申请生效"),
	OrderTicket_ChangeClear(OrderDetail.class,"改单申请删除"),
	
	OrderTicket_AdjustEffect(OrderDetail.class,"订单红冲生效"),
	OrderTicket_AdjustDelete(OrderDetail.class,"订单红冲删除"),
	
	BomTicket_Save(BomDetail.class, "订单Bom物料保存"),
	BomTicket_Delete(BomDetail.class, "订单Bom物料删除"),
	
	PurchaseTicket_Save(OrderDetail.class,"保存采购单"),
	PurchaseTicket_Effect(OrderDetail.class,"采购单生效"),
	PurchaseTicket_Delete(OrderDetail.class,"删除采购单"),

	PurchaseTicket_ChangeEffect(OrderDetail.class,"改单申请生效"),
	PurchaseTicket_ChangeClear(OrderDetail.class,"改单申请删除"),

	PurchaseTicket_AdjustEffect(OrderDetail.class,"采购红冲生效"),
	PurchaseTicket_AdjustDelete(OrderDetail.class,"采购红冲删除"),

	ReceiptTicket_Save(OrderDetail.class,"收货入库"),
	ReceiptTicket_Effect(OrderDetail.class,"收货完成"),
	
	StoreTicket_Restore(OrderDetail.class,"重新计算库存"),
	StoreTicket_Extra(OrderDetail.class,"添加|减少入库重新计算额外库存"),
	StoreTicket_Month(StoreMonth.class,"月尾出入库统计"),
	StoreTicket_Enough(StoreEnough.class,"够用库存统计"),
	
	SaleTicket_Effect(OrderDetail.class,"销售完成"),
	
	BillTicket_Save(BillDetail.class, "应收单|收款单生效"),
	BillTicket_Delete(BillDetail.class, "应收单|收款单删除"),
	
	PrintModel_Save(PrintModel.class, "打印模板保存"),
	;
	
	/**
	 * Ticket的类型
	 */
	private Class<?> ticketClass;

	/**
	 * 业务动作名称
	 */
	private String name;

	ActionEnum(Class<?> ticketClass, String name) {
		this.ticketClass = ticketClass;
		this.name = name;
	}

	/**
	 * @see cr.esm.view.service.Action#getName()
	 */
	@Override
	public String getName() {
		return this.name() + name;
	}

	/**
	 * @see cr.esm.view.service.Action#getTicketClass()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class getTicketClass() {
		return ticketClass;
	}

	/**
	 * @see cr.esm.view.service.Action#getId()
	 */
	@Override
	public String getId() {
		return name();
	}
	
	public String toString() {
		return this.name();
	}
}
