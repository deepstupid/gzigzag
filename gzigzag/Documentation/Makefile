all: distdocs

docdirs=Clang_Design DesignProblems Gentle_Introduction Glossary Spec UsersGuide Nile UseCases VersioningMerge Mediaserver

distdocs:
	set -e ; for d in $(docdirs); do $(MAKE) -C $$d all ; done

dist: distdir=docdisttree
dist: distdocs
	rm -rf $(distdir)
	mkdir $(distdir)
	ln FAQ $(distdir)
	ln index.html $(distdir)
	set -e; for d in $(docdirs); \
	        do \
	            echo -n "mkdir $(distdir)/$$d...";\
	            mkdir "$(distdir)/$$d"; \
	            echo "done."; \
	            files=`find $$d -name \*.html -o -name \*.png`;\
	            echo "Copying `echo $$files` to $(distdir)/$$d..."; \
	            ln -f $$files $(distdir)/$$d; \
	            echo "done."; \
	        done

ps:
	(cd CellScroll; make ps)
	(cd Clang_Design; make ps)
	(cd Gentle_Introduction; make ps)
	(cd Interfacing; make ps)
	(cd Spec; make ps)

# Special target for Tjl only ;)
mvps: ps
	scp */*.ps tjl@gzigzag.sourceforge.net:/home/groups/gzigzag/www/ps/
cleanps:
	rm -f */*.ps
clean: cleanps
	rm -f */*.dvi */*.log */*.xyc */*.pdf
