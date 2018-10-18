#ifndef __ZZBBULGE_H__
#define __ZZBBULGE_H__
/*
 * ZZBBulge.h
 * Vesa Parkkinen
 * Sun Nov 28 14:50:02 EET 1999
 */
#include <glib.h>
#include "ZZBLines.h"
class ZZBBulge {
  
  int small_size;
  int big_size;
  int big_lines; // or bulge_size
  int trans_lines;
  
   int bulge_center;
  ZZBLines *lines;
  
 public:
  
  ZZBBulge(ZZBLines *l, int small, int trans, int big, int count, int bc){ 
    setSmall(small);
    setTransLines(trans);
    setBig(big);
    setBigLines(count);
    if ( bc < 0 ) bc = 0; 
    bulge_center     = bc;
    lines            = l;
    bulge(bc);
  }
  
  ~ZZBBulge() { } 
  
  // bulges ( ? ) lines in given line 
  ZZBLines *bulge( int bulge_mid_line );
  ZZBLines *moveBulge(int dy);
  
  int getSmall()      { return small_size;  }
  int getBig()        { return big_size;    }
  int getBigLines()   { return big_lines ;  }
  int getTransLines() { return trans_lines; }
  
  // XXX miksi ihmeessä ei unsigned sitten??
  // Pitempi kirjoittaa !
  // Helpompi muuttaa, jos sattuu haluamaan muuttaa muuttujien tulkintaa ...
  // Tottumus ...
  // riittääk
  void setSmall(int small)      {  if ( small < 0 ) small = 0; small_size  = small; }
  void setBig(int big)          {  if ( big   < 0 ) big   = 0; big_size    = big;   }
  void setBigLines(int count )  {  if ( count < 0 ) count = 0; big_lines   = count; }
  void setTransLines(int lines) {  if ( lines < 0 ) lines = 0; trans_lines = lines; }
  
};

#endif
