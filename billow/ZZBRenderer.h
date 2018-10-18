/*
 * ZZBRenderer.h
 * Vesa Parkkinen
 * Fri Nov 26 17:02:01 GMT 1999
 */
#include <gtk/gtk.h>

#include <gdk/gdk.h>
#include <gdk/gdkx.h>
#include <gtk/gtk.h>
#include <libart_lgpl/art_misc.h>
#include <libart_lgpl/art_rgb_bitmap_affine.h>
#include <libart_lgpl/art_affine.h>
#include <stl.h>
#include <unistd.h>
#include <math.h>
#include "suckfont.h"
#include "ZZBLines.h"


class ZZBRenderer {

  // cache the fonts used
  // to be done properly !
  // let's use map, now that we've gotten used to it
  map < char *, SuckFont * > font_cache; 
  
  int wx;
  int wy;
  int lower_y;
  int upper_y;
  
  char gamtab[256];

  GdkPixmap *pixmap;
  GdkWindow *wind;
  GdkGC *gc;
  //SuckFont *sf;

 public:
  
  ZZBRenderer(int x, int y, GdkWindow *window){
    //sf == NULL;
    wind = window;
    if ( x < 0 ) x = 0;
    if ( y < 0 ) y = 0;
    wx = x, wy = y;
    int depth;
    gdk_window_get_geometry(wind, 0, 0, 0, 0, &depth);
    pixmap = gdk_pixmap_new(wind, wx, wy, depth);  
    gc = gdk_gc_new(wind);
    for(int i=0; i<256; i++) {
      gamtab[i] = (char) (255*pow((i/255.0),0.7));
    }
    lower_y = 0;
    upper_y = 0;
    
  }
  
  ~ZZBRenderer();
  
  // widget to render, linenumber, the text,the font size, and the coordinates.
  //int render(GdkPixmap *pixmap, int line, char *txt, char *font, double size, int x, int y);
  //int render(guchar  *img, int line, char *txt, char *font, double size, int x, int y);
  int renderLines(guchar  *img, ZZBLines *lines, char *font,int from, int to);

  void gam_tab(guchar *img, int size);
  void flush(guchar *img);
  void flush();
  
};









