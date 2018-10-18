/****************************************************
 * INCLUDES
 ****************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <wchar.h>
// #include <iconv.h>
#include <math.h>

extern "C"{
#include "jni.h"
}
#include "fi_iki_lukka_ZZOpenGLGraphics.h"

#include "gltt/FTEngine.h"
#include "gltt/FTFace.h"
#include "gltt/FTInstance.h"
#include "gltt/FTGlyph.h"
#include "gltt/FTGlyphBitmap.h"
#include "gltt/FTGlyphPixmap.h"
#include "gltt/FTBitmapFont.h"
#include "gltt/GLTTBitmapFont.h"
#include "gltt/GLTTPixmapFont.h"
#include "gltt/GLTTOutlineFont.h"
#include "gltt/GLTTFont.h"
#include "gltt/GLTTGlyphPolygonizer.h"
#include "gltt/GLTTGlyphTriangulator.h"



#include <GL/glx.h>
#include <GL/glu.h>
#include <GL/glut.h>
#include <X11/Xatom.h>
#include <X11/Xmu/StdCmap.h>
#include <X11/keysym.h>



/***********************************************
 * useful little macros
 ************************************************/
#define GET_THIS() jclass zzthis = env->GetObjectClass( obj );
#define FIELD_ID(_name, _sig) env->GetFieldID( zzthis, _name, _sig)
#define SET_INT_FIELD(_name, _value) \
                       env->SetIntField( obj, FIELD_ID(_name, "I"), _value)
#define GET_INT_FIELD(_name) env->GetIntField( obj, FIELD_ID(_name, "I"))

/******************************************************
 * GLOBALS
 ******************************************************/
/* these will be in java class */
Display *disp;
Window   win;
/***/
GLboolean db = GL_TRUE; /* doublebuffering */
GLint disp_list  = 0; 
XSizeHints sizeHints = { 0 };
XEvent event;

GLuint base;

int args[] = { GLX_DOUBLEBUFFER,
               GLX_RGBA, 
               GLX_DEPTH_SIZE, 
               16, 
               None };

void makeRasterFont(Display *dpy)
{
    XFontStruct *fontInfo;
    Font id;
    unsigned int first, last;
    fontInfo = XLoadQueryFont(dpy, 
        "-*-courier-medium-r-normal-*-*-100-*-*-m-*-iso8859-1");
    

    if (fontInfo == NULL) {
        printf ("no font found\n");
        exit (0);
    }
    
    id = fontInfo->fid;
    first = fontInfo->min_char_or_byte2;
    last = fontInfo->max_char_or_byte2;
    
    base = glGenLists(last+1);
    
    if (base == 0) {
        printf ("out of display lists\n");
    
	exit (0);
    }
    
    glXUseXFont(id, first, last-first+1, base+first);

}

/******************************************************
 * printString
 ******************************************************/
void printString(const char *s)
{
    glListBase(base);
    glCallLists(strlen(s), GL_UNSIGNED_BYTE, (unsigned char *)s);
}




/******************************************************
 * getColormap
 ******************************************************/
Colormap getColormap ( XVisualInfo *vis){
  Status stat;
  XStandardColormap *sCm;
  Colormap cm;
  int i;
  int numCmaps;
  
  if ( vis->c_class != TrueColor ){
    printf ("No support for TrueColor!");
    exit(1);
  }
  
  stat = XmuLookupStandardColormap(disp, vis->screen, vis->visualid, 
				   vis->depth,XA_RGB_DEFAULT_MAP, False, True);
  if (stat == 1) {
    stat = XGetRGBColormaps( disp, RootWindow(disp, vis->screen), 
                             &sCm, &numCmaps,
                             XA_RGB_DEFAULT_MAP);
    if (stat == 1) 
      for( i = 0; i < numCmaps; i++ )
        if( sCm[i].visualid == vis->visualid) {
          cm = sCm[i].colormap;
          XFree(sCm);
          return cm;
        }
  }

  cm = XCreateColormap (disp, RootWindow(disp,vis->screen),
                        vis->visual, AllocNone);
  return cm;
}


/******************************************************
 * openWindow
 ******************************************************/
JNIEXPORT void JNICALL 
Java_fi_iki_lukka_ZZOpenGLGraphics_openWindow( JNIEnv *env, 
                                      jobject obj, 
                                      jint width, 
                                      jint height) 
{
  
  XVisualInfo *vi;
  Colormap cm;
  XSetWindowAttributes xswa;
  XWMHints *wmHints;
  Atom wmDeleteWindow;
  GLXContext cx;
  char *display = NULL;

    
  /** connect to X server **/
  disp = XOpenDisplay(display);
  // FIX ME
  if ( disp == NULL ) exit(1);
  printf("__C__: openWindow\n");
  /** check for glx */
  if ( ! glXQueryExtension(disp, NULL, NULL))
    // maybe should use some constants 
    return;// 2;
  
  /** visual & colormap **/
  vi = glXChooseVisual(disp, DefaultScreen(disp),args );
  if ( ! vi ){ 
    // no doublebuffering
    vi = glXChooseVisual(disp, DefaultScreen(disp),&args[1] );
    if ( ! vi ) exit(1);
    db = GL_FALSE;
  }
  
  cm = getColormap(vi);
  
  /** GL rendering context **/
  cx = glXCreateContext(disp, vi, NULL, GL_TRUE);
  if ( cx == NULL )
    exit(1);
  
  /* some hints etc */
  xswa.colormap = cm;
  xswa.border_pixel = 0;
  xswa.event_mask = ExposureMask|ButtonPressMask|KeyPressMask;
  sizeHints.width = width;
  sizeHints.height = height;
  sizeHints.x = 10;
  sizeHints.y = 10;
  
  win = XCreateWindow(disp, RootWindow(disp,vi->screen), 
                      sizeHints.x, sizeHints.y, 
                      width, height, 0, vi->depth, InputOutput, 
                      vi->visual, CWEventMask|CWBorderPixel|CWColormap, &xswa);
  
  wmDeleteWindow = XInternAtom(disp, "WM_DELETE_WINDOW", False);
  XSetWMProtocols(disp, win, &wmDeleteWindow,1);
  XSetStandardProperties(disp, win, "test", "Testi", 
                         None, None, None, &sizeHints);
  
  wmHints = XAllocWMHints();
  wmHints->initial_state = NormalState;
  wmHints->flags = StateHint;
  XSetWMHints(disp, win, wmHints);
  
  /* bind context to window */
  glXMakeCurrent(disp, win, cx);
  
  glEnable       ( GL_DEPTH_TEST   );
  glEnable       ( GL_ALPHA_TEST   );
  glDisable      ( GL_DITHER       );
  glDisable      ( GL_LIGHTING     );
  glDisable      ( GL_FOG          );
  
  glDisable      ( GL_TEXTURE_2D   );
  
  glDisable      ( GL_CULL_FACE    );
  glDisable      ( GL_SCISSOR_TEST );
  glDisable      ( GL_STENCIL_TEST );
  
  glViewport(0, 0, width, height);
  
  glMatrixMode(GL_PROJECTION);
  
  glLoadIdentity();
  
  gluPerspective(65, (GLfloat) width
                 / (GLfloat) height, 1, 1000);
  
  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  
  gluLookAt(0, 0, 100, 0, 0, 0, 0, 1, 0);  

  glMatrixMode(GL_MODELVIEW);
  
  XMapWindow(disp,win);
  
  glXSwapBuffers(disp, win);
  
  makeRasterFont(disp);
  
}

/******************************************************
 * drawLine
 ******************************************************/
JNIEXPORT void JNICALL 
Java_fi_iki_lukka_ZZOpenGLGraphics_drawLine(JNIEnv *env, jobject obj, 
					    jint x1, jint y1, 
					    jint x2, jint y2){
    printf("__C__: drawLine\n");
    glBegin(GL_LINES);
    
    glVertex3d( x1, y1, 0 );
    glVertex3d( x2, y2, 0 );
  
    glEnd();
    
}


JNIEXPORT void JNICALL 
Java_fi_iki_lukka_ZZOpenGLGraphics_drawRect(JNIEnv *env, jobject obj,
					    jint x, jint y, 
					    jint width, jint height){
    
    printf("__C__: drawRect\n");
    glBegin(GL_QUADS);
    
    glVertex3d( x, y, 0 );
    glVertex3d( x, y+height, 0 );
    glVertex3d( x+width, y+height, 0 );
    glVertex3d( x+width, y, 0 );
    glEnd();
    
}


JNIEXPORT void JNICALL 
Java_fi_iki_lukka_ZZOpenGLGraphics_drawPoint(JNIEnv *env, jobject obj,
					     jint x, jint y);


JNIEXPORT void JNICALL 
Java_fi_iki_lukka_ZZOpenGLGraphics_drawText(JNIEnv *env, jobject obj,
					    jint x, jint y, 
					    jstring text, jint size){
    int s;
    //int l;
    const char *str = (const char *)env->GetStringUTFChars( text, 0 );
    
    printf("str = %s\n", str);
    
    s = env->GetStringLength( text);
    
    // Check the size
    printf("s = %d\n", s);
    glLoadIdentity();
    
    /*
    //
    // glut stroke fonts
    glTranslatef(x,y,0);
    glScalef(0.03,0.03,0.03);
    

    
    for(l = 0; l < s; l++){
	glutStrokeCharacter(GLUT_STROKE_ROMAN , str[l]);
	glTranslatef(glutStrokeWidth(GLUT_STROKE_ROMAN,str[l]),0,0);
    }
    */
    printf("__C__: drawText\n");
    //glColor3f(1,1,1);

    glLoadIdentity();    
    
    FTFace face;
    if( ! face.open("/dos/winnt/Fonts/couri.ttf") ){
      printf("unable to open ttf file");
      return ;
    }
    
    GLTTOutlineFont font(&face);
    
    int point_size= size;
    
    if( ! font.create(point_size) ){
      printf("unable to create outline font");
      return ;
    }

    glTranslatef(x,y,0);
    //glRotatef(45,0,1,0);
    //font.setPrecision(4);
    font.output(  str );

    // bitmap fonts
    //printString(str);
    
    glLoadIdentity();
    env->ReleaseStringUTFChars( text, 0);
}


JNIEXPORT void JNICALL 
Java_fi_iki_lukka_ZZOpenGLGraphics_drawingColor(JNIEnv *env, jobject obj, 
						jdouble r, jdouble g,
						jdouble b, jdouble a){
    glColor4d(r,g,b,a);
}


JNIEXPORT void JNICALL 
Java_fi_iki_lukka_ZZOpenGLGraphics_drawArrow(JNIEnv *env, jobject obj,
					     jint x, jint y,
					     jint dx, jint dy, 
					     jint dz){
    glLoadIdentity();
    glScalef(dx,dy,dz);
    glBegin(GL_LINES);
    
    glVertex3f(x,y,0);
    glVertex3f(x,y-1,0);
    
    glVertex3f(x,y,0);
    glVertex3f(x+2,y-2,0);

    glVertex3f(x,y,0);
    glVertex3f(x+1,y,0);
    
    glEnd();
    glLoadIdentity();
}

/******************************************************
 * swapBuffers
 ******************************************************/
JNIEXPORT void JNICALL 
Java_fi_iki_lukka_ZZOpenGLGraphics_swapBuffers(JNIEnv *env, 
					       jobject obj){
  printf("__C__: swapBuffers\n");
  
  glXSwapBuffers(disp,win);
  

  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  
  
  glMatrixMode(GL_MODELVIEW);
  
}


/******************************************************
 * checkEvents
 ******************************************************/
JNIEXPORT void JNICALL 
Java_fi_iki_lukka_ZZOpenGLGraphics_checkEvents( JNIEnv *env, jobject obj){
    
    XKeyEvent *e_key;
    
    XButtonEvent *e_but;
    
    XConfigureEvent *e_conf;
    
    jclass cls = env->GetObjectClass( obj );
    
    jmethodID key_id    = env->GetMethodID( cls,
					    "addKeyEvent", "(II)V");
    
    jmethodID button_id = env->GetMethodID( cls, 
					    "addButtonEvent", "(IIII)V");
        
    jmethodID conf_id = env->GetMethodID( cls, 
					       "configureEvent", "(II)V");
    
    printf("__C__: Check events\n");
    
    if ( button_id == 0 || key_id == 0 ) {
	printf ("no method id\n");
	return ;
    } 
    
    do{

	int ch;
	int type = 0;
	
	XEvent event; 
	
	XNextEvent(disp, &event);
	
	switch ( event.type ){
	case ConfigureNotify:
	    printf("ConfNotify");

	    env->CallVoidMethod( obj, conf_id,
				    e_conf->width, e_conf->height);
	    break;
	case ButtonPress:

	    e_but = (XButtonEvent *) &event;
	    env->CallVoidMethod( obj, button_id, 
				    e_but->button, e_but->x, e_but->y, 0);
	    break;
	case KeyPress:

	    e_key = (XKeyEvent *) &event;
	    ch  = (int )XLookupKeysym(e_key,0);
	    
	    env->CallVoidMethod( obj, key_id, ch, type);
	    break;
	default:

	  ;
	}
	
    } while( XPending( disp ) );
   
    return ;
}
