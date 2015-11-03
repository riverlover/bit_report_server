package zhenhe.li.report.birt.task.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 报表设计文件
 * @author lizhenhe
 *
 */
@Table(name = "report_design_file")
@Entity
public class ReportDesignFile implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7891846236328178855L;
	//报表设计文件ID
	@Id
	private String design_file_id;
	//报表设计文件路径
	private String design_file_path;
	//说明
	private String comment;
	//操作员
	private String operator;
	//创建时间
	private Date create_dt;
	public String getDesign_file_id() {
		return design_file_id;
	}
	public void setDesign_file_id(String design_file_id) {
		this.design_file_id = design_file_id;
	}

	public String getDesign_file_path() {
		return design_file_path;
	}
	public void setDesign_file_path(String design_file_path) {
		this.design_file_path = design_file_path;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public Date getCreate_dt() {
		return create_dt;
	}
	public void setCreate_dt(Date create_dt) {
		this.create_dt = create_dt;
	}
	
}
