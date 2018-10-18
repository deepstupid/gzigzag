#include <math.h>
#include <iostream>
#include <vector>
#include <GL/glut.h>
#include <unistd.h>
#include <stdio.h>

namespace Perlin {
#include "perlin.c"
}


#define GLERR { int er = glGetError(); if(er != GL_NO_ERROR) \
		    cout << "ERROR "<<__FILE__<<" "<<__LINE__ \
			<<gluErrorString(er)<<"\n"; \
	    }

using std::cout;



struct Err {};


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

float offs = 0;


// Get the corner points of a rectange centered at x with sides u and v
void getrectc(double x0, double x1, double x2, double x3,
	     double u0, double u1, double u2, double u3, 
	     double v0, double v1, double v2, double v3,
	     double v[4][4]) {
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

// Get the corner points of a rectange 
void getrect(double x0, double x1, double x2, double x3,
	     double u0, double u1, double u2, double u3, 
	     double v0, double v1, double v2, double v3,
	     double v[4][4]) {
  v[0][0] = x0;
  v[0][1] = x1;
  v[0][2] = x2;
  v[0][3] = x3;

  v[1][0] = x0 + u0;
  v[1][1] = x1 + u1;
  v[1][2] = x2 + u2;
  v[1][3] = x3 + u3;

  v[2][0] = x0 + v0;
  v[2][1] = x1 + v1;
  v[2][2] = x2 + v2;
  v[2][3] = x3 + v3;

  v[3][0] = x0 + u0 + v0;
  v[3][1] = x1 + u1 + v1; 
  v[3][2] = x2 + u2 + v2; 
  v[3][3] = x3 + u3 + v3;
}


void getplane(double x0, double x1, double x2, double x3,
	      double u0, double u1, double u2, double u3,
	      double a0, double a1, double a2, double v3,
	      double v[4][4], int normal) {
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

void display() {

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glColor3f(1, 1, 1);

    glEnable(GL_BLEND);
    glEnable(GL_ALPHA_TEST);
    glAlphaFunc(GL_GREATER, 0.1);
    glEnable(GL_DEPTH_TEST);

    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    GLERR

    float noiseargs[3] = {
	1.5,
	2.3,
	offs * 0.1
    };

    float noise10args[3] = {
	1.5,
	2.3,
    };

for(int i=0; i<9; i++) {

	glPushMatrix();
	glTranslatef((i%3) * 350, (i/3) * 350, 0);
	cout << "Rend "<<i<<"\n";


#define NOISE (noiseargs[0] += 15.1, noiseargs[1] += 3.2, noiseargs[3] += 0.01*offs, \
		Perlin::noise3(noiseargs))
#define NOISE_C (0.8 * NOISE + 0.8)
#define NOISE_T (5 * NOISE)
#define NOISE_M (5 * NOISE)

	// glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	//
	// glEnable(GL_TEXTURE_3D);


	glColor3f(1, 1, 1);

	double v[4][4];
	double v2[4][4];
	double v3[4][4];
	cout << "Mid-1Rend "<<i<<"\n";
	getplane(NOISE_T, NOISE_T, NOISE_T, NOISE + 1,
		 NOISE_T, NOISE_T, NOISE_T, NOISE * 2, 
		 NOISE_T, NOISE_T, NOISE_T, NOISE * 2,v, 1);

	double m = 4;

	cout << "Mid0Rend "<<i<<"\n";
	getrectc(NOISE_T, NOISE_T, NOISE_T, NOISE + 2,
		 m*NOISE_T, m*NOISE_T, m*NOISE_T, NOISE*5,
		 m*NOISE_T, m*NOISE_T, m*NOISE_T, NOISE*5, v2);

	//v2[0][3] = NOISE_C;
	//v2[1][3] = NOISE_C;NOISE + v2[0][3];
	//v2[2][3] = NOISE_C;NOISE + v2[0][3];
	//v2[3][3] = NOISE_C;v2[1][3] + v2[2][3] - v2[0][3];

	cout << "MidRend "<<i<<"\n";

	m = 0.05;
	getplane(NOISE_T, NOISE_T, NOISE_T, NOISE + 2,
		 m*NOISE_T, m*NOISE_T, m*NOISE_T, NOISE * 2, 
		 m*NOISE_T, m*NOISE_T, m*NOISE_T, NOISE * 2, v3, 1);
	

	glBegin(GL_QUAD_STRIP);

	glColor3f(NOISE_C, NOISE_C, NOISE_C);
	glTexCoord4dv(v[0]);
	glMultiTexCoord2d(3, 0, 0);
	glMultiTexCoord4dv(1, v3[0]);
	glMultiTexCoord4dv(2, v2[0]);
	glVertex3f(0,0,0);

	glColor3f(NOISE_C, NOISE_C, NOISE_C);
	glTexCoord4dv(v[1]);
	glMultiTexCoord2d(3, 0, 1);
	glMultiTexCoord4dv(1, v3[1]);
	glMultiTexCoord4dv(2, v2[1]);
	glVertex3f(0,300,0);

	glColor3f(NOISE_C, NOISE_C, NOISE_C);
	glTexCoord4dv(v[2]);
	glMultiTexCoord2d(3, 1, 0);
	glMultiTexCoord4dv(1, v3[2]);
	glMultiTexCoord4dv(2, v2[2]);
	glVertex3f(300,0,0);

	glColor3f(NOISE_C, NOISE_C, NOISE_C);
	glTexCoord4dv(v[3]);
	glMultiTexCoord2d(3, 1, 1);
	glMultiTexCoord4dv(1, v3[3]);
	glMultiTexCoord4dv(2, v2[3]);
	glVertex3f(300,300,0);

	glEnd();

	glPopMatrix();

	// glAccum(GL_ACCUM, 1.0/8);
	cout << "EndRend "<<i<<"\n";
 }


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
    glEnable(GL_TEXTURE_SHADER_NV);

    glClearColor(0, 0.2, 0.2, 0);
    read3File("turb.32t", turb, 2);
    read2File("cell.2t", cell, 4);
    read3File("spots.3t", spots, 1);
    read3File("col.33t", col, 3);

    /*
    for(int i=0; i<TEXSIZE * TEXSIZE * TEXSIZE; i++) {
	((char *)turb)[i] = i % 255;
    }
    */

    // Texture unit 0 ----------------------------------------------------
    glActiveTexture(GL_TEXTURE0);

    glTexEnvi(GL_TEXTURE_SHADER_NV, GL_SHADER_OPERATION_NV, GL_TEXTURE_3D);

    glEnable(GL_TEXTURE_3D);
    GLERR
    glBindTexture(GL_TEXTURE_3D, 1); // the offsetter

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
	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    glTexImage3D(GL_TEXTURE_3D, 0, GL_DSDT_NV, 
		    TEXSIZE, TEXSIZE, TEXSIZE, 0, GL_DSDT_NV, GL_BYTE, turb);

    GLERR
    glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

    // Texture unit 3 ----------------------------------------------------

    glActiveTexture(GL_TEXTURE3);

#define SHCONST(a) \
    glGetTexEnviv(GL_TEXTURE_SHADER_NV, GL_SHADER_CONSISTENT_NV, &c); \
    cout << "SCONSISTENT: "<<a<<" "<<c<<"\n";

    glTexEnvi(GL_TEXTURE_SHADER_NV, GL_SHADER_OPERATION_NV, GL_OFFSET_TEXTURE_2D_NV);
    SHCONST("")
    glTexEnvi(GL_TEXTURE_SHADER_NV, GL_PREVIOUS_TEXTURE_INPUT_NV, GL_TEXTURE0);
    SHCONST("")

    glBindTexture(GL_TEXTURE_2D, 2); // The cell

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
   
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 
		    TEX2, TEX2, 0, GL_RGBA, GL_UNSIGNED_BYTE, cell);

    glDisable(GL_TEXTURE_3D);
    // glEnable(GL_TEXTURE_2D);
    glDisable(GL_TEXTURE_2D);

    float disp = 0.6;
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
    glBindTexture(GL_TEXTURE_3D, 3); //

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
	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    glTexImage3D(GL_TEXTURE_3D, 0, GL_RGB, 
		 TEXSIZE, TEXSIZE, TEXSIZE, 0, GL_RGB, GL_UNSIGNED_BYTE, col);

    GLERR
    glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

    // Texture unit 2 ----------------------------------------------------

    glActiveTexture(GL_TEXTURE2);

    glTexEnvi(GL_TEXTURE_SHADER_NV, GL_SHADER_OPERATION_NV, GL_TEXTURE_3D);

    glEnable(GL_TEXTURE_3D);
    GLERR
    glBindTexture(GL_TEXTURE_3D, 4); //

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
	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    glTexImage3D(GL_TEXTURE_3D, 0, GL_ALPHA, 
		 TEXSIZE, TEXSIZE, TEXSIZE, 0, GL_ALPHA, GL_UNSIGNED_BYTE, spots);

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

