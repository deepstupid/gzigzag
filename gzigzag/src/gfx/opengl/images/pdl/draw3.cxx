#include <math.h>
#include <iostream>
#include <vector>
#include <GL/glut.h>
#include <unistd.h>
#include <stdio.h>
#include <Gummi.hxx>

using std::vector;
using std::cerr;

namespace Perlin {
#include "perlin.c"
}


#define GLERR { int er = glGetError(); if(er != GL_NO_ERROR) \
		    cout << "ERROR "<<__FILE__<<" "<<__LINE__ \
			<<gluErrorString(er)<<"\n"; \
	    }

using std::cout;

typedef Gummi::Point<float> Pt;
typedef Gummi::Point3<float> ZPt;
typedef Gummi::Vector<float> Vec;
typedef Gummi::Vector3<float> ZVec;

void glVertex(const Pt &v) {
    glVertex2f(v.x, v.y);
}

void glVertex(const Pt &v, float f) {
    glVertex3f(v.x, v.y, f);
}

#define EPS 0.000001

/** Intersect a line segment with a half-segment.
 */
bool intersectSegment(Pt s1, Pt s2, Pt from, Vec dir, Pt &inters) {

    Vec v1 = (s1 - from).normalize();
    Vec v2 = (s2 - from).normalize();
    Vec v = dir.normalize();

    float d1 = v1.dot(v);
    float d2 = v2.dot(v);
    float d = v1.dot(v2);

    bool drawGL = true;
    if(d <= d1 && d <= d2) {
	if(drawGL) {
	    glPushAttrib(GL_ENABLE_BIT);
	    glDisable(GL_TEXTURE_1D);
	    glDisable(GL_DEPTH_TEST);
	    glColor3f(1, 1, 0);
	    glBegin(GL_LINES);
	    glVertex(s1);
	    glVertex(s2);
	    glVertex(from);
	    glVertex(from + 200 * v);
	}
	// Hit
	float div = 1.0 / (d - d1 * d2);
	float m1 = (1-d2*d2) / (d1 - d * d2);
	float m2 = (1-d1*d1) / (d2 - d * d1);
	float mt1 = m1 / (s1-from).length();
	float mt2 = m2 / (s2-from).length();
	float mult = m1 + m2;
	float multt = mt1 + mt2;
	inters = s1 + mt2 /multt * (s2 - s1);
	if(drawGL) {
	    glVertex(inters + Vec(-30, -30));
	    glVertex(inters + Vec(30, 30));
	    glVertex(inters + Vec(-30, 30));
	    glVertex(inters + Vec(30, -30));
	}
	// cout << "Inters: "<<s1<<" "<<s2<<" "<<from<<" "<<dir<<" "<<inters<<"\n";
	Vec check = inters - from;
	// cout << d1 << " " << d2 << " " << d << " " << m1 <<" "<<m2<<"\n";
	// cout << (m1 * v1 + m2 * v2)<<"\n";
	// cout << ((m1/mult) * v1 + (m2/mult) * v2)<<"\n";
	// cout << ((m1/mult) * s1.x + (m2/mult) * s2.x)<<"\n";
	// cout << "CHECK " << check.dot(v) << " " << check.length()<<"\n";
	if(check.dot(v) < 0) return false;
	/*
	if(fabs(check.dot(v) - check.length()) > 0.2)
	    abort();
	    */
	if(drawGL) {
	    glEnd();
	    glPopAttrib();
	}
	return true;
    }
    return false;
}


// Assuming from is inside the figure...
struct Shape {
    virtual void intersect(const Pt &from, const Vec &dir, Pt &inters, Vec &tangent) = 0;
    virtual void draw() = 0;
    virtual ~Shape() { }
};

struct Rectangle : public Shape {
    Rectangle(float x, float y) : lr(x, y) { }
    Pt lr; // lower right corner.
    virtual void intersect(const Pt &from, const Vec &dir0, Pt &inters, Vec &outnormal) {
	Vec dir = dir0;
	if(dir.x == 0) dir.x = EPS;
	if(dir.y == 0) dir.y = EPS;
	if(dir.x > 0) {
	    float y = from.y + dir.y * (lr.x - from.x) / dir.x;
	    if(fabs(y) <= lr.y) {
		inters.x = lr.x; inters.y = y;
		outnormal.x = 1; outnormal.y = 0;
		return;
	    }
	} else {
	    float y = from.y + dir.y * (-lr.x - from.x) / dir.x;
	    if(fabs(y) <= lr.y) {
		inters.x = - lr.x; inters.y = y;
		outnormal.x = -1; outnormal.y = 0;
		return;
	    }
	}
	if(dir.y > 0) { 
	    float x = from.x + dir.x * (lr.y - from.y) / dir.y;
	    inters.x = x; inters.y = lr.y;
	    outnormal.x = 0; outnormal.y = 1;
	    return;
	} else {
	    float x = from.x + dir.x * (-lr.y - from.y) / dir.y;
	    inters.x = x; inters.y = -lr.y;
	    outnormal.x = 0; outnormal.y = -1;
	    return;
	}
    }
    virtual void draw() {
	glBegin(GL_QUAD_STRIP);
	corner(-1, -1);
	corner(-1, 1);
	corner(1, 1);
	corner(1, -1);
	corner(-1, -1);
	glEnd();
	glBegin(GL_QUADS);
	corner2(-1, -1);
	corner2(-1, 1);
	corner2(1, 1);
	corner2(1, -1);
	glEnd();
    }
    void corner(int dx, int dy) {
	Pt v = Pt(lr.x * dx, lr.y * dy);
	Vec o = Vec(10*dx, 10*dy);
	Pt v1 = v + o;
	Pt v2 = v - o;
	glTexCoord1f(0);
	glVertex(v1, 0);
	glTexCoord1f(1);
	glVertex(v2, 10);
    }
    void corner2(int dx, int dy) {
	Pt v = Pt(lr.x * dx, lr.y * dy);
	Vec o = Vec(10*dx, 10*dy);
	Pt v2 = v - o;
	glTexCoord1f(1);
	glVertex(v2, 10);
    }
};

struct Polyline : public Shape {
    vector<Pt> points;
    vector<Vec> norms;
    Polyline(Pt *pts, int npoints) : points() { 
	for(int i=0; i<npoints; i++)
	    points.insert(points.end(), pts[i]);
	points.insert(points.end(), points[0]);
	points.insert(points.end(), points[1]);
	points.insert(points.end(), points[2]);
	norms.reserve(points.size());
	for(int i=0; i<points.size()-2; i++) {
	    Pt prev = points[i];
	    Pt cur = points[i+1];
	    Pt next = points[i+2];
	    Vec v1 = (prev - cur).normalize();
	    Vec v2 = (next - cur).normalize();
	    Vec norm1 = v2.cw90();
	    Vec norm = (v2+v1);
	    norm *= 1 / norm.dot(norm1);
	    norms[i+1] = norm;
	}
    }
    void intersect(const Pt &from, const Vec &dir0, Pt &inters, Vec &outnormal) 
    {
//	cout << "IntersectTriangle "<<from<<" "<<dir0<<"\n";
	for(int i=0; i<points.size()-1; i++) {
	    Pt prev = points[i];
	    Pt cur = points[i+1];
	    if(intersectSegment(prev, cur, from, dir0, inters)) {
		outnormal = (cur-prev).cw90();
		outnormal = (1/outnormal.length()) * outnormal;
		if(outnormal.dot(dir0) < 0) continue;
		return;
	    }
	}
	cerr << "Intersectio not found"<<from<<" \n";
	inters = from;
	outnormal = dir0;
    }
    virtual void draw() {
	glBegin(GL_QUAD_STRIP);
	for(int i=0; i<points.size()-2; i++) {
	    Pt cur = points[i+1];
	    Vec norm = norms[i+1];
	    glTexCoord1f(0);
	    glVertex(cur + 10 * norm);
	    glTexCoord1f(1);
	    glVertex(cur - 10 * norm, 10);
	}
	glEnd();
	glBegin(GL_TRIANGLE_FAN);
	glTexCoord1f(1);
	glVertex3f(0, 0, 10);
	for(int i=0; i<points.size()-2; i++) {
	    Pt cur = points[i+1];
	    Vec norm = norms[i+1];
	    glVertex(cur - 10 * norm, 10);
	}
	glEnd();
    }
};

struct TranslatedShape : public Shape {
    Shape *s;
    Vec offs;
    TranslatedShape(Shape *s, Vec offs) :
	    s(s), offs(offs) { }
    void intersect(const Pt &from, const Vec &dir0, Pt &inters, Vec &outnormal) 
    {
	glPushMatrix();
	glTranslatef(offs.x, offs.y, 0);
	Pt nfrom = from - offs;
	s->intersect(nfrom, dir0, inters, outnormal);
	inters = inters + offs;
	glPopMatrix();
    }
    void draw() {
	glPushMatrix();
	glTranslatef(offs.x, offs.y, 0);
	s->draw();
	glPopMatrix();
    }
    Pt trans(Pt p) { return p + offs; }
};

struct Connector {
    void draw(TranslatedShape*s1, Pt p1, TranslatedShape *s2, Pt p2) {
	Pt p1t = s1->trans(p1);
	Pt p2t = s2->trans(p2);
	Vec v = 0.5*(p2t-p1t);
	draw(s1, p1t, v);
	draw(s2, p2t, -v);
    }
    virtual void draw(Shape *s, Pt from, Vec to) = 0;
};
struct NoneConnector : public Connector {
    void draw(Shape *s, Pt from, Vec to) {
	Vec turn = to.cw90();
	turn = (20.0/turn.length()) * turn;
	glBegin(GL_QUAD_STRIP);
	glTexCoord1f(0);
	glVertex(from + turn, 0);
	glVertex(from + turn + to, 0);
	glTexCoord1f(1);
	glVertex(from, 10);
	glVertex(from + to, 10);
	glTexCoord1f(0);
	glVertex(from - turn, 0);
	glVertex(from - turn + to, 0);
	glEnd();
    }
};

void bez4(Pt *ctrl, Pt *curve, int npts) {
    Vec v1 = ctrl[1] - ctrl[0];
    Vec v2 = ctrl[2] - ctrl[0];
    Vec v3 = ctrl[3] - ctrl[0];
    for(int i=0; i<npts; i++) {
	double c = i / (double)(npts-1);
	double c0 = (1-c) * (1-c) * (1-c);
	double c1 = 3 * c * (1-c) * (1-c);
	double c2 = 3 * c * c * (1-c);
	double c3 = c * c * c;
	curve[i] = ctrl[0] + c1 * v1 + c2 * v2 + c3 * v3;
    }
}

struct BezierConnector : public Connector {
#define NPTS 200
    void side(Shape *s, Pt from, Vec to, Vec turn) {
	// Find intersections
	Pt middleinters;
	Vec middlenormal;
	s->intersect(from + 10 * turn, to, middleinters, middlenormal);

	Pt inters;
	Vec normal;
	Pt edgefrom = from + 60*turn;
	s->intersect(edgefrom, to, inters, normal);
	float len = (inters-edgefrom).length();
	float tl = to.length();
	to = (1/tl) * to;

	Pt ctrl[4];
	ctrl[0] = inters;
	ctrl[1] = middleinters;
	ctrl[2] = middleinters;
	ctrl[3] = from + (len + 85) * to + 10 * turn;

	Pt bez[NPTS];
	bez4(ctrl, bez, NPTS);

	    
	glBegin(GL_QUAD_STRIP);
	glTexCoord1f(0);
	glVertex(bez[0] + 10 * normal);

	glTexCoord1f(1);
	glVertex(bez[0] - 10 * normal, 10);

	int dnormal;
	if((bez[2] - bez[0]).cw90().dot(normal) < 0)
	    dnormal = -1;
	else
	    dnormal = 1;

	for(int i=1; i<NPTS-1; i++) {
	    Vec normal;
	    normal = (bez[i+1] - bez[i-1]).cw90();
	    normal = (1/normal.length()) * normal;
	    glTexCoord1f(0);
	    glVertex(bez[i] + dnormal * 10 * normal);
	    glTexCoord1f(1);
	    glVertex(bez[i] - dnormal * 10 * normal, 10);
	}

	glTexCoord1f(0);
	glVertex(from + (len + 85) * to + 20 * turn);
	glTexCoord1f(1);
	glVertex(from + (len + 85) * to, 10);

	glTexCoord1f(0);
	glVertex(from + tl * to + 20 * turn);
	glTexCoord1f(1);
	glVertex(from + tl * to, 10);
	glEnd();
	
    }
    void draw(Shape *s, Pt from, Vec to) {
	Vec turn = to.cw90();
	turn = (1/turn.length()) * turn;
	side(s, from, to, turn);
	side(s, from, to, -turn);
    }
};


Pt offsets[NPTS];
bool inited = false;
void initoffsets() {
    Pt ctrl[4];
    ctrl[0] = Pt(1, 0);
    ctrl[1] = Pt(0, 0);
    ctrl[2] = Pt(0, 0.5);
    ctrl[3] = Pt(0, 1);
    bez4(ctrl, offsets, NPTS);
}

struct OffsetConnector : public Connector {
    void side(Shape *s, Pt from, Vec to, Vec turn) {
	Pt middleinters;
	Vec middlenormal;
	s->intersect(from, to, middleinters, middlenormal);
	float middlel = (middleinters - from ) .length();


	Pt inters; Vec normal;

	s->intersect(from + 60 * turn, to, inters, normal);
	Vec tangent = normal.cw90();

	Pt points[NPTS];
	float tl = to.length();
	to = (1/tl) * to;
	for(int i=0; i<NPTS; i++) {
	    Pt inters; Vec normal;
	    float dx = offsets[i].x;
	    float dy = offsets[i].y;
	    Pt nfrom = from + (10 + 50 * dx) * turn;
	    s->intersect(nfrom, to, inters, normal);

	    Vec towards = to;
	    float dot = normal.dot(towards);
	    if(dot < 0) {
		towards = towards - 100 * dot * normal;
	    } 
	    points[i] = inters + towards * 80 * dy;
	}

	int dnormal;
	if((points[2] - points[0]).cw90().dot(normal) < 0)
	    dnormal = -1;
	else
	    dnormal = 1;
	//	glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

	glBegin(GL_QUAD_STRIP);

	glTexCoord1f(0);
	glVertex(points[0] + 10 * normal);

	glTexCoord1f(1);
	glVertex(points[0] - 10 * normal, 10);

	for(int i=1; i<NPTS-1; i++) {
	    Vec normal;
	    normal = (points[i+1] - points[i-1]).cw90();
	    normal = (1/normal.length()) * normal;
	    glTexCoord1f(0);
	    glVertex(points[i] + dnormal * 10 * normal);
	    glTexCoord1f(1);
	    glVertex(points[i] - dnormal * 10 * normal, 10);
	}

	glTexCoord1f(0);
	glVertex(from + (middlel + 80) * to + 20 * turn);
	glTexCoord1f(1);
	glVertex(from + (middlel + 80) * to, 10);

	glTexCoord1f(0);
	glVertex(from + tl * to + 20 * turn);
	glTexCoord1f(1);
	glVertex(from + tl * to, 10);
	glEnd();

	// Then, the inside of the wide part

	glBegin(GL_QUAD_STRIP);
	glTexCoord1f(1);

	

	for(int i=1; i<NPTS-1; i++) {
	    Vec normal;
	    normal = (points[i+1] - points[i-1]).cw90();
	    normal = (1/normal.length()) * normal;
	    Pt curpoint = points[i] - dnormal * 10 * normal;
	    glVertex(curpoint, 10);
	    glVertex(from + (middlel + 80 * offsets[i].y)*to, 10);
	}
	glEnd();
    }
    void draw(Shape *s, Pt from, Vec to) {
	if(!inited)
	    initoffsets();
	Vec turn = to.cw90();
	turn = (1/turn.length()) * turn;
	side(s, from, to, turn);
	side(s, from, to, -turn);
    }
};

struct Err {};

// ============================================================
// ============================================================

TranslatedShape *shape = new TranslatedShape(new Rectangle(150, 100), Vec(0,0));
TranslatedShape *shape2 = 0;
Connector *conn = new OffsetConnector();
Connector *conn2 = new BezierConnector();

double offs = 0;

void display() {
    if(!shape2) {
	Pt pts[3] = {
	    Pt( 0, 150 ),
	    Pt( -150, -100), 
	    Pt( 150, -100)
	};
	shape2 = new TranslatedShape(new Polyline(pts, 3), Vec(300, 300));
    }

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glColor4f(1, 1, 1, 1);

    glEnable(GL_BLEND);
    glEnable(GL_ALPHA_TEST);
    glAlphaFunc(GL_GREATER, 0.1);
    // glEnable(GL_DEPTH_TEST);

    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


    GLERR

	glPushMatrix();

	glTranslatef(500, 500,0);
	// glRotatef(offs * 10, 0, 0, 1);

	glColor4f(1, 1, 1, 1);

	shape2->offs = Vec(Vec(500 * sin(offs / 5), 500 * cos(offs/5)));
    GLERR
	// glDisable(GL_TEXTURE_1D);
	shape->draw();
    GLERR
	shape2->draw();
    GLERR

	//glColor4f(1, 0.5, 0.5, 1);
    GLERR

	conn->draw(shape, Pt(0,0), shape2, Pt(0, 0));
    GLERR

	glPopMatrix();

	// glAccum(GL_ACCUM, 1.0/8);


    offs += 0.01;

    // glAccum(GL_RETURN, 1.0);

    GLERR
    // glEnable(GL_TEXTURE_3D);

    glFlush();

    glutSwapBuffers();
    glFlush();

    GLERR
}

void reshape(int w, int h) {
    glViewport(0, 0, w, h);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrtho(0, w, 0, h, -100, 100);
    // glTranslatef(0, h, 0);
    // glScalef(1, -1, 1);
    glMatrixMode(GL_MODELVIEW);
}

    int c;

void init() {

    glClearColor(0, 0.2, 0.2, 1);

    /*
    for(int i=0; i<TEXSIZE * TEXSIZE * TEXSIZE; i++) {
	((char *)turb)[i] = i % 255;
    }
    */

    // Texture unit 0 ----------------------------------------------------


    glEnable(GL_TEXTURE_1D);
    GLERR
    glBindTexture(GL_TEXTURE_1D, 1); // the cell edge

    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAX_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_BASE_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_LOD, 0);
    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAX_LOD, 0);

    glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_CLAMP);
    glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
    // glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
   
    GLERR
	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);


#define ETSIZE 1024
    unsigned char edgetex[ETSIZE][4];
    for(int i=0; i<ETSIZE; i++) {
	double c = i / (double)(ETSIZE-1);
	edgetex[i][0] = 255 * ((c > 0.4) & (c < 0.5));
	edgetex[i][1] = 255 * ((c > 0.2) & (c < 0.3));
	edgetex[i][2] = 255 * 1;
	edgetex[i][3] = 255 * (c > 0.1);
    }

    glTexImage1D(GL_TEXTURE_1D, 0, GL_RGBA, 
		    ETSIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE, edgetex);

    GLERR
    glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

}


void idle() { 
    glutPostRedisplay(); 
}

int main(int argc, char **argv) {
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA);
    // glutInitDisplayMode(GLUT_SINGLE | GLUT_RGBA);
    glutInitWindowSize(1000,1000);
    glutInitWindowPosition(0,0);
    glutCreateWindow("fontTest");


    init();

    glutReshapeFunc(reshape);
    glutDisplayFunc(display);
    glutIdleFunc(idle);
    glutMainLoop();
    return 0;
}

