
all : dvi fdvi sdvi

dvi : hyp.dvi
ps : hyp.ps hypf.ps
pdf : hypf.pdf

fdvi: hypf.dvi
sdvi : hyps.dvi

hypf.dvi: hypf.tex hyp.tex ../gzigzag.bib diagram.1 diagram.2 utopia.1
	latex hypf.tex
	BIBINPUTS=..:$$BIBINPUTS bibtex hypf
	latex hypf.tex
	latex hypf.tex


hyp.dvi : hyp.tex ../gzigzag.bib diagram.1 diagram.2 utopia.1
	latex $*
	BIBINPUTS=..:$$BIBINPUTS bibtex $*
	latex $*
	latex $*

hyps.dvi : hyps.tex ../gzigzag.bib 
	latex $*
	BIBINPUTS=..:$$BIBINPUTS bibtex $*
	latex $*
	latex $*

%.ps: %.dvi
	dvips -o $@ $<

%.pdf: %.ps
	ps2pdf $< $@

diagram.1 diagram.2: diagram.mp
	mpost diagram.mp

utopia.1: utopia.mp utopiapara.tex
	rm -f utopia.mpx
	TEX=latex mpost utopia.mp
	rm -f utopia.mpx
	rm -f mpxerr.tex

clean :
	rm -f hyp.ps hyp.dvi hyp.log hyp.aux hyp.bbl hyp.blg hyp.toc 
	rm -rf hypf.dvi hypf.log hypf.aux hypf.ps hypf.pdf
