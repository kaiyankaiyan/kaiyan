var C_View=new View(), C_Tree=new Tree(), C_Menu=new Menu(), C_Dialog=new Dialog(), C_Spliter=new Spliter(), C_SelectList=new SelectList(), C_TabPane=new TabPane(), C_HtmlField=new HtmlField();

function View() {
}
// set row the selected color
View.prototype.rowColor = function(rowStartor, toChecked) {
	if (isDead(rowStartor))			return;
	var row = G_Element.getParentByTag(rowStartor, 'TR');
	var rowother = row.other;
	rowStartor.checked = toChecked;
	L_propertychange(rowStartor,toChecked);
	G_Element.setStyle(row,'background', rowStartor.checked? '#B8D8FE': '');
	if (rowother) {
		G_Element.setStyle(rowother,'background', rowStartor.checked? '#B8D8FE': '');
	}
}
// EditViewBuilder columns width
View.prototype.expandEditView = function(colIds, container, fixIds) {
	var tds=(typeof(colIds)=='string'? $(null, colIds): [colIds]),tdWAll=0;
	var fixtds=typeof(fixIds)=='string'? $(null, fixIds): [];
	if (tds==null || tds.length==0)		return;
	var td0=tds[0], table=td0.parentNode, tablew=null;
	table = G_Element.getParentByTag(td0, 'TABLE');
	tablew = table.offsetWidth;
	if (!container)			container=this.getViewContainer(td0);
	for (var ti=0,tsize=tds.length,td; ti<tsize; ti++) {
		td = tds[ti];
		td.tdw = td.offsetWidth;
		tdWAll += td.tdw;
	}
	for (var ti=0,tsize=fixtds.length,td; ti<tsize; ti++) {
		td = fixtds[ti];
		td.tdw = td.offsetWidth;
	}
	for (var ti=0,tsize=fixtds.length,td; ti<tsize; ti++) {
		td = fixtds[ti];
		td.style.width = td.tdw;
	}
	table.style.width = null;
	tds.sort(function(a,b){return a.tdw>b.tdw?1:-1});
	var expandW = container.clientWidth-tablew+tdWAll;
	for (var tcount=tds.length,ti=tcount,td,tdw,splitw; ti-->0; tcount--,expandW-=tdw) {
		td = tds[ti];
		splitw = Math.floor(expandW/tcount);
		tdw = td.tdw>splitw? td.tdw: splitw;
		td.style.width = tdw;
	}
}
// Freezable ListView Middle container Right expand
View.prototype.setListRightExpand = function(tdright) {
	var table=G_Element.getParentByTag(tdright, 'TABLE'), tdmiddle=tdright.parentNode.cells[1], minner=G_Element.getChildren(tdmiddle,0);
	var wright=null;
	var container=this.getViewContainer(tdright);
	var wtotal=container.clientWidth, wmiddle=minner.offsetWidth;
	for (var tr=G_Element.getParentByTag(tdright, 'TD'), tri=0; tr!=null && ++tri>0; tr=G_Element.getParentByTag(tr.parentNode, 'TD')) {
		if (tri==2) {
			if (tr.colSpan>1)
				break;
			else
				return;
		}
	} 
	if (tdright.parentNode.cells.length==3 && minner.cloneList!=null) {
		var tdleft=tdmiddle.parentNode.cells[0];
		wright=wtotal-tdleft.offsetWidth-wmiddle;
		tdmiddle.style.width = wmiddle;
	} else {
		wright=wtotal-table.offsetWidth;
	}
	if (wright>0) {
		tdright.style.width = wright;
		tdright.style.height=table.offsetHeight;
		tdright.className='ListBodyExtend';
	}
}
//EditViewBuilder container
View.prototype.getViewContainer = function(fromComp) {
	for (var cur=fromComp,prt=null; ; cur=prt,prt=null) {
		prt = cur.parentNode;
		if (cur.getAttribute('viewcontainer')) {
			return cur;
		} else if (prt == document.body.parentNode) {
			break;
		}
	}
	return null;
}
// SqlListBuilder left grid same height as right grid
View.prototype.sameTrHeight = function(ids0Left, ids0Right) {
	var idsLeft=$(null, ids0Left), tableLeft=null;
	var idsRight=$(null, ids0Right), tableRight=null;
	for (var ri=idsLeft.length, rsize=ri, height=0, theight=0; ri-->0; theight+=height) {
		var hLeft=idsLeft[ri], hRight=idsRight[ri], trLeft=hLeft.parentNode.parentNode, trRight=hRight.parentNode.parentNode;
		if (ri==rsize-1) {
			tableLeft = G_Element.getParentByTag(hLeft,'TABLE');
			tableRight = G_Element.getParentByTag(hRight,'TABLE');
			tableRight.ids0Left = ids0Left;
			tableRight.ids0Right = ids0Right;
			if (tableLeft.offsetHeight==tableRight.offsetHeight)
				return;
			if (tableLeft.offsetHeight==tableLeft.theight && tableRight.offsetHeight==tableLeft.theight)
				return;
		}
		trLeft.other = trRight;
		trLeft.hinput = hLeft;
		trRight.other = trLeft;
		trRight.hinput = hRight;
		var tdLeft=G_Element.getParentByTag(hLeft,'TD'), tdRight=G_Element.getParentByTag(hRight,'TD');
		height = (tdLeft.offsetHeight>tdRight.offsetHeight? tdLeft.offsetHeight: tdRight.offsetHeight)+0.5;
		hLeft.style.height = height;
		hRight.style.height = height;
		if (ri==0) {
			tableLeft.theight = theight;
			tableRight.parentNode.style.height = tableLeft.offsetHeight;
		}
	}
}
// PageListBuilder select single row
View.prototype.setSelected = function (rowChecker) {
	if (!rowChecker.header)			rowChecker.header=$(rowChecker.getAttribute('header_id'));
	this.init(rowChecker.header);
	for (var i=0; i<rowChecker.header.itemList.length; i++) {
		var item = rowChecker.header.itemList[i];
		if (item!=rowChecker && item.checked==true) {
			this.rowColor(item, false);
		}
	}
	this.rowColor(rowChecker, rowChecker.checked);
	L_propertychange(rowChecker.header.viewGrid, '');
}

// PageListBuilder add select row
View.prototype.addSelected = function(rowChecker) {
	if (!rowChecker.header)			rowChecker.header=$(rowChecker.getAttribute('header_id'));
	this.init(rowChecker.header);
	this.rowColor(rowChecker, rowChecker.checked);
	L_propertychange(rowChecker.header.viewGrid, '');
}
// CheckListBuilder selectAll/notSelectAll rows
View.prototype.selectAll = function(header) {
	this.init(header);
	for (var i=0; i<header.itemList.length; i++) {
		var item = header.itemList[i];
		this.rowColor(item, header.checked);
	}
	L_propertychange(header.viewGrid, '');
}
View.prototype.init = function(header) {
	if (!header.viewGrid)			header.viewGrid=$(header.getAttribute('viewGrid_id'));
	if (!header.itemList)			header.itemList=$(null,header.getAttribute('item_ids'));
}

// PageListBuilder field-order onMouseOver
View.prototype.orderOnmouseover = function(cell) {
	var orderButton = cell.children[0];
	orderButton.className = "ListViewOrderShow";
}
// PageListBuilder field-order onMouseOut
View.prototype.orderOnmouseout = function(cell) {
	var orderButton = cell.children[0];
	orderButton.className = "ListViewOrderHide";
}
// PageListBuilder field-order onClick
View.prototype.orderOnclick = function(cell) {
	var orderButton = cell.children[0];
	var cell = orderButton.parentNode;
	cell.onmouseout = null;
	orderButton.className = "ListViewOrderShow";
}

// RadioButtonGroup single select
View.prototype.radiogroupOnclick = function(radio) {
	if (!radio.checked) {
		return;
	}
	var grid = G_Element.getParentByTag(radio,'A').parentNode;
	var item_ids = (!grid.itemIds)? $(null, grid.getAttribute('item_ids')): grid.itemIds;
	for (var i=item_ids.length, item; i-->0;) {
		item = item_ids[i];
		if (item != radio) {
			item.onclick();
		}
	}
	L_propertychange(grid, radio.id);
}

function Tree() {
}
Tree.prototype.expandNode = function(node) {
	node.isOpened = !node.isOpened;
	this.openNodePath(node);
	this.setNodeChildren(node, node.isOpened);
	L_propertychange(node, node.isOpened);
}
Tree.prototype.openNodePath = function(node) {
	for (var items=this.getNodePath(node), i=items.length, item; i-->1;) {
		item = items[i];
		if (!item.isOpened) {
			item.isOpened = true;
			this.setNodeChildren(item, item.isOpened);
		}
	}
}
Tree.prototype.getParentBrothers = function(curNode) {
	var prtTable=G_Element.getParentByTag(curNode.parentNode,'TABLE'), prtTr=prtTable.className=='tree'? null: G_Element.getParentByTag(curNode.parentNode,'TR');
	var prtupTr, prtdownTr;
	if (prtTr!=null) {
		try{prtupTr=prtTr.parentNode.children[prtTr.rowIndex-1];}catch(e){}
		try{prtdownTr=prtTr.parentNode.children[prtTr.rowIndex+1];}catch(e){}
		for (var cur=prtupTr,ps,prt=null; cur!=null && !prtdownTr; cur=prt,prt=null) {
			ps=this.getParentBrothers(cur);
			prt=ps[0];
			if (ps[1])					prtdownTr = ps[1];
		}
	}
	return [prtupTr, prtdownTr];
}
Tree.prototype.onkeyupNode = function(tree, curNode, keyCode) {
	var upTr, up2Tr, downTr, down2Tr, firstTr, firstdownTr, lastTr, lastupTr;
	var prtBths=this.getParentBrothers(curNode), prtupTr=prtBths[0], prtdownTr=prtBths[1];
	for (var nlist=curNode.parentNode.children, ni=nlist.length,nsize=ni; ni-->0;) {
		if (nlist[ni]==curNode) {
			firstTr = ni>0? nlist[0]: null;
			firstdownTr = ni>1? nlist[1]: null;
			upTr = ni-1>-1? nlist[ni-1]: null;
			up2Tr = ni-2>-1? nlist[ni-2]: null;
			downTr = (ni+1<nsize? nlist[ni+1]: null);
			down2Tr = ni+2<nsize? nlist[ni+2]: null;
			lastTr = ni<nsize-1? nlist[nsize-1]: null;
			lastupTr = ni<nsize-2? nlist[nsize-2]: null;
			break;
		}
	}
	switch (keyCode) {
		case 38: // up
			if (upTr!=null && upTr.className == 'node') {
				// select up node
				this.selectNode(upTr);
			} else if (up2Tr!=null && up2Tr.className=='node' && up2Tr.isOpened==false) {
				// select unopened up node
				this.selectNode(up2Tr);
			} else if (up2Tr!=null && up2Tr.className=='node' && up2Tr.isOpened==true) {
				// select opened up node's last child node
				var childNodeFirst = G_Element.getChildByClass(upTr, 'node');
				this.onkeyupNode(tree, childNodeFirst, 35);
			} else if (upTr==null && prtupTr!=null && prtupTr.className=='node') {
				// first child node up to parent node
				this.selectNode(prtupTr);
			}
			break;
		case 40: // down
			if (curNode.isOpened==true && downTr!=null && downTr.className=='') {
				// select opened curnode's first child node
				var childNodeFirst = G_Element.getChildByClass(downTr, 'node');
				this.selectNode(childNodeFirst);
			} else if (curNode.isOpened==false && downTr!=null && downTr.className=='' && down2Tr!=null && down2Tr.className=='node') {
				// select closed curnode's down node
				this.selectNode(down2Tr);
			} else if (downTr!=null && downTr.className=='node') {
				// select down node
				this.selectNode(downTr);
			} else if ((downTr==null || downTr.offsetHeight==0) && prtdownTr!=null && prtdownTr.className=='node') {
				// last child node down to parent's down node
				this.selectNode(prtdownTr);
			}
			break;
		case 37: // left
			if (curNode.isOpened==true) {
				// close curNode hide children
				curNode.isOpened = false;
				this.setNodeChildren(curNode, false);
			} else if (prtupTr!=null && prtupTr.className=='node' && prtupTr.isOpened==true) {
				// to parent node
				this.selectNode(prtupTr);
			}
			break;
		case 39: // right
			if (curNode.isOpened==false && downTr!=null && downTr.className=='') {
				// open curNode select first child
				this.expandNode(curNode);
				var childNodeFirst = G_Element.getChildByClass(downTr, 'node');
				this.selectNode(childNodeFirst);
			}
			break;
		case 36: // home
			if (firstTr!=null && firstTr.className=='node') {
				// first brother
				this.selectNode(firstTr);
			}
			break;
		case 35: // end
			if (lastTr.className == 'node') {
				// last leaf brother
				this.selectNode(lastTr);
			} else if (lastupTr!=null && lastupTr.className=='node') {
				// last lranch brother
				this.selectNode(lastupTr);
			}
			break;
	}
} 
Tree.prototype.selectNode = function(node) {
	var tree = this.getTree(node);
	if (tree.selectedNode) {
		var selectedText = $(tree.selectedNode.getAttribute('textId'));
		selectedText.className = 'nodetext';
	}
	var nodeText = $(node.getAttribute('textId'));
	nodeText.className = 'nodetext_selected';
	this.openNodePath(node);
//	nodeText.focus();
	tree.selectedNode = node;
	L_propertychange(this.getTree(node), node.id);
}
Tree.prototype.collapseAll = function(table) {
	for (var i=0, item=null; i < table.rows.length; i++) {
		item = table.rows[i];
		if (item.className == 'node') {
			item.isOpened = false;
			this.setNodeChildren(item, item.isOpened);
		}
		else {
			var c1 = G_Element.getChildren(item,1), c10 = G_Element.getChildren(c1, 0);
			this.collapseAll(c10);
		}
	}
}
Tree.prototype.initTree = function(table) {
	for (var i=0, item=null, node=null; i < table.rows.length; i++) {
		item = table.rows[i];
		if (item.className == 'node') {
			node = item;
			item.isOpened = item.getAttribute('isOpened')=='true';
			this.setNodeChildren(item, item.isOpened);
			if (item.getAttribute('isSelected') == 'true') {
				this.selectNode(item);
			}
		}
		else {
			var c1 = G_Element.getChildren(item,1), c10 = G_Element.getChildren(c1, 0);
			this.initTree(c10);
		}
	}
}
Tree.prototype.getTree = function(node) {
	var tree = null;
	for (var cur=node, prt=cur.parentNode; ; cur=prt, prt=cur.parentNode) {
		if (cur.className == 'tree') {
			tree = cur;
			break;
		} else if (prt == null) {
			break;
		}
	}
	return tree;
}
Tree.prototype.setNodeImage = function(node, isShow) {
	var nodeChilds = node.children;
	var foldTd = nodeChilds[0], nodeTd = nodeChilds[1];
	var foldTdChilds = foldTd.children;
	var foldImg = foldTdChilds.length>0? foldTdChilds[0]: null;
	var imgAttr=isShow?'imgOpen':'imgClose';
	if (foldImg != null && foldImg.getAttribute(imgAttr) != null) {
		foldImg.src = foldImg.getAttribute(imgAttr);
	}
	
	if (nodeTd != null && nodeTd.getAttribute(imgAttr) != null) {
		G_Element.setStyle(nodeTd, "background-image", "url("+nodeTd.getAttribute(imgAttr)+")");
	}
}
Tree.prototype.setNodeChildren = function(node, isShow) {
	if (node.getAttribute('hasNodes') == 'true') {
		var parentChilds = node.parentNode.children;
		var childrenTr = parentChilds[indexOf(parentChilds, node) + 1];
		childrenTr.style.display = isShow? '': 'none';
	}
	this.setNodeImage(node, isShow);
}
Tree.prototype.getParentNode = function(item) {
	var node = null;
	for (var cur=item.parentNode, prt=cur.parentNode; node == null; cur=prt, prt=cur.parentNode) {
		if (!cur.id && cur.tagName=='TR') {
			var nodesTr = cur, nodesTrChilds=nodesTr.parentNode.children, nodeTr = nodesTrChilds[indexOf(nodesTrChilds, nodesTr) - 1];
			node = nodeTr;
		}
		else if (cur.className == 'tree')	break;
	}
	return node;
}
Tree.prototype.getNodePath = function(node) {
	var items = new Array();
	for (var cur=node, prt=this.getParentNode(cur); cur != null; cur=prt, prt=this.getParentNode(cur)) {
		items[items.length] = cur;
		if (prt == null)	break;
	}
	return items;
}

function Menu() {
}
Menu.prototype.hideMenu = function(item){
	var menuBar = this.getMenuBar(item);
	for (var i=0,childs=menuBar.children; i<childs.length; i++) {
		var item = childs[i], idx = item.id.indexOf('Panel');
		if (idx>-1 && idx==item.id.length-'Panel'.length)
			item.style.display = 'none';
	}
	menuBar.selectedItem = null;
	menuBar.items = null;
}
Menu.prototype.overMenu = function(item){
	this.hideMenu(item);
}
Menu.prototype.downMenu = function(item){
	if(this.getMenuBar(item).selectedItem != item) {
		this.setSelectedItem(item);
	}
}
Menu.prototype.showMenu = function(item){
	var itemPanel = this.getItemPanel(item);
	if (itemPanel != null) {
		var posi = G_Element.getPosition(item);
		itemPanel.style.display = '';
		itemPanel.style.top = posi[2];
		itemPanel.style.left = posi[1];
	}
}
Menu.prototype.overMenuItem = function(item){
	if (this.getMenuBar(item).selectedItem != item) {
		this.setSelectedItem(item);
	}
}
Menu.prototype.overoutMenuItem = function(item){
	var menuBar = this.getMenuBar(item);
	if (menuBar.selectedItem==item && this.getItemPanel(item)==null) {
		this.hideMenu(item);
	}
}
Menu.prototype.showMenuItem = function(item){
	var itemPanel = this.getItemPanel(item);
	if (itemPanel != null) {
		var posi = G_Element.getPosition(item);
		itemPanel.style.display = '';
		itemPanel.style.top = posi[0];
		itemPanel.style.left = posi[3];
	}
}
Menu.prototype.getMenuBar = function(item) {
	var menuBar = null;
	for (var cur=item,prt=cur.parentNode; ; cur=prt,prt=cur.parentNode) {
		if (cur.getAttribute('Menutype')) {
			menuBar = cur;
			break;
		}
		else if (prt == null)	break;
	}
	return menuBar;
}
Menu.prototype.getItemPanel = function(item){
	if (!item)	return null;
	return $(item.id+'Panel');
}
Menu.prototype.getSelectedItems = function(item) {
	var items = this.getMenuBar(item).items, selected = this.getMenuBar(item).selectedItem, selectedPanel = this.getItemPanel(selected);
	if (items == null) {
		items = new Array();
	}
	else if (selectedPanel!=null && indexOf(items, selectedPanel)==-1){
		items[items.length] = selectedPanel;
	}
	return items;
}
Menu.prototype.setSelectedItem = function(selected) {
	var menuBar = this.getMenuBar(selected), items=new Array();
	for (var cur=selected, prt=$(cur.getAttribute('menuId')); ; cur=prt,prt=$(cur.getAttribute('menuId'))) {
		items[items.length] = cur;
		if (prt==null || prt==menuBar) {
			if (menuBar.getAttribute('Menutype')=='popupMenu')	items[items.length] = prt;
			break;
		}
	}
	this.hideMenu(selected);
	menuBar.items = items;
	menuBar.selectedItem = selected;
	for (var i=items.length; i-->0;) {
		var item = items[i];
		if (item == menuBar)
			this.getItemPanel(item).style.display = '';
		else if (item.getAttribute('menuId') == null)
			this.showMenu(item);
		else
			this.showMenuItem(item);
	}
}
//---------------popupmenu------------------------------------------------------
Menu.prototype.popupMenu = function(itemId, evt){
	var item = $(itemId+'Panel');
	item.style.display = '';
	item.style.top = evt.clientY;
	item.style.left = evt.clientX;
}

function Dialog() {
}
Dialog.prototype.init = function(dialog) {
	var mask = $('mask');
	G_Float.center(dialog, document.body);
	dialog.mask = mask;
	mask.style.display = 'none';
}
Dialog.prototype.resizeas = function(dialog, elem) {
	if (dialog.offsetWidth < elem.offsetWidth) {
		dialog.style.width = elem.offsetWidth+2;
	}
}
Dialog.prototype.close = function(dialog) {
	var mask = dialog.mask;
	mask.style.display = 'none';
	dialog.parentNode.removeChild(dialog);
	L_propertydelete(dialog);
}
Dialog.prototype.mousedown = function(dialog, evt) {
	var mask = dialog.mask;
	mask.style.display = 'block';
	mask.onmousemove = this.mousemove.bindAsEventListener(dialog);
	mask.onmouseup = this.mouseup.bindAsEventListener(dialog);
	
	dialog.x = evt.clientX;
	dialog.y = evt.clientY;
}
Dialog.prototype.mousemove = function(evt) {
	var dialog = this;
	var _top = evt.clientY - dialog.y + parseInt(dialog.style.top); 
	var _left = evt.clientX - dialog.x + parseInt(dialog.style.left);
	dialog.style.top = _top;
	dialog.style.left = _left;
	dialog.x =  evt.clientX;
	dialog.y =  evt.clientY;
}
Dialog.prototype.mouseup = function(evt) {
	var dialog = this, mask = dialog.mask;
	mask.onmousemove = null;
	mask.onmouseup = null;
	mask.style.display = 'none';
}

function Spliter() {
}
Spliter.prototype.init = function(spliter) {
	var style = spliter.getAttribute('split.style');
	var tdpre = $(spliter.getAttribute('tdpreid')), tdnext = $(spliter.getAttribute('tdnextid')), mask = $('mask');
	spliter.isVertical = style=='vertical';
	spliter.tdpre = tdpre;
	spliter.tdnext = tdnext;
	spliter.mask = mask;
	mask.style.display = 'none';
}
Spliter.prototype.mousedown = function(spliter, evt) {
	spliter.mask.onmousemove = this.mousemove.bindAsEventListener(spliter);
	spliter.mask.onmouseup = this.mouseup.bindAsEventListener(spliter);
	
	var tdpre = spliter.tdpre, tdnext = spliter.tdnext, mask = spliter.mask;
	tdpre.w = tdpre.clientWidth;
	tdpre.h = tdpre.clientHeight;
	tdnext.w = tdnext.clientWidth;
	tdnext.h = tdnext.clientHeight;
	mask.style.display = '';
	spliter.x = evt.clientX;
	spliter.y = evt.clientY;
}
Spliter.prototype.mousemove = function(evt) {
	var spliter = this, tdpre = spliter.tdpre, tdnext = spliter.tdnext;
	
	var _offy = evt.clientY - spliter.y;
	var _offx = evt.clientX - spliter.x;
	if (spliter.isVertical) {
		tdpre.style.width = tdpre.w + _offx;
		tdnext.style.width = tdnext.w - _offx;
	}
	else {
		tdpre.style.height = tdpre.h + _offy;
		tdnext.style.height = tdnext.h - _offy;
	}
}
Spliter.prototype.mouseup = function(evt) {
	var spliter = this, mask = spliter.mask;
	mask.onmousemove = null;
	mask.onmouseup = null;
	mask.style.display = 'none';
}

function SelectList() {
}
SelectList.prototype.initSelectList = function(selectGrid) {
	var optionTexts = new Array(), options = new Array();
	var contentGrid = $(selectGrid.getAttribute('contentGridId')), textInput = $(selectGrid.getAttribute('textInputId'));
	var hsChangeListener = selectGrid.getAttribute('hasChangeListener')=='true';
	contentGrid.parentNode.style.display = '';
	G_Element.setWidth(contentGrid, contentGrid.parentNode.clientWidth);
	contentGrid.parentNode.style.display = 'none';
	// option tds
	for (var itr=0,i=0,tr,tds; itr<contentGrid.rows.length; itr++) {
		tr = contentGrid.rows[itr];
		tds = tr.children;
		if (tr.getAttribute('optionText')) {
			options[i] = tr;
			optionTexts[i] = tr.getAttribute('optionText');
			i++;
		}
		else {
			for (var itd=0,td; itd<tds.length; itd++) {
				td = tds[itd];
				options[i] = td;
				optionTexts[i] = td.getAttribute('optionText');
				i++;
			}
		}
	}
	contentGrid.options = options;
	
	selectGrid.hsChangeListener = hsChangeListener;
	selectGrid.optionTexts = optionTexts;
	selectGrid.textInput = textInput;
	selectGrid.contentGrid = contentGrid;
	textInput.setValuable = (textInput.getAttribute('setValuable')!='false');
	this.hideSelectContent(selectGrid.id);
}
SelectList.prototype.searchOptions = function(selectId, searchText, evt) {
	var selectGrid = this.getSelectGrid(selectId), contentGrid = selectGrid.contentGrid, texts = selectGrid.optionTexts;
	var evtKeyCode = evt==null? 13: evt.keyCode;
	this.showSelectContent(selectId, false);
	switch (evtKeyCode) {
		case 13:	// enter key
			var td = this.getSelectedTd(selectId);
			if (td != null) {
				td.onclick();
			}
			break;
		case 38:	// up arrow key
			for (var i=0, r=this.getSelectedIndex(selectId); i<texts.length-1; i++) {
				r = r-1==-1? texts.length-1: r-1;
				if (contentGrid.options[r].style.display == '') {
					this.setSelectedIndex(selectId, r);
					break;
				}
			}
			break;
		case 40:	// down arrow key
			for (var i=0, r=this.getSelectedIndex(selectId); i<texts.length-1; i++) {
				r = r+1==texts.length? 0: r+1;
				if (contentGrid.options[r].style.display == '') {
					this.setSelectedIndex(selectId, r);
					break;
				}
			}
			break;
		default:
			var iselected = -1, matched = new Array(), matchedText = new Array();
			for (var i=0,im=0,item,itemText; i<contentGrid.options.length; i++) {
				item = contentGrid.options[i];
				itemText = texts[i];
				if (isBlank(itemText))		continue ;
				if (itemText.toLowerCase().split(searchText.toLowerCase()).length > 1) {
					matched[im] = item;
					matchedText[im] = itemText;
					item.style.display = '';
					im++;
				}
				else {
					item.style.display = 'none';
				}
			}
			for (var i=0,im=-1,item,itemText; i<contentGrid.options.length; i++) {
				item = contentGrid.options[i];
				itemText = texts[i];
				if (indexOf(matched, item) == -1)	continue;
				im++;
				if (itemText.toLowerCase() == searchText.toLowerCase()) {
					iselected = i;
					break;
				}
				else if (itemText.toLowerCase().startWith(searchText.toLowerCase())) {
					iselected = i;
				}
				else if (itemText.toLowerCase().endWith(searchText.toLowerCase())) {
					iselected = i;
				}
				else if (im == 0) {
					iselected = i;
				}
			}
			if (iselected > -1)		this.setSelectedIndex(selectId, iselected);
			break;
	}
}
SelectList.prototype.showOptions = function(contentGrid) {
	for (var i=0,item; i<contentGrid.options.length; i++) {
		item = contentGrid.options[i];
		item.style.display = '';
	}
}
SelectList.prototype.selectContent = function(selectId) {
	var selectGrid = this.getSelectGrid(selectId), contentGrid = selectGrid.contentGrid;
	var showed = contentGrid.parentNode.style.display == '';
	if (showed)
		this.hideSelectContent(selectId);
	else
		this.showSelectContent(selectId, true);
}
SelectList.prototype.showSelectContent = function(selectId, showAllOptions) {
	var selectGrid = this.getSelectGrid(selectId), contentGrid = selectGrid.contentGrid;
	contentGrid.parentNode.style.display = '';
	if (showAllOptions)		this.showOptions(contentGrid);
	selectGrid.textInput.focus();
}
SelectList.prototype.hideSelectContent = function(selectId) {
	var selectGrid = this.getSelectGrid(selectId), contentGrid = selectGrid.contentGrid;
	var index = this.getSelectedIndex(selectId);
	if (index == -1)		return;
	L_propertychange(selectId, index);
	if (selectGrid.hsChangeListener==true)			L_action(selectId);
	// contentGrid
	contentGrid.parentNode.style.display = 'none';
	if (selectGrid.textInput.setValuable==true) 	selectGrid.textInput.value = selectGrid.optionTexts[index];
}
SelectList.prototype.getSelectGrid = function(selectId) {
	var selectGrid = (typeof(selectId)=='string'? $(selectId): selectId);
	if (!selectGrid.optionTexts)
		this.initSelectList(selectGrid);
	return selectGrid;
}
SelectList.prototype.setSelected = function(selectId, index) {
	this.setSelectedIndex(selectId, index);
	this.hideSelectContent(selectId);
}
SelectList.prototype.setSelectedIndex = function(selectId, index) {
	var selectGrid = this.getSelectGrid(selectId), contentGrid = selectGrid.contentGrid;
	var tds = contentGrid.options, selected = tds[index];
	for (var i=0,item; i<tds.length; i++) {
		item = tds[i];
		item.className = '';
	}
	selected.style.display = '';
	selected.className = 'optionDown';
}
SelectList.prototype.getSelectedIndex = function(selectId) {
	var selectGrid = this.getSelectGrid(selectId), contentGrid = selectGrid.contentGrid, tds = contentGrid.options;
	for (var itd=0,td; itd<tds.length; itd++) {
		td = tds[itd];
		if (td.className == 'optionDown') {
			return itd;
		}
	}
	return -1;
}
SelectList.prototype.getSelectedTd = function(selectId) {
	var selectGrid = this.getSelectGrid(selectId), contentGrid = selectGrid.contentGrid, tds = contentGrid.options;
	var index = this.getSelectedIndex(selectId);
	if (index > -1) {
		return tds[index];
	}
	return null;
}

function TabPane() {
}
TabPane.prototype.getTabPane = function(paneId) {
	var tabPane = (typeof(paneId)=='string'? $(paneId): paneId);
	if (!tabPane.titlesGrid) {
		var selectedIndex = 0, titlesGrid = $(tabPane.getAttribute('titlesGridId'));
		tabPane.titlesGrid = titlesGrid;
		tabPane.selected = selectedIndex;
		for (var cur=tabPane,prt=cur.parentNode; ; cur=prt,prt=cur.parentNode) {
			if (!prt || prt==document.body) {
				tabPane.fullyHeight = G_Element.getPosition(titlesGrid)[0] + (document.body.clientHeight-cur.offsetHeight);
				break;
			}
		}
		this.setSelectedTab(paneId, selectedIndex);
	}
	return tabPane;
}
TabPane.prototype.setSelectedTab = function(paneId, index) {
	var tabPane = this.getTabPane(paneId), titlesGrid = tabPane.titlesGrid, selected = tabPane.selected;
	var selectTitle = this.getTabTitleTd(titlesGrid,selected), selectContent = $(selectTitle.getAttribute('contentId'));
	var itemTitle = this.getTabTitleTd(titlesGrid,index), itemContent = $(itemTitle.getAttribute('contentId'));
	var itemContentTr = this.getTabContentTr(itemContent);
	
	selectTitle.className = 'TabbedPane_Tab';
	this.getTabContentTr(selectContent).style.display = 'none';
	itemTitle.className = 'TabbedPane_Tab_Selected';
	itemContentTr.style.display = '';
	if (itemContentTr.offsetHeight < tabPane.fullyHeight) {
		itemContentTr.style.height = tabPane.fullyHeight;
	}
	tabPane.selected = index;
	L_propertychange(paneId, index);
	compList.moveFloatlist();
}
TabPane.prototype.getTabTitleTd = function(titlesGrid, index) {
	var titleTd = null;
	var loc = titlesGrid.getAttribute('tabs-location');
	if (loc == 'top' || loc == 'bottom') {
		titleTd = titlesGrid.rows[0].cells[index];
	}
	else if (loc == 'left' || loc == 'right') {
		titleTd = titlesGrid.rows[index].cells[0];
	}
	return titleTd;
}
TabPane.prototype.getTabContentTr = function(itemContent) {
	var tr = null;
	for (var cur=itemContent,prt=cur.parentNode; ; cur=prt,prt=cur.parentNode) {
		if (prt.tagName == 'TR') {
			tr = prt;
			break;
		} else if (prt == null) {
			break;
		}
	}
	return tr;
}

function HtmlField() {
}
HtmlField.prototype.setInner = function(htmlField) {
	var editable = htmlField.getAttribute('contenteditable')=='true'? true: false;
	this.initChildren(htmlField, htmlField, editable);
	if (!htmlField.svalue) {
		htmlField.svalue=htmlField.innerHTML;
	}
	if (!editable && htmlField.getAttribute('editable')==null)		G_Element.setStyle(htmlField, 'border-top-width', '0px');
}
HtmlField.prototype.initChildren = function(elem, htmlField, editable) {
	for (var childs=elem.childNodes,ichild=childs.length,icheck=ichild,child=null; ichild-->0;) {
		child=childs[ichild];
		if (editable==true) {
			if (child.type=='radio')		child.type='checkbox';
			if (!htmlField.onblur)			continue;
			if (child.type=='checkbox' && child.tagName=='INPUT') {
				for (var ic=icheck,nelem=null; ic-->ichild;) {
					nelem=childs[ic];
					nelem.checkbox=child;
				}
				icheck = ichild;
			}
		} else {
			if (child.type=='text' && child.tagName=='INPUT') {
				child.outerHTML = child.value;
			}
		}
		if (child.childNodes)		this.initChildren(child, htmlField, editable);
	}
}
HtmlField.prototype.setInnerOnchange = function(htmlField, event) {
	var pelem=null;
	var editable = htmlField.getAttribute('contenteditable')=='true'? true: false;
	try {if (isFirefox && editable)	pelem=event.rangeParent.checkbox? event.rangeParent.checkbox: event.target.checkbox;}catch(e){}
	if (pelem) {
		pelem.checked = !pelem.checked;
	}
	htmlField.onblur(event);
}
HtmlField.prototype.getSlimly = function(div, isReturn, evt) {
	for (var items=div.getElementsByTagName('IMG'), m=null, i=items.length; i-->0;) {
		m = items[i];
		if (m.src.indexOf(';base64,')==-1)
			this.getImage64(m);
	}
	for (var items=div.childNodes,i=items.length,item;i-->0;){
		item = items[i];
		if (!item.attributes)			continue;
		if (!isUndefined(item.checked))			item.ichecked = item.checked;
		if (!isUndefined(item.selected))		item.iselected = item.selected;
		if (!isUndefined(item.value))			item.ivalue = item.value;
		for (var as=['style','class','name','id','size','width','height','checked','selected','value',
		             ],a=as.length,ai; a-->0;) {
			ai = as[a];
			item.removeAttribute(ai);
		}
		for (var as=['disabled','readOnly','className',
		             'onmousedown','onmouseover','onmouseout','onmouseup','onclick','ondblclick','onblur','onkeyup','onkeydown','ondragenter','onpaste','onchange','onselect',
		             ],a=as.length,ai; a-->0;) {
			ai = as[a];
			item.removeAttribute(ai);
			windowEval.compScript(item, 'if (this.'+ai+') this.'+ai+'=null;');
		}
		if (!isUndefined(item.ichecked) && item.ichecked==true) {
			item.setAttribute('checked', true);
		}
		if (!isUndefined(item.iselected && item.iselected==true)) {
			item.setAttribute('selected', true);
		}
		if (!isUndefined(item.ivalue) && !isBlank(item.ivalue)) {
			item.setAttribute('value', item.ivalue);
		}
		this.getSlimly(item, false, evt);
	}
	if (isReturn) {
		if (div.innerText && isBlank(div.innerText))		return "";
		var html = div.innerHTML.replace(/&nbsp;/g,'').replace(/\s{2,}/g,'');
//		Validator.InsertError(null, div.innerHTML);
		var c=document.querySelector('.ListViewBody');
		if (c) {
			L_propertychange_resize(div);
			C_View.sameTrHeight(c.ids0Left, c.ids0Right);
		}
		return html;
	}
}
HtmlField.prototype.getImage64 = function(img) {
	img.crossOrigin = '';
	var canvs = document.createElement('canvas');
	canvs.width = img.width;
	canvs.height = img.height;
	var ctx = canvs.getContext('2d'); 
	ctx.drawImage(img,0,0);
	var dataURL = canvs.toDataURL('image/jpeg');
	img.src = dataURL;
}
HtmlField.prototype.setPasteImage = function(htmlField, event) {
	var items = event.clipboardData.items;
	for (var i=0, ele=null; items && i<items.length; ++i) {
		ele = items[i];
		if (!(ele.kind=='file' && ele.type.indexOf('image/')!==-1))
			continue;
		var blob = ele.getAsFile();
		window.URL = window.URL || window.webkitURL;
		var blobUrl = window.URL.createObjectURL(blob);
		var img = document.createElement('img');
		img.src = blobUrl;
		img.crossOrigin = '';
		if ('copy into div'.length<0) {
			var sel = window.getSelection();
			var range = sel.getRangeAt(0);
			range.deleteContents();
			var frag = document.createDocumentFragment();
			var lastNode = frag.appendChild(img);
			range.insertNode(frag);
			var contentRange = range.cloneRange();
			contentRange.setStartAfter(lastNode);
			contentRange.collapse(true);
			sel.removeAllRanges();
			sel.addRange(contentRange);
		}
	}
}
