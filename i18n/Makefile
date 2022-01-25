# ====================================================================
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
# ====================================================================

.PHONY: prepare zanata-push push zanata-pull normalize copy-changed pull

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

zanata-push:
	mvn \
	  at.porscheinformatik.zanata:zanata-maven-plugin:4.7.8:push \
	  -Dzanata.pushType="source" \
	  -Dzanata.locales="$(ZANATA_LOCALES)"

push:	prepare zanata-push

zanata-pull:
	mvn \
	  at.porscheinformatik.zanata:zanata-maven-plugin:4.7.8:pull \
	  -Dzanata.pullType="trans" \
	  -Dzanata.locales="$(ZANATA_LOCALES)"

normalize:
	mvn compile exec:exec

copy-changed:
	./copy-changed.sh

pull:	prepare zanata-pull normalize copy-changed
