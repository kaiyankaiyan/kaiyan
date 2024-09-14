package com.haoyong.sales.base.form;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.mily.support.form.SelectTicketFormer4Edit;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.IntegerType;
import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.enumeration.EventListenerType;
import net.sf.mily.ui.event.ActionListener;
import net.sf.mily.ui.event.EventObject;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.Field;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.TextBuilder;
import net.sf.mily.webObject.TextFieldBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.hibernate.util.JoinedIterator;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.SupplyType;
import com.haoyong.sales.base.logic.BomTicketLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.logic.ArrangeTypeLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;

public class BOMForm extends AbstractForm<BomDetail> implements FViewInitable {
	
	public static BOMForm getForm4List(List<BomDetail> detailList, AbstractForm firerForm) {
		BOMForm form = new BOMForm();
		form.setAttr("firerForm", firerForm);
		form.setDetailList(detailList);
		form.getDomain().getBomTicket().setAunit(1);
		form.getDomain().setLevel(3);
		form.setShowBomDetails(new BomTicketLogic().getLevalMax(detailList));
		return form;
	}
	
	public void prepareBomImport() {
		TicketPropertyUtil.copyFieldsSkip(new BomDetail(), this.getDomain());
		this.getDomain().getCommodity().getVoParamMap().put("note", "对应列序号");
		this.getBomImports().clear();
	}
	
	public void validateBom() throws Exception {
		StringBuffer sb=new StringBuffer(), sitem=null;
		BomDetail d=null;
		for (int bi=0, bsize=getDetailList().size(), plevel=0; bi<bsize; bi++, plevel=d.getLevel()) {
			d = getDetailList().get(bi);
			sitem = new StringBuffer();
			if (bi==0 && d.getLevel()!=1)
				sitem.append("物料层级必须是一级，");
			if (d.getLevel()==0)
				sitem.append("物料层级不能是0级，");
			if (plevel>0 && (d.getLevel()-plevel)>1)
				sitem.append("物料跟第").append(bi).append("物料层数相差超过一级，");
			if (new CommodityLogic().getMaterialChoosableLogic().isValid(d.getCommodity(), sitem)==false)
				sitem.append("请补充物料信息，");
			if (d.getBomTicket().getAunit()==0)
				sitem.append("请填写物料数量，");
			if (new SupplyTypeLogic().isPurchaseType(d.getCommodity().getSupplyType()) && new BomTicketLogic().getChildrenBrother(getDetailList(), d).size()>0)
				sitem.append("采购物料不能有子物料，");
			if (sitem.length() > 0)
				sb.append("第").append(bi+1).append("行").append(sitem).append("\t");
		}
		List<BomDetail> prtList = new ArrayList<BomDetail>();
		for (int ri=0,rsize=this.getDetailList().size(); ri<rsize; ri++) {
			BomDetail ditem=this.getDetailList().get(ri), dpre=ri==0? null: this.getDetailList().get(ri-1);
			if (dpre == null) {
			} else if (dpre.getLevel() < ditem.getLevel()) {
				prtList.add(dpre);
			} else if (dpre.getLevel() > ditem.getLevel()) {
				prtList.remove(prtList.size()-1);
			}
			sitem = new StringBuffer();
			String sarrange = ditem.getArrange();
			String sarrPrt = null;
			for (int pi=prtList.size(); pi-->0 && sarrPrt==null;) {
				BomDetail prt = prtList.get(pi);
				String parrange = prt.getArrange();
				if ("去排单".equals(parrange) || new ArrangeTypeLogic().getNormal().equals(parrange))
					sarrPrt = parrange;
				else if (parrange!=null)
					break;
			}
			if (sarrPrt==null && sarrange==null) {
				sitem.append("请选择物料供给方式，");
			} else if ("去排单".equals(sarrPrt) || new ArrangeTypeLogic().getNormal().equals(sarrPrt)) {
				if (sarrange==null)
					sitem.append(sarrPrt).append("的子物料未选择供给方式，");
			} else if ("去请购".equals(sarrPrt) && sarrange==null) {
				sitem.append(sarrPrt).append("的子物料要选择供给方式，");
			} else if (sarrPrt!=null && sarrange==null) {
				sitem.append(sarrPrt).append("的子物料要选择供给方式，");
			}
			if (sitem.length()>0)
				sb.append("第").append(ri+1).append("行").append(sitem).append("\t");
		}
		if (sb.length() > 0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void validateToImport() throws Exception {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isEmpty(getDomain().getCommodity().getRemark()))
			sb.append("请粘贴入单元格内容，");
		if (true) {
			IntegerType type = new IntegerType();
			Set<Integer> colList = new HashSet<Integer>();
			Map<TextFieldBuilder, Object> valueMap = getEntityPropertyValue4Bom(getDomain());
			StringBuffer sitem=new StringBuffer(), serror=new StringBuffer();
			int cok = 0;
			for (Iterator<TextFieldBuilder> iter=valueMap.keySet().iterator(); iter.hasNext();) {
				TextFieldBuilder builder = iter.next();
				Object ovalue = valueMap.get(builder);
				String svalue = ovalue+"";
				Integer ivalue = null;
				if (ovalue==null)
					continue;
				try {
					ivalue = type.parse(svalue);
					if (ivalue < 1) {
						sitem.append("列序号必须大于0,");
					} else if (colList.add(ivalue)==false) {
						sitem.append("存在相同的列序号").append(ivalue).append(",");
					} else {
						cok++;
					}
				} catch(Exception e) {
					serror.append(ovalue).append(",");
				}
			}
			if (sitem.length()>0)
				sb.append(sitem);
			if (serror.length()>0)
				sb.append("列序号格式错误").append(serror);
			if (sb.length()==0 && cok==0)
				sb.append("请输入列序号，");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void validateImport() throws Exception {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (getBomImports().size()==0)
			sb.append("导入明细为空，");
		CommodityLogic logic = new CommodityLogic();
		int ri=1;
		for (Iterator<BomDetail> iter=getBomImports().iterator(); iter.hasNext(); ri++) {
			BomDetail d = iter.next();
			sitem = new StringBuffer();
			if (logic.getPropertyChoosableLogic().isValid(d.getCommodity(), sitem)==false)
				sitem.append("请补充物料内容，");
			if (d.getBomTicket().getAunit() <= 0)
				sitem.append("物料数量要>0，");
			if (sitem.length() > 0)
				sb.append("第").append(ri).append("行").append(sitem).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (true)
			new CommodityLogic().getMaterialChoosableLogic().trunkViewBuilder(viewBuilder);
		if (viewBuilder.getName().equals("BomImport")) {
			ViewBuilder bomBuilder = (ViewBuilder)viewBuilder.getFieldBuilder("domain");
			for (Iterator<FieldBuilder> iter=bomBuilder.getFieldBuilderIterator(); iter.hasNext();) {
				FieldBuilder builder = iter.next();
//				builder.setParameters(ParameterName.Button, null);
//				builder.setParameters(ParameterName.Listener, null);
				if (builder instanceof ViewBuilder) {
					iter = new JoinedIterator(((ViewBuilder)builder).getFieldBuilderIterator(), iter);
				} else if (builder instanceof TextBuilder) {
					continue;
				} else if (!(builder instanceof TextFieldBuilder)) {
					TextFieldBuilder fb = new TextFieldBuilder();
					TicketPropertyUtil.copyFieldsSkip(builder, fb);
					fb.setViewBuilder(builder.getViewBuilder());
					builder.getViewBuilder().getFieldBuilders().set(builder.getViewBuilder().getFieldBuilders().indexOf(builder), fb);
				}
			}
			this.setAttr("BomBuilder", bomBuilder);
		}
	}
	
	public void addBomDetailSame() {
		BomDetail selectDetail = getSelectFormer4Bom().getFirst();
		List<BomDetail> list = new ArrayList<BomDetail>();
		if ("新增空白行数".length()>0)
			for (double i=getDomain().getBomTicket().getAunit(),cnt=getDetailList().size()+1; i-->0; cnt++) {
				BomDetail d = new BomDetail();
				d.setLevel(selectDetail==null? 1: selectDetail.getLevel());
	//			d.setAmount(cnt);
				list.add(d);
			}
		if (selectDetail==null || getDetailList().indexOf(selectDetail)+1==getDetailList().size()) {
			getDetailList().addAll(list);
			getShowList().addAll(list);
		} else {
			for (int i=getDetailList().indexOf(selectDetail)+1, isize=getDetailList().size(); i<isize; i++) {
				BomDetail d = getDetailList().get(i);
				if (d.getLevel() < selectDetail.getLevel()) {
					getDetailList().addAll(i, list);
					if (getShowList().indexOf(d)>-1)
						getShowList().addAll(i, list);
					else 
						getShowList().addAll(getShowList().indexOf(selectDetail)+1, list);
					break;
				} else if (i+1==isize) {
					getDetailList().addAll(list);
					getShowList().addAll(list);
				}
			}
		}
	}
	
	public void addBomDetailSub() {
		BomDetail selectDetail = getSelectFormer4Bom().getFirst();
		List<BomDetail> list = new ArrayList<BomDetail>();
		if ("新增空白行数".length()>0)
			for (double i=getDomain().getBomTicket().getAunit(),cnt=getDetailList().size()+1; i-->0; cnt++) {
				BomDetail d = new BomDetail();
				d.setLevel(selectDetail.getLevel()+1);
	//			d.setAmount(cnt);
				list.add(d);
			}
		for (int i=getDetailList().indexOf(selectDetail)+1, isize=getDetailList().size(); ; i++) {
			if (i==isize) {
				getDetailList().addAll(i, list);
				getShowList().addAll(i, list);
				return;
			}
			BomDetail d = getDetailList().get(i);
			if (d.getLevel() <= selectDetail.getLevel()) {
				getDetailList().addAll(i, list);
				if (getShowList().indexOf(d)>-1)
					getShowList().addAll(i, list);
				else 
					getShowList().addAll(getShowList().indexOf(selectDetail)+1, list);
				break;
			}
		}
	}
	
	public void addBomDetailImport() {
		BomDetail cur = getSelectFormer4Bom().getFirst();
		if (cur==null) {
			getDetailList().addAll(getBomImports());
			getShowList().addAll(getBomImports());
			return;
		}
		BomDetail follow = new BomTicketLogic().getFollowBrother(getDetailList(), cur);
		if (true) {
			List<BomDetail> sourceList = getDetailList();
			int idx = follow==null? sourceList.size(): sourceList.indexOf(follow);
			sourceList.addAll(idx, getBomImports());
		}
		if (true) {
			List<BomDetail> sourceList = getShowList();
			int idx = follow==null? sourceList.size(): sourceList.indexOf(follow);
			sourceList.addAll(idx, getBomImports());
		}
	}
	
	private void getCommoditySearchNumber(TextField input) {
		this.setIsDialogOpen(false);
		String number = input.getText();
		if (StringUtils.isNotEmpty(number)){
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "CommodityQuery", 1|2, "commNumber", number)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	public void setLevelLink(Hyperlink link, int level, BomDetail detail, int row) {
		StringBuffer sb = new StringBuffer();
		for (int i=detail.getLevel(); i-->1;) {
			sb.append("\t\t");
		}
		int inext=getDetailList().indexOf(detail)+1;
		BomDetail next = inext < getDetailList().size()? getDetailList().get(inext): null;
		link.getEventListenerList().removeListener(EventListenerType.Action, getLinkOnClinkListener());
		if (next!=null && getShowList().indexOf(next)==-1){
			sb.append("+");
			link.getEventListenerList().addActionListener(getLinkOnClinkListener());
		} else if (next!=null && getShowList().indexOf(next)>-1 && next.getLevel()>detail.getLevel()) {
			sb.append("-");
			link.getEventListenerList().addActionListener(getLinkOnClinkListener());
		} else {
			sb.append("|");
		}
		link.setText(sb.toString());
	}
	
	public void setCommoditySelect(List<Commodity> commodityList) {
		Commodity commodity = commodityList.size()==0? new Commodity(): commodityList.get(0);
		BomDetail detail = this.getSelectedList().get(0);
		new CommodityLogic().fromTrunk(detail.getCommodity(), commodity);
	}
	
	public List<BomDetail> getSelectedList() {
		String k = "BOMSelectedList";
		List<BomDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<BomDetail>();
			this.setAttr(k, list);
		}
		return list;
	}

	public void setSelectedList(List<BomDetail> selected) {
		getSelectedList().clear();
		getSelectedList().addAll(selected);
	}
	
	public boolean getIsDialogOpen() {
		String k = "IsDialogOpen";
		Boolean ok = this.getAttr(k);
		if (ok == null) {
			ok = Boolean.FALSE;
			this.setAttr(k, ok);
		}
		return ok;
	}
	
	private void setIsDialogOpen(boolean open) {
		String k = "IsDialogOpen";
		this.setAttr(k, open);
	}
	
	public List<String> getBomArrangeOptions(Object detail) {
		List<String> list = new ArrayList<String>();
//		list.add("去排单");
		list.add(new ArrangeTypeLogic().getNormal());
		list.add("去请购");
		return list;
	}
	
	private void setBomArrangeChange() {
		BomDetail cur = (BomDetail)new ArrayList(this.getSelectedList()).get(0);
		String sarrange = (String)cur.getArrange();
		if ("去排单".equals(sarrange) || new ArrangeTypeLogic().getNormal().equals(sarrange)) {
			for (BomDetail d: new BomTicketLogic().getChildrenFold(this.getDetailList(), cur)) {
				d.setArrange(null);
			}
		}
	}
	
	public void setBomArrangeNull() {
		for (BomDetail d: this.getSelectFormer4Bom().getSelectedList()) {
			d.setArrange(null);
		}
	}
	
	public List<BomDetail> getShowList() {
		String k = "BomShows";
		List<BomDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<BomDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	public void setBomShowLevel() {
		this.setShowBomDetails(getDomain().getLevel());
	}
	
	public void setBomLevelUp() {
		for (BomDetail d: this.getSelectFormer4Bom().getSelectedList()) {
			if (d.getLevel()>1)		d.setLevel(d.getLevel()-1);
		}
	}
	
	public void setBomLevelDown() {
		for (BomDetail d: this.getSelectFormer4Bom().getSelectedList()) {
			d.setLevel(d.getLevel()+1);
		}
	}
	
	public void setBomMoveUpTo() {
		BomTicketLogic logic = new BomTicketLogic();
		if (true) {
			List<BomDetail> sourceList=getDetailList(), selectedList=new ArrayList<BomDetail>(getSelectFormer4Bom().getSelectedList());
			for (int ci=0, csize=selectedList.size(); ci<csize; ci++) {
				BomDetail cur = selectedList.get(ci);
				List<BomDetail> list = logic.getChildrenFold(sourceList, cur);
				getSelectFormer4Bom().getSelectedList().removeAll(list);
			}
		}
		if (true) {
			List<BomDetail> sourceList=getShowList(), selectedList=new ArrayList<BomDetail>(getSelectFormer4Bom().getSelectedList());
			BomDetail first = getSelectFormer4Bom().getFirst();
			int ifirst=sourceList.indexOf(first);
			selectedList.remove(first);
			for (int ci=selectedList.size(); ci-->0;) {
				BomDetail cur = selectedList.get(ci);
				List<BomDetail> list = logic.getChildrenFold(sourceList, cur);
				selectedList.addAll(ci+1, list);
			}
			sourceList.removeAll(selectedList);
			sourceList.addAll(ifirst, selectedList);
		}
		if (true) {
			List<BomDetail> sourceList=getDetailList(), selectedList=new ArrayList<BomDetail>(getSelectFormer4Bom().getSelectedList());
			BomDetail first = getSelectFormer4Bom().getFirst();
			int ifirst=sourceList.indexOf(first);
			selectedList.remove(first);
			for (int ci=selectedList.size(); ci-->0;) {
				BomDetail cur = selectedList.get(ci);
				List<BomDetail> list = logic.getChildrenFold(sourceList, cur);
				selectedList.addAll(ci+1, list);
			}
			sourceList.removeAll(selectedList);
			sourceList.addAll(ifirst, selectedList);
		}
	}
	
	public void setBomMoveDownTo() {
		BomTicketLogic logic = new BomTicketLogic();
		if (true) {
			List<BomDetail> sourceList=getDetailList(), selectedList=new ArrayList<BomDetail>(getSelectFormer4Bom().getSelectedList());
			for (int ci=0, csize=selectedList.size(); ci<csize; ci++) {
				BomDetail cur = selectedList.get(ci);
				List<BomDetail> list = logic.getChildrenFold(sourceList, cur);
				getSelectFormer4Bom().getSelectedList().removeAll(list);
			}
		}
		if (true) {
			List<BomDetail> sourceList=getShowList(), selectedList=new ArrayList<BomDetail>(getSelectFormer4Bom().getSelectedList());
			BomDetail last = selectedList.get(selectedList.size()-1);
			int ilast=sourceList.indexOf(last);
			selectedList.remove(last);
			for (int ci=selectedList.size(); ci-->0;) {
				BomDetail cur = selectedList.get(ci);
				List<BomDetail> list = logic.getChildrenFold(sourceList, cur);
				selectedList.addAll(ci+1, list);
			}
			sourceList.removeAll(selectedList);
			ilast -= selectedList.size();
			sourceList.addAll(ilast+1, selectedList);
		}
		if (true) {
			List<BomDetail> sourceList=getDetailList(), selectedList=new ArrayList<BomDetail>(getSelectFormer4Bom().getSelectedList());
			BomDetail last = selectedList.get(selectedList.size()-1);
			int ilast=sourceList.indexOf(last);
			selectedList.remove(last);
			for (int ci=selectedList.size(); ci-->0;) {
				BomDetail cur = selectedList.get(ci);
				List<BomDetail> list = logic.getChildrenFold(sourceList, cur);
				selectedList.addAll(ci+1, list);
			}
			sourceList.removeAll(selectedList);
			ilast -= selectedList.size();
			sourceList.addAll(ilast+1, selectedList);
		}
	}
	
	public void setBomRemove() {
		StringBuffer error = new StringBuffer();
		for (BomDetail bom: this.getSelectFormer4Bom().getSelectedList()) {
			if (bom.getStPurchase()>0)
				error.append("序号").append(bom.getSn()).append(bom.getCommodity().getName()).append("已经采购不能删除，");
		}
		if (error.length()>0)
			throw new LogicException(2, error.deleteCharAt(error.length()-1).toString());
		for (BomDetail cur: this.getSelectFormer4Bom().getSelectedList()) {
			List<BomDetail> list = new BomTicketLogic().getChildrenFold(this.getDetailList(), cur);
			list.add(cur);
			getShowList().removeAll(list);
			getDetailList().removeAll(list);
		}
	}
	
	private void setShowBomDetails(int level) {
		List<BomDetail> list = new ArrayList<BomDetail>();
		for (BomDetail d: getDetailList()) {
			if (d.getLevel() <= level)
				list.add(d);
		}
		getShowList().clear();
		getShowList().addAll(list);
	}
	
	public void setBomDefaultLabelIndex() throws ParseException {
		ViewBuilder bomBuilder = this.getBomBuilder();
		int ci = 0;
		for (TextFieldBuilder builder: new ArrayList<TextFieldBuilder>(bomBuilder.getFieldBuildersDeep(TextFieldBuilder.class))) {
			Object v = builder.getFormatType().parse((++ci)+"");
			builder.setEntityPropertyValue(getDomain(), v);
		}
	}
	
	public void setBomImportFormated() throws Exception {
		List<String> tList = new ArrayList<String>();
		tList.addAll(Arrays.asList(getDomain().getCommodity().getRemark().split("[\t]")));
		for (int ti=tList.size(); ti-->0; ) {
			String t = tList.get(ti);
			if (t.startsWith("\"") && t.endsWith("\""))
				tList.set(ti, new StringBuffer(t).deleteCharAt(t.length()-1).deleteCharAt(0).toString());
			else if (t.indexOf("\n") > -1) {
				List<String> subs = new ArrayList<String>();
				subs.add(t.substring(0, t.indexOf("\n")));
				subs.add("\n");
				subs.add(t.substring(t.indexOf("\n")+1));
				tList.remove(ti);
				tList.addAll(ti, subs);
			}
		}
		ViewBuilder bomBuilder = (ViewBuilder)this.getBomBuilder().getViewBuilder().getFieldBuilder("bomImports");
		Map<TextFieldBuilder, Object> valueMap = this.getEntityPropertyValue4Bom(this.getDomain());
		LinkedHashMap<Integer, FieldBuilder> fieldList = new LinkedHashMap<Integer, FieldBuilder>();
		IntegerType type = new IntegerType();
		for (TextFieldBuilder b: valueMap.keySet()) {
			if (valueMap.get(b)==null)
				continue;
			Integer col = type.parse(valueMap.get(b)+"");
			FieldBuilder colb = bomBuilder.getFieldBuilder(b.getName());
			fieldList.put(col, colb);
		}
		for (int ti=0, tsize=tList.size(); ti<tsize; ti++) {
			BomDetail detail = new BomDetail();
			getBomImports().add(detail);
			StringBuffer error = new StringBuffer();
			for (int ci=0, csize=fieldList.size(); ci<csize && ti<tsize; ci++, ti++) {
				String t=tList.get(ti);
				FieldBuilder builder = fieldList.get(ci+1);
				if (builder==null)
					continue;
				if ("\n".equals(t))
					break;
				try {
					Object v=builder.getFormatType().parse(t), v1=null;
					if (!(builder instanceof TextFieldBuilder)) {
						v1 = builder.pickValue(builder.build(v).getComponent());
						builder.setEntityPropertyValue(detail, v1);
						if (StringUtils.equals(v+"", v1+"")==false)
							throw new ParseException(new StringBuffer().append(v).append(v1).toString(), 0);
					} else {
						builder.setEntityPropertyValue(detail, v);
					}
				} catch(Exception e) {
					error.append(builder.getLabel()).append(t).append(",");
				}
				if (ci+1==csize) {
					for (ti++; ti<tsize && "\n".equals(tList.get(ti))==false; ti++);
					break;
				}
			}
			detail.getCommodity().getVoParamMap().put("error", error.toString());
		}
	}
	
	public Map<TextFieldBuilder, Object> getEntityPropertyValue4Bom(BomDetail entity) {
		LinkedHashMap<TextFieldBuilder, Object> map = new LinkedHashMap<TextFieldBuilder, Object>();
		ViewBuilder bomBuilder = this.getBomBuilder();
		List<TextFieldBuilder> list = bomBuilder.getFieldBuildersDeep(TextFieldBuilder.class);
		for (TextFieldBuilder b: list) {
			StringBuffer name = new StringBuffer();
			name.append(b.getName());
			for (ViewBuilder cur=b.getViewBuilder(),prt=cur.getViewBuilder(); cur.getPresentClass().getBeanClass()!=entity.getClass(); cur=prt,prt=cur==null? null: cur.getViewBuilder()) {
				name.insert(0, cur.getName().concat("."));
			}
			Object value = ReflectHelper.getPropertyValue(entity, name.toString());
			map.put(b, value);
		}
		return map;
	}
	
	public List<BomDetail> getBomImports() {
		String k = "BomImports";
		List<BomDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<BomDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	public List<BomDetail> getDetailList() {
		String k = "BomDetails";
		List<BomDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<BomDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private void setDetailList(List<BomDetail> list0) {
		List<BomDetail> list = new ArrayList<BomDetail>(list0==null? new ArrayList(): list0);
		getDetailList().clear();
		getDetailList().addAll(list);
	}
	
	public BomDetail getDomain() {
		BomDetail d = this.getAttr(BomDetail.class);
		if (d==null) {
			d = new BomDetail();
			this.setAttr(d);
		}
		return d;
	}
	
	public AbstractForm getFirerForm() {
		return this.getAttr("firerForm");
	}
	
	public SelectTicketFormer4Edit<BOMForm, BomDetail> getSelectFormer4Bom() {
		String k = "SelectFormer4Bom";
		SelectTicketFormer4Edit former = this.getAttr(k);
		if (former==null) {
			former = new SelectTicketFormer4Edit<BOMForm, BomDetail>(this);
			this.setAttr(k, former);
		}
		return former;
	}
	
	private ViewBuilder getBomBuilder() {
		String k = "BomBuilder";
		ViewBuilder builder = this.getAttr(k);
		return builder;
	}
	
	public LinkOnClinkListener getLinkOnClinkListener() {
		LinkOnClinkListener listener = getAttr(LinkOnClinkListener.class);
		if (listener == null) {
			listener = new LinkOnClinkListener();
			listener.form = this;
			this.setAttr(listener);
		}
		return listener;
	}
	
	public CommodityForm getCommodityForm() {
		return new CommodityForm();
	}
	
	private	Commodity getMaterialTicket() {
		Commodity ticket = getAttr(Commodity.class);
		if (ticket==null) {
			ticket = new Commodity();
			this.setAttr(ticket);
		}
		return ticket;
	}
	
	public List<SupplyType> getSupplyTypeOptions(Object entity) {
		List<SupplyType> typeList = new SupplyTypeLogic().getTypeList();
		return typeList;
	}
	
	public List<BomDetail> getBomCopy() {
		String k="BomCopyList";
		List<BomDetail> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<BomDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	public void setBomCopy() {
		LinkedHashSet<BomDetail> list = new LinkedHashSet<BomDetail>();
		BomTicketLogic logic = new BomTicketLogic();
		for (BomDetail d: this.getSelectFormer4Bom().getSelectedList()) {
			list.add(d);
			list.addAll(logic.getChildrenFold(getDetailList(), d));
		}
		getBomCopy().clear();
		getBomCopy().addAll(list);
	}
	
	public void setBomPaste() {
		BomDetail cur = getSelectFormer4Bom().getFirst();
		BomDetail next = cur==null? null: new BomTicketLogic().getFollowBrother(getDetailList(), cur);
		List<BomDetail> copyList = new ArrayList<BomDetail>();
		for (BomDetail d: getBomCopy()) {
			BomDetail nd = TicketPropertyUtil.copyProperties(d, new BomDetail());
			copyList.add(nd);
		}
		if (true) {
			List<BomDetail> sourceList = getDetailList();
			sourceList.addAll(next==null? sourceList.size(): sourceList.indexOf(next), copyList);
		}
		if (true) {
			List<BomDetail> sourceList = getShowList();
			sourceList.addAll(next==null? sourceList.size(): sourceList.indexOf(next), copyList);
		}
	}
	
	public static class LinkOnClinkListener implements ActionListener {
		
		private BOMForm form;

		public void perform(EventObject e) {
			Hyperlink link = (Hyperlink)e.getSource();
			BomDetail cur = (BomDetail)link.searchFormerByClass(Field.class).getEntityBean().getBean();
			ListView view = link.searchFormerByClass(ListView.class);
			if (link.getText().endsWith("+")) {
				unfold(cur);
				view.update();
			} else if (link.getText().endsWith("-")) {
				fold(cur);
				view.update();
			}
		}
		
		private void unfold(BomDetail cur) {
			List<BomDetail> list = new BomTicketLogic().getChildrenBrother(form.getDetailList(), cur);
			form.getShowList().addAll(form.getShowList().indexOf(cur)+1, list);
		}
		
		private void fold(BomDetail cur) {
			List<BomDetail> list = new BomTicketLogic().getChildrenFold(form.getDetailList(), cur);
			form.getShowList().removeAll(list);
		}
	}
}
