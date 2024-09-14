package com.haoyong.salel.common.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import net.sf.mily.bean.DynaClass;
import net.sf.mily.mappings.MetaParameter;
import net.sf.mily.support.form.Formable;
import net.sf.mily.support.form.Formable.RevalidateVersionFormable;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.types.TypeFactory;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.ui.event.Validable;
import net.sf.mily.util.PerformTrace;
import net.sf.mily.util.PerformTrace.TraceCombine;
import net.sf.mily.util.PerformTrace.TraceItem;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.ListViewBuilder;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.View;
import net.sf.mily.webObject.event.ActionChain;
import net.sf.mily.webObject.event.FieldActionEvent;
import net.sf.mily.webObject.event.FieldActionListener;
import net.sf.mily.webObject.query.ColumnField;
import net.sf.mily.webObject.query.Fields;
import net.sf.mily.webObject.query.SubFields;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import com.haoyong.salel.common.dao.BaseDAO;
import com.haoyong.salel.common.domain.AbstractDomain;

/**
 * 在领域对象列表中选择domains
 *
 */
public class SelectDomainListener implements FieldActionListener, Validable {

	protected MetaParameter param;
	private List<Proxy4Formable> proxyList;

	public void actionPerformed(FieldActionEvent event, ActionChain chain) {
		Component fComp = event.getField().getComponent();
		EditView editView = event.getEvent().getSource().searchFormerByClass(EditView.class);
		Formable buttonForm = (Formable)editView.getValue();
		Formable listviewFormable = getFormable(fComp);
		ListView listview = getListView(event);
		this.proxyList = getProxy4Formable(buttonForm, listviewFormable, listview);
		for (Proxy4Formable proxy: proxyList) {
			proxy.dotask();
		}
	}
	
	private interface Proxy4Formable {
		public void dotask();
	}
	
	public class Proxy4RevalidateVersionFormable implements Proxy4Formable {
		private SqlListBuilder sqlBuilder;
		private RevalidateVersionFormable formable;
		private ListView listview;
		
		public void dotask() {
			this.validate();
			this.getSelectedPage();
			if (getParameter().getBoolean(ParameterName.Validate, true) && listview.getSelectedPage().size()==0)
				throw new LogicException(2, new StringBuffer("请选择列表明细").toString());
			formable.revalidate();
		}
		
		public void validate() {
			String properties = getParameter().getParameter(ParameterName.Validate).getString(ParameterName.Properties);
			String sMethod = getParameter().getParameter(ParameterName.Validate).getString(ParameterName.Method);
			if (properties==null)		return;
			SqlListBuilder sqlBuilder = (SqlListBuilder)listview.getViewBuilder();
			List<String> fieldnameList = Arrays.asList(properties.split("\\,"));
			List<List<Object>> valiRows = getSelectedListByFieldNames(sqlBuilder, getFieldList(sqlBuilder), listview.getSelectedPage(), fieldnameList);
			Object bean = formable;
			int imethod = sMethod.lastIndexOf(".");
			if (imethod > -1) {
				String s = sMethod.substring(0, imethod);
				sMethod = sMethod.substring(imethod+1);
				bean = ReflectHelper.getPropertyValue(formable, s);
			}
			ReflectHelper.invokeMethod(bean, sMethod, new Class[]{List.class}, new Object[]{valiRows});
		}
		
		private void getSelectedPage() {
			formable.setSqlBuilder(sqlBuilder);
			List<List<Object>> selectedList = getSelectedListByFieldNames(sqlBuilder, getFieldList(sqlBuilder), listview.getSelectedPage(), formable.getVersionFieldNames());
			formable.setSelectVersionMap(selectedList);
		}
		
		private List<String> getVersionFieldNames() {
			List<String> list = new ArrayList<String>();
			if (getParameter().getParameterIterator(ParameterName.ID).hasNext()==false) {
				list.add("id");
				list.add("version");
			} else {
				StringBuffer sb = new StringBuffer();
				String sid = getParameter().getParameter(ParameterName.ID).getString(ParameterName.ID);
				String sversion = getParameter().getParameter(ParameterName.ID).getString(ParameterName.Version);
				String sclass = getParameter().getParameter(ParameterName.ID).getString(ParameterName.CLASS);
				if (sid == null)
					sb.append("加载对象id未配置！");
				if (sversion == null)
					sb.append("加载对象version未配置！");
				if (sclass == null)
					sb.append("加载对象class未配置！");
				list.add(sid);
				list.add(sversion);
				if (sb.length()>0)
					throw new LogicException(2, sb.toString());
			}
			return list;
		}
	}
	
	public class Proxy4SameSelectFormable implements Proxy4Formable {
		private SqlListBuilder sqlBuilder;
		private RevalidateVersionFormable formable;
		private ListView listview;
		
		public void dotask() {
			isValid();
			List<Proxy4RevalidateVersionFormable> proxyList = getProxy4Formable(Proxy4RevalidateVersionFormable.class);
			if (proxyList.size() > 1)
				proxyList.get(0).validate();
			setAppendFilters();
			if (listview.getSelectedPage().size()>0 && "all".equals(this.getParameter().getString(ParameterName.Select, "all")))
				setSelectedPage();
		}
		
		private List<String> getSameFilters() {
			List<String> nameList = new ArrayList<String>();
			for (String name: this.getParameter().getString(ParameterName.Properties).split(",")) {
				nameList.add(name);
			}
			return nameList;
		}
		
		private void setAppendFilters() {
			List<List<Object>> selectedPage = getSelectedPage();
			List<String> nameList = getSameFilters();
			for (Iterator<List<Object>> rowIter=selectedPage.iterator(); rowIter.hasNext();) {
				List<Object> row = rowIter.next();
				Map<String, Object> filters = new HashMap<String, Object>();
				for (Iterator fiter=getFieldList(sqlBuilder).iterator(),viter=row.iterator(); fiter.hasNext();) {
					ColumnField f = (ColumnField)fiter.next();
					Object fv = viter.next();
					String fname = f.getId();
					if (nameList.contains(fname)) {
						filters.put(fname, fv==null? " is null ": "=".concat(TypeFactory.createType(fv.getClass()).sqlFormat(fv)));
					}
				}
				this.formable.setAppendFilters(filters);
				break;
			}
		}
		
		public void setSelectedPage() {
			sqlBuilder.getSqlQuery().getFields().setAppends(this.formable.getAppendFilters());
			List<List<Object>> rows = sqlBuilder.getSqlQuery().loadResultAll(sqlBuilder.getSqlQuery().getFields().sqlSelectSource());
			sqlBuilder.getSqlQuery().getFields().getAppends().clear();
			listview.setSelectedPage(rows);
		}
		
		private List<List<Object>> getSelectedPage() {
			List<List<Object>> selectedPage = new ArrayList<List<Object>>(listview.getSelectedPage());
			return selectedPage;
		}
		
		private boolean isValid() {
			List<String> fieldnameList = getSameFilters();
			List<List<Object>> selectFields = getSelectedListByFieldNames(sqlBuilder, getFieldList(sqlBuilder), listview.getSelectedPage(), fieldnameList);
			Iterator<List<Object>> iter=selectFields.iterator();
			for (List<Object> first=(iter.hasNext()? iter.next(): null), row=null; iter.hasNext();) {
				row = iter.next();
				EqualsBuilder builder = new EqualsBuilder();
				for (int i=fieldnameList.size(); i-->0; ) {
					builder.append(first.get(i), row.get(i));
				}
				if (builder.isEquals()==false) {
					throw new LogicException(2, new StringBuffer("请选择同一").append(first).append("的记录").toString());
				}
			}
			return true;
		}
		
		public MetaParameter getParameter() {
			return SelectDomainListener.this.getParameter().getParameter(ParameterName.Same);
		}
		
		public ListView getListView() {
			return this.listview;
		}
		
		public SqlListBuilder getSqlBuilder() {
			return this.sqlBuilder;
		}
	}
	
	private class Proxy4ParamDomainFormable implements Proxy4Formable {
		private SqlListBuilder sqlBuilder;
		private ListView listview;
		private MetaParameter parameter;
		
		private HashMap<Integer, AbstractDomain> selectedParams;
		
		public void dotask() {
			getSelectedList();
		}
		
		private void getSelectedList() {
			List<List<Object>> selectedPage = getSelectedPage();
			Class clss = sqlBuilder.getViewBuilder().getPresentClass().getClassFinder().find(this.parameter.getString(ParameterName.CLASS));
			this.selectedParams = toDomains(sqlBuilder, getFieldList(sqlBuilder), selectedPage, getParamFieldNames(), clss);
		}
		
		private void setSelectedList(LinkedHashMap<Integer, AbstractDomain> domainList) {
			for (Integer irow: domainList.keySet()) {
				AbstractDomain domain = domainList.get(irow);
				AbstractDomain p = selectedParams.get(irow);
				if (p!=null) {
					domain.setVoparam(p);
					domain.getSnapShot().setVoparam(p.getSnapShot());
				}
			}
		}
		
		private List<List<Object>> getSelectedPage() {
			if ("all".equals(param.getString(ParameterName.Select))) {
				List<List<Object>> selectedAll = sqlBuilder.getSqlQuery().loadResultAll(getSqlSelectSource(sqlBuilder));
				return selectedAll;
			}
			List<List<Object>> selectedPage = new ArrayList<List<Object>>(listview.getSelectedPage());
			return selectedPage;
		}
		
		private List<String> getParamFieldNames() {
			String sid=this.parameter.getString(ParameterName.ID), sversion=this.parameter.getString(ParameterName.Version);
			String sclass = this.parameter.getString(ParameterName.CLASS);
			StringBuffer sb = new StringBuffer();
			if (sid == null)
				sb.append("参数对象id未配置！");
			if (sversion == null)
				sb.append("参数对象version未配置！");
			if (sclass == null)
				sb.append("参数对象class未配置！");
			List<String> nameList = new ArrayList<String>();
			nameList.add(sid);
			nameList.add(sversion);
			if (sb.length() > 0)
				throw new LogicException(2, sb.toString());
			return nameList;
		}
	}

	private class Proxy4ParamPropertyFormable implements Proxy4Formable {
		private SqlListBuilder sqlBuilder;
		private ListView listview;
		private MetaParameter parameter;
		
		private HashMap<Integer, LinkedHashMap<String, Object>> selectedParams;
		
		public void dotask() {
			getSelectedList();
		}
		
		private void getSelectedList() {
			List<List<Object>> selectedPage = getSelectedPage();
			LinkedHashMap<Integer, LinkedHashMap<String, Object>> map = new LinkedHashMap<Integer, LinkedHashMap<String, Object>>();
			List<String> nameList = this.getParamFieldNames();
			List<List<Object>> rows = getSelectedListByFieldNames(sqlBuilder, getFieldList(sqlBuilder), selectedPage, nameList);
			List<ColumnField> columnList = getColumnListByFieldNames(getFieldList(sqlBuilder), nameList);
			for (int i=0,size=rows.size(); i<size; i++) {
				LinkedHashMap<String, Object> rowMap = new LinkedHashMap<String, Object>();
				for (Iterator nameIter=nameList.iterator(), colIter=columnList.iterator(), valueIter=rows.get(i).iterator(); nameIter.hasNext();) {
					StringBuffer sb = new StringBuffer().append(nameIter.next()).append(",").append(((ColumnField)colIter.next()).getLabel());
					rowMap.put(sb.toString(), valueIter.next());
				}
				map.put(i, rowMap);
			}
			this.selectedParams = map;
		}
		
		private void setSelectedList(LinkedHashMap<Integer, AbstractDomain> domainList) {
			for (Integer irow: domainList.keySet()) {
				AbstractDomain domain = domainList.get(irow);
				LinkedHashMap<String, Object> rowProps = selectedParams.get(irow);
				if (rowProps!=null) {
					domain.getVoParamMap().put("PropertyList", rowProps);
					domain.getSnapShot().getVoParamMap().put("PropertyList", rowProps);
				}
			}
		}
		
		private List<List<Object>> getSelectedPage() {
			List<List<Object>> selectedPage = new ArrayList<List<Object>>(listview.getSelectedPage());
			return selectedPage;
		}
		
		private List<String> getParamFieldNames() {
			String pnames=this.parameter.getString(ParameterName.Properties), pnameList[]=pnames.split("\\,");
			StringBuffer sb = new StringBuffer();
			if (StringUtils.isBlank(pnames))
				sb.append("参数对象properties未配置！");
			if (sb.length() > 0)
				throw new LogicException(2, sb.toString());
			return new ArrayList<String>(Arrays.asList(pnameList));
		}
	}

	private class Proxy4SubFieldsFormable implements Proxy4Formable {
		private SqlListBuilder sqlBuilder;
		private ListView listview;
		private SubFields subfields;
		private MetaParameter parameter;
		
		private Map<Integer, List<AbstractDomain>> selectedChildren;
		
		public void dotask() {
			List<List<Object>> selectedChd = getSelectedPage();
			this.selectedChildren = getSelectedChildren(selectedChd);
		}
		
		private void setSelectedList(LinkedHashMap<Integer, AbstractDomain> domainList) {
			for (Integer irow: domainList.keySet()) {
				AbstractDomain domain = domainList.get(irow);
				List<AbstractDomain> pList = selectedChildren.get(irow);
				domain.setVoparam(pList);
			}
		}
		
		private List<List<Object>> getSelectedPage() {
			List<List<Object>> selectedPrt=listview.getSelectedPage(), selectedChd=new ArrayList<List<Object>>(0);
			if (selectedPrt.size()>0) {
				sqlBuilder.getSqlQuery().getFields().getAppends().put(getPrimary(sqlBuilder), getSqlSelectedIdInStr(selectedPrt));
				selectedChd = sqlBuilder.getSqlQuery().loadResultAll(subfields.sqlSelectSource());
				sqlBuilder.getSqlQuery().getFields().getAppends().clear();
			}
			return selectedChd;
		}
		
		private Map<Integer, List<AbstractDomain>> getSelectedChildren(List<List<Object>> selectedChd) {
			List<List<Object>> selectedPrt = listview.getSelectedPage();
			List<List<Object>> selectedRows = getSelectedListByFieldNames((SqlListBuilder)listview.getViewBuilder(), subfields.getFieldList(), selectedChd, getSubFieldsNames());
			Class clss = sqlBuilder.getViewBuilder().getPresentClass().getClassFinder().find(this.parameter.getString(ParameterName.CLASS));
			Map<Integer, AbstractDomain> domainList = toDomains(sqlBuilder, subfields.getFieldList(), selectedChd, getSubFieldsNames(), clss);
			Map<Integer, List<AbstractDomain>> selectedMap = new LinkedHashMap<Integer, List<AbstractDomain>>();
			for (int prti=0, prtSize=selectedPrt.size(), chdi=0; prti<prtSize; prti++) {
				Object prtId = selectedPrt.get(prti).get(0);
				List<AbstractDomain> children = new ArrayList<AbstractDomain>();
				for (int chdSize=selectedRows.size(); chdi<chdSize && selectedRows.get(chdi).get(2).equals(prtId); chdi++) {
					AbstractDomain d = domainList.get(chdi);
					children.add(d);
				}
				selectedMap.put(prti, children);
			}
			return selectedMap;
		}
		
		private List<String> getSubFieldsNames() {
			String sid=this.parameter.getString(ParameterName.ID), sversion=this.parameter.getString(ParameterName.Version);
			StringBuffer sb = new StringBuffer();
			if (sid == null)
				sb.append("参数对象id未配置！");
			if (sversion == null)
				sb.append("参数对象version未配置！");
			if (this.parameter.getString(ParameterName.CLASS) == null)
				sb.append("参数对象class未配置！");
			List<String> nameList = new ArrayList<String>();
			nameList.add(sid);
			nameList.add(sversion);
			nameList.add(getPrimary(sqlBuilder).getName());
			if (sb.length() > 0)
				throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
			return nameList;
		}
	}

	public class Proxy4SqlListBuilder implements Proxy4Formable {
		private SqlListBuilder sqlBuilder;
		private Formable formable;
		private Formable buttonForm;
		private ListView listview;
		
		public void dotask() {
			List<List<Object>> selectedPage = getSelectedPage();
			List<String> nameList = null;
			if (getProxy4Formable(Proxy4RevalidateVersionFormable.class).size() > 0) {
				nameList = getProxy4Formable(Proxy4RevalidateVersionFormable.class).get(0).getVersionFieldNames();
			} else {
				nameList = new ArrayList<String>();
				nameList.add("id");
				nameList.add("version");
			}
			LinkedHashMap<Integer, AbstractDomain> domainList = toDomains(sqlBuilder, getFieldList(sqlBuilder), selectedPage, nameList, getDomainClass(sqlBuilder));
			List<AbstractDomain> domainselectedList = new ArrayList<AbstractDomain>(domainList.values());
			for (Proxy4ParamDomainFormable proxy: getProxy4Formable(Proxy4ParamDomainFormable.class)) {
				proxy.setSelectedList(domainList);
			}
			for (Proxy4ParamPropertyFormable proxy: getProxy4Formable(Proxy4ParamPropertyFormable.class)) {
				proxy.setSelectedList(domainList);
			}
			for (Proxy4SubFieldsFormable proxy: getProxy4Formable(Proxy4SubFieldsFormable.class)) {
				proxy.setSelectedList(domainList);
			}
			for (Proxy4RevalidateVersionFormable proxy: getProxy4Formable(Proxy4RevalidateVersionFormable.class)) {
				proxy.getSelectedPage();
			}
			if (StringUtils.isNotEmpty(param.getString(ParameterName.Setter))) {
				String sSetter = param.getString(ParameterName.Setter);
				int isetter=sSetter==null?-1:sSetter.lastIndexOf(".");
				Object bean = buttonForm;
				if (isetter > -1) {
					String s = sSetter.substring(0, isetter);
					sSetter = sSetter.substring(isetter+1);
					bean = ReflectHelper.getPropertyValue(buttonForm, s);
				}
				ReflectHelper.invokeMethod(bean, sSetter, new Class[]{List.class}, new Object[]{domainselectedList});
			} else {
				formable.setSelectedList(domainselectedList);
			}
		}
		
		private List<List<Object>> getSelectedPage() {
			if ("all".equals(param.getString(ParameterName.Select))) {
				List<List<Object>> selectedAll = sqlBuilder.getSqlQuery().loadResultAll(getSqlSelectSource(sqlBuilder));
				listview.setSelectedPage(selectedAll);
				return selectedAll;
			} else if ("page".equals(param.getString(ParameterName.Select))) {
				List<List<Object>> selectedPage = (List<List<Object>>)listview.getValue();
				listview.setSelectedPage(selectedPage);
				return selectedPage;
			}
			List<List<Object>> selectedPage = new ArrayList<List<Object>>(listview.getSelectedPage());
			return selectedPage;
		}
	}
	
	private class Proxy4EditListBuilder implements Proxy4Formable {
		private ListViewBuilder checkBuilder;
		private Formable formable;
		private ListView listview;
		
		private List<AbstractDomain> selectedEntitys;

		public void dotask() {
			getSelectedList();
			setSelectedList();
		}
		
		private void getSelectedList() {
			if ("all".equals(param.getString(ParameterName.Select))) {
				this.selectedEntitys = new ArrayList((List)listview.getValue());
			} else {
				this.selectedEntitys = new ArrayList(listview.getSelectedPage());
			}
		}
		
		private void setSelectedList() {
			if (StringUtils.isNotEmpty(param.getString(ParameterName.Setter))) {
				String sSetter = param.getString(ParameterName.Setter);
				int isetter=sSetter==null?-1:sSetter.lastIndexOf(".");
				Object bean = formable;
				if (isetter > -1) {
					String s = sSetter.substring(0, isetter);
					sSetter = sSetter.substring(isetter+1);
					bean = ReflectHelper.getPropertyValue(formable, s);
				}
				ReflectHelper.invokeMethod(bean, sSetter, new Class[]{List.class}, new Object[]{selectedEntitys});
			} else {
				formable.setSelectedList(selectedEntitys);
			}
		}
	}
	
	private List<Proxy4Formable> getProxy4Formable(Formable buttonForm, Formable listviewFormable, ListView listview) {
		List<Proxy4Formable> proxyList = new ArrayList<SelectDomainListener.Proxy4Formable>();
		ListViewBuilder listBuilder = listview.getListBuilder();
		if (listBuilder instanceof SqlListBuilder && this.getParameter().getParameterIterator(ParameterName.Same).hasNext()) {
			Proxy4SameSelectFormable proxy = new Proxy4SameSelectFormable();
			proxy.formable = (RevalidateVersionFormable)listviewFormable;
			proxy.listview = listview;
			proxy.sqlBuilder = (SqlListBuilder)listBuilder;
			proxyList.add(proxy);
		}
		if (listviewFormable instanceof RevalidateVersionFormable) {
			Proxy4RevalidateVersionFormable proxy = new Proxy4RevalidateVersionFormable();
			proxy.formable = (RevalidateVersionFormable)listviewFormable;
			proxy.listview = listview;
			proxy.sqlBuilder = (SqlListBuilder)listBuilder;
			proxy.formable.setVersionFieldNames(proxy.getVersionFieldNames());
			proxyList.add(proxy);
		}
		if (listBuilder instanceof SqlListBuilder) {
			for (MetaParameter parameter: getParameter().getParameterList(ParameterName.Parameter)) {
				Proxy4ParamDomainFormable proxy = new Proxy4ParamDomainFormable();
				proxy.listview = listview;
				proxy.sqlBuilder = (SqlListBuilder)listBuilder;
				proxy.parameter = parameter;
				proxyList.add(proxy);
			}
		}
		if (listBuilder instanceof SqlListBuilder) {
			for (Iterator<MetaParameter> iter=getParameter().getParameterList(ParameterName.Property).iterator(); iter.hasNext();) {
				MetaParameter parameter = iter.next();
				Proxy4ParamPropertyFormable proxy = new Proxy4ParamPropertyFormable();
				proxy.listview = listview;
				proxy.sqlBuilder = (SqlListBuilder)listBuilder;
				proxy.parameter = parameter;
				proxyList.add(proxy);
				if (iter.hasNext())
					throw new LogicException(2, "不能配置多个Property");
			}
		}
		if (listBuilder instanceof SqlListBuilder) {
			for (MetaParameter parameter: getParameter().getParameterList(ParameterName.Collection)) {
				Proxy4SubFieldsFormable proxy = new Proxy4SubFieldsFormable();
				proxy.listview = listview;
				proxy.sqlBuilder = (SqlListBuilder)listBuilder;
				proxy.parameter = parameter;
				proxy.subfields = proxy.sqlBuilder.getSqlQuery().getFields().getSubFields();
				proxyList.add(proxy);
			}
		}
		if (listBuilder instanceof SqlListBuilder) {
			Proxy4SqlListBuilder proxy = new Proxy4SqlListBuilder();
			proxy.buttonForm = buttonForm;
			proxy.formable = listviewFormable;
			proxy.listview = listview;
			proxy.sqlBuilder = (SqlListBuilder)listBuilder;
			proxyList.add(proxy);
		}
		if (proxyList.size()==0 && (listBuilder instanceof ListViewBuilder)) {
			Proxy4EditListBuilder proxy = new Proxy4EditListBuilder();
			proxy.formable = listviewFormable;
			proxy.listview = listview;
			proxy.checkBuilder = (ListViewBuilder)listBuilder;
			proxyList.add(proxy);
		}
		return proxyList;
	}
	
	private <P extends Proxy4Formable> List<P> getProxy4Formable(Class<P> clss) {
		List<P> list= new ArrayList<P>();
		for (Proxy4Formable proxy: this.proxyList) {
			if (proxy.getClass()==clss)		list.add((P)proxy);
		}
		return list;
	}
	
	private Formable getFormable(Component fComp) {
		List<EditView> editList = fComp.searchFormerLinkByClass(EditView.class);
		Object viewBean = null;
		String formerGetter = param.getString(ParameterName.Former);
		int vindex=0,vcount=editList.size();
		for (EditView cur=editList.get(vindex),prt=null; vindex<vcount; cur=prt,prt=null,vindex++) {
			Formable form = null;
			if (vindex+1<vcount)	prt=editList.get(vindex+1);
			try {
				viewBean = cur.getValue();
				if (StringUtils.isNotEmpty(formerGetter)) {
					form = (Formable)ReflectHelper.getPropertyValue(viewBean, formerGetter);
				} else if (viewBean instanceof Formable) {
					form = (Formable)viewBean;
				}
				return form;
			} catch(Exception e) {
				//
			}
		}
		return null;
	}
	
	private ListView getListView(FieldActionEvent event) {
		"1".toCharArray();
		Window win = event.getEvent().getSource().searchParentByClass(Window.class);
		LinkedHashSet<ListView> viewList = new LinkedHashSet<ListView>();
		List<ListView> innerViews = (event.getEvent().getSource().searchFormerByClass(View.class).getComponent().getInnerFormerList(ListView.class));
		List<ListView> linkViews = event.getEvent().getSource().searchFormerLinkByClass(ListView.class);
		viewList.addAll(innerViews);
		viewList.addAll(linkViews);
		if (win!=null) {
			List<ListView> outterViews = win.getInnerFormerList(ListView.class);
			viewList.retainAll(outterViews);
			viewList.addAll(outterViews);
		}
		String formerView = param.getString(ParameterName.View_Name);
		if (formerView==null) {
			return viewList.iterator().next();
		}
		for (ListView view: viewList) {
			if (formerView.equals(view.getViewBuilder().getName())) {
				return view;
			} else if (view.getViewBuilder().getFullName().endsWith(formerView)) {
				return view;
			}
		}
		throw new LogicException(2, "找不到视图".concat(formerView));
	}
	
	private ColumnField getPrimary(SqlListBuilder sqlBuilder) {
		return sqlBuilder.getSqlQuery().getPrimary();
	}
	
	private String getSqlSelectSource(SqlListBuilder sqlBuilder) {
		return sqlBuilder.getSqlQuery().getFields().sqlSelectSource();
	}
	
	public List<List<Object>> getSelectedListByFieldNames(SqlListBuilder sqlBuilder, List<ColumnField> fieldList, List<List<Object>> selectedList, List<String> fieldnameList) {
		HashMap<String, Integer> nameMap = new HashMap<String, Integer>();
		StringBuffer serror = new StringBuffer();
		int icol=0;
		HashSet<String> fieldNames = new HashSet<String>();
		for (Iterator<ColumnField> fiter=fieldList.iterator(); fiter.hasNext(); icol++) {
			String fname = fiter.next().getId();
			if (fieldNames.add(fname)==false)
				serror.append(fname).append(",");
			if (fiter.hasNext()==false && serror.length()>0)
				serror.append("列名重复，");
			if (fieldnameList.indexOf(fname) > -1)
				nameMap.put(fname, icol);
		}
		if (true) {
			List<String> unfinds = new ArrayList<String>(fieldnameList);
			unfinds.removeAll(nameMap.keySet());
			if (unfinds.size() > 0)
				serror.append("找不到配置列").append(unfinds);
		}
		if (serror.length()>0)
			throw new LogicException(2, serror.insert(0, "查询".concat(sqlBuilder.getName()).concat(" ")).toString());
		List<List<Object>> selectedRows = new ArrayList<List<Object>>();
		for (Iterator<List<Object>> rowIter=((List<List<Object>>)selectedList).iterator(); rowIter.hasNext();) {
			List<Object> row = rowIter.next();
			List<Object> valiRow = new ArrayList<Object>();
			for (String fname: fieldnameList) {
				int idx = nameMap.get(fname);
				valiRow.add(row.get(idx));
			}
			selectedRows.add(valiRow);
		}
		return selectedRows;
	}
	
	private List<ColumnField> getColumnListByFieldNames(List<ColumnField> fieldList, List<String> fieldnameList) {
		List<ColumnField> colList = new ArrayList<ColumnField>();
		for (Iterator<ColumnField> fiter=fieldList.iterator(); fiter.hasNext();) {
			ColumnField column = fiter.next();
			if (fieldnameList.contains(column.getId())==true)
				colList.add(column);
		}
		return colList;
	}

	private List<ColumnField> getFieldList(SqlListBuilder listBuilder) {
		Fields fields = listBuilder.getSqlQuery().getFields();
		return fields.getFields();
	}
	
	private Class getDomainClass(SqlListBuilder sqlBuilder) {
		String name = getParameter().getParameter(ParameterName.ID).getString(ParameterName.CLASS);
		if (name != null)
			return sqlBuilder.getPresentClass().getClassFinder().find(name);
		return ((DynaClass)sqlBuilder.getPresentClass().getDynaClass()).getClazz();
	}
	
	/**
	 * 批量加载对象集合，并将对象集合按原sql查询的顺序排序好
	 * 对于单头的会加载其明细
	 */
	private <T extends AbstractDomain> LinkedHashMap<Integer, AbstractDomain> toDomains(SqlListBuilder sqlBuilder, List<ColumnField> fieldList, List<List<Object>> selectedPage, List<String> nameList, Class<T> clazz) {
		if (selectedPage.isEmpty())
			return new LinkedHashMap<Integer, AbstractDomain>(0);
		if (true) {
			HashSet<Long> idSet = new HashSet<Long>(); 
			for (List<Object> row: selectedPage) {
				idSet.add((Long)row.get(0));
			}
			if (idSet.size() < selectedPage.size())
				throw new LogicException(2, "查询选择行有重复记录");
		}
		TraceItem traceItem = this.getTrace().onceStart("toDomains");
		LinkedHashMap<Integer, AbstractDomain> domainMap = new LinkedHashMap<Integer, AbstractDomain>();
		List<List<Object>> rows = getSelectedListByFieldNames(sqlBuilder, fieldList, selectedPage, nameList);
		BaseDAO dao = new BaseDAO();
		LinkedHashMap<Long, T> idList=new LinkedHashMap<Long, T>();
		String tableName = dao.getClassTableName(clazz);
		StringBuffer sql=new StringBuffer("select t.* from ").append(tableName).append(" t where t.id").append(getSqlSelectedIdInStr(rows));
		List<T> queryDomainList=dao.nativeQuery(sql.toString(), clazz);
		for (T d: queryDomainList) {
			d.getVoParamMap().remove("SelfSnapShot");
			d.getSnapShot();
			idList.put(d.getId(), d);
		}
		int irow = 0;
		StringBuffer serror = new StringBuffer();
		for (Iterator rIter=rows.iterator(); rIter.hasNext(); irow++) {
			List<Object> row=(List<Object>)rIter.next();
			long id = row.get(0)==null? 0: Long.valueOf(row.get(0)+"");
			int version = row.get(1)==null? 0: Integer.valueOf(row.get(1)+"");
			T domain = idList.get(id);
			if (domain==null) {
			} else if (domain.getVersion() > version) {
				serror.append("数据版本已更新version>").append(version).append("，请刷新后重试！");
			} else if (domain.getVersion() < version) {
				serror.append("数据版本version<").append(version).append("错误!");
			}
			domainMap.put(irow, domain);
		}
		traceItem.onceEnd();
		if (serror.length()>0)
			throw new LogicException(2, serror.insert(0,"查询".concat(sqlBuilder.getName())).toString());
		return domainMap;
	}
	public <T extends AbstractDomain> List<T> toDomains(List<List<Object>> rows, Class<T> clazz) {
		if (rows.isEmpty())			return new ArrayList<T>(0);
		TraceItem traceItem = this.getTrace().onceStart("toDomains");
		BaseDAO dao = new BaseDAO();
		LinkedHashMap<Long, T> idList=new LinkedHashMap<Long, T>();
		String tableName = dao.getClassTableName(clazz);
		StringBuffer sql=new StringBuffer("select t.* from ").append(tableName).append(" t where t.id").append(getSqlSelectedIdInStr(rows)).append(" and t.sellerId=?");
		List<T> queryDomainList=dao.nativeQuery(sql.toString(), clazz);
		for (T d: queryDomainList) {
			d.getVoParamMap().remove("SelfSnapShot");
			d.getSnapShot();
			idList.put(d.getId(), d);
		}
		List<T> domainList = new ArrayList<T>(idList.values());
		for (Iterator dIter=domainList.iterator(),rIter=rows.iterator(); dIter.hasNext();) {
			T domain = (T)dIter.next();
			List<Object> row=(List<Object>)rIter.next();
			if (domain==null) {
				dIter.remove();
				rIter.remove();
				continue;
			}
		}
		traceItem.onceEnd();
		return domainList;
	}
	
	private String getSqlSelectedIdInStr(List<List<Object>> selectedPage) {
		StringBuffer sb = new StringBuffer(" in (0,");
		for (Iterator iter = selectedPage.iterator(); iter.hasNext();) {
			List<Object> row = (List<Object>)iter.next();
			if (row.get(0)!=null)
				sb.append(row.get(0)).append(",");
		}
		return sb.deleteCharAt(sb.length()-1).append(")").toString();
	}
	
	public boolean isNeedValidate() {
		return param.getString(ParameterName.Validate, "true").equals("true");
	}

	public MetaParameter getParameter() {
		return param;
	}

	public void setParameter(MetaParameter parameter) {
		this.param = parameter;
	}
	
	private TraceCombine getTrace() {
		return PerformTrace.getTraceCombine(this);
	}
}
