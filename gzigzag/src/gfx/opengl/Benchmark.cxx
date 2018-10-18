#include <GL/glut.h>
#include <algorithm>
#include <vector>
#include <map>
#include <string>
#include <cstdio>
#include <iostream>

using namespace std;

typedef map<string, float> Sizes;

char buffer[2000];

struct Benchmark {
    virtual void start() = 0;
    virtual void round() = 0;
    virtual void finish() = 0;
    virtual Sizes sizesPerRound() = 0;
    virtual string descr() = 0;
};

struct GLMode_None {
    void start() { }
    void finish() { }
    const char *cname() { return ""; }
};

struct GLTransform_None {
    void apply() { 
	glLoadIdentity(); 
    }
    const char *cname() { return ""; }
};

struct GLTransform_Rotate {
    int r;
    GLTransform_Rotate() : r(1) { }
    void apply() { 
	glRotatef(r, 0, 0, 1);
	r = -r;
    }
    const char *cname() { return "rotate"; }
};

struct GLTransform_Translate {
    int r;
    GLTransform_Translate() : r(1) { }
    void apply() { 
	glTranslatef(r, 0, 0);
	r = -r;
    }
    const char *cname() { return "rotate"; }
};

struct GLPerVertex_None {
    void apply(int i) { }
    const char *cname() { return ""; }
};

template <class GLMode, class GLTransform, class GLPerVertex>
struct Quads : public Benchmark {
    int n, w, h;
    GLMode glmode;
    GLTransform gltransform;
    GLPerVertex glpervertex;
    Quads(int n, int w, int h) : n(n), w(w), h(h) {
    }
    void start() {
	glmode.start();
    }
    void round() {
	gltransform.apply();
	glBegin(GL_QUADS);
	for(int i=0; i<n; i++) {
	 glpervertex.apply(0);
	 glVertex2i(10, 10);
	 glpervertex.apply(1);
	 glVertex2i(10+w, 10);
	 glpervertex.apply(2);
	 glVertex2i(10+w, 10+h);
	 glpervertex.apply(3);
	 glVertex2i(10, 10+h);
	}
	glEnd();
    }
    void finish() {
	glmode.finish();
    }
    Sizes sizesPerRound() {
	Sizes s;
	s["Quads"] = n;
	s["Pixels"] = n * w * h;
	s["Vertices"] = 4 * n;
	return s;
    }
    string descr() {
	sprintf(buffer, "Quads-%s-%s-%s %d %d %d", glmode.cname(), gltransform.cname(), glpervertex.cname(), n, w, h);
	return string(buffer);
    }
};

typedef vector<Benchmark*> Benches;

Benches makeBenches() {
    Benches b;
    for(int s = 1; s < 50; s += 2)
	b.push_back(new Quads<GLMode_None, GLTransform_None, GLPerVertex_None>(1, s, s));
    for(int s = 1; s < 50; s += 2)
	b.push_back(new Quads<GLMode_None, GLTransform_Rotate, GLPerVertex_None>(1, s, s));
    for(int s = 1; s < 50; s += 2)
	b.push_back(new Quads<GLMode_None, GLTransform_Translate, GLPerVertex_None>(1, s, s));
	// b.push_back(new Quads(50, s, s));
    return b;
};


/* We run all the benchmarks here...
 */
void display() {
    Benches b = makeBenches();
    for(Benches::iterator i = b.begin(); i != b.end(); i++) {
	int time = 0;
	int iter;
	for(iter = 16; time < 200; iter *= 2) {
	    (*i)->start();
	    glFinish();
	    int t0 = glutGet(GLUT_ELAPSED_TIME);
	    for(int r = 0; r < iter; r ++) (*i)->round();
	    glFinish();
	    (*i)->finish();
	    int t1 = glutGet(GLUT_ELAPSED_TIME);
	    time = t1 - t0;
	}
	double secpr = time * 0.001 / iter;
	cout << (*i)->descr() << " " << secpr << "\n" << flush;
    }
    exit(0);
}

void init() {
    glClearColor(0, 0, 0, 0);
}

void reshape(int w, int h) {
    glViewport(0, 0, w, h);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluOrtho2D(0, w, 0, h);
    glTranslatef(0, h, 0);
    glScalef(1, -1, 1);
    glMatrixMode(GL_MODELVIEW);
}


int main(int argc, char **argv) {
    
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA);
    glutInitWindowSize(600,600);
    glutInitWindowPosition(0,0);
    glutCreateWindow("bench");
    init();


    glutReshapeFunc(reshape);
    glutDisplayFunc(display);
    // glutIdleFunc(idle);
    glutMainLoop();
    return 0;
}
