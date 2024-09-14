package com.haoyong.salel.common.form;

import net.sf.mily.bus.service.Action;

import com.haoyong.salel.base.domain.City;
import com.haoyong.salel.base.domain.Province;
import com.haoyong.salel.base.domain.Seller;
import com.haoyong.salel.base.domain.User;


/**
 * <pre>
 * Title: Action的枚举
 * Description:	每个枚举值需要指定 Ticket的类、中文名称、优先级权重（越小越优先）。

 * </pre>
 */
public enum ActionEnum implements Action {

	Province_Effect(Province.class,"省份生效"),
	Province_Delete(Province.class,"省份删除"),
	
	City_Effect(City.class,"城市生效"),
	City_Delete(City.class,"城市删除"),
	
	Seller_Create(Seller.class,"商家新增"),
	Seller_Edit(Seller.class,"商家编辑"),
	Seller_Delete(Seller.class,"商家删除"),
	
	User_Save(User.class,"用户保存"),
	User_Delete(User.class,"用户删除"),
	
	User_RolePrivilege(User.class, "保存角色授权"),
	User_RoleActor(User.class, "保存角色共享"),
	;
	
	/**
	 * Ticket的类型
	 */
	private Class<?> ticketClass;

	/**
	 * 业务动作名称
	 */
	private String name;
	
	/**
	 * 是否删除单据的Action
	 */
	private boolean deleted = false;
	private static boolean DeletedAction = true;

	@SuppressWarnings("unchecked")
	ActionEnum(Class<?> ticketClass, String name) {
		this(ticketClass, name, false);
	}
	ActionEnum(Class<?> ticketClass, String name, boolean deleted) {
		this.ticketClass = ticketClass;
		this.name = name;
		this.deleted = deleted;
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
	
	public boolean isDeleted() {
		return deleted;
	}
	
	public String toString() {
		return this.name();
	}
}
