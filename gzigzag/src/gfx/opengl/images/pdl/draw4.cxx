#include <math.h>
#include <iostream>
#include <vector>
#include <GL/glut.h>
#include <Gummi.hxx>
#include <stdlib.h>

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

void glVertex(const ZPt &v) {
    glVertex3f(v.x, v.y, v.z);
}

void glVertex(const Pt &v, float f) {
    glVertex3f(v.x, v.y, f);
}

void glVertex(const ZPt &v, float f) {
    glVertex3f(v.x, v.y, v.z + f);
}

void glNormal(const ZVec &v) {
    glNormal3f(v.x, v.y, v.z);
}

double getZ(Pt p) { return 0; }
double getZ(ZPt p) { return p.z; }

#define EPS 0.000001

#define BEVEL 10
#define BEVELH 10

bool use_lighting = true;

#define NORMOUT 3.5
#define NORMIN 3

double texeps = 0.02;

template<class Pt0>
void drawEdge(vector<Pt0> &points) {
    glBegin(GL_QUAD_STRIP);
    // cout << "Start draawedge\n";
    for(unsigned i=1; i<points.size()-1; i++) {
	Pt prev = Pt(points[i-1]);
	Pt cur = Pt(points[i]);
	double curz = getZ(points[i]);
	Pt next = Pt(points[i+1]);
	Vec v1 = (prev-cur).normalize();
	Vec v2 = (next-cur).normalize();
	Vec unitnormal = (v2-v1).cw90().normalize();
	Vec halfnormal = v2.cw90();
	Vec normal = BEVEL / unitnormal.dot(halfnormal) * unitnormal;
	// cout << "N: "<<v1<<" "<<v2<<" "<<(next-prev).cw90()<<" "<<"\n";
	// cout << "V: "<<cur<<" "<<normal<<"\n";
	if(use_lighting && normal.length() > 1.07 * BEVEL) { // sharp angle
	    Vec n1 = -v1.cw90();
	    Vec n2 = v2.cw90();
	    glNormal(ZVec(n1.x, n1.y, NORMIN).normalize());
	    glTexCoord1f(1-texeps);
	    glVertex(cur,BEVELH + curz);
	    glNormal(ZVec(n1.x, n1.y, NORMOUT).normalize());
	    glTexCoord1f(0);
	    glVertex(cur + normal, curz);

	    glNormal(ZVec(n2.x, n2.y, NORMIN).normalize());
	    glTexCoord1f(1-texeps);
	    glVertex(cur,BEVELH + curz);
	    glNormal(ZVec(n2.x, n2.y, NORMOUT).normalize());
	    glTexCoord1f(0);
	    glVertex(cur + normal, curz);
	} else {
	    glNormal(ZVec(unitnormal.x, unitnormal.y, NORMIN).normalize());
	    glTexCoord1f(1-texeps);
	    glVertex(cur,BEVELH + curz);
	    glNormal(ZVec(unitnormal.x, unitnormal.y, NORMOUT).normalize());
	    glTexCoord1f(0);
	    glVertex(cur + normal, curz);
	}
    }
    glEnd();
}

template<class Pt0>
void drawEdge_line(vector<Pt0> &points) {
    glDisable(GL_TEXTURE_1D);
    glBegin(GL_LINE_STRIP);
    for(int i=1; i<points.size()-1; i++) {
      glVertex(points[i], /*50*/BEVELH);
    }
    glEnd();
    glEnable(GL_TEXTURE_1D);
}

template<class Pt0>
void fillEdge(vector<Pt0> &points, Pt center) {
    glTexCoord1f(1-texeps);
    glNormal3f(0, 0, 1);
    glBegin(GL_TRIANGLE_FAN);
    glVertex(center, BEVELH + 0.5);
    for(unsigned i=1; i<points.size()-1; i++) {
	glVertex(points[i], BEVELH + 0.5);
    }
    glEnd();
}

// A very weird function: go to line if z != 0, otherwise to point
template<class Pt0>
void fillEdge_toLine(vector<Pt0> &points, Pt center, Vec dir) {
    bool dbgcolor = false;
    dir = dir.normalize();
    glTexCoord1f(1-texeps);
    glNormal3f(0, 0, 1);
    if(dbgcolor) glDisable(GL_LIGHTING);
    glBegin(GL_QUAD_STRIP);
    bool hadP = false;
    for(unsigned i=1; i<points.size()-1; i++) {
	if(dbgcolor) glColor3f(1, 0.5 + getZ(points[i]) / 978.0, 0);
	Pt p(points[i]);


	float dot = dir.dot(p-center) ;
	Pt n = center + dot * dir;

	if(getZ(points[i]) != 0) {
	    if(!hadP) {
		glVertex(p, BEVELH + 0.5);
		glVertex(center, BEVELH + 0.5);
		glVertex(p, BEVELH + 0.5);
		glVertex(n, BEVELH + 0.5);
		hadP = true;
	    }
	    glVertex(points[i], BEVELH + 0.5);
	    glVertex(n, BEVELH + 0.5 + getZ(points[i]));
	    // cout << "fillEdge: dot "<<points[i]<<"\n";
	} else {
	    if(hadP) {
		glVertex(p, BEVELH + 0.5);
		glVertex(n, BEVELH + 0.5);
		hadP = false;
	    }
	    glVertex(p, BEVELH + 0.5);
	    glVertex(center, BEVELH + 0.5);
	}
	// cout << "N : "<<n<<"\n";
	if(dbgcolor) glColor3f(1, 1, 1);
    }
    glEnd();
    if(dbgcolor) glEnable(GL_LIGHTING);
}

#define BASE 90.0
#define CTHICK 7.0


#define BETWEEN(a, x, y) (fabs((a)-(x)) <= fabs((x)-(y)) && fabs((a)-(y)) <= fabs((x)-(y)))

/** How much to interpolate towards the connection.
 * Uses a circle.
 */
double conFract(double x) {
    x = fabs(x);
    x -= CTHICK;
    x /= BASE;
    if(x <= 0) return 1;
    if(x >= 1) return 0;
    // return 1-sqrt(sqrt(1-(1-x)*(1-x)*(1-x)*(1-x)));
    // return 1-sqrt(1-((1-x)*(1-x)));
    return 1-pow(1-pow(1-x, 5.0), 1/5.0);
}
double conFract2(double x) {
    x = fabs(x);
    x -= CTHICK;
    x /= BASE;
    if(x <= 0.02) return 1;
    // if(x <= 0.2) return 0.001;
    return 0;
    // if(x >= 1) return 0;
    // return 1-sqrt(sqrt(1-(1-x)*(1-x)*(1-x)*(1-x)));
    // return 1-sqrt(1-((1-x)*(1-x)));
    // return 1-pow(1-pow(1-x, 5.0), 1/5.0);
}

void closePoints(vector<Pt> &points) {
    points.insert(points.end(), points[0]);
    points.insert(points.end(), points[1]);
    points.insert(points.end(), points[2]);
}

Pt findX(double x, Pt p1, Pt p2) {
    return Pt(x, p1.y + (p2.y-p1.y) * (x-p1.x) / (p2.x-p1.x));
}

    void drawConnector(vector<Pt> curve, Pt from, Vec dir, double zdir, bool bevel = true) {

	bool dbg = false;
	if(dbg) glDisable(GL_LIGHTING);

	Vec x, y;
	y = dir.normalize();
	x = y.cw90();
	double l = dir.length();
	vector<Pt> trpt = curve;
	// Transform to coordinate system
	for(unsigned i=0; i<trpt.size(); i++) {
	    Vec v = trpt[i] - from;
	    trpt[i] = Pt(x.dot(v), y.dot(v));
	    // cout << "OrigPoint "<<trpt[i]<<"\n";
	}
	if(dbg) {
	    glLineWidth(3);
	    glColor3f(1, 1, 0);
	    drawEdge(trpt);
	    glLineWidth(1);
	}
	/*
	double minx = 10000; int ind = 0;
	for(int i=1; i<trpt.size()-1; i++) {
	    if(trpt[i].x < minx) {
		minx = trpt[i].x;
		ind = i;
	    }
	}

	rotate

	vector<Pt> npt;
	for(int i=minx-1; i<trpt.size()-1; i++) {
	    if(trpt[i].x > trpt[i-1].x ||
		    trpt[i+1].x > trpt[i].x)
			npt.insert(npt.end(), trpt[i]);
	}
	for(int i=1; i<minx; i++) {
	    if(trpt[i].x > trpt[i-1].x ||
		    trpt[i+1].x > trpt[i].x)
			npt.insert(npt.end(), trpt[i]);
	}
	// Npt now has the points 
	*/
	vector<Pt> actpt(0); 
	if(dbg) {
	    for(unsigned i=0; i<trpt.size(); i++) {
		cout << "Initpt: "<<trpt[i]<<"\n";
	    }
	}
	for(unsigned i=1; i < trpt.size() - 2; i++) {
	    Vec pnormal = (trpt[i] - trpt[i-1]).cw90();
	    Vec nnormal = (trpt[i+1] - trpt[i]).cw90();
	    if(pnormal.y > 0 || nnormal.y > 0) {
		// The x are going downwards...
		if(BETWEEN(CTHICK/2, trpt[i-1].x, trpt[i].x)) {
		    actpt.insert(actpt.end(), findX(CTHICK/2, trpt[i-1], trpt[i]));
		    // cout << "Insert +"<<trpt[i]<<"\n";
		}
		if(BETWEEN(-CTHICK/2, trpt[i-1].x, trpt[i].x)) {
		    actpt.insert(actpt.end(), findX(-CTHICK/2, trpt[i-1], trpt[i]));
		    // cout << "Insert -"<<trpt[i]<<"\n";
		}
	    }
	    actpt.insert(actpt.end(), trpt[i]);
	}
	closePoints(actpt);
	trpt = actpt;
	if(dbg) {
	    for(int i=0; i<trpt.size(); i++) {
		cout << "Actpt: "<<trpt[i]<<"\n";
	    }
	    glColor3f(1, 0, 0);
	    drawEdge(trpt);
	}
	for(int i=1; i<trpt.size()-2; i++) {
	    double f = conFract(trpt[i].x);
	    double f0 = conFract(trpt[i-1].x);
	    if(fabs(f-f0) > 0.01) {
		int n = fabs(f-f0)/0.01 + 1;
		Pt p1 = trpt[i-1];
		Pt p2 = trpt[i];
		Vec offs = p2-p1;
		for(int ins = 1; ins < n; ins++) {
		    double v = ins/(double)n;
		    if(f < f0) 
			v = v*v*v;
		    else
			v = 1-(1-v)*(1-v)*(1-v);
		    trpt.insert(trpt.begin()+i,
				p1 + v * offs);
		    i++;
		}
	    }
	}
	if(dbg) {
	    glColor3f(0, 1, 0);
	    drawEdge(trpt);
	}
	vector<ZPt> zv;
	zv.resize(trpt.size());
	for(unsigned i=1; i<trpt.size()-2; i++) {
	    Vec pnormal = (trpt[i] - trpt[i-1]).cw90();
	    Vec nnormal = (trpt[i+1] - trpt[i]).cw90();
	    if(pnormal.y < 0 || nnormal.y < 0) {
		// cout << "NegNormal "<<trpt[i]<<"\n";
		zv[i] = ZPt(trpt[i], 0);
		continue;
	    }
	    double f = conFract(trpt[i].x);
	    double f2 = conFract2(trpt[i].x);
	    zv[i] = ZPt(trpt[i].x,
			trpt[i].y + f * (l - trpt[i].y),
			f2 * zdir);

	     // cout << "Point "<<trpt[i]<<"\n";
	}
	zv[0] = zv[zv.size()-3];
	zv[zv.size()-2] = zv[1];
	zv[zv.size()-1] = zv[2];
	vector<ZPt> final(zv.size());
	if(dbg) {
	    glColor3f(0, 1, 1);
	    drawEdge(zv);
	}
	ZPt zfrom(from, 0);
	ZVec zx(x, 0);
	ZVec zy(y, 0);
	// cout << "Final:\n";
	for(int i=0; i<zv.size(); i++) {
	    ZPt p = zv[i];
	    final[i] = zfrom + p.x * zx + p.y * zy + ZVec(0, 0, p.z);
	    // cout << final[i] << "\n";
	}
	// glColor3f(1, 1, 1);
	if (bevel) {
	  glBindTexture(GL_TEXTURE_1D, 1); // the cell edge
	  glDisable(GL_TEXTURE_1D);
	  drawEdge(final);
	  glColor3f(1, 0, 0);
	  fillEdge_toLine(final, from, dir);
	} else {
	  glDisable(GL_TEXTURE_1D);
	  glDisable(GL_LIGHTING);
	  glColor3f(1, 1, 1);
	  fillEdge_toLine(final, from, dir);
	  glDisable(GL_LIGHTING);
	  glEnable(GL_TEXTURE_1D);
	  glBindTexture(GL_TEXTURE_1D, 2); // the cell edge
	  drawEdge(final);
	}
    }


/** Subdivide points. Assumes that two points in the end are repeated from
 * the beginning.
 */
vector<Pt> subdivide(vector<Pt> points) {
    vector<Pt> res(0);
    for(unsigned i=1; i<points.size()-2; i++) {
	res.insert(res.end(), points[i] + 0.5 * (points[i-1] - points[i]));
	res.insert(res.end(), points[i]);
    }
    res.insert(res.end(), res[0]);
    res.insert(res.end(), res[1]);
    res.insert(res.end(), res[2]);
    return res;
}

/** Intersect a line segment with a half-segment.
 */
bool intersectSegment(Pt s1, Pt s2, Pt from, Vec dir, Pt &inters) {

    Vec v1 = (s1 - from).normalize();
    Vec v2 = (s2 - from).normalize();
    Vec v = dir.normalize();

    float d1 = v1.dot(v);
    float d2 = v2.dot(v);
    float d = v1.dot(v2);

    bool drawGL = false;
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

bool intersectEdge(vector<Pt> points, Pt from, Vec dir, Pt &inters) {
  for (int i = 0; i < points.size()-1; i++)
    {
      //cout << "Trying to intersect (" << points[i] << " - " << points[i+1] << ") with (" << from << " -> " << dir << ")\n";
      if (intersectSegment(points[i], points[i+1], from, dir, inters))
	return true;
    }
  return false;
}


double offs = 0;

struct Node {
    ZPt center;
    vector<Pt> points;
};

Node makeTriangle(ZPt x, Pt a, Pt b, Pt c) {
    vector<Pt> points;
    vector<Pt> points2;
    points.reserve(20);
    points.resize(3);
    points[0] = a;
    points[1] = b;
    points[2] = c;
    closePoints(points);


    // glPushMatrix();
    // glTranslatef(-100, 0, 0);

//    cout << "Subd " <<points.size()<<"\n";
    points2 = subdivide(points);
    // glTranslatef(0, -100, 0);
    // drawEdge(points2);

//    cout << "Subd1 " <<points2.size()<<"\n";
    points = subdivide(points2);
//    cout << "Subd2 " <<points.size()<<"\n";
    // glTranslatef(0, -100, 0);
    // drawEdge(points);

    points2 = subdivide(points);
//    cout << "Subd3 " <<points2.size()<<"\n";
    points = subdivide(points2);
//    cout << "Subd4 " <<points.size()<<"\n";

    /*
    points2 = subdivide(points);
//    cout << "Subd3 " <<points2.size()<<"\n";
    points = subdivide(points2);
//    cout << "Subd4 " <<points.size()<<"\n";
    */

    Node n = { x, points };
    return n;
}

Node makeRect(ZPt x, float w, float h) {
    vector<Pt> points;
    vector<Pt> points2;
    points.reserve(20);
    points.resize(4);
    points[0] = Pt(-w/2,-h/2);
    points[1] = Pt(+w/2,-h/2);
    points[2] = Pt(+w/2,+h/2);
    points[3] = Pt(-w/2,+h/2);
    closePoints(points);


    // glPushMatrix();
    // glTranslatef(-100, 0, 0);

//    cout << "Subd " <<points.size()<<"\n";
    points2 = subdivide(points);
    // glTranslatef(0, -100, 0);
    // drawEdge(points2);

//    cout << "Subd1 " <<points2.size()<<"\n";
    points = subdivide(points2);
//    cout << "Subd2 " <<points.size()<<"\n";
    // glTranslatef(0, -100, 0);
    // drawEdge(points);

    points2 = subdivide(points);
//    cout << "Subd3 " <<points2.size()<<"\n";
    points = subdivide(points2);
//    cout << "Subd4 " <<points.size()<<"\n";

    /*
    points2 = subdivide(points);
//    cout << "Subd3 " <<points2.size()<<"\n";
    points = subdivide(points2);
//    cout << "Subd4 " <<points.size()<<"\n";
    */

    Node n = { x, points };
    return n;
}

Node makeEllipse(ZPt x, float d1, float d2) {
    vector<Pt> points(20);
    for(unsigned i=0; i<points.size(); i++) {
	double ang = - 2 * M_PI * i / (points.size());
	points[i] = Pt(d1 * sin(ang), d2 * cos(ang));
    }
    closePoints(points);
    Node n = { x, points };
    return n;
}

void drawNode(Node &node) {
    glPushMatrix();
    glTranslatef(node.center.x, node.center.y, node.center.z);
    drawEdge(node.points);
    GLERR
    fillEdge(node.points, Pt(0,0));
    GLERR
    glPopMatrix();
}

void drawNode3(Node &node) {
    glPushMatrix();
    glTranslatef(node.center.x, node.center.y, node.center.z);
    glDisable(GL_LIGHTING);
    glColor3f(0,0,0);
    glEnable(GL_TEXTURE_1D);
    glBindTexture(GL_TEXTURE_1D, 3); // the cell edge
    drawEdge(node.points);
    GLERR
    glDisable(GL_TEXTURE_1D);
    glDisable(GL_LIGHTING);
    glColor3f(1,1,1);
    fillEdge(node.points, Pt(0,0));
    GLERR
    glPopMatrix();
}

void connectNodes(Node &node1, Node &node2) {
    ZVec zto = 0.5 * (node2.center - node1.center);
    Vec to = Vec(zto.x, zto.y);

    glPushMatrix();
    glTranslatef(node1.center.x, node1.center.y, node1.center.z);
    drawConnector(node1.points, Pt(0,0), to, zto.z);
    GLERR
    glPopMatrix();

    glPushMatrix();
    glTranslatef(node2.center.x, node2.center.y, node2.center.z);
    drawConnector(node2.points, Pt(0,0), -to, -zto.z);
    GLERR
    glPopMatrix();
}

void connectNodes2(Node &node1, Node &node2) {
    ZVec zto = 0.5 * (node2.center - node1.center);
    Vec to = Vec(zto.x, zto.y);

    glPushMatrix();
    glTranslatef(node1.center.x, node1.center.y, node1.center.z);
    drawConnector(node1.points, Pt(0,0), to, zto.z, false);
    GLERR
    glPopMatrix();

    glPushMatrix();
    glTranslatef(node2.center.x, node2.center.y, node2.center.z);
    drawConnector(node2.points, Pt(0,0), -to, -zto.z, false);
    GLERR
    glPopMatrix();
}

void connectNodes3(Node &node1, Node &node2) {
    ZVec zto = 0.5 * (node2.center - node1.center);
    Vec to = Vec(zto.x, zto.y);
    Pt inters1, inters2;
    if (!intersectEdge(node1.points, Pt(0,0), to, inters1) ||
	!intersectEdge(node2.points, Pt(0,0), -to, inters2)) {
      cout << "ERROR: cannot find intersections\n";
      abort();
    }
    glDisable(GL_LIGHTING);
    glColor3f(0,0,0);
    glLineWidth(10);
    glBegin(GL_LINE_STRIP);
    ZPt p1 = node1.center + ZVec(inters1.x, inters1.y, BEVELH);
    ZPt p2 = node2.center + ZVec(inters2.x, inters2.y, BEVELH);
    ZVec noz = p2-p1; noz.z = 0;
    ZVec nozu = noz.normalize();
    glVertex(p1 - 10 * nozu);
    glVertex(p1 + 0.4 * noz);
    glVertex(p1+0.5*(p2-p1));
    glVertex(p2 - 0.4 * noz);
    glVertex(p2 + 10*nozu);
    glEnd();
}

void rotateNode(Node &node, float angle) {
  for (int i = 0; i < node.points.size(); i++) {
    float x = node.points[i].x * cos(angle) - node.points[i].y * sin(angle);
    float y = node.points[i].x * sin(angle) + node.points[i].y * cos(angle);
    node.points[i].x = x;
    node.points[i].y = y;
  }
}

int type = 0;

void display() {
    if (type == 0) {
      glClearColor(0, 0.2, 0.2, 1);
    } else {
      glClearColor(1, 1, 1, 1);
    }
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

//      glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

    glColor4f(1, 1, 1, 1);

    glEnable(GL_BLEND);
    glEnable(GL_ALPHA_TEST);
    glAlphaFunc(GL_GREATER, 0.1);
    glEnable(GL_DEPTH_TEST);

    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    glEnable(GL_TEXTURE_1D);

    GLERR

	glPushMatrix();

	glTranslatef(500, 500, 0);
	// glRotatef(offs * 10, 0, 0, 1);

    glShadeModel(GL_SMOOTH);
    GLfloat mat_specular[] = { 1, 1, 1, 1 };
    glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, mat_specular);
    glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, 50);
    GLfloat light1[] = { 0.5, 0.2, 0.2, 1 };

    GLfloat light1pos[] = { 1.5, 1, 2, 0.00 };

    GLfloat light2[] = { 0.2, 0.6, 0.2, 1 };

    GLfloat light2_amb[] = { 0.3, 0.3, 0.3, 1 };

    GLfloat light2pos[] = { 1, 1, 2, 0.00 };
    glLightfv(GL_LIGHT0, GL_POSITION, light1pos);
    glLightfv(GL_LIGHT0, GL_DIFFUSE, light1);
    glLightfv(GL_LIGHT0, GL_SPECULAR, light1);

    glLightfv(GL_LIGHT1, GL_POSITION, light2pos);
    glLightfv(GL_LIGHT1, GL_SPECULAR, light2);
    glLightfv(GL_LIGHT1, GL_DIFFUSE, light2);
    glLightfv(GL_LIGHT1, GL_AMBIENT, light2_amb);

    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
    glEnable(GL_LIGHT1);


    int nodes = 0;
    Node node[16];

    srand(189);
    int i;
    glColor4f(0.6, 0.6, 0.6, 1);
    for (i = 0; i < 16; i++) {
      ZPt pos;
      if (rand()%6==0) continue;
      while (1) {
	pos = ZPt((i % 4) * 250-400+rand()%100-50,
		  (i / 4) * 250-400+rand()%100-50,
		  (i*3711%977)+1);
	//if (pos.x*pos.x+pos.y*pos.y > 200*200) break;
	break;
      }
      

      switch (rand() % 3) {
      case 0:
	node[nodes] = makeTriangle(pos,
				     Pt(0, 75),
				     Pt(-75, -50),
				     Pt(75, -50));
	break;
      case 1:
	node[nodes] = makeEllipse(pos, 60, 100);
	break;
      case 2:
	node[nodes] = makeRect(pos, 200, 50);
	break;
      }

      nodes++;
    }

    srand(9);

    for (i = 0; i < nodes; i++)
      {
	rotateNode(node[i], (rand()%360)*(M_PI/180.0));
	switch (type) {
	case 0: break;
	case 1: break;
	case 2: 
	  glDisable(GL_TEXTURE_1D);
	  glColor3f(0.5, 0.5, 0.5);
	  drawNode3(node[i]); 
	  break;
	}
      }

    srand(791);
    for (i = 0; i < nodes; i++) {
      int j = rand() % (nodes-1);
      if (j >= i) j++;
      switch (type) {
      case 0: connectNodes(node[i], node[j]); break;
      case 1: connectNodes2(node[i], node[j]); break;
      case 2: connectNodes3(node[i], node[j]); break;
      }
    }
	//node[1].center = ZPt(400 * sin(offs / 5), 
	//		 400 * cos(offs/5), 
	//		 100*cos(offs*3));
				     
    //glColor4f(0.6, 0.6, 0.6, 1);
    //drawNode(node[0]);
    //drawNode(node[1]);

    // glDisable(GL_TEXTURE_1D);
    //connectNodes(node[0], node[1]);
    /*
	glBegin(GL_QUAD_STRIP);
	drawEdge(points);
	glEnd();
    */
    GLERR

	//glColor4f(1, 0.5, 0.5, 1);
    GLERR

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
    glOrtho(0, w, 0, h, -10000, 10000);
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
    glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    // glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    // glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
    // glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
   
    GLERR
	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);


#define ETSIZE 1024
    unsigned char edgetex[ETSIZE][4];
    for(int i=0; i<ETSIZE; i++) {
	double c = i / (double)(ETSIZE-1);
	edgetex[i][0] = 255 * (0.5 + 0.5 * ((c > 0.7) & (c < 0.9)));
	edgetex[i][1] = 255 * (0.5 + 0.5 * ((c > 0.2) & (c < 0.4)));
	edgetex[i][2] = 255 * 0.5;
	edgetex[i][3] = 255 * (c > 0.1);
    }

    gluBuild1DMipmaps(GL_TEXTURE_1D, GL_RGBA, 
		    ETSIZE, GL_RGBA, GL_UNSIGNED_BYTE, edgetex);

    GLERR
    glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);


    // Texture unit 0 ----------------------------------------------------


    glEnable(GL_TEXTURE_1D);
    GLERR
    glBindTexture(GL_TEXTURE_1D, 2); // the cell edge (type 2)

    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAX_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_BASE_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_LOD, 0);
    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAX_LOD, 0);

    glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_CLAMP);
    glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    // glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    // glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
    // glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
   
    GLERR
	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);


    for(int i=0; i<ETSIZE; i++) {
	double c = i / (double)(ETSIZE-1);
	double l = c < .5 || c > .8;
	edgetex[i][0] = 255 * ((1 - l) + l *.2);
	edgetex[i][1] = 255 * ((1 - l) + l *.2);
	edgetex[i][2] = 255 * ((1 - l));
	edgetex[i][3] = 255 * (c > 0.3);
    }

    gluBuild1DMipmaps(GL_TEXTURE_1D, GL_RGBA, 
		    ETSIZE, GL_RGBA, GL_UNSIGNED_BYTE, edgetex);

    GLERR
    glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

    // Texture unit 0 ----------------------------------------------------

    glEnable(GL_TEXTURE_1D);
    GLERR
    glBindTexture(GL_TEXTURE_1D, 3); // the cell edge (type 3)

    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAX_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_BASE_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_LOD, 0);
    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAX_LOD, 0);

    glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_CLAMP);
    glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    // glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    // glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
    // glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
   
    GLERR
	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);


    for(int i=0; i<ETSIZE; i++) {
	double c = i / (double)(ETSIZE-1);
	edgetex[i][0] = 0;
	edgetex[i][1] = 0;
	edgetex[i][2] = 0;
	edgetex[i][3] = 255 * (c > 0.2);
    }

    gluBuild1DMipmaps(GL_TEXTURE_1D, GL_RGBA, 
		    ETSIZE, GL_RGBA, GL_UNSIGNED_BYTE, edgetex);

    GLERR
    glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

}

void keypress(unsigned char key, int x, int y) {
  cout << key << " pressed\n";
  if (key >= '0' && key <= '2') type = key - '0';
}

void idle() { 
    glutPostRedisplay(); 
}

int main(int argc, char **argv) {
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH);
    // glutInitDisplayMode(GLUT_SINGLE | GLUT_RGBA);
    glutInitWindowSize(1000,1000);
    glutInitWindowPosition(0,0);
    glutCreateWindow("fontTest");


    init();

    glutReshapeFunc(reshape);
    glutDisplayFunc(display);
    glutKeyboardFunc(keypress);
    glutIdleFunc(idle);
    glutMainLoop();
    return 0;
}

