var G_Element = new GElement(), G_Float = new GFloat();
var isFirefox = navigator.userAgent.indexOf("Firefox") != -1;
var isIE = navigator.userAgent.indexOf("MSIE") != -1;
var isOpera = navigator.userAgent.indexOf("Opera") != -1;
var isSafari = navigator.userAgent.indexOf("AppleWebKit") != -1;
var isMobile = navigator.userAgent.match(/(iPhone|iPod|Android|ios|iPad)/i);

function GElement(){
	this.camelizeList = new Array();
}
// get elem's children, get elem's index child
GElement.prototype.getChildren = function(elem, childIndex) {
	var elementNodes = new Array();
	var cnt = 0;
	for (var items=elem.childNodes,item,i=0; i<items.length;i++){
		item = items[i];
		if (item.nodeType == 1){
			if (cnt == childIndex)			return item;
			elementNodes[cnt++] = item;
		}
	}
	if (isNaN(childIndex)==false)				return null;
	return elementNodes;
}
GElement.prototype.getChildById = function(elem, id) {
	var items= elem.childNodes;
	for (var item,itemRtn,isize=items.length,i=0; i<isize;i++){
		item = items[i];
		if (item.id == id)
			return item;
		if (item.tagName && (itemRtn=this.getChildById(item, id))!=null)
			return itemRtn;
	}
	return null;
}
GElement.prototype.getChildByClass = function(elem, clazz) {
	var items=elem.childNodes;
	for (var item,i=0; i<items.length;i++){
		item = items[i];
		if (item.className == clazz)		return item;
	}
	for (var item,i=0; i<items.length;i++){
		item = items[i];
		var itemRtn = this.getChildByClass(item, clazz);
		if (itemRtn != null)				return itemRtn;
	}
	return null;
}
GElement.prototype.getChildsByClass = function(elem, clazz, rtnList) {
	if (document.body.getElementsByClassName) {
		for (var items=elem.getElementsByClassName(clazz),item=null,i=0,isize=items.length; i<isize; i++) {
			item = items[i];
			rtnList.push(item);
		}
		return ;
	}
	var items=elem.childNodes;
	for (var item,i=0; i<items.length;i++){
		item = items[i];
		if (item.className==clazz)		rtnList.push(item);
	}
	for (var item,i=0; i<items.length;i++){
		item = items[i];
		this.getChildsByClass(item, clazz, rtnList);
	}
}
GElement.prototype.getParentByClass = function(elem, clazz) {
	for (var cur=elem,prt=cur.parentNode; ; cur=prt,prt=cur.parentNode) {
		if (cur.className == clazz) {
			return cur;
		}
		if (prt && prt.bornSource && prt.bornSource.bornParent)
			prt=prt.bornSource.bornParent;
		if (!prt || prt==document.body.parentNode)
			break;
	}
	return null;
}
GElement.prototype.getParentByTag = function(elem, tagPath) {
	var tagList=tagPath.split(/\./g), ti=0, tsize=tagList.length, tagName=tagList[ti];
	for (var cur=elem.parentNode,prt=cur.parentNode; cur!=null; cur=prt,prt=cur.parentNode) {
		if (cur.tagName == tagName) {
			if (++ti<tsize) {
				tagName=tagList[ti];
			} else
				return cur;
		}
		if (!prt || prt==document.body.parentNode) {
			break;
		}
	}
	return null;
}
GElement.prototype.getStyle = function(elem, styleName) {
	var value = elem.style[this.camelize(styleName)];
	if (!value) {
		if (document.defaultView && document.defaultView.getComputedStyle) {
			var css = document.defaultView.getComputedStyle(elem, null);
			value = css ? css.getPropertyValue(styleName) : null;
		} else if (elem.currentStyle) {
			value = elem.currentStyle[this.camelize(styleName)];
		}
	}
	if (/left|right|height|width|top|buttom/.test(styleName)) {
		if (/px/.test(value) == true) {
			value = parseInt(value.replace(/px/g, ''));
		} else if (value=='auto' || (window.opera && this.getStyle(elem, 'position')=='static')) {
			value = 0;
		} else if (isBlank(value)) {
			value = 0;
		}
	}
	return value;
}
GElement.prototype.setStyle = function(elem, styleName, styleValue) {
	try {
		elem.style[this.camelize(styleName)] = styleValue;
	}catch(e) {
		Validator.InsertError(null, e.message);
	}
	return elem;
}
// get element absolute position
GElement.prototype.getPosition = function(elem) {
	var valueT=0, valueL=0;
	for (var element=elem; element; element=element.offsetParent){
		valueT += element.offsetTop || 0;
		valueL += element.offsetLeft || 0
	}
	return [valueT, valueL, valueT+elem.offsetHeight, valueL+elem.offsetWidth];
}
GElement.prototype.isFloat = function(elem) {
	var v = this.getStyle(elem, 'z-index');
	return v!=null && v>0;
}
GElement.prototype.isDisplay = function(elem) {
	var v = this.getStyle(elem, 'display');
	return v != 'none';
}
GElement.prototype.getWidth = function(elem, isGetClientWidth) {
	var w = elem.clientWidth;
	var sw = elem.style.width;
	if (isNaN(isGetClientWidth))	isGetClientWidth = true;
	if (isGetClientWidth) {
		w -= this.getStyle(elem,'padding-left') + this.getStyle(elem,'padding-right');
		w -= this.getStyle(elem,'border-left-width') + this.getStyle(elem,'border-right-width');
	}
	return w;
}
// set element inner width
GElement.prototype.setWidth = function(elem, elemw) {
//	elem.style.width = elemw;
	var tow = elemw - this.getMarginWidth(elem);
	if (tow >= 0)	this.setStyle(elem, 'width', tow);
}
GElement.prototype.getMarginWidth = function(elem) {
	var prt = elem.parentNode;
	var marginw = this.getStyle(elem,'margin-left') + this.getStyle(elem,'margin-right');
	marginw += this.getStyle(prt,'padding-left') + this.getStyle(prt,'padding-right');
	return marginw;
}
// get iframe window
GElement.prototype.getWindow = function(iframe) {
	var win = null;
	if (iframe.window) {
		win = iframe.window;
	} else if (iframe.contentWindow) {
		win = iframe.contentWindow;
	}
	return win;
}
// standardize styleName
GElement.prototype.camelize = function(styleName) {
	for (var items=this.camelizeList, item, i=0; i<items.length; i++) {
		item = items[i];
		if (item[0] == styleName)		return item[1];
	}
	var oStringList = styleName.split('-');
	if (oStringList.length == 1) 	return oStringList[0];

	var camelizedString = styleName.indexOf('-') == 0
		? oStringList[0].charAt(0).toUpperCase() + oStringList[0].substring(1)
		: oStringList[0];

	for (var i = 1, len = oStringList.length; i < len; i++) {
		var s = oStringList[i];
		camelizedString += s.charAt(0).toUpperCase() + s.substring(1);
	}
	this.camelizeList.push([styleName, camelizedString]);
	return camelizedString;
}
GElement.prototype.searchParentByTagName = function(elem, tagname) {
	var e = null;
	for (var cur=elem,prt=cur.parentNode; ; cur=prt,prt=cur.parentNode) {
		if (cur.tagName==tagname.toUpperCase() && cur!=elem) {
			e = cur;
			break;
		}
		if (!prt || prt==document.body.parentNode) {
			break;
		}
	}
	return e;
}

function GFloat() {
}
GFloat.prototype.scrollx = function(div, dWidth) {
	G_Element.setStyle(div, 'width', dWidth);
	G_Element.setStyle(div, 'overflow-x', 'scroll');
}
GFloat.prototype.scrolly = function(div, dHeight) {
	G_Element.setStyle(div, 'height', dHeight);
	G_Element.setStyle(div, 'overflow-y', 'scroll');
}
// to make div contain elem
GFloat.prototype.contain = function(elem) {
	var div=elem.parentNode, posi = G_Element.getPosition(div);
	div.style.height = elem.offsetHeight;
}
GFloat.prototype.center = function(div, container) {
	if (!container)		container=document.body;
	var cw = container.clientWidth, ch = container.clientHeight;
	var dw = div.clientWidth, dh = div.clientHeight;
	var itop = (ch-dh)/2, ileft = (cw-dw)/2;
	if (itop < 0) {
		itop = 0;
		div.style.height = ch;
	}
	if (ileft < 0) {
		ileft = 0;
		div.style.width = cw;
	}
	G_Element.setStyle(div, 'top', itop+document.body.scrollTop);
	G_Element.setStyle(div, 'left', ileft+document.body.scrollLeft);
}
GFloat.prototype.topRight = function(div, container) {
	var cposi = G_Element.getPosition(container), dposi = G_Element.getPosition(div);
	var cr = cposi[3], cw = container.clientWidth, dw =	div.clientWidth+4;
	var dl = isFirefox? cr-dw: cw-dw;
	G_Element.setStyle(div, 'left', dl);
}
String.prototype.contains = function(s) {
	return this.indexOf(s) > -1;
}
String.prototype.startWith = function(s) {
	return this.indexOf(s) == 0;
}
String.prototype.endWith = function(s) {
	return this.lastIndexOf(s) == this.length - s.length;
}
String.prototype.toInt = function() {
	var s = parseFloat(this.replace(/(^[\s0]+)|(\s+$)/g, ''));
	return parseInt(s);
}
String.prototype.len = function() {
	return this.replace(/[^\x00-\xff]/g, "xx").length;
}
Date.prototype.format = function(formatStr) { 
	var str = formatStr; 
	str=str.replace(/yyyy|YYYY/,this.getFullYear()); 
	str=str.replace(/yy|YY/,(this.getYear() % 100)>9?(this.getYear() % 100).toString():'0' + (this.getYear() % 100)); 
	str=str.replace(/MM/,this.getMonth()>9?this.getMonth().toString():'0' + this.getMonth()); 
	str=str.replace(/M/g,this.getMonth()); 
	str=str.replace(/dd|DD/,this.getDate()>9?this.getDate().toString():'0' + this.getDate()); 
	str=str.replace(/d|D/g,this.getDate()); 
	str=str.replace(/hh|HH/,this.getHours()>9?this.getHours().toString():'0' + this.getHours()); 
	str=str.replace(/h|H/g,this.getHours()); 
	str=str.replace(/mm/,this.getMinutes()>9?this.getMinutes().toString():'0' + this.getMinutes()); 
	str=str.replace(/m/g,this.getMinutes()); 
	str=str.replace(/ss|SS/,this.getSeconds()>9?this.getSeconds().toString():'0' + this.getSeconds()); 
	str=str.replace(/s|S/g,this.getSeconds()); 
	return str; 
} 

function isBlank(str) {
	return (!str)==true || /^\s*$/.test(str);
}
function encode(str){
	return str.replace(/[<>&'"]/g,
	function(c) {
		switch (c) {
		case '<': return '&lt;';
		case '>': return '&gt;';
		case '&': return '&amp;';
		case '\'': return '&apos;';
		case '"': return '&quot;';
		}
	});
}
function decode(str){
	return str.replace(/(&lt;)|(&gt;)|(&amp;)|(&apos;)|(&quot;)|(&#160;)/g,
	function(c) {
		switch (c) {
		case '&lt;': return '<';
		case '&gt;': return '>';
		case '&amp;': return '&';
		case '&apos;': return '\'';
		case '&quot;': return '"';
		case '&#160;': return ' ';
		}
	});
}
function hexEncode(str) {
	var bytes = getStringToByte(str);
	var baos = new Array();
	for (var i=0,abyte=null; i<bytes.length; i++) {
		abyte = bytes[i].toString(16).toUpperCase();
		baos.push(abyte);
	}
	return baos.join("");
}
function hexDecode(str) {
	if (isUndefined(str))
		str = '';
	var hexString="0123456789ABCDEFabcdef", bytes=str.toUpperCase();
	var baos = new Array();
	for (var i=0,abyte=null; i < bytes.length; i += 2) {
		abyte = hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i+1));
		baos.push(abyte);
	}
	return getByteToString(baos);
}
function getStringToByte(str) {
	var bytes = new Array();
	var len = str.length;
	for(var i=0,c; i<len; i++) {
		c = str.charCodeAt(i);
		if(c >= 0x010000 && c <= 0x10FFFF) {
			bytes.push(((c >> 18) & 0x07) | 0xF0);
			bytes.push(((c >> 12) & 0x3F) | 0x80);
			bytes.push(((c >> 6) & 0x3F) | 0x80);
			bytes.push((c & 0x3F) | 0x80);
		} else if(c >= 0x000800 && c <= 0x00FFFF) {
			bytes.push(((c >> 12) & 0x0F) | 0xE0);
			bytes.push(((c >> 6) & 0x3F) | 0x80);
			bytes.push((c & 0x3F) | 0x80);
		} else if(c >= 0x000080 && c <= 0x0007FF) {
			bytes.push(((c >> 6) & 0x1F) | 0xC0);
			bytes.push((c & 0x3F) | 0x80);
		} else {
			bytes.push(c & 0xFF);
		}
	}
	return bytes;
}
function getByteToString(arr) {
	var str='', _arr=arr;
	for(var i=0; i < _arr.length; i++) {
		var one=_arr[i].toString(2), v=one.match(/^1+?(?=0)/);
		if(v && one.length == 8) {
			var bytesLength = v[0].length;
			var store = _arr[i].toString(2).slice(7 - bytesLength);
			for(var st = 1; st < bytesLength; st++) {
				store += _arr[st + i].toString(2).slice(2);
			}
			str += String.fromCharCode(parseInt(store, 2));
			i += bytesLength - 1;
		} else {
			str += String.fromCharCode(_arr[i]);
		}
	}
	return str;
}
function ltrim(str) {
	if (typeof(str) == 'string') {
		return str.replace(/^\s+/, '');
	}
}
function rtrim(str) {
	if (typeof(str) == 'string') {
		return str.replace(/\s+$/, '');
	}
}
function trim(str){
	if (typeof(str) == 'string') {
		return str.replace(/^\s+|\s+$/g, '');
	}
}
// get item index from items in array
function indexOf(items, item) {
	for (var i = 0; i < items.length; i++) {
		if (items[i] == item) return i;
	}
	return -1;
}
function isUndefined(variable) {
	return typeof variable=='undefined'? true: false;
}
function in_array(needle, haystack) {
	if (typeof needle == "string") {
		for (var i in haystack) {
			if (haystack[i] == needle) {
				return true;
			}
		}
	}
	return false;
}
function checkFocus() {
	var obj = isUndefined(editMode) || !editMode ? $("postform").message : editwin;
	if (!obj.hasfocus) {
		obj.focus();
	}
}
function zerofill(s) {
	var s = parseFloat(s.toString().replace(/(^[\s0]+)|(\s+$)/g, ''));
	s = isNaN(s) ? 0 : s;
	return (s < 10 ? '0' : '') + s.toString();
}
