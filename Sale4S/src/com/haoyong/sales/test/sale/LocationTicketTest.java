package com.haoyong.sales.test.sale;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.Storehouse;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.common.domain.AbstractDomain;
import com.haoyong.sales.sale.domain.LocationT;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.StoreEnough;
import com.haoyong.sales.sale.form.LocationTicketForm;
import com.haoyong.sales.sale.form.SendTicketForm;
import com.haoyong.sales.sale.form.StoreTicketForm;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.OrderTypeLogic;
import com.haoyong.sales.test.base.AbstractTest;
import com.haoyong.sales.test.base.StorehouseTest;
import com.haoyong.sales.test.base.UserTest;

public class LocationTicketTest extends AbstractTest<LocationTicketForm> {

	public LocationTicketTest() {
		this.setForm(new LocationTicketForm());
	}
	
	public String check迁移开单__1全部迁_2部分迁2(char type, AbstractDomain out, AbstractDomain in, Object... filters0) {
		Object[] filters = this.genFiltersStart(filters0, "selectFormer4Purchase.selectedList");
		this.loadView("OutstoreList");
		this.onButton("迁出开单");
		this.onButton("生成单号");
		this.getForm().getDomain().getLocationTicket().setOut(out);
		this.getForm().getDomain().getLocationTicket().setIn(in);
		this.onButton("添加明细");
		this.setFilters(filters);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		this.onMenu("确定");
		if ("全部迁移过去".length()>0 && type=='1') {
			this.setEditAllSelect(detailCount);
			this.onMenu("全数量迁移");
			this.setEditAllSelect(detailCount);
			new StoreTicketTest().setQ清空();
			String timeDo = this.getTimeDo();
			this.onMenu("提交");
			if (StringUtils.equals(this.getForm().getUserName(), "管理员")) {
				this.loadView("InstoreList", this.genFiltersStart(filters0, "modifytime", timeDo));
				Assert.assertTrue("待迁入确认", this.getListViewValue().size()==detailCount);
			}
			if (true) {
				String outName = this.getForm().getDomain().getLocationTicket().getOut().getName()+"";
				String inName = this.getForm().getDomain().getLocationTicket().getIn().getName()+"";
				OrderDetail pur=this.getForm().getSelectEdit4Purchase().getLast(), spur=pur.getSnapShot();
				Assert.assertTrue("全数量分账迁移开单", pur.getStPurchase()==32 && pur.getVoparam(LocationT.class).getTicket().contains(outName)
						&& pur.getUlocation().contains(outName) && pur.getUlocation().contains(inName)
						&& pur.getLocationTicket().getLocAmount()==pur.getAmount()
						&& StringUtils.equals(pur.getLocationTicket().getOut().getName(), pur.getLocationTicket().getIn().getName())==false);
				this.loadView("OutstoreList", this.genFiltersStart(filters0, "modifytime", timeDo));
				Assert.assertTrue("迁出人看到未确认", this.getListViewValue().size()==detailCount);
				this.loadFormView(new StoreTicketForm(), "RestoreList", "selectFormer4Purchase.selectedList", "number", pur.getOrderTicket().getNumber());
				Assert.assertTrue("在迁移流程中不列入库存", this.getListViewValue().size()==0);
				OrderDetail ord=pur, sord=ord.getSnapShot();
				if (sord.getStOrder()>0) {
					StoreEnough enough = new StoreTicketTest().getEnoughs("instore", ""+inName, "commName", pur.getVoparam(CommodityT.class).getCommName()).get(0);
					if (new OrderTypeLogic().isBackType(ord.getOrderTicket().getOrderType()))
						Assert.assertTrue("备货订单在迁移流程中列入在途"+this.getListCountSum(), enough.getOnroadAmount()>0 && enough.getOrderAmount()==0);
					else
						Assert.assertTrue("客户订单在迁移流程中列入在途、订单数"+this.getListCountSum(), enough.getOnroadAmount()>0 && enough.getOrderAmount()>0);
				}
			}
		}
		if ("拆出部分数量2迁移过去".length()>0 && type=='2') {
			for (OrderDetail detail: this.getForm().getDetailList()) {
				detail.getLocationTicket().setLocAmount(2);
			}
			this.setEditAllSelect(detailCount);
			this.onMenu("部分迁移拆单");
			if (true) {
				OrderDetail pur=this.getForm().getSelectEdit4Purchase().getLast(), spur=pur.getSnapShot().getSnapShot();
				OrderDetail ord=pur, sord=ord.getSnapShot();
				Assert.assertTrue("拆分出的数量拿来准备迁出", pur.getAmount()==2 && new OrderTicketLogic().isSplitMonthnum(pur.getMonthnum(), spur.getMonthnum()));
				if (sord.getStOrder()>0)
					Assert.assertTrue("拆分出的数量拿来准备迁出", ord.getAmount()==2 && ord.getMonthnum().equals(pur.getMonthnum()));
				this.onButton("添加明细");
				this.setFilters(filters);
				Assert.assertTrue("有剩余数可迁出", this.getListViewValue().size()==detailCount*2);
			}
			this.setEditAllSelect(detailCount);
			this.onMenu("提交");
			if (true) {
				String outName = this.getForm().getDomain().getLocationTicket().getOut().getName();
				String inName = this.getForm().getDomain().getLocationTicket().getIn().getName();
				OrderDetail pur = this.getForm().getSelectEdit4Purchase().getLast();
				Assert.assertTrue("全数量分账迁移开单", pur.getStPurchase()==32 && pur.getVoparam(LocationT.class).getTicket().contains(outName+"")
						&& pur.getUlocation().contains(outName+"") && pur.getUlocation().contains(inName+"")
						&& pur.getLocationTicket().getLocAmount()==pur.getAmount());
				this.loadView("OutstoreList", filters0);
				Assert.assertTrue("迁出人看到未确认", this.getListViewValue().size()==detailCount);
				this.loadView("InstoreList", filters0);
				Assert.assertTrue("待迁入确认", this.getListViewValue().size()==detailCount);
			}
		}
		if (this.getForm().getSelectEdit4Purchase().getLast().getStOrder()>0)
			return this.getForm().getSelectEdit4Purchase().getLast().getOrderTicket().getNumber();
		return null;
	}
	
	public void check迁入确认__1签收_2拒收(char type, Object... filters) {
		LocationTicketForm form = this.getForm();
		this.loadView("InstoreList", filters);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		if ("货品签收".length()>0 && type=='1') {
			new StoreTicketTest().setQ清空();
			this.onMenu("确认迁入到货");
			OrderDetail pur=this.getForm().getSelectFormer4Purchase().getLast(), spur=pur.getSnapShot();
			if (true) {
				Assert.assertTrue("迁入仓保持", StringUtils.equals(pur.getLocationTicket().getIn().getName(), spur.getLocationTicket().getIn().getName()));
				this.loadView("InstoreList", filters);
				Assert.assertTrue("确认后不在待确认列表", this.getListViewValue().size()==0);
				this.loadSqlView(new StoreTicketForm(), "RestoreList", "实时采购在库明细", "number", pur.getOrderTicket().getNumber());
				Assert.assertTrue("迁入收货确认了库存增加", this.getListViewValue().size()>0);
				StoreEnough enough = new StoreTicketTest().getEnoughs("instore", ""+pur.getLocationTicket().getIn().getName()).get(0);
				Assert.assertTrue("迁入收货确认了库存增加", enough.getStoreAmount()>0);
			}
		}
		if ("拒收不要，返回".length()>0 && type=='2') {
			this.onMenu("已物流拒收");
			this.loadView("InstoreList", filters);
			Assert.assertTrue("拒收后还在，只是不列入提醒", this.getListViewValue().size()==detailCount);
			this.loadView("InstoreList", this.genFiltersStart(filters, "Remind", 1));
			Assert.assertTrue("不列入提醒", this.getListViewValue().size()==0);
		}
	}
	
	public void test分账迁移() {
if (1==1) {
		if ("迁移开单界面".length()>0) {
			this.setTestStart();
			this.loadView("OutstoreList");
			this.onButton("迁出开单");
			if ("选择仓库".length()>0) {
				this.onButton("选择迁出 ");
				Storehouse house = new StorehouseTest().get验货大仓();
				this.setFilters("selectFormer4Storehouse.selectedList", "name", house.getName());
				this.setSqlListSelect(1);
				this.onMenu("确定仓库");
				Assert.assertTrue("选择仓库迁出", StringUtils.equals(house.getName(), this.getForm().getDomain().getLocationTicket().getOut().getName()));
			}
			if ("选择人员".length()>0) {
				this.onButton("选择迁入 ");
				User user = new UserTest().getUser安装师傅02();
				this.setFilters("selectFormer4User.selectedList", "userName", user.getUserName());
				this.setSqlListSelect(1);
				this.onMenu("确定人员");
				Assert.assertTrue("选择仓库迁入", StringUtils.equals(user.getUserName(), this.getForm().getDomain().getLocationTicket().getIn().getName()));
			}
			if ("安装人员只能迁出有默认自己，不能更改".length()>0) {
				this.setTransUser(new UserTest().getUser安装师傅02());
				this.loadView("OutstoreList");
				this.onButton("迁出开单");
				Assert.assertTrue("安装人员迁出默认是自己", StringUtils.equals("安装师傅02", this.getForm().getDomain().getLocationTicket().getOut().getName()));
				try {
					this.onButton("选择迁出 ");
					Assert.fail("安装人员迁出不能更改");
				}catch(Exception e) {
				}
				try {
					this.onButton("迁出为总账");
					Assert.fail("安装人员迁出不能更改");
				}catch(Exception e) {
				}
			}
			if ("切换迁出分账，要重选择明细".length()>0) {
				this.setTestStart();
				this.get安装领班分账_备货(new UserTest().getUser安装师傅02(), 13,14);
				this.loadView("OutstoreList");
				this.onButton("迁出开单");
				if (true) {
					this.onButton("选择迁出 ");
					User user = new UserTest().getUser安装师傅02();
					this.setFilters("selectFormer4User.selectedList", "userName", user.getUserName());
					this.setSqlListSelect(1);
					this.onMenu("确定人员");
				}
				this.onMenu("添加明细");
				this.getSqlListView("selectFormer4Purchase.selectedList");
				this.setSqlAllSelect(2);
				this.onMenu("确定");
				OrderDetail pur = this.getForm().getSelectFormer4Purchase().getFirst();
				Assert.assertTrue("选择迁出安装师傅的2个库存", this.getForm().getDetailList().size()>0 && pur!=null && StringUtils.equals("安装师傅02", pur.getLocationTicket().getIn().getName()));
				if (true) {
					this.onButton("选择迁出 ");
					User user = new UserTest().getUser安装师傅01();
					this.setFilters("selectFormer4User.selectedList", "userName", user.getUserName());
					this.setSqlListSelect(1);
					this.onMenu("确定人员");
				}
				Assert.assertTrue("切换迁出分账，明细重置", this.getForm().getDetailList().size()==0);
			}
			if ("迁出分账、迁入分账名称不能相同".length()>0) {
				this.loadView("OutstoreList");
				this.onButton("迁出开单");
				if (true) {
					this.onButton("选择迁出 ");
					User user = new UserTest().getUser安装师傅02();
					this.setFilters("selectFormer4User.selectedList", "userName", user.getUserName());
					this.setSqlListSelect(1);
					this.onMenu("确定人员");
				}
				if (true) {
					this.onButton("选择迁入 ");
					User user = new UserTest().getUser安装师傅02();
					this.setFilters("selectFormer4User.selectedList", "userName", user.getUserName());
					this.setSqlListSelect(1);
					this.onMenu("确定人员");
					try {
						this.onMenu("添加明细");
						Assert.fail("迁出迁入分账不能相同");
					}catch(Exception e) {
					}
				}
			}
		}
}
		if ("师傅分账的货退回给总仓".length()>0) {
			this.setTestStart();
			String number = this.get安装领班分账_客户(new UserTest().getUser安装师傅01(), 10, 10);
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.check迁移开单__1全部迁_2部分迁2('1', new UserTest().getUser安装师傅01(), null, new Object[]{"number", number});
			this.setTransUser(new UserTest().getUser管理员());
			this.check迁入确认__1签收_2拒收('1', "number", number);
		}
		if ("订单请购到货可发货，迁移开单，确认".length()>0) {
			this.setTestStart();
			String number=this.getModeList().getSelfReceiptTest().get客户订单_普通(10, 10);
			this.check迁移开单__1全部迁_2部分迁2('1', null, new UserTest().getUser安装师傅01(), new Object[]{"number", number});
			User fromUser = this.setTransUser(new UserTest().getUser安装师傅01());
			this.check迁入确认__1签收_2拒收('1', "number", number);
			this.setTransUser(fromUser);
		}
		if ("订单请购到货可发货，部分迁移2开单，确认，剩余迁移8开单，确认".length()>0) {
			this.setTestStart();
			String number=this.getModeList().getSelfReceiptTest().get客户订单_普通(10, 10);
			this.check迁移开单__1全部迁_2部分迁2('2', null, new UserTest().getUser安装师傅01(), new Object[]{"number", number});
			User fromUser = this.setTransUser(new UserTest().getUser安装师傅01());
			this.check迁入确认__1签收_2拒收('1', "number", number);
			this.setTransUser(fromUser);
			this.check迁移开单__1全部迁_2部分迁2('1', null, new UserTest().getUser安装师傅01(), new Object[]{"number", number});
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.check迁入确认__1签收_2拒收('1', "number", number);
			this.setTransUser(fromUser);
		}
		if ("订单请购到货可发货，迁移开单，不可发货".length()>0) {
			this.setTestStart();
			String number=this.getModeList().getSelfReceiptTest().get客户订单_普通(10, 10);
			this.check迁移开单__1全部迁_2部分迁2('1', null, new UserTest().getUser安装师傅01(), new Object[]{"number", number});
			this.loadFormView(new SendTicketForm(), "SendList", "number", number, "uneditable", "isnull");
			Assert.assertTrue("迁移分账中不可发货", this.getListViewValue().size()==0);
		}
		if ("订单请购到货可发货10，部分迁移2开单师傅确认有库存2，迁移开单，师傅只看到自己的库存2，看不到剩余库存8".length()>0) {
			this.setTestStart();
			String number=this.getModeList().getSelfReceiptTest().get客户订单_普通(10, 10);
			this.check迁移开单__1全部迁_2部分迁2('2', null, new UserTest().getUser安装师傅01(), new Object[]{"number", number});
			User fromUser = this.setTransUser(new UserTest().getUser安装师傅01());
			this.check迁入确认__1签收_2拒收('1', "number", number);
			this.loadView("OutstoreList");
			this.onButton("迁出开单");
			this.onButton("生成单号");
			Assert.assertTrue("默认出库为师傅", this.getForm().getDomain().getLocationTicket().getOut().getName().equals(new UserTest().getUser安装师傅01().getUserName()));
			this.getForm().getDomain().getLocationTicket().getIn().setName("toName");
			this.onButton("添加明细");
			this.setFilters("selectFormer4Purchase.selectedList", "number", number);
			Assert.assertTrue("只看到师傅的库存", this.getListViewValue().size()==2);
			try {
				this.onButton("迁出为总账");
				Assert.fail("师傅不能迁出总账库存");
			}catch(Exception e) {
				this.getForm().getDomain().getLocationTicket().getOut().setName("fromName");
			}
			this.onButton("添加明细");
			this.setFilters("selectFormer4Purchase.selectedList", "number", number, "amount", 8);
			Assert.assertTrue("师傅不能迁出总账库存", this.getListViewValue().size()==0);
			this.setTransUser(fromUser);
		}
		if ("师傅只能迁入收货确认给自己的".length()>0) {
			this.setTestStart();
			String timeDo = this.getTimeDo();
			String number1 = this.getModeList().getSelfReceiptTest().get客户订单_普通(10,10);
			String number2 = this.getModeList().getSelfReceiptTest().get客户订单_普通(10,10);
			this.check迁移开单__1全部迁_2部分迁2('1', null, new UserTest().getUser临时("to1"), new Object[]{"number", number1});
			this.check迁移开单__1全部迁_2部分迁2('1', null, new UserTest().getUser安装师傅01(), new Object[]{"number", number2});
			User fromUser = this.setTransUser(new UserTest().getUser安装师傅01());
			this.loadView("InstoreList", "amount", 10, "modifytime", timeDo);
			Assert.assertTrue("只能看到迁入给自己的", this.getListViewValue().size()==2);
			this.setTransUser(fromUser);
		}
	}
	
	public String get安装领班分账_客户(User user, int... amountList) {
		String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(amountList);
		this.check迁移开单__1全部迁_2部分迁2('1', null, user, new Object[]{"number", number});
		User fromUser = this.setTransUser(user);
		this.check迁入确认__1签收_2拒收('1', "number", number);
		this.setTransUser(fromUser);
		return number;
	}

	public String get安装领班分账_备货(User user, int... amountList) {
		String purName = this.getModeList().getSelfReceiptTest().getPur备货订单_普通(amountList);
		this.check迁移开单__1全部迁_2部分迁2('1', null, user, new Object[]{"purName", purName});
		this.check迁入确认__1签收_2拒收('1', "purName", purName);
		return purName;
	}
	
	protected void setQ清空() {
	}
}
