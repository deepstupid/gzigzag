# DIAGRAMS=linkorder.png beamorder.png

all : ug.html ug-ns4.html newug.html newug-ns4.html

ugdep = ug.wml ../keybindings.wml ../wmlinc/article.wml ../wmlinc/common.wml $(DIAGRAMS)
ug.html : $(ugdep)
ug-ns4.html: $(ugdep)

newugdep = newug.wml ../keybindings.wml ../wmlinc/catart.wml  ../wmlinc/common.wml $(DIAGRAMS)
newug.html: $(newugdep)
newug-ns4.html: $(newugdep)

include ../lib.mk


