package zhenhe.li.report.birt.task.rest;

import java.util.Map;

/**
 * 提交任务的请求类
 * @author lizhenhe
 *
 */
public class SubmitRequest {
	//报表标识，用于关联用于生成报表文件的birt 设计文件
	private String reportId;
	//报表文件名
	private String filename;
	//报表格式 
	private String format;
	//附加属性，使用map动态扩展可以添加的,
	//比如可以提交与数据权限相关的机构编号inst_id、操作员编号operator_id、来源系统标识system_id等等
	private Map attaches;
	
	public String getReportId() {
		return reportId;
	}
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public Map getAttaches() {
		return attaches;
	}
	public void setAttaches(Map attaches) {
		this.attaches = attaches;
	}
	
}
