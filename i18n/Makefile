# ====================================================================
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
# ====================================================================

.PHONY: prepare zanata-push push zanata-pull normalize copy-changed pull

JAVA_18=$(shell alternatives --list | grep 'jre_1.8.0_openjdk' | cut -f 3)
ZANATA_LOCALES=de,es,fr,it,ja,ko,pt-BR,zh-CN,ru,cs

prepare:
	mvn clean
	[ -e "target/" ] || mkdir -p target/zanata
	rsync --archive --prune-empty-dirs --info=name \
		--include="*/" \
		--include="backend/**/src/main/resources/*.properties" \
		--include="backend/**/src/main/resources/**/*.properties" \
		--include="frontend/**/src/main/resources/*.properties" \
		--include="frontend/**/src/main/resources/**/*.properties" \
		--exclude="*" \
		../ target/zanata

zanata-push: JAVA_HOME=$(JAVA_18)
zanata-push:
	mvn \
	  org.zanata:zanata-maven-plugin:4.6.2:push \
	  -Dzanata.pushType="source" \
	  -Dzanata.locales="$(ZANATA_LOCALES)"

push:	prepare zanata-push

zanata-pull: JAVA_HOME=$(JAVA_18)
zanata-pull:
	mvn \
	  org.zanata:zanata-maven-plugin:4.6.2:pull \
	  -Dzanata.pullType="trans" \
	  -Dzanata.locales="$(ZANATA_LOCALES)"

normalize:
	mvn compile exec:exec

copy-changed:
	./copy-changed.sh

pull:	prepare zanata-pull normalize copy-changed
