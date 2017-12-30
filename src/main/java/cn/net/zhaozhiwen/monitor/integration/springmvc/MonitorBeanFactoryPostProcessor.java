package cn.net.zhaozhiwen.monitor.integration.springmvc;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.core.Ordered;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import cn.net.zhaozhiwen.monitor.springmvc.support.DefaultSpringMVC4MonitorHandler;

public class MonitorBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	/**
	 * 修改SpringMVC 中 入口映射类 RequestMappingHandlerMapping
	 * 为基添加拦截器，所有实现了WebRequestInterceptor，HandlerInterceptor接口的业务都被拦截
	 * 
	 * SpringMVC4集成：
	 * 1.pom.xml加入依赖
	 * 	<dependency>
	 *	 	 	<groupId>cn.jufuns.saas.monitor</groupId>
	 *			<artifactId>cn-jufuns-saas-monitor</artifactId>
	 *			<version>0.0.1-SNAPSHOT</version>
	 *	</dependency>
	 * 2.往Spring容器内加入
	 * <bean class="cn.jufuns.saasx.monitor.integration.springmvc.MonitorBeanFactoryPostProcessor"/>
	 * 3.往spring容器内加入实现MonitorHandler接口的bean
	 * 		往容器内加入
	 * 		<bean class="cn.jufuns.jkb.web.utils.MyMonitorHandler" />
	 * 代码如下：
	 *		  	package cn.jufuns.jkb.web.utils;
	 *
	 *			import java.util.Arrays;
	 *			
	 *			import javax.servlet.http.HttpServletRequest;
	 *			import javax.servlet.http.HttpServletResponse;
	 *			
	 *			import org.springframework.beans.factory.ListableBeanFactory;
	 *			
	 *			import cn.jufuns.saasx.monitor.MonitorHandler;
	 *			
	 *			public class MyMonitorHandler implements MonitorHandler {
	 *			
	 *				@Override
	 *				public void before(ListableBeanFactory beanFactory, HttpServletRequest request, HttpServletResponse response)
	 *						throws Exception {
	 *					//此处作业务逻辑
	 *					System.out.println(Arrays.toString(beanFactory.getBeanDefinitionNames()));
	 *			
	 *				}
	 *			
	 *				@Override
	 *				public void after(ListableBeanFactory beanFactory, HttpServletRequest request, HttpServletResponse response)
	 *						throws Exception {
	 *					// TODO Auto-generated method stub
	 *			
	 *				}
	 *			
	 *			}
	 *
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		
		//添入默认实现
		if(!beanFactory.containsBean("cn.jufuns.saas.monitor.springmvc.support.DefaultHandlerInterceptor")){
			beanFactory.registerSingleton("cn.jufuns.saas.monitor.springmvc.support.DefaultHandlerInterceptor", new DefaultSpringMVC4MonitorHandler(beanFactory));
		}
		//"org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping#0"
		BeanDefinition bd = beanFactory.getBeanDefinition(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, RequestMappingHandlerMapping.class)[0]);
		String[] webRequestInterceptorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, WebRequestInterceptor.class);
		String[] handlerInterceptorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, HandlerInterceptor.class);
		ManagedList<RuntimeBeanReference> list = new ManagedList<RuntimeBeanReference>();
		for(String name:webRequestInterceptorNames){
			list.add(new RuntimeBeanReference(name));
		}
		for(String name:handlerInterceptorNames){
			list.add(new RuntimeBeanReference(name));
		}
		bd.getPropertyValues().addPropertyValue("interceptors", list);	
	}

	

}
