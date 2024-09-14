package com.haoyong.sales.common.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import net.sf.mily.bean.BeanClass;
import net.sf.mily.bean.BeanClass.Accessor;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.TypeFactory;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.TextAreaBuilder;
import net.sf.mily.webObject.TextBuilder;
import net.sf.mily.webObject.ViewBuilder;
import net.sf.mily.webObject.query.ColumnField;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.ClientT;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.logic.SellerViewSettingLogic;
import com.haoyong.sales.common.domain.AbstractDomain;
import com.haoyong.sales.common.domain.PropertyChoosable;
import com.haoyong.sales.common.domain.TAlongable;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.sale.domain.ArrangeT;
import com.haoyong.sales.sale.domain.LocationTicket;
import com.haoyong.sales.sale.domain.OrderT;

public abstract class PropertyChoosableLogic<F extends AbstractForm, D extends PropertyChoosable, A extends TAlongable> {
	
	private Class<F> formClss;
	protected D domain;
	protected A talong;
	
	protected PropertyChoosableLogic(F form, D domain, A talong) {
		this.formClss = (Class<F>)form.getClass();
		this.domain = domain;
		this.talong = talong;
	}
	
	public SellerViewSetting getChooseSetting(ViewBuilder standBuilder) {
		SellerViewSetting setting = new SellerViewSettingLogic().getViewSetting(standBuilder, domain.getTrunkDefault());
		return setting;
	}
	
	protected ViewBuilder getChooseBuilder(String pname) {
		FieldBuilder builder = null;
		for (Iterator<FieldBuilder> iter=EntityClass.forName(formClss).getFieldBuilders().iterator(); builder==null && iter.hasNext();) {
			FieldBuilder fb = iter.next();
			if (!((fb instanceof ViewBuilder) && fb.getProperty().getReturnType()==domain.getClass()))
				continue;
			builder = ((ViewBuilder)fb).getFieldBuilder(pname);
		}
		if (builder==null && AbstractForm.class.isAssignableFrom(formClss.getSuperclass())==true && formClss.getSuperclass()!=AbstractForm.class) {
			for (Iterator<FieldBuilder> iter=EntityClass.forName(formClss.getSuperclass()).getFieldBuilders().iterator(); builder==null && iter.hasNext();) {
				FieldBuilder fb = iter.next();
				if (!((fb instanceof ViewBuilder) && fb.getProperty().getReturnType()==domain.getClass()))
					continue;
				builder = ((ViewBuilder)fb).getFieldBuilder(pname);
			}
		}
		return builder==null? null: (ViewBuilder)builder.createClone();
	}

	protected List<String> getCrossFields(BeanClass one, BeanClass other) {
		List<String> oneFields=one.getFieldNames(), otherFields=other.getFieldNames(), crossFields=one.getFieldNames();
		oneFields.removeAll(otherFields);
		crossFields.removeAll(oneFields);
		return crossFields;
	}
	
	public static class Choose12<F extends AbstractForm, D extends PropertyChoosable, A extends TAlongable> extends PropertyChoosableLogic<F, D, A> {
		
		public Choose12(F form, D domain, A talong) {
			super(form, domain, talong);
		}
		
		public void trunkViewBuilder(ViewBuilder curBuilder) {
			for (ViewBuilder stand0=this.getChooseBuilder(), stand=null; stand0!=null && "单头一次".length()>0; stand0=null) {
				for (ViewBuilder curViewBuilder: this.getBeanViewList(curBuilder)) {
					if (stand==null)
						stand = super.getStandardClone(super.getChooseSetting(stand0), stand0);
					super.trunkViewBuilder(curViewBuilder, super.getChooseSetting(stand0).getTrunkList(stand.getFieldBuilderLeafs()), stand);
				}
			}
		}
		
		public void trunkAppend(AbstractDomain entity, String fieldValue) {
			ViewBuilder standardBuilder = this.getChooseBuilder();
			SellerViewSetting setting = this.getChooseSetting(this.getChooseBuilder());
			List<FieldBuilder> chooseList=setting.getTrunkList(standardBuilder.getFieldBuilderLeafs());
			Class beanClass = standardBuilder.getPresentClass().getBeanClass();
			FieldBuilder strBuilder = null;
			List<String> crossList = this.getCrossFields(BeanClass.getBeanClass(this.domain.getClass()), BeanClass.getBeanClass(this.talong.getClass()));
			for (FieldBuilder builder: chooseList) {
				Object val = builder.getProperty().getDynaProperty().get(entity);
				if (crossList.contains(builder.getName()))
					continue;
				if (strBuilder==null && builder.getProperty().getDynaProperty().getReturnType()==String.class)
					strBuilder = builder;
				if (!(val instanceof String))
					continue;
				val = String.valueOf(val).concat(fieldValue);
				builder.getProperty().getDynaProperty().set(entity, val);
				return;
			}
			if (strBuilder != null) {
				strBuilder.getProperty().getDynaProperty().set(entity, fieldValue);
			}
		}
		
		public void chooseViewBuilder(ViewBuilder curBuilder) {
			for (ViewBuilder curViewBuilder: this.getBeanViewList(curBuilder)) {
				ViewBuilder stand0=this.getChooseBuilder();
				SellerViewSetting setting = this.getChooseSetting(stand0);
				ViewBuilder<Component> stand=super.getStandardClone(setting, stand0);
				super.chooseViewBuilder(curViewBuilder, setting.getChooseList(stand.getFieldBuilderLeafs()), stand);
			}
		}
		
		public <A extends TAlongable> void fromTrunk(D targetDomain, A fromT) throws Exception {
			BeanClass dclass=BeanClass.getBeanClass(targetDomain.getClass()), tclass=BeanClass.getBeanClass(fromT.getClass());
			List<String> crossFields=super.getCrossFields(dclass, tclass);
			for (String name: crossFields) {
				Object tvalue = tclass.getFieldAccessor(name).get(fromT);
				dclass.getFieldAccessor(name).set(targetDomain, tvalue);
			}
			if ("主干一次".length()>0)
				super.fromTrunk(targetDomain, fromT, crossFields, fromT.getChooseValue());
		}
		
		public void fromTrunk(D target, D from) {
			if (from==null)
				return;
			if ("主干一次".length()>0)
				super.fromTrunk(getChooseBuilder(), target, from);
		}
		
		public A toTrunk(D domain) {
			A talong = super.talong;
			BeanClass dclass=BeanClass.getBeanClass(domain.getClass()), tclass=BeanClass.getBeanClass(talong.getClass());
			List<String> crossFields = super.getCrossFields(dclass, tclass);
			if (talong.getClass()==CommodityT.class)
				"12".toCharArray();
			for (String name: crossFields) {
				try {
					Object dvalue = dclass.getFieldAccessor(name).get(domain);
					tclass.getFieldAccessor(name).set(talong, dvalue);
				}catch(Exception e) {
					throw new RuntimeException(new StringBuffer().append("读写").append(name).append("失败，").toString());
				}
			}
			if ("主干一次".length()>0) {
				String[] namevalues = super.toTrunk(this.getChooseBuilder(), crossFields, domain, talong);
				talong.setChooseValue(namevalues[0], namevalues[1], namevalues[2]);
			}
			return talong;
		}
		
		public boolean isValid(D domain, StringBuffer serror0) {
			SellerViewSetting viewSetting = getChooseSetting(this.getChooseBuilder());
			TextBuilder textBuilder = new TextBuilder();
			ViewBuilder viewBuilder = getChooseBuilder();
			A talong = super.talong;
			BeanClass dclass=BeanClass.getBeanClass(domain.getClass()), tclass=BeanClass.getBeanClass(talong.getClass());
			List<String> crossFields = super.getCrossFields(dclass, tclass);
			StringBuffer serror = new StringBuffer();
			int csetted = 0;
			for (String name: crossFields) {
				try {
					Object dvalue = dclass.getFieldAccessor(name).get(domain);
					FieldBuilder fbuilder = viewBuilder.getFieldBuilder(name);
					boolean require = fbuilder==null? false: StringUtils.equals("Require", fbuilder.getParameter(ParameterName.Cfg).getString(ParameterName.Type));
					if (require && viewSetting.getRequireList().contains(name)==false)
						viewSetting.getRequireList().add(name);
					if (StringUtils.isBlank(textBuilder.getFormatedText(dvalue)))
						continue;
					tclass.getFieldAccessor(name).set(talong, dvalue);
					csetted++;
				}catch(Exception e) {
					serror.append("读写").append(name).append("失败，");
				}
			}
			if (viewBuilder != null) {
				List<FieldBuilder> trunkList = viewSetting.getTrunkList(viewBuilder.getFieldBuilders());
				List<String> requireList = viewSetting.getRequireList();
				StringBuffer sname=new StringBuffer(), svalue=new StringBuffer();
				if (domain.getClass() == Client.class)
					"1".toCharArray();
				for (Iterator<FieldBuilder> iter=viewBuilder.getFieldBuilderLeafs().iterator(); iter.hasNext();) {
					FieldBuilder f = iter.next();
					Object fvalue = f.getProperty().getDynaProperty().get(domain);
					String ftext = textBuilder.getFormatedText(fvalue);
					if (requireList.contains(f.getName()) && StringUtils.isEmpty(ftext)) {
						serror.append(f.getLabel()).append("不能为空，");
					}
					if (StringUtils.isEmpty(ftext)) {
						continue;
					} else if (trunkList.contains(f)==false || crossFields.contains(f.getName())) {
						continue;
					} else {
						sname.append(f.getName()).append(PropertyChoosable.SplitAppend);
						svalue.append(ftext).append(PropertyChoosable.SplitAppend);
						csetted++;
					}
					if (!iter.hasNext() && svalue.length()>0) {
						sname.deleteCharAt(sname.length()-1);
						svalue.deleteCharAt(svalue.length()-1);
					}
				}
			}
			if (serror.length()>0) {
				serror0.append(serror);
				return false;
			}
			return true;
		}
		
		public List<ViewBuilder> getBeanViewList(ViewBuilder curBuilder) {
			Class beanClass = super.domain.getClass();
			List<ViewBuilder> curViewList = curBuilder.getViewBuilders(beanClass);
			return curViewList;
		}

		public ViewBuilder getChooseBuilder() {
			return super.getChooseBuilder("choose");
		}
	}
	
	public static class TicketDetail<F extends AbstractForm, D extends PropertyChoosable, A extends TAlongable> extends PropertyChoosableLogic<F, D, A> {
		
		public TicketDetail(F form, D domain, A talong) {
			super(form, domain, talong);
		}
		public <A extends TAlongable> void fromTrunk(D targetDomain, A fromT) {
			BeanClass dclass=BeanClass.getBeanClass(targetDomain.getClass()), tclass=BeanClass.getBeanClass(fromT.getClass());
			List<String> crossFields=super.getCrossFields(dclass, tclass);
			try {
				for (String name: crossFields) {
					Object tvalue = tclass.getFieldAccessor(name).get(fromT);
					dclass.getFieldAccessor(name).set(targetDomain, tvalue);
				}
				if (fromT.getClass()==OrderT.class)
					"".toCharArray();
				String[] choose=null;
				if ((choose=fromT.getChooseValue())!=null && "单头一次".length()>0)
					super.fromTrunk(targetDomain, fromT, crossFields, choose);
				if ((choose=fromT.getChooseValue2())!=null && "明细一次".length()>0)
					super.fromTrunk(targetDomain, fromT, crossFields, choose);
				if ((choose=fromT.getHandleValue())!=null && "处理一次".length()>0)
					super.fromTrunk(targetDomain, fromT, crossFields, choose);
			} catch(Exception e) {
				throw LogUtil.getRuntimeException(e);
			}
		}
		
		public void fromTrunk(D target, D from) {
			if (from==null)
				return;
			ViewBuilder vbuilder=null;
			if ((vbuilder=this.getTicketBuilder())!=null && "单头一次".length()>0)
				super.fromTrunk(vbuilder, target, from);
			if ((vbuilder=this.getDetailBuilder())!=null && "明细一次".length()>0)
				super.fromTrunk(vbuilder, target, from);
			if ((vbuilder=this.getHandleBuilder())!=null && "处理一次".length()>0)
				super.fromTrunk(vbuilder, target, from);
		}
		
		public A toTrunk(D domain) {
			A talong = super.talong;
			BeanClass dclass=BeanClass.getBeanClass(domain.getClass()), tclass=BeanClass.getBeanClass(talong.getClass());
			List<String> crossFields = super.getCrossFields(dclass, tclass);
			if (talong.getClass()==ClientT.class)
				"123".toCharArray();
			for (String name: crossFields) {
				try {
					Object dvalue = dclass.getFieldAccessor(name).get(domain);
					tclass.getFieldAccessor(name).set(talong, dvalue);
				}catch(Exception e) {
					throw new RuntimeException(new StringBuffer().append("读写").append(name).append("失败，").toString());
				}
			}
			ViewBuilder vbuilder=null;
			if ((vbuilder=this.getTicketBuilder())!=null && "单头只跑一次".length()>0) {
				String[] namevalues = super.toTrunk(vbuilder, crossFields, domain, talong);
				talong.setChooseValue(namevalues[0], namevalues[1], namevalues[2]);
			}
			if ((vbuilder=this.getDetailBuilder())!=null && "明细只跑一次".length()>0) {
				String[] namevalues = super.toTrunk(vbuilder, crossFields, domain, talong);
				talong.setChooseValue2(namevalues[0], namevalues[1], namevalues[2]);
			}
			if ((vbuilder=this.getHandleBuilder())!=null && "处理只跑一次".length()>0) {
				String[] namevalues = super.toTrunk4Handle(vbuilder, domain);
				talong.setHandleValue(namevalues[0], namevalues[1]);
			}
			return talong;
		}
		
		public boolean isValid(D domain, StringBuffer serror0) {
			SellerViewSetting viewSetting = getChooseSetting(this.getTicketBuilder()!=null? this.getTicketBuilder(): this.getHandleBuilder());
			TextBuilder textBuilder = new TextBuilder();
			A talong = super.talong;
			BeanClass dclass=BeanClass.getBeanClass(domain.getClass()), tclass=BeanClass.getBeanClass(talong.getClass());
			List<String> crossFields = super.getCrossFields(dclass, tclass);
			StringBuffer serror = new StringBuffer();
			int csetted = 0;
			for (String name: crossFields) {
				try {
					Object dvalue = dclass.getFieldAccessor(name).get(domain);
					if (StringUtils.isBlank(textBuilder.getFormatedText(dvalue)))
						continue;
					tclass.getFieldAccessor(name).set(talong, dvalue);
					csetted++;
				}catch(Exception e) {
					serror.append("读写").append(name).append("失败，");
				}
			}
			for (ViewBuilder viewBuilder: new ViewBuilder[]{this.getTicketBuilder(), this.getDetailBuilder(), this.getHandleBuilder()}) {
				if (viewBuilder==null)
					continue;
				List<FieldBuilder> trunkList = viewSetting.getTrunkList(viewBuilder.getFieldBuilderLeafs());
				List<String> requireList = viewSetting.getRequireList();
				StringBuffer sname=new StringBuffer(), svalue=new StringBuffer();
				if (domain.getClass() == Client.class)
					"".toCharArray();
				for (Iterator<FieldBuilder> iter=viewBuilder.getFieldBuilderLeafs().iterator(); iter.hasNext();) {
					FieldBuilder f = iter.next();
					Object fvalue = f.getProperty().getDynaProperty().get(domain);
					String ftext = textBuilder.getFormatedText(fvalue);
					if (requireList.contains(f.getName()) && StringUtils.isEmpty(ftext)) {
						serror.append(f.getLabel()).append("不能为空，");
					}
					if (StringUtils.isEmpty(ftext)) {
					} else if (trunkList.contains(f)==false || crossFields.contains(f.getName())) {
					} else {
						sname.append(f.getName()).append(PropertyChoosable.SplitAppend);
						svalue.append(ftext).append(PropertyChoosable.SplitAppend);
						csetted++;
					}
					if (!iter.hasNext() && svalue.length()>0) {
						sname.deleteCharAt(sname.length()-1);
						svalue.deleteCharAt(svalue.length()-1);
					}
				}
			}
			if (serror.length()>0) {
				serror0.append(serror);
				return false;
			}
			return true;
		}
		
		public void trunkViewBuilder(ViewBuilder curBuilder) {
			if (super.talong.getClass()==ArrangeT.class && curBuilder.getFieldBuildersSelf(SqlListBuilder.class).size()==0)
				"1".toCharArray();
			if ("查询".length()>0 && 1==1) {
				for (ViewBuilder stand0=this.getTicketBuilder(), stand=null; stand0!=null && "Ticket".length()>0; stand0=null) {
					for (Iterator<ViewBuilder> iter=curBuilder.getViewBuilders(domain.getClass()).iterator(); iter.hasNext();) {
						ViewBuilder curViewBuilder0=iter.next();
						if (curViewBuilder0.getClass()!=SqlListBuilder.class)
							continue;
						SqlListBuilder sqlBuilder = (SqlListBuilder)curViewBuilder0;
						List<ColumnField> curList=sqlBuilder.getSqlQuery().getFields().getFields();
						List<FieldBuilder> chooseList=null;
						LinkedHashSet<ColumnField> toList = new LinkedHashSet<ColumnField>();
						chooseList=super.getChooseSetting(stand0).getTrunkList(new ArrayList<FieldBuilder>(curList));
						toList.add(curList.get(0));
						toList.addAll(new ArrayList(chooseList));
						for (ColumnField f: curList) {
							if (stand0.getFieldBuilder(f.getName()) == null) {
								toList.add(f);
							}
						}
						curList.clear();
						curList.addAll(toList);
					}
				}
			}
			if ("编辑".length()>0) {
				"".toCharArray();
				for (ViewBuilder stand0=this.getTicketBuilder(), stand=null; stand0!=null && "Ticket".length()>0; stand0=null) {
					for (Iterator<ViewBuilder> iter=this.getBeanViewList(curBuilder, stand0).iterator(); iter.hasNext();) {
						ViewBuilder curViewBuilder=iter.next();
						if (stand==null)
							stand = super.getStandardClone(super.getChooseSetting(stand0), stand0);
						super.trunkViewBuilder(curViewBuilder, super.getChooseSetting(stand0).getTrunkList(stand.getFieldBuilderLeafs()), stand);
					}
				}
				for (ViewBuilder stand0=this.getDetailBuilder(), stand=null; stand0!=null && "Detail".length()>0; stand0=null) {
					for (Iterator<ViewBuilder> iter=this.getBeanViewList(curBuilder, stand0).iterator(); iter.hasNext();) {
						ViewBuilder curViewBuilder=iter.next();
						if (stand==null)
							stand = super.getStandardClone(super.getChooseSetting(stand0), stand0);
						super.trunkViewBuilder(curViewBuilder, super.getChooseSetting(stand0).getTrunkList(stand.getFieldBuilderLeafs()), stand);
					}
				}
				for (ViewBuilder stand0=this.getHandleBuilder(), stand=null; stand0!=null && "Handle".length()>0; stand0=null) {
					for (Iterator<ViewBuilder> iter=this.getBeanViewList(curBuilder, stand0).iterator(); iter.hasNext();) {
						ViewBuilder curViewBuilder=iter.next();
						if (stand==null)
							stand = super.getStandardClone(super.getChooseSetting(stand0), stand0);
						super.trunkViewBuilder(curViewBuilder, super.getChooseSetting(stand0).getTrunkList(stand.getFieldBuilderLeafs()), stand);
					}
				}
			}
		}
		
		private List<ViewBuilder> getBeanViewList(ViewBuilder curBuilder, ViewBuilder standard) {
			Class beanClass = super.domain.getClass();
			List<ViewBuilder> curViewList = curBuilder.getViewBuilders(beanClass);
			String name = ".".concat(standard.getName());
			List<ViewBuilder> list = new ArrayList<ViewBuilder>();
			for (Iterator<ViewBuilder> iter=curViewList.iterator(); iter.hasNext();) {
				ViewBuilder curItem = iter.next();
				if (curItem.isVisible()==false)
					continue;
				if (curItem.getName().equals(standard.getName()) || curItem.getName().endsWith(name))
					list.add(curItem); 
			}
			return list;
		}
		
		public ViewBuilder getHandleBuilder() {
			return super.getChooseBuilder("Handle");
		}

		public ViewBuilder getTicketBuilder() {
			return super.getChooseBuilder("Ticket");
		}

		public ViewBuilder getDetailBuilder() {
			return super.getChooseBuilder("Detail");
		}
		
		public TicketDetail<F,D,A> setTAlong(A talong) {
			super.talong = talong;
			return this;
		}
	}

	private void trunkViewBuilder(ViewBuilder curViewBuilder, List<FieldBuilder> chooseList, ViewBuilder standardBuilder) {
		SellerViewSetting setting = this.getChooseSetting(standardBuilder);
		if (domain.getClass() == LocationTicket.class)
			"".toCharArray();
		String hiddens = curViewBuilder.getParameter(ParameterName.Cfg).getString(ParameterName.Exclude);
		if (StringUtils.isNotEmpty(hiddens)) {
			"".toCharArray();
			for (String hd: hiddens.split("\\,")) {
				FieldBuilder hidden = standardBuilder.getFieldBuilder(hd);
				if (chooseList.contains(hidden)) {
					chooseList.remove(hidden);
				}
			}
		}
		if (curViewBuilder instanceof SqlListBuilder) {
			this.chooseViewBuilder(curViewBuilder, setting.getChooseList(standardBuilder.getFieldBuilders()), standardBuilder);
			return;
		} else if (StringUtils.equals("Choose-Edit", curViewBuilder.getParameter(ParameterName.Cfg).getString(ParameterName.Property))) {
			"".toCharArray();
			for (FieldBuilder f: chooseList) {
				if (f.getClass()==TextBuilder.class)
					continue;
				FieldBuilder curf = curViewBuilder.getFieldBuilder(f.getName());
				if (curf!=null) {
					curViewBuilder.getFieldBuilders().remove(curf);
					curViewBuilder.addFieldBuilder(curf);
				} else {
					FieldBuilder fi = f.createClone();
					fi.setParameters(fi.cloneParameters());
					curViewBuilder.addFieldBuilder(fi);
				}
			}
		} else if (StringUtils.equals("Inherit", curViewBuilder.getParameter(ParameterName.Cfg).getString(ParameterName.Properties))) {
			curViewBuilder.getFieldBuilders().clear();
			for (FieldBuilder f: chooseList) {
				FieldBuilder fi = f.createClone();
				fi.setParameters(fi.cloneParameters());
				curViewBuilder.addFieldBuilder(fi);
			}
		}
	}
	
	private void chooseViewBuilder(ViewBuilder curViewBuilder, List<FieldBuilder> chooseList, ViewBuilder standardBuilder) {
		SellerViewSetting setting = this.getChooseSetting(standardBuilder);
		Class beanClass = standardBuilder.getPresentClass().getBeanClass();
		if (beanClass == Client.class)
			"".toCharArray();
		String hiddens = curViewBuilder.getParameter(ParameterName.Cfg).getString(ParameterName.Exclude);
		if (StringUtils.isNotEmpty(hiddens)) {
			for (String hd: hiddens.split("\\,")) {
				FieldBuilder hidden = standardBuilder.getFieldBuilder(hd);
				if (chooseList.contains(hidden)) {
					chooseList.remove(hidden);
				}
			}
		}
		if (curViewBuilder instanceof SqlListBuilder) {
			SqlListBuilder sqlBuilder = (SqlListBuilder)curViewBuilder;
			List<ColumnField> curList = sqlBuilder.getSqlQuery().getFields().getFields();
			LinkedHashSet<ColumnField> toList = new LinkedHashSet<ColumnField>();
			chooseList=setting.getChooseList(new ArrayList<FieldBuilder>(curList));
			toList.add(curList.get(0));
			toList.addAll(new ArrayList(chooseList));
			for (ColumnField f: curList) {
				if (standardBuilder.getFieldBuilder(f.getName()) == null) {
					toList.add(f);
				}
			}
			curList.clear();
			curList.addAll(toList);
		} else if (curViewBuilder.getFieldBuilders().size()==0){
			for (FieldBuilder builder: chooseList) {
				FieldBuilder fi = builder.createClone();
				fi.setParameters(fi.cloneParameters());
				fi.setParameters(ParameterName.Button, null);
				fi.setParameters(ParameterName.Listener, null);
				curViewBuilder.addFieldBuilder(fi);
			}
		} else if (StringUtils.equals("Self", curViewBuilder.getParameter(ParameterName.Cfg).getString(ParameterName.Property))) {
		} else {
			List<FieldBuilder> prelist=new ArrayList<FieldBuilder>(curViewBuilder.getFieldBuilders()), exList=new ArrayList<FieldBuilder>();
			curViewBuilder.getFieldBuilders().clear();
			for (FieldBuilder f: prelist) {
				if (standardBuilder.getFieldBuilder(f.getName())==null)
					curViewBuilder.addFieldBuilder(f);
			}
			for (FieldBuilder f: chooseList) {
				if (f.getClass()==TextBuilder.class)		continue;
				FieldBuilder fi = null;
				for (Iterator<FieldBuilder> piter=prelist.iterator(); ;) {
					if (piter.hasNext()==false) {
						fi = f.createClone();
						fi.setParameters(fi.cloneParameters());
						fi.setParameters(ParameterName.Button, null);
						fi.setParameters(ParameterName.Listener, null);
						break;
					}
					FieldBuilder pre = piter.next();
					if (f.getName().equals(pre.getName())) {
						fi = pre;
						break;
					}
				}
				curViewBuilder.addFieldBuilder(fi);
			}
		}
	}
	private void fromTrunk(PropertyChoosable targetDomain, TAlongable fromT, List<String> crossFields, String[] choose) throws Exception {
		HashMap<String, String> namevalues = new HashMap<String, String>();
		namevalues.put(choose[0], choose[1]);
		String chothers=choose.length==2? null: choose[2];
		if (StringUtils.isBlank(chothers)==false) {
			HashMap<String, String> others = new Gson().fromJson(chothers, new TypeToken<HashMap<String, String>>(){}.getType());
			namevalues.putAll(others);
		}
		for (Map.Entry<String, String> entry: namevalues.entrySet()) {
			String chnames=entry.getKey();
			String chvalues=entry.getValue();
			if (StringUtils.isEmpty(chnames))
				continue;
			String[] names=chnames.split(PropertyChoosable.SplitRegex), values=chvalues.split(PropertyChoosable.SplitRegex);
			BeanClass beanClass = BeanClass.getBeanClass(targetDomain.getClass());
			for (int iname=names.length,ivalue=values.length,i=0; i<iname && i<ivalue; i++) {
				String fieldName=names[i], sValue=values[i];
				Accessor accessor = beanClass.getFieldAccessor(fieldName);
				if (crossFields.contains(fieldName) || accessor==null)			continue;
				Object fieldValue = TypeFactory.createType(accessor.getType()).parse(sValue);
				accessor.set(targetDomain, fieldValue);
			}
		}
	}
	public void fromTrunk(ViewBuilder vbuilder, PropertyChoosable target, PropertyChoosable from) {
		for (Iterator<FieldBuilder> iter=vbuilder.getFieldBuilderLeafs().iterator(); iter.hasNext();) {
			FieldBuilder fb = iter.next();
			Object tvalue=fb.getProperty().getDynaProperty().get(target), fvalue=fb.getProperty().getDynaProperty().get(from);
			fb.getProperty().getDynaProperty().set(target, fvalue);
		}
	}
	private String[] toTrunk(ViewBuilder viewBuilder, List<String> crossFields, PropertyChoosable domain, TAlongable talong) {
		StringBuffer sname=new StringBuffer(), svalue=new StringBuffer();
		TextBuilder textBuilder = new TextBuilder();
		SellerViewSetting vsetting = this.getChooseSetting(viewBuilder);
		String[] namevalues = new String[3];
		"1".toCharArray();
		if ((talong instanceof CommodityT) && viewBuilder.getName().equals("Ticket"))
			"12S".toCharArray();
		for (Iterator<FieldBuilder> iter=vsetting.getTitleList(viewBuilder.getFieldBuilderLeafs()).iterator(); iter.hasNext();) {
			FieldBuilder f = iter.next();
			Object fvalue = f.getProperty().getDynaProperty().get(domain);
			String ftext = textBuilder.getFormatedText(fvalue);
			if (StringUtils.isEmpty(ftext)) {
			} else if (crossFields.contains(f.getName())) {
			} else {
				sname.append(f.getName()).append(PropertyChoosable.SplitAppend);
				svalue.append(ftext).append(PropertyChoosable.SplitAppend);
			}
			if (!iter.hasNext() && svalue.length()>0) {
				namevalues[0] = sname.deleteCharAt(sname.length()-1).toString();
				namevalues[1] = svalue.deleteCharAt(svalue.length()-1).toString();
			}
		}
		sname=new StringBuffer();
		svalue=new StringBuffer();
		for (Iterator<FieldBuilder> iter=vsetting.getTrunkList(viewBuilder.getFieldBuilderLeafs()).iterator(); iter.hasNext();) {
			FieldBuilder f = iter.next();
			Object fvalue = f.getProperty().getDynaProperty().get(domain);
			String ftext = textBuilder.getFormatedText(fvalue);
			if (StringUtils.isEmpty(ftext)) {
			} else if (vsetting.getTitleList().contains(f.getName()) || crossFields.contains(f.getName())) {
			} else {
				sname.append(f.getName()).append(PropertyChoosable.SplitAppend);
				svalue.append(ftext).append(PropertyChoosable.SplitAppend);
			}
			if (!iter.hasNext() && svalue.length()>0) {
				String names = sname.deleteCharAt(sname.length()-1).toString();
				String values = svalue.deleteCharAt(svalue.length()-1).toString();
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(names, values);
				String json = new Gson().toJson(map, new TypeToken<HashMap<String, String>>(){}.getType());
				namevalues[2] = json;
			}
		}
		return namevalues;
	}
	private String[] toTrunk4Handle(ViewBuilder viewBuilder, PropertyChoosable domain) {
		StringBuffer sname=new StringBuffer(), svalue=new StringBuffer();
		TextBuilder textBuilder = new TextBuilder();
		SellerViewSetting vsetting = this.getChooseSetting(viewBuilder);
		String[] namevalues = new String[3];
		if ((talong instanceof OrderT) && viewBuilder.getName().equals("Ticket"))
			"1".toCharArray();
		for (Iterator<FieldBuilder> iter=viewBuilder.getFieldBuilderLeafs().iterator(); iter.hasNext();) {
			FieldBuilder f = iter.next();
			Object fvalue = f.getProperty().getDynaProperty().get(domain);
			String ftext = textBuilder.getFormatedText(fvalue);
			if (StringUtils.isEmpty(ftext)) {
			} else {
				sname.append(f.getName()).append(PropertyChoosable.SplitAppend);
				svalue.append(ftext).append(PropertyChoosable.SplitAppend);
			}
			if (!iter.hasNext() && svalue.length()>0) {
				namevalues[0] = sname.deleteCharAt(sname.length()-1).toString();
				namevalues[1] = svalue.deleteCharAt(svalue.length()-1).toString();
			}
		}
		return namevalues;
	}
	
	private ViewBuilder getStandardClone(SellerViewSetting setting, ViewBuilder standard0) {
		ViewBuilder standardBuilder = (ViewBuilder)standard0.createClone();
		if ("重命名".length()>0) {
			for (Map.Entry<String, String> entry: setting.getRenameMap().entrySet()) {
				FieldBuilder builder=standardBuilder.getFieldBuilder(entry.getKey());
				if (builder==null)
					continue;
				builder.setLabel(entry.getValue());
			}
		}
		if ("输入框".length()>0) {
			for (Map.Entry<String, String> entry: setting.getInputMap().entrySet()) {
				Class clss = standardBuilder.getClassFinder().find(entry.getValue());
				FieldBuilder builder0 = standardBuilder.getFieldBuilder(entry.getKey());
				ViewBuilder vprt = builder0.getViewBuilder();
				FieldBuilder builder1 = (FieldBuilder)TicketPropertyUtil.copyFieldsSkip(builder0, ReflectHelper.invokeConstructor(clss, new Object[0]));
				vprt.getFieldBuilders().set(vprt.getFieldBuilders().indexOf(builder0), builder1);
				if (builder1.getClass() == TextAreaBuilder.class) {
					builder1.setParameters(builder1.cloneParameters());
					builder1.setAttribute("row", ParameterName.Cfg, ParameterName.Position);
				}
			}
		}
		if ("选择项".length()>0) {
			for (Map.Entry<String, List<String>> entry: setting.getSelectMap().entrySet()) {
				FieldBuilder builder=standardBuilder.getFieldBuilder(entry.getKey());
				builder.setParameters(builder.cloneParameters());
				builder.setAttribute(entry.getValue(), ParameterName.Options, ParameterName.Options);
			}
		}
		return standardBuilder;
	}
}
