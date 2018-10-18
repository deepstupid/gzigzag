/* 
 * Ripped for ZZXBL by Tjl from:
 *
 * Text item type for GnomeCanvas widget
 *
 * GnomeCanvas is basically a port of the Tk toolkit's most excellent canvas widget.  Tk is
 * copyrighted by the Regents of the University of California, Sun Microsystems, and other parties.
 *
 * Copyright (C) 1998 The Free Software Foundation
 *
 * Author: Federico Mena <federico@nuclecu.unam.mx>
 *
 */

#ifndef SUCKFONT_H
#define SUCKFONT_H

#include <libgnome/gnome-defs.h>

typedef struct _GnomeCanvasTextSuckFont GnomeCanvasTextSuckFont;
typedef struct _GnomeCanvasTextSuckFont SuckFont;
typedef struct _GnomeCanvasTextSuckChar GnomeCanvasTextSuckChar;

struct _GnomeCanvasTextSuckChar {
	int     left_sb;
	int     right_sb;
	int     width;
	int     ascent;
	int     descent;
	int     bitmap_offset; /* in pixels */
};

struct _GnomeCanvasTextSuckFont {
	guchar *bitmap;
	gint    bitmap_width;
	gint    bitmap_height;
	gint    ascent;
	GnomeCanvasTextSuckChar chars[256];
};

GnomeCanvasTextSuckFont *gnome_canvas_suck_font (GdkFont *font);
void gnome_canvas_suck_font_free (GnomeCanvasTextSuckFont *suckfont);


#endif
