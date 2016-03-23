<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%
  String path=request.getContextPath();
%>
<html>

  <head>
     <title>应用修改</title>
     <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
     <link type="text/css" rel="stylesheet" href="<%=path%>/css/style_f.css"/>
	 <link type="text/css" rel="stylesheet" href="<%=path%>/css/item_f.css"/>
	 <script type="text/javascript" src="<%=path%>/js/public/jquery.js"></script>
	 <script type="text/javascript">
	 $(function(){
		document.getElementById("app.pushMode-1").disabled="true";
		document.getElementById("app.pushMode-1").checked="checked";
		document.getElementById("app.pushMode-3").disabled="disabled";
		document.getElementById("app.messageLifeCycle-1").disabled="disabled";
		document.getElementById("app.messageLifeCycle-1").checked="checked";
		document.getElementById("app.messageFormat-1").disabled="disabled";
		document.getElementById("app.messageFormat-1").checked="checked";
	});
	   var flag=${flag};
	   function sel(obj)
	   {
		   var dis=$("#custom_my").css("display");
		  if(obj=="custom")
		  {
			  if(dis=="none")
			  {
				  $("#custom_").val("0");
				  $("#custom_my").css("display","");
			   }
			  else
			  {
				  $("#custom_").val("0");
				  $("#custom_my").css("display","none");
			  }
		  	
		  }
	   }
	 </script>
  </head>
 
<body style="background-color: transparent" class="app_mana">
<s:if test="#request.result==1">
  <script type="text/javascript">
     alert("信息修改成功!");
  </script>
</s:if>
<div class="content">
<h2>应用详细信息</h2>
<form action="<%=path%>/updateApp.action" method="post">
<table cellspacing="10" cellpadding="0" class="userInfoTable">
	<tr>
		<td class="title">应用名称：</td>
		<td><input name="app.appName" id="appName"  value="${app.appName}" type="text" class="inputText"/></td>
	</tr>
	<tr>
		<td class="title">应用类型：</td>
		<td>
		<s:select name="app.appType" theme="simple"   list="#{'yule':'娱乐','yinyue':'音乐','xiaoshuo':'小说','yingshi':'影视'}" cssClass="inputText"></s:select>
		</td>
	</tr>
	<tr>
		<td class="title">包名：</td>
		<td><input name="app.packageName"  id="packageName" value="${app.packageName }" type="text" class="inputText"/></td>
	</tr>
	<tr>
		<td class="title">推送平台：</td>
		<td>
		<s:select name="app.platForm" theme="simple"  list="#{'ANDROID':'ANDROID'}" cssClass="inputText"></s:select>
			</td>
	</tr>
	<tr>
		<td class="title">应用简介：</td>
		<td colspan="1"><textarea name="app.appProfile" cols="" rows="16" class="inputText">${app.appProfile }&nbsp;</textarea></td>
	</tr>
    <s:if test="#request.app.checkView!=0">
	<tr>
		<td class="title">审核意见：</td>
		<td colspan="1"><textarea name="app.checkView" cols="" readonly="readonly" rows="16">${app.checkView }</textarea></td>
	</tr>
   </s:if>
	<tr>
		<td class="bu_td" colspan="2"><input type="submit" value="更新"
			class="bu_input" /> <input type="reset" value="重  置"
			class="bu_input" /></td>
	</tr>
</table>
</form>
</div>

</body>
</html>