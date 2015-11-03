package zhenhe.li.report.birt.task.rest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import zhenhe.li.common.dao.HibernateDao;
import zhenhe.li.common.page.Page;
import zhenhe.li.report.birt.task.entity.ReportDesignFile;
import zhenhe.li.report.birt.task.entity.ReportTask;
import zhenhe.li.report.birt.task.entity.ReportTaskProps;
import zhenhe.li.report.birt.task.param.ReportTaskParam;
import zhenhe.li.report.birt.task.pojo.ReportTaskPojo;
import zhenhe.li.report.birt.task.service.ReportService;
import zhenhe.li.util.sequence.hibernate.entity.SeqRecyclePk;
import zhenhe.li.util.sequence.hibernate.service.SequenceGenerator;
import zhenhe.li.util.spring.ConfigUtil;

@Service
@Path("reportTask")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportTaskRest {
	private final static Logger logger = LoggerFactory.getLogger(ReportTaskRest.class);
	@Inject
	private SequenceGenerator sequenceGenerator;
	@Inject
	private HibernateDao<ReportTask,String> reportTaskDao;
	@Inject
	private HibernateDao<ReportTaskProps,String> reportTaskPropsDao;
	@Inject
	private HibernateDao<ReportDesignFile,String> reportDesignFileDao;
	@Inject
	private ReportService reportTaskService;
	
	/**
	 * jaxrs 方式发布服务，
	 * 该服务处理报表下载任务的提交请求
	 * @param request 报表任务请求对象，
	 * 		包含用什么配置文件(reportId)生成 什么格式(format)的报表 查询条件(attaches)是什么
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public SubmitResponse submit(SubmitRequest request){
		//报表文件名
		String filename = request.getFilename();
		//下载文件格式
		String format = request.getFormat();
		//报表配置文件,根据reportId 获取报表设计文件
		String reportDesignFileId = request.getReportId();
		ReportDesignFile reportDesignFile = reportDesignFileDao.get(ReportDesignFile.class, reportDesignFileId);
		
		ReportTask reportTask=new ReportTask();
		reportTask.setFormat(format);
		reportTask.setRptdesign(reportDesignFile.getDesign_file_path());
		String taskId = UUID.randomUUID().toString().replace("-", "");
		reportTask.setId(taskId);
		Date date=new Date();
		SimpleDateFormat sim =new SimpleDateFormat("yyyyMMdd");
		String keys = sim.format(date);
		String key="id";
		//查询回收表中是否有值
		Serializable recycleId = sequenceGenerator.getIdFromRecycle(key);		
		if(recycleId != null){
			reportTask.setQueue_no(recycleId+"");
			//用完就把回收表中的记录清理
			SeqRecyclePk id = new SeqRecyclePk();
			id.setPrefix(key);
			id.setRecycleVal(recycleId+"");
			sequenceGenerator.removeFromRecycle(id);
		}else{
			int currval= Integer.parseInt(sequenceGenerator.getSeqCurrVal(reportTask.getClass().getSimpleName(),"",8, 1,1));
			String ReportId=String.format("%06d", currval);
			reportTask.setQueue_no(keys+ReportId);
		}
		reportTask.setTask_name(filename);
		reportTask.setCreate_dt(new Date());
		reportTask.setState("00");
		
		Map attaches = request.getAttaches();
		
		//加入birt 需要的默认值 
		attaches.put("__asattachment", "true");
		attaches.put("__filename", filename);
		attaches.put("__overwrite", false);
		attaches.put("__format", format);
		attaches.put("__report", reportDesignFile.getDesign_file_path());
		attaches.put("__emitterid", "uk.co.spudsoft.birt.emitters.excel.XlsxEmitter");
		
		reportTaskDao.save(reportTask);
		
		for(Iterator<String> it =attaches.keySet().iterator();it.hasNext();){
			String k = it.next();
			String v= attaches.get(k) + "";
			ReportTaskProps reportTaskProps=new ReportTaskProps();
			reportTaskProps.setId(UUID.randomUUID().toString().replace("-", ""));
			reportTaskProps.setKey(k);
			reportTaskProps.setValue(v);
		    reportTaskProps.setReport_task_id(taskId);
			reportTaskPropsDao.save(reportTaskProps);
		}
		
		SubmitResponse resp = new SubmitResponse();
		resp.setRespCode("00");
		resp.setRespMsg("成功");
		return resp;
	}
	/**
	 * 根据查询条件获取任务列表
	 * @param param 查询条件，包含起始时间，任务名称等等
	 * @return
	 */
	@POST
	@Path("tasks")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Page<ReportTask> getTasks(ReportTaskParam param){
		reportTaskService.query(param);
		return param.getPage();
	}
	/**
	 * 根据报表任务编号 下载文件
	 * @param reportTaskId 任务编号
	 * @return
	 */
	@GET
	@Path("/download/{queue_no}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Consumes(MediaType.TEXT_PLAIN)
	public Response download(@PathParam(value="queue_no")String reportTaskQueueNo){
		ReportTask reportTask = reportTaskDao.getUnique(ReportTask.class, Restrictions.eq("queue_no",reportTaskQueueNo));
		//下载文件路径，不带路径前缀的
		String downloadFile = reportTask.getDownloadfile();
		//从配置文件中取路径前缀
		String parentPath = ConfigUtil.getContextProperty("destFilePath") + "";
		//文件名从任务对象中取
		String filename = reportTask.getTask_name();
		//文件格式，文件后缀
		String format = reportTask.getFormat();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(parentPath,
					downloadFile));
			byte[] buffer = new byte[1024];
			int len = 0;
			// 将内容读到buffer中，读到末尾为-1
			while ((len = fis.read(buffer)) != -1) {
				// 本例子将每次读到字节数组(buffer变量)内容写到内存缓冲区中，起到保存每次内容的作用
				bos.write(buffer, 0, len);
			}
			return Response
					.ok(bos.toByteArray(),
							"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
					.header("Content-Disposition",
							"attachment; filename="+filename + "." + format)
							.build();
		} catch (FileNotFoundException e) {
			logger.error("",e);
		} catch (IOException e) {
			logger.error("",e);
		} finally {
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					logger.error("",e);
				}
			}
		}
		return Response
				.ok(bos.toByteArray(),
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
				.header("Content-Disposition", "attachment; filename=download_fail.zip")
				.build();
	}
	/**
	 * 根据任务队列号取任务详情
	 * @param reportTaskQueueNo
	 * @return
	 */
	@GET
	@Path("/{queue_no}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	public ReportTaskPojo getReportTask(@PathParam(value="queue_no")String reportTaskQueueNo){
		ReportTaskPojo reportTaskPojo =  new ReportTaskPojo();
		ReportTask reportTask = reportTaskDao.getUnique(ReportTask.class, Restrictions.eq("queue_no",reportTaskQueueNo));
		BeanUtils.copyProperties(reportTask, reportTaskPojo);
		return reportTaskPojo;
	}
}
