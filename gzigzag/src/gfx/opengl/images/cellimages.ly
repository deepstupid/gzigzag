Title: Pretty cells for GZigZag
Rcs-Id: $Id: cellimages.ly,v 1.5 2002/03/03 21:12:23 tjl Exp $
Ly-Version: 0.0.2

<h1>Variable cells using the Renderman interface</h1>

<i>Tuomas J. Lukka</i>

<h2>Abstract</h2>

<h2>Introduction</h2>

For the time being, we shall create the cell and the boundary from a single
rectangle lying in the Z=0 plane by various colorings and distortions.

<h2>The coordinates and a basic cell</h2>

In order to make it easier to create different cell images, we
shall make a function that returns the a cell radial coordinate 
of a point. The function is zero at the "edge" of the standard cell,
positive outside and negative inside, somewhat proportional to the
distance from the edge.

We do need to generate several different types of cell coordinates,
separately for the cell bodies and the argh connections.

-- Cell radial function:
	float cellrad(uniform float type; 
		      float x,y) {
	    extern color Oi;
	    if(type == 1) {
		float xa = abs(x) - 1;
		float ya = abs(y) - 0.5;
		return max(xa, ya);
	    } else if(type == 2) {
		float xa = abs(x) - 1;
		float ya = abs(y) - 0.5;
		float sq = max(xa, ya);

There's a nasty bug in BMRT: return inside if does the wrong thing
(I think).

		float out;
		// if(y > 0.01) {
		    float dx = abs(x) - 0.66;
		    if(dx < 0) {
			float dy = abs(y) - (0.83);
			float r = sqrt(dx*dx + dy*dy);
			out = min(sq, max(0.33 - r, -0.2));
		    } else {
			out = sq;
		    }
		// } else {
		//     out = sq;
		// }
		return out;
	    }
	}

-- Cell radial tester:
	point p = transform("object", P);
	float r = cellrad(type, xcomp(p), ycomp(p));
	Ci = color noise(20*r) * (r < 0 ? 1 : 0.5);
	

Then, we can define a really simple cell.

-- a really simple cell:
	point p = transform("object", P);
	float r = cellrad(type, xcomp(p), ycomp(p));

If outside, transparent

	if(r > 0.1) {
	    Oi = 0; Ci = 0;

Blue border

	} else if(r > -0.1) {
	    Ci = color(0, 0, 1);

White inside.

	} else {
	    Ci = color(1, 1, 1);
	}

<h2>Noises</h2>

In order to make the cell connectors smooth, but the cells different,
we must blend the noise functions at the edges.

-- Blending of noises:
	float noiseBlendFact(point p) {
	    float dx = 1.33 - abs(xcomp(p)) ;
	    float dy = 0.83 - abs(ycomp(p)) ;
	    // return 0;
	    return smoothstep(0.00, 0.3, min(dx, dy));
	}

The correct way of weighing a noise is a bit tricky, since
the top and bottom edges must fit together, but we must respect any
multiplications of the noise. Because of this, we need a function that gives
the value of the noise to be used at the border at any point.
The parameter is p.

	point edgenoisePoint(point p) {
	    float x = xcomp(p);
	    float y = ycomp(p);
	    float xa = abs(x) - 0.5;
	    float ya = abs(y);
	    if(xa > ya) {
		if(x > 0) 
		    x -= 2.66;
	    } else {
		if(y > 0)
		   y -= 2*0.83;
	    }
	    return point(x, y, zcomp(p));
	}

We can precalculate both of these functions.

-- calculate noise blending:
	float noiseblend = noiseBlendFact(p);
	point noisepoint = edgenoisePoint(p);

The exception function.

-- Exception function:
	float exception(point p) {
	    vector v = vsnoise(transform("shader", P) + vector(0, 0, 0.25));
	    float x = xcomp(v);
	    float y = ycomp(v);
	    float z = zcomp(v);
	    float r = x * y * z;
	    return 3 * r * smoothstep(abs(r), 0.2, 0.25);
	}

Simple cell with red exceptions in the background

-- a really simple cell with red blotches:
	point p = transform("object", P);
	float r = cellrad(type, xcomp(p), ycomp(p));
	if(r > 0.1) {
	    Oi = 0; Ci = 0;
	} else if(r > -0.1) {
	    Ci = color(0, 0, 1);
	} else {
	    Ci = color(1, 1, 1) - clamp(exception(3*p), 0, 1) * color(0, 1, 1);
	}

-- a slightly deformed cell with red blotches:
	point p = transform("object", P);
	float r = cellrad(type, xcomp(p), ycomp(p));
	if(r > 0.1) {
	    Oi = 0; Ci = 0;
	} else if(r > -0.1) {
	    Ci = color(0, 0, 1);
	} else {
	    Ci = color(1, 1, 1) - clamp(exception(3*p), 0, 1) * color(0, 1, 1);
	}

<h2>Lighting</h2>

A slightly convex cell, using bump mapping.

-- bumped plastic cell:
	float r = cellrad(type, xcomp(p), ycomp(p));
	if(r > 0) {
	    Oi = 0; Ci = 0;
	} else {
	    float amp = -30 * sqr(sqr(sqr(0.5+r)));
	    P += N * amp;
	    N = normalize(calculatenormal(P));
	    -- modify surface if appropriate.
	    Ci = MaterialPlastic(N, color(1.0, 1.0, 1.0), 0.2, 0.2, 0.5, 0.1);
	    // Ci = color(0.5, 0.5, 0.5);
	}
	
-- modify surface if appropriate:
	if(postmodify == 1) {
	    float noi = snoise(2 * transform("shader", P));
	    float depth = 0.005 * smoothstep(-0.03, 0, noi) 
			* (1 - smoothstep(0, 0.03, noi));
	    P += N * depth;
	    N = normalize(calculatenormal(P));
	}

-- beveled marble cell:
	float r = cellrad(type, xcomp(p), ycomp(p));
	P += N * clamp(-2*r, 0, 0.1);
	N = normalize(calculatenormal(P));
	point p = transform("object", P);
	if(r > 0) {
	    Ci = 0; Oi = 0;
	} else {
	    vector shadP = transform("shader", P);
	    float gain = 0.9 - 0.9 * 
		mix(
		    noise(noisepoint * 1.5),
		    noise(shadP * 1.5), 
		    noiseblend);

	    float offs = mix(
		    turb(noisepoint+0.5, 8, 1.8, gain),
		    turb(shadP+0.5, 8, 1.8, gain),
		    noiseblend);

	    float freq = noise(shadP);
	    float freqn = noise(noisepoint);

	    float c = ycomp(p) + offs;
	    float cn = ycomp(noisepoint) + offs;
	    c = 20 * c * freq;
	    cn = 20 * cn * freqn;
	    c = mod(c, PI);
	    cn = mod(cn, PI);
	    float phase = 0.5 + 0.5 * sin(mix(cn, c, noiseblend));
	    float amt = 0.3 + 0.7 * phase;
	    color basecolor = color(amt, amt, amt);
	    Ci = MaterialPlastic(N, basecolor, 
		0.6, 0.3, 0.1, 0.1);
	}
	

<h2>The rest</h2>


-- file "cell.sl":
	-- useful routines.
	-- Standard includes.
	-- Cell radial function.
	-- Exception function.
	-- Blending of noises.
	surface cell(
	string shader;
	uniform float type;
	uniform float predeform;
	uniform float postmodify;
	    ) {
	    point p = transform("object", P);
	    -- calculate noise blending.

	    if(predeform == 1) {
		P = P + 1.5*
		    mix(vector noise(noisepoint + 3),
			vector noise(P + 3) ,
			noiseblend)
		    * 
		    mix(exception(4 * noisepoint), 
			exception(4 * P),
			noiseblend);
	    }
	    p = transform("object", P);
	    if(shader == "cell1") {
		-- a really simple cell.
	    } else if(shader == "cell2") {
		-- a really simple cell with red blotches.
	    } else if(shader == "cell3") {
		-- a slightly deformed cell with red blotches.
	    } else if(shader == "cell4") {
		-- bumped plastic cell.
	    } else if(shader == "cell5") {
		-- beveled marble cell.
	    } else if(shader == "celltester") {
		-- Cell radial tester.
	    }
	}

-- surface:
    Surface "cell" "string shader" "cell5" "float type" [2] "float postmodify" [1] "float predeform" [0]

-- Standard includes:
    // none

-- useful routines:
	// From ARMAN.

	float sqr(float x) { return x * x; }
	#define snoise(p) (2 * noise(p) - 1)
	#define vsnoise(p) (2 * vector noise(p) - 1)
	/* Compute the color of the surface using a simple plastic-like BRDF.
	 * Typical values are Ka=1, Kd=0.8, Ks=0.5, roughness=0.1.
	 */
	color MaterialPlastic (normal Nf;  color basecolor;
	                       float Ka, Kd, Ks, roughness;)
	{
	     extern vector I;
	     return basecolor * (Ka*ambient() + Kd*diffuse(Nf))
			+ Ks*specular(Nf,-normalize(I),roughness);
	}

	float fBm(point p; 
		uniform float octaves;
		float lacunarity;
		float gain;) {
	    float amp = 1;
	    point pp = p;
	    uniform float i;
	    float sum = 0;
	    for(i=0; i<octaves; i+=1) {
		sum += amp * snoise(pp);
		amp *= gain; pp *= lacunarity; 
	    }
	    return sum;
	}

	vector vfBm(point p; 
		uniform float octaves;
		float lacunarity;
		float gain;) {
	    float amp = 1;
	    point pp = p;
	    uniform float i;
	    point sum = 0;
	    for(i=0; i<octaves; i+=1) {
		sum += amp * vsnoise(pp);
		amp *= gain; pp *= lacunarity; 
	    }
	    return sum;
	}

	float turb(point p; 
		uniform float octaves;
		float lacunarity;
		float gain;) {
	    float amp = 1;
	    point pp = p;
	    uniform float i;
	    float sum = 0;
	    for(i=0; i<octaves; i+=1) {
		sum += amp * abs(snoise(pp));
		amp *= gain; pp *= lacunarity; 
	    }
	    return sum;
	}


-- file "cell.rib":
    ##RenderMan RIB-Structure 1.0
    version 3.03
    # Format 1600 1200 1
    # Format 80 600 1
    Format 750 200 1
    # Format 500 300 1
    #Format 1024 768 1
    #Format 3200 2400 1
    # Format 1600 1200 1
    PixelSamples 1 1
    ShadingRate 4

    Display "cell.tif" "file" "rgb"
    Display "cell.tif" "framebuffer" "rgb"

    Projection "perspective"  "fov" [11]
    Option "searchpath" "shader" [".:../shaders:&"]

    Identity
    Translate 0 0 12
     
    WorldBegin

    TransformBegin
    AttributeBegin

    # Attribute "render" "truedisplacement" [0] 

    LightSource "ambientlight" 1 "intensity" [0.05]
    #    LightSource "pointlight" 2 "lightcolor" [0 1 0] "from" [-0.5 0 -0.5]
    #    LightSource "pointlight" 3 "lightcolor" [1 0 0] "from" [0.5 0.5 -0.5]
    #    LightSource "pointlight" 4 "lightcolor" [0 0 1] "from" [0.5 0.5 -3]
    LightSource "distantlight" 5 "from" [1 2 -4] "to" [0 0 0]
    LightSource "distantlight" 6 "from" [3 2 -4] "to" [0 0 0] "lightcolor" [1 0.6 0.6]
    LightSource "distantlight" 7 "from" [-3 -2 -4] "to" [0 0 0] "lightcolor" [0.6 1 1]
    LightSource "distantlight" 8 "from" [0 0 -4] "to" [0 0 0] "lightcolor" [1 1 1]
     Illuminate 1 1
     #    Illuminate 2 1
     #     Illuminate 3 1
     #     Illuminate 4 1
     Illuminate 5 1
     Illuminate 6 1
     Illuminate 7 1
     Illuminate 8 1

    # Attribute "displacementbound" "coordinatesystem" ["world"] "sphere" [1.5]

    -- surface.

    Patch "bilinear" "P" [ -1.2 1.2 0  1.2 1.2 0  -1.2 -1.2 0  1.2 -1.2 0 ]
    TransformBegin
    Translate 2.5 0 0
    Patch "bilinear" "P" [ -1.2 1.2 0  1.2 1.2 0  -1.2 -1.2 0  1.2 -1.2 0 ]
    TransformEnd
    TransformBegin
    Translate -2.5 0 0
    Patch "bilinear" "P" [ -1.2 1.2 0  1.2 1.2 0  -1.2 -1.2 0  1.2 -1.2 0 ]
    TransformEnd

    AttributeEnd
    TransformEnd

    WorldEnd

The end...
