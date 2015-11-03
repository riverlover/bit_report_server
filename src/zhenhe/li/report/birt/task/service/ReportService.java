package zhenhe.li.report.birt.task.service;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import zhenhe.li.common.dao.HibernateDao;
import zhenhe.li.report.birt.task.entity.ReportTask;
import zhenhe.li.report.birt.task.param.ReportTaskParam;
import zhenhe.li.report.birt.task.pojo.ReportTaskPojo;
@Service("ReportService")
public class ReportService {
	private final static Logger logger = LoggerFactory.getLogger(ReportService.class);
	
	@Resource
	private HibernateDao<ReportTask,String> reportTaskDao;
	
	/**
	 * 根据查询条件，分页查询报表任务列表
	 * @param param 参考对象，包含查询条件，分页条件，分页查询结果
	 * @throws Exception
	 */
	public void query(ReportTaskParam param){
		
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(ReportTask.class);
		detachedCriteria.setFetchMode("reportTaskProps", FetchMode.LAZY);
		String taskName = param.getTask_name();
		String startDate=param.getStart_date();
		String endDate=param.getEnd_date();
		
		SimpleDateFormat sim=new SimpleDateFormat("yyyyMMdd");
		if(StringUtils.isNotEmpty(startDate)){
				Date sDate;
				try {
					sDate = sim.parse(startDate);
					detachedCriteria.add(Restrictions.ge("create_dt",sDate));
				} catch (ParseException e) {
					logger.warn("时间格式化有误，请检查时间格式");
				}
				
		}
		if(StringUtils.isNotEmpty(endDate)){
			Date eDate;
			try {
				eDate = sim.parse(endDate);
				detachedCriteria.add(Restrictions.le("create_dt", eDate));
			} catch (ParseException e) {
				logger.warn("时间格式化有误，请检查时间格式");
			}
			
		}
		if(StringUtils.isNotEmpty(taskName)){
			detachedCriteria.add(Restrictions.like("task_name",taskName,MatchMode.ANYWHERE));
		}
		
		//投影查询
		ProjectionList pList = Projections.projectionList();
		//队列编号
		pList.add(Projections.property("queue_no").as("queue_no"));
		//提交时间
		pList.add(Projections.property("create_dt").as("create_dt"));
		//报表名称
		pList.add(Projections.property("task_name").as("task_name"));
		//处理状态
		pList.add(Projections.property("state").as("state"));
		//失败原因
		pList.add(Projections.property("failure_reason").as("failure_reason"));
		//下载链接
		pList.add(Projections.property("downloadfile").as("downloadfile"));
		
	
		detachedCriteria.setProjection(pList);
		
		reportTaskDao.findPageByDetachedCriteriaProjection(detachedCriteria, param.getPage(), ReportTaskPojo.class);
		
		
	}

}
