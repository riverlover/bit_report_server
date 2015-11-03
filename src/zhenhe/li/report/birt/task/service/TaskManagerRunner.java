package zhenhe.li.report.birt.task.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.birt.report.engine.api.IEngineTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhenhe.li.common.dao.HibernateDao;
import zhenhe.li.report.birt.extend.ReportRunner;
import zhenhe.li.report.birt.task.entity.ReportTask;
public class TaskManagerRunner implements Runnable{
	private final static Logger logger  = LoggerFactory.getLogger(TaskManagerRunner.class);
	private HibernateDao<ReportTask,String> reportTaskDao;
	private Map<String,ReportRunner> threadMap;
	private String ip;
	private int port;
	private TaskService taskService;
	
	public TaskManagerRunner(String ip,int port,Map<String,ReportRunner> threadMap,TaskService taskService) {
		super();
		this.threadMap = threadMap;
		this.ip = ip;
		this.port = port;
		this.taskService = taskService;
		this.reportTaskDao = taskService.getReportTaskDao();
	}
	

	


	@Override
	public void run() {
		try {
//			ServerSocket serverSocket = new ServerSocket();
//			serverSocket.bind(new InetSocketAddress(ip, port));
			ServerSocket serverSocket = new ServerSocket(port);
			while(true){
				final Socket socket = serverSocket.accept();
				new Thread(){

					@Override
					public void run() {
						InputStream in;
						try {
							in = socket.getInputStream();
						
							byte[] rcvLenBytes = new byte[2];
							in.read(rcvLenBytes);
							/* C系统有 unsigned byte类型 ，java中没有无符号型，所以会出现负值 */
							int len1 = (rcvLenBytes[0]>=0?rcvLenBytes[0]:(256 + rcvLenBytes[0]))*256;
							int len2 = rcvLenBytes[1]>=0?rcvLenBytes[1]:(256 + rcvLenBytes[1]);
							int rcvLen = len1 + len2;
							byte[] rcvDataBytes = new byte[rcvLen];
							logger.debug("接收客户端数据长度："+rcvLen);
							in.read(rcvDataBytes);
							String rcvData = new String(rcvDataBytes,"GBK");
							logger.debug("接收客户端数据："+rcvData);	
							String respData = handleRequest(rcvData);
							OutputStream out = socket.getOutputStream();
							int sndLen = respData.length();
							logger.debug("发送的数据长度是："+sndLen +",向客户端发送的数据："+respData);
							byte[] sndLenBytes = new byte[]{(byte) (sndLen/256),(byte) (sndLen%256)};
							out.write(sndLenBytes);
							out.write(respData.getBytes("GBK"));
							logger.debug("向客户端发送完毕！");
							
						} catch (IOException e) {
							logger.error("Socket通讯错误",e);
						}finally{
							if(socket != null)
								try {
									socket.close();
								} catch (IOException e) {
									logger.error("关闭客户端套接字出错",e);
								}
						}
					}
					
				}.start();
			}
		} catch (IOException e1) {
			logger.error("地址绑定错误！",e1);
			return;
		}
		
	}
	/**
	 * Socket接收到客户端请求的报文数据，以“|”分隔
	 * @param rcvData
	 * @return
	 */
	private String handleRequest(String rcvData){
		StringBuffer respBuffer = new StringBuffer();
		ReportTask reportTask = new ReportTask();
		
		String[] data = rcvData.split("\\|");
		String operation = data[0];
		String taskId = data[1];
		respBuffer.append(operation+"|");
		respBuffer.append(taskId+"|");
		if("cancel".equals(operation)){
			reportTask.setId(taskId);
			boolean isSuccess = this.cancelTask(reportTask);
			if(isSuccess){
				respBuffer.append("00|");
				respBuffer.append("取消成功");
				return respBuffer.toString();
			}
			
		}
		respBuffer.append("01"+"|");
		respBuffer.append("处理失败");
		return respBuffer.toString();
		
	}
	public boolean cancelTask(ReportTask t){
		ReportTask reportTask = reportTaskDao.get(ReportTask.class,t.getId());
		String thread_id = reportTask.getBirt_task_id();
		String downloadfile= reportTask.getDownloadfile();
		//处于处理中的任务，需要先取消任务
		if(ReportTask.TASK_INHAND.equals(reportTask.getState())){
			ReportRunner reportRunner = threadMap.get(thread_id);
			if(reportRunner != null){
				IEngineTask task = reportRunner.getRunAndRenderTask();
				if(task!=null && IEngineTask.STATUS_RUNNING == task.getStatus()){
					task.cancel();
				}
			}
			
		}
		try {
			File file = new File(downloadfile);
			logger.debug("任务取消，准备删除任务目标文件"+file.getAbsolutePath());
			if(file.exists()){
				FileUtils.forceDelete(new File(downloadfile));
				logger.debug("文件:"+file.getAbsolutePath()+"删除成功！");			
			}else{
				logger.debug("文件："+file.getAbsolutePath()+"不存在");
			}
		} catch (Exception e) {
			logger.error("删除失败",e);
		}
		threadMap.remove(thread_id);
		reportTaskDao.delete(ReportTask.class, t.getId());
		return true;
	}

}
