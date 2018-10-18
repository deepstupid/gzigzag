/*
 * ZZBLines.cc
 * Vesa Parkkinen
 * Fri Nov 26 17:02:01 GMT 1999
 */

#include "ZZBLines.h"

ZZBLines::ZZBLines(){
  
  lines[0] = new ZZBLine*[1024];
  sizes[0] = new ZZBSize*[1024];
  
  tables    = 1;  
  last_line = 0;
}

ZZBLines::~ZZBLines() {
  // vapauta taulut
}

inline char *ZZBLines::getLine(int line, ZZBMarker **markers){
  // markers to be implemented
  if (line < 0 || line > last_line ) return "virhe! ";
  int table = (int) floor(line/1024);
  
  if ( table > tables ) return "virhe! ";
  
  return  lines[ table ] [ line - table * 1024 ]->getString(NULL);
  //return lines[line]->getString(NULL);
}

inline int ZZBLines::setLine(int line, ZZBMarker *m, char *txt, int size ){
  
  if( line < 0 ) return 1;
  
  int table = (int) line/1024;
  //printf("table: %d, line: %d", table,line);  
  
  while ( table >= tables ){ 
    lines [tables++] = new ZZBLine*[1024];
    sizes [tables++] = new ZZBSize*[1024];
  }
  
  ZZBLine *l = new ZZBLine(txt, m);
  
  ZZBSize *s = new ZZBSize(size);
  // tarkistukset
  lines[ table ] [ line - table *1024 ] =  l;
  sizes[ table ] [ line - table *1024 ] =  s;
  if ( line > last_line  ) last_line = line;
}

inline int ZZBLines::getLineSize(int line){
  if ( line < 0|| line > last_line ) return 0;
  int table = (int)line/1024;
  
  if ( table > tables ) return 1;
  return  sizes[ table ] [ line - table * 1024 ]->getSize();
  
}

inline void ZZBLines::setLineSize(int line, int size){
  if ( line < 0  || line > last_line ) return;
  int table = (int)line/1024;
  
  if ( table > tables ) return ;
  //g_print("SetSize: line %d, size %d \n", line, size);
  return  sizes[ table ] [ line - table * 1024 ]->setSize(size);
}


inline int ZZBLines::isDirty(int i){
  if ( i < 0 || i > last_line ) return 0; 
  int table = (int) i/1024;
  
  if ( table > tables ) return 0;
  
  return  sizes[ table ] [ i - table * 1024 ]->isDirty();
  
}

inline void ZZBLines::clean(int i){
  if ( i < 0 || i > last_line ) return; 
  int table = (int) i/1024;
  
  if ( table > tables ) return ;
  
  sizes[ table ] [ i - table * 1024]->clean();
  
}

inline void ZZBLines::setDirty(int i){
  if ( i < 0 || i > last_line ) return ; 
  int table = (int) i/1024;
  
  if ( table > tables ) return ;
  sizes[ table ] [ i - table * 1024 ]->setDirty();
  
}

inline void  ZZBLines::move_bulge ( int index, int d  ) { 
  if ( index > num_bulges ) return ;
  if ( bulges[index] == NULL ) return ;
  
  //  if( d > 10 ) d = 10;
  //  if( d < -10 ) d = -10;
  
  ZZBBulge2 *b= bulges[index];
  
  int limit = b->center + b->below + b->descent;
  
  for( int i = b->center - b->above - b->ascent; i <= limit ; i++ ) 
    setLineSize(i, b->small);
  
  //for (int i = 0; i < last_line ; i++)
  //  setDirty(i);
  
  b->center = b->center + d;
  
  bulge( index );
  
}

void  ZZBLines::move_bulge_at ( int coord, int d ) { 
  
}

int   ZZBLines::new_bulge ( int line ) { 
  bulges[num_bulges] = new ZZBBulge2(line);
  //bulges[num_bulges]->
  bulge(num_bulges);
  // bulge  
  return num_bulges++;
}

int   ZZBLines::new_bulge ( int center, int big, int above, int below, int ascent, int descent ){ 
  bulges[num_bulges] = new ZZBBulge2(center, big, above, below, ascent, descent);
  //bulges[num_bulges]->bulge();
  bulge(num_bulges);
  return num_bulges++; 
}

inline void ZZBLines::bulge(int i){
  //g_print ( "bulge \n");
  if ( i > num_bulges    ) return ;
  if ( bulges[i] == NULL ) return ;
  
  
  ZZBBulge2 *b= bulges[i];
  
  int limit = b->center + b->below + b->descent;
  
  for ( int j = b->center - b->above - b->ascent; j <limit; j++ ) 
    setLineSize(j, b->small);
  
  int diff = (b->big - b->small) / b->ascent;
  
  limit = b->center - b->above;
  
  for ( int j = b->center - b->above - b->ascent, k=0; j <= limit; j++, k++)   
    setLineSize( j , b->small + k*diff );
  
  limit = b->center + b->below + b->descent;
  
  diff = (b->big - b->small) / b->descent;
  
  for ( int j = b->center + b->below , k=0; j <= limit; j++, k++)   
    setLineSize( j , b->big - k*diff );
  
  setLineSize( b->center + b->below + b->descent , b->small);
  
  limit = b->center + b->below;
  
  for ( int j = b->center - b->above; j <= limit; j++)   
    setLineSize( j , b->big );
  
}













