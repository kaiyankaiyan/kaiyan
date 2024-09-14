package com.haoyong.salel.base.logic;

import java.util.List;

import com.haoyong.salel.base.domain.Province;
import com.haoyong.salel.common.dao.BaseDAO;

/**
 * 查询支持类——省份
 */
public class ProvinceLogic {

	public List<Province> getAll(){
		return new BaseDAO().nativeQuery("select t.* from bs_Province t", Province.class);
	}
	
	public Province getProvince(String name){
		return new BaseDAO().nativeQuerySingleResult("select t.* from bs_Province t where t.name=?", Province.class, name);
	}
	
	public Province getProvinceById(Long provinceId){
		return new BaseDAO().nativeQuerySingleResult("select t.* from bs_Province t where t.id=?", Province.class, provinceId);
	}
}
