package com.haoyong.sales.common.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import net.sf.mily.bean.BeanClass;
import net.sf.mily.bean.BeanClass.FieldAccessor;
import net.sf.mily.bus.service.Action;
import net.sf.mily.common.Notely;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.Type;
import net.sf.mily.types.TypeFactory;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.query.Identity;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.haoyong.sales.common.domain.PropertyChoosable;
import com.haoyong.sales.common.form.ActionEnum;

/**
 * (新增，删除，更新差异控制台输出记录)
 */
public class DomainChangeTracing {
	
	private static Logger log = Logger.getLogger(DomainChangeTracing.class.getSimpleName());
	private static List<Class> excludeClasses = Arrays.asList(new Class[]{});
	private static List<String> excludeProps = Arrays.asList(new String[]{"version","stateNotes","serialVersionUID","modifytime",
			"voAmount","voParamMap",
		});
	private List<DomainItem> domainList=new ArrayList<DomainItem>(), domainHist=new ArrayList<DomainItem>();
	
	private List<Action> actionList = new ArrayList<Action>();
	
	public void close(){
		domainHist.clear();
	}
	
	public void rollback() {
		for (DomainItem d: domainHist) {
			d.rollback();
		}
	}
	
	public StringBuffer print2clear(String sitem, String smain) {
		StringBuffer output = new StringBuffer();
		output.append("\n").append(sitem).append(actionList).append("，");
		boolean printed = false;
		for(DomainItem item: this.domainList){
			boolean p = item.print(output);
			if (p && item.to instanceof Notely && sitem.length()+smain.length()>0) {
				Notely nitem = (Notely)item.to;
				nitem.getStateBuffer().append(nitem.getStateBuffer().length()>0? "\n": "").append(smain).append(smain.length()>0 && sitem.length()>0? ",": "").append(sitem);
			}
			printed = p || printed;
		}
		this.domainHist.addAll(this.domainList);
		this.domainList.clear();
		this.actionList.clear();
		return output;
	}
	
	public static StringBuffer printDomains(String prefix, Object... domainList) {
		DomainItem ditem = new DomainItem();
		boolean blank = StringUtils.isBlank(prefix);
		StringBuffer sb = new StringBuffer(blank? "": prefix);
		for (Iterator<Object> iter=Arrays.asList(domainList).iterator(); iter.hasNext();) {
			Object d = iter.next();
			sb.append(ditem.handleDomain(d));
			if (iter.hasNext())			sb.append("\n");
		}
		return sb;
	}
	
	/**
	 * 新增
	 */
	@SuppressWarnings("rawtypes")
	public void insert(Identity domain){
		if(isTracing(domain)==false){
			return;
		}
		DomainItem item=DomainItem.newInsert(domain);
		addDomainItem(item, -1);
	}
	
	/**
	 * 删除
	 */
	@SuppressWarnings("rawtypes")
	public void delete(Identity toDomain){
		if(isTracing(toDomain)==false){
			return;
		}
		int idxItem=this.indexOf(toDomain, domainList);
		Identity fromDomain = null;
		if (idxItem > -1) {
			DomainItem preDomainItem=this.domainList.get(idxItem);
			fromDomain = preDomainItem.from;
		} else {
			fromDomain = (Identity)ReflectHelper.getPropertyValue(toDomain, "SnapShot");
			fromDomain.setId(toDomain.getId());
		}
		DomainItem item=DomainItem.newDelete(fromDomain,toDomain);
		addDomainItem(item, idxItem);
	}
	
	/**
	 * 更新
	 */
	public void update(Identity domain){
		if(isTracing(domain)==false){
			return;
		}
		int idxItem=this.indexOf(domain, domainList);
		Identity fromDomain = null;
		if (idxItem > -1) {
			DomainItem preDomainItem=this.domainList.get(idxItem);
			fromDomain = preDomainItem.from;
		} else {
			fromDomain = (Identity)ReflectHelper.getPropertyValue(domain, "SnapShot");
			fromDomain.setId(domain.getId());
		}
		DomainItem item=DomainItem.newUpdate(fromDomain,domain);
		addDomainItem(item, idxItem);
	}
	
	public static Logger getLog() {
		return log;
	}
	
	public List<Action> getActonList() {
		return this.actionList;
	}
	
	private void addDomainItem(DomainItem domainItem, int idx) {
		for (Iterator<DomainItem> iter=domainList.iterator(); iter.hasNext();) {
			DomainItem item = iter.next();
			if (item.getChangedDomain().equals(domainItem.getChangedDomain())) {
				iter.remove();
				domainItem.printedInfo = item.printedInfo;
				break;
			}
		}
		domainItem.setPrtDomainItemList(this.domainList);
		if (idx==0 && this.domainList.size()==0)		idx=-1;
		if (idx==-1) {
			this.domainList.add(domainItem);
		} else {
			this.domainList.add(idx, domainItem);
		}
	}
	
	private int indexOf(Identity domain, List<DomainItem> domainItemList) {
		for (DomainItem item: domainItemList) {
			Identity fdomain = item.getChangedDomain();
			if (domain.equals(fdomain))		return domainItemList.indexOf(item);
		}
		return -1;
	}
	
	/**过滤*/
	private boolean isTracing(Identity domain){
		if (1==0) {// true不跟踪
			return false;
		} else if(excludeClasses.contains(domain.getClass())){
			return false;
		} else {
			for (Action a: actionList) {
				ActionEnum action = (ActionEnum)a;
				if (action.name().startsWith("FlowtaskingTicket")) {
					return false;
				}
			}
			return true;
		}
	}
	
	public static class DomainItem{
		
		private Identity from;
		private Identity to;
		private List<DomainItem> prtDomainItemList;
		private String printedInfo;
		
		//0新增，1删除，2修改
		private int type;
		private static DomainItem newInsert(Identity domain){
			DomainItem item = new DomainItem();
			item.to=domain;
			item.type=0;
			return item;
		}
		private static DomainItem newDelete(Identity fromDomain, Identity toDomain){
			DomainItem item = new DomainItem();
			item.from=fromDomain;
			item.to=toDomain;
			item.type=1;
			return item;
		}
		private static DomainItem newUpdate(Identity fromDomain, Identity toDomain){
			if (fromDomain==null)				return newInsert(toDomain);
			DomainItem item = new DomainItem();
			item.from=fromDomain;
			item.to=toDomain;
			item.type=2;
			return item;
		}
		
		/**打印记录*/
		private boolean print(StringBuffer outputAll){
			StringBuffer output = new StringBuffer();
			if(this.type==0){//新增
				this.itemPrint(to, output);
			}else if(this.type==1){//删除
				if(from==null){
					from = to;
				}
				this.itemPrint(from,to, output);
			}else if(this.type==2){//修改
				this.itemPrint(from,to, output);
			}
			boolean ok = !StringUtils.equals(this.printedInfo, output.toString());
			if (ok)			outputAll.append(output);
			this.printedInfo = output.toString();
			return ok;
		}
		
		private void rollback() {
			if (this.type==0) {
				this.to.setId(0);
			} else if (this.type==1) {
				TicketPropertyUtil.copyFieldsUnskip(this.from, this.to);
			}
		}
		
		private String getFormatValue(Object fvalue) {
			if (fvalue==null)
				return "";
			Type type=TypeFactory.createType(fvalue.getClass());
			String value=type.format(fvalue);
			if (value==null)
				return "";
			int iClassName = value.lastIndexOf(fvalue.getClass().getSimpleName());
			if (iClassName > -1) {
				value = value.substring(iClassName+1);
			}
			if(value.startsWith("com.haoyong.sales")){
				int iFirst=value.indexOf("."), iLast=value.lastIndexOf(".");
				if (iLast>iFirst && iFirst>-1) {
					value = value.substring(iLast+1);
				}
			}
			return value;
		}
		
		public int getType() {
			return this.type;
		}
		
		public Identity getFromDomain() {
			return this.from;
		}
		
		public Identity getToDomain() {
			return this.to;
		}
		
		public int getFirstIndex() {
			Class clazz = getChangedDomain().getClass();
			for(int i=0; i<prtDomainItemList.size(); i++) {
				if (clazz == prtDomainItemList.get(i).getChangedDomain().getClass()) {
					return i;
				}
			}
			return prtDomainItemList.size();
		}
		
		public Identity getChangedDomain() {
			if(this.type==0){//新增
				return to;
			}else if(this.type==1){//删除
				return to;
			}else if(this.type==2){//修改
				return to;
			}
			return null;
		}
		
		public String toString() {
			return getChangedDomain().toString();
		}
		
		public List<DomainItem> getPrtDomainItemList() {
			return prtDomainItemList;
		}
		
		public void setPrtDomainItemList(List<DomainItem> prtDomainItemList) {
			this.prtDomainItemList = prtDomainItemList;
		}
		
		/**
		 * 把数据封装到Map中
		 * @param AbstractDerby domain
		 * @return Map<String,String>
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private void getMap(String entityName, Object domain, 
				List<LinkedHashMap<String, String>> enumMapList, 
				List<LinkedHashMap<String, String>> chineseMapList, 
				List<LinkedHashMap<String, String>> englishMapList, 
				List<LinkedHashMap<String, String>> numericMapList, 
				List<LinkedHashMap<String, String>> voMapList){
			LinkedHashMap<String, String> enumMap=new LinkedHashMap<String, String>(), numericMap=new LinkedHashMap<String, String>(), chineseMap=new LinkedHashMap<String, String>(), englishMap=new LinkedHashMap<String, String>(), voMap=new LinkedHashMap<String, String>();
			enumMapList.add(enumMap);
			chineseMapList.add(chineseMap);
			englishMapList.add(englishMap);
			numericMapList.add(numericMap);
			voMapList.add(voMap);
			//domain
			BeanClass beanClass=BeanClass.getBeanClass(domain.getClass());
			List<FieldAccessor> fieldList = new ArrayList((Collection)ReflectHelper.getPropertyValue(beanClass,"fieldProperties"));
			for(FieldAccessor field:fieldList){
				if (Collection.class.isAssignableFrom(field.getType())) {
					continue;
				}
				Object fvalue = null;
				try {
					fvalue = field.get(domain);
				} catch(Exception e) {
					//
				}
				String fname=(entityName.length()==0 && ((fvalue instanceof PropertyChoosable) || (fvalue instanceof Identity)))? fvalue.getClass().getSimpleName(): field.getName();
				String fentity=(entityName.length()>0? entityName.concat(".").concat(fname): fname);
				if (fvalue instanceof PropertyChoosable) {
					getMap(fentity, fvalue, enumMapList, chineseMapList, englishMapList, numericMapList, voMapList);
				} else if (fvalue instanceof Identity) {
					fvalue = ((Identity)fvalue).getId();
				} else if (excludeProps.contains(fname)) {
					String value=getFormatValue(fvalue);
					voMap.put(fentity, value);
				} else {
					String value=getFormatValue(fvalue);
					if (fvalue!=null && fvalue.getClass().isEnum()) {
						enumMap.put(fentity, value);
					} else if (value.matches("^[-\\+]?\\d+(\\.\\d+)?$") || ("".equals(value) && (""+fvalue).matches("^[-\\+]?\\d+(\\.\\d+)?$"))) {
						if (field.getType()==String.class)
							englishMap.put(fentity, value);
						else
							numericMap.put(fentity, value);
					} else if (value.matches("^\\w+([-+.]\\w+)*$")){
						englishMap.put(fentity, value);
					} else {
						chineseMap.put(fentity, value);
					}
				}
			}
		}
		
		/**
		 * 处理(新增|删除Map),输出拼接字符串
		 * @param AbstractDerby domain
		 */
		private void itemPrint(Identity domain, StringBuffer output){
			StringBuffer sbData = new StringBuffer();
			if(type==0){
				sbData.append("新增！");
			}
			sbData.append(handleDomain(domain));
			output.append("\n").append(sbData);
		}
		
		private StringBuffer handleDomain(Object odomain){
			Identity domain = (odomain instanceof Identity)? (Identity)odomain: null;
			StringBuffer sbData = new StringBuffer();
			sbData.append(odomain.getClass().getSimpleName());
			sbData.append("_").append(domain==null? "": domain.getId());
			List<LinkedHashMap<String, String>> enumMapList=new ArrayList<LinkedHashMap<String, String>>(), numericMapList=new ArrayList<LinkedHashMap<String, String>>(), chineseMapList=new ArrayList<LinkedHashMap<String, String>>(), englishMapList=new ArrayList<LinkedHashMap<String, String>>(), voMapList=new ArrayList<LinkedHashMap<String, String>>();
			this.getMap(new String(), odomain, enumMapList, chineseMapList, englishMapList, numericMapList, voMapList);
			for (Iterator<LinkedHashMap<String, String>> enumIter=enumMapList.iterator(), numericIter=numericMapList.iterator(), englishIter=englishMapList.iterator(), chineseIter=chineseMapList.iterator(); enumIter.hasNext(); ) {
				handleMap(enumIter.next(), sbData);
				handleMap(numericIter.next(), sbData);
				handleMap(englishIter.next(), sbData);
				handleMap(chineseIter.next(), sbData);
	//			handleMap(voMap, sbData);
			}
			return sbData;
		}
		
		private void handleMap(LinkedHashMap<String, String> map, StringBuffer output) {
			StringBuffer sbData=new StringBuffer();
			String key=null, value=null;
			for (Entry<String, String> entry: map.entrySet()) {
			    key = entry.getKey();
			    value = entry.getValue();
			    if(!StringUtils.isEmpty(value)){
			    	sbData.append("，").append(key).append("|").append(value);
			    }
			}
			output.append(sbData);
		}
		
		/**
		 * 处理(更新差异fromMap,toMap),输出拼接字符串
		 * @param AbstractDerby from
		 * @param AbstractDerby to
		 */
		private void itemPrint(Identity from,Identity to, StringBuffer output){
			StringBuffer sbData = new StringBuffer();
			boolean isChange=false;
			if(type==2){
				sbData.append("更新！");
			}else if(type==1){
				sbData.append("删除！");
				isChange = true;
			}
			sbData.append(from.getClass().getSimpleName());
			sbData.append("_").append(from.getId());
			List<LinkedHashMap<String, String>> enumMapList1=new ArrayList<LinkedHashMap<String, String>>(), numericMapList1=new ArrayList<LinkedHashMap<String, String>>(), chineseMapList1=new ArrayList<LinkedHashMap<String, String>>(), englishMapList1=new ArrayList<LinkedHashMap<String, String>>(), voMapList1=new ArrayList<LinkedHashMap<String, String>>();
			List<LinkedHashMap<String, String>> enumMapList2=new ArrayList<LinkedHashMap<String, String>>(), numericMapList2=new ArrayList<LinkedHashMap<String, String>>(), chineseMapList2=new ArrayList<LinkedHashMap<String, String>>(), englishMapList2=new ArrayList<LinkedHashMap<String, String>>(), voMapList2=new ArrayList<LinkedHashMap<String, String>>();
			getMap(new String(), from, enumMapList1, chineseMapList1, englishMapList1, numericMapList1, voMapList1);
			getMap(new String(), to, enumMapList2, chineseMapList2, englishMapList2, numericMapList2, voMapList2);
			StringBuffer sbChange=new StringBuffer();
			StringBuffer sbNoChange=new StringBuffer();
			for (Iterator<LinkedHashMap<String, String>>
					enumIter1=enumMapList1.iterator(), numericIter1=numericMapList1.iterator(), englishIter1=englishMapList1.iterator(), chineseIter1=chineseMapList1.iterator(),
					enumIter2=enumMapList2.iterator(), numericIter2=numericMapList2.iterator(), englishIter2=englishMapList2.iterator(), chineseIter2=chineseMapList2.iterator()
					; enumIter1.hasNext(); ) {
				isChange = handleMapDiff(sbChange, sbNoChange, enumIter1.next(), enumIter2.next()) || isChange;
				isChange = handleMapDiff(sbChange, sbNoChange, numericIter1.next(), numericIter2.next()) || isChange;
				isChange = handleMapDiff(sbChange, sbNoChange, englishIter1.next(), englishIter2.next()) || isChange;
				isChange = handleMapDiff(sbChange, sbNoChange, chineseIter1.next(), chineseIter2.next()) || isChange;
	//			handleMapDiff(sbNoChange, sbNoChange, voMapIter1.next, voMapIter2.next);
			}
			if(isChange==true){
				if(StringUtils.isNotEmpty(sbNoChange.toString())){
					sbData.append(sbNoChange);
					if (StringUtils.isNotEmpty(sbChange.toString()))	sbData.append("\n").append(sbChange);
				}else{
					sbData.append(sbChange);
				}
				output.append("\n").append(sbData);
			}
		}
		
		private boolean handleMapDiff(StringBuffer sbChange, StringBuffer sbNoChange, LinkedHashMap<String, String> fromMap, LinkedHashMap<String, String> toMapAll) {
			boolean isChange=false;
			String key=null,fromValue=null,toValue=null;
			for (Entry<String, String> fromEntry: fromMap.entrySet()) {
				key = fromEntry.getKey();
				fromValue = fromEntry.getValue();
				toValue = toMapAll.get(key);
				if((StringUtils.isNotEmpty(fromValue) && !fromValue.equals(toValue)) || (StringUtils.isNotEmpty(toValue) && !toValue.equals(fromValue))){
					isChange=true;
					sbChange.append("，").append(key).append("|").append(fromValue).append("(").append(toValue).append(")");
				} else if(!StringUtils.isEmpty(fromValue)){
					sbNoChange.append("，").append(key).append("|").append(fromValue);
				}
			}
			return isChange;
		}
	}
}
