

JNI_INCLUDE = /usr/include/kaffe/

all : arch/syslog/libgzigzag-syslog.so

%.o : %.c
	$(CC) $(CFLAGS) -I $(JNI_INCLUDE) -fPIC -c -o $@ $<

arch/syslog/org_gzigzag_SysLogger.o : arch/syslog/org_gzigzag_SysLogger.c arch/syslog/org_gzigzag_SysLogger.h

arch/syslog/org_gzigzag_SysLogger.h : org/gzigzag/SysLogger.class
	javah -jni -d arch/syslog org.gzigzag.SysLogger

arch/syslog/libgzigzag-syslog.so : arch/syslog/org_gzigzag_SysLogger.o
	$(CC) -shared -o $@ $^

clean :
	rm -f arch/syslog/libgzigzag-syslog.so arch/syslog/org_gzigzag_SysLogger.o arch/syslog/org_gzigzag_SysLogger.h
