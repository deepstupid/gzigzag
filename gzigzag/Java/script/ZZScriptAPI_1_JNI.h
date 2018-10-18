#ifndef ZZSCRIPT_API_1_JNI_H
#define ZZSCRIPT_API_1_JNI_H

#define EXC() if(env->ExceptionOccurred()) {env->ExceptionDescribe(); env->FatalError("EXCEPTION");}

// XXX ARGH
// #define MID(cell,id) (env->GetMethodID(env->GetObjectClass((jobject)cell), names[id].name, names[id].sig) || (env->FatalError("NoMethod"),(jmethodID)0))
#define MID(cell,id) (env->GetMethodID(env->GetObjectClass((jobject)cell), names[id].name, names[id].sig))
				

#include "ZZScriptAPI_1.h"

class ZZScriptAPI_1_0_JNI : public ZZScriptAPI_1_0 {
  protected:
  JNIEnv *env;
  jclass zzcell;
  enum meths {
	  GETNEIGH, 
	  HEADCELL, 
	  NEWCELL, 
	  DELETECELL,
	  CONNECTCELLS, 
	  INSERTCELL, 
	  DISCONNECT, 
	  GETTEXT, 
	  SETTEXT,
	  LASTMETH_
  };
  static const struct meth {char *name; char *sig;} names[LASTMETH_] 
	  = { 
	  {  "getNeighbour", "(Ljava/lang/String;I)Lfi/iki/lukka/ZZCell;" },
	  {  "getHeadcell", "(Ljava/lang/String;I)Lfi/iki/lukka/ZZCell;" },
	  {  "newCell", "(Ljava/lang/String;I)Lfi/iki/lukka/ZZCell;" },
	  {  "delete", "()V" },
	  {  "connect", "(Ljava/lang/String;Lfi/iki/lukka/ZZCell;)V" },
	  {  "insert", "(Ljava/lang/String;ILfi/iki/lukka/ZZCell;)V" },
	  {  "disconnect", "(Ljava/lang/String;I)V" },
	  {  "getText", "()Ljava/lang/String;" },
	  {  "setText", "(Ljava/lang/String;)V" },
  };
  jmethodID mids[LASTMETH_];
  public:
  ZZScriptAPI_1_0_JNI(JNIEnv *env0) {
	utfref = 0;
	strref = 0;
	env = env0;
  	zzcell = env->FindClass("fi/iki/lukka/ZZCell");
	if(!zzcell) abort();
	zzcell = (jclass)env->NewGlobalRef(zzcell);
	if(!zzcell) abort();
	for(int i=0; i<LASTMETH_; i++) {
		mids[i] = env->GetMethodID(zzcell, names[i].name, names[i].sig);
		if(!mids[i]) {
			abort();
		}
	}
  }

  static const int verbose = 1;

  virtual ZZId get_cell (ZZId from, const char *dim, int dir) {
	  printf("CB GETCELL\n");
  	long res = (long)env->CallObjectMethod((jobject)from, MID(from,GETNEIGH), 
				env->NewStringUTF(dim), (jint)dir);
	EXC();
	return res;
  };
  virtual ZZId get_headcell (ZZId from, const char *dim, int dir)
  {
	  printf("CB HEADCELL\n");
	  EXC();
	  abort();
  };
  virtual ZZId new_cell (ZZId from, const char *dim, int dir) {
	  printf("CB NEWCELL\n");
  	long res =  (long)env->CallObjectMethod((jobject)from, MID(from,NEWCELL), 
				env->NewStringUTF(dim), (jint)dir);
	EXC();
	return res;
  }
  virtual void delete_cell (ZZId cell) { 
	  printf("CB DELCELL\n");
  	env->CallVoidMethod((jobject)cell, MID(cell,DELETECELL));
	EXC();
  };
  virtual void connect_cells (ZZId from, const char *dim, ZZId to) { 
	  printf("CB CONCELL\n");
  	env->CallVoidMethod((jobject)from, MID(from,CONNECTCELLS), 
			env->NewStringUTF(dim), (jobject)to);
	EXC();
  };
  virtual void insert_cell (ZZId where, const char *dim, int dir, ZZId what) { 
	  printf("CB INSCELL\n");
  	env->CallVoidMethod((jobject)where, MID(where,INSERTCELL), 
			env->NewStringUTF(dim), (jint)dir, (jobject)what);
	EXC();
  };
  virtual void disconnect (ZZId from, const char *dim, int dir) { 
	  printf("CB DISCELL\n");
  	env->CallVoidMethod((jobject)from, MID(from,NEWCELL), 
				env->NewStringUTF(dim), (jint)dir);
	EXC();
  };
  const char *utfref; 
  jstring strref; 
  virtual const char *get_text (ZZId cell) { 
	  printf("CB GETTEXT\n");
	  if(utfref) {
		  // XXX?
		  env->ReleaseStringUTFChars(strref, utfref);
	  }
	  strref = (jstring)env->CallObjectMethod((jobject)cell, MID(cell,GETTEXT));
	  utfref = env->GetStringUTFChars(strref,0);
	  EXC();
	  return utfref;
  };
  virtual void set_text (ZZId cell, const char *str) { 
	  printf("CB SETTEXT\n");
	  env->CallVoidMethod((jobject)cell, MID(cell,SETTEXT),
			  	env->NewStringUTF(str));
	  EXC();
	  printf("CB SETTEXT ret\n");
  };
  virtual int popup_warning (const char *text) {
  	abort();
  };


};

#endif
