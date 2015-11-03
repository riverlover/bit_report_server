package zhenhe.li.report.birt.task.param;

import zhenhe.li.common.page.Page;
import zhenhe.li.report.birt.task.entity.ReportTask;

public class ReportTaskParam {
	//分页对象
	private Page<ReportTask> page = new Page<ReportTask>();
	//报表任务名称
	private String task_name;
	//任务创建开始时间
	private String start_date;
	//任务创建截止时间
	private String end_date;
	public String getTask_name() {
		return task_name;
	}
	public void setTask_name(String task_name) {
		this.task_name = task_name;
	}
	public String getStart_date() {
		return start_date;
	}
	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}
	public String getEnd_date() {
		return end_date;
	}
	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}
	public Page<ReportTask> getPage() {
		return page;
	}
	public void setPage(Page<ReportTask> page) {
		this.page = page;
	}
	
}
