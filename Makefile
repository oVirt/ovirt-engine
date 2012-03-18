# ====================================================================
#
#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# ====================================================================
#
# This software consists of voluntary contributions made by many
# individuals on behalf of the Apache Software Foundation.  For more
# information on the Apache Software Foundation, please see
# <http://www.apache.org/>.

MVN=$(shell which mvn)
BUILD_FLAGS=-P gwt-admin,gwt-user
DEPLOY_FLAGS=-f deploy.xml
JBOSS_HOME=/usr/share/jboss-as
EAR_DIR=/usr/share/ovirt-engine/engine.ear
EAR_SRC_DIR=ear/target/engine
PY_SITE_PKGS:=$(shell python -c "from distutils.sysconfig import get_python_lib as f;print f()")

# RPM version
APP_VERSION:=$(shell cat pom.xml | grep '<engine.version>' | awk -F\> '{print $$2}' | awk -F\< '{print $$1}')
RPM_VERSION:=$(shell echo $(APP_VERSION) | sed "s/-/_/")

# Release Version; used to create y in <x.x.x-y> numbering.
# Should be used to create releases.
RELEASE_VERSION=1.8

SPEC_FILE_IN=packaging/fedora/spec/ovirt-engine.spec.in
SPEC_FILE=ovirt-engine.spec
RPMBUILD=$(shell bash -c "pwd -P")/rpmbuild
SRCRPMBUILD=$(shell bash -c "pwd -P")/srcrpmbuild
OUTPUT_DIR=$(shell bash -c "pwd -P")/output
TARBALL=ovirt-engine-$(RPM_VERSION).tar.gz
SRPM=$(OUTPUT_DIR)/ovirt-engine-$(RPM_VERSION)*.src.rpm
ARCH=$(shell uname -i)
BUILD_FILE=$(shell bash -c "pwd -P")/build_mvn
SOURCE_DIR=$(RPMBUILD)/SOURCES

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

install: build_mvn pre_copy create_dirs install_ear common_install

install_without_maven: create_dirs install_brew_ear common_install

common_install: install_quartz install_tools install_image_uploader\
	install_config install_log_collector install_iso_uploader \
	install_sysprep install_notification_service install_db_scripts \
	install_misc install_setup install_sec

# Brew compatibility hack
# We want both env (local and brew) to work the same
pre_copy:
	echo $(SOURCE_DIR)
	cp -f ./backend/manager/tools/engine-tools-common/target/engine-tools-common-$(APP_VERSION).jar $(SOURCE_DIR)/
	cp -f ./backend/manager/tools/engine-config/target/engine-config-$(APP_VERSION).jar $(SOURCE_DIR)/
	cp -f ./backend/manager/modules/engineencryptutils/target/engineencryptutils-$(APP_VERSION).jar $(SOURCE_DIR)/
	cp -f ./backend/manager/modules/compat/target/compat-$(APP_VERSION).jar $(SOURCE_DIR)/
	cp -f ./backend/manager/tools/engine-notifier/engine-notifier-service/target/engine-notifier-service-$(APP_VERSION).jar $(SOURCE_DIR)/
	mkdir -p $(SOURCE_DIR)/ear
	cp -rf $(EAR_SRC_DIR)/* $(SOURCE_DIR)/ear/

tarball: $(TARBALL)
$(TARBALL):
	tar zcf $(TARBALL) `git ls-files`

srpm: $(SRPM)

$(SRPM): tarball $(SPEC_FILE_IN)
	mkdir -p $(OUTPUT_DIR)
	sed -e 's/^Version:.*/Version: $(RPM_VERSION)/' \
            -e 's/^Release:.*/Release: $(RELEASE_VERSION)%{?dist}/' $(SPEC_FILE_IN) > $(SPEC_FILE)
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
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/{sysprep,kerberos,scripts,3rd-party-lib,engine.ear,conf,dbscripts,resources,ovirt-isos,iso-uploader,log-collector,image_uploader,db-backups,engine.ear}
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/engine-config/lib
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/notifier/lib
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/engine-manage-domains/lib
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/log-collector/schemas
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/iso-uploader/schemas
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/image-uploader/schemas
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/image-uploader/ovf
	@mkdir -p $(PREFIX)/usr/share/ovirt-engine/scripts/plugins
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
	cp -rf $(SOURCE_DIR)/ear/* $(PREFIX)$(EAR_DIR)

install_brew_ear:
	@echo "*** Deploying EAR to $(PREFIX)"
	mkdir -p $(PREFIX)$(EAR_DIR)
	unzip $(SOURCE_DIR)/*.ear -d $(PREFIX)$(EAR_DIR)

install_quartz:
	@echo "*** Deploying quartz.jar to $(PREFIX)"
#	cp -f ear/target/quartz/quartz*.jar $(PREFIX)$(JBOSS_HOME)/common/lib/

install_tools:
	@echo "*** Installing Common Tools"
	cp -f $(SOURCE_DIR)/engine-tools-common-$(APP_VERSION).jar $(PREFIX)/usr/share/java/
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
	cp -f ./packaging/fedora/setup/setup_params.py $(PREFIX)/usr/share/ovirt-engine/scripts
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/setup_params.py
	cp -f ./packaging/fedora/setup/setup_sequences.py $(PREFIX)/usr/share/ovirt-engine/scripts
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/setup_sequences.py
	cp -f ./packaging/fedora/setup/setup_controller.py $(PREFIX)/usr/share/ovirt-engine/scripts
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/setup_controller.py
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
	cp -f ./packaging/fedora/setup/plugins/* $(PREFIX)/usr/share/ovirt-engine/scripts/plugins/
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/plugins/*
	ln -s /usr/share/ovirt-engine/scripts/engine-cleanup.py $(PREFIX)/usr/bin/engine-cleanup
	cp -f ./packaging/fedora/setup/engine-upgrade.py $(PREFIX)/usr/share/ovirt-engine/scripts
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/engine-upgrade.py
	ln -s /usr/share/ovirt-engine/scripts/engine-upgrade.py $(PREFIX)/usr/bin/engine-upgrade
	cp -f ./packaging/fedora/setup/post_upgrade.py $(PREFIX)/usr/share/ovirt-engine/scripts
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/post_upgrade.py
	cp -f ./packaging/fedora/setup/engine-check-update $(PREFIX)/usr/bin/
	chmod 755 $(PREFIX)/usr/bin/engine-check-update
	sed -i "s/MYVERSION/$(RPM_VERSION)-$(RELEASE_VERSION)/" $(PREFIX)/usr/share/ovirt-engine/resources/jboss/ROOT.war/engineVersion.js

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
	cp -f $(SOURCE_DIR)/engine-config-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/engine-config/lib/
	rm -f $(PREFIX)/usr/share/ovirt-engine/engine-config/lib/engine-config.jar
	ln -s /usr/share/ovirt-engine/engine-config/lib/engine-config-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/engine-config/lib/engine-config.jar
	cp -f $(SOURCE_DIR)/engineencryptutils-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/engine-config/lib/engine-encryptutils.jar
	cp -f $(SOURCE_DIR)/compat-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/engine-config/lib/engine-compat.jar
	rm -f $(PREFIX)/usr/bin/engine-config
	ln -s /usr/share/ovirt-engine/engine-config/engine-config $(PREFIX)/usr/bin/engine-config

	cp -f ./backend/manager/conf/kerberos/engine-manage-domains $(PREFIX)/usr/share/ovirt-engine/engine-manage-domains/
	chmod 750 $(PREFIX)/usr/share/ovirt-engine/engine-manage-domains/engine-manage-domains
	cp -f ./backend/manager/modules/utils/src/main/resources/engine-manage-domains.conf $(PREFIX)/etc/ovirt-engine/engine-manage-domains/
	chmod 755 $(PREFIX)/etc/ovirt-engine/engine-manage-domains/engine-manage-domains.conf
	cp -f ./backend/manager/modules/utils/src/main/resources/engine-manage-domains/log4j.xml $(PREFIX)/etc/ovirt-engine/engine-manage-domains/
	chmod 644 $(PREFIX)/etc/ovirt-engine/engine-manage-domains/log4j.xml
	cp -f $(SOURCE_DIR)/compat-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/engine-manage-domains/lib/engine-compat.jar
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

install_image_uploader:
	@echo "*** Deploying image uploader"
	cp -f ./backend/manager/tools/engine-image-uploader/src/engine-image-uploader.py $(PREFIX)/usr/share/ovirt-engine/image-uploader/
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/image-uploader/engine-image-uploader.py
	cp -f ./backend/manager/tools/engine-image-uploader/src/imageuploader.conf $(PREFIX)/etc/ovirt-engine/
	chmod 600 $(PREFIX)/etc/ovirt-engine/imageuploader.conf
	cp -f ./backend/manager/tools/engine-tools-common-lib/src/rhev/schemas/api.py $(PREFIX)/usr/share/ovirt-engine/image-uploader/schemas
	cp -f ./backend/manager/tools/engine-tools-common-lib/src/rhev/schemas/__init__.py $(PREFIX)/usr/share/ovirt-engine/image-uploader/schemas
	cp -f ./backend/manager/tools/engine-image-uploader/src/ovf/__init__.py $(PREFIX)/usr/share/ovirt-engine/image-uploader/ovf
	cp -f ./backend/manager/tools/engine-image-uploader/src/ovf/ovfenvelope.py $(PREFIX)/usr/share/ovirt-engine/image-uploader/ovf
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/image-uploader/schemas/*
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/image-uploader/ovf/*
	rm -f $(PREFIX)/usr/bin/engine-image-uploader
	ln -s /usr/share/ovirt-engine/image-uploader/engine-image-uploader.py $(PREFIX)/usr/bin/engine-image-uploader

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
	cp -f $(SOURCE_DIR)/engine-notifier-service-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/notifier/engine-notifier.jar
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
	cp -f ./backend/manager/conf/vds_installer.py $(PREFIX)/usr/share/ovirt-engine/scripts/
	chmod 755 $(PREFIX)/usr/share/ovirt-engine/scripts/vds_installer.py
	ln -s /usr/share/java/postgresql-jdbc.jar $(PREFIX)$(JBOSS_HOME)/modules/org/postgresql/main/postgresql-jdbc.jar
	cp -f ./backend/manager/conf/jboss-log4j.xml $(PREFIX)/usr/share/ovirt-engine/conf
	cp -f ./packaging/fedora/setup/resources/postgres/postgres-ds.xml $(PREFIX)/usr/share/ovirt-engine/conf
	cp -f ./LICENSE $(PREFIX)/usr/share/ovirt-engine
	cp -f ./packaging/resources/ovirtlogrot.sh ${PREFIX}/usr/share/ovirt-engine/scripts/
	cp -f ./packaging/resources/ovirt-cron ${PREFIX}/etc/cron.daily/
	cp -f ./packaging/resources/ovirt-tmpfilesd ${PREFIX}/etc/tmpfiles.d/ovirt-engine.conf
