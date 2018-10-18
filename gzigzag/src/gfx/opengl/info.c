#include <stdio.h>
#include <GL/glut.h>

int buf[10];
GLboolean bbuf[10];
GLfloat fbuf[10];

int main( int argc, char *argv[] )
{
    glutInit( &argc, argv );

    // Should get from params somehow...
    //
    // glutInitDisplayMode( GLUT_RGB );
    glutInitDisplayMode( GLUT_RGB | GLUT_MULTISAMPLE);
    glutCreateWindow(argv[0]);

#define pb(a) \
    glGetBooleanv(a, bbuf); \
    printf(#a ": %d\n", bbuf[0]);

#define p(a) \
    glGetIntegerv(a, buf); \
    printf(#a ": %d\n", buf[0]);

#define p2(a) \
    glGetIntegerv(a, buf); \
    printf(#a ": %d %d\n", buf[0], buf[1]);

#define pf(a) \
    glGetFloatv(a, fbuf); \
    printf(#a ": %f\n", fbuf[0]);
#define pf2(a) \
    glGetFloatv(a, fbuf); \
    printf(#a ": %f %f\n", fbuf[0], fbuf[1]);


    p(GL_MAX_LIGHTS);
    p(GL_MAX_CLIP_PLANES);
    p(GL_MAX_COLOR_MATRIX_STACK_DEPTH);
    p(GL_MAX_MODELVIEW_STACK_DEPTH);
    p(GL_MAX_PROJECTION_STACK_DEPTH);
    p(GL_MAX_TEXTURE_STACK_DEPTH);
    p(GL_SUBPIXEL_BITS);
    p(GL_MAX_3D_TEXTURE_SIZE);
    p(GL_MAX_TEXTURE_SIZE);
#ifdef GL_MAX_CUBE_MAP_TEXTURE_SIZE
    p(GL_MAX_CUBE_MAP_TEXTURE_SIZE);
#endif
    p(GL_MAX_PIXEL_MAP_TABLE);
    p(GL_MAX_NAME_STACK_DEPTH);
    p(GL_MAX_LIST_NESTING);
    p(GL_MAX_EVAL_ORDER)

    p2(GL_MAX_VIEWPORT_DIMS);

    p(GL_MAX_ATTRIB_STACK_DEPTH)
    p(GL_MAX_CLIENT_ATTRIB_STACK_DEPTH)
    p(GL_AUX_BUFFERS)

    pb(GL_RGBA_MODE);
    pb(GL_INDEX_MODE);
    pb(GL_DOUBLEBUFFER);
    pb(GL_STEREO);

    pf2(GL_ALIASED_POINT_SIZE_RANGE);
    pf2(GL_SMOOTH_POINT_SIZE_RANGE);
    pf2(GL_POINT_SIZE_RANGE);
    pf(GL_SMOOTH_POINT_SIZE_GRANULARITY);
    pf(GL_POINT_SIZE_GRANULARITY);
    pf2(GL_ALIASED_LINE_WIDTH_RANGE);
    pf2(GL_SMOOTH_LINE_WIDTH_RANGE);
    pf2(GL_LINE_WIDTH_RANGE);
    pf(GL_SMOOTH_LINE_WIDTH_GRANULARITY);
    pf(GL_LINE_WIDTH_GRANULARITY);

    // ignoring convolution

    p(GL_MAX_ELEMENTS_INDICES)
    p(GL_MAX_ELEMENTS_VERTICES)

#ifdef GL_MAX_TEXTURE_UNITS
    p(GL_MAX_TEXTURE_UNITS)
#endif
#ifdef GL_MAX_TEXTURE_UNITS_ARB
    p(GL_MAX_TEXTURE_UNITS_ARB)
#endif

#ifdef GL_SAMPLE_BUFFERS
    p(GL_SAMPLE_BUFFERS)
#endif
#ifdef GL_SAMPLES
    p(GL_SAMPLES)
#endif
    /// ignoring ctformats
    
#ifdef GL_NUM_COMPRESSED_TEXTURE_FORMATS
    p(GL_NUM_COMPRESSED_TEXTURE_FORMATS)
#endif

    p(GL_RED_BITS);
    p(GL_GREEN_BITS);
    p(GL_BLUE_BITS);
    p(GL_ALPHA_BITS);
    p(GL_INDEX_BITS);
    p(GL_DEPTH_BITS);
    p(GL_STENCIL_BITS);

    p(GL_ACCUM_RED_BITS);
    p(GL_ACCUM_GREEN_BITS);
    p(GL_ACCUM_BLUE_BITS);
    p(GL_ACCUM_ALPHA_BITS);


    return 0;
}

