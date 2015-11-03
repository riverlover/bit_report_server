package zhenhe.li.report.birt.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import zhenhe.li.report.birt.task.service.ReportTaskServer;
/**
 * 启动报表引擎
 * @author lizhenhe
 *
 */
public class ReportEngineStartupListener implements  ServletContextListener{
	private final static Logger logger = LoggerFactory.getLogger(ReportEngineStartupListener.class);

	@Override
	public void contextDestroyed(ServletContextEvent ctxe) {
		
	}

	@Override
	public void contextInitialized(ServletContextEvent ctxe) {
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(ctxe.getServletContext());
		ReportTaskServer reportTaskServer = (ReportTaskServer) ctx.getBean("reportTaskServer");
		reportTaskServer.start();
	}

}
