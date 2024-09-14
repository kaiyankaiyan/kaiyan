package com.haoyong.sales.base.form;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.ui.BlockGrid;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.util.LogUtil;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.EntityField;
import net.sf.mily.webObject.Field;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.FieldBuilderFactory;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.ListViewBuilder;
import net.sf.mily.webObject.TextBuilder;
import net.sf.mily.webObject.TextFieldBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.hibernate.util.JoinedIterator;
import org.junit.Assert;

import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.common.domain.AbstractDomain;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.sale.form.PurchaseTicketForm;
import com.haoyong.sales.test.base.ClientTest;

public class BaseImportForm extends AbstractForm<AbstractDomain> {
	
	public List<TextFieldBuilder> validateIndexes(Component fcomp) {
		this.getImportFieldList().clear();
		StringBuffer sb = new StringBuffer();
		Set<String> colList = new HashSet<String>();
		List<Component> colComps = fcomp.searchFormerByClass(EditView.class).getComponent().getInnerFormerList(ListView.class).get(0).getComponent().getInnerFormerComponentList(Field.class);
		int cok = 0;
		List<TextFieldBuilder> builderList = new ArrayList<TextFieldBuilder>();
		for (Iterator<Component> iter=colComps.iterator(); iter.hasNext();) {
			Component colText = iter.next();
			String svalue = null;
			if (colText instanceof TextField)
				svalue = ((TextField)colText).getText();
			if (StringUtils.isBlank(svalue)==true)
				continue;
			else if (colList.add(svalue)==false) {
				sb.append("存在相同的列序号").append(svalue).append(",");
				continue;
			}
			ImportField field = new ImportField();
			this.getImportFieldList().add(field);
			TextFieldBuilder colBuilder = (TextFieldBuilder)((Field)colText.getFormer()).getFieldBuilder();
			field.builder = colBuilder;
			builderList.add(colBuilder);
			sb.append(field.setLabelCols(svalue));
			cok++;
		}
		if (sb.length()==0 && cok==0)
			sb.append("请输入列序号，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
		return builderList;
	}
	
	public void setImportBuilderInit(ViewBuilder viewBuilder) {
		List<ViewBuilder> viewList = viewBuilder.getFieldBuildersDeep(ListViewBuilder.class);
		ViewBuilder importBuilder = viewList.get(0);
		for (Iterator<FieldBuilder> iter=importBuilder.getFieldBuilderLeafs().iterator(); iter.hasNext();) {
			FieldBuilder builder = iter.next();
			builder.setParameters(builder.cloneParameters());
			builder.setParameters(ParameterName.Button, null);
			builder.setParameters(ParameterName.Listener, null);
			if (builder instanceof ViewBuilder) {
				iter = new JoinedIterator(((ViewBuilder)builder).getFieldBuilderIterator(), iter);
			} else if (builder instanceof TextBuilder) {
				continue;
			} else if (!(builder instanceof TextFieldBuilder)) {
				TextFieldBuilder fb = new TextFieldBuilder();
				TicketPropertyUtil.copyFieldsSkip(builder, fb);
				fb.setViewBuilder(builder.getViewBuilder());
				fb.setAttribute(builder, ParameterName.Type, ParameterName.Type);
				builder.getViewBuilder().getFieldBuilders().set(builder.getViewBuilder().getFieldBuilders().indexOf(builder), fb);
			}
		}
		ViewBuilder listBuilder = viewList.get(1);
		for (Iterator<FieldBuilder> iter=listBuilder.getFieldBuilderLeafs().iterator(); iter.hasNext();) {
			FieldBuilder builder = iter.next();
			builder.setParameters(builder.cloneParameters());
			builder.setParameters(ParameterName.Button, null);
			builder.setParameters(ParameterName.Listener, null);
		}
	}
	
	public void setImportIndexDefault(Component fcomp) {
		List<Component> compList = fcomp.searchFormerByClass(EditView.class).getComponent().getInnerFormerList(ListView.class).get(0).getComponent().getInnerFormerComponentList(Field.class);
		int ci = 0;
		for (Component c: compList) {
			if ((c instanceof TextField)==false)
				continue;
			TextField text = (TextField)c;
			text.setText(++ci+"");
		}
	}
	public void setImportIndexClear(Component fcomp) {
		List<Component> compList = fcomp.searchFormerByClass(EditView.class).getComponent().getInnerFormerList(ListView.class).get(0).getComponent().getInnerFormerComponentList(Field.class);
		for (Component c: compList) {
			if ((c instanceof TextField)==false)
				continue;
			TextField text = (TextField)c;
			text.setText(null);
		}
	}
	
	public <D extends AbstractDomain> List<D> setImportFormated(Component fcomp, D domain) {
		List<String> tList = new ArrayList<String>();
		if ("单元格列表".length()>0) {
			StringBuffer input = new StringBuffer().append(domain.getVoParamMap().get("Remark"));
			if ("null".equals(input.toString()))
				return null;
			tList.addAll(Arrays.asList(input.toString().replaceAll("\\n\\t", "\n \t").split("(\t)")));
			for (int ti=tList.size(); ti-->0; ) {
				String t = tList.get(ti);
				if (t.indexOf("\"")>-1) {
					String svalue = t;
					String regex = "(\")";
					int cnt = svalue.length() - svalue.replaceAll(regex, "").length();
					Assert.assertTrue("合并单元格开头结尾", cnt>0 && cnt%2==0);
					List<String> subs = new ArrayList<String>();
					Matcher m=Pattern.compile(regex).matcher(svalue);
					for (int mi=0, p1=0, f0=0, f1=0; ; ) {
						if (m.find()) {
							mi++;
							if (mi%2==1) {
								f0=m.start();
								if (f0-p1>0)
									subs.add(svalue.substring(p1, f0));
							} else {
								f1=m.start()+1;
								subs.add(svalue.substring(f0, f1));
								p1=f1;
							}
						} else {
							if (svalue.length()>p1)
								subs.add(svalue.substring(p1));
							tList.remove(ti);
							tList.addAll(ti, subs);
							int subLen=0;
							for (String sub: subs) {
								subLen += sub.length();
							}
							Assert.assertTrue("合并单元格字符数齐全", subLen==svalue.length());
							break;
						}
					}
				}
			}
			"".toCharArray();
			for (int ti=tList.size(); ti-->0; ) {
				String t = tList.get(ti);
				if (t.startsWith("\"") && t.endsWith("\"")) {
					tList.set(ti, new StringBuffer(t).deleteCharAt(t.length()-1).deleteCharAt(0).toString());
				} else if (t.indexOf("\n") > -1) {
					String[] tt = t.split("(\n)");
					List<String> subs = new ArrayList<String>();
					if (tt.length>0)	subs.add(tt[0]);
					subs.add("\n");
					if (tt.length==2)	subs.add(tt[1]);
					tList.remove(ti);
					tList.addAll(ti, subs);
				}
			}
		}
		List<List<String>> rowList = new ArrayList<List<String>>();
		if ("整行列表".length()>0) {
			List<String> row=new ArrayList<String>(), priou=null;
			rowList.add(row);
			for (String t: tList) {
				if (StringUtils.equals(t, "\n")) {
					if (priou!=null && priou.size()!=row.size())
						"".toCharArray();
					if (priou!=null)
						Assert.assertTrue(new StringBuffer().append("当前行").append(rowList.size()).append("跟前一行单元格数相等").toString(), priou.size()==row.size());
					priou = row;
					row = new ArrayList<String>();
					rowList.add(row);
				} else {
					row.add(t.trim());
				}
			}
			if (row.size()==0) {
				rowList.remove(row);
			} else if (priou!=null)
			for (int ci=priou.size()-row.size(); ci-->0;) {
				row.add("");
			}
		}
		List<D> importList = new ArrayList<D>();
		if ("写入对象".length()>0) {
			D source = TicketPropertyUtil.deepClone(domain);
			if ("Domain样板清理".length()>0) {
				D entiNew = null;
				try {
					entiNew = (D)domain.getClass().newInstance();
				} catch (Exception e) {
					throw LogUtil.getRuntimeException(e);
				}
				for (ImportField field: this.getImportFieldList()) {
					FieldBuilder builder = field.builder;
					Object propNew = builder.getEntityPropertyValue(entiNew);
					try {
						builder.setEntityPropertyValue(source, propNew);
					} catch (ParseException e) {
					}
				}
			}
			ListView dview = new ClientTest().setWindow(fcomp.searchParentByClass(Window.class)).getEditListView("domain");
			ListViewBuilder vbuilder = (ListViewBuilder)dview.getViewBuilder();
			EntityField enfield = (EntityField)vbuilder.getEntityFieldIterator((BlockGrid)dview.getComponent()).next();
			vbuilder = (ListViewBuilder)enfield.getFieldBuilder();
			for (Iterator<List<String>> rowIter=rowList.iterator(); rowIter.hasNext();) {
				List<String> row = rowIter.next();
				D detail = TicketPropertyUtil.deepClone(source);
				importList.add(detail);
				StringBuffer error = new StringBuffer();
				for (ImportField field: this.getImportFieldList()) {
					if ("一行单元格写入Field ".length()>0)
						field.setVals(row, rowIter.hasNext());
					FieldBuilder builder = field.builder;
					FieldBuilder bfrom = (FieldBuilder)builder.getParameter(ParameterName.Type).getObject(ParameterName.Type);
					String t = field.getEntiValue();
					try {
						builder.getEntityPropertyValue(detail);
						Object v=builder.getFormatType().parse(t);
						if (bfrom!=null) {
							bfrom = TicketPropertyUtil.copyFieldsSkip(vbuilder.getFieldBuilder(builder.getName()), FieldBuilderFactory.create(bfrom.getClass()));
							boolean require = StringUtils.equals("Require", bfrom.getParameter(ParameterName.Cfg).getString(ParameterName.Type));
							Object v1 = bfrom.pickValue(bfrom.build(v).getComponent());
							builder.setEntityPropertyValue(detail, v1);
							if (StringUtils.equals(v+"", v1+"")==false)
								throw new ParseException(new StringBuffer().append(v).append(v1).toString(), 0);
							if (require && v1==null) {
								t = "必填";
								throw new ParseException(new StringBuffer().append(v).append(v1).toString(), 0);
							}
						} else if (builder instanceof TextFieldBuilder) {
							builder.setEntityPropertyValue(detail, v);
						}
					} catch(Exception e) {
						LogUtil.error("导入列值错误".concat(t));
						error.append(builder.getLabel()).append(t).append(",");
					}
				}
				detail.getVoParamMap().put("error", error.toString());
			}
		}
		return importList;
	}
	
	public void getSellerIndexes(Component fcomp) {
		ListView listview = fcomp.searchFormerByClass(EditView.class).getComponent().getInnerFormerList(ListView.class).get(0);
		SellerViewInputs inputs = this.getSellerViewInputs();
		Assert.assertTrue("已设置列配置", inputs!=null);
		String startName = listview.getViewBuilder().getFullName();
		HashMap<String, String> textMap = inputs.getInputs();
		List<Component> colComps = listview.getComponent().getInnerFormerComponentList(Field.class);
		for (Iterator<Component> iter=colComps.iterator(); iter.hasNext();) {
			Component col = iter.next();
			if ((col instanceof TextField)==false)
				continue;
			TextField colField = (TextField)col;
			TextFieldBuilder colBuilder = (TextFieldBuilder)((Field)col.getFormer()).getFieldBuilder();
			String colName = colBuilder.getFullName().substring(startName.length()+1);
			String colText = textMap.get(colName);
			if (colText!=null)
				colField.setText(colText);
		}
	}
	
	public void setSellerIndexes(ListView listview, SellerViewInputs inputs, String... others) {
		inputs.getInputs().clear();
		String startName = listview.getViewBuilder().getFullName();
		HashMap<String, String> textMap = new LinkedHashMap<String, String>();
		for (Iterator<String> iter=Arrays.asList(others).iterator(); iter.hasNext();) {
			textMap.put(iter.next(), iter.next());
		}
		List<Component> colComps = listview.getComponent().getInnerFormerComponentList(Field.class);
		for (Iterator<Component> iter=colComps.iterator(); iter.hasNext();) {
			Component col = iter.next();
			if ((col instanceof TextField)==false)
				continue;
			TextField colField = (TextField)col;
			TextFieldBuilder colBuilder = (TextFieldBuilder)((Field)col.getFormer()).getFieldBuilder();
			String colName = colBuilder.getFullName().substring(startName.length()+1);
			String colText = colField.getText();
			if (StringUtils.isBlank(colText)==false)
				textMap.put(colName, colText);
		}
		inputs.setInputs(textMap);
	}
	
	private UserForm getUserForm() {
		UserForm form = this.getAttr(UserForm.class);
		if (form == null) {
			form = new UserForm();
			this.setAttr(form);
		}
		form.prepareImport();
		return form;
	}
	
	private CommodityForm getCommodityForm() {
		CommodityForm form = this.getAttr(CommodityForm.class);
		if (form == null) {
			form = new CommodityForm();
			this.setAttr(form);
		}
		form.prepareImport();
		return form;
	}
	private StorehouseForm getStorehouseForm() {
		StorehouseForm form = this.getAttr(StorehouseForm.class);
		if (form == null) {
			form = new StorehouseForm();
			this.setAttr(form);
		}
		form.prepareImport();
		return form;
	}
	private SupplierForm getSupplierForm() {
		SupplierForm form = this.getAttr(SupplierForm.class);
		if (form == null) {
			form = new SupplierForm();
			this.setAttr(form);
		}
		form.prepareImport();
		return form;
	}
	
	private ClientForm getClientForm() {
		ClientForm form = this.getAttr(ClientForm.class);
		if (form == null) {
			form = new ClientForm();
			this.setAttr(form);
		}
		form.prepareImport();
		return form;
	}
	
	private SubCompanyForm getSubCompanyForm() {
		SubCompanyForm form = this.getAttr(SubCompanyForm.class);
		if (form == null) {
			form = new SubCompanyForm();
			this.setAttr(form);
		}
		form.prepareImport();
		return form;
	}
	
	private PurchaseTicketForm getPurchaseTicketForm() {
		PurchaseTicketForm form = this.getAttr(PurchaseTicketForm.class);
		if (form == null) {
			form = new PurchaseTicketForm();
			this.setAttr(form);
		}
		form.prepareImport();
		return form;
	}
	
	public void setSelectedList(List<AbstractDomain> selected) {
		this.getSelectedList().clear();
		this.getSelectedList().addAll(selected);
	}
	
	public SellerViewInputs getSellerViewInputs() {
		return this.getAttr(SellerViewInputs.class);
	}

	private List<AbstractDomain> getSelectedList() {
		String k = "SelectedList";
		List<AbstractDomain> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<AbstractDomain>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private List<ImportField> getImportFieldList() {
		String k = "ImportFieldList";
		List<ImportField> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<ImportField>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private static class ImportField {
		
		private FieldBuilder builder;
		private String label;
		private List<Integer> cols = new ArrayList<Integer>();
		private List<Object> vals = new ArrayList<Object>();
		
		public void setVals(List<String> row, boolean hasNext) {
			this.vals.clear();
			for (Integer col: cols) {
				Object cval = "";
				if (hasNext==false && row.size()<col) {
				} else {
					Assert.assertTrue(new StringBuffer("单元格应有列").append(col).toString(), row.size()>=col);
					cval = row.get(col-1);
				}
				this.vals.add(cval);
			}
		}
		
		public StringBuffer setLabelCols(String svalue) {
			this.label = svalue;
			StringBuffer sb = new StringBuffer();
			String regex = "(\\(\\d+\\))|(\\d+)";
			if (true) {
				if (svalue.replaceAll(regex, "").length()==svalue.length()) {
					sb.append("没有列数字序号，");
				}
				for (Matcher m = Pattern.compile(regex).matcher(svalue); m.find(); ) {
					String sd = svalue.substring(m.start(), m.end()).replaceAll("(\\()|(\\))", "");
					this.cols.add(Integer.valueOf(sd));
					if (Integer.valueOf(sd) < 1)
						sb.append("列序号必须大于0,");
				}
			}
			return sb;
		}
		
		public String getEntiValue() {
			StringBuffer sb = new StringBuffer();
			if ("拼组合字符串值".length()>0) {
				String sql = this.label;
				String regex = "(\\(\\d+\\))|(\\d+)";
				Matcher m = Pattern.compile(regex).matcher(sql);
				if (sql.length()>3)
					"".toCharArray();
				for (int msize=0, si=0, pi=0, slen=sql.length(); si<slen; si++) {
					if (m.find()) {
						msize++;
						Object param = this.vals.get(pi++);
						sb.append(sql.substring(si, m.start()));
						sb.append(param);
						si = m.end()-1;
					} else {
						sb.append(sql.substring(si, sql.length()));
						si = sql.length()-1;
					}
					if (si==sql.length()-1)
						Assert.assertTrue("Label列个数=单元格个数", msize==this.vals.size());
				}
			}
			if ("减少空格".length()>0) {
				String[] itemList = sb.toString().trim().split("\\s+");
				LinkedHashSet<String> itemSet = new LinkedHashSet<String>();
				itemSet.addAll(Arrays.asList(itemList));
				StringBuffer st = new StringBuffer();
				for (String s: itemSet) {
					st.append(s).append(" ");
				}
				sb = st.deleteCharAt(st.length()-1);
			}
			return sb.toString();
		}
	}
}
