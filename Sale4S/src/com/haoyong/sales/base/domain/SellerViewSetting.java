package com.haoyong.sales.base.domain;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.common.domain.AbstractDomain;
import com.haoyong.sales.common.domain.PropertyChoosable;

/**
 * 商家ViewBuilder视图配置
 */
@Entity
@Table(name = "bs_sellerviewsetting")
public class SellerViewSetting extends AbstractDomain {

	private String builderName;
	
	private List<String> titleList=new ArrayList<String>();
	private List<String> requireList=new ArrayList<String>();
	private List<String> chooseList=new ArrayList<String>();
	private List<String> trunkList=new ArrayList<String>();
	// 属性编辑框fieldId, inputType
	private Map<String, String> inputMap = new LinkedHashMap<String, String>(1);
	// 属性重命名fieldId, relabel
	private Map<String, String> renameMap = new LinkedHashMap<String, String>(1);
	// 属性多选项fieldId
	private Map<String, List<String>> selectMap = new LinkedHashMap<String, List<String>>(1);
	
	public String getBuilderName() {
		return builderName;
	}

	public void setBuilderName(String builderName) {
		this.builderName = builderName;
	}
	
	public String getTitle() {
		Gson gson = new Gson();
		Type gtype = new TypeToken<ArrayList<String>>(){}.getType();
		return gson.toJson(getTitleList(), gtype);
	}
	
	private void setTitle(String stitle) {
		if (stitle==null)			return;
		Gson gson = new Gson();
		Type gtype = new TypeToken<ArrayList<String>>(){}.getType();
		List<String> list = gson.fromJson(stitle, gtype);
		this.setTitleList(list);
	}

	@Transient
	public List<String> getTitleList() {
		return this.titleList;
	}

	public void setTitleList(List<String> titleList) {
		this.titleList.clear();
		if (titleList!=null)
			this.titleList.addAll(titleList);
	}
	
	public String getRquire() {
		Gson gson = new Gson();
		Type gtype = new TypeToken<ArrayList<String>>(){}.getType();
		return gson.toJson(getRequireList(), gtype);
	}
	
	private void setRquire(String srequire) {
		if (srequire==null)			return;
		Gson gson = new Gson();
		Type gtype = new TypeToken<ArrayList<String>>(){}.getType();
		List<String> list = gson.fromJson(srequire, gtype);
		this.setRequireList(list);
	}

	@Transient
	public List<String> getRequireList() {
		return this.requireList;
	}

	public void setRequireList(List<String> requireList) {
		this.requireList.clear();
		if (requireList!=null)
			this.requireList.addAll(requireList);
	}
	
	public String getChoose() {
		Gson gson = new Gson();
		Type gtype = new TypeToken<ArrayList<String>>(){}.getType();
		return gson.toJson(getChooseList(), gtype);
	}
	
	private void setChoose(String schoose) {
		if (schoose==null)			return;
		Gson gson = new Gson();
		Type gtype = new TypeToken<ArrayList<String>>(){}.getType();
		List<String> list = gson.fromJson(schoose, gtype);
		this.setChooseList(list);
	}

	@Transient
	public List<String> getChooseList() {
		return this.chooseList;
	}

	public void setChooseList(List<String> chooseList) {
		this.chooseList.clear();
		if (chooseList!=null)
			this.chooseList.addAll(chooseList);
	}
	
	public String getMInput() {
		Gson gson = new Gson();
		Type gtype = new TypeToken<LinkedHashMap<String,String>>(){}.getType();
		return this.getInputMap().isEmpty()? null: gson.toJson(this.getInputMap(), gtype);
	}
	
	private void setMInput(String minput) {
		if (StringUtils.isBlank(minput))
			return;
		Gson gson = new Gson();
		Type gtype = new TypeToken<LinkedHashMap<String,String>>(){}.getType();
		LinkedHashMap<String, String> map = gson.fromJson(minput, gtype);
		this.setInputMap(map);
	}

	@Transient
	public Map<String, String> getInputMap() {
		return inputMap;
	}

	public void setInputMap(Map<String, String> inputMap) {
		this.inputMap.clear();
		if (inputMap!=null)
			this.inputMap.putAll(inputMap);
	}
	
	public String getMRename() {
		Gson gson = new Gson();
		Type gtype = new TypeToken<LinkedHashMap<String,String>>(){}.getType();
		return this.getRenameMap().isEmpty()? null: gson.toJson(this.getRenameMap(), gtype);
	}
	
	private void setMRename(String mrename) {
		if (StringUtils.isBlank(mrename))
			return;
		Gson gson = new Gson();
		Type gtype = new TypeToken<LinkedHashMap<String,String>>(){}.getType();
		LinkedHashMap<String, String> map = gson.fromJson(mrename, gtype);
		this.setRenameMap(map);
	}

	@Transient
	public Map<String, String> getRenameMap() {
		return renameMap;
	}

	public void setRenameMap(Map<String, String> renameMap) {
		this.renameMap.clear();
		if (renameMap!=null)
			this.renameMap.putAll(renameMap);
	}
	
	public String getMSelect() {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		for (Map.Entry<String, List<String>> foptions: this.getSelectMap().entrySet()) {
			StringBuffer sb = new StringBuffer();
			for (Iterator<String> oiter=foptions.getValue().iterator(); oiter.hasNext();) {
				sb.append(oiter.next()).append(PropertyChoosable.SplitAppend);
			}
			if (sb.length()>0) {
				map.put(foptions.getKey(), sb.deleteCharAt(sb.length()-1).toString());
			}
		}
		if (map.isEmpty())
			return null;
		Gson gson = new Gson();
		Type gtype = new TypeToken<LinkedHashMap<String,String>>(){}.getType();
		return gson.toJson(map, gtype);
	}
	
	private void setMSelect(String mselect) {
		if (StringUtils.isBlank(mselect))
			return;
		Gson gson = new Gson();
		Type gtype = new TypeToken<LinkedHashMap<String,String>>(){}.getType();
		LinkedHashMap<String,String> frommap = gson.fromJson(mselect, gtype);
		LinkedHashMap<String, List<String>> tomap = new LinkedHashMap<String, List<String>>();
		for (Map.Entry<String, String> foptions: frommap.entrySet()) {
			List<String> list = new ArrayList<String>(Arrays.asList(foptions.getValue().split(PropertyChoosable.SplitRegex)));
			tomap.put(foptions.getKey(), list);
		}
		this.setSelectMap(tomap);
	}

	@Transient
	public Map<String, List<String>> getSelectMap() {
		return selectMap;
	}

	public void setSelectMap(Map<String, List<String>> selectMap) {
		this.selectMap.clear();
		if (selectMap!=null)
			this.selectMap.putAll(selectMap);
	}

	public void setCfgRequire(ViewBuilder vbuilder) {
		List<FieldBuilder> sourceList = vbuilder.getFieldBuilderLeafs();
		for (FieldBuilder item: sourceList) {
			if (StringUtils.equals("Require", item.getParameter(ParameterName.Cfg).getString(ParameterName.Type))==false)
				continue;
			if (this.getRequireList().contains(item.getName())==false)
				this.getRequireList().add(item.getName());
			if (this.getChooseList().contains(item.getName())==false)
				this.getChooseList().add(item.getName());
			if (this.getTrunkList().contains(item.getName())==false)
				this.getTrunkList().add(item.getName());
		}
	}
	
	@Transient
	public List<FieldBuilder> getRequireList(List<FieldBuilder> sourceList) {
		List<FieldBuilder> list = new ArrayList<FieldBuilder>();
		for (int i=getRequireList().size(); i-->0;) {
			list.add(null);
		}
		for (FieldBuilder item: sourceList) {
			int idx = getRequireList().indexOf(item.getName());
			if (idx > -1) {
				list.set(idx, item);
			}
		}
		for (Iterator<FieldBuilder> iter=list.iterator(); iter.hasNext();) {
			if (iter.next()==null)		iter.remove();
		}
		return list;
	}
	
	@Transient
	public List<FieldBuilder> getChooseList(List<FieldBuilder> sourceList) {
		List<FieldBuilder> list = new ArrayList<FieldBuilder>();
		if (getChooseList().size()==0)			return sourceList;
		for (int i=getChooseList().size(); i-->0;) {
			list.add(null);
		}
		for (FieldBuilder item: sourceList) {
			int idx = getChooseList().indexOf(item.getName());
			if (idx > -1)
				list.set(idx, item);
		}
		for (Iterator<FieldBuilder> iter=list.iterator(); iter.hasNext();) {
			if (iter.next()==null)
				iter.remove();
		}
		return list;
	}
	
	@Transient
	public List<FieldBuilder> getTrunkList(List<FieldBuilder> sourceList) {
		List<FieldBuilder> list = new ArrayList<FieldBuilder>();
		for (int i=getTrunkList().size(); i-->0;) {
			list.add(null);
		}
		for (FieldBuilder item: sourceList) {
			int idx = getTrunkList().indexOf(item.getName());
			if (idx > -1) {
				list.set(idx, item);
			}
		}
		for (Iterator<FieldBuilder> iter=list.iterator(); iter.hasNext();) {
			FieldBuilder item = iter.next();
			if (item==null)			iter.remove();
		}
		return list;
	}
	
	@Transient
	public List<FieldBuilder> getTitleList(List<FieldBuilder> sourceList) {
		List<FieldBuilder> list = new ArrayList<FieldBuilder>();
		for (int i=getTitleList().size(); i-->0;) {
			list.add(null);
		}
		for (FieldBuilder item: sourceList) {
			int idx = getTitleList().indexOf(item.getName());
			if (idx > -1) {
				list.set(idx, item);
			}
		}
		for (Iterator<FieldBuilder> iter=list.iterator(); iter.hasNext();) {
			FieldBuilder item = iter.next();
			if (item==null)			iter.remove();
		}
		return list;
	}
	
	public String getTrunk() {
		Gson gson = new Gson();
		Type gtype = new TypeToken<ArrayList<String>>(){}.getType();
		return gson.toJson(getTrunkList(), gtype);
	}
	
	private void setTrunk(String strunk) {
		if (strunk==null)			return;
		Gson gson = new Gson();
		Type gtype = new TypeToken<ArrayList<String>>(){}.getType();
		List<String> list = gson.fromJson(strunk, gtype);
		this.setTrunkList(list);
	}

	@Transient
	public List<String> getTrunkList() {
		return this.trunkList;
	}

	public void setTrunkList(List<String> thrunkList) {
		this.trunkList.clear();
		if (thrunkList!=null)
			this.trunkList.addAll(thrunkList);
	}
}
