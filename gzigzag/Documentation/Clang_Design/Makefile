DEP=../wmlinc/article.wml

all: thales clasm clasm-tutorial

clasm: clasm.html clasm-ns4.html

clasm-tutorial: clasm-tutorial.html clasm-tutorial-ns4.html

thales: thales.html thales-ns4.html

thales.html: thales.wml $(DIAGRAMS) $(DEP)

thales-ns4.html: thales.wml $(DIAGRAMS) $(DEP)

ps: design.ps

include ../lib.mk
