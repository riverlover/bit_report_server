package zhenhe.li.report.birt.task.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity(name="report_task_props")
public class ReportTaskProps {
	@Id
	private String id;
	@Column(name = "k")
	private String key;
	@Column(name = "v")
	private String value;
	@ManyToOne
	@JoinColumn(name="report_task_id",insertable=false,updatable=false)
	@JsonBackReference//避免无限递归解析 json infinite recursion 
	private ReportTask reportTask;
	private String report_task_id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public ReportTask getReportTask() {
		return reportTask;
	}
	public void setReportTask(ReportTask reportTask) {
		this.reportTask = reportTask;
	}
	public String getReport_task_id() {
		return report_task_id;
	}
	public void setReport_task_id(String report_task_id) {
		this.report_task_id = report_task_id;
	}
}
