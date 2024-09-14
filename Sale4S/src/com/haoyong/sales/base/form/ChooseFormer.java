package com.haoyong.sales.base.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.ViewBuilder;
import net.sf.mily.webObject.query.SqlColumnSetting;

import org.apache.commons.lang.StringUtils;
import org.hibernate.util.JoinedIterator;

import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.logic.SellerViewSettingLogic;
import com.haoyong.sales.common.form.AbstractForm;

public class ChooseFormer extends AbstractForm<SqlColumnSetting> {
	
	private List<SqlColumnSetting> chooseList, trunkList;
	private List<SqlColumnSetting> selectedList;
	private ViewBuilder viewBuilder;
	private SellerViewSetting viewSetting;
	
	private void saveBase() {
		this.saveSetting(this.chooseList, "choose", "rename", "input", "options");
		this.saveSetting(this.trunkList, "trunk", "require", "title");
		new SellerViewSettingLogic().saveViewSetting(this.viewSetting);
	}
	
	private void saveTicket() {
		this.saveSetting(this.trunkList, "trunk", "title", "require", "rename", "input", "options");
		new SellerViewSettingLogic().saveViewSetting(this.viewSetting);
	}
	
	private void saveSetting(List<SqlColumnSetting> currentList, String... nameList0) {
		SellerViewSetting vsetting = this.getSellerViewSetting();
		List<String> nameList = Arrays.asList(nameList0);
		String key = "trunk";
		if (nameList.contains(key)) {
			List<String> trunkList = new ArrayList<String>();
			for (Iterator<SqlColumnSetting> iter=currentList.iterator(); iter.hasNext();) {
				SqlColumnSetting item = iter.next();
				for (Boolean trunk=(Boolean)item.getVoParamMap().get(key); trunk!=null && trunk.booleanValue()==true; trunk=null) {
					trunkList.add(item.getFieldId());
				}
			}
			vsetting.setTrunkList(trunkList);
		}
		key = "choose";
		if (nameList.contains(key)) {
			List<String> chooseList = new ArrayList<String>();
			for (Iterator<SqlColumnSetting> iter=currentList.iterator(); iter.hasNext();) {
				SqlColumnSetting item = iter.next();
				for (Boolean choose=(Boolean)item.getVoParamMap().get(key); choose!=null && choose.booleanValue()==true; choose=null) {
					chooseList.add(item.getFieldId());
				}
			}
			vsetting.setChooseList(chooseList);
			vsetting.getTrunkList().retainAll(chooseList);
		}
		key = "title";
		if (nameList.contains(key)) {
			List<String> titleList = new ArrayList<String>();
			for (Iterator<SqlColumnSetting> iter=currentList.iterator(); iter.hasNext();) {
				SqlColumnSetting item = iter.next();
				for (Boolean title=(Boolean)item.getVoParamMap().get(key); title!=null && title.booleanValue()==true; title=null) {
					titleList.add(item.getFieldId());
				}
			}
			vsetting.setTitleList(titleList);
			vsetting.getTitleList().retainAll(vsetting.getTrunkList());
		}
		key = "require";
		if (nameList.contains(key)) {
			List<String> requireList = new ArrayList<String>();
			for (Iterator<SqlColumnSetting> iter=currentList.iterator(); iter.hasNext();) {
				SqlColumnSetting item = iter.next();
				for (Boolean rquire=(Boolean)item.getVoParamMap().get(key); rquire!=null && rquire.booleanValue()==true; rquire=null) {
					requireList.add(item.getFieldId());
				}
			}
			vsetting.setRequireList(requireList);
			vsetting.getRequireList().retainAll(vsetting.getTrunkList());
		}
		key = "rename";
		if (nameList.contains(key)) {
			LinkedHashMap<String, String> renameList = new LinkedHashMap<String, String>();
			for (Iterator<SqlColumnSetting> iter=currentList.iterator(); iter.hasNext();) {
				SqlColumnSetting item = iter.next();
				for (String rename=(String)item.getVoParamMap().get(key); StringUtils.isBlank(rename)==false; rename=null) {
					renameList.put(item.getFieldId(), rename.trim());
				}
			}
			vsetting.setRenameMap(renameList);
		}
		key = "input";
		if (nameList.contains(key)) {
			LinkedHashMap<String, String> inputList = new LinkedHashMap<String, String>();
			for (Iterator<SqlColumnSetting> iter=currentList.iterator(); iter.hasNext();) {
				SqlColumnSetting item = iter.next();
				for (String bname=(String)item.getVoParamMap().get(key); StringUtils.isBlank(bname)==false; bname=null) {
					inputList.put(item.getFieldId(), bname);
				}
			}
			vsetting.setInputMap(inputList);
		}
		key = "options";
		if (nameList.contains(key)) {
			LinkedHashMap<String, List<String>> optionsList = new LinkedHashMap<String, List<String>>();
			for (Iterator<SqlColumnSetting> iter=currentList.iterator(); iter.hasNext();) {
				SqlColumnSetting item = iter.next();
				List<String> splitList = new ArrayList<String>();
				for (String options=(String)item.getVoParamMap().get(key); StringUtils.isBlank(options)==false; options=null) {
					splitList.add(options.trim());
					for (String regex: new String[]{"\\,", "\\，"}) {
						for (String split: new ArrayList<String>(splitList)) {
							splitList.remove(split);
							splitList.addAll(Arrays.asList(split.split(regex)));
						}
					}
				}
				if (splitList.size()>0)
					optionsList.put(item.getFieldId(), splitList);
			}
			vsetting.setSelectMap(optionsList);
		}
	}
	
	public void moveUpto() {
		if (!(selectedList!=null || selectedList.size()>1))				return;
		List<SqlColumnSetting> sourceList=null, selectedList=new ArrayList<SqlColumnSetting>(this.selectedList);
		if (trunkList.containsAll(selectedList))			sourceList=trunkList;
		if (chooseList!=null && chooseList.containsAll(selectedList))			sourceList=chooseList;
		SqlColumnSetting first = selectedList.get(0);
		int ifirst=sourceList.indexOf(first);
		selectedList.remove(first);
		sourceList.removeAll(selectedList);
		sourceList.addAll(ifirst, selectedList);
	}
	
	public void moveDownto() {
		if (!(selectedList!=null || selectedList.size()>1))				return;
		List<SqlColumnSetting> sourceList=null, selectedList=new ArrayList<SqlColumnSetting>(this.selectedList);
		if (trunkList.containsAll(selectedList))			sourceList=trunkList;
		if (chooseList!=null && chooseList.containsAll(selectedList))			sourceList=chooseList;
		SqlColumnSetting last = selectedList.get(selectedList.size()-1);
		int ilast=sourceList.indexOf(last);
		selectedList.remove(last);
		sourceList.removeAll(selectedList);
		ilast -= selectedList.size();
		sourceList.addAll(ilast+1, selectedList);
	}
	
	@Override
	public void setSelectedList(List<SqlColumnSetting> selected) {
		this.selectedList = selected;
	}

	@SuppressWarnings("unchecked")
	public List<SqlColumnSetting> getChooseList() {
		if (chooseList==null) {
			ViewBuilder viewBuilder = getViewBuilder();
			List<FieldBuilder> sourceList = viewBuilder.getFieldBuilderLeafs();
			SellerViewSetting viewSetting = getSellerViewSetting();
			List<FieldBuilder> chooseList=(viewSetting.getChooseList().size()==0? new ArrayList<FieldBuilder>(): viewSetting.getChooseList(sourceList));
			List<FieldBuilder> unchooseList=new ArrayList<FieldBuilder>(sourceList);
			unchooseList.removeAll(chooseList);
			this.chooseList = this.getColumnSetting(chooseList, unchooseList);
		}
		return chooseList;
	}

	@SuppressWarnings("unchecked")
	public List<SqlColumnSetting> getTrunkListFromChoose() {
		if (trunkList==null) {
			ViewBuilder viewBuilder = getViewBuilder();
			List<FieldBuilder> sourceList = viewBuilder.getFieldBuilderLeafs();
			SellerViewSetting viewSetting = getSellerViewSetting();
			List<FieldBuilder> chooseList=viewSetting.getChooseList(sourceList);
			List<FieldBuilder> trunkList=viewSetting.getTrunkList(chooseList), untrunkList=new ArrayList(chooseList);
			untrunkList.removeAll(trunkList);
			this.trunkList = this.getColumnSetting(trunkList, untrunkList);
		}
		return trunkList;
	}
	
	@SuppressWarnings("unchecked")
	public List<SqlColumnSetting> getTrunkListFromAll() {
		if (trunkList==null) {
			ViewBuilder viewBuilder = getViewBuilder();
			List<FieldBuilder> sourceList = viewBuilder.getFieldBuilderLeafs();
			SellerViewSetting viewSetting = getSellerViewSetting();
			List<FieldBuilder> trunkList=viewSetting.getTrunkList(sourceList), untrunkList=new ArrayList(sourceList);
			untrunkList.removeAll(trunkList);
			this.trunkList = this.getColumnSetting(trunkList, untrunkList);
		}
		return trunkList;
	}
	
	private List<SqlColumnSetting> getColumnSetting(List<FieldBuilder> yesList, List<FieldBuilder> noList) {
		LinkedHashMap<String, SqlColumnSetting> list=new LinkedHashMap<String,SqlColumnSetting>();
		int i = 1;
		SellerViewSetting vsetting = this.getSellerViewSetting();
		for(Iterator<FieldBuilder> fiter=new JoinedIterator(yesList.iterator(), noList.iterator()); fiter.hasNext();) {
			FieldBuilder field = fiter.next();
			SqlColumnSetting item = new SqlColumnSetting();
			item.setFieldId(field.getName());
			String rename = vsetting.getRenameMap().get(field.getName());
			item.setFieldLabel(rename!=null? rename: field.getLabel());
			item.getVoParamMap().put("choose", vsetting.getChooseList().contains(field.getName()));
			item.getVoParamMap().put("trunk", vsetting.getTrunkList().contains(field.getName()));
			if (vsetting.getTitleList().size()==2)
				"".toCharArray();
			item.getVoParamMap().put("title", vsetting.getTitleList().contains(field.getName()));
			item.getVoParamMap().put("require", vsetting.getRequireList().contains(field.getName()));
			item.getVoParamMap().put("index", i++);
			item.getVoParamMap().put("rename", rename);
			String input = vsetting.getInputMap().get(field.getName());
			item.getVoParamMap().put("input", field.getClass().getSimpleName().equals(input)? null: input);
			StringBuffer sb = new StringBuffer();
			for (List<String> splitList=vsetting.getSelectMap().get(field.getName()); splitList!=null; splitList=null) {
				for (Iterator<String> iter=splitList.iterator(); iter.hasNext(); sb.append(iter.next()).append(","));
			}
			item.getVoParamMap().put("options", sb.length()==0? null: sb.deleteCharAt(sb.length()-1).toString());
			list.put(item.getFieldId(), item);
		}
		return new ArrayList<SqlColumnSetting>(list.values());
	}

	private ViewBuilder getViewBuilder() {
		return this.viewBuilder;
	}
	
	public void setViewBuilder(ViewBuilder viewBuilder) {
		this.viewBuilder = viewBuilder;
	}
	
	private SellerViewSetting getSellerViewSetting() {
		return this.viewSetting;
	}
	
	public void setSellerViewSetting(SellerViewSetting viewSetting) {
		this.viewSetting = viewSetting;
	}
	
	private List<Map.Entry<String, String>> getInputTypeOptions(Object setting) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("文本", "TextFieldBuilder");
		map.put("多行文本", "TextAreaBuilder");
		map.put("日期", "DateBuilder");
		map.put("下拉单选", "SelectListBuilder");
		map.put("组合多选", "CheckBoxGroupBuilder");
		return new ArrayList<Map.Entry<String,String>>(map.entrySet());
	}
}
