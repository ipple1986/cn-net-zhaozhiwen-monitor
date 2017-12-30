package cn.net.zhaozhiwen.monitor.xworkstruts.support;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.ListableBeanFactory;

import cn.net.zhaozhiwen.monitor.BasicInfoLoggable;
import cn.net.zhaozhiwen.monitor.MonitorHandler;
import cn.net.zhaozhiwen.monitor.Utils;
import cn.net.zhaozhiwen.monitor.entity.BasicInfo;

public class DefaultStruts2MonitorHandler implements MonitorHandler {
	
	//负责拦截用户请求地址，请求时间，用户名，ip
	@Override
	public void before(ListableBeanFactory beanFactory,HttpServletRequest request,HttpServletResponse response)
			throws Exception {
		BasicInfo info = null;
		if(ServletActionContext.getRequest().getSession().getAttribute("USER")!=null){
			Map<?,?> map = new org.apache.commons.beanutils.BeanMap(ServletActionContext.getRequest().getSession().getAttribute("USER"));
			if(map.containsKey("oprcode")){
				 info = new BasicInfo();
				info.setUserName(map.get("oprcode").toString());
				info.setIpAddress(Utils.getIpAddress(ServletActionContext.getRequest()));
				info.setOperDateTime(new Date());
				info.setUrl(ServletActionContext.getRequest().getServletPath());
			}
			if(info != null){
				//处理BasicInfoLoggable接口
				Map<String, BasicInfoLoggable> loggmaps = beanFactory.getBeansOfType(BasicInfoLoggable.class, Boolean.FALSE, Boolean.FALSE);
				for(BasicInfoLoggable loggable:loggmaps.values())loggable.logBasicInfo(beanFactory, info);				
			}
		}
	}

	@Override
	public void after(ListableBeanFactory beanFactory,HttpServletRequest request,HttpServletResponse response) throws Exception {
		// TODO 暂保留

	}


}
