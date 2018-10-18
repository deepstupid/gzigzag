#include "ZZBMarker.h"
class Lines {
 public:
        // Get the string on a line
        // If rendered, put the markers coordinates here.
  virtual char *getLine(int line, ZZBMarker **marks) = 0;
  virtual 	~Lines() { };
};
