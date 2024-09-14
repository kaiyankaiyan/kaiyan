/**response xml format:
 <mily-response>
 	<content id='' action='0'>
 		
 		...[component element]
 	</content>
 </mily-response>

 action					command
	 0						update the component
	 1						add the component
	 2						remove the component
	 
	 20					use object
 */
var ajax;
var loading;
var compList;

var headUpdater = new L_headUpdater();
var throwableHandler = new L_throwableHandler();
var windowEval = new L_windowEval();
var windowAlert = new L_windowAlert();

function MilyAjax(){
	this.http = null;
	this.nextReady = true;
}
MilyAjax.prototype.init=function(http){
	if(http!=null){
		return http;
	}
	//branch for native XMLHttpRequest object
	if(window.XMLHttpRequest){
		try{
			http = new XMLHttpRequest();
		}catch(e){
			http = null;
		}
	}
	else if(window.ActiveXObject){
		try{
			http = new ActiveXObject('Msxml2.XMLHTTP');
		}catch(e){
			try{
				http = new ActiveXObject('Microsoft.XMLHttp');
			}catch(e){
				http = null;
			}
		}
	}
	return http;
}
MilyAjax.prototype.request=function(furl, doc){
	this.nextReady = false;
	if (!furl)
		furl = document.URL;
	loading.start();
	this.http = this.init(this.http);
	if(this.http){
		this.http.open('POST',furl,true);
		this.http.setRequestHeader('Content-Type','application/x-www-form-urlencoded;charset=utf-8');
		try {
			this.http.send(doc);
		}catch(e) {
		}
//		console.info(['send',furl, doc].join('\t'));
		this.http.onreadystatechange = this.response.bind(this);
	}
}
MilyAjax.prototype.response=function(){
	var http = this.http;
	var isReady = false;
	try {
		if ((http.readyState==4 && http.status==200)==false)
			return ;
		isReady = (http.responseXML!=null || http.response!=null);
	}catch(e) {
	}
	if(isReady){
		try {
			console.info(http.response);
			this.attributeNode(http.responseXML);
			this.process(http.responseXML);
			compList.resetFoots();
			compList.resetFloatlist();
			compList.reset();
		}catch(e){
			Validator.InsertError(null, e.stack);
			window.location='timeout.jsp';
		}
		http.abort();
		windowEval.evalHandle(window.document.body, "loading.end();compList.moveFloatlist();", 10);
		windowEval.evalHandle(window.document.body, "ajax.nextReady=true;compList.getLazyRequest();", 100);
	}
}
MilyAjax.prototype.process=function(xmlDoc){
	var contents = xmlDoc.getElementsByTagName('content');
	var activeId='';
	if (document.activeElement) {
		activeId=document.activeElement.xml.match(/id\=\"\w+\"/g);
		activeId=(activeId && activeId.length>0)? activeId[0]: '';
		activeId=activeId.substring(4,activeId.length-1);
	}
	for(var contentIndex=0; contentIndex<contents.length; contentIndex++){
		var contentItem=contents.item(contentIndex), id=contentItem.getAttribute('id');
		var action = parseInt(contentItem.getAttribute('action')), html = null;
		var chgElem=$(id), prtElem=chgElem==null? null: chgElem.parentNode;
		if (!chgElem && action<=2)					continue;
		switch (action) {
			case 0: // update component
				Validator.ClearState(chgElem);
				chgElem.outerHTML = L_getNodeXmls(contentItem.childNodes);
				break;
			case 1: // add component
				var html = L_getNodeXmls(contentItem.childNodes);
				var pnl = document.createElement( 'div' );
				pnl.innerHTML = html;
				for (var inw=0,nwelems=pnl.children,nw; inw<nwelems.length; inw++) {
					nw = nwelems[inw];
					chgElem.appendChild(nw);
				}
				break;
			case 2: // remove component
				prtElem.removeChild(chgElem);
				break;
			case 20:
				var nme=contentItem.getAttribute('name').replace('L_', '');
				var updater = eval(nme);
				updater.responseHandle(contentItem);
				break;
		}
	}
	if (activeId.length>0)
		try{eval(activeId).focus();}catch(e){}
}
MilyAjax.prototype.attributeNode=function(node) {
	var id=null;
	if (node.attributes && (id=node.getAttribute('id'))) {
		for(var attrs=node.attributes,attr,ai=attrs.length,aname,avalue,ascript,rm=0; ai-->0; rm=0){
			attr = attrs[ai];
			aname = attr.nodeName;
			avalue = attr.nodeValue;
			if (typeof(avalue)=='string') {
				avalue = decode(avalue);
				try {attr.nodeValue = avalue;}catch(e){}
			}
			if (aname.startWith('af_')) {
				ascript = 'this.'+aname.substring(3)+'=function(){'+avalue+'}';
				compList.addInit(id, ascript, 0), rm++;
			} else if (aname.startWith('as_')) {
				ascript = 'this.'+aname.substring(3)+'='+avalue;
				compList.addInit(id, ascript, 0), rm++;
			} else if (aname.startWith('ats_')) {
				ascript = 'var sval=\"'+avalue.replace(/\"/g, '\\"').replace(/\s{2,}/g, '')+'\"; this.'+aname.substring(4)+'=sval;';
				compList.addInit(id, ascript, 0), rm++;
			} else if (aname.startWith('ads_')) {
				ascript = 'var sval=\"'+avalue.replace(/\"/g, '\\"').replace(/\s{2,}/g, '')+'\"; this.'+aname.substring(4)+'=sval;';
				compList.addInit(id, ascript, 0), rm++;
			} else if (aname=='initscript0') {
				ascript = avalue;
				compList.addInit(id, ascript, 0), rm++;
			} else if (aname=='initscript1') {
				ascript = avalue;
				compList.addInit(id, ascript, 1), rm++;
			} else if (aname=='initscript2') {
				ascript = avalue;
				compList.addInit(id, ascript, 2), rm++;
			}
			if (rm>0 && node.tagName==node.tagName.toLowerCase())
				node.removeAttribute(aname);
		}
	}
	if (node.childNodes) {
		for (var clist=node.childNodes,citem,ci=clist.length; ci-->0;) {
			citem = clist[ci];
			if (citem.nodeType==3)
				citem.nodeValue = decode(citem.nodeValue);
			this.attributeNode(citem);
		}
	}
}
function L_createHtmlElement(nodes, userElement){
	var ElemNews = new Array();
	for (var nindex=0,node=null; nindex<nodes.length; nindex++) {
		node = nodes[nindex];
		if (node.nodeType==3 && isBlank(node.nodeValue)) {
			continue;
		} else if(node.nodeType==3) {
			var text = document.createTextNode(node.nodeValue);
			userElement.appendChild(text);
		} else {
			var htmlElement = document.createElement(node.nodeName);
			var attrs = node.attributes;
			ElemNews.push(htmlElement);
			for(var i=0,aname,avalue; attrs && i<attrs.length; i++){
				aname = attrs[i].nodeName;
				avalue = attrs[i].nodeValue;
				htmlElement.setAttribute(aname,avalue);
			}
			L_createHtmlElement(node.childNodes, htmlElement);
			if (node.offsetWidth)	htmlElement.style.width=node.offsetWidth;
			userElement.appendChild(htmlElement)
		}
	}
	return ElemNews;
}

function L_action(eventId) {
	var elem = (eventId && eventId.tagName)? (!eventId.sid? eventId: $(eventId.sid)): $(eventId);
	var validateId = (elem && elem.getAttribute)? elem.getAttribute('needValidate'): null;
	var ok = elem? true: false;
	if (validateId) {
		var elems=$(null, validateId);
		if (!Validator.Validate(elems))		ok=false;
	}
	if (ok) {
		Validator.ClearState($('errorPanel'));
		compList.addEvent(elem);
	}
}
function L_confirm(info, yesDo) {
	$('confirm_info').innerHTML = info;
	var panel=$('confirm'), byes=$('confirm_byes');
	byes.outerHTML = "<a id=\"confirm_byes\" ondblclick=\""+yesDo+"\">"+byes.innerHTML+"</a>";
	panel.style.display='';
	G_Float.center(panel);
}
function L_cyclingDo(elem, whilescript, doscript, interseconds, prepareseconds) {
	if (isDead(elem))			return;
	if (prepareseconds) {
		return window.setTimeout("L_cyclingDo($('"+elem.id+"'),\""+whilescript+"\",\""+doscript+"\","+interseconds+")", prepareseconds*1000);
	} else {
		if (eval(whilescript)) {
			windowEval.evalHandle(elem, doscript);
			return window.setTimeout("L_cyclingDo($('"+elem.id+"'),\""+whilescript+"\",\""+doscript+"\","+interseconds+")", interseconds*1000);
		} else
			return ;
	}
}
function L_propertychange(id, newValue){
	var elem = (id && id.tagName)? (!id.sid? id: $(id.sid)): $(id);
	if (elem==null)			return;
	if (1==0 && elem.tagName=='DIV') {
		Validator.InsertError(null, "elem.svalue"+elem.svalue+"，newValue_"+newValue);
	}
	compList.addChange(elem, newValue);
	elem.svalue = newValue;
	if (elem.setter) {
		for (var alist=eval(elem.setter),aitem,asize=alist.length,ai=0; ai<asize; ai++) {
			aitem = alist[ai];
			if (aitem.getter)
				windowEval.compScript(aitem, aitem.getter);
			else
				Validator.itemValidate(aitem, aitem.svalue);
			if (aitem.onblur)		aitem.onblur();
			if (aitem.editable)		L_propertyFocus(aitem);
		}
	}
}
function L_propertychange_neighbor(elem0, evt){
	var keyCode0=evt.keyCode;
	if (keyCode0==13) {
	} else if (keyCode0==9) {
		if (evt.shiftKey==false && elem0.selectionEnd==elem0.value.length) {// right
			keyCode0 = 39;
		} else if (evt.shiftKey==true && elem0.selectionEnd==0) {// left
			keyCode0 = 37;
		}
		if (evt.keyCode!=keyCode0)
			evt.preventDefault();
		else
			return;
	} else if (35<=keyCode0 && keyCode0<=40) {
	} else {
		return;
	}
	for (var elem=elem0,next=null,keyCode=keyCode0, ncount=0; ; next=null,ncount++) {
		var neighbor=elem.getAttribute('neighbor'), bors=$(null, neighbor);
		var stLen=(elem.offsetWidth>0? elem.value.length:0), stEnd=(elem.offsetWidth>0? elem.selectionEnd:0);
		if (elem.selectionEnd==null)
			stLen=stEnd=0;
		// top
		if (keyCode==38 && bors[0]!=null) {
			next = bors[0];
			if (next.offsetWidth==0)	elem=next;
		// right
		} else if (keyCode==39 && stLen==stEnd && bors[1]!=null) {
			next = bors[1];
			if (next.offsetWidth==0)	elem=next;
		// bottom
		} else if (keyCode==40 && bors[2]!=null) {
			next = bors[2];
			if (next.offsetWidth==0)	elem=next;
		// left
		} else if (keyCode==37 && 0==stEnd && bors[3]!=null) {
			next = bors[3];
			if (next.offsetWidth==0)	elem=next;
		// home
		} else if (keyCode==36 && 0==stEnd && bors[4]!=null) {
			next = bors[4];
			if (next.offsetWidth==0) {
				elem=next;
				keyCode=39;
			}
		// end
		} else if (keyCode==35 && stLen==stEnd && bors[5]!=null) {
			next = bors[5];
			if (next.offsetWidth==0) {
				elem=next;
				keyCode=37;
			}
		} else if (keyCode==13 && bors[2]!=null) {
		// enter bottom
			next = bors[2];
			if (next.offsetWidth==0)	elem=next;
		}
		if (next!=null && next.offsetWidth>0) {
			next.focus();
			if (keyCode0==36) {// home
				document.body.scrollLeft=0;
			} else if (keyCode0==37) {// left
				next.selectionStart=next.selectionEnd=0;
			} else if (keyCode0==39) {// right
				next.selectionStart=next.selectionEnd=next.value.length;
			}
			break;
		} else if (next==null || ncount==11) {
			break;
		} else if (elem==next) {
			continue;
		}
	}
}
//auto change listview input textfield size
function L_propertychange_resize(elem, newValue){
	var td = G_Element.getParentByTag(elem, 'TD');
	var table = G_Element.getParentByTag(td, 'TABLE');
	for (; (td!=null && table!=null) && table.rows[0].className!='ListViewHeaderTwidth'; ) {
		td=G_Element.getParentByTag(table,'TD');
		if (td)
			table = G_Element.getParentByTag(td, 'TABLE');
		else
			return;
	}
	var tdi = indexOf(td.parentNode.cells, td);
	var twidth=table.rows[0].cells[tdi].getElementsByTagName('INPUT')[0], stwidth=!table.bornSource? twidth: table.bornSource.rows[0].cells[tdi].getElementsByTagName('INPUT')[0];
	if (!stwidth.tdList)
		stwidth.tdList=new Array();
	if (elem.className=='htmlfield' || (''+newValue).length>td.offsetWidth*2 || isUndefined(newValue)) {
		newValue = '';
		for (var iwidth=elem.offsetWidth/6.5+1; iwidth-->0; newValue+='A');
		stwidth.style.width=twidth.parentNode.offsetWidth;
		var td1=td.parentNode.hinput, td2=td1? td.parentNode.other.hinput: null;
		if (td2)		td2.style.height = td.offsetHeight;
	} else {
		for (var ditems=stwidth.tdList,di=ditems.length; ;) {
			if (di>0 && ditems[--di]==td)
				ditems.splice(di,1);
			if (di==0) {
				td.dvalue = newValue;
				ditems.push(td);
				di=ditems.length;
				for (var maxtd=td; di-->0;) {
					if (ditems[di].dvalue.len()>maxtd.dvalue.len())
						maxtd = ditems[di];
					if (di==0)
						td = maxtd;
				}
				break;
			}
		}
		var tdw = td.offsetWidth;
		var tdexw = td.offsetWidth - elem.offsetWidth;
		var px=6.5;
		var valuelen = td.dvalue.len();
		var size=null, tsize=(tdw-tdexw)/px;
		if (!twidth.parentNode.initsize)		twidth.parentNode.initsize=tsize;
		var initsize = twidth.parentNode.initsize;
		if (valuelen>tsize) {// longer
			size=valuelen + 10;
		} else if (valuelen<tsize && tsize==initsize) {// within init
		} else if ((tsize>initsize && valuelen<initsize) || valuelen<tsize-16) {// shorter
			size = valuelen>initsize? valuelen: initsize;
		}
		if (size != null)
			stwidth.style.width = size * px;
	}
	for (var tr0=G_Element.getParentByTag(stwidth,'TR'), tr1=tr0.other, trTable=G_Element.getParentByTag(tr0,'TABLE'), tlist0=tr0.getElementsByTagName('INPUT'), tlist=tlist0, telem, ti=tlist.length; ti-->0;) {
		telem = tlist[ti];
		for (var citems=(!trTable.cloneList? []: trTable.cloneList), ci=citems.length, cwidth=null; ci-->0;) {
			cwidth = citems[ci].rows[0].getElementsByTagName('INPUT')[ti];
			if (Math.abs(cwidth.offsetWidth-telem.parentNode.offsetWidth)>0)
				cwidth.style.width = telem.parentNode.offsetWidth;
		}
		if (ti==0 && tlist==tlist0 && tr1) {
			trTable=G_Element.getParentByTag(tr1,'TABLE');
			tlist = tr1.getElementsByTagName('INPUT');
			ti = tlist.length;
		}
	}
	compList.moveFloatlist();
}
function L_propertyFocus(elem, event, rollbackscript) {
	var ok = windowEval.compScript(elem, elem.editable, false)==true? true: false;
	if (!ok) {
		if (event && event.type)
			event.preventDefault? event.preventDefault(): (event.returnValue = false);
		if (elem.getter)
			windowEval.compScript(elem, elem.getter);
		else if (rollbackscript)
			windowEval.compScript(elem, rollbackscript);
	}
	if (elem.className=='textfield' || elem.className=='htmlfield' || elem.className=='select')
		G_Element.setStyle(elem, 'border-top-width', ok?'1px':'0px');
	return ok;
}
function L_propertydelete(elem) {
	compList.addDelete(elem);
}
function L_setWindowInited(formw) {
	if (!ajax) {
		loading = new L_loading();
		ajax = new MilyAjax();
		compList = new L_compList();
	}
	compList.doInit();
	window.embed = (formw.parentNode.url? true: false);
	var uparam = ['_action=updator', '&win'+formw.id+'=inited',
	'&attr_expltype='+(isFirefox?'isFirefox':'')+(isIE?'isIE':'')+(isOpera?'isOpera':'')+(isSafari?'isSafari':'')+(isMobile?'isMobile':''),
	compList.getAttrs(), compList.getDelwins()];
	compList.addWindow(formw);
	for (var prt=formw.parentNode,w,h; ; prt=prt.parentNode) {
		w=prt.clientWidth, h=prt.clientHeight;
		if (w>0) {
			uparam.push('&attr_width='+w+'&attr_height='+h);
			break;
		}
	}
	compList.requests.unshift([formw.parentNode.url, uparam.join('')]);
	compList.getLazyRequest();
	window.onkeydown=function(event){
		if (!event)		event=window.event;
		var wstop=true, target=(event.srcElement?event.srcElement:event.target);
		if (event.keyCode==8 && isUndefined(target.readOnly)==false) {
			wstop = (target.readOnly==true);
		} else if (event.keyCode==8 && isUndefined(target.contentEditable)==false) {
			wstop = (target.contentEditable=='true')==false;
		} else if (event.keyCode==116 && window.top!=window) {
		} else {
			wstop = false;
		}
		if (wstop==true) {
			try{
				event.preventDefault();
			}catch(e){
				event.keyCode=0;
				event.returnValue=false;
			}
			return true;
		}
	};
}
function L_getOptionValues(selectObject){
	var str = '';
	for(var i=0;i<selectObject.options.length;i++){
		if(selectObject.options(i).selected){
			str += selectObject.options(i).value + ','; 
		} 
	}
	return str;
}

function L_headUpdater(){
}

L_headUpdater.prototype.responseHandle1=function(contentItem) {
	var chgElem = document.getElementsByTagName('HEAD')[0];
	var html = L_getNodeXmls(contentItem.childNodes);
	var pnl = document.createElement( 'div' );
	pnl.innerHTML = html;
	for (var inw=0,nwelems=pnl.children,nw; inw<nwelems.length; inw++) {
		nw = nwelems[inw];
		chgElem.appendChild(nw);
	}
}
L_headUpdater.prototype.responseHandle=function(item) {
	var operation = item.getAttribute('operation');
	if(operation=='1'){
		var htmlElement = document.getElementsByTagName('HEAD')[0];
		var elemNews = L_createHtmlElement(item.childNodes, htmlElement);
	}
}

function L_loading(){
	this.start = function(){
		var lelem = $('loading');
		lelem.style.display = '';
		G_Float.center(lelem);
		$('floatbar').style.display='none';
	}
	this.end = function(){
		if ($('loading'))						$('loading').style.display = 'none';
		$('floatbar').style.display='';
		compList.doInit();
	}
}
function L_compList(){
	this.attrs = new Array();
	this.events = new Array();
	this.changes = new Array();
	this.deletes = new Array();
	this.requests = new Array();
	this.foots = new Array();
	this.floatRowlist = new Array();
	this.floatCollist = new Array();
	this.initials = new Array();
	
	this.reset = function() {
		// events
		for (var fi=this.events.length, f; fi-->0;) {
			f = this.events[fi];
			if (isDead(f))					this.events.splice(fi, 1);
		}
		// changes
		for (var fi=this.changes.length, f; fi-->0;) {
			f = this.changes[fi];
			if (isDead(f[0]))				this.changes.splice(fi, 1);
		}
		// deletes
		for (var fi=this.deletes.length, f; fi-->0;) {
			f = this.deletes[fi];
			if (isDead(f))					this.deletes.splice(fi, 1);
		}
	}
	this.addAttr = function(key, value) {
		var item = new Array();
		item[0] = key;
		item[1] = value;
		compList.attrs.push(item);
	}
	this.getAttrs = function() {
		var str = '';
		if (this.attrs.length > 0) {
			var elem=this.attrs.pop();
			str += '&attr_'+elem[0]+'='+elem[1];
		}
		return str;
	}
	this.addInit = function(comp, script, type) {
		var initItem = new Array();
		initItem[0] = comp;
		initItem[1] = script;
		initItem[2] = type;
		compList.initials.push(initItem);
	}
	this.doInit = function() {
		var inits0=new Array(), inits1=new Array(), inits2=new Array();
		for (var nsize=compList.initials.length,ni=0,nitem; ni<nsize; ni++) {
			nitem = compList.initials[ni];
			if (typeof(nitem[0])=='string')		nitem[0] = $(nitem[0]);
			if (nitem[2]==1) {
				inits1.push(nitem);
			} else if(nitem[2]==2) {
				inits2.push(nitem);
			} else if(nitem[2]==0) {
				inits0.push(nitem);
			}
			if (ni+1==nsize)
				compList.initials.splice(0,nsize);
		}
		var cntInited = 0;
		for (var nsize=inits0.length,ni=0,nitem; ni<nsize; ni++) {
			nitem = inits0[ni];
			if (nitem[0] && isShow(nitem[0])) {
				windowEval.compScript(nitem[0], nitem[1]);
				cntInited++;
			} else {
				compList.initials.push(nitem);
			}
		}
		for (var nsize=inits1.length,ni=0,nitem; ni<nsize; ni++) {
			nitem = inits1[ni];
			if (nitem[0] && isShow(nitem[0])) {
				windowEval.compScript(nitem[0], nitem[1]);
				cntInited++;
			} else {
				compList.initials.push(nitem);
			}
		}
		for (var nsize=inits2.length,ni=0,nitem; ni<nsize; ni++) {
			nitem = inits2[ni];
			if (nitem[0] && isShow(nitem[0])) {
				windowEval.compScript(nitem[0], nitem[1]);
				cntInited++;
			} else {
				compList.initials.push(nitem);
			}
		}
		if (cntInited>0)		compList.moveFloatlist();
		if (compList.initials.length>0)		windowEval.evalHandle(document.body, "compList.doInit();", 500);
	}
	this.addFloatCol = function(grid, rowsize) {
		if (window.embed || isMobile)		return;
		var gridPosi=G_Element.getPosition(grid), gridTop=gridPosi[0], gridLeft=gridPosi[1];
		var fromIndex = compList.floatCollist.length;
		for (var floati=0, floatsize=compList.floatCollist.length; floati < floatsize; floati++) {
			var cur=compList.floatCollist[floati], curTop=cur.gtop;
			if (cur.bornSource.offsetHeight == 0) {
				continue;
			} else if (gridTop < curTop) {
				fromIndex = floati;
				break;
			}
		}
		var prt=grid.parentNode;
		grid.gtop=gridTop;
		grid.bornParent = prt;
		grid.style.zIndex = 1;
		var floatList = new Array();
		var isLast = fromIndex==compList.floatCollist.length;
		for (var floati=0, floatsize=compList.floatCollist.length; floati<floatsize; floati++) {
			var cur=compList.floatCollist[floati];
			if (floati == fromIndex) {
				floatList.push(grid);
			}
			floatList.push(cur);
		}
		if (isLast)		floatList.push(grid);
		compList.floatCollist = floatList;
	}
	this.addFloatRow = function(grid1,rowsize1, grid2,rowsize2) {
		var ok=false;
		if (window.embed || isMobile) {
		} else {
			ok = true;
		}
		if (ok==false)		return;
		var gridTop = G_Element.getPosition(grid1)[0];
		var fromIndex = compList.floatRowlist.length;
		if (grid2)
			grid1.grid2=grid2;
		for (var floati=0, floatsize=compList.floatRowlist.length; floati < floatsize; floati++) {
			var cur=compList.floatRowlist[floati][0];
			var curTop=cur.gtop;
			if (cur.bornSource.offsetHeight == 0) {
				continue;
			} else if (gridTop < curTop) {
				fromIndex = floati;
				break;
			}
		}
		var rowList = new Array();
		for (var i=0; i<arguments.length; i+=2) {
			var grid=arguments[i], rowsize=arguments[i+1], prtTd=grid.parentNode;
			var gd = this.cloneFloatSourceGrid(grid, rowsize);
			gd.id = gd.id+'_r';
			gd.gtop = gridTop;
			rowList.push(gd);
			$('floatrows').appendChild(gd);
			var gridPosi = G_Element.getPosition(grid.bornSource);
			gd.className = 'floatRowitem';
			if (i==0) {
				gd.style.top = gridPosi[0];
				gd.style.left = gridPosi[1];
			} else {
				gd.style.top = gridPosi[0];
				gd.style.left = gridPosi[1];
				gd.style.zIndex = 10;
			}
		}
		var floatList = new Array();
		for (var floati=0, floatsize=compList.floatRowlist.length; floati <= floatsize; floati++) {
			if (floati == fromIndex)
				floatList.push(rowList);
			if (floati < floatsize)
				floatList.push(compList.floatRowlist[floati]);
		}
		compList.floatRowlist = floatList;
		window.onscroll = function(e) {
					if (this.tMoveFloat) {
						return ;
					}
					this.tMoveFloat = window.setTimeout(function() {
						compList.moveFloatlist();
						this.tMoveFloat = null;
					},20);
			};
	}
	this.cloneFloatSourceGrid = function(grid, rowsize) {
		if (!grid.bornSource) {
			grid.bornSource = grid;
			if (!grid.bornParent)
				grid.bornParent = grid.parentNode;
			grid.cloneList = new Array();
		}
		var gd = grid.cloneNode(false);
		grid.cloneList.push(gd);
		gd.bornSource = grid;
		gd.style.cssText = '';
		var tbody = grid.getElementsByTagName('TBODY')[0].cloneNode(false);
		var sid = '';
		for (var i=grid.cloneList.length; i>0; i--, sid=sid+'_');
		for (var rows=grid.rows,ri=0,wcount=0; ri<rowsize; ri++) {
			var rw0=rows[ri], rw1=rw0.cloneNode(true);
			for (var tlist0=rw0.getElementsByTagName('INPUT'),tlist1=rw1.getElementsByTagName('INPUT'),ti=tlist0.length; ti-->0;) {
				var t0=tlist0[ti], t1=tlist1[ti];
				if (!isUndefined(t0.svalue))
					t1.svalue=t0.svalue;
				t1.id = t0.id+sid;
				t1.sid = t0.id;
				if (!t0.cloneList) {
					t0.cloneList = new Array();
					t0.cloneList.push(t0);
					t0.swidth = t0.parentNode.offsetWidth;
				}
				if (t0.offsetHeight==1 && t0.offsetWidth>0 && ++wcount>0)
					t1.style.width = t0.swidth;
				t0.cloneList.push(t1);
			}
			tbody.appendChild(rw1);
			if (ri+1==rowsize && wcount==0)
				gd.style.width = grid.offsetWidth;
		}
		for (var tlist0=grid.rows[0].getElementsByTagName('INPUT'),ti=tlist0.length,t0; grid.cloneList.length==1 && ti-->0;) {
			t0=tlist0[ti];
			t0.style.width = t0.swidth;
		}
		gd.appendChild(tbody);
		return gd;
	}
	this.moveFloatlist = function() {
			var c1=$('errorPanel'), c2=$('floatbar');
			if (!c1 || c2.style.display=='none')			return;
			for (var childs=compList.floatCollist,ci=0,csize=childs.length; ci<csize; ci++) {
				var c=childs[ci], t=G_Element.getParentByTag(c.bornSource, 'TABLE.TABLE');
				var sposi=G_Element.getPosition(c.grid2);
				c.isShow=t.isShow=false;
				if (c.bornSource.bornParent.offsetHeight==0) {
				} else if (sposi[3]<document.body.scrollLeft+sposi[1]){// over right
				} else if (sposi[2]<document.body.scrollTop){//over bottom
				} else {
					c.isShow=t.isShow=true;
				}
				c.style.display = c.isShow==true? '': 'none';
				if (c.bornSource.offsetHeight==0)
					continue;
				c.style.left = document.body.scrollLeft;
			}
			var errorHeight=errorPanel.offsetHeight, prerowButtom=errorHeight, currowTop=0, rowHeight=0;
			for (var floati=0, floatsize=compList.floatRowlist.length; floati < floatsize; floati++,prerowButtom+=rowHeight,currowTop=0,rowHeight=0) {
				for (var childs=compList.floatRowlist[floati],ci=0,csize=childs.length; ci<csize; ci++) {
					var c=childs[ci];
					c.style.display=(c.bornSource.bornParent.offsetHeight==0 || childs[0].bornSource.isShow==false)? 'none': '';
					if (c.bornSource.offsetHeight==0 || c.style.display=='none')		continue;
					rowHeight = c.offsetHeight;
					var sposi=G_Element.getPosition(c.bornSource.bornParent);
					c.style.left=c.style.zIndex==10? sposi[1]: sposi[1]+document.body.scrollLeft;
					currowTop = sposi[0]-document.body.scrollTop<prerowButtom? prerowButtom+document.body.scrollTop: sposi[0];
					c.style.top = currowTop-errorHeight;
				}
			}
	}
	this.resetFloatlist = function() {
		// float cols
		var freshFloatList = new Array();
		for (var childs=compList.floatCollist,csize=childs.length,ci=0; ci < csize; ci++) {
			var f=childs[ci];
			if (f && isDead(f.bornSource.bornParent)) {
				f.parentNode.removeChild(f);
			} else if (!isDead(f)){
				freshFloatList.push(f);
			}
		}
		this.floatCollist = freshFloatList;
		// float rows
		var freshFloatList=new Array();
		for (var floati=0,fsize=this.floatRowlist.length; floati < fsize; floati++) {
			var freshItemList = this.floatRowlist[floati];
			if (freshItemList[0] && isDead(freshItemList[0].bornSource)) {
				for (var childs=freshItemList,ci=childs.length; ci-- > 0;) {
					var f=childs[ci];
					f.parentNode.removeChild(f);
				}
			} else if(!isDead(freshItemList[0])){
				freshFloatList.push(freshItemList);
			}
		}
		this.floatRowlist = freshFloatList;
	}
	this.addFoot = function(elem) {
		if (window.embed || isIE)		return;
		var footer = $('footer');
		var elemTop=G_Element.getPosition(elem)[0], elemHeight=elem.parentNode.offsetHeight, elemNext=null;
		var footItem = document.createElement('div');
		var fromIndex = this.foots.length;
		footer.style.display = '';
		for (var childs=this.foots,csize=childs.length,ci=0; ci<csize; ci++) {
			var cur=childs[ci], ctop=G_Element.getPosition(cur.bornParent)[0];
			var next=null, ntop=0;
			if (ci+1 < csize) {
				next=childs[ci+1];
				ntop=G_Element.getPosition(next.bornParent)[0];
			}
			if (cur.bornParent.offsetWidth==0) {
				continue;
			}
			if (ctop < elemTop && elemTop < ntop) {
				elemNext=next;
				fromIndex = ci+1;
				break;
			} else if(elemTop < ctop) {
				elemNext=cur;
				fromIndex = ci;
				break;
			}
		}
		var prt = elem.parentNode;
		prt.removeChild(elem);
		prt.style.height = elemHeight;
		footItem.style.width='100%';
		elem.style.width='100%';
		footItem.appendChild(elem);
		if (elemNext != null) {
			footer.insertBefore(footItem, elemNext.parentNode);
		} else {
			footer.appendChild(footItem);
		}
		elem.bornParent = prt;
		var footList = new Array();
		for (var childs=this.foots,csize=childs.length,ci=0; ci<csize; ci++) {
			var cur=childs[ci];
			if (ci == fromIndex) {
				footList.push(elem);
			}
			footList.push(cur);
		}
		if (elemNext == null)		footList.push(elem);
		this.foots = footList;
		footer.style.height = footer.offsetHeight+elemHeight;
	}
	this.resetFoots = function() {
		for (var fsize=this.foots.length, fi=fsize; fi-->0;) {
			var f = this.foots[fi], fprt = f.bornParent;
			if (isDead(f.bornParent)) {
				f.parentNode.parentNode.removeChild(f.parentNode);
				this.foots.splice(fi, 1);
			}
		}
		if (fsize==0)				$('footer').style.height=0;
	}
	this.addChange = function(elem, newValue) {
		var ito, finded=false;
		for(var i=0, ito=this.changes.length; i<this.changes.length;i++){
			if(this.changes[i][0]==elem){
				finded=true;
				ito = i;
				break;
			}
		}
		if (!finded) {
			this.changes[ito] = new Array();
			this.changes[ito][0] = elem;
			this.changes[ito][1] = newValue;
		} else if (this.changes[ito][1] != newValue){
			this.changes[ito][1] = newValue;
		} else {
			return false;
		}
		return true;
	}
	this.isChange = function(elem) {
		for (var i=this.changes.length; i-->0; ) {
			if(this.changes[i][0]==elem){
				return true;
			}
		}
		return false;
	}
	this.getChanges = function(formw) {
		var slist = new Array();
		for (var i=this.changes.length, oneMax=1520000; i-->0;){
			var comp=this.changes[i][0], value=this.changes[i][1];
			if (G_Element.getParentByClass(comp, 'Formw')!=formw)
				continue;
			if (value.replace)
				value = value.replace(/\%/g, escape('%')).replace(/\=/g, escape('=')).replace(/\&/g, escape('&')).replace(/\+/g, '%2B').replace(/\//g, '%2F');
			if ('long long parameter'.length>0 && value.replace && value.length>oneMax) {
				for (var rows=value.split(/\r?\n/g),rw=null,one=new Array(),oneLen=0,ri=0,rsize=rows.length; ri<rsize; ri++) {
					rw=rows[ri];
					oneLen += rw.len();
					one.push(rw);
					if (oneLen>oneMax || ri+1==rsize) {
						var uparam = ['_action=part', '&comp'+comp.id+'=', one.join('\n'), '\n', '&win'+formw.id+'=opened'];
						this.requests.unshift([formw.parentNode.url, uparam.join('')]);
						oneLen = 0;
						one = new Array();
					}
				}
				value='PartParam';
			}
			slist.push('&comp'+comp.id+'=');
			slist.push(value);
			this.changes.splice(i, 1);
		}
		return slist.join('');
	}
	this.addEvent = function(eventElem) {
		for (var items=this.events,item=null,i=items.length; i-->0;) {
			item=items[i];
			if (item==eventElem)
				return;
		}
		this.events.unshift(eventElem);
		if (compList.requests.length==0 && ajax.nextReady==true)		compList.getLazyRequest();
	}
	this.getEvents = function(formw) {
		var str = '';
		for (var i=0,isize=this.events.length; i<isize; i++) {
			var comp=this.events[i];
			if (G_Element.getParentByClass(comp, 'Formw')==formw && !isDead(comp)) {
				this.events.splice(i, 1);
				return '&eventId='+comp.id;
			}
		}
		return '';
	}
	this.getLazyRequest = function() {
		var flist=new Array();
		G_Element.getChildsByClass(document.body,'Formw',flist);
		for (var formw=null,fi=flist.length; fi-->0;) {
			formw=flist[fi];
			var sevent=compList.getEvents(formw);
			if (sevent.length == 0)
				continue;
			var uparam = ['_action=updator', sevent, compList.getChanges(formw), compList.getDeletes(formw), '&win', formw.id, '=opened'];
			this.requests.unshift([formw.parentNode.url, uparam.join('')]);
		}
		if (this.requests.length>0 && ajax.nextReady==true) {
			var url=this.requests.pop();
			ajax.request(url[0], url[1]);
		}
	}
	this.addDelete = function(elem) {
		this.deletes.push(elem);
	}
	this.getDeletes = function(formw) {
		var slist = new Array();
		for (var i=this.deletes.length; i-->0;){
			var comp=this.deletes[i];
			if (G_Element.getParentByClass(comp, 'Formw')!=formw)
				continue;
			if (slist.length==0)
				slist.push('&del=');
			slist.push(comp.id+',');
			this.deletes.splice(i, 1);
		}
		return slist.join('');
	}
	this.getTopWin = function() {
		var cur=window;
		for (var prt=(cur.opener? cur.opener: cur.parent); (prt==null || cur==prt)==false; cur=prt,prt=(cur.opener? cur.opener: cur.parent)) {
			try {
				prt.document.body.id;
				prt.location.href;
				if (prt.wins)
					prt.wins.length;
			}catch(e){
				break;
			}
		}
		if (!cur.wins)
			cur.wins = new Array();
		return cur;
	}
	this.addWindow = function(formw) {
		L_cyclingDo(formw, "true", ["compList.addEvent($('",formw.id,"'));"].join(''), 300, 150);
		this.getTopWin().wins.push([formw, formw.id]);
	}
	this.getDelwins = function() {
		var wins=this.getTopWin().wins, winList=new Array();
		var str='';
		if (wins.length==0)			return str;
		for (var size=wins.length, i=0, witem, w=null,wid=null; i<size; i++) {
			witem = wins[i];
			w=witem[0], wid=witem[1];
			var oked=null;
			try {oked=(!!w.parentNode);}catch(e){};
			if (oked)
				winList.push(witem);
			else
				str += '&win' + wid + '=closed';
		}
		this.getTopWin().wins = winList;
		return str;
	}
}
function L_throwableHandler(){
	this.responseHandle=function(item) {
		var type=item.getAttribute('type');
		var html = L_getNodeXmls(item.childNodes);
		if (type=='warn') {
			Validator.InsertWarn(null, html);
		} else {
			Validator.InsertError(null, html);
		}
	}
}

function L_windowEval(){
}
L_windowEval.prototype.responseHandle=function(contentItem) {
	var script = L_getNodeXmls(contentItem.childNodes);
	try{eval(script);}catch(e){throw e;}
}
L_windowEval.prototype.evalHandle=function(elem, script, delay) {
	if (isDead(elem))			return;
	window.setTimeout("try{"+script+"}catch(e){Validator.InsertError(null, e.message+'\t'+\""+script.substring(0,100)+"\");}", (delay? delay: 10));
}
L_windowEval.prototype.compScript=function(comp, script0, DefaultReturn) {
	if (!script0)				return;
	this.comp = comp;
	var script = 'var dhis=windowEval.comp;'+script0.split('this').join('dhis');
	try{
		var rt=null;
		rt = eval(script);
		if (isUndefined(rt)==false)
			return rt;// return eval value
	}catch(e){
		console.info(comp.id+'\t'+script0);
		if (isUndefined(DefaultReturn)==false)
			return DefaultReturn;
		Validator.InsertError(null, e.message+'\t'+script0.substring(0,100));// eval fail show error
	}
}

function L_windowAlert(){
}
L_windowAlert.prototype.responseHandle=function(items) {	
	var html = L_getNodeXmls(items.childNodes);
	alert(html);
}
function L_getNodeXmls(items){
	var html = '';
	for(var i=0,item,itemXml; i<items.length; i++){
		item = items.item(i);
		itemXml = item.xml;
		if (isBlank(itemXml))		continue;
		html += itemXml;
	}
	return html.replace(/<\/br>/g, '');
}
function $(sid, sidList0) {
	var element = null;
	if (sid) {
		if (/\,$/.test(sid)) {
			sidList0 = sid.substring(0, sid.length-1);
			var sidList = sidList0.split(",");
			for (var i=0,isize=sidList.length,sid,f; i<isize; i++) {
				sid = sidList[i];
				sidList[i]=$(sid);
			}
			for (var list=sidList,si=list.length; si-->0;) {
				if (list[si]!=null || si==0)
					return list[si];
			}
		}
		element = document.getElementById(sid);
		var f=element;
		if (f && (f.offsetHeight+f.offsetWidth)==0) {
			var formwList=new Array();
			G_Element.getChildsByClass(document.body, 'Formw', formwList);
			if (formwList.length>0) {
				f = G_Element.getChildById(formwList[0], sid);
				if (f!=null)
					element = f;
			}
		}
	} else if (sidList0) {
		if (/\,$/.test(sidList0))
			sidList0 = sidList0.substring(0, sidList0.length-1);
		var sidList = sidList0.split(",");
		for (var i=0,isize=sidList.length,sid,f; i<isize; i++) {
			sid = sidList[i];
			sidList[i]=null;
			if (sid.length>0 && (f=$(sid))) {
				if (f.cloneList)
					f=f.cloneList.slice(-1)[0];
				sidList[i]=f;
			}
		}
		return sidList;
	}
	return element;
}

function isDead(elem) {
	for (var cur=elem,prt=null; cur!=null; cur=prt,prt=null) {
		prt = cur.parentNode;
		if (prt==document.body.parentNode) {
			return false;
		}
	}
	return true;
}
function isShow(elem) {
	if (elem.offsetWidth+elem.offsetHeight>0) {
	} else if (isNaN(elem.offsetWidth+elem.offsetHeight)==true) {
	} else {
		return false;
	}
	return true;
}

Function.prototype.bind = function(object) {
	var __method = this;
	return function() {
		return __method.apply(object, arguments);
	}
}

Function.prototype.bindAsEventListener = function(object) {
	var __method = this;
	return function(event) {
		return __method.call(object, event || window.event);
	}
}
if (!(!!document.all)){
	// is not IE, is firefox
	XMLDocument.prototype.__proto__.__defineGetter__( 'xml' , function (){
		var d = document.createElement( 'div' );
		d.appendChild( this.cloneNode( true ));
		return d.innerHTML;
	});
	Element.prototype.__proto__.__defineGetter__( 'xml' , function (){
		var d = document.createElement( 'div' );
		d.appendChild( this.cloneNode( true ));
		return d.innerHTML;
	});
	HTMLElement.prototype.__defineGetter__('outerHTML', function(s){
		var d = document.createElement( 'div' );
		d.appendChild( this.cloneNode( true ));
		return d.innerHTML;
	});
	HTMLElement.prototype.__defineSetter__('outerHTML', function(s){
		var range = this.ownerDocument.createRange();
		range.setStartBefore(this);
		var fragment = range.createContextualFragment(s);
		this.parentNode.replaceChild(fragment, this);
	});
	HTMLElement.prototype.__defineGetter__('children',function (){
		var elementNodes = new Array();
		for (var items=this.childNodes,item,i=0,cnt=0; i<items.length;i++){
			item = items[i];
			if(item.nodeType == 1) {
				elementNodes[cnt++] = item;
			}
		}
		return elementNodes;
	});
}