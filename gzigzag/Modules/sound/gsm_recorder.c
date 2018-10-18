/*   
gsm_recorder.c
 *    
 *    Copyright (c) 2000, Ted Nelson, Tuomas Lukka and Vesa Parkkinen
 *
 *    You may use and distribute under the terms of either the GNU Lesser
 *    General Public License, either version 2 of the license or,
 *    at your choice, any later version. Alternatively, you may use and
 *    distribute under the terms of the XPL.
 *
 *    See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of 
 *    the licenses.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
 *    file for more details.
 *
 */
/*
 * Written by Vesa Parkkinen
 */
/**
 * simple gsm recorder
 */


/****************************************************
 * INCLUDES
 ****************************************************/
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <fcntl.h>
#include <sys/ioctl.h>

#include <sys/soundcard.h>

#include <pthread.h>
#include <gsm/gsm.h>

#include "jni.h"
#include "org_gzigzag_module_SimpleRecorder.h"
#include "gsm_def.h"

/****************************************************
 * DEFINES
 ****************************************************/
#define DEVICE "/dev/dsp"
#define DEFAULT_DSP_SPEED  8000
#define DEFAULT_DSP_FRAG   0
#define DEFAULT_DSP_SAMPLE 
#define RECS               10
#define READ_BLOCK         1280
#define BUFFER_SIZE        20 * READ_BLOCK
#define GSM_RECORDER_DEBUG 1
//#define ENCODED_SIZE       20 * READ_BLOCK

#ifndef uint
#define uint unsigned int
#endif

#ifndef ulong
#define ulong unsigned long
#endif

#ifndef DEBUG_SOUND
#define DEBUG_SOUND 0
#endif 

#define DEBUG(s) if(DEBUG_SOUND)printf(s);
/****************************************************
 * STRUCTS
 ****************************************************/

/**
 * Recording  
 * Struct to hold infomation about recordings.
 */
typedef struct {
  
  char *file;         // file to save to
  int   audio;        // handle to audio device
  int   speed;        
  int   samplesize;
  int   fragsize;
  int   channels;     // mono 1, stereo 2
  ulong offset;       // bytes from beginning
  int   format;
  int   stop;
  
} Recording;

Recording *recs[RECS];
int cur_rec = 0;

/**
 *
 */
int init_recording(Recording *rec){
  rec->file       = "/tmp/sndscroll";
  rec->audio      = 0;
  rec->speed      = 8000;
  rec->samplesize = 8; 
  rec->fragsize   = 0;  // not used at the moment
  rec->channels   = 1;
  rec->offset     = 0;
  rec->stop       = 0;
  return 0;
}

/**
 * Creates a new recording structure, and increments 
 * curr_rec by 1. 
 * @return a pointer to new Recording struvct or NULL
 */
Recording *new_recording(int *rn){
  Recording *r = (Recording *) malloc(sizeof(Recording));
  cur_rec++;
  recs[cur_rec] = r;
  *rn = cur_rec;
  return r;
}

/**
 * Initializes the recording device.
 */
int init_device(const char *file){
  int rn = 0;
  Recording *r = new_recording(&rn);
  init_recording(r);
  r->file = strdup(file);
  r->stop = -1;

  DEBUG("init device\n");
  
  if ( (r->audio = open(DEVICE, O_RDONLY) ) < 0 ) {
    perror("OPEN FAILED");
  }
  
  DEBUG("opened device\n");

  if( ioctl(r->audio, SNDCTL_DSP_SAMPLESIZE, &r->samplesize) == -1){
    perror("SNDCTL_DSP_SAMPLESIZE");
    return -1;
  }

  if (ioctl(r->audio, SNDCTL_DSP_CHANNELS, &r->channels) == -1) {
    perror("SNDCTL_DSP_CHANNELS");
    return -1;
  }
  
  if (ioctl (r->audio, SNDCTL_DSP_SPEED, &r->speed) == -1){
    perror("set speed failed");
  }
  DEBUG("initialized device\n");
  return rn;
}


/*******************************************************************
 *                    GET CURRENT OFFSET                           *
 *******************************************************************/
JNIEXPORT jlong JNICALL 
Java_org_gzigzag_module_SimpleRecorder_recorder_1getCurrentOffset
(JNIEnv *env, jobject obj , jint handle){
  Recording *r = recs[handle];
  if( r == NULL ) return 0;
  return r->offset;
}

/*******************************************************************
 *                            INIT                                 *
 *******************************************************************/
JNIEXPORT jint JNICALL Java_org_gzigzag_module_SimpleRecorder_recorder_1init
(JNIEnv *env, jobject obj, jstring file, jlong offset){
  int rv=0;
  const char *fn = (const char *)(*env)->GetStringUTFChars(env, file, 0);
  rv = init_device(fn);  
  (*env)->ReleaseStringUTFChars(env, file, fn);
  recs[rv]->offset = offset;
  return rv;
}

/*******************************************************************
 *                              STOP                               *
 *******************************************************************/
JNIEXPORT void JNICALL Java_org_gzigzag_module_SimpleRecorder_recorder_1stop
(JNIEnv *env, jobject obj, jint handle){
  Recording *r = recs[handle];
  if ( r == NULL) return ;
  r->stop = 1;
}

/*******************************************************************
 *                           START                                 *
 *******************************************************************/
JNIEXPORT void JNICALL Java_org_gzigzag_module_SimpleRecorder_recorder_1start
(JNIEnv *env, jobject obj, jint handle){
  
  int fd = 0;
  char *audiobuf;
  
  gsm gsm = gsm_create();
  gsm_signal sample[160];
  gsm_frame frame;
  
  Recording *r = recs[handle];

  if ((fd = open (r->file, O_WRONLY | O_CREAT, 0666)) == -1){
    perror("recorder start: open failed");
    return ;
  }
  
  lseek(fd, r->offset, SEEK_SET);
  
  while ( r->stop < 1 ){
    read (r->audio, sample, sizeof(sample) );
    gsm_encode(gsm, sample, frame);
    
    // write
    if (write (fd, frame, sizeof(frame)) != sizeof(frame)){
      perror("write failed");
      //exit (-1);
      return ;
    }
    r->offset += sizeof(frame);
  }
  if (fd != 1)
    close (fd);
  
  close( r->audio );

  gsm_destroy(gsm);
  
  printf("recorded\n");
}
