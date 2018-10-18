IMAGES=d1start.ps d1end.ps composite.ps

all: vob.ps vobf.ps

vobf.dvi: vobf.tex vob.tex $(IMAGES) ../gzigzag.bib
	latex vobf
	BIBINPUTS=..:$$BIBINPUTS bibtex vobf
	latex vobf
	latex vobf

vob.dvi: vob.tex $(IMAGES) ../gzigzag.bib
	latex vob
	BIBINPUTS=..:$$BIBINPUTS bibtex vob
	latex vob
	latex vob

vob.pdf: vob.ps
	ps2pdf vob.ps vob.pdf

vobf.pdf: vobf.ps
	ps2pdf vobf.ps vobf.pdf

vob.ps: vob.dvi
	dvips vob -o

vobf.ps: vobf.dvi
	dvips vobf -o

%.ps : %.ppm Makefile
	ppmtopgm $< | pnmscale 0.72 | pnmtops -noturn >$@
