
%.html : %.wml ../wmlinc/*
	wml -I ../wmlinc $< -o $@

%-ns4.html : %.wml ../wmlinc/*
	wml -I ../wmlinc -Dmode=ns4 $< -o $@

%.ps: %.dia
	dia -e $@ $<

%.png: %.ps
	# gs -q -sDEVICE=ppmraw -sOutputFile=expdia.pnm -r288 -dNOPAUSE -dBATCH $<
	gs -q -sDEVICE=ppmraw -sOutputFile=expdia.pnm -r144 -dNOPAUSE -dBATCH $<
	pnmcrop expdia.pnm | pnmpad -white -l20 -r20 -t20 -b20 >expdiac.pnm
	mogrify -antialias -geometry '20%' expdiac.pnm
	# mogrify -antialias -geometry '10%' expdiac.pnm
	pnmtopng expdiac.pnm >$@
	rm expdia*

%.tex : %.ptex
	gpic -t <$< >$@.new
	mv $@.new $@

%.dvi : %.tex
	latex $*
	latex $*
	latex $*

%.ps : %.dvi
	dvips -o $@ $<

clean :
	rm -f *.html
