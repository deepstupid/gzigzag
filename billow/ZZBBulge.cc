/*
 * ZZBBulge.cc
 * Vesa Parkkinen
 * Sun Nov 28 14:50:02 EET 1999
 */
#include "ZZBBulge.h"

ZZBLines *ZZBBulge::bulge(int bulge_mid_line ) {
  
  if ( bulge_mid_line < 0 ) bulge_mid_line = 0;
  if ( lines == NULL      ) return NULL;
  
  bulge_center = bulge_mid_line;
  
  lines->setLineSize(bulge_center, big_size);
  
  for (int i = 1; i < big_lines; i++){
    lines->setLineSize(bulge_center + i, big_size); 
    lines->setLineSize(bulge_center - i, big_size); 
  }
  
  double diff = ( big_size - small_size ) / trans_lines;
  
  for ( int i = 0; i < trans_lines; i++ ){
    lines->setLineSize( bulge_center + big_lines + i, big_size - diff*i );
    lines->setLineSize( bulge_center - big_lines - i, big_size - diff*i );
  }
  return lines;

}

ZZBLines *ZZBBulge::moveBulge(int dy){
  int diff = dy ;
  if ( dy < 0 ){
    for ( int i = 0; i < -diff; i++ ) {  
      // the space for other lines have changed, we have to calculate all lines
      if (  bulge_center - big_lines - trans_lines < 0 ) {
	int ll   = lines->getLastLine(); 
	for ( int j = 0; j < ll; j++ ) lines->setDirty(j);
      }
      lines->setLineSize(  bulge_center + big_lines + trans_lines , small_size );
      
      bulge(bulge_center - 1);
    }
  }
  
  else if ( dy > 0 ) { 
    for ( int i = 0; i < diff; i++ ) {
      // the space for other lines have changed, we have to calculate all lines
      if ( bulge_center - big_lines - trans_lines < 0 ) {
	int ll   = lines->getLastLine(); 
	for ( int j = 0; j < ll; j++ ) lines->setDirty(j);
      }
      
      lines->setLineSize( bulge_center - big_lines - trans_lines, small_size );
      bulge(bulge_center + 1);
    }
  }
  return lines;
}








