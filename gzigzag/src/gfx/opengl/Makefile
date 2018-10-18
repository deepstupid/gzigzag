OBJ = testGzzGL.o GZZGL-linux.o GzzGL.o
LIBOBJ = GZZGL-linux.o GzzGL.o GzzGL-jni.o
LIBS = -lfreetype -lGL -lGLU -lglut `gdk-pixbuf-config --libs` -lstdc++
CXX=libtool --mode=compile g++-3.0
CXXLINK=libtool --mode=link g++-3.0

JNIINC=/BIG/java2/IBMJava2-13/include
#JNIINC=/usr/lib/j2sdk1.3/include -I/usr/lib/j2sdk1.3/include/linux

GUMMI=../../../../gummiterm/libgummi


# OPTIMIZE =  -O3 -fomit-frame-pointer -ffast-math 
OPTIMIZE = -O0

CXXFLAGS = -g -Wall -I$(JNIINC) -I$(GUMMI) `gdk-pixbuf-config --cflags` $(OPTIMIZE)
CCFLAGS = -g -Wall

SHARED = -shared

JAVAC=javac
JAVA=java

GUMMIOBJ=$(GUMMI)/GummiFont.lo $(GUMMI)/GummiTexture.lo

all: org_gzigzag_gfx_GZZGL.h libGZZGL.so testGzzGL benchmark

testDist: $(TESTDIST)
#	$(CXX) -static $(CXXFLAGS) -o testDist $(TESTDIST) $(GUMMIOBJ) -lfreetype -lglut  -lGL -lGLU -L/usr/X11R6/lib -lXmu -lXext -lX11 -lpthread -ldl
	$(CXXLINK) $(CXXFLAGS) -o testDist $(TESTDIST) -lfreetype -lglut  -lGL -lGLU -L/usr/X11R6/lib -lX11

testGzzGL: $(OBJ)
	$(CXXLINK) $(CXXFLAGS) -o testGzzGL $(OBJ) $(GUMMIOBJ) $(LIBS) 

benchmark: Benchmark.cxx
	$(CXXLINK) $(CXXFLAGS) -o benchmark Benchmark.cxx -lGL -lGLU -lglut

info: info.c
	$(CC) $(CCFLAGS) -o info info.c -lGL -lGLU -lglut

%.o: %.cxx 
	$(CXX) -c $(CXXFLAGS) -I/usr/include/freetype2 $<

org_gzigzag_gfx_GZZGL.h: GZZGL.java
	$(JAVAC) -classpath $(CLASSES):../../CLASSES -d ../../CLASSES GZZGL.java
	javah -classpath $(CLASSPATH):../../CLASSES org.gzigzag.gfx.GZZGL

GZZGL.java GzzGLRen-jni.cxx: renderables.py
	python renderables.py

libGZZGL.so: GzzGLRen-jni.cxx $(LIBOBJ)
	# $(CXXLINK) -module -no-install -dlopen=self -o GZZGL.la $(SHARED) $(CXXFLAGS) $(LIBOBJ) $(GUMMIOBJ) $(LIBS) 
	gcc-3.0 -rdynamic -fpic -o libGZZGL.so $(SHARED) $(CXXFLAGS) $(LIBOBJ) $(GUMMIOBJ) $(LIBS) 
	# libtool install libGZZGL.la $(PWD)/libs/
	# strace -f ld -o libGZZGL.so $(LIBOBJ) $(GUMMIOBJ) $(LIBS) 

compile-java: org_gzigzag_gfx_GZZGL.h libGZZGL.so
	$(JAVAC) -classpath ../../CLASSES:$(CLASSPATH) -d ../../CLASSES *.java

try: compile-java
	LD_LIBRARY_PATH=. $(JAVA) -cp $(CLASSPATH):../../CLASSES org.gzigzag.gfx.TryGZZGL

psyko: compile-java
	LD_LIBRARY_PATH=. $(JAVA) -cp $(CLASSPATH):../../CLASSES org.gzigzag.gfx.PsykoTest

psyko2: compile-java
	LD_LIBRARY_PATH=. $(JAVA) -cp $(CLASSPATH):../../CLASSES org.gzigzag.gfx.PsykoTest2

