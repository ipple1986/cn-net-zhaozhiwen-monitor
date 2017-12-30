package cn.net.zhaozhiwen.monitor;

import org.springframework.beans.factory.ListableBeanFactory;

import cn.net.zhaozhiwen.monitor.entity.BasicInfo;

public interface BasicInfoLoggable {
	void logBasicInfo(ListableBeanFactory beanFactory,BasicInfo info);
}
