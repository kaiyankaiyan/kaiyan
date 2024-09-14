<%@ page contentType="text/html; charset=utf-8" language="java" %>
<html>
<HEAD>
<title>&#160;</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link type="text/css" href="/mily/css/common.css" rel="stylesheet" />
	<link type="text/css" href="/mily/css/table.css" rel="stylesheet" />
	<link type="text/css" href="/mily/css/menu.css" rel="stylesheet" />
	<script language="JavaScript" src="/mily/js/controller.js" ></script>
	<script language="JavaScript" src="/mily/js/common.js" ></script>
	<script language="JavaScript" src="/mily/js/components.js" ></script>
	<script language="JavaScript" src="/mily/js/validator.js" ></script>
<script type="text/javascript">
loading = new L_loading();
ajax = new MilyAjax(document.URL);
compList = new L_compList();
</script>
</HEAD>
<body>
<div id="topbar">
	<div id="errorPanel" style="display:none;"></div>
	<div id='loading' class='loading_panel'><div class='loading_inner'>loading</div></div>
	<div class="confirm_panel" id="confirm" style="display:none;">
		<table id="_60165">
		  <tbody>
			<tr>
			  <td id="_60166" style="padding:20 10 10 10;">
				<font id="confirm_info">提问内容</font>
			  </td>
			</tr>
			<tr>
			  <td id="_60168" style="padding:0 80 20 80;">
				<table id="_60167" style="margin:0 auto;">
				  <tbody>
					<tr>
					  <td onclick="$('confirm_byes').ondblclick(event);$('confirm').style.display='none';" id="confirm_yes">
						<a id="confirm_byes">[确认]</a>
					  </td>
					  <td id="_60171">
						<a id="_60170">　　</a>
					  </td>
					  <td onclick="$('confirm').style.display='none';" id="confirm_cancel">
						<a id="_60172">[取消]</a>
					  </td>
					</tr>
				  </tbody>
				</table>
			  </td>
			</tr>
		  </tbody>
		</table>
	</div>
	<div class="mask_panel" id="mask" style="display:none;">.</div>
</div>
<div id='floatbar' style='display:none;position:relative;'><div id='floatrows'></div><div id='floatcols'></div><div id='footer' style='display:none;'></div></div>
<div id='_0fwp'><div id='_0fw'>
	<script type="text/javascript">
	if (window.location.href.indexOf('?sid=')>20) {
		window.location=window.location.href.split('/index.jsp')[0];
	} else {
		_0fw.parentNode.url=['actionform.jsp?action=base.MainForm'].join('');
		compList.requests.unshift([_0fw.parentNode.url,['_action=initor','&_fw=_0fw'].join('')]);
	}
	</script>
</div></div>
<script type="text/javascript">
	compList.getLazyRequest();
</script>

</body>
</html>