目录：
一，saasWeb植入监控：
二，jkbWeb或phoneinfWeb植入监控
三，openapi植入监控



=================================================邪恶的分割线=======================================================
一，SaasWeb植入监控：
1.pom.xml加入依赖
	<dependency>
		<groupId>cn.jufuns.saas.monitor</groupId>
		<artifactId>cn-jufuns-saas-monitor</artifactId>
		<version>0.0.1-SNAPSHOT</version>
	</dependency>
 
 2.修改web.xml 
将web.xml中
	<filter>
		<filter-name>Struts2</filter-name>
		<filter-class>org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter</filter-class>
	</filter>
改为
	<filter>
		<filter-name>Struts2</filter-name>
		<filter-class>cn.jufuns.saasx.monitor.integration.xworkstruts.filter.MonitorStrutsPrepareAndExecuteFilter</filter-class>
	</filter>
	
 3.往spring容器内加入实现BasicInfoLoggable接口或MonitorHandler接口的bean
 编写拦截业务类，代码如下

package cn.jufuns.struts2.filter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.ListableBeanFactory;

import com.alibaba.fastjson.JSONObject;

import cn.jufuns.saasx.monitor.MonitorHandler;

public class SaasWebMonitorHandler implements MonitorHandler {

	private static Logger LOGGER = Logger.getLogger(SaasWebMonitorHandler.class);
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static List<Interceptor> interceptors = new ArrayList<Interceptor>();
	private static final String SEPARTOR_SYMBOL = "*";
	private static final String LOG_FORMATTER_STR = "%s".concat(SEPARTOR_SYMBOL).concat("%s").concat(SEPARTOR_SYMBOL)
			.concat("%s").concat(SEPARTOR_SYMBOL).concat("%s").concat(SEPARTOR_SYMBOL).concat("%s");
	static {		
		interceptors.add(new Interceptor(){//saasweb拦截
			@Override
			public void intercept(ListableBeanFactory beanFactory, HttpServletRequest request,
					HttpServletResponse response) {
				if(request.getSession().getAttribute("USER")!=null){
					Map<?,?> map = new BeanMap(request.getSession().getAttribute("USER"));
					if(map.containsKey("oprcode")){
						String username = map.get("oprcode").toString();
						String sessionId = request.getSession().getId();
						String requestUrl = request.getServletPath();
						String parameters =  JSONObject.toJSONString(request.getParameterMap());
						String operDatetime = formatter.format(new Date());
						LOGGER.info(String.format(LOG_FORMATTER_STR, 
								operDatetime,username,sessionId,requestUrl,parameters));
					}
				}
			}
		});
		
	}
	@Override
	public void before(ListableBeanFactory beanFactory, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
			for(Interceptor i:interceptors)i.intercept(beanFactory, request, response);
	}

	@Override
	public void after(ListableBeanFactory beanFactory, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	interface Interceptor{
		void intercept(ListableBeanFactory beanFactory, HttpServletRequest request, HttpServletResponse response);
	}
}

4.最后将业务类注入Spring容器中
在applicationContext.xml内加入以下代码 
<bean class="cn.jufuns.struts2.filter.SaasWebMonitorHandler"/>

=================================================邪恶的分割线=======================================================

二，jkbWeb或phoneinfWeb植入监控
1.pom.xml加入依赖
	<dependency>
		<groupId>cn.jufuns.saas.monitor</groupId>
		<artifactId>cn-jufuns-saas-monitor</artifactId>
		<version>0.0.1-SNAPSHOT</version>
	</dependency>
2.往Spring容器内（spring-mvc.xml）加入如下代码
<bean class="cn.jufuns.saasx.monitor.integration.springmvc.MonitorBeanFactoryPostProcessor"/>

3.编写实现MonitorHandler接口的bean,样例代码如下
package cn.jufuns.jkb.web.utils;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.ListableBeanFactory;

import cn.jufuns.saasx.monitor.MonitorHandler;

public class MyMonitorHandler implements MonitorHandler {
	@Override
	public void before(ListableBeanFactory beanFactory, HttpServletRequest request, HttpServletResponse response)throws Exception {
		//此处作业务逻辑
		System.out.println(Arrays.toString(beanFactory.getBeanDefinitionNames())); 
	 }

	@Override
	public void after(ListableBeanFactory beanFactory, HttpServletRequest request, HttpServletResponse response)throws Exception {
		// TODO Auto-generated method stub
	 
	}
}
4.最后注入spring容器（spring-mvc.xml）中
<bean class="cn.jufuns.jkb.web.utils.MyMonitorHandler" />
	
=================================================邪恶的分割线=======================================================

三，openapi植入监控
1.在SpringMVCConfigration组件类上注解@EnableMonitor
package cn.jufuns.core.configuration;
......

@Configuration
@EnableWebMvc
@EnableMonitor//支持监控
@ComponentScan(value={"cn.jufuns.saas.controller"})
public class SpringMVCConfiguration extends WebMvcConfigurerAdapter {

2.编写拦截代码，即实现MonitorHandler接口，样例代码如下
package cn.jufuns.saas.util;
......
public class MyMonitorHandler implements MonitorHandler {

	@Override
	public void before(ListableBeanFactory beanFactory, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		System.out.println("---"+Arrays.toString(beanFactory.getBeanDefinitionNames()));
	}
	@Override
	public void after(ListableBeanFactory beanFactory, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// TODO Auto-generated method stub
	}
}

2.将上面的类注入容器中
package cn.jufuns.saas.schedule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import cn.jufuns.saas.sync.config.AbstractDataSyncSchedulingConfigurer;
import cn.jufuns.saas.sync.config.ConfigurableDataSyncScheduleConfigurer;
import cn.jufuns.saas.sync.config.properties.VersionMediaChannelDataSyncPropertiesConfig;
import cn.jufuns.saas.util.MyMonitorHandler;

@Configuration
@EnableScheduling
public class ScheduleConfiguration {
	......
	@Bean //此处注入业务拦截bean
	public MyMonitorHandler handler(){
		return new MyMonitorHandler();
	}
	
	
}