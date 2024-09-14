package com.haoyong.sales.common.form;

import net.sf.mily.webObject.IEditViewBuilder;

/**
 * 有界面加载前要调整功能的Form
 */
public interface FViewInitable {

	public void viewinit(IEditViewBuilder viewBuilder);
}
