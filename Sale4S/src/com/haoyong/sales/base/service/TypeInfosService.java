package com.haoyong.sales.base.service;

import java.util.List;

import net.sf.mily.bus.annotation.ActionService;

import com.haoyong.sales.base.domain.TInfo;
import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.base.logic.TypeInfosLogic;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;

/**
 * 分类库Service
 */
@ActionService
public class TypeInfosService {

	@MatchActions({ActionEnum.TypeInfos_Save})
	public <T extends TInfo> void save(ViewData<TypeInfos> viewData) {
		TypeInfos domain = viewData.getFirst();
		int id = 0;
		for (TInfo item: domain.getInfoList()) {
			item.setId(++id);
		}
		new BaseDAO().saveOrUpdate(domain);
	}

	@MatchActions({ActionEnum.TypeInfos_ItemSave})
	public <T extends TInfo> void itemSave(ViewData<TypeInfos> viewData) {
		TypeInfos domain = viewData.getFirst();
		T info = (T)viewData.getParam("info");
		saveInfo(info);
	}

	@MatchActions({ActionEnum.TypeInfos_ItemDelete})
	public <T extends TInfo> void itemDelete(ViewData<TypeInfos> viewData) {
		TypeInfos domain = viewData.getFirst();
		T info = (T)viewData.getParam("info");
		deleteInfo(info);
	}

	private <T extends TInfo> void saveInfo(T info) {
		TypeInfosLogic logic = new TypeInfosLogic();
		Class<T> clss = (Class<T>)info.getClass();
		List<T> infoList = logic.getInfos(clss).getInfoList();
		if (info.getId() > 0) {
			for (int i=infoList.size(); i-->0;) {
				T t = infoList.get(i);
				if (t.getId() == info.getId()) {
					infoList.set(i, info);
					break;
				}
			}
		} else {
			if (infoList.isEmpty()) {
				info.setId(1);
			} else {
				info.setId(infoList.get(infoList.size()-1).getId()+1);
			}
			infoList.add(info);
		}
		save(clss, infoList);
	}
	
	private <T extends TInfo> void deleteInfo(T info) {
		TypeInfosLogic logic = new TypeInfosLogic();
		Class<T> clss = (Class<T>)info.getClass();
		List<T> infoList = logic.getInfos(clss).getInfoList();
		for (int i=infoList.size(); i-->0;) {
			T type = infoList.get(i);
			if (type.getId()==info.getId()) {
				infoList.remove(i);
				break;
			}
		}
		save((Class<T>)info.getClass(), infoList);
	}
	
	private <T extends TInfo> void save(Class<T> clss, List<T> infoList) {
		TypeInfosLogic logic = new TypeInfosLogic();
		TypeInfos infos = logic.getInfos(clss);
		infos.setInfoList(infoList);
		new BaseDAO().saveOrUpdate(infos);
	}
}
