package cn.net.zhaozhiwen.monitor.springmvc.support;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import cn.net.zhaozhiwen.monitor.BasicInfoLoggable;
import cn.net.zhaozhiwen.monitor.MonitorHandler;
import cn.net.zhaozhiwen.monitor.Utils;
import cn.net.zhaozhiwen.monitor.entity.BasicInfo;

public class DefaultSpringMVC4MonitorHandler implements HandlerInterceptor {

	private ConfigurableListableBeanFactory context;
	private Map<String, MonitorHandler> handlerMaps;
	public DefaultSpringMVC4MonitorHandler(){}
	public DefaultSpringMVC4MonitorHandler(ConfigurableListableBeanFactory context){
		this.context = context;
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		BasicInfo info = null;
		if(request.getSession().getAttribute("USER")!=null){
			Map<?,?> map = new org.apache.commons.beanutils.BeanMap(request.getSession().getAttribute("USER"));
			if(map.containsKey("oprcode")){
				 info = new BasicInfo();
				info.setUserName(map.get("oprcode").toString());
				info.setIpAddress(Utils.getIpAddress(request));
				info.setOperDateTime(new Date());
				info.setUrl(request.getServletPath());
			}
			if(info != null){
				//处理BasicInfoLoggable接口
				Map<String, BasicInfoLoggable> loggmaps = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, BasicInfoLoggable.class);
				for(BasicInfoLoggable loggable:loggmaps.values())loggable.logBasicInfo(context, info);				
			}
		}
		//处理MonitorHandler接口
		if(handlerMaps==null){
			handlerMaps = Collections.unmodifiableMap(BeanFactoryUtils.beansOfTypeIncludingAncestors(context, MonitorHandler.class));			
		}
		for(MonitorHandler handlerx:handlerMaps.values()){//Controller方法调用前拦截
			handlerx.before(context,request,response);
		}
		return Boolean.TRUE;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		//处理MonitorHandler接口
		if(handlerMaps==null){
			handlerMaps = Collections.unmodifiableMap(BeanFactoryUtils.beansOfTypeIncludingAncestors(context, MonitorHandler.class));			
		}
		for(MonitorHandler handlerx:handlerMaps.values()){//Controller方法调用后拦截
			handlerx.after(context,request,response);
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		
	}


}
