all: gi.html gi-ns4.html
# zzgentle.tex
zzgentle.dvi: zzgentle.tex
	pic -t <$*.tex >$*.tex
	latex $*
	cat $*.dvi >$*.dvi
ps: zzgentle.ps

# DIAGRAMS=linkorder.png beamorder.png
DIAGRAMS=../wmlinc/article.wml

gi.html: gi.wml $(DIAGRAMS)

gi-ns4.html: gi.wml $(DIAGRAMS)

include ../lib.mk



