.SUFFIXES: $(SUFFIXES) .eps .fig .png

all: conns

proctext: rects.eps proctext-nvshading.eps
	latex proctext
	BIBINPUTS=..:$$BIBINPUTS bibtex proctext
	latex proctext

scene-outline.eps: scene-outline.png
	convert -monochrome -gamma 0.1 scene-outline.png scene-outline.eps

conns: boxline-ambiguity.eps ink-erase.eps edgeless.eps grid4x4.eps \
cond0.eps cond1.eps cond2.eps cond3.eps cond4.eps cond5.eps cond6.eps cond7.eps \
scene-bevel.eps scene-line.eps scene-outline.eps
	mpost general.mp
	mpost alphaimgs.mp
	latex conns
	BIBINPUTS=..:$$BIBINPUTS bibtex conns
	latex conns
	dvips conns

RPL=../../../src/gfx/opengl/images/pdl/cell.pl
RSRC=../../../src/gfx/opengl/images/pdl/allcells.pnm
rects.eps: $(RSRC)
	pnmtops -noturn -imagewidth 3 $(RSRC) >rects.eps

$(RSRC): $(RPL)
	(cd ../../../src/gfx/opengl/images/pdl; perl cell.pl)

.fig.eps:
	fig2dev -L eps $< $@

.png.eps:
	convert $< $@
