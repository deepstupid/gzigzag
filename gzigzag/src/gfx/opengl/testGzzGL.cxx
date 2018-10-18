#include <unistd.h>
#include "GzzGL.hxx"
#include <GL/gl.h>

using namespace Gzz::AbstractWin;
using namespace Gzz;
using namespace Gummi;
using namespace Gummi::Font;

using std::cout;

Tex::MosaicBuilder *mosaicbuilder;

TexturedQuad *tq;
NURBSConnector *nuc;

Gzz::Renderer<AffineCoords> r;

int tqid;
int txtid;
int nucid;

float points1[500] = {
    100, 100, // center
    1, 0,
    0, 1,
    0,
    500, 500, // center
    1, 0,
    0, 1,
    0
};

float points2[500] = {
    200, 300, // center
    1, 1,
    -0.5, 1,
    0,
    500, 500, // center
    1, 0,
    0, 1,
    0
};
float cur = 0;
float curd = 0.01;

Gzz::AbstractWin::Window *w;

ObjectStorer<Renderable> renderables;
ObjectStorer<Renderable2> renderable2s;

class EH : public Eventhandler {

    void repaint() {
	w->setCurrent();
	// cout << "REPAINT!!\n";
	int codes[20] = {
	    Gzz::RENDERABLE | tqid,
	    0,
	    Gzz::RENDERABLE | txtid,
	    0,
	    Gzz::RENDERABLE2 | nucid,
	    0, 
	    1,
	    0
	};
	r.setPoints(14, points1, 0, points2, cur);
	cur += curd;
	if(cur > 1) { curd = -0.01; }
	if(cur < 0) { curd = 0.01; }

	glClearColor(0.2, 0, 0, 1);
	glClear(GL_COLOR_BUFFER_BIT);
	glColor3f(1, 1, 1);
	glEnable(GL_TEXTURE_2D);
	glEnable(GL_BLEND);

        glEnable(GL_ALPHA_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	setStandardCoordinates(w->getSize());
	r.renderScene(codes, renderables, renderable2s);

	w->swapBuffers();

	w->repaint();
    }
};

typedef RandomRenderer<DenseGlyphs<char>, char> MyRenderer;

int main() {
    WindowSystem *ws = WindowSystem::getInstance();
    w  = ws->openWindow(0, 0, 256, 256);
    w->setCurrent();
    sleep(1);

    Image *img = ws->loadImageFile("argh/ex3-cell.png");
    if(!img) throw Gzz::Problem();

    mosaicbuilder = new Tex::MosaicBuilder(
	new Tex::Texture2DFactory(
	    new Tex::Raster<Tex::Format::RGBA>(512, 512)));

    Tex::MosaicTile tile = mosaicbuilder->alloc(img->w, img->h);
    img->loadInto(tile, 0, 0, img->w, img->h);

    tq = new TexturedQuad();
    tq->texrect = tile.getRect();
    tq->corners[0] = Pt(0,0);
    tq->corners[1] = Pt(0,100);
    tq->corners[2] = Pt(100,100);
    tq->corners[3] = Pt(100,0);

    tqid = renderables.add(tq);

    nuc	= new NURBSConnector();
    nuc->start = tile.getRect();
    nuc->end = tile.getRect();
    nuc->p1_left = Pt(400, 0);
    nuc->p1_right = Pt(350, 0);
    nuc->p2_left = Pt(500, 300);
    nuc->p2_right = Pt(550, 300);

    char *fontFile = "../../../../gummiterm/examples/n022003l.pfb";
    
    Gummi::Font::Font *f = new FTFont(fontFile, 24);
    Gummi::Font::Font *fb_lo = new Font_Bordered(f, 4);

    nucid = renderable2s.add(nuc);


    RandomRenderer<DenseGlyphs<char>, char> *rend = new MyRenderer(fb_lo);

    Gzz::HorizText<RandomRenderer<DenseGlyphs<char>, char> > *txt 
	= new HorizText<RandomRenderer<DenseGlyphs<char>, char> >
	    ();
    txt->r = rend;
    txt->txt = "GZZ!";
    txt->origin = Pt(20, 20);

    txtid = renderables.add(txt);

    mosaicbuilder->prepare();

    w->setEventHandler(new EH());


    cout << "Main loop\n";
    ws->eventLoop();
    cout << "Main exiting\n";
}
