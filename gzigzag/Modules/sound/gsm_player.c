/*   
gsm_player.c
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


/****************************************************
 * INCLUDES
 ****************************************************/
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/soundcard.h>
#include <gsm/gsm.h>

#include "jni.h"
#include "org_gzigzag_module_SimplePlayer.h"
#include "gsm_def.h"

/****************************************************
 * DEFINES
 ****************************************************/
#define DEVICE "/dev/dsp"
#define DEFAULT_DSP_SPEED  8000
#define DEFAULT_DSP_FRAG   0
#define DEFAULT_DSP_SAMPLE 
#define PLAYS 10


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
 * playing
 * Struct to hold infomation about playing.
 */
typedef struct {
  char *file;       // file to read from
  int  audio;       // handle to audio device
  int  speed;        
  int  samplesize;
  int  fragsize;
  int  channels;     // mono 1, stereo 2
  long offset;      // bytes from beginning
  int  stop;
  int  format;
} Playing;


Playing *pls[PLAYS];

int cur_play = 0;

/**
 * Error handling
 */
void per(char *str){
  printf("** player error: %s\n", str);
  perror("ERROR: " );
}

/**
 * Creates a new playing structure, and increments 
 * curr_play by 1. 
 * @return a pointer to new Playing struvct or NULL
 */
Playing *new_playing(int *rn){
  Playing *r = (Playing *) malloc(sizeof(Playing));
  cur_play++;
  pls[cur_play] = r;
  *rn = cur_play;
  return r;
}


int init_playing(Playing *rec){
  rec->file       = "/tmp/soundscroll";
  rec->audio      = 0;
  rec->speed      = 8000;
  rec->samplesize = 8; 
  rec->fragsize   = 0;  // not used at the moment
  rec->channels   = 1;
  rec->offset     = 0;
  return 0;
}

/**
 * Initializes the recording device.
 */
int init_playing_device(const char *file){
  
  int rn = 0;
  Playing *r = new_playing(&rn);
  init_playing(r);
  r->file = strdup(file);
  r->stop = -1;
  r->offset = 0;
  DEBUG("init device\n");
  
  if ( (r->audio = open(DEVICE, O_WRONLY) ) < 0 ) {
    per("OPEN FAILED");
    return 0;
  }
  DEBUG("opened device\n");
  
  ioctl(r->audio, SNDCTL_DSP_SAMPLESIZE, &r->samplesize);
  
  if ( ioctl(r->audio, SNDCTL_DSP_SETFMT,
	     &r->format) == -1 ) {
    perror("ioctl format");
  }
  
  if (ioctl (r->audio, SNDCTL_DSP_SPEED, &r->speed) == -1){
    per("set speed failed");
  }
  
  if (ioctl(r->audio, SNDCTL_DSP_CHANNELS, &r->channels) == -1) {
    per("SNDCTL_DSP_CHANNELS");
    return 0;
  }
  
  return rn;
  
}


/*******************************************************************
 *                            INIT                                 *
 *******************************************************************/
JNIEXPORT jint JNICALL Java_org_gzigzag_module_SimplePlayer_player_1init
(JNIEnv *env, jobject obj, jstring file){
  int rv=0;
  const char *fn = (const char *)(*env)->GetStringUTFChars(env, file, 0);
  rv = init_playing_device(fn);  
  (*env)->ReleaseStringUTFChars(env, file, fn);
  return rv;
}


/*******************************************************************
 *                              STOP                               *
 *******************************************************************/
JNIEXPORT void JNICALL Java_org_gzigzag_module_SimplePlayer_player_1stop
(JNIEnv *env, jobject obj, jint handle){
  Playing *r = pls[handle];
  if ( r == NULL) return ;
  DEBUG("native STOP\n");
  r->stop = 1;
}

/*******************************************************************
 *                           START                                 *
 *******************************************************************/
JNIEXPORT void JNICALL Java_org_gzigzag_module_SimplePlayer_player_1start
(JNIEnv *env, jobject obj, jint handle, jlong from, jlong to){
  int fd = 0;
  gsm_frame buf;
  long f = from;
  long t = to;
  int g=0;
  int i;
  Playing *r = pls[handle];
  unsigned char accbuf[160];
  gsm gsm = gsm_create();
  gsm_signal gsm_out[160];
  
  DEBUG("starting...\n");
  
  if ((fd = open (r->file, O_RDONLY)) == -1){
    per("open failed");
    return;
  }
  
  lseek(fd, f, SEEK_SET);
  printf("to= %ld\n", t);
  r->offset = f;
  while (r->stop < 1 && r->offset < t){
    if (read (fd, (char *)buf, sizeof(buf)) != sizeof(buf)){
      per("read failed");
      close(fd);
      close(r->audio);
      return;
    }
    
    if( gsm_decode(gsm, buf, gsm_out) < 0){
      perror("error decoding...");
      return ;
    }
    
    for(g=0;g<160;g++){
      i=(int)gsm_out[g]+32768;
      i>>=8;
      /*gsm_out[g]=(unsigned char)i;*/
      accbuf[g]=(unsigned char)i;
    }
    
    if ( (write (r->audio, accbuf, sizeof(accbuf))) != sizeof(accbuf)){
      per("write failed");
      close(fd);
      close(r->audio);
      return;
    }
    r->offset += sizeof(buf);
  }                       /* While */
  
  if (fd != 1)
    close (fd);
  
  close( r->audio );
  
  DEBUG("played\n");
}
