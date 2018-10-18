#include "ZZBRenderer.h"
#include "suckfont.h"

//#define FULL_ANTIALIAS 1
#define IMGX 300
#define IMGY 150

/* Draw only solid blocks for letters */
#undef TEST_SOLID
//#define TEST_SOLID 
// The tightest loop is here - do optimize...
inline void add_char(guchar *dst, 
		     int dsx, int dsy,
		     int dstride,
		     guchar *src, int sstride,
		     int width, int height, 
		     int divisor)
{
#ifdef TEST_SOLID
  for(int y=0; y<= height/divisor; y++) {
    for(int x=0; x<=width/divisor; x++) {
      dst[(y+dsy/divisor)*dstride + (x+dsx/divisor)] ++;
      
    }
  }
  return;
#endif
#ifdef FULL_ANTIALIAS
  for(int y=0; y<height; y++) {
    for(int x=0; x<width; x++) {
      if(src[y*sstride + (x>>3)] & (128 >> (x&7))) {
	dst[((y+dsy)/divisor)*dstride + ((x+dsx)/divisor)] ++;
      }
    }
  }
#else
  if(divisor <= 3) {
    for(int y=0; y<height; y++) {
      for(int x=0; x<width; x++) {
	if(src[y*sstride + (x>>3)] & (128 >> (x&7))) {
	  dst[((y+dsy)/divisor)*dstride + ((x+dsx)/divisor)] ++;
	}
      }
    }
  } else {
    // Use less than the full set of points - approx. O(divisor) points.
    
#define DO_JITTER(jx,jy) \
    for(int y=(divisor*jy)/jitmult; y<height; y+=divisor) { \
      guchar *curdst = dst + ((y+dsy)/divisor)*dstride; \
      guchar *cursrc = src + y*sstride; \
      for(int x=(divisor*jx)/jitmult; x<width; x+=divisor) { \
	if(cursrc[(x>>3)] & (128 >> (x&7))) { \
	  curdst[((x+dsx)/divisor)] ++; \
	} \
      } \
    }
    int jitmult=16;
    
    DO_JITTER(9, 7)
      DO_JITTER(1, 15)
      DO_JITTER(5, 11)
      DO_JITTER(11, 13)
      DO_JITTER(11, 3)
      DO_JITTER(15, 9)
      DO_JITTER(7, 1)
      DO_JITTER(3, 5)
      
      }
#endif
}

inline int ndiv(int div) {
#ifdef TEST_SOLID
  return 1;
#endif
  if(div <= 3) return div*div;
  return 8;
}

inline void rend_str(guchar *dst,
		     int dsx, int dsy, int dstride, int divisor,
		     SuckFont *font, const char *string) 
{
  while(*string != 0) {
    GnomeCanvasTextSuckChar *ch = &(font->chars[(unsigned) *string]);
    add_char(dst, dsx, dsy, dstride, 
	     font->bitmap + (ch->bitmap_offset >> 3), (font->bitmap_width>>3),
	     ch->width, font->bitmap_height, divisor);
    
    dsx += ch->left_sb + ch->width + ch->right_sb;
    string++;
  }
}


//int ZZBRenderer::render(GdkPixmap *w, int line, char *txt, char *font, double size, int x, int y){
/*
  int ZZBRenderer::render(guchar *img, int line, char *txt, char *font, double size, int x, int y){
  
  SuckFont *sf;
  
  // cache
  map<char *, SuckFont *>::iterator p = font_cache.find(font);
  
  if ( p!= font_cache.end() ) sf = p->second;
  
  else {
  GdkFont* gf = gdk_font_load(font);
  sf = gnome_canvas_suck_font(gf);    
  font_cache[font] = sf;
  }
  
  //GdkGC* gc = gdk_gc_new(w);
  
  //guchar img[wx*wy];
  
  //for(int l=0; l<wx*wy; l++) img[l]=0;
  
  int shrink = (int) floor( 60/size );
  
  //for(int y=0; y<5; y++) {
  rend_str(img, 0 , sf->bitmap_height * y, wx, shrink, sf, txt);
  //}
  int n = ndiv(shrink);
  for( int l = (int) floor( size * y * wx); l <  size * (y+1) * wx;  l++ ){ 
    if ( l > wx * wy ) break; 
    img[l] = gamtab[ (255*img[l]) / n  ]; }
    //if ( shrink )
    
    //else  for(int l=0; l<wx*wy; l++) img[l]= gamtab[(255*img[l])];
    
    //gdk_draw_gray_image(w,gc,0,0,wx,wy, GDK_RGB_DITHER_NONE, img, wx);
    //  gdk_draw_pixmap(wind,gc,w, 0,0,0,0, IMGX, IMGY);
    //gdk_flush();
    
    }
*/
int ZZBRenderer::renderLines(guchar  *img, ZZBLines *lines, char *font, int from, int to){
  
  SuckFont *sf;
  
  // cache
  
  map<char *, SuckFont *>::iterator p = font_cache.find(font);
  
  if ( p!= font_cache.end() ) sf = p->second;
  
  else {
    GdkFont* gf = gdk_font_load(font);
    sf = gnome_canvas_suck_font(gf);    
    font_cache[font] = sf;
  }
  
  /*
    if ( sf == NULL ){
    GdkFont* gf = gdk_font_load(font);
    sf = gnome_canvas_suck_font(gf);    
    }
  */
  
  int ll = lines->getLastLine();
  
  int y = 0;
  
  for ( int i = 0; i <= ll; i++ ){
    
    int size   = lines->getLineSize(i);
    
    if ( y + size > wy ) return 1;
    
    if  ( ! lines->isDirty(i) ){
      y = y + size;
      continue;
    }
    
    //    if ( *lower_y == 0 )
    //      *lower_y = y;
    
    int wsxy = wx * (y + size);
    for( int l = y * wx ; l < wsxy;  l++) img[l]=0;
    int shrink = 60/size;
    //g_print("%d \n", y);
    //for(int y=0; y<5; y++) {
    rend_str(img, 0, y*shrink, wx, shrink, sf, lines->getLine(i, NULL));
    //}
    int n = ndiv(shrink);
    for( int l = y * wx; l < wsxy;  l++ ){ 
      if ( l > wx * wy ) break; 
      //img[l] = gamtab[ (255*img[l]) / n ]; }
      img[l] = gamtab[ ((img[l]<<8)  ) / n ]; }
    //if ( shrink )
    lines->clean(i);
    
    y = y + size; 
    //*upper_y = y;
  }
}

void ZZBRenderer::gam_tab(guchar *img, int size){
  int shrink =  60/size;
}

void  ZZBRenderer::flush(guchar *img){
  
  gdk_draw_gray_image(pixmap,gc,0,lower_y,wx,upper_y, GDK_RGB_DITHER_NONE, img, wx);
  //gdk_draw_gray_image(pixmap,gc,0,lower_y,wx,upper_y, GDK_RGB_DITHER_NONE, img, wx);
  //gdk_draw_pixmap(wind,gc,pixmap,0,0, 0,0, wx, wy);
  gdk_draw_pixmap(wind,gc,pixmap,0,lower_y, 0,0, wx, upper_y);
  //  gdk_draw_pixmap(wind,gc,pixmap,0,lower_y, 0,lower_y, wx, upper_y);
  
  // gdk_flush();
  lower_y = 0;
  upper_y = 0;
  
}

void  ZZBRenderer::flush(){
    
  gdk_draw_pixmap(wind,gc,pixmap,0,0, 0,0, wx, wy);
  
}




