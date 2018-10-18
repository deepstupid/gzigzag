.PHONY: all clean

CS_SCM = constraint.scm util.scm

all: VanishingView.java

clean:
	rm -f VanishingView.java vanishing.out *~

VanishingView.java: VanishingView.jpp vanishing.out
	cpp -P $< > $@.new
	mv $@.new $@

vanishing.out: vanishing.scm $(CS_SCM)
	mzscheme -r $< > $@.new
	mv $@.new $@

