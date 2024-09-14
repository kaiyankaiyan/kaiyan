package com.haoyong.sales.sale.service;

import net.sf.mily.bus.annotation.ActionService;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.sale.domain.Question;

/**
 * 提问服务
 */
@ActionService
public class QuestionService {

	@MatchActions({ActionEnum.Question_Save})
	public void save(ViewData<Question> viewData) {
		BaseDAO dao = new BaseDAO();
		for (Question detail: viewData.getTicketDetails()) {
			dao.saveOrUpdate(detail);
		}
	}
}
