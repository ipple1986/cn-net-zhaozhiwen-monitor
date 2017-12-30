package cn.net.zhaozhiwen.monitor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.ListableBeanFactory;

public interface MonitorHandler {

	public void before(ListableBeanFactory beanFactory,HttpServletRequest request,HttpServletResponse response) throws Exception;
	public void after(ListableBeanFactory beanFactory,HttpServletRequest request,HttpServletResponse response) throws Exception;
}
