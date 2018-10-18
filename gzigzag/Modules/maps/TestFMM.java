/*   
TestFMM.java
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

package org.gzigzag.test;
import org.gzigzag.map.*;
import java.util.*;
import junit.framework.*;
import java.awt.*;
import java.awt.event.*;

/** Test FMM.
 */

public class TestFMM extends TestCase {
    public TestFMM(String s) { super(s); }

    float xmin = (float)0.0;
    float xmax = (float)1.0;
    float ymin = (float)0.0;
    float ymax = (float)1.0;

    public boolean isvalid(int[][] il, int x, int y) {
	int dx, dy;
        for(int i=0; i<27; i++) {
	    dx = il[i][0] - x;
	    dy = il[i][1] - y;
	    if(dx*dx+dy*dy < 4 || dx*dx+dy*dy > 18) {
		System.out.println("dx:"+dx+" dy:"+dy);
		return false;
	    }
	}
	System.out.println("\nx:"+x+" y:"+y+" xo:"+il[0][0]+" yo:"+il[0][1]);
	for(int j=0; j<27; j++) { 
	    System.out.print("X");
	    if(j==26) continue;
	    if(il[j][1] < il[j+1][1]) System.out.println("");
	    if(il[j+1][0]-il[j][0] > 2) System.out.print("---");
	}
        return true;
    }

    public void checkParticle(FMMParticle fp, float ccx, float ccy, 
			      int level) {

	float fcx = (xmax-xmin) / (1<<level);
	float fcy = (ymax-ymin) / (1<<level);
 
	//System.out.println("fp ("+fp.x+", "+fp.y+")");
	//System.out.println("fp.x-ccx = "+(fp.x-ccx)+" 0.5*fcx="+(0.5*fcx));
	//System.out.println("fp.y-ccy = "+(fp.y-ccy)+" 0.5*fcy="+(0.5*fcy));

	if(fp.x-ccx >= -0.5*fcx && fp.x-ccx <= 0.5*fcx &&
	   fp.y-ccy >= -0.5*fcy && fp.y-ccy <= 0.5*fcy ) return;
	assertTrue("checkParticle: fp.x="+fp.x+", ccx="+ccx+", fcx="+fcx+"\nfp.y="+fp.y+", ccy="+ccy+", fcy="+fcy, false);
    }

    public static String binStr(int c) {
	StringBuffer sb = new StringBuffer("00000000000000000000000000000000");
	int b = 1;
	for(int i=31; i>=0; i-- ) {
	    if((c & b) != 0) 
		sb.setCharAt(i, '1');
	    else sb.setCharAt(i, '0');
	    b <<= 1;
	}
	return sb.toString();
    }

    public void complex() {
	Complex t = new Complex((float)-0.1, (float)10.0);
	Complex t2 = new Complex(t);
	t.mul(new Complex((float)0.3, (float)-5.0));
	t.div(new Complex((float)0.3, (float)-5.0));
	assertTrue("Complex mul/div-bug!", t.near(t2, (float)0.001)); 

    }

    public void keys() {
	int N = 10;
	FMMHashTree fms = new FMMHashTree((float)-2.0, (float)6.0, (float)-0.3, (float)12.0, N, (float)0.001);
	System.out.println("N="+N+" levels in hierarchy="+fms.nLevel()); 
	System.out.println("Keytests:");

	String keyStr;
	int keyA, keyB, keyC, keyD, xkey, ykey;
	keyA = fms.makeKeyPart((float)1.0, new float[] {(float)-2.0, (float)6.0});
	keyStr = binStr(keyA);
	System.out.println("makeKeyPart((float)1.0, (float)-2.0, (float)6.0) = "+keyStr);
	
	keyB = fms.makeKeyPart((float)11.0, new float[] {(float)-0.3, (float)12.0});
	keyStr = binStr(keyB);
	System.out.println("makeKeyPart((float)11.0, (float)-0.3, (float)12.0) = "+keyStr);

	float[] midp = new float[2];
	keyC = fms.makeKey((float)1.0, (float)11.0, midp);
	keyStr = binStr(keyC);   
	System.out.println("makeKey((float)-2.0, (float)6.0) = "+keyStr);
	System.out.println("midp["+midp[0]+","+midp[1]+"]");


	assertTrue("buildKey", 
		    (keyC == (fms.buildKey(keyA, keyB, fms.nLevel()))) 
	);
	int[] keystuff = fms.breakKey(keyC);
	xkey = keystuff[0];
	ykey = keystuff[1];

	System.out.println("level(breakKey gen): "+keystuff[2]);
	System.out.println("xkey(true): "+binStr(keyA));
	System.out.println("xkey(breakKey gen): "+binStr(xkey));
	System.out.println("ykey(true): "+binStr(keyB));
	System.out.println("ykey(breakKey gen): "+binStr(ykey));

	assertTrue("breakKey", (xkey == keyA) && (ykey == keyB));

	keyD = fms.transformKey(keyC, fms.nLevel(), fms.nLevel()-1);
	System.out.println("key(level="+fms.nLevel()+"): "+binStr(keyC));
	System.out.println("key(level="+(fms.nLevel()-1)+"): "+binStr(keyD));

	keyD = fms.transformKey(keyC, fms.nLevel(), fms.nLevel()+1);
	keyD = fms.transformKey(keyD, fms.nLevel()+1, fms.nLevel());
	assertTrue("transformKey", keyC == keyD);
    }

    public void dumpChildren(FMM.FMMStructure fms, FMMCell fc) {
	FMMCell[] fccs = fms.getChildren(fc);
	for(int i=0; i<fc.keylevel; i++)
	    System.out.print("  ");
	System.out.println(binStr(fc.key));
	if(fccs==null) return;
	for(int i=0; i<fccs.length; i++) 
	    dumpChildren(fms, fccs[i]);
    }

    public void tree() {
	FMMHashTree fms = new FMMHashTree((float)0.0, (float)1.0, (float)0.0, (float)1.0, 10, (float)0.1);
	fms.add((float)0.0, (float)0.0, 1);
	fms.add((float)1.0, (float)1.0, 1);
	FMMCell[] parents = fms.get(0);
	assertTrue("Parent null!", parents != null);
	int level = fms.nLevel();
        dumpChildren(fms, parents[0]);

	Random r = new Random();
	float x, y;
	for(int i=0; i<100; i++) {
	    x = xmax*r.nextFloat();
	    y = ymax*r.nextFloat();
	    float[] c = new float[2];
	    int key = fms.makeKey(x, y, c);
	    FMMCell fc = (FMMCell)fms.fmmcells[key];
	    FMMCell[] fcs = fms.getChildren(fc);
	    assertTrue("cell impotent, no children!", fcs.length==4 || fc.keylevel==fms.nLevel());
	    for(int j=0; j<fcs.length; j++) {
		assertTrue("wrong parent!", fms.getParent(fcs[j]).equals(fc));
	    }
	}
    }

    private void fmsadd(FMM.FMMStructure[] fmss, 
			float x, float y, float q) {
	int[] qi = new int[]{Math.abs((int)(100*q)), (int)(100*q)};
	for(int i=0; i<fmss.length; i++) {
	    fmss[i].add(x, y, qi);
	}
    }

    private void fmsadd(FMM.FMMStructure[] fmss, 
			float x, float y, int[] q) {
	int qsum = 0;
	for(int i=0; i<q.length; i++) qsum += Math.abs(q[i]);
	int[] qn = new int[q.length+1];
	System.arraycopy(q, 0, qn, 1, q.length);
	qn[0] = qsum;
	for(int i=0; i<fmss.length; i++) {
	    fmss[i].add(x, y, qn);
	}
    }
    public FMM.FMMStructure storeParticles(FMM.FMMStructure fms, 
					   int N, int which) {
	return (storeParticles(new FMM.FMMStructure[] {fms}, N, which))[0];
    }

    public FMM.FMMStructure[] storeParticles(FMM.FMMStructure[] fmss, 
					     int N, int which) {
	float x, y;    
	if(which==0) {
	    Random r = new Random();
	    for(int i=0; i<N; i++) {
		x = (float)0.2+((float)0.6*xmax*r.nextFloat());
		y = (float)0.2+((float)0.6*ymax*r.nextFloat());
  		fmsadd(fmss, x, y, r.nextFloat());
  	    }
  	}

  	if(which==1) {
  	    //fmsadd(fmss, (float)0.5, (float)0.5, (float)-20.0);
  	    for(float rad=(float)0.0; rad<(float)6.28; rad += (float)0.3) {
  		x = (float)((1.1+Math.cos(rad))/2.2);
  		y = (float)((1.1+Math.sin(rad))/2.2);
		fmsadd(fmss, x, y, -2*x-2*y);
	    }
	}

	if(which==2) {
	    for(x=(float)0.25; x<(float)1.0; x += (float)0.5)
		for(y=(float)0.25; y<(float)1.0; y += (float)0.25)
		    fmsadd(fmss, x, y, (float)1.0);
	}
	
	if(which==3) {
	    for(x=(float)(0.125/2.0); x<(float)1.0; x += (float)0.125)
		for(y=(float)(0.125/2.0); y<(float)1.0; y += (float)0.125)
		    fmsadd(fmss, x, y, (float)1.0);
	}

	if(which==4) {
	    fmsadd(fmss, (float)0.25, (float)0.25, new int[] {-50, 00});
	    fmsadd(fmss, (float)0.75, (float)0.25, new int[] {-50, -50});
	    fmsadd(fmss, (float)0.25, (float)0.75, new int[] {-50, 00});
	    fmsadd(fmss, (float)0.75, (float)0.75, new int[] {00, -50});
	}
	
	if(which==5) {
	    int d = 8;
	    Random r = new Random();
	    for(int i=0; i<N; i++) {
		x = (float)0.2+((float)0.6*xmax*r.nextFloat());
		y = (float)0.2+((float)0.6*ymax*r.nextFloat());
		int[] q = new int[d];
		for(int j=0; j<d; j++) {
		    q[j] = (int)(-r.nextFloat()*50.0);
		    if(q[j]>-10) q[j] = 0;
		    if(q[j]<-10) q[j] = -50;
		} 
		fmsadd(fmss, x, y, q);
	    }
	}
	return fmss;
    }

    public void structure_validity() {
	FMM.FMMStructure fmss[] = new FMMHashTree[1];
	fmss[0] = new FMMHashTree(xmin, xmax, ymin, ymax, 150, (float)0.01);

	storeParticles(fmss, 150, 0);
	
	FMM.FMMStructure fms = fmss[0];
	int level = fms.nLevel();
	for(int i=0; i<=level; i++) {
	    FMMCell[] fcs = fms.get(i);
	    for(int j=0; j<fcs.length; j++) {
		FMMParticle[] fps = fms.getParticles(fcs[j]);
		for(int k=0; k<fps.length; k++) {
		    int key = fcs[j].key;
		    float ccx = fcs[j].xcenter;
		    float ccy = fcs[j].ycenter;
		    checkParticle(fps[k], ccx, ccy, level);
		}
	    }
	}
    }

    public void compare(FMM.FMMStructure fms1, FMM.FMMStructure fms2,
			   float precision) {
	Complex p1 = new Complex();
	Complex p2 = new Complex();
	FMMCell[] fcs = fms1.get(fms1.nLevel());

	for(int j=0; j<fcs.length; j++) {
	    FMMParticle[] fps = fms1.getParticles(fcs[j]);
	    for(int i=0; i<fps.length; i++) {
		float x = fps[i].x;
		float y = fps[i].y;
		FMMParticle fp2 = fms2.getParticle(x, y);
		assertTrue("no match in fms2!", fp2 != null);
		p1.set(fps[i].F);
		p2.set(fp2.F);
		assertTrue("fms1("+p1+") <<>> fms2("+p2+")!", p1.near(p2, precision*(float)2.0));
	    }
	}
    }
    
    class TestView extends Panel {
	FMM.FMMStructure[] fmss;
	TestView(FMM.FMMStructure[] fmss) { 
	    super(); 
	    this.fmss = fmss;
	    addComponentListener(new ComponentAdapter() {
		    public void componentShown(ComponentEvent e) {
                        repaint();
		    }
		});
	}



	private Color makeColor(Color b, int i) {
	    float cof = (float)Math.pow(0.9, i);
	    return new Color(
			     (int)(cof*b.getRed()),
			     (int)(cof*b.getGreen()),
			     (int)(cof*b.getBlue())
			     );
	}

	private float radius(int q) {
	    return (float)(0.01*Math.sqrt(q/Math.PI));
	}

	public void paintNDParticle(Graphics gr, FMMParticle fp) {
	    int rx, r, x, y, dx, dy;
	    Dimension vd = getSize();

	    r = (int)((radius(fp.q[0])/(ymax-ymin))*vd.height);
	    x = (int)((fp.x/(xmax-xmin))*vd.width) - (r>>1);
	    y = (int)((fp.y/(ymax-ymin))*vd.height) - (r>>1);
	    for(int i=1; i<fp.q.length; i++) {
		rx = (int)((radius(Math.abs(fp.q[i]))/(xmax-xmin))*vd.width);
		if(fp.q[i]>0)
		    gr.setColor(makeColor(Color.red, i<<1));
		else
		    gr.setColor(makeColor(Color.blue, i<<1));
		gr.fillRect(x, y, rx, r);
		x += rx;
	    }
	}

	public void paintParticle(Graphics gr, FMMParticle fp) {
	    int rx, ry, x, y, dx, dy;

	    if(fp.q.length>2) {
		paintNDParticle(gr, fp);
		return;
	    }

	    Dimension vd = getSize();

	    rx = (int)((radius(fp.q[0])/(xmax-xmin))*vd.width);
	    ry = (int)((radius(fp.q[0])/(ymax-ymin))*vd.height);
	    x = (int)((fp.x/(xmax-xmin))*vd.width);
	    y = (int)((fp.y/(ymax-ymin))*vd.height);
	    if(fp.q[1]>0)
		gr.setColor(Color.red);
	    else
		gr.setColor(Color.blue);		
	    gr.fillOval(x-(rx>>1), y-(ry>>1), rx, ry);
	    
	    Complex s = new Complex();
	    s.set(fp.far);
	    s.div(fp.q[0]);
	    dx = (int)((s.r()/(xmax-xmin))*vd.width/100.0);
	    dy = (int)((s.i()/(ymax-ymin))*vd.height/100.0);
	    gr.setColor(Color.cyan);
	    gr.drawLine(x, y, x+dx, y+dy);
		
	    s.set(fp.near);
	    s.div(fp.q[0]);
	    dx = (int)((s.r()/(xmax-xmin))*vd.width/100.0);
	    dy = (int)((s.i()/(ymax-ymin))*vd.height/100.0);
	    gr.setColor(Color.magenta);
	    gr.drawLine(x, y, x+dx, y+dy);
	    
	    s.set(fp.F);
	    s.div(fp.q[0]);
	    dx = (int)((s.r()/(xmax-xmin))*vd.width/100.0);
	    dy = (int)((s.i()/(ymax-ymin))*vd.height/100.0);
	    gr.setColor(Color.black);
	    gr.drawLine(x, y, x+dx, y+dy);
	    /*
	    if(fms_ref == null) return;
	    // error
	    s.set(fp.F);
	    FMMParticle fp2 = fms_ref.getParticle(fp.x, fp.y);
	    s.sub(fp2.F);
	    s.div(fp.q[0]);
	    dx = (int)((s.r()/(xmax-xmin))*vd.width/100.0);
	    dy = (int)((s.i()/(ymax-ymin))*vd.height/100.0);
	    gr.setColor(Color.cyan);
	    gr.drawLine(x, y, x+dx, y+dy);
	    */
	}

	public void paint(Graphics gr) {
	    for(int i=1; i<fmss.length; i++) {
		FMM.FMMStructure fms = fmss[i];
		FMMParticle[] fps = fms.getAllParticles();
		for(int j=0; j<fps.length; j++)
		    paintParticle(gr, fps[j]);
	    }
	}
    }

    class TestAnimation extends TestView {
	private FMM fmm;
	private int qlength;
	private int bQ;
	private float borderSharpness, 
	    borderElasticity, mediumViscosity;

	TestAnimation(FMM.FMMStructure[] fmss) { 
	    super(fmss);
	    fmm = new FMM(fmss[0]);
	    // XXX hack!! have to get one particle by hook or by crook
	    FMMParticle[] fps = fmss[0].getAllParticles();
	    qlength = fps[0].q.length;
	    bQ = -200; // border 'charge'
	    borderSharpness = (float)1.0;
	    borderElasticity = (float)0.3;
	    mediumViscosity = (float)0.6;
	}

	// fp.V will be updated according to fp.F
	private void updateParticlePosition(FMMParticle fp) {
	    float e, dx, dy;

	    Complex F, borderF, a;
	    borderF = new Complex((float)-bQ/((fp.x-xmin)*borderSharpness)
				  +(float)bQ/((xmax-fp.x)*borderSharpness),
				  (float)-bQ/((fp.y-ymin)*borderSharpness)
				  +(float)bQ/((ymax-fp.y)*borderSharpness));

	    F = new Complex(fp.F);
	    F.add(borderF);

	    a = new Complex(F); 
	    a.div(fp.q[0]);
	    fp.V.set(a);

	    Complex s = fp.V;

	    fp.x += (float)(s.r()/5000.0);
	    fp.y += (float)(s.i()/5000.0);

	    if(fp.x<=xmin) { 
		fp.x = (float)xmin+(xmax-xmin)*(float)0.01;
		fp.V.neg(); fp.V.mul(borderElasticity);
	    }
	    if(fp.x>=xmax) { 
		fp.x = (float)xmax-(xmax-xmin)*(float)0.01;
		fp.V.neg(); fp.V.mul(borderElasticity);
	    }
	    if(fp.y<=ymin) { 
		fp.y = (float)ymin+(ymax-ymin)*(float)0.01;
		fp.V.neg(); fp.V.mul(borderElasticity);
	    }
	    if(fp.y>=ymax) { 
		fp.y = (float)ymax-(ymax-ymin)*(float)0.01;
		fp.V.neg(); fp.V.mul(borderElasticity);
	    }

	}

	private int i=0;
	public void paint(Graphics gr) {
	    if(i==0) { updateField(); i=10;}
	    i--;
	    FMM.FMMStructure fms = fmss[0];
	    FMMParticle[] fps = fms.getAllParticles();
	    fms.empty();
	    for(int j=0; j<fps.length; j++) {
		updateParticlePosition(fps[j]);
		fms.add(fps[j]);
		paintParticle(gr, fps[j]);
	    }
	}

	public void updateField() {
	    if(qlength == 1) {
		fmm.proceed(0);
		return;
	    }
	    fmm.proceed(1);
	    for(int i=2; i<qlength; i++)
		fmm.proceed(i, (float)1.0, true);	
	}

    }

    public void visualization() {
	Dimension viewdim;
	FMMHashTree[] fmss = new FMMHashTree[2];
        fmss[0] = new FMMHashTree(xmin, xmax, ymin, ymax, 1, (float)0.1);
	fmss[1] = new FMMHashTree(xmin, xmax, ymin, ymax, 16, (float)0.0001);
	storeParticles(fmss, 16, 2);
	do_algorithm(fmss[0]);
	do_algorithm(fmss[1]);

	//if(true) return;
	TestView fv = new TestView(fmss);
	
	Frame f = new Frame("testi");
	if(xmax-xmin > ymax-ymin) {
	    viewdim = new Dimension(400, (int)(400.0*(ymax-ymin)/(xmax-xmin)));
	} else {
	    viewdim = new Dimension((int)(400.0*(xmax-xmin)/(ymax-ymin)), 400);;
	}
	f.setSize(viewdim); f.add(fv);
	while(true) {
	    fv.repaint();
	    f.setVisible(true); 
	}
    }

    public void animation(int which) {
	Dimension viewdim;
	FMMHashTree[] fmss = new FMMHashTree[1];
	fmss[0] = new FMMHashTree(xmin, xmax, ymin, ymax, 16, (float)0.0001);
	storeParticles(fmss, 10, which);
	do_algorithm(fmss[0]);

	TestAnimation fv = new TestAnimation(fmss);

	Frame f = new Frame("animationtest");
	if(xmax-xmin > ymax-ymin) {
	    viewdim = new Dimension(400, (int)(400.0*(ymax-ymin)/(xmax-xmin)));
	} else {
	    viewdim = new Dimension((int)(400.0*(xmax-xmin)/(ymax-ymin)), 400);;
	}


	f.setSize(viewdim); f.add(fv);
	fv.repaint();
	f.setVisible(true);
	System.out.println("\n\nInput vectors as color vecs. Blue means repulsion force, red attraction power!");
	while(true) {
	    fv.repaint();
	    f.setVisible(true);
	}
    }

    public void isNan(FMM.FMMStructure fms, int l) {
	FMMCell[] fcs = fms.get(fms.nLevel());
	int ibox;
	for(ibox=0; ibox<fcs.length; ibox++) {
	    FMMCell fc=fcs[ibox];
	    for(int j=0; j<fc.b.length; j++) {
		assertTrue("b isNaN, stage:"+l, !fc.b[j].isNaN());		
		assertTrue("a isNaN, stage:"+l, !fc.a[j].isNaN());
	    }
	}	
    }

    private void do_algorithm(FMM.FMMStructure fms) {
	int level = fms.nLevel();
	FMM fmm = new FMM(fms);
	//for(int i=0; i<100; i++)
	fmm.proceed(0);
    }

    public long algorithm(int card, float err) {
	FMM.FMMStructure fmss[] = new FMMHashTree[2];
	fmss[0] = new FMMHashTree(xmin, xmax, ymin, ymax, card>>3, err);
	fmss[1] = new FMMHashTree(xmin, xmax, ymin, ymax, 1, err);

	storeParticles(fmss, card, 0);

	do_algorithm(fmss[1]);
	long cur_time = System.currentTimeMillis();
	System.out.println("proceed...");
	do_algorithm(fmss[0]);
	long elapsed = System.currentTimeMillis()-cur_time;
	System.out.println("elapsed(N="+card+",err="+err+"):"+elapsed+" ms");
	//algorithm(fms2);

        compare(fmss[0], fmss[1], err);
	return elapsed;
    }

    public void algorithm_benchmark() {
	float err = (float)0.001;
	algorithm(30, err);
	algorithm(100, err);
	//algorithm(1000, err);
    }

    public void algorithm_details() {
	//translation (Lemma 2.5)
	int p=5;
	FMMCell[] fccs = new FMMCell[] {
	    new FMMCell(0, -1, -1, 0, p),
	    new FMMCell(0,  1,  1, 0, p),
	    new FMMCell(0,  1, -1, 0, p),
	    new FMMCell(0, -1,  1, 0, p)
	};
	FMMCell fc = new FMMCell(0, 0, 0, 0, p);
	for(int i=0; i<p+1; i++) fc.b[i].set(i+1);
	for(int i=0; i<fccs.length; i++) {
	    FMMCell fcc = fccs[i];
	    Complex[] z0 = FMM.zpow(fcc.xcenter-fc.xcenter, 
	    		    fcc.ycenter-fc.ycenter, p+1);
		Complex tmp = new Complex();
		//Complex[] z0 = FMM.zpow(fc.xcenter-fcc.xcenter, 
		//fc.ycenter-fcc.ycenter, p+1);
	    for(int l=0; l<p+1; l++) {
		for(int k=l; k<p+1; k++) {
		    tmp.set(fc.b[k]);
		    tmp.mul((float)FMM.bincof(k, l));
		    tmp.mul(z0[k-l]);
		    if(((k-l) & 1) == 1) tmp.neg();
		    
		    fcc.b[l].add(tmp);
		}
	    }
	}
	Complex zj = new Complex();
	Complex z = new Complex();
	Complex f = new Complex();
	Complex s1 = new Complex();
	Complex s2 = new Complex();

	zj.set((float)1.0, (float)0.0);

	for(int j=0; j<p+1; j++) {
	    f.set(fc.b[j]);
	    f.mul(zj);
	    s1.add(f);
	    zj.mul(z);
	}
	for(int i=0; i<fccs.length; i++) {
	    FMMCell fcc = fccs[i];
	    z.set(fcc.xcenter, fcc.ycenter);
	    zj.set((float)1.0, (float)0.0);
            s2.set((float)0.0);
	    for(int j=0; j<p+1; j++) {
		f.set(fccs[i].b[j]);
		f.mul(zj);
		s2.add(f);
		zj.mul(z);
	    }
	    assertTrue("translation error! s1<>s2", s1.near(s2, (float)0.001));
	}
    }

    public void testAll() {
	//complex();
	//keys();
	//tree();
	//structure_validity();
	//algorithm_details();
	//algorithm_benchmark();
	//visualization();
	//animation(4);
	//animation(5);
    } 

}

 

