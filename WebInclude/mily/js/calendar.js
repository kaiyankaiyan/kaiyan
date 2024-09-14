/*	$Revision: 1.3 $
*/

var controlid = null;
var currdate = null;
var startdate = null;
var enddate  = null;
var yy = null;
var mm = null;
var hh = null;
var ii = null;
var currday = null;
var addDate=true,addTime = false;
var today = new Date();

function getposition(obj) {
	var r = new Array();
	r['x'] = obj.offsetLeft;
	r['y'] = obj.offsetTop;
	while(obj = obj.offsetParent) {
		r['x'] += obj.offsetLeft;
		r['y'] += obj.offsetTop;
	}
	return r;
}
function parsedate(svalue) {
	var today=new Date();
	var dlist=/(\d+)[、。：\\.:/-](\d+)[、。：\\.:/-](\d+)\s*(\d*)[、。：\\.:/-]?(\d*)[、。：\\.:/-]?(\d*)/.exec(svalue), tlist=/(\d*)[、。：\\.:/-]?(\d*)[、。：\\.:/-]?(\d*)/.exec(svalue);
	try {
		if (addDate==true && dlist!=null) {
			var m1 = parseFloat(dlist[1]);
			var m2 = parseFloat(dlist[2]);
			var m3 = parseFloat(dlist[3]);
			var m4 = (dlist[4])!=""? parseFloat(dlist[4]) : 0;
			var m5 = (dlist[5])!=""? parseFloat(dlist[5]) : 0;
			var m6 = (dlist[6])!=""? parseFloat(dlist[6]) : 0;
			if ((m1>0 && m1<10000) && (m2>0 && m2<13) && (m3>0 && m3<32) && (m4>-1 && m4<24) && (m5>-1 && m5<60) && (m6>-1 && m6<60)) {
				return new Date(m1, m2 - 1, m3, m4, m5, m6);
			}
		} else if(addDate==false && addTime==true && tlist!=null) {
			var m1 = today.getFullYear();
			var m2 = today.getMonth() + 1;
			var m3 = today.getDate();
			var m4 = parseFloat(tlist[1]);
			var m5 = parseFloat(tlist[2]);
			var m6 = (tlist[3])!=""? parseFloat(tlist[3]) : 0;
			if ((m4>-1 && m4<24) && (m5>-1 && m5<60) && (m6>-1 && m6<60)) {
				return new Date(m1, m2 - 1, m3, m4, m5, m6);
			}
		}
	} catch(e) {
	}
	return today;
}

function settime(d, event) {
	$('calendar').style.display = 'none';
	$('calendar_year').style.display = 'none';
	$('calendar_month').style.display = 'none';
	$('calendar_hour').style.display = 'none';
	$('calendar_minute').style.display = 'none';
	var h=0,m=0;
	if (addTime==true) {
		h = parseInt($('hour').innerHTML);
		m = parseInt($('minute').innerHTML);
	}
	var x1=new Date(yy,mm,d,h,m);
	var yy1=x1.getFullYear(), mm1=x1.getMonth(), d1=x1.getDate();
	if (addDate==true && addTime==true) {
		controlid.value = yy1 + "-" + zerofill(mm1 + 1) + "-" + zerofill(d1) + ' '+zerofill(h) + ':' + zerofill(m);
	} else if (addDate==true && addTime==false) {
		controlid.value = yy1 + "-" + zerofill(mm1 + 1) + "-" + zerofill(d1);
	} else if (addDate==false && addTime==true) {
		controlid.value = zerofill(h) + ':' + zerofill(m);
	}
	if(controlid.onchange)		controlid.onchange(event);
	if(controlid.onblur)		controlid.onblur(event);
}

function showcalendar(event, controlid1, type, startdate1, enddate1) {
	controlid = controlid1;
	addDate = (type=='DateTimeType' || type=='DateType' || type=='StringType');
	addTime = (type=='DateTimeType' || type=='TimeType');
	startdate = startdate1 ? parsedate(startdate1) : false;
	enddate = enddate1 ? parsedate(enddate1) : false;
	currday = controlid.value ? parsedate(controlid.value) : today;
	hh = currday.getHours();
	ii = currday.getMinutes();
	var p = getposition(controlid);
	$('calendar').style.display = 'block';
	$('calendar').style.left = (p['x'] + 2)+'px';
	$('calendar').style.top	= (p['y'] + 20)+'px';
	refreshcalendar(currday.getFullYear(), currday.getMonth());
	if ($('calendar_year_' + currday.getFullYear()))		$('calendar_year_' + currday.getFullYear()).className = 'calendar_checked';
	if ($('calendar_year_' + today.getFullYear()))			$('calendar_year_' + today.getFullYear()).className = 'calendar_today';
	$('calendar_month_' + (currday.getMonth() + 1)).className = 'calendar_checked';
	$('calendar_month_' + (today.getMonth() + 1)).className = 'calendar_today';
	for (var rows=G_Element.getParentByTag($('hourminute'), 'TABLE').rows, ri=rows.length-1; ri-->0;) {
		rows[ri].style.display = addDate ? '' : 'none';
	}
	$('hourminute').style.display = addTime ? '' : 'none';
	$('confirmtime').style.display = (!addDate && addTime) ? '' : 'none';
}

function refreshcalendar(y, m) {
	var x = new Date(y, m, 1);
	var mdays = new Date(y, m+1, 0).getDate();
	var mv = x.getDay()+7;
	yy = x.getFullYear();
	mm = x.getMonth();
	$("year").innerHTML = yy;
	$("month").innerHTML = mm + 1 > 9  ? (mm + 1) : '0' + (mm + 1);
	for(var i=0,d=-mv+1,dd=null; i++<mv;d++) {
		dd = $("d" + i);
		x = new Date(y, m, d);
		dd.innerHTML = x.getDate();
		dd.date = d;
		dd.className = 'calendar_expire';
	}
	for (var d=0,dd=null; d++<mdays;) {
		dd = $("d" + (d + mv));
		x = new Date(y, m, d);
		dd.innerHTML = d;
		dd.date = d;
		if(d==today.getDate() && x.getMonth()==today.getMonth() && x.getFullYear()==today.getFullYear()) {
			dd.className = 'calendar_today';
		} else if(d==currday.getDate() && x.getMonth()==currday.getMonth() && x.getFullYear()==currday.getFullYear()) {
			dd.className = 'calendar_checked';
		} else {
			dd.className = 'calendar_default';
		}
	}
	for (var i=mdays+mv,d=mdays+1,dd; i++<56; d++) {
		dd = $("d" + i);
		x = new Date(y, m, d);
		dd.innerHTML = x.getDate();
		dd.date = d;
		dd.className = 'calendar_expire';
	}
	if(addTime) {
		$('hour').innerHTML = zerofill(hh);
		$('minute').innerHTML = zerofill(ii);
	}
}

function clickDay(td, event) {
	var d = parseInt(td.date);
	if (d) {
		settime(d, event);
	}
}

function clickToday(event) {
	today = new Date();
	yy = today.getFullYear();
	mm = today.getMonth();
	var d = today.getDate();
	$('hour').innerHTML = today.getHours();
	$('minute').innerHTML = today.getMinutes();
	settime(d, event);
}

function clickTotime(event) {
	if (addDate==true && addTime==true) {
		today = new Date();
		$('hour').innerHTML = today.getHours();
		$('minute').innerHTML = today.getMinutes();
	} else if (addDate==false && addTime==true) {
		today = new Date();
		yy = today.getFullYear();
		mm = today.getMonth();
		var d = today.getDate();
		$('hour').innerHTML = today.getHours();
		$('minute').innerHTML = today.getMinutes();
		settime(d);
	}
}

function clickTime(event) {
	today = new Date();
	yy = today.getFullYear();
	mm = today.getMonth();
	var d = today.getDate();
	settime(d, event);
}

function showdiv(id) {
	var p = getposition($(id));
	$('calendar_' + id).style.left = p['x']+'px';
	$('calendar_' + id).style.top = (p['y'] + 16)+'px';
	$('calendar_' + id).style.display = 'block';
}
