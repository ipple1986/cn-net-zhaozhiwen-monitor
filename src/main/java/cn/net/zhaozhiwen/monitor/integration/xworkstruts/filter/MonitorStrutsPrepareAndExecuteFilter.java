/*
 * $Id: DefaultActionSupport.java 651946 2008-04-27 13:41:38Z apetrelli $
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cn.net.zhaozhiwen.monitor.integration.xworkstruts.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.opensymphony.xwork2.ActionContext;

import cn.net.zhaozhiwen.monitor.MonitorHandler;
import cn.net.zhaozhiwen.monitor.integration.xworkstruts.StrutsMonitorInterceptor;
import cn.net.zhaozhiwen.monitor.xworkstruts.support.DefaultStruts2MonitorHandler;

/**
 * 
 * 
 * @author ipple1986
 * @code
 * 
 * Struts2集成：
 * 1.pom.xml加入依赖
 * 	<dependency>
	 	 	<groupId>cn.jufuns.saas.monitor</groupId>
			<artifactId>cn-jufuns-saas-monitor</artifactId>
			<version>0.0.1-SNAPSHOT</version>
	  </dependency>
 * 2.修改web.xml 
 * <filter>
		<filter-name>Struts2</filter-name>
		<filter-class>org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter</filter-class>
	</filter>
	改为
 * <filter>
		<filter-name>Struts2</filter-name>
		<filter-class>cn.jufuns.saasx.monitor.integration.xworkstruts.filter.MonitorStrutsPrepareAndExecuteFilter</filter-class>
	</filter>
	
 * 3.往spring容器内加入实现BasicInfoLoggable接口或MonitorHandler接口的bean
 * applicationContext.xml内加入
 * <bean class="cn.jufuns.struts2.filter.DefaultMonitorLog"/>
 * 代码如下：
 *	package cn.jufuns.struts2.filter;
 *	import org.springframework.beans.factory.ListableBeanFactory;
 *	
 *	import cn.jufuns.saasx.monitor.BasicInfoLoggable;
 *	import cn.jufuns.saasx.monitor.entity.BasicInfo;
 *	
 *	public class DefaultMonitorLog implements BasicInfoLoggable {
 *	
 *		@Override
 *		public void logBasicInfo(ListableBeanFactory beanFactory,BasicInfo info) {
 *			System.out.println(beanFactory.getBeanDefinitionNames());//此处作业务处理
 *			System.out.println(info);
 *		}
 *	
 *	}
 */
public class MonitorStrutsPrepareAndExecuteFilter extends StrutsPrepareAndExecuteFilter {

    

    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        try {
            if (excludedPatterns != null && prepare.isUrlExcluded(request, excludedPatterns)) {
                chain.doFilter(request, response);
            } else {
                prepare.setEncodingAndLocale(request, response);
                prepare.createActionContext(request, response);
                prepare.assignDispatcherToThread();
                request = prepare.wrapRequest(request);
                ActionMapping mapping = prepare.findActionMapping(request, response, true);
                if (mapping == null) {
                    boolean handled = execute.executeStaticResourceRequest(request, response);
                    if (!handled) {
                        chain.doFilter(request, response);
                    }
                } else {
                	ApplicationContext applicationContext = (ApplicationContext) ActionContext.getContext().getApplication().get(
                            WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
                	if(applicationContext != null){
                		if(!applicationContext.containsBean(StrutsMonitorInterceptor.DEFAULT_STRUTS_MONITORHANDLER_NAME)){
                			((ConfigurableApplicationContext)applicationContext).getBeanFactory().registerSingleton(StrutsMonitorInterceptor.DEFAULT_STRUTS_MONITORHANDLER_NAME, new DefaultStruts2MonitorHandler());	
                		}
                        //处理MonitorHandler接口
                        Map<String, MonitorHandler> maps = applicationContext.getBeansOfType(MonitorHandler.class, Boolean.FALSE, Boolean.FALSE);
            			for(MonitorHandler handler:maps.values()){//Action方法调用前拦截
            				handler.before((ListableBeanFactory)applicationContext,ServletActionContext.getRequest(),ServletActionContext.getResponse());
            			}
            			execute.executeAction(request, response, mapping); 
            			for(MonitorHandler handler:maps.values()){//Action方法调用前拦截
            				handler.after((ListableBeanFactory)applicationContext,ServletActionContext.getRequest(),ServletActionContext.getResponse());
            			}
                	}else{
                        execute.executeAction(request, response, mapping);                		
                	}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            prepare.cleanupRequest(request);
        }
    }

    public void destroy() {
        super.destroy();
    }

}
