package zhenhe.li.report.birt.task.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name="report_task")
public class ReportTask {
	public final static String TASK_PENDING = "00";
	public final static String TASK_INHAND = "01";
	public final static String TASK_COMPLETE = "02";
	public final static String TASK_CANCEL = "03";
	public final static String TASK_FAILURE = "04";
	@Id
	private String id;
	private String queue_no;
	private String task_name;
	private Date create_dt;
	private String schd_crontab_exp;
	private Date close_dt;
	private String state;
	private String initiator_id;
	private String birt_task_id;
	private String rptdesign;
	private String format;
	private String emitterid;
	private String downloadfile;
	private String failure_reason;
	@OneToMany(mappedBy="reportTask",fetch=FetchType.EAGER)
	@JsonIgnore
	private Set<ReportTaskProps> reportTaskProps;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getTask_name() {
		return task_name;
	}
	public void setTask_name(String task_name) {
		this.task_name = task_name;
	}
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
	public String getSchd_crontab_exp() {
		return schd_crontab_exp;
	}
	public void setSchd_crontab_exp(String schd_crontab_exp) {
		this.schd_crontab_exp = schd_crontab_exp;
	}
	public Set<ReportTaskProps> getReportTaskProps() {
		return reportTaskProps;
	}
	public void setReportTaskProps(Set<ReportTaskProps> reportTaskProps) {
		this.reportTaskProps = reportTaskProps;
	}
	public Date getClose_dt() {
		return close_dt;
	}
	public void setClose_dt(Date close_dt) {
		this.close_dt = close_dt;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getInitiator_id() {
		return initiator_id;
	}
	public void setInitiator_id(String initiator_id) {
		this.initiator_id = initiator_id;
	}
	public String getBirt_task_id() {
		return birt_task_id;
	}
	public void setBirt_task_id(String birt_task_id) {
		this.birt_task_id = birt_task_id;
	}

	public String getDownloadfile() {
		return downloadfile;
	}
	public void setDownloadfile(String downloadfile) {
		this.downloadfile = downloadfile;
	}
	public String getFailure_reason() {
		return failure_reason;
	}
	public void setFailure_reason(String failure_reason) {
		this.failure_reason = failure_reason;
	}
	public String getRptdesign() {
		return rptdesign;
	}
	public void setRptdesign(String rptdesign) {
		this.rptdesign = rptdesign;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getEmitterid() {
		return emitterid;
	}
	public void setEmitterid(String emitterid) {
		this.emitterid = emitterid;
	}
}
