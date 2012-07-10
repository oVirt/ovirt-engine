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
EXTRA_BUILD_FLAGS=
BUILD_FLAGS=-P gwt-admin,gwt-user
DEPLOY_FLAGS=-f deploy.xml
EAR_DIR=/usr/share/ovirt-engine/engine.ear
EAR_SRC_DIR=ear/target/engine
PY_SITE_PKGS:=$(shell python -c "from distutils.sysconfig import get_python_lib as f;print f()")

# RPM version
APP_VERSION:=$(shell cat pom.xml | grep '<engine.version>' | awk -F\> '{print $$2}' | awk -F\< '{print $$1}')
RPM_VERSION:=$(shell echo $(APP_VERSION) | sed "s/-/_/")

# Release Version; used to create y in <x.x.x-y> numbering.
# Should be used to create releases.
RELEASE_VERSION=3

SPEC_FILE_IN=packaging/fedora/spec/ovirt-engine.spec.in
SPEC_FILE=ovirt-engine.spec
RPMBUILD=rpmbuild
OUTPUT_RPMBUILD=$(shell pwd -P)/tmp.rpmbuild
OUTPUT_DIR=output
TARBALL=ovirt-engine-$(RPM_VERSION).tar.gz
SRPM=$(OUTPUT_DIR)/ovirt-engine-$(RPM_VERSION)*.src.rpm
ARCH=noarch
BUILD_FILE=$(shell bash -c "pwd -P")/build_mvn
SOURCE_DIR=$(OUTPUT_RPMBUILD)/SOURCES

CURR_DIR=$(shell bach -c "pwd -P")
all: build_mvn

build_mvn:
	export MAVEN_OPTS="${MAVEN_OPTS} -XX:MaxPermSize=512m"
	$(MVN) install $(BUILD_FLAGS) $(EXTRA_BUILD_FLAGS) -D skipTests
	touch $(BUILD_FILE)

clean:
	$(MVN) clean $(EXTRA_BUILD_FLAGS)
	rm -rf $(OUTPUT_RPMBUILD) $(SPEC_FILE) $(OUTPUT_DIR) $(BUILD_FILE)

test:
	$(MVN) install $(BUILD_FLAGS) $(EXTRA_BUILD_FLAGS)

install: \
	build_mvn \
	pre_copy \
	create_dirs \
	install_ear \
	common_install

install_without_maven: \
	create_dirs \
	install_brew_ear \
	common_install

common_install: \
	install_config \
	install_sysprep \
	install_notification_service \
	install_db_scripts \
	install_setup \
	install_misc \
	install_sec \
	install_aio_plugin \
	install_jboss_modules \
	install_service

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

tarball:
	sed -e 's/^Version:.*/Version: $(RPM_VERSION)/' \
            -e 's/^Release:.*/Release: $(RELEASE_VERSION)%{?dist}/' $(SPEC_FILE_IN) > $(SPEC_FILE)
	tar zcf $(TARBALL) `git ls-files` $(SPEC_FILE)
	rm -f $(SPEC_FILE)
	@echo
	@echo You can use $(RPMBUILD) -tb $(TARBALL) to produce rpms
	@echo

srpm:	tarball
	rm -rf $(OUTPUT_RPMBUILD)
	mkdir -p $(OUTPUT_RPMBUILD)/{SPECS,RPMS,SRPMS,SOURCES,BUILD,BUILDROOT}
	mkdir -p $(OUTPUT_DIR)
	$(RPMBUILD) -ts --define="_topdir $(OUTPUT_RPMBUILD)" $(TARBALL)
	mv $(OUTPUT_RPMBUILD)/SRPMS/*.rpm $(OUTPUT_DIR)
	rm -rf $(OUTPUT_RPMBUILD)
	@echo
	@echo srpm is ready at $(OUTPUT_DIR)
	@echo

rpm:	srpm
	rm -rf $(OUTPUT_RPMBUILD)
	mkdir -p $(OUTPUT_RPMBUILD)/{SPECS,RPMS,SRPMS,SOURCES,BUILD,BUILDROOT}
	mkdir -p $(OUTPUT_DIR)
	$(RPMBUILD)  --define="_topdir $(OUTPUT_RPMBUILD)" --rebuild $(SRPM)
	mv $(OUTPUT_RPMBUILD)/RPMS/$(ARCH)/*.rpm $(OUTPUT_DIR)
	rm -rf $(OUTPUT_RPMBUILD)
	@echo
	@echo rpms are ready at $(OUTPUT_DIR)
	@echo

create_dirs:
	@echo "*** Creating Directories"
	@install -dm 755 $(PREFIX)/usr/share/ovirt-engine/{kerberos,scripts,engine.ear,conf,dbscripts,resources,ovirt-isos,db-backups,engine.ear}
	@install -dm 755 $(PREFIX)/usr/share/ovirt-engine/engine-config
	@install -dm 755 $(PREFIX)/usr/share/ovirt-engine/notifier
	@install -dm 755 $(PREFIX)/usr/share/ovirt-engine/engine-manage-domains
	@install -dm 755 $(PREFIX)/usr/share/ovirt-engine/scripts/plugins
	@install -dm 755 $(PREFIX)/usr/share/ovirt-engine/service
	@install -dm 755 $(PREFIX)/usr/share/java
	@install -dm 755 $(PREFIX)/usr/bin
	@install -dm 755 $(PREFIX)/usr/share/man/man8
	@install -dm 755 $(PREFIX)$(PY_SITE_PKGS)/sos/plugins
	@install -dm 755 $(PREFIX)/etc/ovirt-engine/notifier
	@install -dm 755 $(PREFIX)/var/log/ovirt-engine/{notifier,engine-manage-domains}
	@install -dm 755 $(PREFIX)/var/run/ovirt-engine/notifier
	@install -dm 755 $(PREFIX)/var/lock/ovirt-engine
	@install -dm 755 $(PREFIX)/etc/tmpfiles.d
	@install -dm 755 $(PREFIX)/etc/cron.daily
	@install -dm 755 $(PREFIX)/etc/security/limits.d
	@install -dm 755 $(PREFIX)/etc/rc.d/init.d
	@install -dm 755 $(PREFIX)/etc/ovirt-engine/{engine-config,engine-manage-domains,sysprep}
	@install -dm 755 $(PREFIX)$(EAR_DIR)
	@install -dm 755 $(PREFIX)/usr/share/ovirt-engine/resources/jboss/modules/org
	@install -dm 755 $(PREFIX)/etc/pki/ovirt-engine/{keys,private,requests,certs}
	@install -dm 755 $(PREFIX)/etc/sysconfig
	@install -dm 755 $(PREFIX)/var/lib/ovirt-engine
	@install -dm 755 $(PREFIX)/var/lib/ovirt-engine/deployments
	@install -dm 755 $(PREFIX)/var/lib/ovirt-engine/content
	@install -dm 755 $(PREFIX)/var/cache/ovirt-engine
	@install -dm 755 $(PREFIX)/usr/lib/systemd/system

install_ear:
	@echo "*** Deploying EAR to $(PREFIX)"
	install -dm 755 $(PREFIX)$(EAR_DIR)
	cp -rf $(SOURCE_DIR)/ear/* $(PREFIX)$(EAR_DIR)

install_brew_ear:
	@echo "*** Deploying EAR to $(PREFIX)"
	install -dm 755 $(PREFIX)$(EAR_DIR)
	unzip $(SOURCE_DIR)/*.ear -d $(PREFIX)$(EAR_DIR)

install_setup:
	@echo "*** Deploying setup executables"

	# Configuration files:
	install -m 644 packaging/fedora/setup/engine-config-install.properties $(PREFIX)/usr/share/ovirt-engine/conf
	install -m 644 packaging/fedora/setup/iptables.default $(PREFIX)/usr/share/ovirt-engine/conf
	install -m 644 packaging/fedora/setup/nfs.sysconfig $(PREFIX)/usr/share/ovirt-engine/conf

	# Shared python modules:
	install -m 644 packaging/fedora/setup/nfsutils.py $(PREFIX)/usr/share/ovirt-engine/scripts
	install -m 644 packaging/fedora/setup/basedefs.py $(PREFIX)/usr/share/ovirt-engine/scripts
	install -m 644 packaging/fedora/setup/engine_validators.py $(PREFIX)/usr/share/ovirt-engine/scripts
	install -m 644 packaging/fedora/setup/setup_params.py $(PREFIX)/usr/share/ovirt-engine/scripts
	install -m 644 packaging/fedora/setup/setup_sequences.py $(PREFIX)/usr/share/ovirt-engine/scripts
	install -m 644 packaging/fedora/setup/setup_controller.py $(PREFIX)/usr/share/ovirt-engine/scripts
	install -m 644 packaging/fedora/setup/common_utils.py $(PREFIX)/usr/share/ovirt-engine/scripts
	install -m 644 packaging/fedora/setup/output_messages.py $(PREFIX)/usr/share/ovirt-engine/scripts
	install -m 644 packaging/fedora/setup/post_upgrade.py $(PREFIX)/usr/share/ovirt-engine/scripts

	# Example Plugin:
	install -m 644 packaging/fedora/setup/plugins/example_plugin_000.py $(PREFIX)/usr/share/ovirt-engine/scripts/plugins

	# Main programs and links:
	install -m 755 packaging/fedora/setup/engine-setup.py $(PREFIX)/usr/share/ovirt-engine/scripts
	ln -s /usr/share/ovirt-engine/scripts/engine-setup.py $(PREFIX)/usr/bin/engine-setup
	install -m 755 packaging/fedora/setup/engine-cleanup.py $(PREFIX)/usr/share/ovirt-engine/scripts
	ln -s /usr/share/ovirt-engine/scripts/engine-cleanup.py $(PREFIX)/usr/bin/engine-cleanup
	install -m 755 packaging/fedora/setup/engine-upgrade.py $(PREFIX)/usr/share/ovirt-engine/scripts
	ln -s /usr/share/ovirt-engine/scripts/engine-upgrade.py $(PREFIX)/usr/bin/engine-upgrade
	install -m 755 packaging/fedora/setup/engine-check-update $(PREFIX)/usr/bin/

	# Configuration file for the index page:
	install -m 644 packaging/fedora/setup/resources/jboss/web-conf.js $(PREFIX)/etc/ovirt-engine
	sed -i "s/MYVERSION/$(RPM_VERSION)-$(RELEASE_VERSION)/" $(PREFIX)$(EAR_DIR)/root.war/engineVersion.js

install_aio_plugin:
	install -m 755 packaging/fedora/setup/plugins/all_in_one_100.py $(PREFIX)/usr/share/ovirt-engine/scripts/plugins

install_sec:
	# Create the directories:
	install -dm 755 $(PREFIX)/etc/pki/ovirt-engine
	install -dm 755 $(PREFIX)/etc/pki/ovirt-engine/certs
	install -dm 755 $(PREFIX)/etc/pki/ovirt-engine/keys
	install -dm 755 $(PREFIX)/etc/pki/ovirt-engine/private
	install -dm 755 $(PREFIX)/etc/pki/ovirt-engine/requests

	# Configuration files:
	install -m 644 backend/manager/conf/ca/openssl.conf $(PREFIX)/etc/pki/ovirt-engine
	install -m 644 backend/manager/conf/ca/cacert.template $(PREFIX)/etc/pki/ovirt-engine
	install -m 644 backend/manager/conf/ca/cert.template $(PREFIX)/etc/pki/ovirt-engine

	# Certificate database:
	install -m 644 backend/manager/conf/ca/database.txt $(PREFIX)/etc/pki/ovirt-engine
	install -m 644 backend/manager/conf/ca/serial.txt $(PREFIX)/etc/pki/ovirt-engine

	# Scripts:
	install -m 755 backend/manager/conf/ca/*.sh $(PREFIX)/etc/pki/ovirt-engine
	install -m 755 backend/manager/conf/ca/generate-ssh-keys $(PREFIX)/etc/pki/ovirt-engine

install_config:
	@echo "*** Deploying engine-config & engine-manage-domains"

	# Configuration files for the configuration tool:
	install -m 644 backend/manager/tools/engine-config/src/main/resources/engine-config.conf $(PREFIX)/etc/ovirt-engine/engine-config/
	install -m 644 backend/manager/tools/engine-config/src/main/resources/engine-config.*properties $(PREFIX)/etc/ovirt-engine/engine-config/
	install -m 644 backend/manager/tools/engine-config/src/main/resources/log4j.xml $(PREFIX)/etc/ovirt-engine/engine-config/

	# Main program for the configuration tool:
	install -m 750 backend/manager/tools/engine-config/src/main/resources/engine-config $(PREFIX)/usr/share/ovirt-engine/engine-config/
	ln -s /usr/share/ovirt-engine/engine-config/engine-config $(PREFIX)/usr/bin/engine-config

	# Configuration files for the domain management tool:
	install -m 644 backend/manager/modules/utils/src/main/resources/engine-manage-domains.conf $(PREFIX)/etc/ovirt-engine/engine-manage-domains/
	install -m 644 backend/manager/modules/utils/src/main/resources/engine-manage-domains/log4j.xml $(PREFIX)/etc/ovirt-engine/engine-manage-domains/

	# Main program for the domain management tool:
	install -m 750 backend/manager/conf/kerberos/engine-manage-domains $(PREFIX)/usr/share/ovirt-engine/engine-manage-domains/
	ln -s /usr/share/ovirt-engine/engine-manage-domains/engine-manage-domains $(PREFIX)/usr/bin/engine-manage-domains

install_sysprep:
	@echo "*** Deploying sysperp"
	install -m 644 backend/manager/conf/sysprep/* $(PREFIX)/etc/ovirt-engine/sysprep

install_notification_service:
	@echo "*** Deploying notification service"

	# Configuration files:
	install -m 644 backend/manager/tools/engine-notifier/engine-notifier-resources/src/main/resources/log4j.xml $(PREFIX)/etc/ovirt-engine/notifier/
	install -m 640 backend/manager/tools/engine-notifier/engine-notifier-resources/src/main/resources/notifier.conf $(PREFIX)/etc/ovirt-engine/notifier/

	# Main program:
	install -m 755 backend/manager/tools/engine-notifier/engine-notifier-resources/src/main/resources/notifier.sh $(PREFIX)/usr/share/ovirt-engine/notifier/
	install -m 755 backend/manager/tools/engine-notifier/engine-notifier-resources/src/main/resources/engine-notifierd $(PREFIX)/etc/rc.d/init.d/

install_db_scripts:
	@echo "*** Deploying Database scripts"
	cp -r backend/manager/dbscripts/* $(PREFIX)/usr/share/ovirt-engine/dbscripts
	find $(PREFIX)/usr/share/ovirt-engine/dbscripts -type d -exec chmod 755 {} \;
	find $(PREFIX)/usr/share/ovirt-engine/dbscripts -type f -name '*.sql' -exec chmod 644 {} \;
	find $(PREFIX)/usr/share/ovirt-engine/dbscripts -type f -name '*.sh' -exec chmod 755 {} \;

install_misc:
	@echo "*** Copying additional files"
	install -m 644 backend/manager/conf/jaas.conf $(PREFIX)/usr/share/ovirt-engine/conf
	install -m 640 backend/manager/conf/engine.conf $(PREFIX)/etc/ovirt-engine/
	install -m 644 backend/manager/conf/jboss-log4j.xml $(PREFIX)/usr/share/ovirt-engine/conf
	install -m 644 backend/manager/conf/kerberos/* $(PREFIX)/usr/share/ovirt-engine/kerberos
	# XXX: Does this script need execution permission? It
	# needs when copied to the host, but I am not sure it
	# should it have it in the manager machine.
	install -m 755 backend/manager/conf/vds_installer.py $(PREFIX)/usr/share/ovirt-engine/scripts/
	install -m 644 backend/manager/conf/jboss-log4j.xml $(PREFIX)/usr/share/ovirt-engine/conf
	install -m 755 packaging/resources/ovirtlogrot.sh ${PREFIX}/usr/share/ovirt-engine/scripts/
	install -m 755 packaging/resources/ovirt-cron ${PREFIX}/etc/cron.daily/
	install -m 644 packaging/resources/ovirt-tmpfilesd ${PREFIX}/etc/tmpfiles.d/ovirt-engine.conf

install_jboss_modules:
	@echo "*** Deploying JBoss modules"

	# Create the modules directory:
	install -dm 755 $(PREFIX)/usr/share/ovirt-engine/modules

	# PostgreSQL driver:
	install -dm 755 $(PREFIX)/usr/share/ovirt-engine/modules/org/postgresql/main
	install -m 644 deployment/modules/org/postgresql/main/module.xml $(PREFIX)/usr/share/ovirt-engine/modules/org/postgresql/main/.
	ln -s /usr/share/java/postgresql-jdbc.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/postgresql/main/.

install_service:
	@echo "*** Deploying service"

	# Install the files:
	install -m 644 packaging/fedora/engine-service.xml.in $(PREFIX)/usr/share/ovirt-engine/service
	install -m 644 packaging/fedora/engine-service-logging.properties $(PREFIX)/usr/share/ovirt-engine/service
	install -m 644 packaging/fedora/engine-service.sysconfig $(PREFIX)/etc/sysconfig/ovirt-engine
	install -m 644 packaging/fedora/engine-service.limits $(PREFIX)/etc/security/limits.d/10-ovirt-engine.conf
	install -m 755 packaging/fedora/engine-service.py $(PREFIX)/usr/share/ovirt-engine/service
	install -m 644 packaging/fedora/engine-service.systemd $(PREFIX)/usr/lib/systemd/system/ovirt-engine.service

	# Install the links:
	ln -s /usr/share/ovirt-engine/service/engine-service.py $(PREFIX)/usr/bin/engine-service

