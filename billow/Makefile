CFLAGS=`gtk-config --cflags` -g -O2
# CFLAGS=`gtk-config --cflags` -g 
LDFLAGS=-lImlib2 `gtk-config --libs` -g 
# LDFLAGS=`gtk-config --libs` -g 
CC=g++
LIBS=ZZBLine.o ZZBLines.o ZZBMarker.o ZZBSizes.o main2.o ZZBRenderer.o suckfont.o ZZBBulge.o 

billow: $(LIBS)
	g++ $(LIBS) $(LDFLAGS) -o billow 
old: main.o suckfont.o
	g++ main.o suckfont.o $(LDFLAGS) -o old 
.cc.o:
	g++ -c $(CFLAGS) $*.cc 
clean:
	rm -f *.o billow
