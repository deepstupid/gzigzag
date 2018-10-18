/*   
AwtArtefactFlobFactory.zob
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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
import org.gzigzag.module.multimedia.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.lang.Thread;

/** An abstract flob factory that shows awt artefacts.
 */
 
public abstract class AwtArtefactFlobFactory implements FlobFactory {
public static final String rcsid = "";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    private MediaFlobFactory mediafact=new MediaFlobFactory();

    public static Hashtable imagehash = new Hashtable();

    private AwtCursors crs = null;

    public void setCursors(AwtCursors acrs) { 
	crs = acrs; 
    }

    public class AwtBgFlob extends CellBgFlob {

        public AwtBgFlob(int x, int y, int d, int w, int h, ZZCell c) {
            super(x, y, d, w, h, c);
        }

        public void render(Graphics g, int mx, int my, int md, int mw, int mh){

            Color oldfg = g.getColor();
            Shape oldclip = g.getClip();
            g.clipRect(mx, my, mw, mh);

	    String style = "#DEFAULT", prefix = "", cursorStyle = "4";
            
	    // look & feel ;)
	    ZZCell lookfeel=c.s(AwtDim.d_style);
	    if(lookfeel != null) {
		style = lookfeel.t();
		prefix = lookfeel.h("d.clone").h("d.2").s("d.1", -1).t();
		cursorStyle = lookfeel.h("d.clone").s("d.1").t();
	    } 
	    /*
	    System.out.println("style:"+style);
	    System.out.println("prefix:"+prefix);
	    System.out.println("cursorStyle:"+cursorStyle);
	    */
	    if(style.indexOf("#") < 0 && prefix.indexOf("://") >= 0) {
		URL url = null;
		try {
		    url = new URL(""+prefix+style);
		} catch(MalformedURLException e) {}
		if(url!=null) {
		    //pa("url != null!!");
		    Image img;
		    if(imagehash.containsKey(url))
			img = (Image)imagehash.get(url);
		    else {
			img = Toolkit.getDefaultToolkit().getImage(url);
			int w = img.getWidth(null);
			int count = 2000000;
			while ((w < 0) && (count-- > 0)) {
			    pa("w:"+w);
			    try {
			    	Thread.currentThread().sleep(100);
			    } catch(Exception e) {}
			    w = img.getWidth(null);
			}

			imagehash.put(url, img);
		    }
		    g.drawImage(img, mx, my, mw, mh, null);
		}
	    } else {
		if(style.indexOf("#") >= 0) {
		    try {
			bg=Color.decode(style);
			g.setColor(bg);
			g.fillRect(mx, my, mw, mh);
		    } catch(Exception e) {}
		}
	    }

	    g.setColor(Color.decode("#E5E5E5"));
	    g.drawRect(mx-1, my-1, mw+1, mh+3);
	    g.drawRect(mx-2, my-2, mw+3, mh+3);

            if(cursorStyle.equals("1")) 
		renderCursorStyle1(g, mx, my, md, mw, mh);
            else if(cursorStyle.equals("2")) 
		renderCursorStyle2(g, mx, my, md, mw, mh);
            else if(cursorStyle.equals("3")) 
		renderCursorStyle3(g, mx, my, md, mw, mh);
            else if(cursorStyle.equals("4"))
		renderCursorStyle4(g, mx, my, md, mw, mh);

            g.setColor(oldfg);
            renderContent(g, mx, my, mw, mh);

            g.setColor(oldfg.darker());
            //g.drawRect(mx, my, mw-1, mh-1);

            g.setColor(oldfg);
            g.setClip(oldclip);
        }

	private void renderCursorStyle1(Graphics g, int mx, int my, 
				   int md, int mw, int mh) {
            Color[] solids = getSolidColors();
            int nsolids = 0;
            if(solids!=null) nsolids = solids.length;

	    int cr1=0, cr2=0;
            if(solids != null) {
                int iq=0, id=0;
		nsolids = nsolids>4 ? 4 : nsolids;
                for(int i=0; i<nsolids-1; i++) {
		    if(solids[i]==null) continue;
		    if(!solids[i].equals(AwtCursors.c_input1.color()) && 
		       !solids[i].equals(AwtCursors.c_input2.color()))
			continue;
                    g.setColor(solids[i]);
                    if(mw-iq-5>0 && mh-iq-5>0) {
                        g.drawRect(mx+id+1, my+id+1, mw-iq-3, mh-iq-3);
                        g.drawRect(mx+id+2, my+id+2, mw-iq-5, mh-iq-5);
                        id += 2;
                        iq += 4;
                    } else break;
		    //g.setColor(bg);
		    //g.fillRect(mx+id+1, my+id+1, mw-iq-3, mh-iq-3);
                }
		cr1=id+1;
		cr2=-iq-2;
            } else {
                //g.setColor(bg);
                //g.fillRect(mx, my, mw, mh);
            }
	}

	private void renderCursorStyle2(Graphics g, int mx, int my, 
				   int md, int mw, int mh) {
            Color[] solids = getSolidColors();
            int nsolids = 0;
            if(solids!=null) nsolids = solids.length;

            if(solids != null) {
		nsolids = nsolids>4 ? 4 : nsolids;
                for(int i=0; i<nsolids-1; i++) {
		    if(solids[i]==null) continue;
		    if(!solids[i].equals(AwtCursors.c_input1.color()) && 
		       !solids[i].equals(AwtCursors.c_input2.color()))
			continue;
                    g.setColor(solids[i]);
		    g.drawLine(mx+2+i*2, my, mx+2+i*2, my+mh);
		    g.drawLine(mx+2+i*2+1, my, mx+2+i*2+1, my+mh);
		}
	    }
	}

	private void renderCursorStyle3(Graphics g, int mx, int my, 
				   int md, int mw, int mh) {
            Color[] solids = getSolidColors();
            int nsolids = 0;
            if(solids!=null) nsolids = solids.length;

	    int[] xp = new int[4];
	    int[] yp = new int[4];

            if(solids != null) {
		nsolids = nsolids>4 ? 4 : nsolids;
                for(int i=0; i<nsolids-1; i++) {
		    if(solids[i]==null) continue;
		    if(!solids[i].equals(AwtCursors.c_input1.color()) && 
		       !solids[i].equals(AwtCursors.c_input2.color()))
			continue;
                    g.setColor(solids[i]);
		    xp[0] = mx+10+8*i;
		    xp[1] = mx+18+8*i;
		    xp[2] = mx;
		    xp[3] = mx;
		    yp[0] = my;    
		    yp[1] = my;    
		    yp[2] = my+18+8*i;
		    yp[3] = my+10+8*i;
                    g.setColor(solids[i]);
		    g.fillPolygon(xp, yp, 4);
		}
	    }
	}
	private void renderCursorStyle4(Graphics g, int mx, int my, 
				   int md, int mw, int mh) {
            Color[] solids = getSolidColors();
            int nsolids = 0;
            if(solids!=null) nsolids = solids.length;

	    int cr1=0, cr2=0;
            if(solids != null) {
                int iq=0, id=0;
		nsolids = nsolids>4 ? 4 : nsolids;
                for(int i=0; i<nsolids-1; i++) {
		    if(solids[i]==null) continue;
		    if(!solids[i].equals(AwtCursors.c_input1.color()) && 
		       !solids[i].equals(AwtCursors.c_input2.color()))
			continue;
                    g.setColor(solids[i]);
                    if(mw-iq-5>0 && mh-iq-5>0) {
                        g.drawRoundRect(mx+id+1, my+id+1, mw-iq-3, mh-iq-3, mw>>3, mh>>3);
                        g.drawRoundRect(mx+id+2, my+id+2, mw-iq-5, mh-iq-5, mw>>3, mh>>3);
                        id += 2;
                        iq += 4;
                    } else break;
		    //g.setColor(bg);
		    //g.fillRect(mx+id+1, my+id+1, mw-iq-3, mh-iq-3);
                }
		cr1=id+1;
		cr2=-iq-2;
            }
	    g.setColor(Color.black);
	    g.drawRoundRect(mx+1, my+1, mw-3, mh-3, mw>>3, mh>>3);
		
	}
    }



    protected AwtMetrics metrics = null;

    public AwtArtefactFlobFactory(AwtMetrics awtm) { this.metrics = awtm; }
    public AwtArtefactFlobFactory() { this.metrics = null; }

    public void setMetrics(AwtMetrics awtm) { this.metrics = awtm; }

    public Dimension getSize(ZZCell c, float fract) {
	AwtArtefact artf;
	double[] vDim = AwtArtefact.getDimension(c);
	double[] vCoord = AwtArtefact.getCoord(c); 
	return metrics.getRealDimension(vCoord, vDim);
    }


    public Flob makeFlob(FlobSet into, ZZCell c, ZZCell handleCell,
		float fract, double x, double y, int d, double w, double h) {
	double[] vDim = new double[] {w, h};
	double[] vCoord = new double[] {x, y};
	Dimension rDim = metrics.getRealDimension(vCoord, vDim);
	Point rCoord = metrics.mapToRealView(vCoord);
        return makeFlob(into, c, handleCell, (float)1.0, rCoord.x,rCoord.y, d, rDim.width,rDim.height);       
    }

    public Flob makeFlob(FlobSet into, ZZCell c, ZZCell handleCell,
		float fract, int x, int y, int d, int w, int h) {
        AwtBgFlob bgfl = new AwtBgFlob(x-2, y-2, d+1, w+4, h+4, c);
        CellFlobFactory2.addSolidColors(into, (CellBgFlob)bgfl);
        ((CellBgFlob)bgfl).setBg(Color.lightGray);
        renderFlob(into, c, handleCell, (float)1.0, x, y, d, w, h);       
	/*
	if(crs != null) {
	    if(c.equals(crs.get(AwtCursors.c_input1))) 
		((Colorer)bgfl).addColor(crs.c_input1.color());
	    if(c.equals(crs.get(AwtCursors.c_input1))) 
		((Colorer)bgfl).addColor(crs.c_input2.color());
	}
	*/
        into.add(bgfl);
	return bgfl;
    }

    abstract public Flob renderFlob(FlobSet into, ZZCell c, ZZCell handleCell,
                                  float fract, int x, int y, int d, int w, int h);

    public Flob placeFlob(FlobSet into, ZZCell c, ZZCell handleCell,
		    float fract, 
			int x, int y, int depth,
			float xfract, float yfract) {
        Dimension d = getSize(c, fract);
        return makeFlob(into, c, handleCell, fract,
                        (int)(x-xfract*d.width),
                        (int)(y-yfract*d.height),
                        depth, d.width, d.height);
  
    }

    public Flob centerFlob(FlobSet into, ZZCell c, ZZCell handleCell,
		float fract, Point p, int xalign, int yalign,
		int depth, Dimension d) {
        if(d == null) d = getSize(c, fract);
        int x, y;
	x = p.x - d.width/2; y = p.y - d.height/2;
        if(xalign < 0) x = p.x;
        if(xalign > 0) x = p.x - d.width;
        if(yalign < 0) y = p.y;
        if(yalign > 0) y = p.y - d.height;
        return makeFlob(into, c, handleCell, fract, x, y,
                        depth, d.width, d.height);
   
    }

    public Flob centerFlob(FlobSet into, ZZCell c, ZZCell handleCell,
		float fract, Point p, int xalign, int yalign, int depth) {
        return centerFlob(into, c, handleCell, fract, p, xalign, yalign,
                          depth, null);
    }

    public Flob centerFlob(FlobSet into, ZZCell c, ZZCell handleCell,
		float fract, double[] vp, int xalign, int yalign,
		int depth, double[] vd) {
        double x, y;
	x = vp[0] - vd[0]/2.0; y = vp[1] - vd[1]/2;
        if(xalign < 0) x = vp[0];
        if(xalign > 0) x = vp[0] - vd[0];
        if(yalign < 0) y = vp[1];
        if(yalign > 0) y = vp[1] - vd[1];
        return makeFlob(into, c, handleCell, fract, x, y,
                        depth, vd[0], vd[1]);
    }

    public Flob centerFlob(FlobSet into, ZZCell c, ZZCell handleCell,
		float fract, double[] vp, int xalign, int yalign,
		int depth) {
        double[] vd = AwtArtefact.getDimension(c);
        return centerFlob(into, c, handleCell, fract, vp, xalign, yalign, 
                          depth, vd);
    }

}

