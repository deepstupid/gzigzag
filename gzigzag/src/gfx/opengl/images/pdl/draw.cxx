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

double t = 0;

#define TEXSIZE 128

char turb[TEXSIZE][TEXSIZE][TEXSIZE];

void read3File(const char *name, void *ptr, int b) {
    int n = b * TEXSIZE * TEXSIZE * TEXSIZE;
    FILE *f = fopen(name, "r");
    if(!f) { abort(); }
    int i;
    // if(fread(ptr, n, 1, f) != n) abort();
    if((i = fread(ptr, 1, n, f)) != n) fprintf(stderr, "Reading : %d of %d\n", i, n);
    fclose(f);
}

float offs = 0;

void display() {

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//    glClear(GL_ACCUM_BUFFER_BIT);

    // cout << "DISPLAY\n";

    glColor3f(1, 1, 1);

    glEnable(GL_BLEND);
    glEnable(GL_ALPHA_TEST);
    glAlphaFunc(GL_GREATER, 0.1);
    glEnable(GL_DEPTH_TEST);

    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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

    float noiseargs[3] = {
	1.5,
	2.3,
	offs * 0.1
    };

    float noise10args[3] = {
	1.5,
	2.3,
    };

#define NOISE (noiseargs[0] += 15.1, noiseargs[1] += 3.2, noiseargs[3] += 0.01*offs, \
		Perlin::noise3(noiseargs))
#define NOISE_T (25 * NOISE)
#define NOISE_M (5 * NOISE)

    for(int i=0; i<1; i++) {
	// glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	//
	glEnable(GL_TEXTURE_3D);

	glPushMatrix();

	glColor3f(1, 1, 1);

	glBegin(GL_TRIANGLES);

#define TC \
	glTexCoord3f(NOISE_T, NOISE_T, NOISE_T); \
	// glMultiTexCoord2dARB(1, NOISE_M, NOISE_M); \
	// glMultiTexCoord2dARB(2, NOISE_T, NOISE_T); \
	// glMultiTexCoord2dARB(3, NOISE_M, NOISE_M); \

	TC
	// glTexCoord3f(0.1, 0.1, 0.1);
	glVertex3f(0,0,0);
	TC
	// glTexCoord3f(0.1, 0.5, 0.1);
	glVertex3f(0,3000,0);
	TC
	// glTexCoord3f(0.5, 0.5, 0.5);
	glVertex3f(3000,0,0);

	glEnd();

	glPopMatrix();

	// glAccum(GL_ACCUM, 1.0/8);


    }

    offs += 0.01;

    // glAccum(GL_RETURN, 1.0);

    GLERR
    glEnable(GL_TEXTURE_3D);

    t += 0.02;

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
//    glEnable(GL_TEXTURE_SHADER_NV);

    glClearColor(0, 0, 0, 0);

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

    read3File("turb.3t", turb, 1);

    /*
    for(int i=0; i<TEXSIZE * TEXSIZE * TEXSIZE; i++) {
	((char *)turb)[i] = i % 255;
    }
    */

    glEnable(GL_TEXTURE_3D);
    GLERR

    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAX_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_BASE_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_LOD, 0);
    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAX_LOD, 0);
    GLERR
	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    glTexImage3D(GL_TEXTURE_3D, 0, GL_LUMINANCE, 
		    TEXSIZE, TEXSIZE, TEXSIZE, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, turb);

    GLERR
    glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);


//    glEnable(GL_REGISTER_COMBINERS_NV);
 //   glCombinerInputNV(GL_COMBINER0_NV, GL_RGB, 

}


void idle() { 
    glutPostRedisplay(); 
}

int main(int argc, char **argv) {
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA);
    // glutInitDisplayMode(GLUT_SINGLE | GLUT_RGBA);
    glutInitWindowSize(1400,1100);
    glutInitWindowPosition(0,0);
    glutCreateWindow("fontTest");


    init();

    glutReshapeFunc(reshape);
    glutDisplayFunc(display);
    glutIdleFunc(idle);
    glutMainLoop();
    return 0;
}

