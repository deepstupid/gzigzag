#include <math.h>
#include <iostream>
#include <vector>
#include <GL/glut.h>
#include <GummiBasic.hxx>
#include <GummiTexture.hxx>



#include </usr/include/gdk-pixbuf-1.0/gdk-pixbuf/gdk-pixbuf.h>


#define GLERR { int er = glGetError(); if(er != GL_NO_ERROR) \
		    cout << "ERROR "<<__FILE__<<" "<<__LINE__ \
			<<gluErrorString(er)<<"\n"; \
	    }

using std::cout;

using namespace Gummi;

typedef Tex::Raster<Tex::Format::RGBA> ImageRaster;
ImageRaster loadImage(char *filename) ;

Tex::MosaicBuilder *mosaicbuilder;

const int borderPixels = 11;



void renderCell(int x1, int y1, int x2, int y2) {
}

struct Err {};

/** A texture rectangle where we still remember the pixel size.
 */
struct PixRect {
    int w, h;
    Tex::Rect r;
    PixRect(char *filename) {
	ImageRaster image = loadImage(filename);
	Tex::MosaicTile tile = mosaicbuilder->alloc(image.getWidth(), image.getHeight(), 2);
	tile.loadImage(image);
	r = tile.getRect();
	w = image.getWidth();
	h = image.getHeight();
    }
};

/** Load an image into a raster using gdk-pixbuf.
 */
ImageRaster loadImage(char *filename) {
    GdkPixbuf* pb = gdk_pixbuf_new_from_file(filename);
    if(!pb) throw Err();

    cout << "NC "<<gdk_pixbuf_get_n_channels(pb)
	<<" ALP "<<gdk_pixbuf_get_has_alpha(pb)
	<<" BPS "<<gdk_pixbuf_get_bits_per_sample(pb) 
	<<" PIX "<<int(gdk_pixbuf_get_pixels(pb))
	<<" W "<<gdk_pixbuf_get_width(pb)
	<<" H "<<gdk_pixbuf_get_height(pb)
	<<" RS "<<gdk_pixbuf_get_rowstride(pb)
	<<"\n";

    GLuint *c = (GLuint *)gdk_pixbuf_get_pixels(pb);
    vector<Tex::Format::RGBA::ValueType> data;
    int w = gdk_pixbuf_get_width(pb);
    int h = gdk_pixbuf_get_height(pb);
    copy(c, c + w * h, back_inserter(data));
    for(int i=0; i<w*h; i++) 
	cout << data[i] << " ";
    cout << "\n";
    gdk_pixbuf_unref(pb);
    return ImageRaster(w, h, data);
}

/** A general 2D linear transformation.
 */
struct VectorBasis {
    Vector<float> x, y;
    Vector<float> operator()(Vector<float> &v) {
	return Vector<float>(v.x * x.x + v.y * y.x,
			     v.x * x.y + v.y * y.y);
    }

    VectorBasis(double angle, double mag) {
	setAngleMag(angle, mag);
    }

    void setAngleMag(double angle, double mag) {
	y = mag * Vector<float>( sin(angle), cos(angle) );
	x = y.cw90();
    }
};

/** A general 2D affine transformation and Z coordinate.
 */
struct CellPosition {
    Point<float> center;
    float z;
    VectorBasis coords;

    CellPosition(float x, float y, float z, double angle, double mag) 
	: center(x, y), z(z), coords(angle, mag) {
    }

};

struct CellDrawer {
    static const int borderpix = borderPixels;
    PixRect p;
    /** Vector from center to upper left in pixels.
     */
    Vector<float> ctr2ul;
    /** Vector from center to upper right in pixels.
     */
    Vector<float> ctr2ur;
    /** Vector from center to upper left border-center 
     * in pixels.
     */
    Vector<float> ctr2ulb;
    CellDrawer(char *filename) : p(filename), 
	    ctr2ul(-p.w/2, -p.h/2),
	    ctr2ur(p.w/2, -p.h/2),
	    ctr2ulb(-p.w/2 + borderpix, -p.h/2 + borderpix) {
    }
    void draw(CellPosition &pos)  {
	Vector<float> tul = pos.coords(ctr2ul);
	Vector<float> tur = pos.coords(ctr2ur);

	p.r.tex.bind();
	glBegin(GL_QUADS);
	    p.r.texcoord(0, 0);
	    vert(pos.center + tul, pos.z);
	    p.r.texcoord(1, 0);
	    vert(pos.center + tur, pos.z);
	    p.r.texcoord(1, 1);
	    vert(pos.center - tul, pos.z);
	    p.r.texcoord(0, 1);
	    vert(pos.center - tur, pos.z);
	glEnd();
    }
private:
    inline void vert(const Point<float> &f, float z) {
	glVertex3f(f.x, f.y, z);
    }
};

void clampTexcoord(GLfloat &f, int n) {
    float eps = 0.5 / (float)n;
    if(f <= eps) f = eps;
    if(f >= 1-eps) f = 1-eps;
}

struct LinkDrawer {
    GLUnurbsObj *nr;

    static const int borderpix = borderPixels;
    PixRect p;
    LinkDrawer(char *filename) : p(filename) {
	nr = gluNewNurbsRenderer();
	gluNurbsProperty(nr, GLU_SAMPLING_METHOD, GLU_DOMAIN_DISTANCE);
	gluNurbsProperty(nr, GLU_U_STEP, 10);
	gluNurbsProperty(nr, GLU_V_STEP, 1);
	/*
	gluNurbsProperty(nr, GLU_DISPLAY_MODE, GLU_OUTLINE_POLYGON);
	*/
	GLERR
    }

    struct Side {
	LinkDrawer &parent;
	Point<float> l, r; // left and right, when going down.
	Point<float> ol, or; // left and right control point, going away from cell
	Side(LinkDrawer &parent,
		CellDrawer &d, CellPosition &pos, bool horiz, int side): 
		    parent(parent) {
	    Vector<float> ul = d.ctr2ulb;
	    Vector<float> vl, vr;
	    if(horiz) {
		ul.y += borderpix;
		vl = ul;
		vr = ul; vr.x = -vr.x;
	    } else {
		ul.x += borderpix;
		vl = ul; vl.y = -vl.y;
		vr = ul; 
	    }
	    double origlen = (vr-vl).length();
	    vl = pos.coords(vl);
	    vr = pos.coords(vr);
	    vl = side * vl;
	    vr = side * vr;
	    Vector<float> dif = vr - vl;

	    // Then, scale the difference so that at mag we have exactly p.width 
	    // pixels between them.
	    double sca =  parent.p.w / origlen;

	    Vector<float> ll = vl + (0.5 - 0.5 * sca) * dif;
	    Vector<float> lr = vr - (0.5 - 0.5 * sca) * dif;

	    l = pos.center + ll;
	    r = pos.center + lr;

	    Vector<float> rot = (r-l).cw90();
	    rot = rot * 2;
	    ol = l + rot; or = r + rot;

	    // cout << "Side: "<<pos.center<<" "<<" "<<horiz<< " "<<side<< " " 
	// 	    <<ul<<" "<<vl<<" "<<vr<<" "<<l<<" "<<r<<"\n";
	}
    };

    void draw(CellDrawer &d1, CellPosition &p1, bool horiz1, int side1,
	    CellDrawer &d2, CellPosition &p2, bool horiz2, int side2) {
	Side s1(*this, d1, p1, horiz1, side1);
	Side s2(*this, d2, p2, horiz2, side2);

	float z1 = p1.z;
	float z2 = p2.z;
	// Place the connectors in front of cells always.
	z1 += 0.05;
	z2 += 0.05;

	GLfloat control[4][2][3] = {
	    {
		{ s1.l.x, s1.l.y, z1 },
		{ s1.r.x, s1.r.y, z1 }
	    },
	    {
		{ s1.ol.x, s1.ol.y, z1 },
		{ s1.or.x, s1.or.y, z1 }
	    },
	    {
		{ s2.or.x, s2.or.y, z2 },
		{ s2.ol.x, s2.ol.y, z2 }
	    },
	    {
		{ s2.r.x, s2.r.y, z2 },
		{ s2.l.x, s2.l.y, z2 }
	    }
	};
	GLfloat uknots[8] = {
	    0, 0, 0, 0, 1, 1, 1, 1
	};
	GLfloat vknots[4] = {
	    0, 0, 1, 1
	};
	GLfloat tc = 1;
	const int NTCI = 4, NTCJ = 2;
	GLfloat tcoords[4][2][2] = {
	    {
		{ 0, 0 },
		{ 1, 0 }
	    },
	    {
		{ 0, tc },
		{ 1, tc }
	    },
	    {
		{ 0, 1-tc },
		{ 1, 1-tc }
	    },
	    {
		{ 0, 1 },
		{ 1, 1 }
	    },
	};
	for(int i=0; i<NTCI; i++)
	    for(int j=0; j<NTCJ; j++) {
		clampTexcoord(tcoords[i][j][0], p.w);
		clampTexcoord(tcoords[i][j][1], p.h);
		p.r.changeTexcoords(tcoords[i][j][0], tcoords[i][j][1]);
	}
	p.r.tex.bind();
	GLERR
	gluBeginSurface(nr);
	GLERR
	    gluNurbsSurface(nr, 8, uknots, 4, vknots, 6, 3, (GLfloat *)control,
		    4, 2, GL_MAP2_VERTEX_3);
	GLERR
	    gluNurbsSurface(nr, 8, uknots, 4, vknots, 4, 2, (GLfloat *)tcoords,
		    4, 2, GL_MAP2_TEXTURE_COORD_2);
	GLERR
	gluEndSurface(nr);
	GLERR
    }
};

CellDrawer *cd;
LinkDrawer *ld;

double t = 0;

void display() {

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    cout << "DISPLAY\n";

    glColor3f(1, 1, 1);

    glEnable(GL_BLEND);
    glEnable(GL_ALPHA_TEST);
    glAlphaFunc(GL_GREATER, 0.1);
    glEnable(GL_DEPTH_TEST);

    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//    glDisable(GL_TEXTURE_2D);
    /*
 cout << "Cell: " << cell;
    cell.tex.bind();
    glBegin(GL_QUADS);
	cell.texcoord(0, 0);
	glVertex2f(0, 0);
	cell.texcoord(0, 1);
	glVertex2f(0, 1000);
	cell.texcoord(1, 1);
	glVertex2f(1000, 1000);
	cell.texcoord(1, 0);
	glVertex2f(1000, 0);
    glEnd();
    */

    GLERR

    CellPosition pos1(300 + 20 * sin(t), 100 + 20 * sin(1.02 * t), 0,  
				sin(0.1 * t), 0.25);
    CellPosition pos2(300 + 60 * sin(0.9 * t), 500, 0.1, 
			0.7 * sin(0.09 * t), 0.25*(2 + sin(0.21 * t)));
    CellPosition pos3(700 + 60 * sin(0.7 * t), 500 - sin(1.03 * t), 0.2, 
			0.5 * sin(0.19 * t), 0.25*(2 + sin(0.28 * t)));
    Vector<float> offs(0, -400);
    Point<float> c = pos3.center + pos3.coords(offs);
    CellPosition pos4(c.x, c.y, 0.3,
			0.5 * sin(0.19 * t), 0.25*(2 + sin(0.24 * t)));
    CellPosition pos5(300, c.y, -0.1, 0, 0.25);

    // glDisable(GL_TEXTURE_2D);

    ld->draw(*cd, pos1, true, -1,
	     *cd, pos2, true, 1
	    );

    ld->draw(*cd, pos2, false, -1,
	     *cd, pos3, false, 1
	    );


    ld->draw(*cd, pos4, true, -1,
	     *cd, pos3, true, 1
	    );

    ld->draw(*cd, pos5, false, -1,
	     *cd, pos4, false, 1
	    );

    GLERR
    glEnable(GL_TEXTURE_2D);

    cd->draw(pos1);
    cd->draw(pos2);
    cd->draw(pos3);
    cd->draw(pos4);
    cd->draw(pos5);

    t += 0.02;

    glutSwapBuffers();

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


void init() {
    glClearColor(0, 0, 0, 0);

    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
    // glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
    glEnable(GL_TEXTURE_2D);

    cd = new CellDrawer("ex3-cell.png");
    ld = new LinkDrawer("ex3-link.png");

    mosaicbuilder->prepare();
}


void idle() { 
    glutPostRedisplay(); 
}

int main(int argc, char **argv) {
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA);
    // glutInitDisplayMode(GLUT_SINGLE | GLUT_RGBA);
    glutInitWindowSize(1100,1100);
    glutInitWindowPosition(0,0);
    glutCreateWindow("fontTest");

    mosaicbuilder = new Tex::MosaicBuilder(
	new Tex::Texture2DFactory(
	    new Tex::Raster<Tex::Format::RGBA>(512, 512)));

    init();

    glutReshapeFunc(reshape);
    glutDisplayFunc(display);
    glutIdleFunc(idle);
    glutMainLoop();
    return 0;
}

