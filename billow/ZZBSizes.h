/*
 * ZZBSizes.h
 * Vesa Parkkinen
 * Fri Nov 26 17:02:01 GMT 1999
 */
#include "BillowSizes.h"
#include <math.h>

class ZZBSizes : public BillowSizes {
  
  int *lineCoord[256]; // pointer to coord-line table
  int *lineSize [256]; // pointer to line-size table
  
  int     line_tables;
  int     coord_tables;
  
 public:  
  
  ZZBSizes();
  
  ~ZZBSizes();
  
  int    setLineCoord(int y, int line );
  int    setLineSize(int line, int size);
  int    setLine(int line, int coord, int size);
  double getSize(int line, double &y);
  int    getLine(double y); // Get line index at y-coordinate
  // If we need the x index, we re-render (or virtually so)
  
}; 
