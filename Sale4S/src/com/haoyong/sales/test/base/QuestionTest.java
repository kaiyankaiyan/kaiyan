package com.haoyong.sales.test.base;

import java.util.Date;

import net.sf.mily.types.DateTimeType;

import org.junit.Assert;

import com.haoyong.sales.sale.form.QuestionForm;
import com.haoyong.sales.util.SSaleUtil;

public class QuestionTest extends AbstractTest<QuestionForm> {
	
	public QuestionTest() {
		this.setForm(new QuestionForm());
	}
	
	protected void check提问__1新增_2编辑_3回复_4关闭(char type, String Option, Object[]... PropVals0_Filters1) {
		QuestionForm form = this.getForm();
		Assert.assertTrue("查看的提问类型", Option!=null);
		this.loadView("QuestionList");
		this.setRadioGroup(Option);
		if ("新增提问".length()>0 && type=='1') {
			this.onMenu("新增");
			Object[] propVals=PropVals0_Filters1[0];
			Assert.assertTrue("有提问属性", propVals.length>0 && propVals.length%2==0);
			for (int ti=0,tsize=propVals.length; ti<tsize; ti+=2) {
				String p=(String)propVals[ti], v=(String)propVals[ti+1];
				this.setFieldText(p, v);
			}
			this.onMenu("提交");
			this.setFilters("idd", form.getDomain().getId());
			Assert.assertTrue("在提问列表", this.getListViewValue().size()==1);
		}
		if ("编辑提问".length()>0 && type=='2') {
			Object[] filters=PropVals0_Filters1[1];
			this.setFilters(filters);
			Assert.assertTrue("有一条可编辑的提问", this.getListViewValue().size()==1);
			this.setSqlListSelect(1);
			this.onMenu("编辑");
			Object[] propVals=PropVals0_Filters1[0];
			Assert.assertTrue("有提问属性", propVals.length>0 && propVals.length%2==0);
			for (int ti=0,tsize=propVals.length; ti<tsize; ti+=2) {
				String p=(String)propVals[ti], v=(String)propVals[ti+1];
				this.setFieldText(p, v);
			}
			this.onMenu("提交");
			this.setFilters("idd", form.getDomain().getId());
			Assert.assertTrue("在提问列表", this.getListViewValue().size()==1);
		}
		if ("回复提问".length()>0 && type=='3') {
			Object[] filters=PropVals0_Filters1[1];
			this.setFilters(filters);
			Assert.assertTrue("有一条可编辑的提问", this.getListViewValue().size()==1);
			this.setSqlListSelect(1);
			this.onMenu("回复");
			Object[] propVals=PropVals0_Filters1[0];
			Assert.assertTrue("有提问属性", propVals.length>0 && propVals.length%2==0);
			for (int ti=0,tsize=propVals.length; ti<tsize; ti+=2) {
				String p=(String)propVals[ti], v=(String)propVals[ti+1];
				this.setFieldText(p, v);
			}
			this.onMenu("已解决提交");
			this.setFilters("idd", form.getDomain().getId());
			Assert.assertTrue("已回复不在提问列表", this.getListViewValue().size()==0);
		}
		if ("关闭提问".length()>0 && type=='4') {
			Object[] filters=PropVals0_Filters1[1];
			this.setFilters(filters);
			Assert.assertTrue("有一条可关闭的提问", this.getListViewValue().size()==1);
			this.setSqlListSelect(1);
			this.onMenu("关闭");
			this.setFilters(filters);
			Assert.assertTrue("已关闭不在提问列表", this.getListViewValue().size()==0);
			if ("在关闭列表".length()>0) {
				this.setRadioGroup("已关闭");
				this.setFilters(filters);
				Assert.assertTrue("在已关闭提问列表", this.getListViewValue().size()==1);
			}
		}
	}
	
	private void test提问() {
		this.setQ清空();
		String title="提问"+new DateTimeType().format(new Date());
		this.check提问__1新增_2编辑_3回复_4关闭('1', "提出", new Object[]{"rightName","test", "title",title, "question","提问内容"});
		this.check提问__1新增_2编辑_3回复_4关闭('2', "提出", new Object[]{"question", "提问内容"+title}, new Object[]{"title", title});
		this.check提问__1新增_2编辑_3回复_4关闭('3', "提出", new Object[]{"reply","回答"}, new Object[]{"title", title});
		this.check提问__1新增_2编辑_3回复_4关闭('4', "提出&解决", new Object[0], new Object[]{"title", title});
	}

	@Override
	protected void setQ清空() {
		String sql = "delete from sa_question where sellerId=?";
		SSaleUtil.executeSqlUpdate(sql);
	}
}
