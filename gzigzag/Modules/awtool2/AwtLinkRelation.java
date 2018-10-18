/*   
AwtLinkRelation.java
 *    
 *    Copyright (c) 1999-2000, Ted Nelson and Tuomas Lukka
 *
 *    You may use and distribute under the terms of either the GNU Lesser
 *    General Public License, either version 2 of the license or,
 *    at your choice, any later version. Alternatively, you may use and
 *    distribute under the terms of the XPL.
 *
 *    See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of 
 *    the licenses.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
 *    file for more details.
 *
 */
/*
 * Written by Kimmo Wideroos
 */
 
package org.gzigzag.module;
import org.gzigzag.*;
import java.awt.*;
import java.util.*;


//class AwtLinkRelation /*extends AwtRelation*/ {
public class AwtLinkRelation implements AwtAccursable {
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) ZZLogger.log(s); }
    protected static void pa(String s) { ZZLogger.log(s); }

    static String LINK_TO = "to";
    static String LINK_FROM = "from";
    
    ZZCell cell;

    public AwtLinkRelation(ZZCell cell) {
	this.cell = cell;
    }

    public ZZCell getCell() { return cell; }

    public static ZZCell make(ZZCell from, ZZCell to) {
	return make(from, to, false);
    }

    public static ZZCell make(ZZCell from, ZZCell to, boolean directed) {
        ZZCell new_link, lc1, lc2;
        boolean lr1, lr2;

	if(!valid(from, to)) {
	    pa("LINK NOT VALID!!!");
	    return null;
	}
	//remove(from, to);

        lr1 = isLinkRoot(from);
        lr2 = isLinkRoot(to);

        if(lr1 == lr2) {
            // new central link needed
	    lc1 = from.h(AwtDim.link, 1).N(AwtDim.link);
	    setLinkType(lc1, LINK_FROM);
	    lc2 = to.h(AwtDim.link, 1).N(AwtDim.link);
	    setLinkType(lc2, LINK_TO);
	    lc1.connect("d.2", lc2);
            new_link = lc1.N("d.2", -1);
	    new_link.setText(""+(from.t())+"->"+(to.t()));
        } else {
            // attach single artefact to actual link cell
            if(lr1) {
                new_link = from.h("d.2", 1).N("d.2");
                new_link.connect(AwtDim.link, -1, to.h(AwtDim.link, 1));
		setLinkType(new_link, LINK_TO);
            } else {
                new_link = to.h("d.2", 1).N("d.2");
                new_link.connect(AwtDim.link, -1, from.h(AwtDim.link, 1));
		setLinkType(new_link, LINK_FROM);
            }
        }
        return new_link;
    }

    static public void remove(ZZCell from, ZZCell to) {
	ZZCell to_be_removed = link(from, to);
	remove(to_be_removed);
    }

    static public void change(ZZCell from, ZZCell to) {
	ZZCell to_be_changed = link(from, to);
	change(to_be_changed);
    }
	
    static public void change(ZZCell lc) {
	if(lc == null) return;

	if(isLinkRelation(lc))
	    changeLinkType(lc);
	else 
	if(one2one(lc)) 
	    for(ZZCell c=lc.s("d.2"); c!=null; c=c.s("d.2")){
		changeLinkType(c);
	    }
    }

    /* is link 'from' - 'to' valid? No loops allowed.
     * returns true, if valid.
     */
    static public boolean valid(ZZCell from, ZZCell to) {
	ZZCell lc, maybe_lc, layer;
	ZZCell[] as1, as2; // artefacts
	int i;
	// links not allowed between layers!!
	if(AwtLayer.valid(from) || AwtLayer.valid(to)) return false; 
	// if source and target are both artefacts, they should
	// be on the same layer
	if(AwtArtefact.valid(from) && AwtArtefact.valid(to)) {
	    layer = AwtLayer.getLayer(from);
		if(!AwtLayer.isMember(layer, to)) return false;
	}
	if(!isLinkRoot(from) && !isLinkRoot(to)) 
	    return !one2one(from, to);
	lc = to;
	maybe_lc = from;
	if(isLinkRoot(from)) {
	    lc = from;
	    maybe_lc = to;
	} 
	as1 = getTreeArtefacts(lc);
	Hashtable hash = new Hashtable();
	for(i=0; i<as1.length; i++)
	    hash.put(as1[i], as1[i]);
	if(!isLinkRoot(maybe_lc)) {
	    if(hash.containsKey(maybe_lc)) return false;
	} else {
	    // first, check brutally that artefacts are on 
	    // the same layer. 
	    as2 = getTreeArtefacts(maybe_lc);
	    if(as1.length > 0 && as2.length > 0) {
		layer = AwtLayer.getLayer(as1[0]);
		if(!AwtLayer.isMember(layer, as2[0])) return false;
	    }
	    // artefs are not allowed to exist several time
	    // in same link tree.
	    for(i=0; i<as2.length; i++)
		if(hash.containsKey(as2[i])) return false;
	}
	return true;
    }

    static public ZZCell link(ZZCell from, ZZCell to) {
	Hashtable fromwh = new Hashtable();
	ZZCell tree = null, lc_from, lc_to;
	int to_level, from_level;
	ZZCell[] tows, fromws;
	boolean fromIsLink = isLinkRoot(from);
	boolean toIsLink = isLinkRoot(to);
	if(fromIsLink) {
	    pa("from is linkrelation!");
	    fromws = new ZZCell[1]; 
	    fromws[0] = getLinktree(from);
	} else 
	    fromws = getLinktrees(from);
	for(int i=0; i<fromws.length; i++) {
	    ZZCell fw = fromws[i];
	    fromwh.put(fw, fw);
	}
	if(toIsLink) {
	    pa("to is linkrelation!");
	    tows = new ZZCell[1]; 
	    tows[0] = getLinktree(to);
	} else
	    tows = getLinktrees(to);
	int mintree, ntree;
	mintree = 1000;
	for(int i=0; i<tows.length; i++) {
	    ZZCell tow = tows[i];
	    if(fromwh.containsKey(tow)) {
		ntree = getTreeArtefacts(tow).length;
		if(ntree>mintree) continue;
		mintree = ntree;
		tree = tow;
	    }
	}
	// 'tree' now is the root of smallest tree containing
	// both 'to' and 'from'  
	
	if(tree == null) return null;

	if(fromIsLink) lc_from = from.h(AwtDim.link, 1);
	else lc_from = findLinkCell(from, tree);

	if(toIsLink) lc_to = to.h(AwtDim.link, 1);
	else lc_to = findLinkCell(to, tree);

	to_level = getLevel(lc_to);
	from_level = getLevel(lc_from);

	ZZCell r = null;
	if(lc_to.h("d.2").equals(lc_from.h("d.2")) && to_level == from_level) {
	    //if(!one2one(lc_to)) return;
	    // remove whole linkroot
	    r = lc_to.h("d.2");
	}

	if(to_level > from_level) r = lc_to;
	if(to_level < from_level) r = lc_from;

	return r;

    }

    static public void remove(ZZCell lc) {
	ZZCell nxt = null;
	if(lc.s("d.2", -1) != null && one2one(lc)) remove(lc.h("d.2"));
	if(lc.s("d.2", -1) == null) nxt = lc.h(AwtDim.link, 1, true);
	if(nxt != null && one2one(nxt)) remove(nxt.h("d.2"));
	lc.excise(AwtDim.link);
	// is 'lc' linkroot?
	if(lc.s("d.2", -1) == null) {
	    ZZCell[] lcs = lc.readRank("d.2", 1, false, null);
	    for(int i=0; i<lcs.length; i++) { 
		lcs[i].excise(AwtDim.link);
		lcs[i].excise("d.2");
	    }
	} else {
	    lc.excise("d.2");
	}
    }

    public void remove() {
	remove(cell);
    }

    static public ZZCell findLinkCell(ZZCell artef, ZZCell tree) {
	/*
	ZZCell[] lroots = artef.readRankHeadcells(AwtDim.link, 1, false, 
						  "d.2", -1, null);
	*/
	ZZCell[] lcs = artef.readRank(AwtDim.link, 1, false); 
		
	for(int i=0; i<lcs.length; i++)
	    if(getLinktree(lcs[i].h("d.2")).equals(tree)) 
		return lcs[i];
	return null;
    }

    static public ZZCell getLinkRoot(ZZCell lc) {
	return lc.h("d.2");
    }

    /* return true if 'c' is a link relation maincell 
     */
    static public boolean isLinkRoot(ZZCell c) {
        if(c.h("d.2") != c || c.s("d.2") == null) return false;
        if(c.s("d.2").s(AwtDim.link, -1) == null) return false;
        return true;
    }

    /* return true if 'c' is a link relation cell 
     */
    static public boolean isLinkRelation(ZZCell c) {
        if(c.h(AwtDim.link, -1, true) == null) return false;
	if(c.s("d.2", -1) == null) return false;
        return true;
    }

    static public boolean one2one(ZZCell ar1, ZZCell ar2) {
	for(ZZCell c=ar1.s(AwtDim.link); c!=null; c=c.s(AwtDim.link)) {
	    if(!one2one(c)) continue;
	    ZZCell c2 = c.s("d.2")!=null ? c.s("d.2") : c.s("d.2", -1);
	    if(c2.h(AwtDim.link).equals(ar2)) return true;
	}
	return false;
    }

    static public boolean one2one(ZZCell lc) {
	return lc.h("d.2").readRank("d.2", 1, false).length == 2;
    }

    public boolean one2one() {
	return one2one(cell);
    }

    public static ZZCell getLinktree(ZZCell linkroot) {
	if(linkroot.s(AwtDim.link, 2) != null) 
	    throw new ZZError("AwtLinkRelation: linkroot is not a tree!");
	if(linkroot.s(AwtDim.link) == null) return linkroot;
	return getLinktree(linkroot.s(AwtDim.link).h("d.2"));
    }

    public ZZCell getLinktree() {
	return getLinktree(cell);
    }

    public static ZZCell[] getLinktrees(ZZCell artef) {
	ZZCell[] lroots;
	int i;
	lroots = artef.readRankHeadcells(AwtDim.link, 1, false, 
					 "d.2", -1, null);
	for(i=0; i<lroots.length; i++) { 
	    ZZCell wc = getLinktree(lroots[i]);
	    lroots[i] = wc;
	}
	return lroots;
    }

    private static int getTreeArtefacts(ZZCell tree, 
					ZZCell[][] res, int ind) {
	ZZCell c, artef;
	ZZCell la;
	String[] ldim = new String[2];
	int start, end;
	for(c=tree.s("d.2"); c!=null; c=c.s("d.2")) {
	    la = c.h(AwtDim.link);
	    if(la.s("d.2") == null) {
		// 'la' is actually an artefact!
		res[0][ind] = la;
		ind++;
		if(ind == res[0].length) {
		    // enlarge result array
		    ZZCell[] new_res = new ZZCell[ind+50];
		    System.arraycopy(new_res, 0, res[0], 0, ind-1);
		    res[0] = new_res;
		}
	    } else {
		ind = getTreeArtefacts(la, res, ind);
	    }
	}
	return ind;
    }

    public static ZZCell[] getTreeArtefacts(ZZCell tree) {
	ZZCell[][] res = new ZZCell[1][];
	res[0] = new ZZCell[50];
	int last_ind = getTreeArtefacts(tree, res, 0);
	ZZCell[] ret_artefs = new ZZCell[last_ind];
	System.arraycopy(res[0], 0, ret_artefs, 0, last_ind);
	return ret_artefs;
    }

    
    // linkcell's hierarchy level
    public static int getLevel(ZZCell linkcell) {
	ZZCell lc = linkcell;
	int i = 0;
	while(lc != null) {
	    lc = lc.h(AwtDim.link, 1);
	    if(lc==null) break;
	    lc = lc.h("d.2", -1, true);
	    i++;
	}
	return i;
    }

    public int getLevel() {
	return getLevel(cell);
    }

    /* 'lc' is link cell. if only 2 nodes are linked with it,
     * return the source node (if possible), otherwise null.
     */
    public static ZZCell source(ZZCell lc) {
        ZZCell c;
	int i=0;
	for(c = lc.h("d.2").s("d.2"); c!=null; c=c.s("d.2")) { 
	    if(linkType(c, LINK_FROM)) return c.h(AwtDim.link);
	    if(i==1) break;
	    i++;
	}
	return null;
    }

    /* 'lc' is link cell. if only 2 nodes are linked with it,
     * return the target node (if possible), otherwise null.
     */
    public static ZZCell target(ZZCell lc) { 
        ZZCell c;
	int i=0;
	for(c = lc.h("d.2").s("d.2"); c!=null; c=c.s("d.2")) { 
	    if(linkType(c, LINK_TO)) return c.h(AwtDim.link);
	    if(i==1) break;
	    i++;
	}
	return null;
    }


    public static void changeLinkType(ZZCell lc) {
	String newt = "";
	if(linkType(lc).equals(LINK_TO)) newt = LINK_FROM; 
	if(linkType(lc).equals(LINK_FROM)) newt = LINK_TO;
	lc.setText(newt);
    } 


    public static void setLinkType(ZZCell lc, String type) {
	lc.setText(type);
    } 
    public void setLinkType(String type) {
	setLinkType(type);
    }

    public static String linkType(ZZCell lc) {
	return lc.t();
    } 
    public String linkType() {
	return linkType(cell);
    }

    public static boolean linkType(ZZCell lc, String type) {
	return lc.t().equals(type);
    } 
    public boolean linkType(String type) {
	return linkType(cell, type);
    }
    

}

