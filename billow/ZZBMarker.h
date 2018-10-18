/*
 * ZZBMarker.h
 * Vesa Parkkinen
 * Fri Nov 26 17:51:24 GMT 1999 
 */
#ifndef __XXBMARKER_H__
#define __XXBMARKER_H__
class ZZBMarker {
  
  int xind;
  ZZBMarker *next;
  double w_x; // window coordinates
  double w_y;
  
 public:
  ZZBMarker();
  
  
  
};

#endif

