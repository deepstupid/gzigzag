/*
 *
 *
 */
#include "ZZBSizes.h"

ZZBSizes::ZZBSizes(){
  
  lineCoord[0] = new int[1024];  // coor by the #line 
  lineSize[0]  = new int[1024];  // size by the #line
  line_tables  = 1;
  coord_tables = 1;
  
}

ZZBSizes::~ZZBSizes(){}

int ZZBSizes::setLineCoord(int y, int line ){
  
  if ( line < 0 ) line = 0;
  if ( y    < 0 ) y    = 0;
  
  int table = (int) floor(line/1024);
  //printf("table: %d, line: %d", table,line);
  
  while ( table > line_tables ) {
    lineCoord [line_tables++] = new int[1024];
    if ( ! lineCoord[line_tables] ) return 2;  
  }
  
  lineCoord [ table ] [ line - table *1024 ] =  y;
  
}

int ZZBSizes::setLineSize(int line, int size){
  
  if ( line < 0 ) line = 0;
  if ( size < 0 ) size = 0;
  
  int table = (int) floor(line/1024);
  //printf("table: %d, line: %d", table,line);
  
  while ( table > coord_tables ) {
    lineSize [coord_tables++] = new int[1024];
    if ( ! lineSize[coord_tables] ) return 2;  
  }
  
  lineSize [ table ] [ line - table *1024 ] =  size;
  
}

int ZZBSizes::setLine(int line, int size, int coord){
  
  if ( line  < 0 ) line  = 0;
  if ( size  < 0 ) size  = 0;
  if ( coord < 0 ) coord = 0;
  
  int table = (int) floor(line/1024);
  //printf("table: %d, line: %d", table,line);
  
  while ( table > line_tables ) {
    lineSize [line_tables++] = new int[1024];
    if ( ! lineSize[line_tables] ) return 2;  
  }
  while ( table > coord_tables ) {
    lineCoord [coord_tables++] = new int[1024];
    if ( ! lineCoord[ coord_tables ] ) return 2;  
  }
  lineSize  [ table ] [ line - table *1024 ] =  size;
  lineCoord [ table ] [ line - table *1024 ] =  coord;
  
}


double ZZBSizes::getSize(int line, double &y){
  
    
}

int    ZZBSizes::getLine(double y){} // Get line index at y-coordinate




