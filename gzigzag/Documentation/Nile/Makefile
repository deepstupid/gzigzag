# DIAGRAMS=linkorder.png beamorder.png

all : nileug.html nileug-ns4.html nile.html nile-ns4.html

niledep = nile.wml ../wmlinc/art.css ../wmlinc/catart.wml  ../wmlinc/common.wml $(DIAGRAMS)
nileugdep = nileug.wml ../wmlinc/art.css ../wmlinc/catart.wml  ../wmlinc/common.wml $(DIAGRAMS)
nile.html: $(niledep)
nile-ns4.html: $(niledep)
nileug.html: $(nileugdep)
nileug-ns4.html: $(nileugdep)

include ../lib.mk


