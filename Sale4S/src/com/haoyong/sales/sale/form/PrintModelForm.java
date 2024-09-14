package com.haoyong.sales.sale.form;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.attributes.AttributeName;
import net.sf.mily.attributes.ClientEventName;
import net.sf.mily.attributes.StyleName;
import net.sf.mily.http.HtmlElement;
import net.sf.mily.http.RenderingContext;
import net.sf.mily.renderer.RendererFactory;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.ui.BlockGrid;
import net.sf.mily.ui.CheckBox;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Container;
import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.Panel;
import net.sf.mily.ui.Text;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.ui.enumeration.ElementName;
import net.sf.mily.ui.facable.OnchangeFormer;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.ComponentBuilder;
import net.sf.mily.webObject.EntityField;
import net.sf.mily.webObject.EntityField.FooterField;
import net.sf.mily.webObject.Field;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.ViewBuilder;
import net.sf.mily.webObject.query.Footer;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.common.domain.AbstractCommodityItem;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.sale.domain.PrintModel;
import com.haoyong.sales.sale.domain.TicketUser;
import com.haoyong.sales.sale.logic.PrintModelLogic;


public class PrintModelForm<D extends AbstractCommodityItem> extends AbstractForm<D> implements OnchangeFormer {
	
	private AbstractForm<D> form;
	private ViewBuilder builder;
	private PrintModel model;
	
	public static <D extends AbstractCommodityItem> PrintModelForm getForm(AbstractForm<D> fform, ViewBuilder builder) {
		if (fform instanceof FViewInitable)
			((FViewInitable)fform).viewinit((IEditViewBuilder)builder);
		PrintModel model = new PrintModelLogic().getPrintModel(builder);
		PrintModelForm form = new PrintModelForm();
		form.form = fform;
		form.builder = builder;
		form.model = model;
		model.setBuilder(builder.getFullViewName());
		return form;
	}
	
	private void validateModel() {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isBlank(this.model.getName()))
			sb.append("请填写模板名称，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private Panel showPrintModel() {
		Component tcomp = builder.build(form).getComponent();
		for (Iterator<Field> iter=tcomp.getInnerFormerList(Field.class).iterator(); iter.hasNext();) {
			Field field = iter.next();
			if (field.getClass() != Field.class)
				continue;
			if (field.getComponent().getClass()==Text.class) {
				Text ftext = (Text)field.getComponent();
				if (EntityField.getEntityField(ftext) instanceof FooterField) {
				} else
					ftext.setText(this.getLabel(field.getFieldBuilder()));
			}
		}
		for (Iterator<CheckBox> iter=tcomp.getInnerComponentList(CheckBox.class).iterator(); iter.hasNext();) {
			CheckBox check = iter.next();
			check.getParent().fireComponentRemove(check);
			if (check.searchParentByClass(BlockGrid.class).getColSize()>2)
				check.getParent().getInnerComponentList(Text.class).get(0).setText("(序号)");
		}
		for (FooterField footerRow: tcomp.getInnerFormerList(FooterField.class)) {
			for (Field field: footerRow.getFieldList()) {
				Text ftext = (Text)field.getComponent();
				ftext.setText(this.getFootLabel(field.getFieldBuilder()));
			}
		}
		Panel panel = this.getPanel();
		panel.getContent().delete(0, panel.getContent().length());
		HtmlElement telem = (HtmlElement)new RendererFactory().getRenderer(tcomp).render(tcomp, new RenderingContext(null), new HtmlElement(ElementName.CONTENT), new HtmlElement(ElementName.CONTENT));
		StringWriter writer = new StringWriter();
		telem.render(new PrintWriter(writer));
		writer.flush();
		panel.getContent().append(writer.toString());
		return panel;
	}
	
	public void showPrintOne(Component fcomp) {
		Component tcomp = builder.build(form).getComponent();
		if (this.model.getContent().length()==0)
			throw new LogicException(2, "请先设置打印模板");
		String one = this.model.getContent().toString().split("__tr__")[0];
		if ("单头".length()>0)
			for (Iterator<Field> iter=tcomp.getInnerFormerList(Field.class).iterator(); iter.hasNext();) {
				Field field = iter.next();
				if (!(field.getComponent()!=null && field.getComponent().getClass()==Text.class))
					continue;
				Text ftext = (Text)field.getComponent();
				if (EntityField.getEntityField(ftext) instanceof FooterField) {
				} else if (field.getComponent().searchFormerLinkByClass(ListView.class).size()==0) {
					one = StringUtils.replace(one, this.getLabel(field.getFieldBuilder()), ftext.getText());
				}
			}
		if ("明细合计".length()>0)
			for (FooterField footerRow: tcomp.getInnerFormerList(FooterField.class)) {
				for (Field field: footerRow.getFieldList()) {
					Text ftext = (Text)field.getComponent();
					one = StringUtils.replace(one, this.getFootLabel(field.getFieldBuilder()), ftext.getText());
				}
			}
		if ("明细".length()>0) {
			String[] htmlList = this.model.getContent().toString().split("__tr__");
			for (int di=htmlList.length; di-->1;) {
				String detail = htmlList[di];
				ListView listview = tcomp.getInnerFormerList(ListView.class).get(0);
				StringBuffer detailList = new StringBuffer();
				for (Iterator<EntityField> eiter=listview.getListBuilder().getEntityFieldIterator((BlockGrid)listview.getComponent()); eiter.hasNext();) {
					EntityField enti = eiter.next();
					String adetail = StringUtils.replace(new String(detail), "(序号)", enti.getRowIndex()+1+"");
					for (Iterator<Field> fiter=enti.getFieldList().iterator(); fiter.hasNext();) {
						Field field = fiter.next();
						if (!(field.getComponent()!=null && field.getComponent().getClass()==Text.class))
							continue;
						Text ftext = (Text)field.getComponent();
						adetail = StringUtils.replace(adetail, this.getLabel(field.getFieldBuilder()), ftext.getText());
					}
					detailList.append("\n").append(adetail).append("\n");
				}
				one = StringUtils.replace(one, detail, detailList.toString());
			}
			if (true) {
				Panel panel = new Panel();
				panel.addStyle(StyleName.WIDTH, "99.6%").addStyle(StyleName.HEIGHT, "99%").addStyle(StyleName.BACKGROUND, "white").addStyle(StyleName.BORDER, "solid 1px black").addStyle(StyleName.Overflow, "hidden");
				panel.addAttribute(AttributeName.ContentEditable, true).addAttribute(ClientEventName.ONCONTEXTMENU, "return false;");
				panel.getContent().append(one);
				WindowMonitor.getMonitor().addAttribute("PrintOne", panel);
			}
		}
		if (true) {
			StringBuffer openStr=new StringBuffer();
			String url="actionform.jsp?action=sale.PrintModelForm&prepare=getPrintOne";
			openStr.append("window.open('").append(url).append("','a_blank');");
			fcomp.addAttribute(ClientEventName.InitScript0, openStr.toString());
		}
	}
	
	public String getPropertyName() {
		return this.builder.getName();
	}
	
	public void onComponentChange(String newValue) {
		this.model.getContent().delete(0, this.model.getContent().length());
		this.model.getContent().append(newValue);
	}
	
	private void setModel4Service(ViewData<PrintModel> viewData) {
		viewData.setTicketDetails(this.model);
	}
	
	private TicketUser genTicketUser() {
		TicketUser user = new TicketUser();
		user.setUser(this.getUserName());
		user.setDate(new Date());
		return user;
	}
	
	private void setModelUser(ViewData<PrintModel> viewData) {
		String suser = genTicketUser().getUserDate();
		for (PrintModel model: viewData.getTicketDetails()) {
			model.setUcreate(suser);
		}
	}

	private StringBuffer getRemind() {
		String k="Remind";
		StringBuffer sb = this.getAttr(k);
		if (sb==null) {
			sb = new StringBuffer();
			sb.append("可全选清空、编写文本，可粘贴入Excel单元格、Word文本、网页。");
			this.setAttr(k, sb);
		}
		return sb;
	}
	private void setRemind(String s) {
		getRemind().delete(0, getRemind().length());
		getRemind().append(s);
	}
	
	private Component getPrintOne() {
		Panel panel = (Panel)WindowMonitor.getMonitor().getAttribute("PrintOne");
		if (panel==null)
			throw new LogicException(2, "打印源头已关闭");
		else
			WindowMonitor.getMonitor().removeAttribute("PrintOne");
		return panel;
	}

	public void setSelectedList(List<D> selected) {
	}
	
	private void getTicketLabels(Hyperlink link, Object labels, PrintModel model) {
		this.getTicketLabels().clear();
		List<FieldBuilder> list=(List<FieldBuilder>)model.getVoParamMap().get("TicketList");
		Container grid = new Container();
		grid.addStyle(StyleName.WhiteSpace, "normal");
		for (FieldBuilder b: list) {
			Text t = new Text(this.getLabel(b));
			this.getTicketLabels().add(t);
			grid.add(t.setStyleClass("ListViewCell"));
		}
		link.add(grid);
		if (model.getId()==0)
			grid.addAttribute(ClientEventName.InitScript0, "L_action('_PrintModel');");
	}
	
	private void getDetailLabels(Hyperlink link, Object labels, PrintModel model) {
		this.getDetailLabels().clear();
		List<FieldBuilder> list=(List<FieldBuilder>)model.getVoParamMap().get("DetailList");
		Container grid = new Container();
		grid.addStyle(StyleName.WhiteSpace, "normal");
		if (true) {
			Text t = new Text(new StringBuffer().append("(序号)").toString());
			this.getDetailLabels().add(t);
			grid.add(t.setStyleClass("ListViewCell"));
		}
		for (FieldBuilder b: list) {
			Text t = new Text(this.getLabel(b));
			this.getDetailLabels().add(t);
			grid.add(t.setStyleClass("ListViewCell"));
		}
		link.add(grid);
	}
	
	private void getDetailCounts(Hyperlink link, Object counts, PrintModel model) {
		this.getCountLabels().clear();
		List<Footer> list=(List<Footer>)model.getVoParamMap().get("FooterList");
		Container grid = new Container();
		grid.addStyle(StyleName.WhiteSpace, "normal");
		for (Footer b: list) {
			Text t = new Text(this.getFootLabel(b.getField()));
			this.getCountLabels().add(t);
			grid.add(t.setStyleClass("ListViewCell"));
		}
		link.add(grid);
	}
	
	private Panel getContent(Object prt, ComponentBuilder builder) {
		Panel panel = this.getPanel();
		PrintModel model = (PrintModel)prt;
		if (panel == null) {
			panel = new Panel();
			panel.setViewContainerable();
			panel.addStyle(StyleName.WIDTH, "99.99%").addStyle(StyleName.HEIGHT, "100%").addStyle(StyleName.BACKGROUND, "white").addStyle(StyleName.BORDER, "solid 1px black").addStyle(StyleName.Overflow_X, "hidden");
			panel.addAttribute(AttributeName.ContentEditable, true).addAttribute(ClientEventName.ONCONTEXTMENU, "return false;");
			panel.addAttribute(ClientEventName.InitScript0, "for (var td=this.parentNode,table=G_Element.getParentByTag(td,'TABLE'); table!=null; td.style.height='100%',table.style.height='100%',td=table.parentNode,table=G_Element.getParentByTag(td,'TABLE'));");
			this.setAttr("Panel", panel);
			panel.setFormer(this);
		}
		StringBuffer labelIds = new StringBuffer();
		for (Text text: this.getTicketLabels()) {
			labelIds.append(text.getIdentifier()).append(",");
		}
		labelIds.deleteCharAt(labelIds.length()-1).append(";");
		for (Text text: this.getDetailLabels()) {
			labelIds.append(text.getIdentifier()).append(",");
		}
		labelIds.deleteCharAt(labelIds.length()-1).append(";");
		for (Text text: this.getCountLabels()) {
			labelIds.append(text.getIdentifier()).append(",");
		}
		labelIds.deleteCharAt(labelIds.length()-1).append(";");
		panel.addAttribute(AttributeName.ItemIDS, labelIds.deleteCharAt(labelIds.length()-1).toString());
		panel.addAttribute(ClientEventName.ONMOUSEOVER, "CheckLabels(this);");
		panel.addAttribute(ClientEventName.InitScript2, "this.svalue=this.innerHTML;ChangeModel(this);");
		panel.addAttribute(ClientEventName.ONBLUR, new StringBuffer().append("if ( (this.tvalue0=(this.tvalue || this.svalue))!=(this.tvalue=this.innerHTML)) ChangeModel(this);").toString());
		if (model.getId()>0 && model.getContent().length()>10) {
			panel.getContent().delete(0, panel.getContent().length());
			panel.getContent().append(this.model.getContent().toString().split("__tr__")[0]);
		} else {
			ReflectHelper.invokeMethod(this.form, "preparePrintModel", new Object[0]);
			panel = this.showPrintModel();
		}
		panel.addAttribute(ClientEventName.InitScript0, new StringBuffer("CheckLabels(this);this.focus();").toString());
		panel.addAttribute(ClientEventName.InitScript1, "ajax.attributeNode(this);compList.doInit();");
		return panel;
	}
	
	private Panel getPanel() {
		String k = "Panel";
		return (Panel)this.getAttr(k);
	}
	
	private List<Text> getTicketLabels() {
		String k = "TicketLabelList";
		List<Text> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<Text>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private List<Text> getDetailLabels() {
		String k = "DetailLabelList";
		List<Text> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<Text>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private List<Text> getCountLabels() {
		String k = "DetailCountLabelList";
		List<Text> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<Text>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private String getLabel(FieldBuilder builder) {
		String l = new StringBuffer("(").append(builder.getLabel()).append(")").toString();
		return l;
	}
	
	private String getFootLabel(FieldBuilder builder) {
		String l = new StringBuffer("(").append(builder.getViewBuilder().getLabel()).append(builder.getLabel()).append(".").append(")").toString();
		return l;
	}
}
