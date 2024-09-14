package com.haoyong.sales.common.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.sf.mily.bean.PropertyNotFoundException;
import net.sf.mily.exception.MilyException;
import net.sf.mily.exui.filetransfer.Download;
import net.sf.mily.exui.filetransfer.DownloadProvider;
import net.sf.mily.mappings.MetaParameter;
import net.sf.mily.support.form.Formable;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.event.ActionChain;
import net.sf.mily.webObject.event.FieldActionEvent;
import net.sf.mily.webObject.event.FieldActionListener;
import net.sf.mily.webObject.facable.SqlListBuilderable;
import net.sf.mily.webObject.query.ColumnField;
import net.sf.mily.webObject.query.SqlQuery;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;


/**
 * 获取查询列表的全部查询结果
 *
 *view-name为指定要导出的表格名，如果有多个值用逗号分隔
 */
public class SelectQueryresultZipListener implements FieldActionListener {
	
	private static final String CHARSET_GB2312="GB2312";
	private MetaParameter param;

	public void actionPerformed(FieldActionEvent event, ActionChain chain) {
		String viewName = param.getString(ParameterName.View_Name, event.getField().getFieldBuilder().getName());
		String formerGetter = param.getString(ParameterName.Former);
		String formerSetter = param.getString(ParameterName.Setter);
		List<String> nameList = Arrays.asList(viewName.split(","));
		Component fComp = event.getField().getComponent();
		Exception error = null;
		boolean hasForm=false;
		
		File tempFile=null;
		FileOutputStream out=null;
		ZipOutputStream zipOut=null;
		try {
			tempFile = File.createTempFile("lxdzip"+System.nanoTime(), "zip");
			tempFile.deleteOnExit();
			out=new FileOutputStream(tempFile);
			zipOut=new ZipOutputStream(out);
			
			for (Iterator<ListView> iter = fComp.getParent().getInnerFormerList(ListView.class).iterator(); iter.hasNext();) {
				ListView listview = iter.next();
				try {
					if (nameList.contains(listview.getViewBuilder().getName())) {
						if (null!=formerSetSelected(zipOut,fComp, listview, formerGetter,formerSetter)) {
							hasForm=true;
						}
					}					
				} catch (Exception e) {
					error = e;
				}
			}
		} catch (IOException e1) {
			LogUtil.error(e1);
		}finally{
			if (zipOut!=null) {
				try {
					zipOut.close();
				} catch (IOException e) {
					LogUtil.error(e);
				}
			}
		}
		
		//输出
		Download download=new Download();
		download.setProvider(new ZipProviderDownloader(null,tempFile));
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
	
	private static class ZipProviderDownloader implements DownloadProvider {
		private String defaultName=null;
		private File file;
		public  ZipProviderDownloader(String filename,File zipfile) {
			this.defaultName=filename;
			this.file=zipfile;
		}
		@Override
		public String getContentType() {
			return " application/x-zip";
		}
		@Override
		public String getFileName() {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.set(Calendar.MONTH, c.get(Calendar.MONTH)+1);
			StringBuffer sb=new StringBuffer();
			sb.append(c.get(Calendar.YEAR));
			sb.append(c.get(Calendar.MONTH));
			sb.append(c.get(Calendar.DATE));
			sb.append(c.get(Calendar.HOUR_OF_DAY));
			sb.append(c.get(Calendar.MINUTE));
			sb.append(c.get(Calendar.SECOND));
			if(StringUtils.isEmpty(defaultName)){
				defaultName=sb.toString();
				return defaultName+".zip";
			}else{
				return defaultName+".zip";
			}
		}
		@Override
		public int getSize() {
			return Long.valueOf(file.length()).intValue();
		}
		@Override
		public void writeFile(OutputStream out) throws IOException {	
			FileInputStream inputStream=new FileInputStream(file);
			byte buf[]=new byte[1024];
			int readed=-1;
			while((readed=inputStream.read(buf))!=-1){
				out.write(buf, 0, readed);
			}
			inputStream.close();
			out.close();
			file.delete();
		}		
	}
	
	private Formable formerSetSelected(ZipOutputStream zipOut, Component fComp, ListView listview,
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
					setSelectedList(zipOut,form, formerSetter, listview);
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

	private void setSelectedList(ZipOutputStream zipOut,Formable form, String formerSetter, ListView listview) throws IOException {
		List<ColumnField> builderList =null;
		if (listview.getListBuilder() instanceof SqlListBuilder) {
			SqlListBuilder sqlListBuilder = (SqlListBuilder)listview.getListBuilder();
			builderList = sqlListBuilder.getSqlQuery().getFields().getFields();
		}
		//分页获取写入zip；获取完所有数据之后，再把服务器zip文件写到客户端；
		SqlListBuilder localSqlListBuilder = (SqlListBuilder)listview.getListBuilder();
		SqlListBuilderable sqlListBuilderable=	(SqlListBuilderable)localSqlListBuilder;
		SqlQuery sqlQuery=localSqlListBuilder.getSqlQuery();
		sqlQuery.setPageSize(10000);//最多1万条
		long pageCount=sqlQuery.getPageCount();
		
		//sheet名称
		String label = listview.getFieldBuilder().getLabel();
		if (StringUtils.isEmpty(label)) {
			label = String.valueOf(System.currentTimeMillis());
		}
		
		//声明数组对象 
		String[] title = new String[builderList.size()];// 表头
		String[] param = new String[builderList.size()];// 参数列表
		// 给：表头,参数列表,数组赋值
		for (int i = 0; i < builderList.size(); i++) {
			ColumnField column = (ColumnField) builderList.get(i);
			String labelName = column.getLabel();
			String entityName = column.getId();
			title[i] = labelName;
			param[i] = entityName;
		}
		
		//大数据，多页，zip
		for(int i=0;i<pageCount;i++){
			List selectedList=sqlQuery.toPage(i+1);
			//导出csv
			createCSV(zipOut,label+createSheetName(i)+".csv",title,selectedList);
		}
	}
	
	
	
	/*--------------------------已下是导出Excel封装 method------------------------------*/
	private void createCSV(ZipOutputStream zipOut,String sheetName,String[] headers,List<List<Object>>datas){
		File tempCsv=null;
		try {
			tempCsv = File.createTempFile("csvlxd", null);
			tempCsv.deleteOnExit();
			
			commonCsv(tempCsv,headers,datas);
			
			zipOut.putNextEntry(new ZipEntry(sheetName));
			FileInputStream inputStream=new FileInputStream(tempCsv);
			byte[]buff=new byte[1024];
			int readed=-1;
			while((readed=inputStream.read(buff))!=-1){
				zipOut.write(buff, 0, readed);
			}
			zipOut.closeEntry();
			inputStream.close();
			tempCsv.delete();
		} catch (IOException e) {
			LogUtil.error(e);
		}
	}
	private void commonCsv(File outfile,String headers[],List<List<Object>>datas) throws IOException{
		OutputStreamWriter print=new OutputStreamWriter(new FileOutputStream(outfile), CHARSET_GB2312);
		CSVPrinter printer=CSVFormat.EXCEL.withHeader(headers).print(print);
		for (Iterator<List<Object>> iterator = datas.iterator(); iterator.hasNext();) {
			List<Object> object = iterator.next();
			formatRow(object);
			printer.printRecord(object);
		}
		printer.close();
		print.close();
	}
	private void formatRow(List<Object>row){
		//设置单元格数据
		Format dateFormat=new SimpleDateFormat("yyyy-MM-dd");
		Format dateTimeFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Format timeFormat=new SimpleDateFormat("HH:mm");
		Calendar calendar=Calendar.getInstance();
		
		for (int j = row.size()-1; j>=0; j--) {
			Object vObject =  row.get(j);
			if (vObject instanceof BigDecimal) { //保留两位小数
				BigDecimal decimal=new BigDecimal(String.valueOf(vObject));
				row.set(j, decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
				continue;
			}
			if (vObject instanceof Double) { //保留两位小数
				BigDecimal decimal=new BigDecimal(String.valueOf(vObject));
				row.set(j,decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
				continue;
			}
			if (vObject instanceof Date) {   //时间
				Date date=(Date) vObject;
				calendar.setTime(date);
				if (calendar.get(Calendar.YEAR)==1970
					&&	calendar.get(Calendar.MONTH)==Calendar.JANUARY
					&&	calendar.get(Calendar.DAY_OF_MONTH)==1 
						) {  //去掉  1970-01-01
					row.set(j,timeFormat.format(date));
					continue;
				} else if(((Date)vObject).getTime()%(1000*60*60)==0){ //去掉后面的0，比如：２０１５－１０－８　００：００：００.０　转为：２０１５－１０－８
					row.set(j,dateFormat.format(vObject));
					continue;
				}else{
					row.set(j,dateTimeFormat.format(vObject));
				}
			}
		}
	}
	
	
	
	/**
	 * 
	 * @param sheetIndex 0第一页
	 * @return
	 */
	private String createSheetName(int sheetIndex){
		if(sheetIndex<0){
			return "";
		}
		sheetIndex=sheetIndex*10000;
		return "{ "+sheetIndex+"~"+(sheetIndex+10000)+" }";
	}
	
	
	/********************getter or setter****************************/
	public MetaParameter getParameter() {
		return param;
	}

	public void setParameter(MetaParameter parameter) {
		this.param = parameter;
	}
}
