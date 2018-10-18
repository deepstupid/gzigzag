/*
 *
 *
 */


class ZZBBulge2 {
  
  // temporarily  
 public:
  int center;   
  
  int above;     // how many big lines above
  
  int below;     // how many big lines below
  
  int ascent;    // how many lines we have to ascent 
  
  int descent;   // how many lines we have to descent
  
  int big;       // size of the big lines
  
  int small;     // size of the small ones
 
  
  ZZBBulge2( int c=0, int bi=10, int a=0, int b=0, int asc=2, int dsc=2 ) { 
    center = c;  
    above = a;
    below = b;
    
    ascent  = asc;
    descent = dsc;
    
    big = bi;
    
    small = 2;
  }
  
  
};
