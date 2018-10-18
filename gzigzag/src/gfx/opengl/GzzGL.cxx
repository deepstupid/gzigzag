#include "GzzGL.hxx"
#include <algorithm>


namespace Gzz {

    void setStandardCoordinates(Vec windowSize) {
	int w = (int)windowSize.x;
	int h = (int)windowSize.y;
	glViewport(0, 0, w, h);
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	glOrtho(0, w, h, 0, 10000, -10000);
	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();

	
    }

	SmoothConnector::Side::Side(const ZPt &l0, const ZPt &l0i, 
				   const ZPt &r0, const ZPt &r0i) 
	  : l(l0), li(l0i), r(r0), ri(r0i) {

	    mid = l + 0.5 * (r - l);
	    length = (r-l).length();
	    rot = (l - li) + (r - ri);
	    rot = (1.0 / rot.length()) * rot;
	}

	void SmoothConnector::Side::setMag(double f) {
	    ol = l + f * (l - li);
	    or__ = r + f * (r - ri);
	    omid = ol + 0.5 * (or__ - ol);
	}

#define NCONTROL 9
#define NSUB 5
#define NSPLINE (NCONTROL*32)

	GLfloat splineCoeff[NSPLINE][NCONTROL];

	static bool started = false;
	void init() {
	    started = true;
	    GLfloat f1[NSPLINE][NCONTROL], f2[NSPLINE][NCONTROL];
	    GLfloat (*p1)[NCONTROL], (*p2)[NCONTROL];
	    p1 = f1; p2 = f2;
	    int n = NCONTROL;
	    for(int i=0; i<NCONTROL; i++) {
		for(int j=0; j<NCONTROL; j++) 
		    p1[i][j] = (float)(i == j);
	    }
	    for(int s = 0; s < NSUB; s++) {
		for(int j=0; j<NCONTROL; j++) {
		    for(int i=0; i<n; i++) {
			p2[i*2][j] = p1[i][j];
			p2[i*2+1][j] = p1[i][j];
		    }
		    double c1 = 0.6, c2 = 0.2;
		    p1[0][j] = p2[0][j]; p1[2*n-1][j] = p2[2*n-1][j];
		    for(int i=1; i< 2*n-1; i++) {
			p1[i][j] = c2 * p2[i-1][j] +
				c1 * p2[i][j] +
				c2 * p2[i+1][j];
		    }
		    for(int i=1; i< 2*n-1; i++) {
			p2[i][j] = c2 * p1[i-1][j] +
				c1 * p1[i][j] +
				c2 * p1[i+1][j];
		    }
		}
		std::swap(p1, p2);
		n *= 2;
	    }
	    for(int i=0; i<NSPLINE; i++) {
		for(int j=0; j<NCONTROL; j++) {
		    splineCoeff[i][j] = p1[i][j];
		    cout << p1[i][j] << " ";
		}
		cout << "\n";
	    }
	}

	void evalSpline(double at, GLfloat (*tcoords)[2][2], 
				    GLfloat (*control)[2][3]) {

	     for(int j=0; j<2; j++) {
		 GLfloat tc[2] = {0, 0};
		 GLfloat coord[3] = {0, 0, 0};
		 for(int c = 0; c<NCONTROL; c++) {
		     float r = splineCoeff[(int)(at * (NSPLINE-1))][c];
		     if(r != 0) {
			 for(int x=0; x<2; x++) 
			     tc[x] += r*tcoords[c][j][x];
			 for(int x=0; x<3; x++) 
			     coord[x] += r*control[c][j][x];
		     }
		 }
		 // cout << "Vert: "<<coord[0]<<" "<<coord[1]<<
	     	   //  " "<<coord[2]<<"\n";

		 glTexCoord2fv(tc);
		 glVertex3fv(coord);
	     }
	}

	void SmoothConnector::renderImpl(Side &s1, Side &s2) {
	    ZPt mid = s1.mid + 0.5 * (s2.mid - s1.mid);
	    mid.z += 4;

	    ZVec v = mid - s1.mid;
	    double v_orig_len = v.length();
	    v.z = 0;
	    v = 1/v.length() * v;

	    double dot1 = s1.rot.dot(v);
	    ZVec nv = -v;
	    double dot2 = s2.rot.dot(nv);

	    // Now, if the dots are large, make connector length to be 1/3
	    // of length of v. If dots are smaller, scale up towards length(v)

	    double len = lerp(.75, 3, 0.5 * sqrt(2 - dot1 - dot2));
	    //if(len > (s1.length + s2.length) / v_orig_len)
	    //  len = (s1.length + s2.length) / v_orig_len;
	    s1.setMag(len);
	    s2.setMag(len);

	    /*
	    cout << "Nurb calc: "<<v<<" "<<dot1<<" "<<dot2<<"\n"<<
		    len<<" "<<mv;

	    cout << "Mid: "<<s1.l<<" "<<s2.l<<" "<<midl<<"\n\n"<<
			s1.r<<" "<<s2.r<<" "<<midr<<"\n\n";
	    */

	    //s1.drawNurbs(start, startb, midl, midr, nmvl, nmvr);
	    //s2.drawNurbs(end, endb, midr, midl, mvr, mvl);

	    ZPt midl = s1.ol + 0.5 * (s2.or__ - s1.ol);
	    ZPt midr = s1.or__ + 0.5 * (s2.ol - s1.or__);

	    midl.z += 4;
	    midr.z += 4;


	    GLfloat control[NCONTROL][2][3] = {
		{
		    { s1.li.x, s1.li.y, s1.li.z - 1 },
		    { s1.ri.x, s1.ri.y, s1.ri.z - 1 }
		}, 
		{
		    { s1.l.x, s1.l.y, s1.li.z - 1 },
		    { s1.r.x, s1.r.y, s1.ri.z - 1 }
		}, 
		{
		    { s1.li.x+1.5*(s1.l-s1.li).x, s1.li.y+1.5*(s1.l-s1.li).y, s1.li.z - 1 },
		    { s1.ri.x+1.5*(s1.r-s1.ri).x, s1.ri.y+1.5*(s1.r-s1.ri).y, s1.ri.z - 1 }
		}, 
		{
		    { s1.ol.x, s1.ol.y, s1.ol.z - 1 },
		    { s1.or__.x, s1.or__.y, s1.or__.z - 1 }
		}, 
		{
		    { midl.x, midl.y, midl.z + 5 },
		    { midr.x, midr.y, midr.z + 5 }
		},
		{
		    { s2.or__.x, s2.or__.y, s2.or__.z - 1 },
		    { s2.ol.x, s2.ol.y, s2.ol.z - 1 }
		}, 
		{
		    { s2.ri.x+1.5*(s2.r-s2.ri).x, s2.ri.y+1.5*(s2.r-s2.ri).y, s2.ri.z - 1 },
		    { s2.li.x+1.5*(s2.l-s2.li).x, s2.li.y+1.5*(s2.l-s2.li).y, s2.li.z - 1 }
		}, 
		{
		    { s2.r.x, s2.r.y, s2.ri.z - 1 },
		    { s2.l.x, s2.l.y, s2.li.z - 1 }
		}, 
		{
		    { s2.ri.x, s2.ri.y, s2.ri.z - 1 },
		    { s2.li.x, s2.li.y, s2.li.z - 1 }
		}
	    };

	    if(!started) init();

	    for (int side=0; side<2; side++) {

		float eps = 0.01;
		GLfloat tcoords[NCONTROL][2][2] = {
		    {
			{ eps, eps },
			{ 1-eps, eps }
		    },
		    {
			{ eps, startb },
			{ 1-eps, startb },
		    },
		    {
			{ eps, startb * 1.5 },
			{ 1-eps, startb * 1.5 },
		    },
		    {
			{ eps, .6 },
			{ 1-eps, .6 },
		    },
		    {
			{ eps, 1-eps },
			{ 1-eps, 1-eps }
		    },
		    {
			{ eps, .6 },
			{ 1-eps, .6 },
		    },
		    {
			{ eps, endb * 1.5 },
			{ 1-eps, endb * 1.5 },
		    },
		    {
			{ eps, endb },
			{ 1-eps, endb },
		    },
		    {
			{ eps, eps },
			{ 1-eps, eps },
		    }
		};

		Gummi::Tex::Rect &rect = side ? end : start;
		
		for(int i=0; i<NCONTROL; i++) 
		    for(int j=0; j<2; j++)
			rect.changeTexcoords(tcoords[i][j][0], tcoords[i][j][1]);
		rect.tex.bind();
		
#undef DEBUG_LINES

#ifdef DEBUG_LINES

		glPushAttrib(GL_CURRENT_BIT | GL_ENABLE_BIT | GL_POLYGON_BIT | GL_LINE_BIT);

#endif


		//glPushAttrib(GL_CURRENT_BIT | GL_ENABLE_BIT | GL_POLYGON_BIT | GL_LINE_BIT);
		//glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		//glDisable(GL_TEXTURE_2D);
		glBegin(GL_QUAD_STRIP);

#define evalspline(a) evalSpline(a, tcoords, control)

		evalspline(side ? 1.0 : 0.0);
		for(int i=3; i<=15; i++) 
		    evalspline(side ? 1 - i/30.0 : i/30.0);
		glEnd();
		//glPopAttrib();

#ifdef DEBUG_LINES

		glDisable(GL_TEXTURE_2D);

		// Draw the control points for debugging in blue
		glDisable(GL_DEPTH_TEST);
		// glDisable(GL_BLEND);
		glColor4f(0, 0, 0.5, 0.5);
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		// glLineWidth(4);
		glBegin(GL_QUAD_STRIP);
		    for(int i=0; i<NCONTROL; i++) 
			for(int j=0; j<2; j++)
			    glVertex3f(control[i][j][0], 
					control[i][j][1],
					control[i][j][2]);
		glEnd();


		glPopAttrib();
#endif
	    }

	}



	void SmoothConnector::Side::drawNurbs(
			Gummi::Tex::Rect &rect, float txtb,
			ZPt &ml, ZPt &mr, ZVec &mrotl, ZVec &mrotr) {
	    }
}

namespace Gzz {


#define TEXSIZE 128
#define TEX2 512

char turb[TEXSIZE][TEXSIZE][TEXSIZE][2];
char cell[TEX2][TEX2][4];
char spots[TEXSIZE][TEXSIZE][TEXSIZE];
char col[TEXSIZE][TEXSIZE][TEXSIZE][3];

void read3File(const char *name, void *ptr, int b) {
    int n = b * TEXSIZE * TEXSIZE * TEXSIZE;
    FILE *f = fopen(name, "r");
    if(!f) { abort(); }
    int i;
    // if(fread(ptr, n, 1, f) != n) abort();
    if((i = fread(ptr, 1, n, f)) != n) fprintf(stderr, "Reading : %d of %d\n", i, n);
    fclose(f);
}

void read2File(const char *name, void *ptr, int b) {
    int n = b * TEX2 * TEX2 ;
    FILE *f = fopen(name, "r");
    if(!f) { abort(); }
    int i;
    // if(fread(ptr, n, 1, f) != n) abort();
    if((i = fread(ptr, 1, n, f)) != n) fprintf(stderr, "Reading : %d of %d\n", i, n);
    fclose(f);
}



void ShaderRect::load(const char *turbf, const char *colf, 
		      const char *spotsf, const char *cellf) {

  cout << "Reading files\n";
  cout << turbf << "\n";
  read3File(turbf/*"turb.32t"*/, turb, 2);
  cout << cellf << "\n";
  read2File(cellf/*"cell.2t"*/, cell, 4);
  cout << spotsf << "\n";
  read3File(spotsf/*"spots.3t"*/, spots, 1);
  cout << colf << "\n";
  read3File(colf/*"col.33t"*/, col, 3);

  cout << "Setting up textures\n";

  int  c;

  listname = glGenLists(2);
  GLERR
  glGenTextures(4, texnames);

  glActiveTexture(GL_TEXTURE0);
  glBindTexture(GL_TEXTURE_3D, texnames[0]); // the offsetter
  glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
  glTexImage3D(GL_TEXTURE_3D, 0, GL_DSDT_NV, 
	       TEXSIZE, TEXSIZE, TEXSIZE, 0, GL_DSDT_NV, GL_BYTE, turb);
  GLERR

  glActiveTexture(GL_TEXTURE3);
  glBindTexture(GL_TEXTURE_2D, texnames[1]); // The cell
  glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
  glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 
	       TEX2, TEX2, 0, GL_RGBA, GL_UNSIGNED_BYTE, cell);
  GLERR

  glActiveTexture(GL_TEXTURE1);
  glBindTexture(GL_TEXTURE_3D, texnames[2]); //
  glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
  glTexImage3D(GL_TEXTURE_3D, 0, GL_RGB, 
	       TEXSIZE, TEXSIZE, TEXSIZE, 0, GL_RGB, GL_UNSIGNED_BYTE, col);
  GLERR

  glActiveTexture(GL_TEXTURE2);
  glBindTexture(GL_TEXTURE_3D, texnames[3]); //
  glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
  glTexImage3D(GL_TEXTURE_3D, 0, GL_ALPHA, 
	       TEXSIZE, TEXSIZE, TEXSIZE, 0, GL_ALPHA, GL_UNSIGNED_BYTE, spots);
  GLERR

  glActiveTexture(GL_TEXTURE0);


    glNewList(listname, GL_COMPILE/*_AND_EXECUTE*/);

    glPushAttrib(GL_CURRENT_BIT | GL_ENABLE_BIT | GL_POLYGON_BIT | GL_LINE_BIT | GL_COLOR_BUFFER_BIT);

    glEnable(GL_TEXTURE_SHADER_NV);

    // Texture unit 0 ----------------------------------------------------
    glActiveTexture(GL_TEXTURE0);

    glTexEnvi(GL_TEXTURE_SHADER_NV, GL_SHADER_OPERATION_NV, GL_TEXTURE_3D);

    glEnable(GL_TEXTURE_3D);
    GLERR
    glBindTexture(GL_TEXTURE_3D, texnames[0]); // the offsetter

    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAX_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_BASE_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_LOD, 0);
    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAX_LOD, 0);

    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_REPEAT);
    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    //glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    //glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
    // glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
   

    GLERR
    glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

    // Texture unit 3 ----------------------------------------------------

    glActiveTexture(GL_TEXTURE3);


    glTexEnvi(GL_TEXTURE_SHADER_NV, GL_SHADER_OPERATION_NV, GL_OFFSET_TEXTURE_2D_NV);
    SHCONST("")
    glTexEnvi(GL_TEXTURE_SHADER_NV, GL_PREVIOUS_TEXTURE_INPUT_NV, GL_TEXTURE0);
    SHCONST("")

    glBindTexture(GL_TEXTURE_2D, texnames[1]); // The cell

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_LOD, 0);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LOD, 0);

    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_CLAMP);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    //glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    //glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    // glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
   
    // glDisable(GL_TEXTURE_3D);
    // glDisable(GL_TEXTURE_2D);

    // float disp = 0.3;
    float disp = 0.01;
    GLfloat mat[4] = {disp, 0, 0, disp};
    // GLfloat mat[4] = {0.15, 0, 0, 0.15};
    // GLfloat mat[4] = {1.2, 0, 0, 1.2};
    glTexEnvfv(GL_TEXTURE_SHADER_NV, GL_OFFSET_TEXTURE_MATRIX_NV, mat);

//    glEnable(GL_REGISTER_COMBINERS_NV);
 //   glCombinerInputNV(GL_COMBINER0_NV, GL_RGB, 
    SHCONST("")

    // glDisable(GL_TEXTURE3);
    GLERR


    // Texture unit 1 ----------------------------------------------------
    glActiveTexture(GL_TEXTURE1);

    glTexEnvi(GL_TEXTURE_SHADER_NV, GL_SHADER_OPERATION_NV, GL_TEXTURE_3D);

    glEnable(GL_TEXTURE_3D);
    GLERR
    glBindTexture(GL_TEXTURE_3D, texnames[2]); //

    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAX_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_BASE_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_LOD, 0);
    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAX_LOD, 0);

    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_REPEAT);
    // glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    // glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
    // glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
   
    GLERR
    glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

    // Texture unit 2 ----------------------------------------------------

    glActiveTexture(GL_TEXTURE2);

    glTexEnvi(GL_TEXTURE_SHADER_NV, GL_SHADER_OPERATION_NV, GL_TEXTURE_3D);

    glEnable(GL_TEXTURE_3D);
    GLERR
    glBindTexture(GL_TEXTURE_3D, texnames[3]); //

    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAX_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_BASE_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_LOD, 0);
    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAX_LOD, 0);

    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_REPEAT);
    // glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    // glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    // glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
   
    GLERR

    glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_EXT);    
    GLERR
    glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB_EXT, GL_INTERPOLATE_EXT);    
    GLERR
    glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE0_RGB_EXT, GL_PREVIOUS_EXT);    
    GLERR
    glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE1_RGB_EXT, GL_PRIMARY_COLOR_EXT);    
    GLERR
    glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE2_RGB_EXT, GL_TEXTURE);    
    glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE2_ALPHA_EXT, GL_TEXTURE);    
    GLERR
    glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_ALPHA_EXT, GL_ADD);    

    GLERR


    // Display

      //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    //glColor3f(1, 1, 1);

    glEnable(GL_BLEND);
    glEnable(GL_ALPHA_TEST);
    glAlphaFunc(GL_GREATER, 0.1);
    glEnable(GL_DEPTH_TEST);

    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


    glEndList();


    // cleanup ----------------------------------------------------------
    
    glNewList(listname + 1, GL_COMPILE/*_AND_EXECUTE*/);

    glActiveTexture(GL_TEXTURE0);
    glDisable(GL_TEXTURE_SHADER_NV);
    glBindTexture(GL_TEXTURE_1D, 0);
    glBindTexture(GL_TEXTURE_2D, 0);
    glBindTexture(GL_TEXTURE_3D, 0);
    glPopAttrib();

    glEndList();
}



void ShaderRect::unload() {
  // XXX: release textures and display lists
}

namespace Perlin {
#include "images/pdl/perlin.c"
}

// Get the corner points of a rectange centered at x with sides u and v
void getrectc(double x0, double x1, double x2, double x3,
	     double u0, double u1, double u2, double u3, 
	     double v0, double v1, double v2, double v3,
	     GLdouble v[4][4], double o1, double o2) {
  double tmp;
  tmp = u0; u0 += o1 * v0; v0 += o2 * tmp;
  tmp = u1; u1 += o1 * v1; v1 += o2 * tmp;
  tmp = u2; u2 += o1 * v2; v2 += o2 * tmp;
  tmp = u3; u3 += o1 * v3; v3 += o2 * tmp;

  v[0][0] = x0 + .5 * (-u0 - v0); 
  v[0][1] = x1 + .5 * (-u1 - v1); 
  v[0][2] = x2 + .5 * (-u2 - v2); 
  v[0][3] = x3 + .5 * (-u3 - v3);

  v[1][0] = x0 + .5 * (u0 - v0); 
  v[1][1] = x1 + .5 * (u1 - v1); 
  v[1][2] = x2 + .5 * (u2 - v2); 
  v[1][3] = x3 + .5 * (u3 - v3);

  v[2][0] = x0 + .5 * (-u0 + v0); 
  v[2][1] = x1 + .5 * (-u1 + v1); 
  v[2][2] = x2 + .5 * (-u2 + v2); 
  v[2][3] = x3 + .5 * (-u3 + v3);

  v[3][0] = x0 + .5 * (u0 + v0); 
  v[3][1] = x1 + .5 * (u1 + v1); 
  v[3][2] = x2 + .5 * (u2 + v2); 
  v[3][3] = x3 + .5 * (u3 + v3);
}

void getplane(double x0, double x1, double x2, double x3,
	      double u0, double u1, double u2, double u3,
	      double a0, double a1, double a2, double v3,
	      GLdouble v[4][4], int normal) {
  double v0, v1, v2;
  if(normal) {
  v0 = u1 * a2 - u2 * a1;
  v1 = -u0 * a2 + u2 * a0;
  v2 = u0 * a1 - u1 * a0;
  
  double m = sqrt(u0*u0+u1*u1+u2*u2) / sqrt(v0*v0+v1*v1+v2*v2);
  v0 *= m;
  v1 *= m;
  v2 *= m;
  } else {
    v0 = a0; v1 = a1; v2 = a2;
  }
  v[0][0] = x0; v[0][1] = x1; v[0][2] = x2;
  v[1][0] = x0 + u0; v[1][1] = x1 + u1; v[1][2] = x2 + u2;
  v[2][0] = x0 + v0; v[2][1] = x1 + v1; v[2][2] = x2 + v2;
  v[3][0] = x0 + u0 + v0; v[3][1] = x1 + u1 + v1; v[3][2] = x2 + u2 + v2;

  v[0][3] = x3 + .5 * (-u3 - v3);
  v[1][3] = x3 + .5 * (u3 - v3);
  v[2][3] = x3 + .5 * (-u3 + v3);
  v[3][3] = x3 + .5 * (u3 + v3);
}


#define NOISE (noiseargs[0] += 15.1, noiseargs[1] += 3.2, noiseargs[3] += 0.01*offs, \
		Perlin::noise3(noiseargs))
#define NOISE_C (0.4 * NOISE + 0.8)
#define NOISE_T (5 * NOISE)
#define NOISE_M (5 * NOISE)
  
  void ShaderRect::getcoords(GLdouble v[4][4], GLdouble v2[4][4], GLdouble v3[4][4], GLfloat col[4][3], float offs) {

    glActiveTexture(GL_TEXTURE3);
    float disp = 0.5;
    GLfloat mat[4] = {disp, 0, 0, disp};
    // GLfloat mat[4] = {0.15, 0, 0, 0.15};
    // GLfloat mat[4] = {1.2, 0, 0, 1.2};
    glTexEnvfv(GL_TEXTURE_SHADER_NV, GL_OFFSET_TEXTURE_MATRIX_NV, mat);

    float noiseargs[3] = {
	1.5,
	2.3,
	offs * 0.1
    };

	double m = 1.5;
	getplane(NOISE_T, NOISE_T, NOISE_T, NOISE + 10,
		 m*NOISE_T, m*NOISE_T, m*NOISE_T, NOISE * 2, 
		 m*NOISE_T, m*NOISE_T, m*NOISE_T, NOISE * 2, v, 1);

	m = 4;
	getrectc(NOISE_T, NOISE_T, NOISE_T, NOISE + 2,
		 m*NOISE_T, m*NOISE_T, m*NOISE_T, NOISE*5,
		 m*NOISE_T, m*NOISE_T, m*NOISE_T, NOISE*5, v2, NOISE, NOISE);

	//v2[0][3] = NOISE_C;
	//v2[1][3] = NOISE_C;NOISE + v2[0][3];
	//v2[2][3] = NOISE_C;NOISE + v2[0][3];
	//v2[3][3] = NOISE_C;v2[1][3] + v2[2][3] - v2[0][3];

	m = 0.05;
	getplane(NOISE_T, NOISE_T, NOISE_T, NOISE + 3,
		 m*NOISE_T, m*NOISE_T, m*NOISE_T, NOISE * 2, 
		 m*NOISE_T, m*NOISE_T, m*NOISE_T, NOISE * 2, v3, 1);

	col[0][0] = NOISE_C; col[0][1] = NOISE_C; col[0][2] = NOISE_C;
	col[1][0] = NOISE_C; col[1][1] = NOISE_C; col[1][2] = NOISE_C;
	col[2][0] = NOISE_C; col[2][1] = NOISE_C; col[2][2] = NOISE_C;
	col[3][0] = NOISE_C; col[3][1] = NOISE_C; col[3][2] = NOISE_C;
	
  }


}
