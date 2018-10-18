#include <GL/gl.h>
#include <GL/glx.h>

#include "GzzGL.hxx"

#include </usr/include/gdk-pixbuf-1.0/gdk-pixbuf/gdk-pixbuf.h>

#include <map>


#define BARF(m) { cerr << m; abort(); }



namespace Gzz {
 namespace AbstractWin {

using std::cerr;
using std::cout;

using namespace Gummi;
typedef Tex::Raster<Tex::Format::RGBA> ImageRaster;
ImageRaster loadImage(const char *filename) ;

using std::vector;

static int doubleBufferAttributes[] = {
    GLX_RGBA,
    GLX_DOUBLEBUFFER,
    GLX_RED_SIZE, 1,
    GLX_GREEN_SIZE, 1,
    GLX_BLUE_SIZE, 1,
    GLX_DEPTH_SIZE, 1, 
    None
};

struct LXImage : public Gzz::AbstractWin::Image {
    ImageRaster r;
    LXImage(ImageRaster &r) : Gzz::AbstractWin::Image(r.getWidth(), r.getHeight()), r(r)
    { 
    }
    ~LXImage() { }
    void loadInto(Gummi::Tex::MosaicTile &tile, 
	    int x, int y, int w, int h) {
	if(x != 0 || y != 0 || w != r.getWidth() || h != r.getHeight()) throw Gzz::Problem();
	tile.loadImage(r);
    }
};

struct LXWindow;

struct LXWindowSystem : public Gzz::AbstractWin::WindowSystem {
    Display              *dpy;
    XSetWindowAttributes swa;
    int swaMask;
    int numReturned;
    XVisualInfo *vinfo;

    vector<LXWindow *> windows;
    // vector<IdleTasks *> idletasks;
    
    GLXContext context;

    typedef ::Window Win; // work around a syntax error.
    std::map<Win, LXWindow *> windowsByX;

    LXWindowSystem() {
	if(!(dpy = XOpenDisplay( NULL ))) BARF("Can't start X");


	vinfo = glXChooseVisual(dpy, DefaultScreen(dpy), doubleBufferAttributes);
	if(!vinfo) BARF("Can't get dblbuf visual");

	context = glXCreateContext(dpy, vinfo, 0, GL_TRUE);


	swa.border_pixel = 0;

	swa.colormap = XCreateColormap(dpy, DefaultRootWindow(dpy), 
			vinfo->visual, AllocNone);

	swa.background_pixmap = None;
	swa.background_pixel = 0;
	swa.event_mask = (StructureNotifyMask | 
		ButtonPressMask |
		ButtonReleaseMask | 
		ButtonMotionMask |
		KeyPressMask | 
		KeyReleaseMask |
		ExposureMask);
	// swaMask = (CWBorderPixel | CWEventMask);
	swaMask = (CWColormap|CWEventMask
		    | CWBorderPixel | 
		    CWBackPixmap 
		    );

    }


    Gzz::AbstractWin::Window *openWindow(int x, int y, int w, int h);

    Image *loadImageFile(const char *filename) {
	ImageRaster r = loadImage(filename);
	return new LXImage(r);
    }
    Image *loadImageData(const char *imageData, int len) {
	return 0;
    }
    Gummi::Font::Font *loadFont(const char *javaName, int pt) {
	Gummi::Font::Font *f = new Gummi::Font::FTFont(javaName, pt);
	return f;
    }

    void eventLoop(bool wait);

    /*
    void addIdle(IdleTasks *task) {
	idletasks.insert(idletasks.end(), task);
    }
    */

private:
    bool tryRepaint();
    
};

// static char eventStringBuf[256];

struct LXWindow : public Gzz::AbstractWin::Window {
    LXWindowSystem *ws;
    ::Window xw;
    bool needRepaint;

    LXWindow(LXWindowSystem *ws, int x, int y, int w, int h) : ws(ws), 
		needRepaint(true){
	cout << "Create win: "<<ws->vinfo->depth<<" "<<ws->vinfo->visual<<"\n";
	cout << ws->vinfo->visualid <<"\n";
	xw = XCreateWindow( ws->dpy, 
		    RootWindow(ws->dpy, ws->vinfo->screen),
		    x, y, w, h,
		    0, ws->vinfo->depth, InputOutput, 
		    ws->vinfo->visual, 
		    ws->swaMask, &ws->swa
		);

	XMapWindow( ws->dpy, xw );
	glXWaitX();
	setCurrent();
    }


    virtual void setCurrent() {
	// cout << "SetCurrent\n";
	glXMakeCurrent(ws->dpy, xw, ws->context);
    }

    virtual void swapBuffers() {
	glXSwapBuffers(ws->dpy, xw);
    }

    virtual void repaint() {
	needRepaint = true;
    }

    bool tryRepaint() {
	// cout << "TryRepaint\n";
	if(needRepaint) {
	    needRepaint = false;
	    if(eventhandler) {
		eventhandler->repaint();
		return true;
	    }
	}
	return false;
    }

    Gzz::Vec getSize() {
	// cout << "GetGeometry "<<int(ws)<<" "<<int(ws->dpy)<<" "<<int(xw)<<"\n";
	XWindowAttributes attrs;
	XGetWindowAttributes(ws->dpy, xw, &attrs);
	return Gzz::Vec(attrs.width, attrs.height);
    }

    void deliverEvent(XEvent *e) {
	if(!eventhandler) return;
	switch(e->type) {
	case KeyPress:  {
	    const char *str = XKeysymToString(XLookupKeysym(&e->xkey, 0));
	    //cout << "Sending keystroke '"<<str<<"'\n";
	    eventhandler->keystroke(str);
	    break;
	}
	case ButtonPress:
	    break;
	case Expose:
	    repaint();
	    break;
	case ConfigureNotify:
	    repaint();
	    break;
	default:
		cout << "Unknown event "<<e->type<<"\n";
	}
    }
};

Gzz::AbstractWin::Window *LXWindowSystem::openWindow(int x, int y, int w, int h) {
    LXWindow *win = new LXWindow(this, x, y, w, h);
    windows.insert(windows.end(), win);
    windowsByX[win->xw] = win;
    return win;
}

WindowSystem *WindowSystem::instance = 0;
WindowSystem *WindowSystem::getInstance() {
    if(!instance)
	instance = new LXWindowSystem();
    return instance;
}


void LXWindowSystem::eventLoop(bool wait) {
    cout << "In C++ eventloop : "<<wait<<"\n";
    // We don't want to block;
    while(1) {
	cout << "Start loop\n";
	XEvent e;
	if(tryRepaint()) {
	    if(!XEventsQueued(dpy, QueuedAfterFlush))
		continue;
	}
	/*
	for(int i=0; i<idletasks.size(); i++) {
	    donthang = (donthang || idletasks[i]->tick());
	}
	*/
	cout << "Repaint done\n";
	if(!wait && !XEventsQueued(dpy, QueuedAfterFlush))
	    return;
	cout << "Get next event\n";
	wait = false;
	XNextEvent(dpy, &e);
	//cout << "getWindow\n";
	LXWindow *w = windowsByX[e.xkey.window];
	if(!w) {
	    cout << "Event for unknown window\n";
	    continue;
	}
	//cout << "deliver\n";
	w->deliverEvent(&e);
    }
}

bool LXWindowSystem::tryRepaint() {
    for(unsigned i=0; i<windows.size(); i++) {
	if(windows[i]->tryRepaint()) return true;
    }
    return false;
}

/** Load an image into a raster using gdk-pixbuf.
 */
ImageRaster loadImage(const char *filename) {
    GdkPixbuf* pb = gdk_pixbuf_new_from_file(filename);
    if(!pb) throw Gzz::Problem();

    cout << "NC "<<gdk_pixbuf_get_n_channels(pb)
	<<" ALP "<<gdk_pixbuf_get_has_alpha(pb)
	<<" BPS "<<gdk_pixbuf_get_bits_per_sample(pb) 
	<<" PIX "<<int(gdk_pixbuf_get_pixels(pb))
	<<" W "<<gdk_pixbuf_get_width(pb)
	<<" H "<<gdk_pixbuf_get_height(pb)
	<<" RS "<<gdk_pixbuf_get_rowstride(pb)
	<<"\n";

    GLuint *c = (GLuint *)gdk_pixbuf_get_pixels(pb);
    vector<Tex::Format::RGBA::ValueType> data;
    int w = gdk_pixbuf_get_width(pb);
    int h = gdk_pixbuf_get_height(pb);
    copy(c, c + w * h, back_inserter(data));
    /*
    for(int i=0; i<w*h; i++) 
	cout << data[i] << " ";
    */
    cout << "\n";
    gdk_pixbuf_unref(pb);
    return ImageRaster(w, h, data);
}

}
}
