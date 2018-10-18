/*
 * ZZBLines.h
 * Vesa Parkkinen
 * Fri Nov 26 17:02:01 GMT 1999
 */
#ifndef __ZZBLINES_H__
#define __ZZBLINES_H__

#include "Lines.h"
#include "ZZBLine.h"
#include "ZZBSize.h"
#include "ZZBBulge2.h"
#include <math.h>
#include <stdio.h>

class ZZBLines: public Lines {
  
  ZZBLine **lines[1024];
  ZZBSize **sizes[1024];
  
  int tables;
  int last_line;
  int num_bulges;
  
  ZZBBulge2 *bulges[256]; 
  
 public:
  
  ZZBLines();
  ~ZZBLines();
  
  char *getLine( int line, ZZBMarker **markers); 
  int   setLine( int line, ZZBMarker *m, char *txt, int size );
  
  int   getLastLine() { return last_line; }
  
  int   getLineSize( int line );
  void  setLineSize( int line, int size );
  
  int   isDirty  ( int i );
  void  setDirty ( int i );
  void  clean    ( int i );
  
  void  move_bulge    ( int index, int d );
  void  move_bulge_at ( int coord, int d ); 
  
  int   new_bulge ( int line ); // returns index
  int   new_bulge ( int center, int big, int above, int below, int ascent, int descent ); 
  
  void bulge(int i);
};

#endif



