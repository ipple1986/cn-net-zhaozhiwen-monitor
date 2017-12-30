package cn.net.zhaozhiwen.monitor.entity;

import java.util.Date;

public class BasicInfo {

	public String ipAddress;
	public String userName;
	public Date operDateTime;
	public String url;
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Date getOperDateTime() {
		return operDateTime;
	}
	public void setOperDateTime(Date operDateTime) {
		this.operDateTime = operDateTime;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Override
	public String toString() {
		return "BasicInfo [ipAddress=" + ipAddress + ", userName=" + userName + ", operDateTime=" + operDateTime
				+ ", url=" + url + "]";
	}
	
}
