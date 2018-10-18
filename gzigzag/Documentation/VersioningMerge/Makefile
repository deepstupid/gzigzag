DIAGRAMS=mergemath001.png

all : vm.html vm-ns4.html

mergemath001.png: mergemath.ps Makefile
	pstopnm -xsize 2048 mergemath.ps
	for i in 001 002; do \
	    pnmcrop -white mergemath$$i.ppm | ppmbrighten -v -10 | pnmscale -width=380 |pnmtopng >mergemath$$i.png ;\
	done

mergemath.ps: mergemath.tex
	latex mergemath
	dvips mergemath -o mergemath.ps

vm.html: vm.wml $(DIAGRAMS)

vm-ns4.html: vm.wml $(DIAGRAMS)

include ../lib.mk


