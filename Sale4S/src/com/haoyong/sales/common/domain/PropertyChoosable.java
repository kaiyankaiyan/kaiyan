package com.haoyong.sales.common.domain;

import java.util.List;


public interface PropertyChoosable<D extends Object> {
	
	public static String SplitAppend="¸", SplitRegex="\\¸";

	public List<String> getTrunkDefault();
}
