
#include <gdk/gdk.h>
#include <gdk/gdkx.h>
#include <gtk/gtk.h>
#include <libart_lgpl/art_misc.h>
#include <libart_lgpl/art_rgb_bitmap_affine.h>
#include <libart_lgpl/art_affine.h>
#include <unistd.h>
#include "suckfont.h"
#include <math.h>
#include <stdio.h>

char gamtab[256];

// Max shrink 16x16 due to guchar result
// src==bitmap

// The tightest loop is here - do optimize...
void add_char(guchar *dst, 
		int dsx, int dsy,
		int dstride,
		guchar *src, int sstride,
		int width, int height, 
		int divisor)
{
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
		int flag=0;
#define DO_JITTER(jx,jy) \
		for(int y=(divisor*jy)/jitmult; y<height; y+=divisor) { \
			for(int x=(divisor*jx)/jitmult; x<width; x+=divisor) { \
				if(src[y*sstride + (x>>3)] & (128 >> (x&7))) { \
					dst[((y+dsy)/divisor)*dstride + ((x+dsx)/divisor)] ++; \
				} \
				flag=!flag; \
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

int ndiv(int div) {
	if(div <= 3) return div*div;
	return 8;
}

void rend_str(guchar *dst,
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

#define IMGX 400
#define IMGY 450

int main(int argc,char**argv) {
	gdk_init(&argc, &argv);
	gdk_rgb_init();
	for(int i=0; i<256; i++) {
		gamtab[i] = (char) (255*pow((i/255.0),0.7));
	}
	GdkWindowAttr attrs;
	attrs.wclass = GDK_INPUT_OUTPUT;
	attrs.window_type = GDK_WINDOW_TOPLEVEL;
	GdkWindow *w = gdk_window_new(GDK_ROOT_PARENT(),&attrs, 0);
	gdk_window_move(w,200,200);
	gdk_window_resize(w,IMGX,IMGY);
	gdk_window_show(w);
	gdk_window_clear(w);

	int depth;
	gdk_window_get_geometry(w, 0, 0, 0, 0, &depth);
	GdkPixmap *pixmap = gdk_pixmap_new(w, IMGX, IMGY, depth);

	// GdkFont* gf = gdk_font_load("variable");
	GdkFont* gf = gdk_font_load("-*-courier-*-*-*-*-60-*-*-*-*-*-*-*");
	SuckFont* f = gnome_canvas_suck_font(gf);
	if(!f) {
		printf("NO FONT\n"); exit(1);
	}

	GdkGC* gc = gdk_gc_new(w);

	// GdkBitmap *bm = gdk_bitmap_create_from_data(w, (gchar*)f->bitmap, f->bitmap_width, f->bitmap_height);
	// gdk_draw_pixmap(w, gc, bm, 0,0,0,0,f->bitmap_width, f->bitmap_height);
	
	// gdk_draw_rgb_image(w,gc,0,0,f->bitmap_width, f->bitmap_height,GDK_RGB_DITHER_NONE, f->bitmap, 0);
	//
	int aoffs = f->chars['a'].bitmap_offset;
	
	guchar gray[500*f->bitmap_height];
	for(int x=0; x<500; x++) {
		for(int y=0; y<f->bitmap_height; y++) {
			int c = !!(f->bitmap[(aoffs>>3)+y*(f->bitmap_width>>3) + (x>>3)] & (128 >> (x&7)));
			// gray[500*y+x] = 255*c;
			gray[500*y+x] = c*255;
		}
	}
	gdk_draw_gray_image(w,gc,0,0,500, f->bitmap_height,GDK_RGB_DITHER_NONE, gray, 500);

/*

	guchar rgb[500*f->bitmap_height * 3];

	ArtAlphaGamma *a = art_alphagamma_new(1.0);

	double ident[6];
	art_affine_scale(ident,0.5,0.5);

	art_rgb_bitmap_affine(rgb, 0, 0, 250, f->bitmap_height/2 +1 ,  500*3, 
				f->bitmap + (aoffs>>3), 500, f->bitmap_height, (f->bitmap_width>>3),
				0xffffff20,
				ident,
				ART_FILTER_NEAREST,
				a);
		// gdk_draw_rgb_image(w,gc,0,100+30*i,500, f->bitmap_height,GDK_RGB_DITHER_NONE, rgb, 500*3);
 */

/*
	int cury = f->bitmap_height+5;
	for(int i=2; i<60; i++) {
		int str = 500/i + 1;
		int dep = f->bitmap_height/i + 1;
		int gray2[str * dep]; for(int l = 0; l<str*dep; l++) gray2[l] = 0;
		for(int x=0; x<500; x++) {
			for(int y=0; y<f->bitmap_height; y++) {
				if(gray[500*y+x])
					gray2[(y/i)*str + (x/i)] ++;
			}
		} 
		guchar g2[str*dep];
		for(int l = 0; l<str*dep; l++) g2[l] = gamtab[(255*gray2[l])/(i*i)];

		cury += dep + 5;

		gdk_draw_gray_image(w,gc,0,cury,str, dep,GDK_RGB_DITHER_NONE, g2, str);

	}
*/

	int shrink = 4;
	while(1) {
		guchar img[IMGX*IMGY];
		for(int l=0; l<IMGX*IMGY; l++) img[l]=0;

		for(int y=0; y<5; y++) {
			rend_str(img, 0, y*f->bitmap_height, IMGX, shrink, f, 
					"This is a really simple { I mean simple } test() string;");
			gdk_draw_gray_image(pixmap,gc,0,0,IMGX, IMGY,GDK_RGB_DITHER_NONE, img+y*f->bitmap_height, IMGX);
		}

		int nd = ndiv(shrink);
		for(int l=0; l<IMGX*IMGY; l++) img[l]= gamtab[(255*img[l])/(nd)];
		gdk_draw_gray_image(pixmap,gc,0,0,IMGX, IMGY,GDK_RGB_DITHER_NONE, img, IMGX);
		gdk_draw_pixmap(w,gc,pixmap, 0,0,0,0, IMGX, IMGY);
		gdk_flush();
		if(shrink++ > 10) shrink = 3;
	}

	sleep(60);
}
