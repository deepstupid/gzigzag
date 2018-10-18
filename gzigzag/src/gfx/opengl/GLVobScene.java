/*   
GLVobScene.java
 *    
 *    Copyright (c) 2001, Tuomas Lukka
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
 * Written by Tuomas Lukka
 */
package org.gzigzag.gfx;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class GLVobScene {
public static final String rcsid = "$Id: GLVobScene.java,v 1.8 2002/03/25 19:47:20 jvk Exp $";
    public static void p(String s) { System.out.println(s); }
    float[] coordsys = new float[10000]; int ncoordsys;

    float[] interpcoordsys = new float[10000];

    int[] list = new int[6000];;

    int ncoordless = 0;

    int nvobs;
    GLVob[] vobs = new GLVob[2000];
    int[] coordsys1 = new int[2000];
    int[] coordsys2 = new int[2000];

    int[] coordsysindices = new int[2000];

    int newAffineCoordSys(float cx, float cy,
		    float x_x, float x_y, 
		    float y_x, float y_y,
		    float z
		    ) {
	coordsys[ncoordsys] 	= cx;
	coordsys[ncoordsys + 1] = cy;
	coordsys[ncoordsys + 2] = x_x;
	coordsys[ncoordsys + 3] = x_y;
	coordsys[ncoordsys + 4] = y_x;
	coordsys[ncoordsys + 5] = y_y;
	coordsys[ncoordsys + 6] = z;
	int was = ncoordsys;
	ncoordsys += 7;
	return was / 7;
    }
    void put(GLVob vob) {
	vobs[nvobs] = vob;
	coordsys1[nvobs] = -1;
	coordsys2[nvobs] = -1;
	putHash(nvobs, vob);
	nvobs++;
    }
    void put(GLVob vob, int coordsys) {
	vobs[nvobs] = vob;
	coordsys1[nvobs] = coordsys;
	coordsys2[nvobs] = -1;
	putHash(nvobs, vob);
	nvobs++;
    }
    void put(GLVob vob, int c1, int c2) {
	vobs[nvobs] = vob;
	coordsys1[nvobs] = c1;
	coordsys2[nvobs] = c2;
	putHash(nvobs, vob);
	nvobs++;
    }

    void makeInterpList(GLVobScene other) {
	curs = 0;
	for(int i=0; i<coordsysindices.length; i++)
	    coordsysindices[i] = 0;
	for(int i=0; i<nvobs; i++) {
	    GLVob v = vobs[i];
	    int ind = other.getVobIndex(v.key);
	    // p("GetVobIndex "+v.key+" got "+ind);
	    if(ind < 0 && coordsys1[i] != -1) continue;
	    
	    if (coordsys1[i] != -1) {
		coordsysindices[coordsys1[i]] = other.coordsys1[ind];
		// Possible bug - if number of coordsystems changes between
		// views. Shouldn't happen though.
		if(coordsys2[i] != -1)
		    coordsysindices[coordsys2[i]] = other.coordsys2[ind];
	    }

	    curs = vobs[i].addToList(list, curs, coordsys1[i], coordsys2[i]);
	}
	list[curs++] = 0;
    }

    void renderInterp(GZZGL.Window win, GLVobScene other, float fract) {
	/*
	p("Renderinterp "+fract);
	plist(list);
	p("Sysind:");
	plist(coordsysindices);
	*/
	GZZGL.render(win, list, coordsys, coordsysindices, other.coordsys,
			    ncoordsys,
				fract);
    }

    int curs;
    void makeFullList() {
	curs = 0;
	for(int i=0; i<nvobs; i++) {
	    curs = vobs[i].addToList(list, curs, coordsys1[i], coordsys2[i]);
	}
	list[curs++] = 0;
    }

    void renderFull(GZZGL.Window win) {
	p("Renderfull");
	GZZGL.render(win, list, coordsys, coordsys, ncoordsys, 0);
    }


    int getVobIndex(Object key) {
	return findIndex(key);
    }

    int getCoordSys(int vobIndex) {
	return coordsys1[vobIndex];
    }



    void dump() {
	p("COORDSYS:");
	plist(coordsys);
	p("ThList");
	plist(list);
    }
    void plist(int[] list) {
	for(int i=0; i<curs; i++) {
	    p(" "+list[i]);
	}
    }

    void plist(float[] list) {
	for(int i=0; i<curs; i++) {
	    p(" "+list[i]);
	}
    }


//----------------------------------------------------
// 		HASH INDEX

    /** The hash indices of the vobs.
     */
    int[] hashinds = new int[2000];

    /** Find the index of the given object in the given set.
     */
    int findIndex(Object key) {
	if(key == null) return -1;
	int start = key.hashCode() % hashinds.length;
	if(start < 0) start += hashinds.length;
	while(hashinds[start] != 0) {
	    if(key.equals(vobs[hashinds[start]-1].key)) {
		return hashinds[start]-1;
	    }
	    start ++;
	    start %= hashinds.length;
	}
	return -1;
    }

    void putHash(int i, GLVob vob) {
	if(vob.key == null) return;
	if(nvobs > hashinds.length / 2)
	    rehash(nvobs * 4);
	int start = vob.key.hashCode() % hashinds.length;
	if(start < 0) start += hashinds.length;
	// linear probing
	while(hashinds[start] != 0) {
	    start ++;
	    start %= hashinds.length;
	}
	hashinds[start] = i+1;
    }

    void rehash(int size) {
	hashinds = new int[size];
	for(int i=0; i<nvobs; i++) 
	    putHash(i, vobs[i]);
    }


}
