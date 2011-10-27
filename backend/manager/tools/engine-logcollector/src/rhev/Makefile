PYTHON=python
EXCLUDES=__init__.py
PACKAGE = $(shell basename `pwd`)
PYVER := $(shell $(PYTHON) -c 'import sys; print "%.3s" %(sys.version)')
PYSYSDIR := $(shell $(PYTHON) -c 'import sys; print sys.prefix')
PYLIBDIR = $(PYSYSDIR)/lib/python$(PYVER)
PKGDIR = $(PYLIBDIR)/site-packages/$(PACKAGE)

man:
	gzip -c engine-log-collector.8  > engine-log-collector.8.gz
	
all: man
	$(PYTHON) -m compileall -x $(EXCLUDES) .
	$(PYTHON) -OO -m compileall -x $(EXCLUDES) .	
 	
clean:
	rm -f *.pyc *.pyo *~
 	
#install: all
#	mkdir -p $(DESTDIR)/$(PKGDIR)
#	for p in $(wildcard *.py) ; do \
#		install -m 755 $$p $(DESTDIR)/$(PKGDIR)/$$p; \
#	done


