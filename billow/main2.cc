#include "ZZBLines.h"
#include "ZZBSizes.h" 
#include "ZZBRenderer.h"
#include "ZZBBulge.h"

#include <gtk/gtk.h>

#include <libart_lgpl/art_misc.h>
#include <libart_lgpl/art_rgb_bitmap_affine.h>
#include <libart_lgpl/art_affine.h>
#include <unistd.h>
#include "suckfont.h"
#include <math.h>
#include <gdk/gdkkeysyms.h>

// for file reading
#include <fstream.h>

// for oGL stuff
#include <GL/glut.h>
// use openGL ?
//#define OPEN_GL
//#undef GDKRGB

/* Use gdkrgb instead of imlib? */
#define GDKRGB
/* Use a pixmap between the rendering and the window? */
#undef USE_PIXMAP

#ifndef GDKRGB
extern "C" {
#include <Imlib2.h>
}
#endif

#define IMGX 500
#define IMGY 600

/* gtk widgets */
GtkWidget *window1;
GtkWidget *table1;
GtkWidget *handlebox;
GtkWidget *table_handle;
GtkWidget *drawingarea;


#ifdef USE_PIXMAP
GdkPixmap *pixmap;
#else 
#define pixmap w
#endif

GdkColormap *bestcmap=0;

GdkVisual *bestvisual;

ZZBBulge *b;
ZZBLines *lines;
GdkGC    *gc;
guchar   img[IMGX*IMGY];

int bulge;

unsigned argbimg[IMGX*IMGY];

ZZBRenderer *r;
GdkWindow *w;

// mouse button press coords
int but_x, but_y;


int updateadded = 0;

inline gint update_drawing_really (void *ptr) {
  if(!bestcmap) {
    bestcmap = gdk_window_get_colormap(
				       GTK_WIDGET(window1)->window);
  }
  updateadded = 0;
  // if(!ptr) return 0;
  r->renderLines(img, lines,  "-*-courier-*-*-*-*-60-*-*-*-*-*-*-*",0,0);
  //if ( j == 0 ) j = IMGX;
#ifdef GDKRGB
  gdk_draw_gray_image(pixmap,gc,0,0,IMGX, IMGY, GDK_RGB_DITHER_NONE, img, IMGX);
#else
  /*  
      for(int i=0; i<IMGX*IMGY; i++) {
      ((unsigned *)argbimg)[i] =
      (img[i] + (img[i] << 8) + (img[i] << 16));
      }
      Imlib_Image im = imlib_create_image_using_data(IMGX, IMGY, argbimg);
      imlib_render_image_on_drawable(im, 
      GDK_WINDOW_XDISPLAY(w),
      GDK_WINDOW_XWINDOW(pixmap),
      GDK_VISUAL_XVISUAL(bestvisual),
      GDK_COLORMAP_XCOLORMAP(bestcmap),
      16, 0, 0,
      0, 0, 0,IMLIB_OP_COPY);
  */ 
#endif
  
#ifdef USE_PIXMAP
  gdk_draw_pixmap(w,gc,pixmap,0,0,0,0, IMGX, IMGY );
#endif
  // gdk_flush();
  return 0;
} 

void update_drawing() {
  if(!updateadded) {
    gtk_idle_add(update_drawing_really, 0);
    updateadded = 1;
  }
}

/*
 * deleteEvent
 */

gint delete_event( GtkWidget *widget, GdkEvent  *event, gpointer   data ){
  gtk_main_quit();
  return FALSE;
}

gint button_press_event(GtkWidget *widget, GdkEventButton *event, gpointer data){
  but_x = event->x;
  but_y = event->y; 
}

static gint motion_notify_event (GtkWidget *widget, GdkEventMotion *event)
{
  int x, y;
  GdkModifierType state;
  
  if (event->is_hint)
    gdk_window_get_pointer (event->window, &x, &y, &state);
  else
    {
      x = event->x;
      y = event->y;
      state = (GdkModifierType)event->state;
    }
  int i=0,j=0;
  if (state & GDK_BUTTON1_MASK && pixmap != NULL) {
    //b->moveBulge( (y - but_y)/2);
    lines->move_bulge( bulge, (y - but_y)/2);
    update_drawing();
    but_x = x;
    but_y = y;
  }
  return TRUE;
}

static gint key_press_event(GtkWidget *widget, GdkEventKey *event, gpointer data){   
  int i=0,j=0;
  //if ( b == NULL ) return 0;
  switch (event->keyval) {
    
  case GDK_Up:
    //g_print("up");
    lines->move_bulge(bulge,-1);
    break;
  case GDK_Down:
    //g_print("down");
    lines->move_bulge( bulge,1);
    break;
  case GDK_Page_Down:
    //g_print("down");
    lines->move_bulge( bulge, 10 );
    break;
  case GDK_Page_Up:
    //g_print("down");
    lines->move_bulge( bulge, -10);
    break;
    
  }
  update_drawing();
  return 0;
  
  //r->flush(img);
}

gint expose_event(GtkWidget *widget, GdkEventExpose *event, gpointer data) {
  
  gdk_draw_pixmap(widget->window,
		  widget->style->fg_gc[GTK_WIDGET_STATE (widget)],
		  pixmap,
		  event->area.x, event->area.y,
		  event->area.x, event->area.y,           
		  event->area.width, event->area.height);
  
  //r->renderLines(img, lines,  "-*-courier-*-*-*-*-60-*-*-*-*-*-*-*",1);
  //r->flush();
  return TRUE;
} 

/*
 * readFile
 */


ZZBLines *readFile(char *file){
  ifstream istr(file);
  if ( ! istr  ) return NULL;
  char txt[81]; 
  
  istr.getline(txt,80);
  txt[81] = '\0';
  
  ZZBLines *lines = new ZZBLines();
  int i = 0;
  while ( istr /* i < 200*/ ){
    lines->setLine(i, NULL, txt, 2);
    istr.getline(txt,80, '\n');
    txt[81]=0;
    
    g_print("%d: %s\n",i, lines->getLine(i,NULL));
    
    i++;
    
  }  
  return lines;
  
}
#ifdef OPEN_GL
void init();
void display();
void reshape(int w, int h);

void init(void)
{    
  glClearColor (0.0, 0.0, 0.0, 0.0);
  glShadeModel(GL_FLAT);
  //makeCheckImage();
  glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
}

inline void display(void)
{
  glClear(GL_COLOR_BUFFER_BIT);
  glRasterPos2i(0, 0);
  //glRasterPos2i(0, IMGY);
  //glPixelZoom(1.0,-1.0);
  glDrawPixels(IMGX, IMGY, /*GL_RGB*/ GL_LUMINANCE, 
  	       GL_UNSIGNED_BYTE, img);
  //glPixelZoom(1,1);
  glutSwapBuffers();
  //glutPostRedisplay();
  //glFlush();
}

void reshape(int w, int h)
{
  glViewport(0, 0, (GLsizei) w, (GLsizei) h);
  //height = (GLint) h;
  glMatrixMode(GL_PROJECTION);
  glLoadIdentity();
  gluOrtho2D(0.0, (GLdouble) w, 0.0, (GLdouble) h );
  glMatrixMode(GL_MODELVIEW);
  glLoadIdentity();
}

void motion(int x, int y){
  
  //static GLint screeny;
  
  //screeny = height - (GLint) y;
  //glRasterPos2i (x, screeny);
  //glPixelZoom (1.0, -1.0);
  //glCopyPixels (0, 0, IMGX, IMGY, GL_COLOR);
  //glPixelZoom (1.0, 1.0);
  //glutSwapBuffers();
  //glFlush ();
}

inline void keyboard(unsigned char key, int x, int y){   
  //int i=0,j=0;
  //if ( b == NULL ) return 0;
  switch (key) {
    
  case 'e':
    //g_print("up");
    lines->move_bulge(bulge,-1);
    break;
  case 'c':
    //g_print("down");
    lines->move_bulge( bulge,1);
    break;
    //case GDK_Page_Down:
    //g_print("down");
    //lines->move_bulge( bulge, 10 );
    //break;
    //case GDK_Page_Up:
    //g_print("down");
    //lines->move_bulge( bulge, -10);
  default:
    return;
    break;
    
  }
    //update_drawing();
  //update_drawing();
  if ( updateadded ) {
    r->renderLines(img, lines,  "-*-courier-*-*-*-*-60-*-*-*-*-*-*-*",0,0);
    glutPostRedisplay();
    updateadded = 0;
  }
  else 
    updateadded = 1;
  //return 0;
  
  //r->flush(img);
}


#endif



/*
 * createWindow
 */


void createWindow(){
  
  window1 = gtk_window_new (GTK_WINDOW_TOPLEVEL);
  gtk_object_set_data (GTK_OBJECT (window1), "window1", window1);
  gtk_window_set_title (GTK_WINDOW (window1), "window1");
  gtk_window_set_default_size(GTK_WINDOW(window1),400,400);
  
  gtk_signal_connect(GTK_OBJECT(window1), "delete_event",
                     GTK_SIGNAL_FUNC(delete_event), NULL );
  
  
  table1 = gtk_table_new (10, 10, FALSE);
  gtk_widget_ref (table1);
  
  gtk_object_set_data_full (GTK_OBJECT (window1), "table1", table1,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (table1);
  gtk_container_add (GTK_CONTAINER (window1), table1);
  
  handlebox = gtk_handle_box_new ();
  gtk_widget_ref (handlebox);
  gtk_object_set_data_full (GTK_OBJECT (window1), "handlebox", handlebox,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (handlebox);
  
  gtk_table_attach (GTK_TABLE (table1), handlebox, 0, 10, 0, 1,
                    (GtkAttachOptions) (GTK_EXPAND | GTK_SHRINK | GTK_FILL),
                    (GtkAttachOptions) (GTK_EXPAND | GTK_SHRINK | GTK_FILL), 0,0);
  gtk_container_set_border_width (GTK_CONTAINER (handlebox), 2);
  
  table_handle = gtk_table_new (2, 6, FALSE);
  gtk_widget_ref (table_handle);
  gtk_object_set_data_full (GTK_OBJECT (window1), "table_handle", table_handle,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (table_handle);
  
  gtk_container_add (GTK_CONTAINER (handlebox), table_handle);
  drawingarea = gtk_drawing_area_new ();
  gtk_widget_ref (drawingarea);
  gtk_object_set_data_full (GTK_OBJECT (window1), "drawingarea", drawingarea,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (drawingarea);
  
  gtk_signal_connect (GTK_OBJECT (drawingarea),"expose_event",
                      GTK_SIGNAL_FUNC (expose_event), NULL);
  
  //  gtk_widget_set_events (drawingarea, GDK_BUTTON_PRESS_MASK|GDK_KEY_PRESS_MASK);
  gtk_widget_set_events (drawingarea, GDK_EXPOSURE_MASK
			 | GDK_LEAVE_NOTIFY_MASK
			 | GDK_BUTTON_PRESS_MASK
			 | GDK_POINTER_MOTION_MASK
			 | GDK_POINTER_MOTION_HINT_MASK);  
  gtk_signal_connect (GTK_OBJECT (window1), "button_press_event",
                     (GtkSignalFunc) button_press_event, NULL);

  gtk_signal_connect(GTK_OBJECT(window1), "key_press_event",
                     GTK_SIGNAL_FUNC(key_press_event), NULL);
  gtk_signal_connect (GTK_OBJECT (drawingarea), "motion_notify_event",
		      (GtkSignalFunc) motion_notify_event, NULL);  
  

  gtk_table_attach (GTK_TABLE (table1), drawingarea, 1, 9, 1, 9,
                    (GtkAttachOptions) (GTK_EXPAND | GTK_SHRINK | GTK_FILL),
                    (GtkAttachOptions) (GTK_EXPAND | GTK_SHRINK | GTK_FILL), 1,0);
  gtk_widget_set_usize(drawingarea, IMGX,IMGY);
  gtk_widget_show(window1);
  gtk_widget_grab_focus(drawingarea);
   
}


/*
 * Main
 */
int main(int argc, char **argv)
{
  
  //  ZZBLines *l = new ZZBLines();
  //l->setLine(1, NULL, "Testi");
  //l->setLine(2, NULL, "Toinen, joskin hieman pitempi rivi asdfasdf as");
  //l->setLine(3, NULL, "Kolmas lyhyt rivi      ");
  //for ( int i = 4; i < 1000; i++ )
  // l->setLine(i, NULL, "Kolmas, joskin asf asdf");
  //char *s = l->getLine(1,NULL);
  
  lines = readFile("./test");
  
  if(!lines) {
    printf("No lines\n");
    exit(1);
  }
  //    line, big size,above, below, ascent ,descent 
  bulge = lines->new_bulge(10, 10, 3, 3, 6, 6);
  
  //if( s )
  //  printf("%s\n",s);
  //s = l->getLine(2,NULL);
  
  //if( s ) 
  //  printf("%s\n",s);
  
  //ZZBSizes *bs = new ZZBSizes();
  //bs->setLine(1,15.3, 14.2);
  
  
  //gdk_init(&argc, &argv);
  //gdk_rgb_init();
  
  //#ifndef OPEN_GL
  gtk_init(&argc, &argv);

#ifndef OPEN_GL 
  createWindow();
  w = drawingarea->window;
#endif  

  gdk_rgb_init();
  
  bestvisual = gdk_visual_get_best();
  
  int depth;
  
  gdk_window_get_geometry(w, 0, 0, 0, 0, &depth);
  
  //#endif
  

#ifdef USE_PIXMAP
  pixmap = gdk_pixmap_new(w, IMGX, IMGY, depth);
#endif
  
  r = new ZZBRenderer(IMGX, IMGY, w);
  
  //b = new ZZBBulge(lines,4, 6, 10, 3, 0);
  //if ( b==NULL ) { printf( "no b" ); exit(1); }
  //b->bulge(lines,13);
  //b->bulge(lines,57);
  //lines->setLineSize(3,14);
  //lines->setLineSize(10,10);
  //  ZZBBulge(int small, int trans, int big, int count){ 
  //#ifndef OPEN_GL
  gc = gdk_gc_new(w);
  //#endif
  
  
  for(int l=0; l<IMGX*IMGY; l++) img[l]=0;
  int size = 14;
  /*
    for ( int i = 0; i < 20 ; i++ ){
    r->render(img, i,lines->getLine(i, NULL) , "-*-courier-*-*-*-*-60-*-*-*-*-*-*-*", size, 0, i);
    }
  */
  
  
  //for ( int i = 0; i < 1; i++ ) {
    //for(int l=0; l<IMGX*IMGY; l++) img[l]=0;
  //int *i,*j;
  //b->moveBulge(1,i,j);
  //int y1=0,y2=0;
  
#ifdef OPEN_GL
  r->renderLines(img, lines,  "-*-courier-*-*-*-*-60-*-*-*-*-*-*-*",0,0);  
  glutInit(&argc, argv);
  glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB);
  glutInitWindowSize(IMGX, IMGY);
  glutInitWindowPosition(100, 100);
  glutCreateWindow(argv[0]);
  init();
  glutDisplayFunc(display);
  glutReshapeFunc(reshape);
  glutKeyboardFunc(keyboard);
  glutMotionFunc(motion);

  glDisable(GL_DITHER);
  glDisable      ( GL_LIGHTING   ) ;
  glDisable      ( GL_FOG        ) ;
  glDisable      ( GL_TEXTURE_2D ) ;
  glDisable      ( GL_DEPTH_TEST ) ;
  glDisable      ( GL_CULL_FACE  ) ;

  glutMainLoop();


#else
  update_drawing();
  // r->renderLines(img, lines,  "-*-courier-*-*-*-*-60-*-*-*-*-*-*-*", 0,0);
  // gdk_draw_gray_image(pixmap,gc,0,0,IMGX,IMGY, GDK_RGB_DITHER_NONE, img, IMGX);
  // gdk_draw_pixmap(w,gc,pixmap,0,0, 0,0, IMGX,IMGY);
  // gdk_flush();
  //r->flush(img);
  //}
  
  // int renderLines(guchar  *img, ZZBLines *lines, char *font);
  //r->gamtab(img, size);
  /*
    gdk_draw_gray_image(pixmap,gc,0,0,IMGX,IMGY, GDK_RGB_DITHER_NONE, img, IMGX);
    gdk_draw_pixmap(w,gc,pixmap,0,0, 0,0, IMGX, IMGY);
    
    gdk_flush();
  */
  gtk_main();
#endif
  return 0;
}
























