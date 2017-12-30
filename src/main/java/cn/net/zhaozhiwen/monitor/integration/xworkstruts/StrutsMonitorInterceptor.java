package cn.net.zhaozhiwen.monitor.integration.xworkstruts;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import cn.net.zhaozhiwen.monitor.MonitorHandler;
import cn.net.zhaozhiwen.monitor.xworkstruts.support.DefaultStruts2MonitorHandler;

public class StrutsMonitorInterceptor extends AbstractInterceptor {

	/**
	 * 针对Struts2进行拦载器扩展
	 * 
	 * 1.pom.xml加入依赖
	 * <dependency>
	 *	 		<groupId>cn.jufuns.saas.monitor</groupId>
	 *			<artifactId>cn-jufuns-saas-monitor</artifactId>
	 *			<version>0.0.1-SNAPSHOT</version>
	 * </dependency>
	 * 
	 * 
	 * 2.向Spring容器内注入MonitorHandler类型的bean
	 * DefaultMonitorHandler默认实现从会话中提取登录用户名称及请求url
	 * <bean class="cn.jufuns.struts2.filter.DefaultMonitorLog"/>
	 * 	代码如下
	 *	package cn.jufuns.struts2.filter;
	 *
	 *	import org.springframework.beans.factory.ListableBeanFactory;
	 *
	 *	import cn.jufuns.saasx.monitor.BasicInfoLoggable;
	 *	import cn.jufuns.saasx.monitor.entity.BasicInfo;
	 *
	 *	public class DefaultMonitorLog implements BasicInfoLoggable {
	 *
	 *		@Override
	 *		public void logBasicInfo(ListableBeanFactory beanFactory,BasicInfo info) {
	 *			System.out.println(beanFactory.getBeanDefinitionNames());
	 *			System.out.println(info);
	 *		}
	 *
	 *	}
	 *
	 * 即可对方法前后进行拦截
	 * 
	 * 3.修改wagon-default.xml，添加monitor拦截器,fileUploadStack2拦截栈
	 *    
	 *  <package name="wagon-default" namespace="/" extends="struts-default">
	 *		<interceptors>
	 *			......
	 *			<interceptor name="monitor" class="cn.jufuns.saasx.monitor.integration.xworkstruts.StrutsMonitorInterceptor"/>
	 *			<interceptor-stack name="exampleStack">		
	 *				......
	 *				<interceptor-ref name="monitor" />
	 *			</interceptor-stack>
	 *			<interceptor-stack name="fileUploadStack2">
   	 *             <interceptor-ref name="fileUpload"/>
   	 *             <interceptor-ref name="basicStack"/>
   	 *             <interceptor-ref name="monitor" />
   	 *         </interceptor-stack>
	 *		</interceptors>
	 *		......
	 *	</package>
	 *
	 * 4.所有继承wagon-default的xxx-support.xml的包，如果有文件上传（配置fileUploadStack,统一改成fileUploadStack2）
	 * 以system-support.xml为例
	 * <struts>
	 *	   <package name="system" namespace="/system" extends="wagon-default">
	 *			<!-- 基础模块 组织架构 org -->
	 *	        <action name="org_*" class="cn.jufuns.saas.system.org.web.OrgAction" method="{1}">
	 *		       	 <interceptor-ref name="fileUpload">
	 *		        	<param name="allowedTypes">jpg/jpeg/png/gif/bmp</param>
	 *		        </interceptor-ref>
	 *		    	<interceptor-ref name ="fileUploadStack2"/>//此处改为fileUploadStack2
	 *		......
	 *		</package>
	 *</struts>
	 */
	private static final long serialVersionUID = 1L;
	private ConfigurableApplicationContext applicationContext;
	public static final String DEFAULT_STRUTS_MONITORHANDLER_NAME = "cn.jufuns.saas.monitor.support.DefaultStruts2MonitorHandler";
	
	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		if(applicationContext==null){//开始时设置并注入默认监控处理器DefaultMonitorHandler
			applicationContext = (ConfigurableApplicationContext) ActionContext.getContext().getApplication().get(
	                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);	
		}
		if(applicationContext != null && !applicationContext.containsBean(DEFAULT_STRUTS_MONITORHANDLER_NAME)){
			applicationContext.getBeanFactory().registerSingleton(DEFAULT_STRUTS_MONITORHANDLER_NAME, new DefaultStruts2MonitorHandler());
		}
		if(applicationContext==null){//获取不到spring容器，即没用springweb容器时，不拦截
			return invocation.invoke();
		}else{
			HttpServletRequest req = (HttpServletRequest )ActionContext.getContext().get(org.apache.struts2.StrutsStatics.HTTP_REQUEST);
			HttpServletResponse res = (HttpServletResponse )ActionContext.getContext().get(org.apache.struts2.StrutsStatics.HTTP_RESPONSE);
			//处理MonitorHandler接口
			Map<String, MonitorHandler> maps = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, MonitorHandler.class);
			for(MonitorHandler handler:maps.values()){//Action方法调用前拦截
				handler.before(applicationContext,req,res);
			}
			String result = invocation.invoke();
			for(MonitorHandler handler:maps.values()){//Action方法调用后拦截
				handler.after(applicationContext,req,res);
			}
			return result;
		}
	}

}
