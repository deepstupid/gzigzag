/** Note by Tjl:
 * This code has been taken from the CD distributed with
 * Texturing&Modeling 2nd edition. Ken Perlin also distributes
 * this on his home page.
 * 
 * No license has been specified in this code or on the web page, 
 * so I'm assuming it's freely distributable and including it here.
 * If this is not the case, please inform me and I will remove it promptly.
 */

#include <stdlib.h>
#include <stdio.h>
#include <math.h>

float bias(float a, float b)
{
	return pow(a, log(b) / log(0.5));
}

float gain(float a, float b)
{
	float p = log(1. - b) / log(0.5);

	if (a < .001)
		return 0.;
	else if (a > .999)
		return 1.;
	if (a < 0.5)
		return pow(2 * a, p) / 2;
	else
		return 1. - pow(2 * (1. - a), p) / 2;
}

float noise1(float arg);
float noise2(float vec[]);
float noise3(float vec[]);

float noise(float vec[], int len)
{
	switch (len) {
	case 0:
		return 0.;
	case 1:
		return noise1(vec[0]);
	case 2:
		return noise2(vec);
	default:
		return noise3(vec);
	}
}

float turbulence(float *v, float freq)
{
	float t, vec[3];

	for (t = 0. ; freq >= 1. ; freq /= 2) {
		vec[0] = freq * v[0];
		vec[1] = freq * v[1];
		vec[2] = freq * v[2];
		t += fabs(noise3(vec)) / freq;
	}
	return t;
}

/* noise functions over 1, 2, and 3 dimensions */

#define B 0x100
#define BM 0xff

#define N 0x1000
#define NP 12   /* 2^N */
#define NM 0xfff

static int p[B + B + 2];
static float g3[B + B + 2][3];
static float g2[B + B + 2][2];
static float g1[B + B + 2];
static int start = 1;

static void init(void);

#define s_curve(t) ( t * t * (3. - 2. * t) )

#define lerp(t, a, b) ( a + t * (b - a) )

#define setup(i,b0,b1,r0,r1)\
	t = vec[i] + N;\
	b0 = ((int)t) & BM;\
	b1 = (b0+1) & BM;\
	r0 = t - (int)t;\
	r1 = r0 - 1.;

float noise1(float arg)
{
	int bx0, bx1;
	float rx0, rx1, sx, t, u, v, vec[1];

	vec[0] = arg;
	if (start) {
		start = 0;
		init();
	}

	setup(0, bx0,bx1, rx0,rx1);

	sx = s_curve(rx0);

	u = rx0 * g1[ p[ bx0 ] ];
	v = rx1 * g1[ p[ bx1 ] ];

	return lerp(sx, u, v);
}

float noise2(float vec[2])
{
	int bx0, bx1, by0, by1, b00, b10, b01, b11;
	float rx0, rx1, ry0, ry1, *q, sx, sy, a, b, t, u, v;
	register int i, j;

	if (start) {
		start = 0;
		init();
	}

	setup(0, bx0,bx1, rx0,rx1);
	setup(1, by0,by1, ry0,ry1);

	i = p[ bx0 ];
	j = p[ bx1 ];

	b00 = p[ i + by0 ];
	b10 = p[ j + by0 ];
	b01 = p[ i + by1 ];
	b11 = p[ j + by1 ];

	sx = s_curve(rx0);
	sy = s_curve(ry0);

#define at2(rx,ry) ( rx * q[0] + ry * q[1] )

	q = g2[ b00 ] ; u = at2(rx0,ry0);
	q = g2[ b10 ] ; v = at2(rx1,ry0);
	a = lerp(sx, u, v);

	q = g2[ b01 ] ; u = at2(rx0,ry1);
	q = g2[ b11 ] ; v = at2(rx1,ry1);
	b = lerp(sx, u, v);

	return lerp(sy, a, b);
}

float noise3(float vec[3])
{
	int bx0, bx1, by0, by1, bz0, bz1, b00, b10, b01, b11;
	float rx0, rx1, ry0, ry1, rz0, rz1, *q, sy, sz, a, b, c, d, t, u, v;
	register int i, j;

	if (start) {
		start = 0;
		init();
	}

	setup(0, bx0,bx1, rx0,rx1);
	setup(1, by0,by1, ry0,ry1);
	setup(2, bz0,bz1, rz0,rz1);

	i = p[ bx0 ];
	j = p[ bx1 ];

	b00 = p[ i + by0 ];
	b10 = p[ j + by0 ];
	b01 = p[ i + by1 ];
	b11 = p[ j + by1 ];

	t  = s_curve(rx0);
	sy = s_curve(ry0);
	sz = s_curve(rz0);

#define at3(rx,ry,rz) ( rx * q[0] + ry * q[1] + rz * q[2] )

	q = g3[ b00 + bz0 ] ; u = at3(rx0,ry0,rz0);
	q = g3[ b10 + bz0 ] ; v = at3(rx1,ry0,rz0);
	a = lerp(t, u, v);

	q = g3[ b01 + bz0 ] ; u = at3(rx0,ry1,rz0);
	q = g3[ b11 + bz0 ] ; v = at3(rx1,ry1,rz0);
	b = lerp(t, u, v);

	c = lerp(sy, a, b);

	q = g3[ b00 + bz1 ] ; u = at3(rx0,ry0,rz1);
	q = g3[ b10 + bz1 ] ; v = at3(rx1,ry0,rz1);
	a = lerp(t, u, v);

	q = g3[ b01 + bz1 ] ; u = at3(rx0,ry1,rz1);
	q = g3[ b11 + bz1 ] ; v = at3(rx1,ry1,rz1);
	b = lerp(t, u, v);

	d = lerp(sy, a, b);

	return lerp(sz, c, d);
}

static void normalize2(float v[2])
{
	float s;

	s = sqrt(v[0] * v[0] + v[1] * v[1]);
	v[0] = v[0] / s;
	v[1] = v[1] / s;
}

static void normalize3(float v[3])
{
	float s;

	s = sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
	v[0] = v[0] / s;
	v[1] = v[1] / s;
	v[2] = v[2] / s;
}

static void init(void)
{
	int i, j, k;

	for (i = 0 ; i < B ; i++) {
		p[i] = i;

		g1[i] = (float)((random() % (B + B)) - B) / B;

		for (j = 0 ; j < 2 ; j++)
			g2[i][j] = (float)((random() % (B + B)) - B) / B;
		normalize2(g2[i]);

		for (j = 0 ; j < 3 ; j++)
			g3[i][j] = (float)((random() % (B + B)) - B) / B;
		normalize3(g3[i]);
	}

	while (--i) {
		k = p[i];
		p[i] = p[j = random() % B];
		p[j] = k;
	}

	for (i = 0 ; i < B + 2 ; i++) {
		p[B + i] = p[i];
		g1[B + i] = g1[i];
		for (j = 0 ; j < 2 ; j++)
			g2[B + i][j] = g2[i][j];
		for (j = 0 ; j < 3 ; j++)
			g3[B + i][j] = g3[i][j];
	}
}

/* More additions by tjl */

#define csetup(i,b)\
	t = vec[i] + N;\
	b = ((int)t) & BM;

float cellnoise3(float vec[3]) {
    int bx, by, bz;
    float t;
    if (start) {
	    start = 0;
	    init();
    }
    csetup(0, bx);
    csetup(1, by);
    csetup(2, bz);
    return g1[ p[ p[ bx ] + by ] + bz ];
}

#define vsetup(v,b)\
	t = v + N;\
	b = ((int)t) & BM;


void voronoise3(float vec[3], float jitter, float *fo1, float *fo2, float *fd) {
    int x, y, z;
    int bx, by, bz;
    float t;
    float fx, fy, fz;
    float o1[3], o2[3], d[2];
    if (start) {
	    start = 0;
	    init();
    }

    d[0] = 1000; d[1] = 1000;
    vsetup(vec[0]+x, bx);
    vsetup(vec[1]+y, by);
    vsetup(vec[2]+z, bz);
    bx ++; by ++; bz ++; // Make it so that we can always add / subtract one
    fx = floor(vec[0]);
    fy = floor(vec[1]);
    fz = floor(vec[2]);
    for(x=-1; x<=1; x++) {
	for(y=-1; y<=1; y++) {
	    for(z=-1; z<=1; z++)  {
		int rx = bx + x, ry = by + y, rz = bz + z;
		float ox = g1[ p[ p[ rx ] + ry ] + rz ];
		float oy = g1[ p[ p[ rx ^ 64 ] + ry ] + rz ];
		float oz = g1[ p[ p[ rx ^ 32 ] + ry ] + rz ];
		float px = 0.5 + (fx + x + jitter * ox);
		float py = 0.5 + (fy + y + jitter * oy);
		float pz = 0.5 + (fz + z + jitter * oz);
		float dx = vec[0] - px;
		float dy = vec[1] - py;
		float dz = vec[2] - pz;
		float dist = sqrt(dx*dx + dy*dy + dz * dz);
		/* printf("Jitt: %f %f %f -- %f %f %f\n", ox, oy, oz, px, py, pz);
		 */
		if(dist < d[0]) {
		    d[1] = d[0];
		    o2[0] = o1[0];
		    o2[1] = o1[1];
		    o2[2] = o1[2];

		    d[0] = dist;
		    o1[0] = px;
		    o1[1] = py;
		    o1[2] = pz;
		} else if(dist < d[1]) {
		    d[1] = dist;
		    o2[0] = px;
		    o2[1] = py;
		    o2[2] = pz;
		}
	    }
	}
    }
    fo1[0] = o1[0]; fo2[0] = o2[0];
    fo1[1] = o1[1]; fo2[1] = o2[1];
    fo1[2] = o1[2]; fo2[2] = o2[2];
    fd[0] = d[0];
    fd[1] = d[1];
}

/** A version of voronoise that can save a lot of time when used
 * for generating spots.
 */
float voronoise3_spot(float vec[3], float jitter, float maxdist) {
    int x, y, z;
    int bx, by, bz;
    float t;
    float fx, fy, fz;
    float ret = 1000;
    if (start) {
	    start = 0;
	    init();
    }
    // printf("Vorospot %f %f %f\n", vec[0], vec[1], vec[2]);

    vsetup(vec[0], bx);
    vsetup(vec[1], by);
    vsetup(vec[2], bz);
    bx ++; by ++; bz ++; // Make it so that we can always add / subtract one
    fx = floor(vec[0]);
    fy = floor(vec[1]);
    fz = floor(vec[2]);
    for(x=-1; x<=1; x++) {
	float px0 = fx + x + 0.5;
	if(fabs(vec[0] - px0) > maxdist + jitter) continue;
	for(y=-1; y<=1; y++) {
	    float py0 = fy + y + 0.5;
	    if(fabs(vec[1] - py0) > maxdist + jitter) continue;
	    for(z=-1; z<=1; z++)  {
		float pz0 = fz + z + 0.5;
		if(fabs(vec[2] - pz0) > maxdist + jitter) continue;

		{
		    int rx = bx + x, ry = by + y, rz = bz + z;
		    float ox = g1[ p[ p[ rx ] + ry ] + rz ];
		    float oy = g1[ p[ p[ rx ^ 64 ] + ry ] + rz ];
		    float oz = g1[ p[ p[ rx ^ 32 ] + ry ] + rz ];
		    float px = px0 + jitter * ox;
		    float py = py0 + jitter * oy;
		    float pz = pz0 + jitter * oz;
		    float dx = vec[0] - px;
		    float dy = vec[1] - py;
		    float dz = vec[2] - pz;
		    float dist = sqrt(dx*dx + dy*dy + dz * dz);
		    // printf("Jitt: %d %d %d %f %f %f -- %f %f %f\n", x, y, z, ox, oy, oz, px, py, pz);
		    if(dist < ret) 
			ret = dist;
		}
	    }
	}
    }
    return ret;
}

float smoothstep(float low, float high, float x) {
    float norm;
    if(x < low) return 0;
    if(x > high) return 1;
    norm = (x-low) / (high-low);
    return s_curve(norm);
}

// lacu > 1
float fBm(float *v, int octaves, float lacu, float gain) {
    int i;
    float res = 0;
    float amp = 1;
    float vec[3]; vec[0] = v[0]; vec[1] = v[1]; vec[2] = v[2];
    for(i=0; i<octaves; i++) {
	res += amp * noise3(vec);
	vec[0] *= lacu;
	vec[1] *= lacu;
	vec[2] *= lacu;
	amp *= gain;
    }
    return res;
}

// lacu > 1
float faBm(float *v, int octaves, float lacu, float gain) {
    int i;
    float res = 0;
    float amp = 1;
    float vec[3]; vec[0] = v[0]; vec[1] = v[1]; vec[2] = v[2];
    for(i=0; i<octaves; i++) {
	res += fabs(amp * noise3(vec));
	vec[0] *= lacu;
	vec[1] *= lacu;
	vec[2] *= lacu;
	amp *= gain;
    }
    return res;
}
