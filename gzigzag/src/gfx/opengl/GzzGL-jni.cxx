#include "org_gzigzag_gfx_GZZGL.h"

#include "GzzGL.hxx"

using namespace Gzz::AbstractWin;
using namespace Gzz;
using namespace Gummi;
using Gummi::Font::DenseGlyphs;
using Gummi::Font::RandomRenderer;

using std::cout;

WindowSystem *ws;


// XXX MEMLEAK
struct RealFont {
    Gummi::Font::Font *f;
   RandomRenderer<DenseGlyphs<char>, char> *rend;
   RealFont(Gummi::Font::Font *f) : f(f) {
       rend = new TextRenderer(f);
   }
};

ObjectStorer<Window> windows;
ObjectStorer<Image> images;
ObjectStorer<Tex::MosaicTile> imagetiles;
ObjectStorer<Gzz::ShaderRect> shaderrects;
ObjectStorer<RealFont> fonts;
ObjectStorer<Renderable0> renderable0s;
ObjectStorer<Renderable1> renderable1s;
ObjectStorer<Renderable2> renderable2s;

Gzz::Renderer<AffineCoords> renderer;

Tex::MosaicBuilder *mosaicbuilder;

Window *defaultWindow; // A kludge

struct JavaException { };

JNIEnv *jnienv_eventloop;

struct GZZJNIEventHandler : public Gzz::AbstractWin::Eventhandler {
    
    jobject globalRef;
    jclass globalclass; // must keep for mid to remain valid
    jmethodID mid_repaint;
    jmethodID mid_keystroke;
    
    GZZJNIEventHandler(JNIEnv *env, jobject globalRef) : globalRef(globalRef) { 
	if(globalRef) {
	    jclass cls = env->GetObjectClass(globalRef);
	    globalclass = (jclass)env->NewGlobalRef(cls);
	    mid_repaint = env->GetMethodID(globalclass, 
			 "repaint", "()V");
	    mid_keystroke = 
		 env->GetMethodID(cls, 
			 "keystroke", "(Ljava/lang/String;)Z");
	}
    }
    ~GZZJNIEventHandler() {
    }
    void repaint() {
	// cout << "CALLING REPAINT!!!\n";
	jnienv_eventloop->CallVoidMethod(globalRef, mid_repaint);

	if(jnienv_eventloop->ExceptionOccurred()) {
	    cout << "Java exception in event handler!\n";
	    throw JavaException();
	}
	
	// jnienv_eventloop->DeleteLocalRef(cls);

	// cout << "CALLED REPAINT\n";
    }
    virtual void keystroke(const char *str) {
	//cout << "Keystroke being sent to java\n";
	jstring jstr = jnienv_eventloop->NewStringUTF(str);
	//cout << "Keystroke has been sent\n";

	// cout << "Method id keystroke: "<<mid<<"\n";

	jnienv_eventloop->CallBooleanMethod(globalRef, mid_keystroke, jstr);

	// cout << "Call finished\n";

	// jnienv_eventloop->DeleteLocalRef(jstr);
	// jnienv_eventloop->DeleteLocalRef(cls);
    }
};

/*
struct GZZJNIIdler : public Gzz::AbstractWin::IdleTasks {
    jobject globalRef;
    jclass globalclass; // must keep for mid to remain valid
    jmethodID mid_tick;
    GZZJNIIdler(JNIEnv *env, jobject globalRef) : globalRef(globalRef) { 
	jclass cls = env->GetObjectClass(globalRef);
	globalclass = (jclass)env->NewGlobalRef(cls);
	mid_tick = env->GetMethodID(globalclass, 
		     "tick", "()Z");
    }
    ~GZZJNIIdler() {
    }
    bool tick() {
	// cout << "Starting to call tick\n";
	// cout << "Calling tick "<<mid<<"\n";
	bool ret = jnienv_eventloop->CallBooleanMethod(globalRef, mid_tick);
	// cout << "Finished: \n" << ret;
	return ret;
    }
};
*/

extern "C" {

JNIEXPORT jint JNICALL Java_org_gzigzag_gfx_GZZGL_init
  (JNIEnv *env, jclass, jint) {
      cout << "Initializing GZZGL\n";
    ws = WindowSystem::getInstance();
      cout << "Calling 'new' to see if we crash...\n";

    defaultWindow = ws->openWindow(0, 0, 2, 2);


      GZZJNIEventHandler *eh = new GZZJNIEventHandler(env, 0);
      eh = eh; // eat warning
      cout << "Creating texturefactory\n";
      Tex::Texture2DFactory *fact = 
	new Tex::Texture2DFactory(
	    new Tex::Raster<Tex::Format::RGBA>(512, 512)
	    );
      cout << "Initializing Mosaicbuilder\n";
    mosaicbuilder = new Tex::MosaicBuilder(fact);
      cout << "Returning\n";
    return 0;
  }


// Window

JNIEXPORT jint JNICALL Java_org_gzigzag_gfx_GZZGL_createWindowImpl
  (JNIEnv *env, jclass, jint x, jint y, jint w, jint h, jobject eh) {
      jobject ehglobal = env->NewGlobalRef(eh);
      GZZJNIEventHandler *evh = new GZZJNIEventHandler(env, ehglobal);
      Window *win = ws->openWindow(x, y, w, h);
      win->setEventHandler(evh);
      return windows.add(win);
  }

JNIEXPORT void JNICALL Java_org_gzigzag_gfx_GZZGL_deleteWindow
  (JNIEnv *env, jclass, jint id) {
      
      Window *w = windows.get(id);
      GZZJNIEventHandler *h = (GZZJNIEventHandler *)w->getEventHandler();
      env->DeleteGlobalRef(h->globalRef);
      windows.remove(id);
  }

JNIEXPORT void JNICALL Java_org_gzigzag_gfx_GZZGL_repaintWindow
  (JNIEnv *env, jclass, jint id) {
      // cout << "RepaintWindow called\n";
      Window *w = windows.get(id);
      w->repaint();
  }

// Image

JNIEXPORT jint JNICALL Java_org_gzigzag_gfx_GZZGL_createImageImpl
  (JNIEnv *env, jclass, jstring filename) {
      const char *utf = env->GetStringUTFChars(filename, 0);
      Image *img = ws->loadImageFile(utf);
      env->ReleaseStringUTFChars(filename, utf);
      return images.add(img);
  }

JNIEXPORT void JNICALL Java_org_gzigzag_gfx_GZZGL_deleteImage
  (JNIEnv *, jclass, jint img) {
      images.remove(img);
  }

// TexRect

JNIEXPORT jint JNICALL Java_org_gzigzag_gfx_GZZGL_createTexRectImpl
  (JNIEnv *, jclass, jint id)
{
    Image *img = images.get(id);
    Tex::MosaicTile *t = new Tex::MosaicTile();
    int w = img->w;
    int h = img->h;
    *t = mosaicbuilder->alloc(w, h);
    img->loadInto(*t, 0, 0, w, h);
    mosaicbuilder->prepare();
    return imagetiles.add(t);
}

JNIEXPORT void JNICALL Java_org_gzigzag_gfx_GZZGL_deleteTexRect
  (JNIEnv *, jclass, jint id)
{
    // MEMLEAK! MUST FREE MOSAICTILE FIRST!
    imagetiles.remove(id);
}

// ShaderRect

JNIEXPORT jint JNICALL Java_org_gzigzag_gfx_GZZGL_createShaderRectImpl
  (JNIEnv *env, jclass, jstring turb, jstring col, jstring spots, jstring cell)
{
    const char *utf_turb = env->GetStringUTFChars(turb, 0);
    const char *utf_col = env->GetStringUTFChars(col, 0);
    const char *utf_spots = env->GetStringUTFChars(spots, 0);
    const char *utf_cell = env->GetStringUTFChars(cell, 0);

    ShaderRect *sh = new ShaderRect();
    sh->load(utf_turb, utf_col, utf_spots, utf_cell);
    
    return shaderrects.add(sh);
}

JNIEXPORT void JNICALL Java_org_gzigzag_gfx_GZZGL_deleteShaderRect
  (JNIEnv *, jclass, jint id)
{
    shaderrects.get(id)->unload();
    shaderrects.remove(id);
}


// Font

JNIEXPORT jint JNICALL Java_org_gzigzag_gfx_GZZGL_createFontImpl
  (JNIEnv *env, jclass, jstring file, jint pt) {
      const char *utf = env->GetStringUTFChars(file, 0);
      Gummi::Font::Font *gf = ws->loadFont(utf, pt);
      env->ReleaseStringUTFChars(file, utf);
      RealFont *f = new RealFont(gf);
      return fonts.add(f);
  }

JNIEXPORT void JNICALL Java_org_gzigzag_gfx_GZZGL_deleteFont
  (JNIEnv *, jclass, jint i) {
      fonts.remove(i);
  }


JNIEXPORT void JNICALL Java_org_gzigzag_gfx_GZZGL_deleteRenderable0
  (JNIEnv *, jclass, jint id) {
      renderable0s.remove(id);
  }

JNIEXPORT void JNICALL Java_org_gzigzag_gfx_GZZGL_deleteRenderable1
  (JNIEnv *, jclass, jint id) {
      renderable1s.remove(id);
  }

JNIEXPORT void JNICALL Java_org_gzigzag_gfx_GZZGL_deleteRenderable2
  (JNIEnv *, jclass, jint id) {
      renderable2s.remove(id);
  }
// functions

JNIEXPORT void JNICALL Java_org_gzigzag_gfx_GZZGL_eventLoop
  (JNIEnv *env, jclass, jboolean wait) {
      cout << "Going into eventloop in C++\n";
      try {
	jnienv_eventloop = env;
	  ws->eventLoop(wait);
      } catch(JavaException e) {
	  cout << "CAUGHT JAVA EXCEPTION\n";
      }
      /*
	ticker = env->NewGlobalRef(ticker);
	ws->addIdle(new GZZJNIIdler(env, ticker));
	ws->eventLoop();
      */
  }

JNIEXPORT void JNICALL Java_org_gzigzag_gfx_GZZGL_renderImpl
  (JNIEnv *env, jclass, jint window,
	jintArray codes, jfloatArray pts1, jintArray indices2,
	    jfloatArray pts2,
	    jint numpts, jfloat fract) {
      // cout << "RENDER\n";
      Window *win = windows.get(window);
      win->setCurrent();
    
      if(sizeof(jint) != sizeof(jint) ||
	 sizeof(jfloat) != sizeof(float))
	  env->FatalError("Invalid data type sizes!");

      jint *ncodes = env->GetIntArrayElements(codes, 0);
      jfloat *npts1 = env->GetFloatArrayElements(pts1, 0);
      jint *ninds2 = 0;
      if(indices2 != 0) {
	  ninds2 = env->GetIntArrayElements(indices2, 0);
      }
      jfloat *npts2 = env->GetFloatArrayElements(pts2, 0);

      renderer.setPoints(numpts, npts1, (int *)ninds2, npts2, fract);

      setStandardCoordinates(win->getSize());
      renderer.renderScene((int *)ncodes, renderable0s, renderable1s, renderable2s);
      win->swapBuffers();

      env->ReleaseIntArrayElements(codes, ncodes, JNI_ABORT);

      env->ReleaseFloatArrayElements(pts1, npts1, JNI_ABORT);
      env->ReleaseFloatArrayElements(pts2, npts2, JNI_ABORT);

      if(indices2 != 0) {
	  env->ReleaseIntArrayElements(indices2, ninds2, JNI_ABORT);
      }

  }


#include "GzzGLRen-jni.cxx"

}
