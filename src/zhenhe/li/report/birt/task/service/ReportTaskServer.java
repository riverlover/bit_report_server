package zhenhe.li.report.birt.task.service;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import zhenhe.li.common.dao.HibernateDao;
import zhenhe.li.report.birt.extend.BirtReportException;
import zhenhe.li.report.birt.extend.ReportRunner;
import zhenhe.li.report.birt.task.entity.ReportTask;
import zhenhe.li.report.birt.task.entity.ReportTaskProps;

@Component
public class ReportTaskServer extends Thread{
	private final static Logger logger = LoggerFactory.getLogger(ReportTaskServer.class);
	//定义总线程数
	private int maxThreadsNum = 1;
	//任务轮询时间
	private long rollInterval = 10;
	//生成报表的目标目录 
	private String destFilePath = "";
	//报表服务管理端监听IP
	private String managerIP = "10.0.6.2";
	//报表服务管理端监听端口
	private int managerPort = 9123;
	//报表设计文件存放目录
	private String reportDesignParentPath = "F:\\";
	//过期任务清理间隔时间（单位：天）
	private int expiredDays = 10;
	@Resource
	private TaskService taskService;
	@Resource
	private HibernateDao<ReportTaskProps,String> reportTaskPropsDao;
	public void run(){

		//存储报表处理线程的上下文，在取消任务线程中使用
		Map<String, ReportRunner> threadMap = Collections.synchronizedMap( new HashMap<String, ReportRunner>() );
		// 启动框架
		
		//启动取消任务处理线程
		new Thread(new TaskManagerRunner(managerIP, managerPort, threadMap,taskService)).start();
//		logger.info("任务管理服务启动成功，管理地址为："+managerIP+":"+managerPort);
		
		//设定taskService reportDesign父级目录
		if(!reportDesignParentPath.endsWith(File.separator)){
			reportDesignParentPath = reportDesignParentPath + File.separator;
		}
		taskService.setReportDesignParentPath(reportDesignParentPath);
		
		while(true){
			//logger.info("开始清理过期任务！");
			//清理过期任务
			taskService.clearExpiredTasks(expiredDays);
			List<ReportTask> tasks = taskService.getToDo(1);
			if(tasks.size()==0 || threadMap.size() == maxThreadsNum){
				if(tasks.size() == 0){
					logger.debug("暂时没有要处理的任务");
				}else if(threadMap.size() == maxThreadsNum){
					logger.debug("没有空闲任务线程：" +
						"允许总线程数为:"+maxThreadsNum+"正在运行的线程数为："+threadMap.size());
				}
				long sleepInterval = rollInterval*1000;
				logger.debug("暂时没有处理的需休眠"+sleepInterval/1000+"秒钟");
				try {
					Thread.currentThread().sleep(sleepInterval);
				} catch (InterruptedException e) {
					logger.error("线程休眠失败，",e);
				}
				continue;
			}
			ReportTask task = tasks.get(0);
			String filename= null;
			try {
				filename = taskService.getFilenameWithTimestampPath(destFilePath,task
						.getTask_name()+"."+task.getFormat());
				logger.debug("待生成的报表目标文件："+filename);
			} catch (IOException e) {
				logger.error("创建目标报表文件路径失败",e);
				task.setState(ReportTask.TASK_FAILURE);
				task.setFailure_reason(e.getMessage());
				taskService.updateTaskState(task);
				continue;
			}
			
			task.setDownloadfile(filename);
			task.setState(ReportTask.TASK_INHAND);
			
			String[] options = taskService.parseOptions(task, destFilePath + File.separator + filename);
			
			ReportRunner reportRunner=null;
			try {
				reportRunner = new ReportRunner(threadMap,taskService,task,options);	
				
				String birt_task_id = UUID.randomUUID().toString();
				threadMap.put(birt_task_id, reportRunner);
				reportRunner.start();					
				task.setBirt_task_id(birt_task_id);
				task.setDownloadfile(filename);
				task.setState(ReportTask.TASK_INHAND);				
			} catch (BirtReportException e) {
				logger.error("BIRT报表处理引擎失败",e);
				task.setDownloadfile(null);
				task.setState(ReportTask.TASK_FAILURE);
				task.setFailure_reason(e.getMessage());
				if(threadMap != null){
					threadMap.remove(reportRunner);
				}
			}
			taskService.updateTaskState(task);
			}
	}
	public int getMaxThreadsNum() {
		return maxThreadsNum;
	}
	public void setMaxThreadsNum(int maxThreadsNum) {
		this.maxThreadsNum = maxThreadsNum;
	}
	public long getRollInterval() {
		return rollInterval;
	}
	public void setRollInterval(long rollInterval) {
		this.rollInterval = rollInterval;
	}
	public String getDestFilePath() {
		return destFilePath;
	}
	public void setDestFilePath(String destFilePath) {
		this.destFilePath = destFilePath;
	}
	public String getManagerIP() {
		return managerIP;
	}
	public void setManagerIP(String managerIP) {
		this.managerIP = managerIP;
	}
	public int getManagerPort() {
		return managerPort;
	}
	public void setManagerPort(int managerPort) {
		this.managerPort = managerPort;
	}
	public TaskService getTaskService() {
		return taskService;
	}
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}
	public String getReportDesignParentPath() {
		return reportDesignParentPath;
	}
	public void setReportDesignParentPath(String reportDesignParentPath) {
		this.reportDesignParentPath = reportDesignParentPath;
	}
	public int getExpiredDays() {
		return expiredDays;
	}
	public void setExpiredDays(int expiredDays) {
		this.expiredDays = expiredDays;
	}
	
	
}
