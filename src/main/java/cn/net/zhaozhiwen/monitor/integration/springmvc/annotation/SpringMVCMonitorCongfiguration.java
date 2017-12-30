package cn.net.zhaozhiwen.monitor.integration.springmvc.annotation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import cn.net.zhaozhiwen.monitor.integration.springmvc.MonitorBeanFactoryPostProcessor;
/**
 * 
 * @author ipple1986
 * 
 * 注解方式集成：
 * 
 * 1.注解@EnableMonitor
 * 
 *  @Configuration
 *  @EnableWebMvc
 *  @EnableMonitor //此处添加注解
 *  @ComponentScan(value={"cn.jufuns.saas.controller"})
 *  public class SpringMVCConfiguration extends WebMvcConfigurerAdapter {
 *  	......
 *  }
 * 
 * 2.往spring容器内加入实现MonitorHandler接口的bean
 * 
 * @Configuration
 * public class XXXCongfiguration{
 * 		@Bean
 * 		public MyMonitorHandler handler(){
 * 			return new MyMonitorHandler();
 * 		}
 * 	}
 * 
 * 实现拦截的代码如下：
 * 
 * 	package cn.jufuns.saas.util;
 * 	
 * 	import java.util.Arrays;
 * 		
 * 	import javax.servlet.http.HttpServletRequest;
 * 	import javax.servlet.http.HttpServletResponse;
 * 		
 * 	import org.springframework.beans.factory.ListableBeanFactory;
 * 		
 * 	import cn.jufuns.saasx.monitor.MonitorHandler;
 * 		
 * 	public class MyMonitorHandler implements MonitorHandler {
 * 		
 * 			@Override
 * 			public void before(ListableBeanFactory beanFactory, HttpServletRequest request, HttpServletResponse response)
 * 					throws Exception {
 * 				System.out.println("---"+Arrays.toString(beanFactory.getBeanDefinitionNames()));
 * 		
 * 			}
 * 	
 * 			@Override
 * 	 		public void after(ListableBeanFactory beanFactory, HttpServletRequest request, HttpServletResponse response)
 * 					throws Exception {
 * 				// TODO Auto-generated method stub
 * 		
 * 			}
 * 		
 * 	}
 */
@Configuration
public class SpringMVCMonitorCongfiguration {

	public SpringMVCMonitorCongfiguration() {
		// TODO Auto-generated constructor stub
	}
	@Bean(name="cn.jufuns.saas.monitor.springmvc.internalMonitorBeanFactoryPostProcessor")
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public static MonitorBeanFactoryPostProcessor monitorBeanFactoryPostProcessor(){
		return new MonitorBeanFactoryPostProcessor();
	}

}
