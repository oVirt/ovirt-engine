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

include version.mak
# major, minor, seq
POM_VERSION:=$(shell cat pom.xml | grep '<engine.version>' | sed -e 's/.*>\(.*\)<.*/\1/' -e 's/-SNAPSHOT//')
# major, minor from pom and fix
APP_VERSION=$(shell echo $(POM_VERSION) | sed 's/\([^.]*\.[^.]\)\..*/\1/').$(FIX_RELEASE)
RPM_VERSION=$(APP_VERSION)
PACKAGE_VERSION=$(APP_VERSION)$(if $(MILESTONE),_$(MILESTONE))
PACKAGE_NAME=ovirt-engine
DISPLAY_VERSION=$(PACKAGE_VERSION)

BUILD_GWT=1
BUILD_LOCALES=0

MVN=mvn
EXTRA_BUILD_FLAGS=
BUILD_FLAGS:=
ifneq ($(BUILD_GWT),0)
BUILD_FLAGS:=$(BUILD_FLAGS) -P gwt-admin,gwt-user
endif
ifneq ($(BUILD_LOCALES),0)
BUILD_FLAGS:=$(BUILD_FLAGS) -P all-langs
endif
ENGINE_NAME=$(PACKAGE_NAME)
PREFIX=/usr/local
LOCALSTATE_DIR=$(PREFIX)/var
ENGINE_STATE=$(LOCALSTATE_DIR)/lib/$(ENGINE_NAME)
BIN_DIR=$(PREFIX)/bin
PID_DIR=$(LOCALSTATE_DIR)/run
SYSCONF_DIR=$(PREFIX)/etc
DATAROOT_DIR=$(PREFIX)/share
MAN_DIR=$(DATAROOT_DIR)/man
DATA_DIR=$(DATAROOT_DIR)/$(ENGINE_NAME)
MAVENPOM_DIR=$(DATAROOT_DIR)/maven-poms
JAVA_DIR=$(DATAROOT_DIR)/java
PKG_SYSCONF_DIR=$(SYSCONF_DIR)/$(ENGINE_NAME)
PKG_PKI_DIR=$(SYSCONF_DIR)/pki/$(ENGINE_NAME)
PKG_EAR_DIR=$(DATA_DIR)/engine.ear
PKG_JBOSS_MODULES=$(DATA_DIR)/modules
PKG_CACHE_DIR=$(LOCALSTATE_DIR)/cache/$(ENGINE_NAME)
PKG_LOG_DIR=$(LOCALSTATE_DIR)/log/$(ENGINE_NAME)
PKG_TMP_DIR=$(LOCALSTATE_DIR)/tmp/$(ENGINE_NAME)
PKG_USER=ovirt
PKG_GROUP=ovirt
RPMBUILD=rpmbuild
PYTHON=python
PYTHON_DIR:=$(shell $(PYTHON) -c "from distutils.sysconfig import get_python_lib as f;print(f())")

OUTPUT_RPMBUILD=$(shell pwd -P)/tmp.rpmbuild
OUTPUT_DIR=output
TARBALL=$(PACKAGE_NAME)-$(PACKAGE_VERSION).tar.gz
SRPM=$(OUTPUT_DIR)/$(PACKAGE_NAME)-$(RPM_VERSION)*.src.rpm
ARCH=noarch
BUILD_FILE=tmp.built
MAVEN_OUTPUT_DIR_DEFAULT=$(shell pwd -P)/tmp.repos
MAVEN_OUTPUT_DIR=$(MAVEN_OUTPUT_DIR_DEFAULT)

ARTIFACTS = \
	backend \
	bll \
	common \
	compat \
	dal \
	tools \
	interface-common-jaxrs \
	manager \
	manager-modules \
	restapi-definition \
	restapi-jaxrs \
	restapi-parent \
	restapi-types \
	root \
	scheduler \
	searchbackend \
	utils \
	vdsbroker \
	$(NULL)

# Don't use any of the bultin rules, in particular don't use the rule
# for .sh files, as that means that we can't generate .sh files from
# templates:
.SUFFIXES:
.SUFFIXES: .in

# Rule to generate files from templates:
.in:
	sed \
	-e "s|@ENGINE_DEFAULTS@|$(DATA_DIR)/conf/engine.conf.defaults|g" \
	-e "s|@ENGINE_VARS@|$(PKG_SYSCONF_DIR)/engine.conf|g" \
	-e "s|@ENGINE_USER@|$(PKG_USER)|g" \
	-e "s|@ENGINE_GROUP@|$(PKG_GROUP)|g" \
	-e "s|@ENGINE_ETC@|$(PKG_SYSCONF_DIR)|g" \
	-e "s|@ENGINE_PKI@|$(PKG_PKI_DIR)|g" \
	-e "s|@ENGINE_LOG@|$(PKG_LOG_DIR)|g" \
	-e "s|@ENGINE_TMP@|$(PKG_TMP_DIR)|g" \
	-e "s|@ENGINE_USR@|$(DATA_DIR)|g" \
	-e "s|@ENGINE_VAR@|$(ENGINE_STATE)|g" \
	-e "s|@ENGINE_CACHE@|$(PKG_CACHE_DIR)|g" \
	-e "s|@ENGINE_PID@|$(PID_DIR)/$(ENGINE_NAME).pid|g" \
	-e "s|@RPM_VERSION@|$(RPM_VERSION)|g" \
	-e "s|@RPM_RELEASE@|$(RPM_RELEASE)|g" \
	-e "s|@PACKAGE_NAME@|$(PACKAGE_NAME)|g" \
	-e "s|@PACKAGE_VERSION@|$(PACKAGE_VERSION)|g" \
	$< > $@

# List of files that will be generated from templates:
GENERATED = \
	backend/manager/conf/engine.conf.defaults \
	backend/manager/tools/src/main/shell/engine-prolog.sh \
	backend/manager/tools/src/main/conf/engine-config-log4j.xml \
	backend/manager/tools/src/main/conf/engine-manage-domains-log4j.xml \
	backend/manager/tools/src/main/conf/engine-notifier-log4j.xml \
	packaging/services/config.py \
	packaging/services/engine-service.systemd \
	packaging/services/engine-service.sysv \
	packaging/fedora/spec/ovirt-engine.spec \
	$(NULL)

all: \
	generated-files \
	$(BUILD_FILE) \
	$(NULL)

generated-files:	$(GENERATED)
	chmod a+x packaging/services/engine-service.sysv

$(BUILD_FILE):
	-[ "$(MAVEN_OUTPUT_DIR_DEFAULT)" = "$(MAVEN_OUTPUT_DIR)" ] && rm -fr "$(MAVEN_OUTPUT_DIR)"
	export MAVEN_OPTS="${MAVEN_OPTS} -XX:MaxPermSize=512m"
	$(MVN) \
		$(BUILD_FLAGS) \
		$(EXTRA_BUILD_FLAGS) \
		-D skipTests \
		-D altDeploymentRepository=install::default::file://$(MAVEN_OUTPUT_DIR) \
		deploy
	touch $(BUILD_FILE)

clean:
	# Clean maven generated stuff:
	$(MVN) clean $(EXTRA_BUILD_FLAGS)
	rm -rf $(OUTPUT_RPMBUILD) $(OUTPUT_DIR) $(BUILD_FILE)
	-[ "$(MAVEN_OUTPUT_DIR_DEFAULT)" = "$(MAVEN_OUTPUT_DIR)" ] && rm -fr "$(MAVEN_OUTPUT_DIR)"

	# Clean files generated from templates:
	rm -rf $(GENERATED)

test:
	$(MVN) install $(BUILD_FLAGS) $(EXTRA_BUILD_FLAGS)

install: \
	all \
	create_dirs \
	install_artifacts \
	install_config \
	install_sysprep \
	install_notification_service \
	install_db_scripts \
	install_setup \
	install_misc \
	install_sec \
	install_aio_plugin \
	install_jboss_modules \
	install_service \
	$(NULL)

packaging/fedora/spec/ovirt-engine.spec: version.mak

dist:	packaging/fedora/spec/ovirt-engine.spec
	git ls-files | tar --files-from /proc/self/fd/0 -czf $(TARBALL) packaging/fedora/spec/ovirt-engine.spec
	@echo
	@echo You can use $(RPMBUILD) -tb $(TARBALL) to produce rpms
	@echo

# legacy
tarball:	dist

srpm:	dist
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
	$(RPMBUILD) --define="_topdir $(OUTPUT_RPMBUILD)" $(RPMBUILD_EXTRA_ARGS) --rebuild $(SRPM)
	mv $(OUTPUT_RPMBUILD)/RPMS/$(ARCH)/*.rpm $(OUTPUT_DIR)
	rm -rf $(OUTPUT_RPMBUILD)
	@echo
	@echo rpms are ready at $(OUTPUT_DIR)
	@echo

# This is intended to quickly build a set of RPMs that don't
# contain working copies of the GWT applications, mostly useful
# for testing the RPM build process itself or for testing only the
# backend and the RESTAPI
rpm-quick:
	$(MAKE) \
		rpm \
		RPMBUILD_EXTRA_ARGS='--define="ovirt_build_quick 1"'
	@echo
	@echo WARNING:
	@echo rpms produces from quick are partial!
	@echo *DO NOT* use them for any other use but debug.
	@echo

create_dirs:
	@echo "*** Creating Directories"
	@install -dm 755 $(DESTDIR)$(BIN_DIR)
	@install -dm 755 $(DESTDIR)$(DATA_DIR)/bin
	@install -dm 755 $(DESTDIR)$(DATA_DIR)/ui-plugins
	@install -dm 755 $(DESTDIR)$(DATA_DIR)/conf
	@install -dm 755 $(DESTDIR)$(DATA_DIR)/db-backups
	@install -dm 755 $(DESTDIR)$(DATA_DIR)/ovirt-isos
	@install -dm 755 $(DESTDIR)$(DATA_DIR)/scripts/plugins
	@install -dm 755 $(DESTDIR)$(DATA_DIR)/scripts/dbutils
	@install -dm 755 $(DESTDIR)$(MAN_DIR)/man8
	@install -dm 755 $(DESTDIR)$(PYTHON_DIR)/sos/plugins
	@install -dm 755 $(DESTDIR)$(PKG_SYSCONF_DIR)/engine-config
	@install -dm 755 $(DESTDIR)$(PKG_SYSCONF_DIR)/engine-manage-domains
	@install -dm 755 $(DESTDIR)$(SYSCONF_DIR)/cron.daily
	@install -dm 755 $(DESTDIR)$(SYSCONF_DIR)/rc.d/init.d
	@install -dm 755 $(DESTDIR)$(SYSCONF_DIR)/firewalld/services

install_artifacts:
	@echo "*** Deploying EAR to $(DESTDIR)"
	install -dm 755 $(DESTDIR)$(PKG_EAR_DIR)
	install -dm 755 $(DESTDIR)$(MAVENPOM_DIR)

	X=`find "$(MAVEN_OUTPUT_DIR)" -name 'engine-server-ear-$(POM_VERSION)*'.ear` && unzip -o "$$X" -d "$(DESTDIR)$(PKG_EAR_DIR)"

	for artifact_id in  $(ARTIFACTS); do \
		POM=`find "$(MAVEN_OUTPUT_DIR)" -name "$${artifact_id}-$(POM_VERSION)*.pom"`; \
		if ! [ -f "$${POM}" ]; then \
			echo "ERROR: Cannot find artifact $${artifact_id}"; \
			exit 1; \
		fi; \
		JAR=`echo "$${POM}" | sed 's/\.pom/.jar/'`; \
		install -p -m 644 "$${POM}" "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-$${artifact_id}.pom"; \
	done

	sed -i "s/MYVERSION/$(DISPLAY_VERSION)/" $(DESTDIR)$(PKG_EAR_DIR)/root.war/engineVersion.js

install_setup:
	@echo "*** Deploying setup executables"

	# Configuration files:
	install -m 644 packaging/fedora/setup/engine-config-install.properties $(DESTDIR)$(DATA_DIR)/conf
	install -m 644 packaging/fedora/setup/iptables.default $(DESTDIR)$(DATA_DIR)/conf
	install -m 644 packaging/fedora/setup/firewalld.ovirt.xml $(DESTDIR)$(SYSCONF_DIR)/firewalld/services/ovirt.xml
	install -m 644 packaging/fedora/setup/nfs.sysconfig $(DESTDIR)$(DATA_DIR)/conf
	install -m 644 packaging/fedora/setup/ovirt-engine-proxy.conf.in $(DESTDIR)$(DATA_DIR)/conf
	install -m 644 packaging/fedora/setup/ovirt-engine-root-redirect.conf.in $(DESTDIR)$(DATA_DIR)/conf

	# Shared python modules:
	install -m 644 packaging/fedora/setup/nfsutils.py $(DESTDIR)$(DATA_DIR)/scripts
	install -m 644 packaging/fedora/setup/basedefs.py $(DESTDIR)$(DATA_DIR)/scripts
	install -m 644 packaging/fedora/setup/engine_validators.py $(DESTDIR)$(DATA_DIR)/scripts
	install -m 644 packaging/fedora/setup/engine_firewalld.py $(DESTDIR)$(DATA_DIR)/scripts
	install -m 644 packaging/fedora/setup/setup_params.py $(DESTDIR)$(DATA_DIR)/scripts
	install -m 644 packaging/fedora/setup/setup_sequences.py $(DESTDIR)$(DATA_DIR)/scripts
	install -m 644 packaging/fedora/setup/setup_controller.py $(DESTDIR)$(DATA_DIR)/scripts
	install -m 644 packaging/fedora/setup/common_utils.py $(DESTDIR)$(DATA_DIR)/scripts
	install -m 644 packaging/fedora/setup/miniyum.py $(DESTDIR)$(DATA_DIR)/scripts
	install -m 644 packaging/fedora/setup/output_messages.py $(DESTDIR)$(DATA_DIR)/scripts
	install -m 644 packaging/fedora/setup/post_upgrade.py $(DESTDIR)$(DATA_DIR)/scripts
	install -m 644 packaging/fedora/setup/add_fn_db_get_async_tasks_function.sql $(DESTDIR)$(DATA_DIR)/scripts

	# Install man pages
	install -m 644 packaging/engine-setup.8 $(DESTDIR)$(MAN_DIR)/man8/
	install -m 644 packaging/engine-upgrade.8 $(DESTDIR)$(MAN_DIR)/man8/

	# Example Plugin:
	install -m 644 packaging/fedora/setup/plugins/example_plugin_000.py $(DESTDIR)$(DATA_DIR)/scripts/plugins

	# Main programs and links:
	install -m 755 packaging/fedora/setup/engine-setup.py $(DESTDIR)$(DATA_DIR)/scripts
	ln -sf $(DATA_DIR)/scripts/engine-setup.py $(DESTDIR)$(BIN_DIR)/engine-setup
	install -m 755 packaging/fedora/setup/engine-cleanup.py $(DESTDIR)$(DATA_DIR)/scripts
	ln -sf $(DATA_DIR)/scripts/engine-cleanup.py $(DESTDIR)$(BIN_DIR)/engine-cleanup
	install -m 755 packaging/fedora/setup/engine-upgrade.py $(DESTDIR)$(DATA_DIR)/scripts
	ln -sf $(DATA_DIR)/scripts/engine-upgrade.py $(DESTDIR)$(BIN_DIR)/engine-upgrade
	install -m 755 packaging/fedora/setup/engine-check-update $(DESTDIR)$(BIN_DIR)/

	# Backups folder
	install -dm 750 $(DESTDIR)$(ENGINE_STATE)/backups

	# Task cleaner
	install -m 750 backend/manager/tools/dbutils/common.sh $(DESTDIR)$(DATA_DIR)/scripts/dbutils
	install -m 750 backend/manager/tools/dbutils/taskcleaner.sh $(DESTDIR)$(DATA_DIR)/scripts/dbutils
	install -m 640 backend/manager/tools/dbutils/taskcleaner_sp.sql $(DESTDIR)$(DATA_DIR)/scripts/dbutils
	install -m 750 backend/manager/tools/dbutils/fkvalidator.sh $(DESTDIR)$(DATA_DIR)/scripts/dbutils
	install -m 640 backend/manager/tools/dbutils/fkvalidator_sp.sql $(DESTDIR)$(DATA_DIR)/scripts/dbutils

install_aio_plugin:
	install -m 644 packaging/fedora/setup/plugins/all_in_one_100.py $(DESTDIR)$(DATA_DIR)/scripts/plugins

install_sec:
	install -dm 755 $(DESTDIR)$(PKG_PKI_DIR)/certs
	install -dm 755 $(DESTDIR)$(PKG_PKI_DIR)/keys
	install -dm 750 $(DESTDIR)$(PKG_PKI_DIR)/private
	install -dm 755 $(DESTDIR)$(PKG_PKI_DIR)/requests

	# Configuration files:
	install -m 644 backend/manager/conf/ca/openssl.conf $(DESTDIR)$(PKG_PKI_DIR)
	install -m 644 backend/manager/conf/ca/cacert.template.in $(DESTDIR)$(PKG_PKI_DIR)
	install -m 644 backend/manager/conf/ca/cert.template.in $(DESTDIR)$(PKG_PKI_DIR)

	# Scripts:
	install -m 755 backend/manager/conf/ca/*.sh $(DESTDIR)$(PKG_PKI_DIR)
	install -m 644 backend/manager/conf/ca/*.lock $(DESTDIR)$(PKG_PKI_DIR)

install_config:
	@echo "*** Deploying engine-config & engine-manage-domains"

	# Configuration files for the configuration tool:
	install -m 644 backend/manager/tools/src/main/conf/engine-config.conf $(DESTDIR)$(PKG_SYSCONF_DIR)/engine-config/
	install -m 644 backend/manager/tools/src/main/conf/engine-config*.properties $(DESTDIR)$(PKG_SYSCONF_DIR)/engine-config/
	install -m 644 backend/manager/tools/src/main/conf/engine-config-log4j.xml $(DESTDIR)$(PKG_SYSCONF_DIR)/engine-config/log4j.xml

	# Main program for the configuration tool:
	install -m 750 backend/manager/tools/src/main/shell/engine-config.sh $(DESTDIR)$(DATA_DIR)/bin
	ln -sf $(DATA_DIR)/bin/engine-config.sh $(DESTDIR)$(BIN_DIR)/engine-config

	# Configuration files for the domain management tool:
	install -m 644 backend/manager/tools/src/main/conf/engine-manage-domains.conf $(DESTDIR)$(PKG_SYSCONF_DIR)/engine-manage-domains/
	install -m 644 backend/manager/tools/src/main/conf/engine-manage-domains-log4j.xml $(DESTDIR)$(PKG_SYSCONF_DIR)/engine-manage-domains/log4j.xml

	# Main program for the domain management tool:
	install -m 750 backend/manager/tools/src/main/shell/engine-manage-domains.sh $(DESTDIR)$(DATA_DIR)/bin
	ln -sf $(DATA_DIR)/bin/engine-manage-domains.sh $(DESTDIR)$(BIN_DIR)/engine-manage-domains

	# Script to encrypt passwords:
	install -m 750 backend/manager/tools/src/main/shell/engine-encrypt-passwd.sh $(DESTDIR)$(DATA_DIR)/bin

	# Install man pages
	install -m 644 packaging/engine-manage-domains.8 $(DESTDIR)$(MAN_DIR)/man8/

install_sysprep:
	@echo "*** Deploying sysperp"
	@install -dm 755 $(DESTDIR)$(PKG_SYSCONF_DIR)/sysprep
	install -m 644 backend/manager/conf/sysprep/* $(DESTDIR)$(PKG_SYSCONF_DIR)/sysprep

install_notification_service:
	@echo "*** Deploying notification service"

	install -dm 755 $(DESTDIR)$(PKG_SYSCONF_DIR)/notifier

	# Configuration files:
	install -m 644 backend/manager/tools/src/main/conf/engine-notifier-log4j.xml $(DESTDIR)$(PKG_SYSCONF_DIR)/notifier/log4j.xml
	install -m 640 backend/manager/tools/src/main/conf/engine-notifier.conf $(DESTDIR)$(PKG_SYSCONF_DIR)/notifier/notifier.conf

	# Main program:
	install -m 755 backend/manager/tools/src/main/shell/engine-notifier.sh $(DESTDIR)$(DATA_DIR)/bin/engine-notifier.sh
	install -m 755 backend/manager/tools/src/main/shell/engine-notifier-service.sh $(DESTDIR)$(SYSCONF_DIR)/rc.d/init.d/engine-notifierd

install_db_scripts:
	@echo "*** Deploying Database scripts"
	install -dm 755 $(DESTDIR)$(DATA_DIR)/dbscripts
	cp -r backend/manager/dbscripts/* $(DESTDIR)$(DATA_DIR)/dbscripts
	find $(DESTDIR)$(DATA_DIR)/dbscripts -type d -exec chmod 755 {} \;
	find $(DESTDIR)$(DATA_DIR)/dbscripts -type f -name '*.sql' -exec chmod 644 {} \;
	find $(DESTDIR)$(DATA_DIR)/dbscripts -type f -name '*.sh' -exec chmod 755 {} \;

install_misc:
	@echo "*** Copying additional files"

	# Shell scripts used by several programs:
	install -m 755 backend/manager/tools/src/main/shell/engine-prolog.sh $(DESTDIR)$(DATA_DIR)/bin

	# Other misc things:
	install -m 644 backend/manager/conf/jaas.conf $(DESTDIR)$(DATA_DIR)/conf
	install -m 644 backend/manager/conf/engine.conf.defaults $(DESTDIR)$(DATA_DIR)/conf
	install -m 644 backend/manager/conf/engine.conf $(DESTDIR)$(PKG_SYSCONF_DIR)/
	install -dm 755 $(DESTDIR)$(PKG_SYSCONF_DIR)/engine.conf.d
	install -m 755 packaging/resources/ovirtlogrot.sh ${DESTDIR}$(DATA_DIR)/scripts/
	install -m 755 packaging/resources/ovirt-cron ${DESTDIR}$(SYSCONF_DIR)/cron.daily/

	# USB filter:
	install -m 644 frontend/usbfilter.txt $(DESTDIR)$(PKG_SYSCONF_DIR)

	# Create a version file
	echo $(DISPLAY_VERSION) > $(DESTDIR)$(DATA_DIR)/conf/version

install_jboss_modules:
	@echo "*** Deploying JBoss modules"

	# Uncompress and install the contents of the modules archives to
	# the directory containing engine modules:
	find "$(MAVEN_OUTPUT_DIR)" -name "*-$(POM_VERSION)*-modules.zip" -exec unzip -o {} -d "$(DESTDIR)$(PKG_JBOSS_MODULES)" \;

install_service:
	@echo "*** Deploying service"

	# Install the files:
	install -dm 755 $(DESTDIR)$(DATA_DIR)/services
	install -m 644 packaging/services/__init__.py $(DESTDIR)$(DATA_DIR)/services
	install -m 644 packaging/services/config.py $(DESTDIR)$(DATA_DIR)/services
	install -m 644 packaging/services/service.py $(DESTDIR)$(DATA_DIR)/services
	install -m 644 packaging/services/engine-service.xml.in $(DESTDIR)$(DATA_DIR)/services
	install -m 644 packaging/services/engine-service-logging.properties.in $(DESTDIR)$(DATA_DIR)/services
	install -m 755 packaging/services/engine-service.py $(DESTDIR)$(DATA_DIR)/services
