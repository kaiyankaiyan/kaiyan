package com.haoyong.sales.common.listener;

import java.math.BigDecimal;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.mily.bean.PropertyNotFoundException;
import net.sf.mily.exception.MilyException;
import net.sf.mily.exui.filetransfer.Download;
import net.sf.mily.mappings.MetaParameter;
import net.sf.mily.support.form.Formable;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.EditListBuilder;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.event.ActionChain;
import net.sf.mily.webObject.event.FieldActionEvent;
import net.sf.mily.webObject.event.FieldActionListener;
import net.sf.mily.webObject.query.ColumnField;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 获取查询列表的全部查询结果
 *
 *view-name为指定要导出的表格名，如果有多个值用逗号分隔
 */
public class SelectQueryresultListener implements FieldActionListener {
	
	private MetaParameter param;

	public void actionPerformed(FieldActionEvent event, ActionChain chain) {
		Workbook workbook =  new HSSFWorkbook();//建立新HSSFWorkbook对象
		String viewName = param.getString(ParameterName.View_Name, event.getField().getFieldBuilder().getName());
		String formerGetter = param.getString(ParameterName.Former);
		String formerSetter = param.getString(ParameterName.Setter);
		List<String> nameList = Arrays.asList(viewName.split(","));
		Component fComp = event.getField().getComponent();
		Exception error = null;
		boolean hasForm=false;
		for (Iterator<ListView> iter = fComp.getParent().getInnerFormerList(ListView.class).iterator(); iter.hasNext();) {
			ListView listview = iter.next();
			try {
				List selectedList = listview.getRecordPageAll();
				if (nameList.contains(listview.getViewBuilder().getName())) {
					if (null!=formerSetSelected(workbook,fComp, listview, selectedList,formerGetter,formerSetter)) {
						hasForm=true;
					}
				}
			} catch (Exception e) {
				error = e;
			}
		}
		//输出
		Download download=new Download();
		String excelName=String.valueOf(System.currentTimeMillis());
		download.setActive(true);
		Window win = fComp.searchParentByClass(Window.class);
		win.add(download);
		
		if (hasForm) {
			chain.end();
			if (error != null) {
				throw new MilyException(error);
			}
			return;
		}
	}

	
	private Formable formerSetSelected(Workbook workbook, Component fComp, ListView listview, List selectedList,
			String formerGetter,String formerSetter	) throws Exception {
		Object viewBean = null;
		Formable form = null;
		Exception error = null;
		List<EditView> viewList = fComp.searchFormerLinkByClass(EditView.class);
		int vindex=0,vcount=viewList.size();
		for (EditView cur=viewList.get(vindex),prt=null; vindex<vcount; cur=prt,prt=null,vindex++) {
			if (vindex+1<vcount)	prt=viewList.get(vindex+1);
			try {
				viewBean = cur.getValue();
				if (StringUtils.isNotEmpty(formerGetter)) {
					form = (Formable)ReflectHelper.invokeMethod(viewBean, formerGetter, new Object[0]);
				} else if (viewBean instanceof Formable) {
					form = (Formable)viewBean;
				}
				if (form != null) {
					setSelectedList(workbook,form, formerSetter, listview, selectedList);
				}
				break;
			} catch(PropertyNotFoundException e) {
				if (error == null)		error = e;
			} catch(Exception e) {
				error = e;
			}
			if (prt == null) {
				if (error != null) {
					throw new MilyException(error);
				}
			}
		}
		return form;
	}

	private void setSelectedList(Workbook workbook,Formable form, String formerSetter, ListView listview, List selectedList) {
		//sheet名称
		String label=listview.getFieldBuilder().getLabel();
		if (StringUtils.isEmpty(label)) {
			label=String.valueOf(System.currentTimeMillis());
		}
		if(workbook.getSheet(label)!=null){//可能名称已经被使用了
			label=String.valueOf(System.currentTimeMillis());
		}
		//判断builder类型
		if(listview.getListBuilder() instanceof SqlListBuilder) {
			SqlListBuilder sqlListBuilder = (SqlListBuilder)listview.getListBuilder();
			List<ColumnField> builderList= sqlListBuilder.getSqlQuery().getFields().getFields();
			if (StringUtils.isNotEmpty(formerSetter)) {
				ReflectHelper.invokeMethod(form, formerSetter, new Class[]{List.class,List.class}, new Object[]{selectedList,builderList});
			}
			//控制导出数量最多4万一次、按我的本地JVM最低配置,否则java heap space
			int icount=selectedList.size();
			selectedList.subList(0, icount>40000? 40000: icount);
			//导出Excel
			sqlListToWorkbook(workbook, selectedList, builderList, label);
		}else if(listview.getListBuilder() instanceof EditListBuilder){
			//控制导出数量最多4万一次、按我的本地JVM最低配置,否则java heap space
			int icount=selectedList.size();
			selectedList.subList(0, icount>40000? 40000: icount);
			//导出Excel
			checkListToWorkbook(workbook, selectedList,label);
		}
	}
	
	/*--------------------------已下是导出Excel封装 method------------------------------*/
	private void sqlListToWorkbook(Workbook wb,List<?> selectedList, List<?> builderList,String sheetName){
		//声明数组对象 
		String[] title=new String[builderList.size()];//表头
		String[] param=new String[builderList.size()];//参数列表
		//声明List对象
		List<Map<String,Object>> listMap=new ArrayList<Map<String,Object>>();//要导出的列表数据
		//Sheet名称
		String sheetNameE=sheetName;
		
		//给：表头,参数列表,数组赋值
		for(int i=0;i<builderList.size();i++){
			ColumnField column=(ColumnField)builderList.get(i);
			String labelName=column.getLabel();
			String entityName=column.getId();
			title[i]=labelName;
			param[i]=entityName;
		}
		//给：列表数据赋值,i=0
		for(int i=0;i<selectedList.size();i++){
			//对应参数列表存值
			Map<String,Object> map=new HashMap<String, Object>();
			List<?> list=(List<?>) selectedList.get(i);
			for(int j=0;j<list.size();j++){
				map.put(param[j], list.get(j));
			}
			listMap.add(map);
		}
		
		/*导出数据*/
		createSheet(wb,title,param,listMap,sheetNameE);
	}
	
	@SuppressWarnings("unchecked")
	private void checkListToWorkbook(Workbook wb,List<?> selectedList,String sheetName){
		//声明List对象
		List<Map<String,Object>> listMap=new ArrayList<Map<String,Object>>();//要导出的列表数据
		//Sheet名称
		String sheetNameE=sheetName;
		List<Object> listObj=(List<Object>) selectedList.get(0);
		//声明数组对象 
		String[] title=new String[listObj.size()];//表头;
		String[] param=new String[listObj.size()];//参数列表
		//给：表头,参数列表,数组赋值
		for(int i=0;i<listObj.size();i++){
			String labelName=(String) listObj.get(i);
			title[i]=labelName;
			param[i]=""+i;
		}
		//给：列表数据赋值,i=1
		for(int i=1;i<selectedList.size();i++){
			//对应参数列表存值
			Map<String,Object> map=new HashMap<String, Object>();
			List<?> list=(List<?>) selectedList.get(i);
			for(int j=0;j<list.size();j++){
				map.put(param[j], list.get(j));
			}
			listMap.add(map);
		}
		/*导出数据*/
		createSheet(wb,title,param,listMap,sheetNameE);
	}
	
	/**
	 * @param sheetIndex 0第一页
	 */
	private String createSheetName(int sheetIndex){
		if(sheetIndex<0){
			return "";
		}
		sheetIndex=sheetIndex*10000;
		return "{ "+sheetIndex+"~"+(sheetIndex+10000)+" }";
	}
	
	private void createSheet(Workbook wb,String[] title,String[] param,List<Map<String,Object>> list,String sheetName) {
		int sheetIndex=0;//sheet分页标记id++
		Sheet sheet = wb.createSheet(sheetName+createSheetName(sheetIndex++));//建立新的sheet对象
		int i = 0;
		Row row = sheet.createRow(i++);//建立新行
		row.setHeight((short)400);
		CellStyle cStyle = wb.createCellStyle();//建立新的cell样式
		cStyle.setFillForegroundColor(HSSFColor.WHITE.index);
		cStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		cStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		cStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		Font font = wb.createFont();//设置excel字体
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setFontName("微软雅黑");
		font.setFontHeightInPoints((short)10);
		cStyle.setFont(font);
		cStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		//首次设置表头列表
		String[] headTitle = title;
		for (int j = 0; j < headTitle.length; j++) {
			Cell cell = row.createCell(j);//建立新cell
			sheet.autoSizeColumn(j, true);
			cell.setCellStyle(cStyle);
			cell.setCellValue(new HSSFRichTextString(headTitle[j]));//设置cell的字符类型的值
		}
		CellStyle cStyle2 = wb.createCellStyle();//建立新的cell样式
		cStyle2.setFillForegroundColor(HSSFColor.WHITE.index);
		cStyle2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cStyle2.setBorderTop(HSSFCellStyle.BORDER_THIN);
		cStyle2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		cStyle2.setBorderLeft(CellStyle.BORDER_THIN);
		cStyle2.setBorderRight(HSSFCellStyle.BORDER_THIN);
		cStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER_SELECTION);
		cStyle2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		Font font2 = wb.createFont();//设置excel字体
		font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
		font2.setFontName("宋体");
		cStyle2.setFont(font2);
		cStyle2.setWrapText(true);
		Row row2 = null;
		Cell cell2 = null;
		//设置列表数据
		if(list!=null&&list.size()>0){
			for (Map<String, Object> l : list) { 
				row2 = sheet.createRow(i++);
				if(i%10001==0){/*2013&2007版excel最大行为:1048576。2003版excel最大行为:65536、设置数据达到6万条换一个Sheet.现设置1万换*/
					i = 0;
					sheet = wb.createSheet(sheetName+createSheetName(sheetIndex++));
					row = sheet.createRow(i++);
					row.setHeight((short)400);
					//换一个Sheet设置表头列表
					for (int j = 0; j < headTitle.length; j++) {
						Cell cell=row.createCell(j);
						sheet.autoSizeColumn(j, true);
						cell.setCellStyle(cStyle);
						cell.setCellValue(new HSSFRichTextString(headTitle[j]));
					}
				}
				//设置单元格数据
				Format dateFormat=new SimpleDateFormat("yyyy-MM-dd");
				for (int j = 0; j < headTitle.length; j++) {
					cell2 = row2.createCell(j);
					if(i<10){
						sheet.autoSizeColumn(j, true);
					}
					cell2.setCellStyle(cStyle2);
					Object vObject=l.get(param[j]);
					if (vObject instanceof BigDecimal) { //保留两位小数
						BigDecimal decimal=new BigDecimal(String.valueOf(vObject));
						cell2.setCellValue(decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
						continue;
					}
					if (vObject instanceof Double) { //保留两位小数
						BigDecimal decimal=new BigDecimal(String.valueOf(vObject));
						cell2.setCellValue(decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
						continue;
					}
					if (vObject instanceof Date) {   //时间，去掉后面的0，比如：２０１５－１０－８　００：００：００.０　转为：２０１５－１０－８
						if(((Date)vObject).getTime()%(1000*60*60)==0){
							cell2.setCellValue(dateFormat.format(vObject));
							continue;
						}
					}
					cell2.setCellValue(new HSSFRichTextString(l.get(param[j])==null?"":l.get(param[j])+""));	
				}
			}
		}
	}
	
	public MetaParameter getParameter() {
		return param;
	}
	public void setParameter(MetaParameter parameter) {
		this.param = parameter;
	}
}
