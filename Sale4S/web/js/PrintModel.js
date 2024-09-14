function CheckLabels(panel) {
	var ids = panel.getAttribute('item_ids').split(';');
	for(var items=$(null,ids[0]),titem,ti=0,tsize=items.length; ti<tsize; ti++) {
		titem=items[ti];
		titem.parentNode.style.backgroundColor=(panel.innerHTML.indexOf(titem.innerHTML)>-1? 'white': '');
	}
	for(var items=$(null,ids[1]),titem,ti=0,tsize=items.length; ti<tsize; ti++) {
		titem=items[ti];
		titem.parentNode.style.backgroundColor=(panel.innerHTML.indexOf(titem.innerHTML)>-1? 'white': '');
	}
	for(var items=$(null,ids[2]),titem,ti=0,tsize=items.length; ti<tsize; ti++) {
		titem=items[ti];
		titem.parentNode.style.backgroundColor=(panel.innerHTML.indexOf(titem.innerHTML)>-1? 'white': '');
	}
}
function ChangeModel(panel) {
	var ids = panel.getAttribute('item_ids').split(';');
	var trList=panel.getElementsByTagName('TR'), trRows=new Array();
	for (var items=$(null,ids[1]),titem,ti=0,tsize=items.length; ti<tsize; ti++) {// get labeled trs
		titem=items[ti];
		if (panel.innerHTML.indexOf(titem.innerHTML)==-1)
			continue;
		for (var fi=trList.length,fitem; fi-->0;) {
			fitem=trList[fi];
			if (fitem.innerHTML.indexOf(titem.innerHTML)>-1 && indexOf(trRows, fitem)==-1)
				trRows.push(fitem);
		}
	}
	for (var items=trRows,titem,ti=items.length; ti-->0;) {// remove bigger tr container
		titem=items[ti];
		for (var fi=ti,fitem; fi-->0;) {
			fitem=items[fi];
			if (indexOf(titem.getElementsByTagName('TR'), fitem)>-1) {
				trRows.splice(ti, 1);
				ti=items.length;
				break;
			} else if (indexOf(fitem.getElementsByTagName('TR'), titem)>-1) {
				trRows.splice(fi, 1);
				ti=items.length;
				break;
			}
		}
	}
	var trXml='';
	for (var items=trRows,titem,ti=items.length; ti-->0;) {
		titem=items[ti];
		trXml = trXml+'__tr__'+titem.outerHTML;
	}	
	L_propertychange(panel, (panel.innerHTML+trXml).replace(/\s{2,}/g, ''));
}
