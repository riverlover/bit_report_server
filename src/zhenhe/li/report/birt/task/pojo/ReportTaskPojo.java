package zhenhe.li.report.birt.task.pojo;

import java.util.Date;

/**
 * 报表任务对象，向查询客户端输出投影结果时的封装类
 * @author lizhenhe
 *
 */
public class ReportTaskPojo {
	//任务队列编号
	private String queue_no;
	//任务创建时间
	private Date create_dt;
	//任务名称
	private String task_name;
	//文件类型
	private String format;
	//任务状态
	private String state;
	//失败原因
	private String failure_reason;
	//下载链接
	private String downloadfile;
	public String getQueue_no() {
		return queue_no;
	}
	public void setQueue_no(String queue_no) {
		this.queue_no = queue_no;
	}
	public Date getCreate_dt() {
		return create_dt;
	}
	public void setCreate_dt(Date create_dt) {
		this.create_dt = create_dt;
	}
	public String getTask_name() {
		return task_name;
	}
	public void setTask_name(String task_name) {
		this.task_name = task_name;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getFailure_reason() {
		return failure_reason;
	}
	public void setFailure_reason(String failure_reason) {
		this.failure_reason = failure_reason;
	}
	public String getDownloadfile() {
		return downloadfile;
	}
	public void setDownloadfile(String downloadfile) {
		this.downloadfile = downloadfile;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
}
