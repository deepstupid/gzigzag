#include <math.h>
#include <Gummi.hxx>

#define GLERR { int er = glGetError(); if(er != GL_NO_ERROR) \
		    cout << "ERROR "<<__FILE__<<" "<<__LINE__ \
			<<gluErrorString(er)<<"\n"; \
	    }

namespace Gzz {
    typedef Gummi::Point<float> Pt;
    typedef Gummi::Point3<float> ZPt;
    typedef Gummi::Vector<float> Vec;
    typedef Gummi::Vector3<float> ZVec;

    typedef Gummi::Tex::Rect TexRect;

using Gummi::Font::DenseGlyphs;
using Gummi::Font::RandomRenderer;
    typedef RandomRenderer<DenseGlyphs<char>, char> TextRenderer;

    using std::vector;
    using std::cout;

    struct Problem { };

    enum CoordsType {
	AFFINE = 1,
	ORTHO = 2
    };

    const int RENDERABLE0 = 0x0800000;
    const int RENDERABLE1 = 0x1000000;
    const int RENDERABLE2 = 0x2000000;
    const int RMASK = 0xf800000;

    // XXX NOT ENOUGH SAFETY CHECKS FOR REMOVE!
    // GET MUSTN'T CHECK; MUST CHECK RANGE BEFORE.
    template<class T> class ObjectStorer {
	vector<T *> vec;
    public:
	int add(T *p) {
	    if(p == 0) {
		return 0; // invalid value
	    }
	    int i = vec.size();
	    vec.insert(vec.end(), p);
	    return i;
	}
	void remove(int p) {
	    delete vec[p];
	    vec[p] = 0;
	}
	T *get(int p) {
	    if(vec[p] == NULL)
		throw Problem();
	    return vec[p];
	}

	T *operator[](int p) { return get(p); }
    };


    /** A namespace which contains non-portable classes surrounding OpenGL.
     */
    namespace AbstractWin {

	class Image {
	public:
	    const int w, h;
	    Image(int w, int h) : w(w), h(h) { }
	    virtual ~Image() { }

	    /** Load a region of this image into a region of a texture.
	     * WARNING: artifacts near edges!
	     */
	    virtual void loadInto(Gummi::Tex::MosaicTile &tile, 
		    int x, int y, int w, int h) = 0;
	};

	class Eventhandler {
	public:
	    virtual ~Eventhandler() { } // not called by Window..
	    virtual void keystroke(const char *str) {}
	    virtual void mousepress(int x, int y, const char *str) {}
	    virtual void resize(int w, int h) { repaint(); }
	    virtual void repaint() = 0;
	};

	/*
	class IdleTasks {
	public:
	    virtual ~IdleTasks() { } // not called by WindowSystem
	    * One unit of processing.
	     * @return True, if a tick is needed again ASAP.
	     *
	    virtual bool tick() = 0;
	};
	*/

	class Window {
	protected:
	    Eventhandler *eventhandler;
	    /** Queue a repaint request.
	     */
	public:
	    Window() : eventhandler(0) { }
	    virtual void repaint() = 0;
	    virtual void setEventHandler(Eventhandler *h) {
		eventhandler = h;
		repaint();
	    }
	    virtual Eventhandler *getEventHandler() { 
		return eventhandler;
	    }

	    // virtual pair<int, int> getSize() = 0;
	    /** Enable this window for rendering.
	     */
	    virtual void setCurrent() = 0;

	    virtual void swapBuffers() = 0;

	    virtual Vec getSize() = 0;
	};

	class WindowSystem {
	    static WindowSystem *instance;
	public:
	    static WindowSystem *getInstance();

	    virtual ~WindowSystem() {}

	    virtual Window *openWindow(int x, int y, int w, int h) = 0;
	    virtual Image *loadImageFile(const char *filename) = 0;
	    virtual Image *loadImageData(const char *imageData, int len) = 0;
	    virtual Gummi::Font::Font *loadFont(const char *javaName, 
			    int pt) = 0;

	    virtual void eventLoop(bool wait) = 0;
	};

    }

    class ShaderRect {
    public:
	GLuint texnames[4];
	GLuint listname;
	void load(const char *turbf, const char *colf, 
		  const char *spotsf, const char *cellf);
        void unload();
        void getcoords(GLdouble v[4][4], GLdouble v2[4][4], GLdouble v3[4][4], GLfloat col[4][3], float offs);
    };



    void setStandardCoordinates(Vec windowSize);

    /** A general affine coordinate system.
     */
    struct AffineCoords {
	Pt center;
	Vec x;
	Vec y;
	float z;
	AffineCoords() { }
	AffineCoords(Pt center, Vec x, Vec y) : center(center), x(x), y(y) {}
	enum { nFloatParams = 7 };
	void load(float *f) {
	    center = Pt(f[0], f[1]);
	    x = Vec(f[2], f[3]);
	    y = Vec(f[4], f[5]);
	    z = f[6];
	}

	void vertex(ZPt &p) const {
	    ZPt tmp = transform(p);
	    // std::cout << "Affine vertex: "<<tmp<<" z "<<z<<"\n";
	    glVertex3f(tmp.x, tmp.y, tmp.z);
	}
	void vertex(Pt &p) const {
	    ZPt tmp = transform(p);
	    // std::cout << "Affine vertex: "<<tmp<<" z "<<z<<"\n";
	    glVertex3f(tmp.x, tmp.y, tmp.z);
	}
	ZPt transform(Pt &p) const {
	    Pt tmp = center + x * p.x + y * p.y;
	    return ZPt(tmp.x, tmp.y, z);
	}
	ZPt transform(ZPt &p) const {
	    Pt tmp = center + x * p.x + y * p.y;
	    return ZPt(tmp.x, tmp.y, z + p.z);
	}

    };

    /** A coordinate system which only translates
     * and scales.
     */
    struct OrthoCoords {
	Pt center;
	float xScale;
	float yScale;
	float z;
	OrthoCoords() { }
	OrthoCoords(Pt center, float xScale, float yScale) 
		: center(center), xScale(xScale), yScale(yScale) {}
	void load(float *f) {
	    center = Pt(f[0], f[1]);
	    xScale = f[2];
	    yScale = f[3];
	    z = f[4];
	}
	void vertex(ZPt &p) const {
	    Pt tmp = center + Vec(xScale * p.x, yScale * p.y);
	    // std::cout << "Ortho vertex: "<<tmp<<" z "<<z<<"\n";
	    glVertex3f(tmp.x, tmp.y, z + p.z);
	}
	void vertex(Pt &p) const {
	    Pt tmp = center + Vec(xScale * p.x, yScale * p.y);
	    // std::cout << "Ortho vertex: "<<tmp<<" z "<<z<<"\n";
	    glVertex3f(tmp.x, tmp.y, z);
	}
	ZPt transform(Pt &p) const {
	    return ZPt(center.x + xScale * p.x, 
			    center.y + yScale*p.y,
			    z);
	}
    };

    /** An OpenGL thing that can be rendered without a coordinate
     * system.
     */
    struct Renderable0 {
	virtual ~Renderable0() { }
	virtual void render() = 0;
    };
#define IMPLEMENTRENDER0 \
	void render() { renderImpl(); } \

    /** An OpenGL thing that can be rendered in a coordinate
     * system.
     */
    struct Renderable1 {
	virtual ~Renderable1() { }
	virtual void render(AffineCoords &c) = 0;
	virtual void render(OrthoCoords &c) = 0;
    };

#define IMPLEMENTRENDER1 \
	void render(AffineCoords &c) { renderImpl(c); } \
	void render(OrthoCoords &c) { renderImpl(c); }

    /** An OpenGL thing which starts in one coordinate
     * system and ends in another. Used for connections.
     */
    struct Renderable2 {
	virtual ~Renderable2() { }
	virtual void render(AffineCoords &c1, AffineCoords &c2) = 0;
	virtual void render(OrthoCoords &c1, OrthoCoords &c2) = 0;
    };

#define IMPLEMENTRENDER2 \
	virtual void render(AffineCoords &c1, AffineCoords &c2) \
	    { renderImpl(c1, c2); } \
	virtual void render(OrthoCoords &c1, OrthoCoords &c2) \
	    { renderImpl(c1, c2); }

    #include "GzzGLRen.hxx"

    inline float lerp(float a, float b, float fract) {
	return a + fract * (b-a);
    }

    using std::vector;

    template<class CoordsType> class Renderer {
	vector<float> floatBuffer;
	vector<CoordsType> coordsBuffer;

	public:
	Renderer() : floatBuffer(), coordsBuffer() { }

	void setPoints(int npts, float *points1, 
				int *indices2,
				float *points2, float fract) {
	    floatBuffer.reserve(npts);
	    for(int i=0; i<npts; i++)  {
		int ind2 = i;
		if(indices2) {
		    ind2 = CoordsType::nFloatParams *
				indices2[i / CoordsType::nFloatParams];
		}
		floatBuffer[i] = lerp(points1[i], 
			    points2[ind2 + i % CoordsType::nFloatParams], 
					fract);
	    }
	    int ncoords = npts / CoordsType::nFloatParams;
	    coordsBuffer.reserve(ncoords);
	    for(int i=0; i<ncoords; i++)
		coordsBuffer[i].load( & floatBuffer[i * CoordsType::nFloatParams] );
	}

	void renderScene(int *codes, 
				ObjectStorer<Renderable0> &r0s,
				ObjectStorer<Renderable1> &r1s,
				ObjectStorer<Renderable2> &r2s) {
	    int i=0; 
	    while(codes[i] != 0) {
		int code = codes[i] & ~RMASK;
		if(codes[i] & RENDERABLE0) {
		    r0s[code]->render();
		    i += 1;
		}
		else if(codes[i] & RENDERABLE1) {
		    r1s[code]->render(coordsBuffer[codes[i+1]]);
		    i += 2;
		}
		else if(codes[i] & RENDERABLE2) {
		    r2s[code]->render(coordsBuffer[codes[i+1]], 
				    coordsBuffer[codes[i+2]]);
		    i += 3;
		}
		else {
		    // We have a problem
		    throw Problem();
		}
	    }
	}
    };
}


