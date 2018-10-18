# This Makefile requires GNU make.


# all must compile everywhere
all: compile batchtest


#
# Architectures
#	- which architecture-dependent code to compile
#	  (directories in arch/)
# ARCH=

# Default: none - set on command line.

# Put here the shell argument line to generate the build tag for the
# window title
BUILD_TAG := $(shell echo "Custom CVS Build `date -u +%Y-%m-%dT%H:%M:%SZ` by `whoami`@`hostname`")

# It would be nice to use standard paths !
#
#JAVA_LIB=/usr/local/java/lib
JAVA_DOC=/usr/local/java/docs/api

# For jar builds:
JARARCHES=""
JARMODULES=""
JARNAME=gzigzag.jar
MODULESUPPORT=no

# Location of the depends/ CVS module:
ifeq (,$(GZZ_DEPENDS))
  GZZ_DEPENDS=../../depends
endif

CLASSDIR=CLASSES/
CLASSPATH =$(CLASSDIR):$(GZZ_DEPENDS)/HTTPClient.zip:$(GZZ_DEPENDS)/junit.jar:$(GZZ_DEPENDS)/jython.jar:$(shell echo $$CLASSPATH)
export CLASSPATH
Z_DIR=../../Z/
Z_PRIVATE=../../Z-private/
MEDIASERVER=http://himalia.it.jyu.fi/ms

mainclass = org.gzigzag.client.Client

TESTRUNNER=junit.awtui.TestRunner

httpsync: 
	$(JAVA) org.gzigzag.impl.Synch -dir $(Z_DIR) -url $(MEDIASERVER)/gzz-base

sync:
#	$(JAVA) org.gzigzag.impl.Synch -dir $(Z_DIR) -url $(MEDIASERVER)
	./sync-remote-private.sh $(Z_DIR) gzz-base

sync-private:
	./sync-remote-private.sh $(Z_PRIVATE) gzz-jyu-private


rm-private:
	find $(Z_DIR) -name 'b_*'|xargs rm -f
	find $(Z_DIR) -name 'ps_*'|xargs rm -f

get-private: rm-private sync-private

JAVADOCOPTS=-use -version -author -windowtitle "GZigZag Java API" 

TEMP=/tmp

%.java: %.zob zob2java.pl ; perl zob2java.pl $< $@

TESTS=core/test/*.java client/test/*.java impl/test/*.java impl/clasm/test/*.java util/test/*.java vob/test/*.java test/*.java mediaserver/test/*.java

GENERATED=impl/Merge1.java impl/PartialOrder.java mediaserver/MediaTypes.java impl/ExternalEditor.java impl/clasm/ClasmPrimitiveSet1.java impl/PlainVStreamDim.java

impl/Merge1.java: impl/Merge1 impl/genmerge.pl
	perl impl/genmerge.pl impl/Merge1

impl/PartialOrder.java: impl/PartialOrder impl/genmerge.pl
	perl impl/genmerge.pl impl/PartialOrder

impl/ExternalEditor.java: impl/ExternalEditor impl/genmerge.pl
	perl impl/genmerge.pl impl/ExternalEditor

impl/PlainVStreamDim.java: impl/PlainVStreamDim.ly impl/PlainVStreamDim-trees.ly impl/PlainVStreamDim-trees-nu.ly
	python $(GZZ_DEPENDS)/ly.py impl/PlainVStreamDim.ly impl/PlainVStreamDim-trees.ly impl/PlainVStreamDim-trees-nu.ly

impl/clasm/ClasmPrimitiveSet1.java: impl/clasm/clasm-primitives.ly impl/clasm/clasm-primitives-internals.ly
	python $(GZZ_DEPENDS)/ly.py impl/clasm/*.ly

# find . -name '*.java' | xargs grep -h "^package" | sort | uniq | sed 's/package //;s/;//;' | grep -v java.tex

subdirs = modules/constraints

DOCPKGS=org.gzigzag.benchmark org.gzigzag.client org.gzigzag.gfx org.gzigzag.impl.clasm org.gzigzag.impl org.gzigzag.mediaserver.http org.gzigzag.mediaserver.http.server org.gzigzag.mediaserver.http.client org.gzigzag.mediaserver.ids org.gzigzag.mediaserver.storage org.gzigzag.mediaserver org.gzigzag.test org.gzigzag.util org.gzigzag.vob.demo org.gzigzag.vob org.gzigzag


# Can't add ZZClangOps since javac complains of doubly defined class...
# maybe should use uniq?
SRC0:=$(GENERATED) $(wildcard core/*.java core/errors/*.java impl/*.java impl/clasm/*.java impl/zob/*.java util/*.java vob/*.java vob/demo/*.java vob/demo/text/*.java gfx/*.java mediaserver/*.java mediaserver/*/*.java mediaserver/http/*/*.java ideas/*.java client/*.java modules/pp/*.java modules/pp/vob/*.java modules/contraints/*.java modules/mm/*.java) $(TESTS)


# Remove duplicates
tmpfile := $(shell mktemp /tmp/fileXXXXXX)
foo := $(shell echo 1>&2 "Tempfile is $(tmpfile)")
SRC:=$(shell echo $(SRC0) | perl -pe 's/\s+/\n/g' - | sort | uniq | tee $(tmpfile))


# jikes := $(shell which jikes)
# Disable for now, since on debian jikes is at a bad version
jikes := 
# DEBUG=-g

ifeq (,$(JAVAC))
 ifneq (,$(jikes))
	JAVAC=jikes +P
 else
	JAVAC=javac
 endif
endif

ifeq (,$(JAVA))
	JAVA=java
endif

JAVANCSS=/BIG/javancss9.24/javancss

archcompile = set -e; for d in "$(ARCH)"; do if [ -e arch/$(ARCH)/arch.mk ] ; then $(MAKE) -f arch/$(ARCH)/arch.mk; fi; done
archclean = set -e; for d in "$(ARCH)"; do if [ -e arch/$(ARCH)/arch.mk ] ; then $(MAKE) -f arch/$(ARCH)/arch.mk clean; fi; done

# What testrunner to run after making all target
# Default: batch runner
TESTCOMMAND=$(JAVA) -Dzdir=$(Z_DIR) $(TESTRUNNER)

compile: $(SRC)
	# $(JAVAC) -deprecation $(DEBUG) -d $(CLASSDIR)  BuildInfo.java
#	fix circular dependency problem in clean builds
	mkdir -p $(CLASSDIR)/org/gzigzag/mediaserver/ids
	# -deprecation
	$(JAVAC) $(DEBUG) -d $(CLASSDIR) $(SRC)
	$(archcompile)

constraints:
	$(MAKE) -C modules/constraints

indyjar:
	mkdir independent
	cp -r $(CLASSDIR)/* independent/
	#unzip $(GZZ_DEPENDS)/junit.jar -d independent/
	unzip $(GZZ_DEPENDS)/HTTPClient.zip -d independent/
	echo 'Main-Class: org.gzigzag.client.Run' > independent/manifest
	jar cfm gzz-independent.jar independent/manifest -C independent/ .
	rm -r independent

independent: clean dephack indyjar



slow:
	$(JAVA) -Dzdir=$(Z_DIR) -Dgzigzag.cellstr=idtext junit.textui.TestRunner org.gzigzag.test.TestSlow

awtslow: 
	$(JAVA) -Dzdir=$(Z_DIR) junit.awtui.TestRunner org.gzigzag.test.TestSlow


.PHONY: BuildInfo.java

BuildInfo.java : 
	@echo "*** NOTE: YOU MAY HAVE TO RUN 'make' MORE THAN ONE TIME"
	@echo "*** TO GET EVERYTHING TO COMPILE DUE TO DEPENDENCIES"
	rm -f BuildInfo.java
	sed 's/@@@@@/$(BUILD_TAG)/' BuildInfo.java.in > BuildInfo.java.new && mv BuildInfo.java.new BuildInfo.java

mediaserver/MediaTypes.java: mediaserver/make-mt.pl mediaserver/mime.types
	cd mediaserver && ./make-mt.pl

# HIGHLY EXPERIMENTAL!!!
gcj :
	gcj-3.0 --main=$(mainclass) -o gzigzag `find $(CLASSDIR) -name \*.class`
gcj-src:
	gcj-3.0 --main=(mainclass) -o gzigzag $(SRC)


doc::  
	sh ./make-src-tree.sh $(TEMP)/zzsrc
	rm -Rf doc
	mkdir doc
	javadoc $(JAVADOCOPTS) -d doc -sourcepath $(TEMP)/zzsrc -overview $(TEMP)/zzsrc/overview.html $(DOCPKGS)
	rm -Rf $(TEMP)/zzsrc

clean:
	$(RM) -r $(CLASSDIR)
	$(RM) $(GENERATED)
	$(RM) gzz-independent.jar
	$(RM) -r independent/
	set -e ; for d in  $(subdirs) ; do $(MAKE) -C $$d clean ; done

NOTEST=true
jar: buildir=$(JARNAME).build
jar : 
	cd .. && ./makedist.sh src/$(buildir)
	-$(MAKE) -C $(buildir)/src ARCH="$(JARARCHES)" MODULES="$(JARMODULES)" compile \
	|| $(MAKE) -C $(buildir)/src ARCH="$(JARARCHES)" MODULES="$(JARMODULES)" compile \
	|| $(MAKE) -C $(buildir)/src ARCH="$(JARARCHES)" MODULES="$(JARMODULES)" compile \
	|| $(MAKE) -C $(buildir)/src ARCH="$(JARARCHES)" MODULES="$(JARMODULES)" compile \
	|| $(MAKE) -C $(buildir)/src ARCH="$(JARARCHES)" MODULES="$(JARMODULES)" compile \
	|| $(MAKE) -C $(buildir)/src ARCH="$(JARARCHES)" MODULES="$(JARMODULES)" compile \
	|| $(MAKE) -C $(buildir)/src ARCH="$(JARARCHES)" MODULES="$(JARMODULES)" compile \
	|| $(MAKE) -C $(buildir)/src ARCH="$(JARARCHES)" MODULES="$(JARMODULES)" compile \
	|| $(MAKE) -C $(buildir)/src ARCH="$(JARARCHES)" MODULES="$(JARMODULES)" compile \
	|| $(MAKE) -C $(buildir)/src ARCH="$(JARARCHES)" MODULES="$(JARMODULES)" compile
	$(NOTEST) $(MAKE) -C $(buildir)/src ARCH="$(JARARCHES)" MODULES="$(JARMODULES)" batchtest slow
	echo 'Main-Class: $(mainclass)' > $(buildir)/src/$(CLASSDIR)/manifest
	set -e ; if [ "$(MODULESUPPORT)" != "no" ] ; \
		 then \
		    (cd $(buildir)/Java && jar -xvf $(MODULESUPPORT) gnu org) ;\
		 fi
	cp $(buildir)/LICENSE.lgpl $(buildir)/LICENSE.xpl $(buildir)/src/$(CLASSDIR)
	cp README $(buildir)/src/$(CLASSDIR)
	cd $(buildir)/src/$(CLASSDIR) && jar cvfm $(JARNAME) manifest *
	mv $(buildir)/src/$(CLASSDIR)/$(JARNAME) .
	rm -rf $(buildir)


zobdoc::  
	sh ./make-src-tree.sh $(TEMP)/zzsrc
	rm -Rf zobdoc
	mkdir zobdoc
	javadoc -doclet org.gzigzag.ZobDoc -d zobdoc \
	    -sourcepath $(TEMP)/zzsrc \
		org.gzigzag org.gzigzag.clang org.gzigzag.module 
	rm -Rf $(TEMP)/zzsrc


#      perl -pi.bak -e's|</h1>|"</h1>".`cat Package.html`|e' doc/Package-org.gzigzag.html

tags: $(SRC)
	ctags $(SRC)

CVS2CL=cvs2cl

ChangeLog::
	$(MAKE) -C .. ChangeLog

ncss:: $(SRC)
	$(JAVANCSS) -function $(SRC) | perl mungencss.pl |  \
			sort -rnk3 -k1 >ncss-ccn
	sort -rnk2 -k1 <ncss-ccn >ncss-ncss
	sort -rnk4 -k1 <ncss-ccn >ncss-jvdc
#	$(JAVANCSS) -all $(SRC) >ncss


TESTCLASS=org.gzigzag.test.Test

test::
	# $(JAVA) -Dswing.plaf.metal.userFont=SansSerif-8 -Dswing.plaf.metal.systemFont=SansSerif-8 -Dswing.plaf.metal.controlFont=SansSerif-8 -Dzdir=$(Z_DIR) junit.swingui.TestRunner org.gzigzag.test.Test
	$(TESTCOMMAND) $(TESTCLASS)

testunstable:
	# $(JAVA) -Dswing.plaf.metal.systemFont=SansSerif-8 -Dswing.plaf.metal.controlFont=SansSerif-8 -Dzdir=$(Z_DIR) junit.swingui.TestRunner org.gzigzag.test.TestUnstable
	$(TESTCOMMAND) org.gzigzag.test.TestUnstable

batchtestunstable:
	$(JAVA) -Dzdir=$(Z_DIR) junit.textui.TestRunner org.gzigzag.test.TestUnstable

batchtest::
	$(JAVA) -Dzdir=$(Z_DIR) junit.textui.TestRunner $(TESTCLASS)

EDITOR=/usr/bin/gvim
CLIENT=org.gzigzag.client.Client
PPCLIENT=org.gzigzag.modules.pp.Client

runclient:
	$(JAVA) -Dgzigzag.keybindings=hardcoded -Duser.editor=$(EDITOR) $(CLIENT) $(DBG) $(Z_DIR) $(Z_PRIVATE) $(OTHERS)

runpp:
	$(JAVA) -Duser.editor=$(EDITOR) $(PPCLIENT) $(DBG) $(Z_DIR) $(OTHERS)



dephack: depclean dep compile depclean

perls: $(GENERATED)

dep: perls 
	sh make-hack-tree.sh

depclean:
	rm -rf temp/ org/



alltests: batchtest batchtestunstable
