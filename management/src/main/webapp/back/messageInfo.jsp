<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" isELIgnored="false"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>

<head>
<title>消息详细信息</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<link type="text/css" rel="stylesheet" href="<%=basePath %>/css/style_f.css"/>
	<link type="text/css" rel="stylesheet" href="<%=basePath %>/css/item_f.css"/>
	<link type="text/css" rel="stylesheet" href="<%=basePath%>/js/themes/base/jquery.ui.all.css">
	<script type="text/javascript" src="<%=basePath%>/js/public/jquery.js"></script>
	<script type="text/javascript" src="<%=basePath%>/js/ui/jquery.ui.core.js"></script>
	<script type="text/javascript" src="<%=basePath%>/js/ui/jquery.ui.widget.js"></script>
	<script type="text/javascript" src="<%=basePath%>/js/ui/jquery.ui.datepicker.js"></script>
	<script type="text/javascript" src="<%=basePath%>/js/broadcast.js"></script>
<script type="text/javascript">
   function check_message(obj)
   {
      $("#message_value").val(obj);
      $("#msg_form").submit();
   }
   $(document).ready(function(){
	   	$("#subscribeDate").datepicker({ disabled: true });
	   	var hs = Date.parse("${message.scheduleDate}");
		var d = new Date(hs);
		$("#subscribeDate").datepicker( "setDate" , d);
  });
</script>
</head>

<body style="background-color: transparent" class="news_push">
<form action="<%=path %>/checkMessage.action" method="post" id="msg_form">
        <div class="content">
        <h2>消息详细信息</h2>
        <br/>
        <div>${dateTip }</div>
<table cellspacing="10" cellpadding="0" class="userInfoTable" style="width:600px">
  <tr>
    <td  class="title">应用名称：</td>
    <td >
      <input type="text" class="inputText130" value="${app.appName }" readonly="readonly" class="inputText130"/>
    </td>
    <td  class="title">推送目标：</td>
    <td width="39%"><input class="inputText130" type="text" value="${message.pushTarget }"  readonly="readonly"/> </td>
  </tr>
  <tr>
    <td class="title">消息格式：</td>
    <td><input type="text" class="inputText130" value="${message.messageFormat }"  readonly="readonly"/> </td>
    <!-- 
    <td class="title">消息时效：</td>
    <td id="insert">
      <input type="text" class="inputText130" value="${message.messageAge }"  readonly="readonly"/>
     </td>
    -->
    <td class="title1">推送预订日期：</td>
    <td><input type="text" class="inputText130" name="subscribeDate" id="subscribeDate"/> </td>
  </tr>
  <tr>
    <td class="title">推送开始时间：</td>
    <td><select name="beginHour" id="beginHour" class="inputText130">
    		<option>7</option>
    		<option selected="selected">8</option>
    		<option>9</option>
    		<option>10</option>
    		<option>11</option>
    		<option>12</option>
    		<option>13</option>
    		<option>14</option>
    	</select></td>
    <td class="title1">推送结束时间：</td>
    <td><select name="endHour" id="endHour" class="inputText130">
    		<option>12</option>
    		<option>13</option>
    		<option>14</option>
    		<option>15</option>
    		<option>16</option>
    		<option>17</option>
    		<option>18</option>
    		<option>19</option>
    		<option selected="selected">20</option>
    		<option>21</option>
    		<option>22</option>
    	</select></td>
  </tr>
  
  <tr>
    <td class="title">消息内容：</td>
    <td colspan="3"><textarea class="inputText130" name="msg" id="msg" cols="" rows="" readonly="readonly" style="width:496px;height:50px">${message.messageContent }</textarea></td>
  </tr>
  <tr>
    <td class="title">审批意见：</td>
    <td colspan="3"><textarea class="inputText130" name="checkNote" id="checkNote" cols="" rows="" style="width:496px;height:50px">${message.checkNote }</textarea></td>
  </tr>
      <tr class="add_bu">
    <td colspan="4"><img src="<%=basePath %>/images/pics.png"/><a href="#">添加图片</a><img src="<%=basePath %>/images/sound_bu.png"/><a href="#">添加音频</a><img src="<%=basePath %>/images/vedio.png"/><a href="#">添加视频</a><img src="<%=basePath %>/images/plus.png"/><a href="#">添加URL</a></td>
    </tr>
      <tr class="add_bu">
    <td colspan="4" style="text-align:center;"><input type="button" value="审核通过" id="bu_broadcast" class="bu_input" onclick="check_message('1')"/>
      <input type="button" value="审核不通过" id="bu_broadcast" class="bu_input" onclick="check_message('-1')"/>
      <input type="hidden" id="message_value" name="message_value" value=""/>
    </td>
    </tr>
</table>
</div>
</form>
</body>
</html>