/*
 *
 *
 */

class ZZBSize {

  int size;
  int y;       // y-coord in the window
  
  int dirty;   // should we be repainted
  
 public:
  
  ZZBSize(int s):size(s), dirty ( 1 ){ }

  int getSize() { return size; }
  int getY()    { return y;    }
  
  void setSize(int s ) { if (size == s ) return; 
  size = s; dirty = 1;  }
  void setY(int coord) { y    = coord; }

  void clean()  { dirty = 0;    }
  int  isDirty(){ return dirty; }
  void setDirty(){ dirty = 1; }
  

};
