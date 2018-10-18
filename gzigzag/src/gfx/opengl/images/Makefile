BMRT=/BIG/renderman/BMRT2.6/

all: bmrt

tangle:
	python ../../../../../depends/ly.py cellimages.ly

bmrt: tangle
	$(BMRT)/bin/slc -dso cell.sl
	SHADERS=$(BMRT)/shaders $(BMRT)/bin/rendrib cell.rib

aqsis: tangle
	aqsl cell.sl
	aqsis -shaders /usr/local/aqsis/shaders cell.rib
