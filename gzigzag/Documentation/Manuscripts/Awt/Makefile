
all : dvi fdvi

dvi : awt.dvi
ps : awt.ps awtf.ps
pdf : awtf.pdf

fdvi: awtf.dvi

awtf.dvi: awtf.tex awt.tex ../gzigzag.bib
	latex awtf.tex
	BIBINPUTS=..:$$BIBINPUTS bibtex awtf
	latex awtf.tex
	latex awtf.tex


awt.dvi : awt.tex ../gzigzag.bib
	latex $*
	BIBINPUTS=..:$$BIBINPUTS bibtex $*
	latex $*
	latex $*

awtf.ps: awtf.dvi
	dvips -o awtf.ps awtf.dvi

awt.ps: awt.dvi
	dvips -o awt.ps awt.dvi

awtf.pdf: awtf.ps
	ps2pdf $< $@

clean :
	rm -f awt.ps awt.dvi awt.log awt.aux awt.bbl awt.blg awt.toc 
	rm -rf awtf.dvi awtf.log awtf.aux awtf.bbl awtf.blg awtf.ps awtf.pdf
