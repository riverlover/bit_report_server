package zhenhe.li.report.birt.task.rest;
/**
 * 提交报表任务时的响应类
 * @author lizhenhe
 *
 */
public class SubmitResponse {
	//响应码
	private String respCode;
	//响应信息
	private String respMsg;
	
	public String getRespCode() {
		return respCode;
	}
	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}
	public String getRespMsg() {
		return respMsg;
	}
	public void setRespMsg(String respMsg) {
		this.respMsg = respMsg;
	}
}
