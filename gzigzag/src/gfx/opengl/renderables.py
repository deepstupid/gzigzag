import string

def nurbsparams():
    r = ""
    for num in [1, 2]:
	for dir in ["left", "right"]:
	    r = r + "p" +repr(num)+"_"+dir+".x = x" +repr(num)+dir[0]+";"
	    r = r + "p" +repr(num)+"_"+dir+".y = y" +repr(num)+dir[0]+";"
	    r = r + "p" +repr(num)+"_"+dir+"0.x = x" +repr(num)+dir[0]+"i;"
	    r = r + "p" +repr(num)+"_"+dir+"0.y = y" +repr(num)+dir[0]+"i;"
    return r

idded = [
    "Image", "TexRect", "Font", "Window", "ShaderRect"
]

convert = {
    "String" : {
	"JNI" : "jstring",
	"CXX" : "std::string",
	"pre" : (lambda v, p : "const char *utf_"+p\
	    +" = env->GetStringUTFChars("+p \
	    +", 0); std::string "+v+"(utf_"+p+");  \
	    env->ReleaseStringUTFChars("+p+", utf_"+p+");\n"),
    },
    "Font" : {
	"JNI" : "jint",
	"CXX" : "TextRenderer *",
	"pre" : (lambda v, p : "TextRenderer *"+v+" = " \
		    " fonts["+p+"]->rend;")
    },
    "TexRect" : {
	"JNI" : "jint",
	"CXX" : "TexRect",
	"pre" : (lambda v, p : "TexRect "+v+" = " \
		    " imagetiles.get("+p+")->getRect();")
    },
    "ShaderRect" : {
	"JNI" : "jint",
	"CXX" : "ShaderRect",
	"pre" : (lambda v, p : "ShaderRect "+v+" = " \
		    " *shaderrects.get("+p+");")
    },
}

rs = [
{
    "Type" : "0",
    "Name" : "ClearBgModes",
    "Data" : """GLfloat clearColor[4];
	float fogDensity;
	float fogStart;
	float fogEnd;
	""",
    "Params" : "float r, float g, float b, float a, float fogd, float fogs, float foge",
    "ParamCode" : """
		    clearColor[0] = r;
		    clearColor[1] = g;
		    clearColor[2] = b;
		    clearColor[3] = a;
		    fogDensity = fogd; 
		    fogStart = fogs;
		    fogEnd = foge;
		""",
    "RenderCode" : """
	    glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);

	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	glColor3f(1, 1, 1);
	glEnable(GL_TEXTURE_2D);
	//glEnable(GL_BLEND);
	glEnable(GL_DEPTH_TEST);

	glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    	glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glEnable(GL_ALPHA_TEST);
	glAlphaFunc(GL_GREATER, 0.1);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	glFogi(GL_FOG_MODE, GL_EXP); 
	glFogfv(GL_FOG_COLOR, clearColor);
	glFogf(GL_FOG_DENSITY, fogDensity);   
	// glHint(GL_FOG_HINT, GL_NICEST/*GL_DONT_CARE*/);
	glFogf(GL_FOG_START, fogStart);
	glFogf(GL_FOG_END, fogEnd);
	glEnable(GL_FOG);
    """,

},
{
    "Type" : "0",
    "Name" : "Color",
    "Data" : "GLfloat c[4];",
    "Params" : "float r, float g, float b, float a",
    "ParamCode" : """
		    c[0] = r;
		    c[1] = g;
		    c[2] = b;
		    c[3] = a;
		""",
    "RenderCode" : """
	    glColor4fv(c);
	    """,
},

{
    "Type" : "2",
    "Name": "SmoothConnector",
    "Data" : """Pt p1_left, p1_right, p1_left0, p1_right0,
		p2_left, p2_right, p2_left0, p2_right0;
	    TexRect start, end;
	    float startb, endb;
	    """,
    "Params" : """
	    TexRect imgstart, float startb,
	    TexRect imgend, float endb,			       
	    float x1l, float y1l, float x1r, float y1r,
	    float x1li, float y1li, float x1ri, float y1ri,
	    float x2l, float y2l, float x2r, float y2r,
	    float x2li, float y2li, float x2ri, float y2ri
	    """,
    "ParamCode" : nurbsparams() + """
	this->startb = startb;
	this->endb = endb;
	this->start = imgstart;
	this->end = imgend;
	""",
    "ExtraClass" : """

	struct Side {
	    ZVec rot;
	    ZPt l, li, r, ri;
	    ZPt mid;

	    ZPt ol, or__;
	    ZPt omid;
	    float length;
	    Side(const ZPt &l, const ZPt &li, const ZPt &r, const ZPt &ri);
	    void setMag(double f);
	    void drawNurbs(Gummi::Tex::Rect &rect, float txtb,
			ZPt &ml, ZPt &mr, ZVec &mrotl, ZVec &mrotr) ;
	};
	void renderImpl(Side &s1, Side &s2);


    """,
    "RenderCode" : """
	    Coords &c1 = coords1;
	    Coords &c2 = coords2;
	    Side s1(c1.transform(p1_left), c1.transform(p1_left0),
		    c1.transform(p1_right), c1.transform(p1_right0));
	    Side s2(c2.transform(p2_left), c2.transform(p2_left0), 
		    c2.transform(p2_right), c2.transform(p2_right0));
	    renderImpl(s1, s2);
	    """
},

{
    "Type" : "1",
    "Name": "TexturedQuad",
    "Data": "Pt corners[4]; TexRect rect; float eps;",
    "Params" : """
	    TexRect img, float x0, float y0, float x1, float y1, float eps
	""",
    "ParamCode" : """
	    rect = img;
	    this->eps = eps;
	    corners[0].x = corners[1].x = x0;
	    corners[0].y = corners[3].y = y0;
	    corners[2].x = corners[3].x = x1;
	    corners[1].y = corners[2].y = y1;
	""",
    "RenderCode" : """
	    rect.tex.bind();

	    glBegin(GL_QUADS);

	    rect.texcoord(0+eps, 0+eps);
	    coords1.vertex(corners[0]);

	    rect.texcoord(0+eps, 1-eps);
	    coords1.vertex(corners[1]);

	    rect.texcoord(1-eps, 1-eps);
	    coords1.vertex(corners[2]);

	    rect.texcoord(1-eps, 0+eps);
	    coords1.vertex(corners[3]);
	    
	    glEnd();
	    GLERR
	""",
}    ,

{
    "Type" : "1",
    "Name": "ShaderQuad",
    "Data": "Pt corners[4]; ShaderRect rect; float offs;",
    "Params" : """
	    ShaderRect img, float x0, float y0, float x1, float y1, float offs
	""",
    "ParamCode" : """
	    rect = img;
	    this->offs = offs;
	    corners[0].x = corners[1].x = x0;
	    corners[0].y = corners[3].y = y0;
	    corners[2].x = corners[3].x = x1;
	    corners[1].y = corners[2].y = y1;
	""",
    "RenderCode" : """

	int c;

#define SHCONST(a) { \\
    int tmp; \\
    glGetIntegerv(GL_ACTIVE_TEXTURE, &tmp); \\
    glActiveTexture(GL_TEXTURE0); \\
    glGetTexEnviv(GL_TEXTURE_SHADER_NV, GL_SHADER_CONSISTENT_NV, &c); \\
    cout << "SCONSISTENT0: "<<a<<" "<<c<<"\n"; \\
    glActiveTexture(GL_TEXTURE1); \\
    glGetTexEnviv(GL_TEXTURE_SHADER_NV, GL_SHADER_CONSISTENT_NV, &c); \\
    cout << "SCONSISTENT1: "<<a<<" "<<c<<"\n"; \\
    glActiveTexture(GL_TEXTURE2); \\
    glGetTexEnviv(GL_TEXTURE_SHADER_NV, GL_SHADER_CONSISTENT_NV, &c); \\
    cout << "SCONSISTENT2: "<<a<<" "<<c<<"\n"; \\
    glActiveTexture(GL_TEXTURE3); \\
    glGetTexEnviv(GL_TEXTURE_SHADER_NV, GL_SHADER_CONSISTENT_NV, &c); \\
    cout << "SCONSISTENT3: "<<a<<" "<<c<<"\n"; \\
    glActiveTexture(tmp); \\
    }


	//cout << "SHADER DISPLAY LIST " << rect.listname << " START\\n";	
        glCallList(rect.listname);	
	//cout << "SHADER DISPLAY LIST END\\n";	
	//SHCONST("");
	
	// glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	//
	// glEnablgldise(GL_TEXTURE_3D);

	//	glPushMatrix();

	glColor3f(1, 1, 1);

	GLdouble v[4][4];
	GLdouble v2[4][4];
	GLdouble v3[4][4];
	GLfloat col[4][3];

	rect.getcoords(v, v2, v3, col, offs);

	//glTranslatef(200, 200, 0);

	glBegin(GL_QUAD_STRIP);
	GLERR

	glColor3fv(col[0]);
	GLERR
	glTexCoord4dv(v[0]);
	GLERR
	glMultiTexCoord2d(3, 0, 0);
	GLERR
	glMultiTexCoord4dv(1, v3[0]);
	GLERR
	glMultiTexCoord4dv(2, v2[0]);
	GLERR
	coords1.vertex(corners[0]);
	GLERR
	//glVertex3f(0, 0, 0);

	glColor3fv(col[1]);
	GLERR
	glTexCoord4dv(v[1]);
	GLERR
	glMultiTexCoord2d(3, 0, 1);
	GLERR
	glMultiTexCoord4dv(1, v3[1]);
	GLERR
	glMultiTexCoord4dv(2, v2[1]);
	GLERR
	coords1.vertex(corners[1]);
	GLERR
	//glVertex3f(0, 500, 0);

	glColor3fv(col[2]);
	GLERR
	glTexCoord4dv(v[2]);
	GLERR
	glMultiTexCoord2d(3, 1, 0);
	GLERR
	glMultiTexCoord4dv(1, v3[2]);
	GLERR
	glMultiTexCoord4dv(2, v2[2]);
	GLERR
	coords1.vertex(corners[3]);
	GLERR
	//glVertex3f(500, 0, 0);

	glColor3fv(col[3]);
	GLERR
	glTexCoord4dv(v[3]);
	GLERR
	glMultiTexCoord2d(3, 1, 1);
	GLERR
	glMultiTexCoord4dv(1, v3[3]);
	GLERR
	glMultiTexCoord4dv(2, v2[3]);
	GLERR
	coords1.vertex(corners[2]);
	GLERR
	//glVertex3f(500, 500, 0);

	glEnd();
	//GLERR //XXX: This is still failing

	//glPopMatrix();

	// glAccum(GL_ACCUM, 1.0/8);


	 //offs += 0.01;

	glCallList(rect.listname+1);
	""",
}    ,

{
    "Type": "1",
    "Name": "HorizText",
    "Data": """
	TextRenderer *r;
	std::string txt;
	ZPt origin;
	""",
    "Params" : """
	Font f,
	String text, int x, int y, float z
	""",
    "ParamCode" : """
	r = f;
	txt = text; origin.x = x; origin.y = y; origin.z = z;
	""",
    "ExtraClass" : """
	// template<class Coords> struct Vertexer {
	template<class Coords> struct Vertexer {
	    const Coords &c;
	    float z;
	    Vertexer(Coords &c, float z) : c(c), z(z) { }
	    template<class T> void operator()(const T &x, const T &y) {
		ZPt tmp(x, y, z);
		c.vertex(tmp);
	    }
	};
	""",
    "RenderCode" : """
	    Vertexer<Coords> v(coords1, origin.z);
	    Gummi::Font::renderIter(*r, txt.begin(), txt.end(), 
		    (int)origin.x, (int)origin.y, 
		    v
		    );
			    
    """	
}, 
]

def iddedParam(p):
    if p[0] in idded : return p[1] + ".getId()"
    return p[1];
	

def idintParam(p):
    if p[0] in idded : return "int "+p[1]
    return p[0]+" "+p[1];

def jniparam(p):
    if convert.has_key(p[0]) :
	return convert[p[0]]["JNI"] + " " + p[1] + "__pre"
    if p[0] in idded : return "jint " + p[1]
    return "j"+p[0] + " " + p[1]

def jnipreproc(p):
    if convert.has_key(p[0]) :
	return convert[p[0]]["pre"](p[1], p[1]+"__pre")
    return ""

def cxxparam2(p):
    print "CXX "+p[1]
    if convert.has_key(p[0]) :
	return convert[p[0]]["CXX"] + " " + p[1]
    return p[0] + " " + p[1]

def cxxparam(p):
    c = cxxparam2(p)
    print "CXXR: " + c
    return c

cls = jni = cobj = ""

for r in rs:
    ExtraClass = None
    for k in r.keys() : locals()[k] = r[k]
    params = [param.strip().split(" ") for param in Params.split(",")]
    idparams = [param for param in params if param[0] in idded]
    
    cls += (" static public class " + Name + " extends Renderable" 
	+ Type + "JavaObject { \n" 
	+ string.join([p[0]+" "+p[1]+";\n" for p in idparams], "")  
	+ "private "+Name+"(int id " 
	+  string.join([","+p[0]+" "+p[1] for p in idparams], "")  
	+ ") { super(id); " 
	+ string.join(["this."+p[1]+"="+p[1]+";" for p in idparams],"") 
	+ "\n}\n}");

    createImpl = "create"+Name+"Impl"

    cls += (" static public "+Name+" create"+Name 
	+ "(" 
	+ Params + ") { \n return new "+Name+"("+createImpl+"("
	+ string.join([ iddedParam(p) for p in params ], ",") 
	+ ")" + string.join([","+p[1] for p in idparams],"")+");\n"
	+ "}\n")

    cls += ("static private native int "+createImpl+"(\n"
	+ string.join([ idintParam(p) for p in params ], ",")+");\n\n");

    jni += ("\n\nJNIEXPORT jint JNICALL Java_org_gzigzag_gfx_GZZGL_"
	+ createImpl+"(JNIEnv *env, jclass"
	+ string.join([", "+jniparam(p) for p in params])
	+")\n{\n")

    jni += (
	string.join([jnipreproc(p) for p in params], "\n")
	+ Name + " *obj = new "+Name+"("
	+ string.join([p[1] for p in params], ",") + ");\n\n"
	)

    jni += "return renderable"+Type+"s.add(obj);\n\n}\n\n"

    print params
    print ParamCode

    cobj += ("struct " + Name + " : public Renderable"+Type+" {\n"
	+ Name + "("
	+   string.join([cxxparam(p) for p in params], ",") + ")\n {\n"
	+   ParamCode + "\n}\n\n"
	+ Data + " \n IMPLEMENTRENDER"+Type+"\n\n" + 
	    (ExtraClass or "")+ "\n\n"
	)

    if Type == "2" :
	cobj += "template<class Coords> void renderImpl(Coords &coords1, Coords &coords2)" 
    elif Type == "1" :
	cobj += "template<class Coords> void renderImpl(Coords &coords1)" 
    else :
	cobj += "void renderImpl()" 

    if RenderCode : 
	cobj += ("{ // cout << \"Render "+Name+"\\n\";\n "+RenderCode+
	    " // cout << \"Rendered "+Name+"\\n\";\n" +
	    " } \n");
    else :
	cobj += ";\n"

    cobj += "};\n\n"

    for k in r.keys() : locals()[k] = None


print cls
print jni
print cobj

gzzgljava = """
    /*   
    GZZGL.java
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

    /** The interface to the native OpenGL library.
     * Note: here we must be VERY careful, as this is one of the places
     * where foreign code is not sandboxed automatically for us.
     *
     * All parameters that go to C level must be checked either here
     * or at the C level, otherwise -- BOOM.
     *
     */
    public class GZZGL {
	public static boolean dbg = true;
	public static void p(String s) { if(dbg) System.out.println(s); }

	private static native int init(int debug);

	public interface EventHandler {
	    void repaint();
	    /** Receive a keystroke event.
	     * @return Whether the event loop should stay looping or move to
	     * 	a non-waiting mode (false).
	     */
	    boolean keystroke(String s);
	}

	public interface Ticker {
	    boolean tick();
	}

	public static final int RENDERABLE0 = 0x0800000;
	public static final int RENDERABLE1 = 0x1000000;
	public static final int RENDERABLE2 = 0x2000000;

	/** The Java proxy for a C++ object.
	 */
	static public abstract class JavaObject extends org.gzigzag.gfx.GLVob {
	    private int id = 0;
	    JavaObject(int id) { super(null); this.id = id; }
	    public void finalize() {
		if(id != 0) throw new Error("Zero id object!");
		deleteObj();
		id = 0;
	    }
	    protected abstract void deleteObj();
	    protected int getId() { return id; }
	}

	static public abstract class NonRenderableJavaObject extends JavaObject {
	    NonRenderableJavaObject(int id) { super(id); }
	    public int addToList(int[] list, int cur, int cs1, int cs2) {
		throw new Error("Not right to try to add me to a display list");
	    }
	}

	/** The Java proxy representing a Renderable object.
	 */
	static public abstract class Renderable0JavaObject extends JavaObject {
	    public Renderable0JavaObject(int id) { super(id); }
	    /** Add this object to the given list with the given coordinate system.
	     * Usage:
	     * <pre>
	     * 	int[] list;
	     * 	int curs;
	     * 	curs = obj.addToList(list, curs, coordsys);
	     * </pre>
	     * @param list The display list to add this to.
	     * @param cur The current index, to which the first int goes
	     * @param coordsys The number of the coordinate system.
	     * @return The new current index after adding these.
	     */
	    public int addToList(int[] list, int cur) {
		list[cur++] = (RENDERABLE0 | getId());
		return cur;
	    }
	    public int addToList(int[] list, int cur, int cs1, int cs2) {
		return addToList(list, cur);
	    }
	    protected void deleteObj() {
		deleteRenderable1(getId());
	    }
	}
	static private native void deleteRenderable0(int id);


	/** The Java proxy representing a Renderable object.
	 */
	static public abstract class Renderable1JavaObject extends JavaObject {
	    public Renderable1JavaObject(int id) { super(id); }
	    /** Add this object to the given list with the given coordinate system.
	     * Usage:
	     * <pre>
	     * 	int[] list;
	     * 	int curs;
	     * 	curs = obj.addToList(list, curs, coordsys);
	     * </pre>
	     * @param list The display list to add this to.
	     * @param cur The current index, to which the first int goes
	     * @param coordsys The number of the coordinate system.
	     * @return The new current index after adding these.
	     */
	    public int addToList(int[] list, int cur, int coordsys) {
		list[cur++] = (RENDERABLE1 | getId());
		list[cur++] = coordsys;
		return cur;
	    }
	    public int addToList(int[] list, int cur, int cs1, int cs2) {
		return addToList(list, cur, cs1);
	    }
	    protected void deleteObj() {
		deleteRenderable1(getId());
	    }
	}
	static private native void deleteRenderable1(int id);

	/** The Java proxy representing a Renderable2 object.
	 */
	static public abstract class Renderable2JavaObject extends JavaObject {
	    public Renderable2JavaObject(int id) { super(id); }
	    /** Add this object to the given list with the given coordinate 
	     * systems.
	     * Usage:
	     * <pre>
	     * 	int[] list;
	     * 	int curs;
	     * 	curs = obj.addToList(list, curs, coordsys1, coordsys2);
	     * </pre>
	     * @param list The display list to add this to.
	     * @param cur The current index, to which the first int goes
	     * @param coordsys1 The number of the first system.
	     * @param coordsys2 The number of the second system.
	     * @return The new current index after adding these.
	     */
	    public int addToList(int[] list, int cur, 
				int coordsys1, int coordsys2) {
		list[cur++] = (RENDERABLE2 | getId());
		list[cur++] = coordsys1;
		list[cur++] = coordsys2;
		return cur;
	    }
	    protected void deleteObj() {
		deleteRenderable2(getId());
	    }
	}
	static private native void deleteRenderable2(int id);

    //--------- Window
	final static public class Window extends NonRenderableJavaObject {

	    private Window(int id) { super(id); }

	    protected void deleteObj() { deleteWindow(getId()); }

	    public void repaint() { GZZGL.repaintWindow(getId()); }
	}

	static public Window createWindow(int x, int y, int w, int h, EventHandler eh) {
	    return new Window(createWindowImpl(x, y, w, h, eh));
	}
	static private native int createWindowImpl(int x, int y, int w, int h, EventHandler eh);
	static private native void deleteWindow(int i);

	static private native void repaintWindow(int id);


    //--------- Image
	static public class Image extends NonRenderableJavaObject {
	    private Image(int id) { super(id); }
	    protected void deleteObj() { deleteImage(getId()); }
	}
	
	/** THIS METHOD IS A SEVERE SECURITY HOLE AND WILL BE REMOVED.
	 * Exploit: load something that the image loader library doesn't like...
	 * Need to work out how this should properly interact with mediaserver.
	 */
	static public Image createImage(String filename) {
	    return new Image(createImageImpl(filename));
	}
	static private native int createImageImpl(String filename);
	static private native void deleteImage(int i);

    //--------- TexRect
	/** A rectangular region of an image, loaded into a texture.
	 */
	static public class TexRect extends NonRenderableJavaObject {
	    private TexRect(int id) { super(id); }
	    protected void deleteObj() { deleteImage(getId()); }
	}
	
	static public TexRect createTexRect(Image img) {
	    return new TexRect(createTexRectImpl(img.getId()));
	}
	static private native int createTexRectImpl(int img);
	static private native void deleteTexRect(int i);

    //--------- ShaderRect
	/** 3D/2D shader image data, loaded into textures.
	 */
	static public class ShaderRect extends NonRenderableJavaObject {
	    private ShaderRect(int id) { super(id); }
	    protected void deleteObj() { deleteImage(getId()); }
	}
	
	static public ShaderRect createShaderRect(String turb, String col, String spots, String cell) {
	    return new ShaderRect(createShaderRectImpl(turb, col, spots, cell));
	}
	static private native int createShaderRectImpl(String turb, String col, String spots, String cell);
	static private native void deleteShaderRect(int i);

    //--------- Font
	static public class Font extends NonRenderableJavaObject{
	    private Font(int id) { super(id); }
	    protected void deleteObj() { deleteFont(getId()); }
	}
	static public Font createFont(String name, int loadPt) {
	    return new Font(createFontImpl(name, loadPt));
	}
	static private native int createFontImpl(String name, int loadPt);
	static private native void deleteFont(int id);

    //----------Misc

	public static void render(
		    Window win, int[] codes, float[] pts1, float[] pts2,
		    int numpts, float fract) {
	    renderImpl(win.getId(), codes, pts1, null, pts2, numpts, fract);
	}
	public static void render(
		    Window win, int[] codes, float[] pts1, int[] indices2,
		    float[] pts2,
		    int numpts, float fract) {
	    renderImpl(win.getId(), codes, pts1, indices2, pts2, numpts, fract);
	}
	private static native void renderImpl(
		    int window, int[] codes, float[] pts1, int[] indices2,
				    float[] pts2,
		    int numpts, float fract);

	public static void eventLoop(Ticker t) {
	    while(true) {
		p("TICK");
		boolean ret = t.tick();
		p("GOING TO EVENTLOOP");
		eventLoop(!ret);
	    }
	}

	/** Process native events.
	 * @param wait If false, this function will return once there are no more
	 * 		native events to process. If true, this function will wait
	 * 		for the next native event.
	 */
	private static native void eventLoop(boolean wait);

	static {
	    System.loadLibrary("GZZGL");
	    init(1);
	}
    """ + cls + " } \n"

open("GZZGL.java", "w").write(gzzgljava);

open("GzzGLRen.hxx", "w").write(cobj);


open("GzzGLRen-jni.cxx", "w").write(jni);

