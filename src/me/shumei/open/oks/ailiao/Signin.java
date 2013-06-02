package me.shumei.open.oks.ailiao;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * 使签到类继承CommonData，以方便使用一些公共配置信息
 * @author wolforce
 *
 */
public class Signin extends CommonData {
	String resultFlag = "false";
	String resultStr = "未知错误！";
	
	/**
	 * <p><b>程序的签到入口</b></p>
	 * <p>在签到时，此函数会被《一键签到》调用，调用结束后本函数须返回长度为2的一维String数组。程序根据此数组来判断签到是否成功</p>
	 * @param ctx 主程序执行签到的Service的Context，可以用此Context来发送广播
	 * @param isAutoSign 当前程序是否处于定时自动签到状态<br />true代表处于定时自动签到，false代表手动打开软件签到<br />一般在定时自动签到状态时，遇到验证码需要自动跳过
	 * @param cfg “配置”栏内输入的数据
	 * @param user 用户名
	 * @param pwd 解密后的明文密码
	 * @return 长度为2的一维String数组<br />String[0]的取值范围限定为两个："true"和"false"，前者表示签到成功，后者表示签到失败<br />String[1]表示返回的成功或出错信息
	 */
	public String[] start(Context ctx, boolean isAutoSign, String cfg, String user, String pwd) {
		//把主程序的Context传送给验证码操作类，此语句在显示验证码前必须至少调用一次
		CaptchaUtil.context = ctx;
		//标识当前的程序是否处于自动签到状态，只有执行此操作才能在定时自动签到时跳过验证码
		CaptchaUtil.isAutoSign = isAutoSign;
		
		try{
			//存放Cookies的HashMap
			HashMap<String, String> cookies = new HashMap<String, String>();
			//Jsoup的Response
			Response res;
			
			String loginUrl = getLoginUrl(ctx, user, pwd);
			String signinPageUrl = "";
			String signinSubmitUrl = "";
			
			//登录
			//<?xml version="1.0" encoding="utf-8"?><result><cmd>2</cmd><retVal>0</retVal><hwstatus>0</hwstatus><ip>171.110.37.179</ip><upmarke>0</upmarke><msg>登录成功</msg></result>
			res = Jsoup.connect(loginUrl).userAgent(UA_ANDROID).timeout(TIME_OUT).ignoreContentType(true).method(Method.GET).execute();
			cookies.putAll(res.cookies());
			
			if (res.body().contains("登录成功")) {
				//访问签到页面
				signinPageUrl = getSigninPageUrl(user, pwd);
				res = Jsoup.connect(signinPageUrl).cookies(cookies).userAgent(UA_ANDROID).timeout(TIME_OUT).ignoreContentType(true).method(Method.GET).execute();
				cookies.putAll(res.cookies());
				
				//提交签到请求
				//{"data":2145}
				signinSubmitUrl = getSigninSubmitUrl(res.body(), user);
				res = Jsoup.connect(signinSubmitUrl).cookies(cookies).userAgent(UA_ANDROID).timeout(TIME_OUT).ignoreContentType(true).method(Method.GET).execute();
				if (res.body().length() > 0) {
					JSONObject jsonObj = new JSONObject(res.body());
					String data = String.valueOf(jsonObj.getInt("data"));
					String randomnumber = data.substring(3);
					int statuscode = Integer.valueOf(data.substring(0, 3));
					switch (statuscode) {
						case 201:
							resultFlag = "false";
							resultStr = "您参数有误,请稍后重试.";
							break;
							
						case 203:
							resultFlag = "false";
							resultStr = "您尚未激活爱聊,无法完成签到.";
							break;
							
						case 204:
							resultFlag = "false";
							resultStr = "签到程序里模拟的爱聊软件版本过低.";
							break;
							
						case 206:
							resultFlag = "false";
							resultStr = "签到程序里模拟的爱聊软件版本过低——iPhone.";
							break;
							
						case 211:
							resultFlag = "true";
							resultStr = "签到成功!恭喜您成功获得" + randomnumber + "个聊豆.";
							break;
							
						case 212:
							resultFlag = "true";
							resultStr = "签到成功!恭喜您成功获得" + randomnumber + "个聊豆.\n酷~！升级VIP会员，每天可签到两次。";
							break;
							
						case 213:
							resultFlag = "true";
							resultStr = "您今天已签到过了，获得了" + randomnumber + "个聊豆,请明天再来吧.";
							break;
							
						case 214:
							resultFlag = "true";
							resultStr = "您今天已签到过了, 获得了" + randomnumber + "个聊豆.\n酷~！升级VIP会员，每天可签到两次。";
							break;
							
						case 215:
							resultFlag = "true";
							resultStr = "您今天已签到过了, 获得了" + randomnumber + "个聊豆.\n酷~！升级VIP会员，每天可签到两次。";
							break;
							
						case 216:
							resultFlag = "true";
							resultStr = "您今天已签到过了, 获得了" + randomnumber + "个聊豆.\n酷~！升级VIP会员，每天可签到两次。";
							break;
							
						case 221:
							resultFlag = "true";
							resultStr = "签到成功!恭喜您成功获得" + randomnumber + "个聊豆.";
							break;
							
						case 222:
							resultFlag = "true";
							resultStr = "签到成功!恭喜您成功获得" + randomnumber + "个聊豆.";
							break;
							
						case 223:
							resultFlag = "true";
							resultStr = "签到成功!恭喜您成功获得" + randomnumber + "个聊豆——iPhone";
							break;
							
						case 205:
							resultFlag = "true";
							resultStr = "您今天已签到过了\n获得了" + randomnumber + "个聊豆,请明天再来吧.";
							break;
							
						case 224:
							//android
							resultFlag = "false";
							resultStr = "签到失败！<br />您本月内至少在爱聊软件内下载一款精品软件才能签到。";
							break;
							
						case 225:
							//iphone
							resultFlag = "false";
							resultStr = "签到失败！<br />您本月内至少在爱聊软件内下载一款精品软件才能签到。";
							break;
	
						default:
							resultFlag = "false";
							resultStr = "登录成功，但提交签到请求后返回未知数据";
							break;
					}
					
				} else {
					resultFlag = "false";
					resultStr = "登录成功，但提交签到请求时出现未知错误";
				}
			} else {
				resultFlag = "false";
				resultStr = "登录失败";
			}
			
			
			
			
		} catch (IOException e) {
			this.resultFlag = "false";
			this.resultStr = "连接超时";
			e.printStackTrace();
		} catch (Exception e) {
			this.resultFlag = "false";
			this.resultStr = "未知错误！";
			e.printStackTrace();
		}
		
		return new String[]{resultFlag, resultStr};
	}
	
	/**
	 * 获取登录URL
	 * @param context
	 * @param user
	 * @param pwd
	 * @return
	 */
	private String getLoginUrl(Context context, String user, String pwd) {
		String imei = "456156451534587";//默认IMEI串
		String imsi = "460020914541001";//默认IMSI串
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			imei = telephonyManager.getDeviceId();
			imsi = telephonyManager.getSubscriberId();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("http://a.iitalk.net/login.asp?hwstatus=0&hwip=&ver=android(2.2.7)&imei=");
		sb.append(imei);
		sb.append("&imsi=");
		sb.append(imsi);
		sb.append("&username=");
		sb.append(user);
		sb.append("&pwd=");
		sb.append(MD5.md5("nvasd4JDS*(^$#" + user + pwd + "@$^"));
		return sb.toString();
	}
	
	
	/**
	 * 获取签到页面的URL
	 * @param user
	 * @param pwd
	 * @return
	 */
	private String getSigninPageUrl(String user, String pwd) {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append("http://220.231.194.251/dayup/index.php/dayup?username=");
			sb.append(user);
			sb.append("&pwd=");
			sb.append(MD5.md5(user + "$%^2cDFs3" + pwd));
			sb.append("&ver=2.2.7");
			sb.append("&perform=android&rel=&linkid=0");
			sb.append("&ad=yes&adid=1&title=");
			sb.append(URLEncoder.encode("每日签到", "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	/**
	 * 获取提交签到信息的URL
	 * @param str 签到页面的HTML代码
	 * @param user 用户名
	 * @return
	 */
	private String getSigninSubmitUrl(String str, String user) {
		Pattern pattern = Pattern.compile("var sid.*=.*'(.+)';");
		Matcher matcher = pattern.matcher(str);
		String sid = MD5.md5(user);
		while (matcher.find()) {
			sid = matcher.group(1);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("http://220.231.194.251/dayup/index.php/ajax_data?");
		sb.append("callback=?&u=");
		sb.append(user);
		sb.append("&v=2.2.7&p=android&l=0&w=1&sid=");
		sb.append(sid);
		return sb.toString();
	}
	
	
	
}
