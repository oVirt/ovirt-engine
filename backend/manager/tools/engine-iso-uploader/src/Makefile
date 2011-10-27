PYTHON=python
XGETTEXT=xgettext
MSGINIT=msginit
MSGFMT=msgfmt
EXCLUDES=__init__.py
LOCALES=en_US en_CA
TMP_LOCALE_DIR=tmp_locale

.SILENT:  # don't echo commands executed 

# Step 1 of translation
create-pot:
	for p in $(filter-out $(EXCLUDES), $(wildcard *.py)) ; do \
		echo Generating Portable Object Template for $$p with $(XGETTEXT); \
		$(XGETTEXT) --language=Python --keyword=_ --output=`basename $$p .py`.pot $$p; \
	done
	
# Step 2 of translation.	
create-po: 
	for p in $(filter-out $(EXCLUDES), $(wildcard *.pot)) ; do \
		for l in $(LOCALES) ; do \
			echo "Generating Portable Object(s) for locale $$l from $$p with $(MSGINIT)"; \
			$(MSGINIT) --no-translator --output-file=`basename $$p .pot`-$$l.po --input=$$p --locale=$$l; \
		done \
	done
	echo "Please send the .po files to a translation center for translation, place the resulting files \
	back in this directory, and then run the makemo target."

# Step 3 of translation	
create-mo: 
	for l in $(LOCALES) ; do \
		echo "Generating .mo for $$l with $(MSGFMT) and placing results in $(TMP_LOCALE_DIR)/$$l/LC_MESSAGES"; \
		mkdir -p $(TMP_LOCALE_DIR)/$$l/LC_MESSAGES; \
		$(MSGFMT) --output-file=$(TMP_LOCALE_DIR)/$$l/LC_MESSAGES/`basename *-$$l.po -$$l.po`.mo *-$$l.po; \
	done
	
test:
	# To run this simple test...
	# 1. Run create-pot
	# 2. Run create-po
	# 3. Edit the "version" msgstr in engine-iso-uploader-en_CA.po with some sort of fake message
	# 4. Run this target 
	LC_MESSAGES=$PWD/$(TMP_LOCALE_DIR)/en_CA/LC_MESSAGES; LANG=en_CA; python engine-iso-uploader.py --version

man:
	gzip -c engine-iso-uploader.8  > engine-iso-uploader.8.gz	
	
all: man
	$(PYTHON) -m compileall -x $(EXCLUDES) .
	$(PYTHON) -OO -m compileall -x $(EXCLUDES) .	
 	
clean:
	rm -rf *.pyc *.pyo *~ *.po *.pot $(TMP_LOCALE_DIR)
 	


