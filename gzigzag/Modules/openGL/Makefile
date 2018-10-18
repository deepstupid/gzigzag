# IBMCFLAGS=-Dint64_t="long long" -Dlinux -g
IBMCFLAGS=-Dlinux -g

JAVAC=javac
CC=g++
#CC=/usr/bin/gcc
OFLAGS= -Wall  # -O2 -ffast-math -fexpensive-optimizations 
INCLUDES= -I/usr/local/java/include -I/usr/local/java/include/linux -I/usr/X11R6/include
LIBS= -L/usr/X11R6/lib -lm -lGL -lGLU -lglut -lX11 -lXext -lXmu -lXt -lXi\
 -lSM -lICE -lgltt
#  -lSM -lICE -liconv -lgltt
lib: 
	$(CC) $(IBMCFLAGS) -shared ZZOpenGLGraphics.cc $(INCLUDES) $(LIBS) \
	 -o libzz3d.so $(OFLAGS) 
java:
	$(JAVAC) *.java
clean:
	-rm *.o *~ *.class javacore.txt
old: 
	$(CC) -shared Scene_Xlib.cc $(INCLUDES) $(LIBS) \
	 -o libzz3d.so $(OFLAGS)

