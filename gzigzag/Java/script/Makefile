PERLDIR=../../C++/script/Perl


IBMCFLAGS=-Dint64_t="long long" -Dlinux -g -I/BIG/ibmjdk/include -I/BIG/ibmjdk/include/linux -I../../C++ -I../../C++/script/Perl 
CFLAGS=-g -I$(PERLDIR)

IBMLDFLAGS=-L/BIG/ibmjdk/lib/linux/native_threads/

all: classes hdr jzigzag

jzigzag: invoke.o zzfoo.o ZZScriptPerl.o ZZScriptAPI_1_JNI.o
	gcc -o jzigzag -export-dynamic invoke.o zzfoo.o ZZScriptPerl.o ZZScriptAPI_1_JNI.o $(IBMLDFLAGS) -ljava `perl -MExtUtils::Embed -e ldopts`

invoke.o: invoke.c
	gcc $(IBMCFLAGS) -c invoke.c

zzfoo.o: $(PERLDIR)/zzfoo.cpp
	g++ $(CFLAGS) `perl -MConfig -e 'print "$$Config{ccflags} -I$$Config{installarchlib}/CORE"'` -c -o zzfoo.o -I../../C++ $(PERLDIR)/zzfoo.cpp 

ZZScriptPerl.o: ZZScriptPerl.cpp *.h
	g++ $(IBMCFLAGS) -c -o ZZScriptPerl.o ZZScriptPerl.cpp

ZZScriptAPI_1_JNI.o: ZZScriptAPI_1_JNI.cpp *.h
	g++ $(IBMCFLAGS) -c -o ZZScriptAPI_1_JNI.o ZZScriptAPI_1_JNI.cpp
	

# all: classes hdr libZZScriptPerl.la

classes:
	javac -d .. *.java

hdr: fi_iki_lukka_ZZScriptPerl.h

fi_iki_lukka_ZZScriptPerl.h: ../fi/iki/lukka/ZZScriptPerl.class
	javah -jni fi.iki.lukka.ZZScriptPerl

# libZZScriptPerl.la: ZZScriptPerl.lo zzfoo.lo
# 	libtool g++ $(CFLAGS) -o libZZScriptPerl.la ZZScriptPerl.lo zzfoo.lo `perl -MConfig -e 'print "-L$$Config{installarchlib}/CORE -lperl $$Config{libs}"'` 
# 	
# # Doesn't work due to DynaLoader! ARGH!
# #`perl -MExtUtils::Embed -e ccopts -e ldopts`
# 
# zzfoo.lo: $(PERLDIR)/zzfoo.cpp
# 	libtool g++ $(CFLAGS) `perl -MConfig -e 'print "$$Config{ccflags} -I$$Config{installarchlib}/CORE"'` -c -o zzfoo.lo -I../../C++ $(PERLDIR)/zzfoo.cpp 
# 
# 
# 
# $(PERLDIR)/zzfoo.cpp: ../../C++/script/Perl/zzfoo.xs
# 	(cd $(PERLDIR); make zzfoo.cpp)
# 
# 
# ZZScriptPerl.lo: ZZScriptPerl.cpp *.h
# 	libtool g++ $(IBMCFLAGS) -c -o ZZScriptPerl.lo ZZScriptPerl.cpp
