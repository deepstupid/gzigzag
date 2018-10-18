#include <jni.h>
#include "ZZId.h"
#include "ZZScriptAPI_1_JNI.h"
#include "zzfoo.h"

static jclass zzspclass;
static jfieldID ptrid;

extern "C" {
JNIEXPORT void JNICALL Java_fi_iki_lukka_ZZScriptPerl_init
  (JNIEnv *env, jobject foo)
{
	if(!zzspclass)
		zzspclass = (jclass)env->NewGlobalRef(env->GetObjectClass(foo));
	ptrid = env->GetFieldID(zzspclass, "ptr", "J");
	if(!ptrid) abort();
	ZZScriptAPI_1_0_JNI *j = new ZZScriptAPI_1_0_JNI(env);
	zzperl_init(j);
	env->SetLongField(foo, ptrid, (jlong)j);
}

JNIEXPORT void JNICALL Java_fi_iki_lukka_ZZScriptPerl_call
  (JNIEnv *env, jobject foo, jstring code0, jobject view0, jobject
  	cview0, jobject scr0, jobject cur0, jstring dimx0, jstring dimy0)
{
	printf("ENTERING PERLCALL\n");
	ZZScriptAPI_1_0_JNI *j = (ZZScriptAPI_1_0_JNI *) env->GetLongField(foo, ptrid);
	if(!j) abort();
	char *code = (char *)env->GetStringUTFChars(code0, 0);
	char *dimx = (char *)env->GetStringUTFChars(dimx0, 0);
	char *dimy = (char *)env->GetStringUTFChars(dimy0, 0);

	zzperl_call(code, (long)view0, (long)cview0, (long)scr0, (long)cur0, dimx, dimy);

	env->ReleaseStringUTFChars(code0,code);
	env->ReleaseStringUTFChars(dimx0,dimx);
	env->ReleaseStringUTFChars(dimy0,dimy);
	printf("RETURNING FROM PERLCALL\n");

}

JNINativeMethod ms[2] = {
{ "init", "()V", Java_fi_iki_lukka_ZZScriptPerl_init }, 
{ "call", "(Ljava/lang/String;Lfi/iki/lukka/ZZCell;Lfi/iki/lukka/ZZCell;Lfi/iki/lukka/ZZCell;Lfi/iki/lukka/ZZCell;Ljava/lang/String;Ljava/lang/String;)V", 
Java_fi_iki_lukka_ZZScriptPerl_call }
};

void zsp_install(JNIEnv *env) {
	zzspclass = env->FindClass("fi/iki/lukka/ZZScriptPerl");
	if(!zzspclass) abort();
	zzspclass = (jclass)env->NewGlobalRef(zzspclass);
	env->RegisterNatives(zzspclass, ms, 2);
}

}
