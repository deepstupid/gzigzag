
all :
	@echo "Please cd to a subdirectory first (see README)."
	@exit 1

CVS=cvs
Manifest:
	rm -f $@.new $@.edi
	./cvsparser.pl | ./manifest-filter.pl > $@.edi
	sort $@.edi -o $@.new
	mv $@.new $@
	rm -f $@.edi

VERSION=snapshot-`date -u +%Y%m%d`
TAG="Unofficial"

dist : DISTDIR=gzigzag-$(VERSION)
dist : dist.tgz dist.zip

dist.tgz : distdir
	tar cf $(DISTDIR).tar $(DISTDIR)
	gzip -9vf $(DISTDIR).tar

dist.zip : distdir
	zip -r9 $(DISTDIR).zip $(DISTDIR)

release : DISTDIR=gzigzag-$(VERSION)
release : Z_DIR=$(CURDIR)/Z
release :
	$(MAKE) dist
	$(MAKE) -C src MODULES="" compile \
	|| $(MAKE) -C src MODULES="" compile \
	|| $(MAKE) -C src MODULES="" compile \
	|| $(MAKE) -C src MODULES="" compile \
	|| $(MAKE) -C src MODULES="" compile \
	|| $(MAKE) -C src MODULES="" compile \
	|| $(MAKE) -C src MODULES="" compile \
	|| $(MAKE) -C src MODULES="" compile \
	|| $(MAKE) -C src MODULES="" compile \
	|| $(MAKE) -C src MODULES="" compile \
	|| $(MAKE) -C src MODULES="" compile
	mkdir -p $(Z_DIR)
	$(MAKE) -C src Z_DIR=$(Z_DIR) sync
	$(MAKE) -C src  NOTEST="" Z_DIR=$(Z_DIR) \
	                BUILD_TAG="$(TAG) $(VERSION) (with modules)" \
	                JARNAME=$(DISTDIR)-with-modules.jar jar
	mv src/$(DISTDIR)-with-modules.jar .
	$(MAKE) -C src  NOTEST="" MODULES="" Z_DIR=$(Z_DIR) \
	                BUILD_TAG="$(TAG) $(VERSION) (without modules)" \
	                JARNAME=$(DISTDIR)-without-modules.jar jar
	mv src/$(DISTDIR)-without-modules.jar .
	$(MAKE) -C Documentation dist
	mv Documentation/docdisttree $(DISTDIR)-doc
	tar cf $(DISTDIR)-doc.tar $(DISTDIR)-doc
	gzip -9vf $(DISTDIR)-doc.tar
	zip -r9 $(DISTDIR)-doc.zip $(DISTDIR)-doc
	rm -f $(DISTDIR).md5sums.new
	set -e; for f in $(DISTDIR)-without-modules.jar \
	                 $(DISTDIR)-with-modules.jar \
	                 $(DISTDIR).tar.gz \
	                 $(DISTDIR).zip \
	                 $(DISTDIR)-doc.tar.gz \
	                 $(DISTDIR)-doc.zip; \
	     do \
	         sum=$$(md5sum $$f | awk '{ print $$1; }'); \
	         size=$$(ls -l $$f | awk '{ print $$5; }'); \
	         echo "$$sum $$size $$f" >> $(DISTDIR).md5sums.new; \
	     done
	mv $(DISTDIR).md5sums.new $(DISTDIR).md5sums
	rm -rf docdisttree

CVSTAG=none
official-release : VERSION=$(shell echo $(CVSTAG) \
			           | sed -e 's/^rel_//' -e 's/_/./g')
official-release : grbdir=gzigzag-release-build
official-release : modulesupport.jar
	rm -rf $(grbdir)
	set -e ;\
	  if $$(echo $(VERSION) \
	        | egrep '^[0-9]+\.[0-9]+\.(pre)?[0-9]+$$' >/dev/null);\
	  then\
	      :; \
	  else \
	      echo "Bad VERSION"; \
	      exit 1; \
	  fi
	cvs -z3 -d$(shell cat CVS/Root) checkout -r $(CVSTAG) \
	                                         -d $(grbdir) gzigzag
	ln modulesupport.jar $(grbdir)
	make -C $(grbdir) VERSION=$(VERSION) TAG="Official" release
	ln -f $(grbdir)/gzigzag-$(VERSION)-with-modules.jar 
	ln -f $(grbdir)/gzigzag-$(VERSION)-without-modules.jar
	ln -f $(grbdir)/gzigzag-$(VERSION).tar.gz
	ln -f $(grbdir)/gzigzag-$(VERSION).zip
	ln -f $(grbdir)/gzigzag-$(VERSION)-doc.tar.gz
	ln -f $(grbdir)/gzigzag-$(VERSION)-doc.zip
	ln -f $(grbdir)/gzigzag-$(VERSION).md5sums
	rm -rf $(grbdir)


DISTDIR=gzigzag-dist
distdir:
	./makedist.sh $(DISTDIR)

CVS2CL=cvs2cl

CVS_RSH=ssh
ChangeLog ::
	CVS_RSH=$(CVS_RSH) $(CVS2CL) -U changelog-authors -l -d'2001-01-01\<now' -W 1200 -b --gmt -w --ignore ChangeLog

.PHONY : ChangeLog Manifest distdir dist dist.tgz dist.zip

