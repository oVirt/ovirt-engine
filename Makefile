# Copyright 2008 Red Hat, Inc. and/or its affiliates.
#
# Licensed to you under the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.  See the files README and
# LICENSE_GPL_v2 which accompany this distribution.
#
MVN=$(shell which mvn)
BUILD_FLAGS=-P gwt-admin,gwt-user
DEPLOY_FLAGS=-f deploy.xml
JBOSS_HOME=/usr/share/jboss-as
EAR_DIR=/usr/share/ovirt-engine/engine.ear
EAR_SRC_DIR=ear/target/engine
PY_SITE_PKGS:=$(shell python -c "from distutils.sysconfig import get_python_lib as f;print f()")
APP_VERSION:=$(shell cat pom.xml | grep '<engine.version>' | awk -F\> '{print $$2}' | awk -F\< '{print $$1}')
RPM_VERSION:=$(shell echo $(APP_VERSION) | sed "s/-/_/")

#RPM_RELEASE:=$(shell echo $(APP_VERSION) | awk -F\. '{print $$3"%{?dist}"}')
SPEC_FILE_IN=packaging/fedora/spec/ovirt-engine.spec.in
SPEC_FILE=ovirt-engine.spec
RPMBUILD=$(shell bash -c "pwd -P")/rpmbuild
SRCRPMBUILD=$(shell bash -c "pwd -P")/srcrpmbuild
OUTPUT_DIR=$(shell bash -c "pwd -P")/output
TARBALL=ovirt-engine-$(RPM_VERSION).tar.gz
SRPM=$(OUTPUT_DIR)/ovirt-engine-$(RPM_VERSION)*.src.rpm
ARCH=$(shell uname -i)
BUILD_FILE=$(shell bash -c "pwd -P")/build_mvn

CURR_DIR=$(shell bach -c "pwd -P")
all: build_mvn

build_mvn:
	export MAVEN_OPTS="-XX:MaxPermSize=512m"
	$(MVN) install $(BUILD_FLAGS) -D skipTests
	touch $(BUILD_FILE)

clean:
	$(MVN) clean
	rm -rf $(RPMBUILD) $(SPEC_FILE) $(OUTPUT_DIR) $(SRCRPMBUILD) $(BUILD_FILE)

test:
	$(MVN) install $(BUILD_FLAGS)

install: build_mvn create_dirs install_ear install_quartz install_tools \
		install_config install_log_collector install_iso_uploader \
		install_sysprep install_notification_service install_db_scripts \
		install_misc install_setup install_sec

tarball: $(TARBALL)
$(TARBALL):
	tar zcf $(TARBALL) `git ls-files`

srpm: $(SRPM)

$(SRPM): tarball $(SPEC_FILE_IN)
	mkdir -p $(OUTPUT_DIR)
	sed 's/^Version:.*/Version: $(RPM_VERSION)/' $(SPEC_FILE_IN) > $(SPEC_FILE)
	mkdir -p $(SRCRPMBUILD)/{SPECS,RPMS,SRPMS,SOURCES,BUILD,BUILDROOT}
	cp -f $(SPEC_FILE) $(SRCRPMBUILD)/SPECS/
	cp -f  $(TARBALL) $(SRCRPMBUILD)/SOURCES/
	rpmbuild -bs --define="_topdir $(SRCRPMBUILD)" --define="_sourcedir ." $(SPEC_FILE)
	mv $(SRCRPMBUILD)/SRPMS/*.rpm $(OUTPUT_DIR)
	rm -rf $(SRCRPMBUILD) $(SPEC_FILE) $(TARBALL)

rpm: $(SRPM)
	rm -rf $(RPMBUILD)
	mkdir -p $(RPMBUILD)/{SPECS,RPMS,SRPMS,SOURCES,BUILD,BUILDROOT}
	rpmbuild  --define="_topdir $(RPMBUILD)" --rebuild  $<
	mv $(RPMBUILD)/RPMS/$(ARCH)/*.rpm $(OUTPUT_DIR)
	rm -rf $(RPMBUILD)

create_dirs:
	@echo "*** Creating Directories"
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/{sysprep,kerberos,scripts,3rd-party-lib,engine.ear,conf,dbscripts,resources,ovirt-isos,iso-uploader,log-collector,db-backups,engine.ear}
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/engine-config/lib
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/notifier/lib
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/engine-manage-domains/lib
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/log-collector/schemas
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/iso-uploader/schemas
	@mkdir -p $(PREFIX)/usr/share/java
	@mkdir -p $(PREFIX)/usr/bin
	@mkdir -p $(PREFIX)/usr/share/man/man8
	@mkdir -p $(PREFIX)$(PY_SITE_PKGS)/sos/plugins
	@mkdir -p $(PREFIX)/etc/ovirt-engine/notifier
	@mkdir -p $(PREFIX)/var/log/ovirt-engine/{notifier,engine-manage-domains}
	@mkdir -p $(PREFIX)/var/run/ovirt-engine/notifier
	@mkdir -p $(PREFIX)/var/lock/ovirt-engine
	@mkdir -p $(PREFIX)/etc/{init.d,tmpfiles.d,cron.daily}
	@mkdir -p $(PREFIX)/etc/ovirt-engine/{engine-config,engine-manage-domains}
	@mkdir -p $(PREFIX)$(EAR_DIR)
	@mkdir -p $(PREFIX)$(JBOSS_HOME)/modules/org/postgresql/main/
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/resources/jboss/modules/org
	@mkdir -p $(PREFIX)/etc/pki/ovirt-engine/{keys,private,requests,certs}

install_ear:
	@echo "*** Deploying EAR to $(PREFIX)"
	mkdir -p $(PREFIX)$(EAR_DIR)
	cp -rf $(EAR_SRC_DIR)/* $(PREFIX)$(EAR_DIR)

install_quartz:
	@echo "*** Deploying quartz.jar to $(PREFIX)"
#	cp -f ear/target/quartz/quartz*.jar $(PREFIX)$(JBOSS_HOME)/common/lib/

install_tools:
	@echo "*** Installing Common Tools"
	cp -f ./backend/manager/tools/engine-tools-common/target/engine-tools-common-$(APP_VERSION).jar $(PREFIX)/usr/share/java/
	rm -f $(PREFIX)/usr/share/java/engine-tools-common.jar
	ln -s /usr/share/java/engine-tools-common-$(APP_VERSION).jar $(PREFIX)/usr/share/java/engine-tools-common.jar

install_setup:
	@echo "*** Deploying setup executables"
	cp -f ./packaging/fedora/setup/engine-config-install.properties $(PREFIX)/usr/share/ovirt-engine/conf
	chmod 644 $(PREFIX)/usr/share/ovirt-engine/conf/engine-config-install.properties
	cp -f ./packaging/fedora/setup/iptables.default $(PREFIX)/usr/share/ovirt-engine/conf
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/conf/iptables.default
	cp -f ./packaging/fedora/setup/nfs.sysconfig $(PREFIX)/usr/share/ovirt-engine/conf
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/conf/nfs.sysconfig
	cp -f ./packaging/fedora/setup/engine-setup.py $(PREFIX)/usr/share/ovirt-engine/scripts
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/engine-setup.py
	cp -f ./packaging/fedora/setup/nfsutils.py $(PREFIX)/usr/share/ovirt-engine/scripts
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/nfsutils.py
	cp -f ./packaging/fedora/setup/basedefs.py $(PREFIX)/usr/share/ovirt-engine/scripts
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/basedefs.py
	cp -f ./packaging/fedora/setup/engine_validators.py $(PREFIX)/usr/share/ovirt-engine/scripts
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/engine_validators.py
	cp -f ./packaging/fedora/setup/common_utils.py $(PREFIX)/usr/share/ovirt-engine/scripts
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/common_utils.py
	cp -f ./packaging/fedora/setup/resources/jboss/web-conf.js $(PREFIX)/etc/ovirt-engine
	chmod 755 $(PREFIX)/etc/ovirt-engine/web-conf.js
	cp -f ./packaging/fedora/setup/output_messages.py $(PREFIX)/usr/share/ovirt-engine/scripts
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/output_messages.py
	ln -s /usr/share/ovirt-engine/scripts/engine-setup.py $(PREFIX)/usr/bin/engine-setup
	cp -af ./packaging/fedora/setup/resources/jboss/* $(PREFIX)/usr/share/ovirt-engine/resources/jboss/
	cp -af ./deployment/modules/org/* $(PREFIX)/usr/share/ovirt-engine/resources/jboss/modules/org/
	ln -s /usr/share/java/postgresql-jdbc.jar $(PREFIX)/usr/share/ovirt-engine/resources/jboss/modules/org/postgresql/main/
	cp -f ./packaging/fedora/setup/engine-cleanup.py $(PREFIX)/usr/share/ovirt-engine/scripts
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/engine-cleanup.py
	ln -s /usr/share/ovirt-engine/scripts/engine-cleanup.py $(PREFIX)/usr/bin/engine-cleanup
	sed -i "s/MYVERSION/$(RPM_VERSION)/" $(PREFIX)/usr/share/ovirt-engine/resources/jboss/ROOT.war/engineVersion.js

install_sec:
	cd backend/manager/3rdparty/pub2ssh/; chmod +x pubkey2ssh.sh; mkdir -p bin; ./pubkey2ssh.sh; cd -
	rm -rf $(PREFIX)/etc/pki/engine-config/*.bat
	cp -f ./backend/manager/3rdparty/pub2ssh/bin/pubkey2ssh $(PREFIX)/etc/pki/ovirt-engine
	chmod 755 $(PREFIX)/etc/pki/ovirt-engine
	cp -a  ./backend/manager/conf/ca/* $(PREFIX)/etc/pki/ovirt-engine

install_config:
	@echo "*** Deploying engine-config & engine-manage-domains"
	cp -f ./backend/manager/tools/engine-config/src/main/resources/engine-config $(PREFIX)/usr/share/ovirt-engine/engine-config/
	chmod 750 $(PREFIX)/usr/share/ovirt-engine/engine-config/engine-config
	cp -f ./backend/manager/tools/engine-config/src/main/resources/engine-config.conf $(PREFIX)/etc/ovirt-engine/engine-config/
	chmod 755 $(PREFIX)/etc/ovirt-engine/engine-config/engine-config.conf
	cp -f ./backend/manager/tools/engine-config/src/main/resources/engine-config.*properties $(PREFIX)/etc/ovirt-engine/engine-config/
	chmod 644 $(PREFIX)/etc/ovirt-engine/engine-config/engine-config.*properties
	cp -f ./backend/manager/tools/engine-config/src/main/resources/log4j.xml $(PREFIX)/etc/ovirt-engine/engine-config/
	chmod 644 $(PREFIX)/etc/ovirt-engine/engine-config/log4j.xml
	cp -f ./backend/manager/tools/engine-config/target/engine-config-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/engine-config/lib/
	rm -f $(PREFIX)/usr/share/ovirt-engine/engine-config/lib/engine-config.jar
	ln -s /usr/share/ovirt-engine/engine-config/lib/engine-config-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/engine-config/lib/engine-config.jar
	cp -f ./backend/manager/modules/engineencryptutils/target/engineencryptutils-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/engine-config/lib/
	cp -f ./ear/target/engine/lib/engine-compat.jar $(PREFIX)/usr/share/ovirt-engine/engine-config/lib/
	rm -f $(PREFIX)/usr/bin/engine-config
	ln -s /usr/share/ovirt-engine/engine-config/engine-config $(PREFIX)/usr/bin/engine-config

	cp -f ./backend/manager/conf/kerberos/engine-manage-domains $(PREFIX)/usr/share/ovirt-engine/engine-manage-domains/
	chmod 750 $(PREFIX)/usr/share/ovirt-engine/engine-manage-domains/engine-manage-domains
	cp -f ./backend/manager/modules/utils/src/main/resources/engine-manage-domains.conf $(PREFIX)/etc/ovirt-engine/engine-manage-domains/
	chmod 755 $(PREFIX)/etc/ovirt-engine/engine-manage-domains/engine-manage-domains.conf
	cp -f ./backend/manager/modules/utils/src/main/resources/engine-manage-domains/log4j.xml $(PREFIX)/etc/ovirt-engine/engine-manage-domains/
	chmod 644 $(PREFIX)/etc/ovirt-engine/engine-manage-domains/log4j.xml
	cp -f ./ear/target/engine/lib/engine-compat.jar $(PREFIX)/usr/share/ovirt-engine/engine-manage-domains/lib/
	rm -f $(PREFIX)/usr/bin/engine-manage-domains
	ln -s /usr/share/ovirt-engine/engine-manage-domains/engine-manage-domains $(PREFIX)/usr/bin/engine-manage-domains

install_log_collector:
	@echo "*** Deploying log collector"
	cp -f ./backend/manager/tools/engine-logcollector/src/rhev/logcollector.py $(PREFIX)/usr/share/ovirt-engine/log-collector/
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/log-collector/logcollector.py
	/usr/bin/gzip -c ./backend/manager/tools/engine-logcollector/src/rhev/engine-log-collector.8 > $(PREFIX)/usr/share/man/man8/engine-log-collector.8.gz
	chmod 644 $(PREFIX)/usr/share/man/man8/engine-log-collector.8.gz
	cp -f ./backend/manager/tools/engine-logcollector/src/rhev/logcollector.conf $(PREFIX)/etc/ovirt-engine/
	chmod 600 $(PREFIX)/etc/ovirt-engine/logcollector.conf
	cp -f ./backend/manager/tools/engine-tools-common-lib/src/rhev/schemas/api.py $(PREFIX)/usr/share/ovirt-engine/log-collector/schemas
	cp -f ./backend/manager/tools/engine-tools-common-lib/src/rhev/schemas/hypervisors.py $(PREFIX)/usr/share/ovirt-engine/log-collector/schemas
	cp -f ./backend/manager/tools/engine-tools-common-lib/src/rhev/schemas/__init__.py $(PREFIX)/usr/share/ovirt-engine/log-collector/schemas
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/log-collector/schemas/*
	cp -f ./backend/manager/tools/engine-logcollector/src/sos/plugins/jboss.py $(PREFIX)$(PY_SITE_PKGS)/sos/plugins
	cp -f ./backend/manager/tools/engine-logcollector/src/sos/plugins/engine.py $(PREFIX)$(PY_SITE_PKGS)/sos/plugins
	cp -f ./backend/manager/tools/engine-logcollector/src/sos/plugins/postgresql.py $(PREFIX)$(PY_SITE_PKGS)/sos/plugins
	chmod 755 $(PREFIX)$(PY_SITE_PKGS)/sos/plugins/*
	rm -f $(PREFIX)/usr/bin/engine-log-collector
	ln -s /usr/share/ovirt-engine/log-collector/logcollector.py $(PREFIX)/usr/bin/engine-log-collector	

install_iso_uploader:
	@echo "*** Deploying iso uploader"
	cp -f ./backend/manager/tools/engine-iso-uploader/src/engine-iso-uploader.py $(PREFIX)/usr/share/ovirt-engine/iso-uploader/
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/iso-uploader/engine-iso-uploader.py
	/usr/bin/gzip -c ./backend/manager/tools/engine-iso-uploader/src/engine-iso-uploader.8 > $(PREFIX)/usr/share/man/man8/engine-iso-uploader.8.gz
	chmod 644 $(PREFIX)/usr/share/man/man8/engine-iso-uploader.8.gz
	cp -f ./backend/manager/tools/engine-iso-uploader/src/isouploader.conf $(PREFIX)/etc/ovirt-engine/
	chmod 600 $(PREFIX)/etc/ovirt-engine/isouploader.conf
	cp -f ./backend/manager/tools/engine-tools-common-lib/src/rhev/schemas/api.py $(PREFIX)/usr/share/ovirt-engine/iso-uploader/schemas
	cp -f ./backend/manager/tools/engine-tools-common-lib/src/rhev/schemas/__init__.py $(PREFIX)/usr/share/ovirt-engine/iso-uploader/schemas
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/iso-uploader/schemas/*
	rm -f $(PREFIX)/usr/bin/engine-iso-uploader
	ln -s /usr/share/ovirt-engine/iso-uploader/engine-iso-uploader.py $(PREFIX)/usr/bin/engine-iso-uploader

install_sysprep:
	@echo "*** Deploying sysperp"
	cp -f ./backend/manager/conf/sysprep/* $(PREFIX)/usr/share/ovirt-engine/sysprep
	chmod 644 $(PREFIX)/usr/share/ovirt-engine/sysprep/*

install_notification_service:
	@echo "*** Deploying notification service"
	cp -f ./backend/manager/tools/engine-notifier/engine-notifier-resources/src/main/resources/log4j.xml $(PREFIX)/etc/ovirt-engine/notifier/
	chmod 644 $(PREFIX)/etc/ovirt-engine/notifier/log4j.xml
	cp -f ./backend/manager/tools/engine-notifier/engine-notifier-resources/src/main/resources/notifier.conf $(PREFIX)/etc/ovirt-engine/notifier/
	chmod 640 $(PREFIX)/etc/ovirt-engine/notifier/notifier.conf
	cp -f ./backend/manager/tools/engine-notifier/engine-notifier-resources/src/main/resources/notifier.sh $(PREFIX)/usr/share/ovirt-engine/notifier/
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/notifier/notifier.sh
	cp -f ./backend/manager/tools/engine-notifier/engine-notifier-resources/src/main/resources/engine-notifierd $(PREFIX)/etc/init.d/
	chmod 755 $(PREFIX)/etc/init.d/engine-notifierd
	cp -f ./backend/manager/tools/engine-notifier/engine-notifier-service/target/engine-notifier-service-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/notifier/engine-notifier.jar
	chmod 644 $(PREFIX)/usr/share/ovirt-engine/notifier/engine-notifier.jar

install_db_scripts:
	@echo "*** Deploying Database scripts"
	cp -a ./backend/manager/dbscripts/* $(PREFIX)/usr/share/ovirt-engine/dbscripts

install_misc:
	@echo "*** Copying additional files"
	cp -f ./backend/manager/conf/jaas.conf $(PREFIX)/usr/share/ovirt-engine/conf
	chmod 644 $(PREFIX)/usr/share/ovirt-engine/conf/jaas.conf
	cp -f ./backend/manager/conf/engine.conf $(PREFIX)/etc/ovirt-engine/
	chmod 640 $(PREFIX)/etc/ovirt-engine/engine.conf
	cp -f ./backend/manager/conf/jboss-log4j.xml $(PREFIX)/usr/share/ovirt-engine/conf
	chmod 644 $(PREFIX)/usr/share/ovirt-engine/conf/jboss-log4j.xml
	cp -f ./backend/manager/conf/kerberos/* $(PREFIX)/usr/share/ovirt-engine/kerberos
	chmod 644 $(PREFIX)/usr/share/ovirt-engine/kerberos/*
	rm -rf $(PREFIX)/usr/share/ovirt-engine/keberos/*.bat
	cp -f ./backend/manager/conf/vds_installer.py $(PREFIX)/usr/share/ovirt-engine/scripts
	cp -f ./packaging/ovirtlogrot.sh $(PREFIX)/usr/share/ovirt-engine/scripts
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/vds_installer.py
	ln -s /usr/share/java/postgresql-jdbc.jar $(PREFIX)$(JBOSS_HOME)/modules/org/postgresql/main/postgresql-jdbc.jar
	cp -f ./backend/manager/conf/jboss-log4j.xml $(PREFIX)/usr/share/ovirt-engine/conf
	cp -f ./packaging/fedora/setup/resources/postgres/postgres-ds.xml $(PREFIX)/usr/share/ovirt-engine/conf
	cp -f ./LICENSE $(PREFIX)/usr/share/ovirt-engine
	cp -f ./packaging/ovirtlogrot.sh ${PREFIX}/usr/share/ovirt-engine/scripts/
	cp -f ./packaging/resources/ovirt-cron ${PREFIX}/etc/cron.daily/
	cp -f ./packaging/resources/ovirt-tmpfilesd ${PREFIX}/etc/tmpfiles.d/ovirt-engine.conf


