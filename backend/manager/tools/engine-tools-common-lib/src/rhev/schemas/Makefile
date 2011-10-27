PYTHON=python
EXCLUDES=__init__.py
GENDS=/usr/bin/generateDS.py --silence
PACKAGE = $(shell basename `pwd`)
PYVER := $(shell $(PYTHON) -c 'import sys; print "%.3s" %(sys.version)')
PYSYSDIR := $(shell $(PYTHON) -c 'import sys; print sys.prefix')
PYLIBDIR = $(PYSYSDIR)/lib/python$(PYVER)
PKGDIR = $(PYLIBDIR)/site-packages/$(PACKAGE)
.SILENT:  # don't echo commands executed 

xsdtopy:
	for p in $(wildcard *.xsd) ; do \
		echo Compiling $$p to python; \
		$(GENDS) -o `basename $$p .xsd`.py $$p; \
	done	
	
all: 
	$(PYTHON) -m compileall -x $(EXCLUDES) .
	$(PYTHON) -OO -m compileall -x $(EXCLUDES) .	
 	
clean:
	rm -f *.pyc *.pyo *~
 	



