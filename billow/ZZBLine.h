/*
 * ZZBLine.h
 * Vesa Parkkinen
 * Fri Nov 26 17:02:01 GMT 1999
 */
#include "ZZBMarker.h"
#include <gtk/gtk.h>

class ZZBLine
{
  
  char *txt;
  
  ZZBMarker *mark;
  
  //int size;
  
  int dirty; 

 public:
  
  ZZBLine( char *t, ZZBMarker *m) { 
    txt = g_strdup(t);
    //txt = t;
    mark = m; 
    //size = s;
    dirty = 1;
  }
  
  ~ZZBLine(){ }
  
  
  //char *getString(ZZBMarker *m);
  char *getString(ZZBMarker *m){
  // support for markers later.
  return txt;
}


  
  //int getSize() { return size; }
  //void setSize(int s ) { 
  //  if  ( s < 0 ) s = 0;
  // //if( size == s ) return;  
  //  size = s; 
  //  dirty = 1;
    //g_print ("setSize: %d\n",size);
  //} 
  void clean()  { dirty = 0;    }
  int  isDirty(){ return dirty; }
  void setDirty(){ dirty = 1; }
};




