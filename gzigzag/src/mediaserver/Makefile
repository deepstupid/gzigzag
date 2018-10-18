
src = $(wildcard *.java) $(wildcard */*.java)

# the following is specific to A-J's system...
# usejava = . ~/usejava &&
usejava =

GCJ = gcj-3.0

package = org.gzigzag.mediaserver.httpd

classdir = classes

CLASSPATH = $(classdir):$(shell echo $$CLASSPATH)
export CLASSPATH

all :
	$(usejava) CLASSPATH=$(classdir):$$CLASSPATH \
	    jikes +E -deprecation -d $(classdir) $(src)

bah : $(src:.java=.o)
	$(GCJ) -g --main=$(package).HTTPResponse -o $@ $^

%.o : %.java
	$(GCJ) -g -C -d $(classdir) $<
	$(GCJ) -g -c -d $(classdir) -o $@ $<

%.d : %.java
	set -e; $(GCJ) -M  $< \
	  | sed 's/\($*\)\.o[ :]*/\1.o $@ : /g' > $@.new
	mv $@.new $@

doc : all
	mkdir -p doc
	javadoc -classpath $(classdir) -d doc $(src)

clean :
	rm -f *.d *.o rm -r org *.class

#include $(src:.java=.d)
