/*   
org_gzigzag_SysLogger.c
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
 * Written by Antti-Juhani Kaijanaho
 */

#include "org_gzigzag_SysLogger.h"
#include <syslog.h>

/*
 * Class:     org_gzigzag_SysLogger
 * Method:    openlog
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_gzigzag_SysLogger_openlog(JNIEnv * env, jclass class) {
        openlog("gzigzag", LOG_CONS, LOG_DAEMON);
}

/*
 * Class:     org_gzigzag_SysLogger
 * Method:    syslog
 * Signature: (Ljava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_org_gzigzag_SysLogger_syslog(JNIEnv * env, jclass class, jstring msg, jboolean verb) {
        const char * s;

        jboolean isCopy = JNI_FALSE;
        s = (*env)->GetStringUTFChars(env, msg, &isCopy);
        syslog((verb == JNI_TRUE) ? LOG_NOTICE : LOG_INFO, s);
        (*env)->ReleaseStringUTFChars(env, msg, s);
}
