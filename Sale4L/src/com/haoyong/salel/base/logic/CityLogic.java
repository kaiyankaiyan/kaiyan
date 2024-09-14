package com.haoyong.salel.base.logic;

import java.util.List;

import com.haoyong.salel.base.domain.City;
import com.haoyong.salel.base.domain.Province;
import com.haoyong.salel.common.dao.BaseDAO;
/**
 * 查询支持类——城市
 *
 */
public class CityLogic {
	
	public List<City> getAll(Province province){
		return new BaseDAO().nativeQuery("select t.* from bs_City t where t.provinceId=?", City.class, province.getId());
	}
}
