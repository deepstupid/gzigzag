/*   
ZZGraphics.java
 *    
 *    Copyright (c) 2000, Ted Nelson, Tuomas Lukka and Vesa Parkkinen
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
 * Written by Vesa Parkkinen
 */

package org.gzigzag;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

/** A class that implements the ZZGraphics interface using OpenGL.
 * Tjl's comments:
 * <ul>
 *  <li> You DON'T want to derive everything from OGLColorThing: that
 * 	 slows down rendering. Note how it's done in ZZCanvas: there's
 *	 a ColorThing which sets the foreground/background color which
 * 	 is then used.
 *
 *       A state machine... Well, whatever you want. 
 *       Doesn't that somewhat dictate the ordering of drawing, 
 *       what if for some reason (optimization etc.) I want to
 *       draw the in different order ? I admit it slows rendering, 
 *       but I still would like to have objects with color, not just
 *       global pens to use in drawing, at least in the object level. 
 *       The implementation of rendering is a completely different thing,
 *       and shouldn't dictate design issues.
 *       The actual rendering is unbeliavably simple at the moment, 
 *       but i just wanted to  get the java-C-java pipeline to work. /vp
 *
 *	 Tjl: In that case, put in a reference to a reusable OGLColorThing
 *	 (in Design Patterns, this is called a flyweight).
 *	 the order of drawing is an excellent point: I was still thinking
 *	 in terms of java.awt.Graphics where order is visible; if we have
 *	 a Z-buffer, that's right. However, if we don't use a Z-buffer, the
 * 	 order of rendering is really important.
 *
 *	 OTOH: for drawing order, using the color makes sense since in 
 *       some cases changing the color (or material) in OpenGL 
 *       can be one of the slowest operations.
 *
 *       vp: That's why I would like to have attributes of objects to be
 *       within objects, so i could _easily_ handle them in the order
 *       that makes most sense at that moment.
 *
 *  <li> For Colors, extend java.awt.Color with alpha - in Java1.2, this
 *	 is already done. 
 *
 *       Why ? I just need 4 doubles. Of course there will be methods 
 *       to use Color, but internally i'd like to keep color as 
 *       doubles /vp
 *
 *	 Tjl: double = 8 bytes. 4 doubles = 32 bytes. java.awt.Color = 4 bytes.
 *	 first of all, we want to avoid float arithmetic so using bytes or 
 *       an integer would be better. glColor3b can be much faster.
 *
 *       vp: well it's pretty much same to me if it is a byte, float
 *       or double, but I would like to put it to the format it is used
 *       at creation time rather than in rendering time. 
 *
 *  <li> Why does colorthing have coordinates?
 *
 *       Because a Thing is always in space somewhere. They are objects,
 *       not just some global "pen swithes". /vp
 *
 *	 Tjl: see above for how to handle ColorThing - that way it doesn't
 *	 have coordinates.
 *	
 *	 One big question is: *DO* we really want actual "models" or just 
 *       drawing things?
 *	 Both the layer above and the layer below are about drawing with a 
 *       pen - why have a layer with models in between?
 *
 *       vp: As long as we already have objects why don't use them. 
 *       It gives much more flexibility when trying to do more complex
 *       things. BTW, What is the layer above that is about drawing with a
 *       pen?
 *       If we would strip the awt from zzcanvas, and added a simple way
 *       to use any ZZGraphics, then this wouldn't be an issue: you could
 *       do it the way you wanted in ZZCanvas. And that is the way I think
 *       it should be done.
 *
 * 
 *  <li> Basically, check out how ZZCanvas does things and do the same.
 *       Actually, we might want to think about separating the class
 *	 structure in a way as to have an abstract base class for graphic
 *	 beams and then two implementations, one for opengl and the other
 *	 for awt. Or even have things implement ZZOpenGLThing interface.
 *	 ZZCanvas is pretty abstract.
 *       
 *       I don't think so, ZZCanvas is way
 *       too tightly connected to awt. If one could bring his own graphics,
 *       with some basic methods, drawCell, drawLink etc., then it would
 *       be good. In fact that is the way I think it should be done. /vp
 *        
 *	 Tjl: Explain further. The connection to awt is actually limited to 
 *       the methods render*.
 *       
 *       vp: Exactly. We should have ZZCanvas(zzgraphics g), so we could use
 *       zzcanvas with any graphics we would like to implement. All rendering
 *       would go through that zzgraphics. So every render method
 *       should have something like g.drawLine, where g is zzgraphics not
 *       graphics.
 *       
 *  <li> if you extend Component, do you need addKeyListener and 
 *       addMouseListener? 
 *
 *       If I want to throw my own keyevents, yes./vp
 *
 *	 they are already implemented there. 
 *
 *       Is there somewhere a method to fire your own key & mouse events? /vp
 *
 * 	 Alternatively, not deriving from Component might be good.
 *
 *       I know, I  should have added a Component and used that to fire
 *       those events... /vp
 *
 *	java.awt.Component.processEvent(KeyEvent) /vp
 *
 * </ul>
 */

public class ZZOpenGLGraphics extends Component implements ZZGraphics, Runnable {
    String rcsid = "$Id: ZZOpenGLGraphics.java,v 1.9 2000/11/05 12:49:39 tjl Exp $";

    public static final boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }


    KeyListener actionListener = null;

    public void addKeyListener(KeyListener l) {
	actionListener = AWTEventMulticaster.add(actionListener, l);
    }
    public void removeKeyListener(KeyListener l) {
	actionListener = AWTEventMulticaster.remove(actionListener, l);
    }
    public void processKeyEvent(KeyEvent e) {
	
	if (actionListener != null) {
	    actionListener.keyPressed(e);
	}         
    }

    MouseListener mouseListener = null;

    public void addMouseListener(MouseListener l) {
	mouseListener = AWTEventMulticaster.add(mouseListener, l);
    }
    
    public void removeMouseListener(MouseListener l) {
	mouseListener = AWTEventMulticaster.remove(mouseListener, l);
    }
    
    
    public void processMouseEvent(MouseEvent e) {
	
	if (mouseListener != null){
	    mouseListener.mouseClicked(e);
	}         
    }
    
    int GLcontext = 0;
    int window    = 0;
    int display   = 0;
    
    int font_Size = 10;

    /* just testing */
    int cx = 0;
    int cy = 0;
    int cz = 50;

    
    
    Vector things = new Vector();
    
    
    public abstract class OGLThing {
	
	int x;
	int y;
	
	OGLThing(int x, int y ){
	    this.x = x;
	    this.y = y;
	}
	
	OGLThing( ){
	    x = 0;
	    y = 0;
	}
	
	public void coords(int x, int y) {
	    this.x = x;
	    this.y = y;
	}
	
	public int getX() { return x; }
	
	public int getYs() { return y; }
	
	// rendes to this context
	public abstract void render(); 
	
    }
    
    public class OGLColorThing extends OGLThing{
	
	float r;
	float g;
	float b;
	float a;
	
	OGLColorThing(int x, int y, float r, float g, float b, float a){
	    super(x,y);
	    this.r = r;
	    this.g = g;
	    this.b = b;
	    this.a = a;
	}
	
	OGLColorThing(){
	    super();
	    this.r = 1;
	    this.g = 1;
	    this.b = 1;
	    this.a = 1;

	}
	
	OGLColorThing(int x, int y){
	    super(x,y);
	    this.r = 1;
	    this.g = 1;
	    this.b = 1;
	    this.a = 1;
	    
	}

	OGLColorThing(Color c){
	    super(0,0);
	    setColor(c);
	    
	}

	public void setColor(Color c){
	    
	}
	
	public void setColor(int r, int g, int b, int a){
	    this.r = r;
	    this.g = g;
	    this.b = b;
	    this.a = a;
	    
	}

	public void render(){
	    p("COLOR: RENDER\n");
	    /* this is just a temporary solution,
	       add checking of color */
	    drawingColor(r,g,b,a);
	}
    }
    
    public abstract class OGLCell extends OGLColorThing{
	
	ZZCell c;
	
	public OGLCell(){ 
	    super();
	    c = null;
	}
    
	public OGLCell(int x, int y, ZZCell c){ 
	    super(x,y);
	    this.c = c;
	}
	public OGLCell( ZZCell c ){ 
	    super();
	    this.c = c;
	}
	
    }

    public class OGL2DCell extends OGLCell {
	
	int width;
	int height;
	
	
	
	public OGL2DCell(){ 
	    super();
	    width = 0;
	    height = 0;
	}
	
	public OGL2DCell(int x, int y, int width, int height, ZZCell cell){ 
	    super(x,y,cell);
	    this.width = width;
	    this.height = height;
	    
	}
	
	public void render(){

	    super.render();
	    
	    p("CELL: RENDER" + x + " " +y);
	    
	    drawRect(x,y,width,height);
	    
	    if( c != null ) {
		drawingColor(0,0,0,1);
		
		drawText(x+2, y+2, c.getText(),10); 
	    }
	}
    }    

    public class OGLText extends OGLColorThing{
	int size;
	String text;
	
	public OGLText(int x, int y, String txt){
	    super(x,y);
	    text = txt;
	    size = 10;
	}
	
	public void render(){
	    //super.render();
	    drawText(x, y, text, size );
	}
    }
    
    public class OGLLink extends OGLColorThing{

	int x2;
	int y2;

	OGLLink(int x1, int y1, int x2, int y2){
	    super(x1,y1);
	    this.x2 = x2;	   
	    this.y2 = y2;	   
	}
	
	public void render(){
	    super.render();
	    drawLine(super.x,super.y, x2, y2);
	}
	
    }
    
    public class OGLLine extends OGLColorThing{
	int x2;
	int y2;
	
	OGLLine(int x1, int y1, int x2, int y2){
	    super(x1,y1);
	    this.x2 = x2;	   
	    this.y2 = y2;	   
	}
	
	public void render(){
	    super.render();
	    drawLine(super.x,super.y, x2, y2);
	}
	
    }
    
    public class OGLMark extends OGLColorThing{
	
	ZZCell c;
	
	int h;
	
	OGLMark(int x1, int y1, int h, ZZCell c){
	    super(x1,y1);
	    this.c = c;
	    this.h = h;
	}
	
	public void render(){
	    super.render();
	    drawLine(super.x,super.y, x, y+h);
	}
	
    }
    
    
    class OGLArrow extends OGLColorThing{
	// Hmm...
	int dir_x;
	int dir_y;
	int dir_z;
	
	public OGLArrow(int x, int y){
	    super(x,y);
	    dir_x = 2;
	    dir_y = 2;
	    dir_z = 1;
	}
	
	public void render(){
	    super.render();
	    drawArrow(super.x,super.y, dir_x, dir_y, dir_z);
	}
    }
    
    public Dimension getSize(){
	return null;
    }
    
    public Dimension getDefaultCellSize(ZZCell c){
	return null;
    }
    
    public void putCell(ZZCell c, int x, int y, int w, int h, String style){
	things.addElement(new OGL2DCell(x,y,w,h,c));
    }
    
    public void putLinks(String dim, float dx, float dy){
	
    }
    
//     public void putBeams(ZZCanvasTextSpanSet set1, ZZCanvasTextSpanSet set2){
// 	
//     }

    public void setColor(Color c){}
    
    public int getDefaultFontSize(){ return 0;}
    
    public FontMetrics getFontMetrics(Font f){ return null;}

    public void setFont(Font f){}
     
    public void putPolyLine(int x1, int y1, int x2, int y2, String style){}
    
    public void putText(String s, int x, int y){
	things.addElement(new OGLText(x,y,s));
    }
  
    public void putText(String s, int x, int y, int size){
	things.addElement(new OGLText(x,y,s));
    }
    /*
    public void putText(String s, int x, int y, int z){
	things.addElement(new OGLText(x,y,z,s));
    }
    */
    
    public Object putSpan(Span s, String str, ZZCell c, int x, int y,
			  int w, int h, int xoffs, int yoffs,
			  ZZCanvasTextSpanSet collect,
			  Object prev,
			  Font f, FontMetrics fm){ 
	return null;}
    
    public void putTextMark(int x, int y, int h, ZZCell mkc){
	things.addElement(new OGLMark(x,y,h, mkc));
    }
    
    public void putLine(int x1, int y1, int x2, int y2, String style){
	//drawLine(x1, y1, x2, y2);
	things.addElement(new OGLLine(x1,y1,x2,y2));
	
    }
    
    public void putCoordNames(String[] names){}
    
    public void putArrow( int x1, int y1, 
			  int x2, int y2, 
			  Color intern, String style){
	things.addElement(new OGLArrow(x1,y1));
    }
    
//    public void putDragCursor(ZZGraphics.DragCursor dc, int x, int y){}
    
 //   public class OpenGLDragCursor implements ZZGraphics.DragCursor {
//	public boolean accept(Object o){ return false; }
 //   }
    
    static {
        System.loadLibrary("zz3d");
    }
    
    public void render(){
	p("RENDER:");
	
	for (Enumeration e = things.elements() ; e.hasMoreElements() ;) {
	    ((OGLThing)e.nextElement()).render();
	}
	
	swapBuffers();
	
    }
    
    private Thread thread = null;
    
    public void start() {
	if (thread == null) {
	    thread = new Thread(this, "render");
	    thread.start();
	}
    }
    
    
    public void run() {
	
	Thread myThread = Thread.currentThread();
	p("RUN:");
	
	while (thread == myThread) {
	    checkEvents();
	    render();
	    try {
		Thread.sleep(100);
	    } catch (InterruptedException e){
		// the VM doesn't want us to sleep anymore,
		// so get back to work
	    }
	}
    }
    
    void addKeyEvent(int key, int type){
	p("KEY_EVENT: " + (char)key + " " + type);
	// XXX add missing info: time, modifiers ...
	processKeyEvent(new KeyEvent(this,1000,1,0,key,(char)key));
    }
    
    void addButtonEvent(int button, int x, int y, int modifiers){
	p("BUTTON_EVENT: " + button + " " + x + y);
	processMouseEvent(new MouseEvent(this,1001,1,0,x,y,1,false));
    }
    
    void configureEvent(int x, int y){
	p("CONFIGURE_EVENT:" + x + " " + y); 
    }
    
    /******************/
    /* NATIVE METHODS */
    /******************/
    
    /**
     * This is supposed to create a native window, put right values
     * to variales in this class, create glx context...
     * 
     */
    public native void openWindow( int width, int height );
    
    public native void drawLine( int x1, int y1, int x2, int y2 );
  
    public native void draw3DLine( int x1, int y1, int z1, 
				   int x2, int y2, int z2 );
  
    public native void drawRect( int x, int y, int width, int height );
    
    public native void draw2DPoint( int x, int y );
    
    public native void draw3DPoint( int x, int y, int z );

    public native void drawText( int x, int y, String text, int size  );
    
    public native void draw3DText( int x, int y, int z, 
				   String text, int size  );
    
    public native void drawingColor( double r, double g, double b, double a );
    
    public native void drawArrow(int x, int y, 
				 int dir_x, int dir_y, int dir_z);    
    
    public native void draw3DArrow(int x, int y, int z,
				   int dir_x, int dir_y, int dir_z);    

    public native void swapBuffers();

    public native void checkEvents();

    public native void setCamera( int x,  int y,  int z, 
				  int ex, int ey, int ez, 
				  int lx, int ly, int lz );
}

