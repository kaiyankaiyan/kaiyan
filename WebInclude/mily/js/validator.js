
/*************************************************
Validator v1.02
*************************************************/
Validator = {
	Require : /.+/,
	Email : /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/,
	Phone : /^((\(\d{3}\))|(\d{3}\-))?(\(0\d{2,3}\)|0\d{2,3}-)?[1-9]\d{6,7}$/,
	Mobile : /^((\(\d{3}\))|(\d{3}\-))?1\d{10}$/,
	URL : /^http:\/\/[A-Za-z0-9]+\.[A-Za-z0-9]+[\/=\?%\-&_~`@[\]\':+!]*([^<>\"\"])*$/,
	IdCard : /^\d{15}(\d{2}[A-Za-z0-9])?$/,
	Currency : /^\d+(\.\d+)?$/,
	Number : /^\d+$/,
	Zip : /^[1-9]\d{5}$/,
	QQ : /^[1-9]\d{4,8}$/,
	Integer : /^[-\+]?\d+$/,
	Double : /^[-\+]?\d+(\.\d+)?$/,
	Double1 : /^[-\+]?(\d{1,3}(,\d\d\d)*(\.\d+)?|\d+(\.\d+)?)$/,
	English : /^[A-Za-z]+$/,
	Chinese : /^[\u0391-\uFFE5]+$/,
	UnSafe : /^(([A-Z]*|[a-z]*|\d*|[-_\~!@#\$%\^&\*\.\(\)\[\]\{\}<>\?\\\/\'\"]*)|.{0,5})$|\s/,
	IsSafe : "true || !this['UnSafe'].test(elemValue)",
	Date : "Validator.IsDate(elemValue, getAttribute('format'), elem)",
	Repeat : "elemValue == document.getElementsByName(getAttribute('to'))[0].value",
	Compare : "this['Double'].test(elemValue) && this['Double'].test(getAttribute('to')) && Validator.compare(elemValue,getAttribute('operator'),getAttribute('to'))",
	Custom : "Validator.Exec(elemValue, getAttribute('regexp'))",
	Group : "Validator.MustChecked(getAttribute('groupname'), getAttribute('greaterthan'), getAttribute('lessthan'))",

	errorList: new Array(),
Validate : function(elements){
	var ok = true;
	for (var i=0,isize=elements.length,item=null,itemValue=null; i<isize; i++) {
		item = elements[i];
		if (item==null)
			continue;
		if (item.onblur)		item.onblur();
		itemValue = item.value;
		if (isUndefined(item.tvalue)==false) {
			itemValue=item.tvalue;
		} else if (isUndefined(item.svalue)==false) {
			itemValue=item.svalue;
		}
		if (this.itemValidate(item, itemValue)==false)
			ok = false;
	}
	return ok;
},
itemValidate : function(elem, elemValue) {
	Validator.ClearState(elem);
	var isValid = (elem.validate)? windowEval.compScript(elem, elem.validate, false): true;
	if (elem.getAttribute('datatype')) {
		if((elem.getAttribute('datatype')||'').contains('Require')==false && isBlank(elemValue)) 		return true;
		for (var typeList=(elem.getAttribute('datatype') || '').split(/\,/),tsize=typeList.length,ti=0; ti<tsize && isValid; ti++) {
			var _dataType=typeList[ti];
			if(isBlank(_dataType)==true) 		continue;
			with(elem)
			switch(_dataType){
				case 'Date' :
				case 'Repeat' :
				case 'Compare' :
				case 'Custom' :
				case 'IsSafe' :
					isValid = isValid && eval(Validator[_dataType]);
					break;
				case 'Group' :
					isValid = eval(Validator[_dataType]);
					ti=tsize;
					break;
				case 'Range' :
				case 'Limit' :
					if (_dataType=='Limit')			elemValue=elemValue.length;
				case 'LimitB' :
					if (_dataType=='LimitB')		elemValue=Validator.LenB(elemValue);
					var sg=getAttribute('greater'), sgth=getAttribute('greaterthan'), sl=getAttribute('less'), slth=getAttribute('lessthan');
					var g=(sg==null?null:eval(sg)), gth=(sgth==null?null:eval(sgth)), l=(sl==null?null:eval(sl)), lth=(slth==null?null:eval(slth));
					isValid = isValid && this.limit(eval(elemValue),g,gth,l,lth);
					break;
				default :
					isValid = isValid && Validator[_dataType].test(elemValue);
					break;
			}
		}
	}
	if (!isValid)
		Validator.AddError(elem, elem.getAttribute('placeholder'));
	return isValid;
},
limit : function(len, greater, greaterthan, less, lessthan){
	var ok=true;
	if (ok && greater!=null)		ok=len>greater;
	if (ok && greaterthan!=null)	ok=len>=greaterthan;
	if (ok && less!=null)			ok=less<len;
	if (ok && lessthan!=null)		ok=lessthan<len;
	return ok;
},
LenB : function(str){
	return str.replace(/[^\x00-\xff]/g,'**').length;
},
ClearState : function(inputElem){
	if (inputElem==null)		throw 'clear state element is null';
	var elem = inputElem.id=='errorPanel'? inputElem: inputElem.parentNode;
	for (var i=0, childs=elem.children; childs && i<childs.length; i++) {
		var item = childs[i];
		if (item.className == 'warn') {
			elem.removeChild(item);
			break;
		} else if (item.className=='floatWarn' || item.className=='floatError') {
			elem.removeChild(item);
			elem.style.height = 0;
			break;
		}
	}
	this.pushItem(inputElem, false);
},
AddError : function(inputElem, errorMsg){
	if (inputElem.hasError==true)		this.ClearState(inputElem);
	var prt = inputElem.parentNode;
	var errorPnl = document.createElement( 'div' );
	errorPnl.className = 'warn'
	errorPnl.innerHTML = errorMsg;
	if (inputElem.tagName=='TABLE') {
		prt.insertBefore(errorPnl, inputElem);
	} else {
		prt.appendChild(errorPnl);
	}
	this.pushItem(inputElem, true);
},
InsertWarn: function(bodyElem, errorMsg) {
	if (!bodyElem)		bodyElem = $('errorPanel');
	if (bodyElem.style.display=='none') {
		bodyElem.style.display = '';
	}
	var errorPnl = document.createElement( 'div' );
	errorPnl.className = 'floatWarn';
	errorPnl.innerHTML = errorMsg;
	bodyElem.innerHTML = '';
	bodyElem.appendChild(errorPnl);
	bodyElem.style.height = errorPnl.offsetHeight;
	this.pushItem(bodyElem, true);
	return errorPnl;
},
InsertError: function(bodyElem, errorMsg) {
	if (!bodyElem)		bodyElem = $('errorPanel');
	if (bodyElem.style.display=='none') {
		bodyElem.style.display = '';
	}
	var errorPnl = document.createElement( 'div' );
	errorPnl.className = 'floatError';
	errorPnl.innerHTML = errorMsg;
	bodyElem.innerHTML = '';
	bodyElem.appendChild(errorPnl);
	bodyElem.style.height = errorPnl.offsetHeight;
	this.pushItem(bodyElem, true);
	return errorPnl;
},
pushItem : function(inputelem, hasError){
	inputelem.hasError = hasError;
	var list=this.errorList, fsize=list.length;
	if (hasError==true)					list.push(inputelem);
	for (var i=list.length, item, chg=false; i-->0; chg=false) {
		item = list[i];
		if (item.hasError==false)		chg=true;
		if (item.offsetHeight==0)		chg=true;
		if (chg==true)					list.splice(i, 1);
	}
	if (fsize!=list.length)				compList.moveFloatlist();
},
Exec : function(op, reg){
	return new RegExp(reg,'g').test(op);
},
compare : function(op1,operator,op2){
	switch (operator) {
		case 'NotEqual':
			return (op1 != op2);
		case 'GreaterThan':
			return (op1 > op2);
		case 'GreaterThanEqual':
			return (op1 >= op2);
		case 'LessThan':
			return (op1 < op2);
		case 'LessThanEqual':
			return (op1 <= op2);
		default:
			return (op1 == op2);
	}
},
MustChecked : function(name, min, max){
	var groups = document.getElementsByName(name);
	var hasChecked = 0;
	min = min || 1;
	max = max || groups.length;
	for(var i=groups.length-1;i>=0;i--) {
		if(groups[i].checked) hasChecked++;
	}
	return min <= hasChecked && hasChecked <= max;
},
IsDate : function(elemValue, formatString, elem){
	formatString = formatString || 'ymd';
	var date = this.ToDate(elemValue, formatString);
	if (date != null) {
		if (elem) {
			elem.value = this.FormatDate(date, formatString);
		}
		return true;
	}
	return false;
},
FormatDate : function(date, formatString) {
	var sdate='';
	if (date == null)	return sdate;
	switch (formatString) {
		case 'ymd':
			sdate = date.getFullYear() + '/' + zerofill(date.getMonth()+1) + '/' + zerofill(date.getDate());
			break;
		case 'ymdhms':
			sdate = date.getFullYear() + '/' + zerofill(date.getMonth()+1) + '/' + zerofill(date.getDate()) + ' ' +
				zerofill(date.getHours()) + ':' + zerofill(date.getMinutes()) + ':' + zerofill(date.getSeconds());
			break;
		case 'ymdhm':
			sdate = date.getFullYear() + '/' + zerofill(date.getMonth()+1) + '/' + zerofill(date.getDate()) + ' ' +
				zerofill(date.getHours()) + ':' + zerofill(date.getMinutes());
			break;
		case 'hms':
			sdate = zerofill(date.getHours()) + ':' + zerofill(date.getMinutes()) + ':' + zerofill(date.getSeconds());
			break;
		case 'hm':
			sdate = zerofill(date.getHours()) + ':' + zerofill(date.getMinutes());
			break;
		default:
			break;
	}
	return sdate;
},
ToDate : function(elemValue, formatString){
	var today=new Date();
	var dlist=/(\d+)[、。：\\.:/-](\d+)[、。：\\.:/-](\d+)\s*(\d*)[、。：\\.:/-]?(\d*)[、。：\\.:/-]?(\d*)/.exec(elemValue), tlist=/(\d*)[、。：\\.:/-]?(\d*)[、。：\\.:/-]?(\d*)/.exec(elemValue);
	try {
		if ("ymdhms,ymdhm,ymd,".indexOf(formatString+",")>-1 && dlist != null) {
			var m1 = parseFloat(dlist[1]);
			var m2 = parseFloat(dlist[2]);
			var m3 = parseFloat(dlist[3]);
			var m4 = (dlist[4])!=""? parseFloat(dlist[4]) : 0;
			var m5 = (dlist[5])!=""? parseFloat(dlist[5]) : 0;
			var m6 = (dlist[6])!=""? parseFloat(dlist[6]) : 0;
			if ((m1>0 && m1<10000) && (m2>0 && m2<13) && (m3>0 && m3<32) && (m4>-1 && m4<24) && (m5>-1 && m5<60) && (m6>-1 && m6<60)) {
				return new Date(m1, m2 - 1, m3, m4, m5, m6);
			}
		} else if("hms,hm,".indexOf(formatString+",")>-1 && tlist != null) {
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
	return null;
}
}
