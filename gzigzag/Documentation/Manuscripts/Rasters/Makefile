
all : dvi 

dvi : raster.dvi

raster.dvi: raster.tex ../gzigzag.bib
	latex raster.tex
	BIBINPUTS=..:$$BIBINPUTS bibtex raster
	latex raster.tex
	latex raster.tex

	
