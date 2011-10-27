
all: html html-single pdf

html:
	publican build --langs=en-US --formats=html

html-single:
	publican build --langs=en-US --formats=html-single

pdf:
	publican build --langs=en-US --formats=pdf

clean:
	publican clean

upload: all
	rsync -e ssh -vaPz --del --exclude=xml* tmp/en-US fedorapeople.org:~markmc/public_html/restapi-definition/
