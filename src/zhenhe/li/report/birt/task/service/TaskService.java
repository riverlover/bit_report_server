package zhenhe.li.report.birt.task.service;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import zhenhe.li.common.dao.HibernateDao;
import zhenhe.li.common.page.Page;
import zhenhe.li.report.birt.task.entity.ReportTask;
import zhenhe.li.report.birt.task.entity.ReportTaskProps;

@Qualifier("taskService")
@Service
public class TaskService {
	private final static Logger logger = LoggerFactory.getLogger(TaskService.class);
	@Resource
	private HibernateDao<ReportTask,String> reportTaskDao;

	private String reportDesignParentPath;
	/**
	 * 获取一个最早提交,待处理的任务
	 * @param fetchNo
	 * @return
	 */
	public List<ReportTask> getToDo(int fetchNo){
		DetachedCriteria topNCriDetachedCriteria = DetachedCriteria.forClass(ReportTask.class);
		topNCriDetachedCriteria.addOrder(Order.asc("create_dt"));
		topNCriDetachedCriteria.add(Restrictions.in("state", new String[]{ReportTask.TASK_PENDING,ReportTask.TASK_INHAND}));
		Page<ReportTask> page = new Page<ReportTask>();
		page.setOrder("ASC");
		page.setSort("create_dt");
		page.setPage(1);
		//每次只取一个任务，这里可以做成配置化的
		page.setRows(fetchNo);
		reportTaskDao.findPageByDetachedCriteriaProjection(topNCriDetachedCriteria, page);
		return page.getData();
	}
	/**
	 * 
	 * @param task 报表任务对象
	 * @param locale 本地语言
	 * @param encoding 编码
	 * @param paramFile 配置文件路径 
	 * @param output 下载路径
	 * @return
	 */
	public String[] parseOptions(ReportTask task,String locale,String encoding,String paramFile,String output){
		List<String> params = new ArrayList<String>();
	
		params.add("--format");
		params.add(task.getFormat());
		params.add("--locale");		
		params.add((StringUtils.isEmpty(locale)?"zh_CN":locale));
		params.add("--encoding");
		params.add((StringUtils.isEmpty("encoding")?"GBK":encoding));
		if(StringUtils.isNotEmpty(paramFile)){
			params.add("--file");
			params.add(paramFile);
		}		
		params.add("--output");
		params.add(output);
		
		Set<ReportTaskProps> reportTaskProps = task.getReportTaskProps();
		for(ReportTaskProps p:reportTaskProps){
			
			if(StringUtils.isNotEmpty(p.getValue())){
				params.add("-p");
				params.add(p.getKey()+"="+p.getValue());
			}
		}
		
		params.add(reportDesignParentPath+task.getRptdesign());
		
		
		String[] args = new String[params.size()];
		for(int i=0;i<params.size();i++){
			args[i]=params.get(i);
		}
		return args;
	}
	public String[] parseOptions(ReportTask task,String output){
		return parseOptions(task, "zh_CN", "GBK", null, output);
	}
	/**
	 * 产生时间前缀的路径
	 * @return
	 * @throws IOException 
	 */
	public String getFilenameWithTimestampPath(String parent,String filename) throws IOException{
			DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			FileUtils.forceMkdir(new File(parent));
			//只返回文件名，不返回父路径，以方便目录迁移，2015/10/28 10:30 am 李真河
			return df.format(new Date())+"_"+filename;
	}
	public void updateTaskState(ReportTask task){
		reportTaskDao.update(task);
	}
	public void clearExpiredTasks(int daysBefore){
		DetachedCriteria topNCriDetachedCriteria = DetachedCriteria.forClass(ReportTask.class);
		topNCriDetachedCriteria.addOrder(Order.asc("create_dt"));
		long before = System.currentTimeMillis() - daysBefore*24*3600*1000;
		topNCriDetachedCriteria.add(Restrictions.le("create_dt", new Date(before)));
		Page<ReportTask> page = new Page<ReportTask>();
		page.setOrder("ASC");
		page.setSort("create_dt");
		page.setPage(1);
		page.setRows(100);
		reportTaskDao.findPageByDetachedCriteriaProjection(topNCriDetachedCriteria, page);
		List<ReportTask> reportTasks = page.getData();
		for(ReportTask t:reportTasks){
			reportTaskDao.delete(t);
			logger.debug("任务【"+t.getQueue_no()+"-"+t.getTask_name()+"】已过期，已从数据库表中清理");
			try {
				if(t.getDownloadfile() == null)
					continue;
				File f = new File(t.getDownloadfile());
				if(f.exists()){
					FileUtils.forceDelete(f);
					logger.debug("清理过期下载任务目标文件"+f.getAbsolutePath()+"成功");
				}
			} catch (IOException e) {
				logger.error("清理过期的下载文件失败",e);
			}
		}
	}
	public HibernateDao<ReportTask, String> getReportTaskDao() {
		return reportTaskDao;
	}
	public void setReportTaskDao(HibernateDao<ReportTask, String> reportTaskDao) {
		this.reportTaskDao = reportTaskDao;
	}
	public static void main(String[] args) {
		System.out.println(File.separator);
	}
	public String getReportDesignParentPath() {
		return reportDesignParentPath;
	}
	public void setReportDesignParentPath(String reportDesignParentPath) {
		this.reportDesignParentPath = reportDesignParentPath;
	}
	
}
