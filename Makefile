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

#
# CUSTOMIZATION-BEGIN
#
BUILD_GWT=1
BUILD_GWT_USERPORTAL=1
BUILD_GWT_WEBADMIN=1
BUILD_LOCALES=0
BUILD_UT=0
EXTRA_BUILD_FLAGS=
DEV_REBUILD=1
DEV_BUILD_GWT_DRAFT=0
DEV_EXTRA_BUILD_FLAGS=
DEV_EXTRA_BUILD_FLAGS_GWT_DEFAULTS=-D gwt.userAgent=gecko1_8

PACKAGE_NAME=ovirt-engine
ENGINE_NAME=$(PACKAGE_NAME)
MVN=mvn
RPMBUILD=rpmbuild
PYTHON=python
PREFIX=/usr/local
LOCALSTATE_DIR=$(PREFIX)/var
ENGINE_STATE=$(LOCALSTATE_DIR)/lib/$(ENGINE_NAME)
BIN_DIR=$(PREFIX)/bin
PID_DIR=$(LOCALSTATE_DIR)/run
SYSCONF_DIR=$(PREFIX)/etc
DATAROOT_DIR=$(PREFIX)/share
MAN_DIR=$(DATAROOT_DIR)/man
DOC_DIR=$(DATEROOT_DIR)/doc
DATA_DIR=$(DATAROOT_DIR)/$(ENGINE_NAME)
MAVENPOM_DIR=$(DATAROOT_DIR)/maven-poms
JAVA_DIR=$(DATAROOT_DIR)/java
PKG_SYSCONF_DIR=$(SYSCONF_DIR)/$(ENGINE_NAME)
PKG_PKI_DIR=$(SYSCONF_DIR)/pki/$(ENGINE_NAME)
PKG_DOC_DIR=$(DOC_DIR)/$(ENGINE_NAME)
PKG_EAR_DIR=$(DATA_DIR)/engine.ear
PKG_JBOSS_MODULES=$(DATA_DIR)/modules
PKG_CACHE_DIR=$(LOCALSTATE_DIR)/cache/$(ENGINE_NAME)
PKG_LOG_DIR=$(LOCALSTATE_DIR)/log/$(ENGINE_NAME)
PKG_TMP_DIR=$(LOCALSTATE_DIR)/tmp/$(ENGINE_NAME)
SPICE_DIR=/usr/share/spice
JBOSS_HOME=/usr/share/jboss-as
PYTHON_DIR=$(PYTHON_SYS_DIR)
PKG_USER=ovirt
PKG_GROUP=ovirt
#
# CUSTOMIZATION-END
#

include version.mak
# major, minor, seq
POM_VERSION:=$(shell cat pom.xml | grep '<engine.version>' | sed -e 's/.*>\(.*\)<.*/\1/' -e 's/-SNAPSHOT//')
# major, minor from pom and fix
APP_VERSION=$(shell echo $(POM_VERSION) | sed 's/\([^.]*\.[^.]\)\..*/\1/').$(FIX_RELEASE)
RPM_VERSION=$(APP_VERSION)
PACKAGE_VERSION=$(APP_VERSION)$(if $(MILESTONE),_$(MILESTONE))
DISPLAY_VERSION=$(PACKAGE_VERSION)

BUILD_FLAGS:=
ifneq ($(BUILD_GWT),0)
ifneq ($(BUILD_GWT_USERPORTAL),0)
BUILD_FLAGS:=$(BUILD_FLAGS) -P gwt-user
endif
ifneq ($(BUILD_GWT_WEBADMIN),0)
BUILD_FLAGS:=$(BUILD_FLAGS) -P gwt-admin
endif
endif
ifneq ($(BUILD_LOCALES),0)
BUILD_FLAGS:=$(BUILD_FLAGS) -P all-langs
endif
ifeq ($(BUILD_UT),0)
BUILD_FLAGS:=$(BUILD_FLAGS) -D skipTests
endif
BUILD_FLAGS:=$(BUILD_FLAGS) $(EXTRA_BUILD_FLAGS)

DEV_BUILD_FLAGS:=
ifneq ($(DEV_BUILD_GWT_DRAFT),0)
DEV_BUILD_FLAGS:=$(DEV_BUILD_FLAGS) -P gwtdraft
endif
DEV_BUILD_FLAGS:=$(DEV_BUILD_FLAGS) $(DEV_EXTRA_BUILD_FLAGS_GWT_DEFAULTS)
DEV_BUILD_FLAGS:=$(DEV_BUILD_FLAGS) $(DEV_EXTRA_BUILD_FLAGS)

PYTHON_SYS_DIR:=$(shell $(PYTHON) -c "from distutils.sysconfig import get_python_lib as f;print(f())")
OUTPUT_RPMBUILD=$(shell pwd -P)/tmp.rpmbuild
OUTPUT_DIR=output
TARBALL=$(PACKAGE_NAME)-$(PACKAGE_VERSION).tar.gz
ARCH=noarch
BUILD_FILE=tmp.built
MAVEN_OUTPUT_DIR=.
BUILD_TARGET=install

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
	-e "s|@ENGINE_NOTIFIER_DEFAULTS@|$(DATA_DIR)/conf/notifier.conf.defaults|g" \
	-e "s|@ENGINE_NOTIFIER_VARS@|$(PKG_SYSCONF_DIR)/notifier/notifier.conf|g" \
	-e "s|@ENGINE_WSPROXY_DEFAULT_FILE@|$(DATA_DIR)/conf/ovirt-websocket-proxy.conf.defaults|g" \
	-e "s|@ENGINE_WSPROXY_VARS@|$(PKG_SYSCONF_DIR)/ovirt-websocket-proxy.conf|g" \
	-e "s|@ENGINE_USER@|$(PKG_USER)|g" \
	-e "s|@ENGINE_GROUP@|$(PKG_GROUP)|g" \
	-e "s|@ENGINE_ETC@|$(PKG_SYSCONF_DIR)|g" \
	-e "s|@ENGINE_PKI@|$(PKG_PKI_DIR)|g" \
	-e "s|@ENGINE_LOG@|$(PKG_LOG_DIR)|g" \
	-e "s|@ENGINE_TMP@|$(PKG_TMP_DIR)|g" \
	-e "s|@ENGINE_USR@|$(DATA_DIR)|g" \
	-e "s|@ENGINE_DOC@|$(PKG_DOC_DIR)|g" \
	-e "s|@ENGINE_VAR@|$(ENGINE_STATE)|g" \
	-e "s|@ENGINE_CACHE@|$(PKG_CACHE_DIR)|g" \
	-e "s|@ENGINE_PID@|$(PID_DIR)/$(ENGINE_NAME).pid|g" \
	-e "s|@RPM_VERSION@|$(RPM_VERSION)|g" \
	-e "s|@RPM_RELEASE@|$(RPM_RELEASE)|g" \
	-e "s|@PACKAGE_NAME@|$(PACKAGE_NAME)|g" \
	-e "s|@PACKAGE_VERSION@|$(PACKAGE_VERSION)|g" \
	-e "s|@DISPLAY_VERSION@|$(DISPLAY_VERSION)|g" \
	-e "s|@SPICE_DIR@|$(SPICE_DIR)|g" \
	-e "s|@JBOSS_HOME@|$(JBOSS_HOME)|g" \
	$< > $@

# List of files that will be generated from templates:
GENERATED = \
	packaging/bin/engine-prolog.sh \
	packaging/bin/pki-common.sh \
	packaging/conf/engine.conf.defaults \
	packaging/conf/notifier.conf.defaults \
	packaging/conf/ovirt-websocket-proxy.conf.defaults \
	packaging/etc/engine-config/log4j.xml \
	packaging/etc/engine-manage-domains/log4j.xml \
	packaging/etc/engine-manage-domains/engine-manage-domains.conf \
	packaging/etc/notifier/log4j.xml \
	packaging/sys-etc/logrotate.d/ovirt-engine \
	packaging/services/config.py \
	packaging/services/ovirt-engine.systemd \
	packaging/services/ovirt-engine.sysv \
	packaging/services/ovirt-engine-notifier.systemd \
	packaging/services/ovirt-engine-notifier.sysv \
	packaging/services/ovirt-websocket-proxy.systemd \
	packaging/services/ovirt-websocket-proxy.sysv \
	packaging/setup/ovirt_engine_setup/config.py \
	packaging/bin/ovirt-engine-log-setup-event.sh \
	ovirt-engine.spec \
	$(NULL)

all: \
	generated-files \
	$(BUILD_FILE) \
	$(NULL)

generated-files:	$(GENERATED)
	chmod a+x packaging/services/ovirt-engine.sysv
	chmod a+x packaging/services/ovirt-engine-notifier.sysv
	chmod a+x packaging/services/ovirt-websocket-proxy.sysv

# support force run of maven
maven:
	export MAVEN_OPTS="${MAVEN_OPTS} -XX:MaxPermSize=512m"
	$(MVN) \
		$(BUILD_FLAGS) \
		$(BUILD_TARGET)
	touch "$(BUILD_FILE)"

$(BUILD_FILE):
	$(MAKE) maven

clean:
	# Clean maven generated stuff:
	$(MVN) clean $(EXTRA_BUILD_FLAGS)
	rm -rf $(OUTPUT_RPMBUILD) $(OUTPUT_DIR) $(BUILD_FILE)

	# Clean files generated from templates:
	rm -rf $(GENERATED)

test:
	$(MVN) install $(BUILD_FLAGS) $(EXTRA_BUILD_FLAGS)

install: \
	all \
	create_dirs \
	install_artifacts \
	install_poms \
	install_config \
	install_sysprep \
	install_db_scripts \
	install_setup \
	install_misc \
	install_sec \
	install_aio_plugin \
	install_service \
	install_notification_service \
	$(NULL)

ovirt-engine.spec: version.mak

dist:	ovirt-engine.spec
	git ls-files | tar --files-from /proc/self/fd/0 -czf "$(TARBALL)" ovirt-engine.spec
	@echo
	@echo You can use $(RPMBUILD) -tb $(TARBALL) to produce rpms
	@echo

# legacy
tarball:	dist

srpm:	dist
	rm -rf "$(OUTPUT_RPMBUILD)"
	mkdir -p "$(OUTPUT_RPMBUILD)"/{SPECS,RPMS,SRPMS,SOURCES,BUILD,BUILDROOT}
	mkdir -p "$(OUTPUT_DIR)"
	$(RPMBUILD) -ts --define="_topdir $(OUTPUT_RPMBUILD)" "$(TARBALL)"
	mv "$(OUTPUT_RPMBUILD)/SRPMS"/*.rpm "$(OUTPUT_DIR)"
	rm -rf "$(OUTPUT_RPMBUILD)"
	@echo
	@echo srpm is ready at $(OUTPUT_DIR)
	@echo

rpm:	srpm
	rm -rf "$(OUTPUT_RPMBUILD)"
	mkdir -p "$(OUTPUT_RPMBUILD)"/{SPECS,RPMS,SRPMS,SOURCES,BUILD,BUILDROOT}
	mkdir -p "$(OUTPUT_DIR)"
	$(RPMBUILD) --define="_topdir $(OUTPUT_RPMBUILD)" $(RPMBUILD_EXTRA_ARGS) --rebuild "$(OUTPUT_DIR)/$(PACKAGE_NAME)-$(RPM_VERSION)"*.src.rpm
	mv $(OUTPUT_RPMBUILD)/RPMS/$(ARCH)/*.rpm "$(OUTPUT_DIR)"
	rm -rf "$(OUTPUT_RPMBUILD)"
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
	@install -dm 755 "$(DESTDIR)$(BIN_DIR)"
	@install -dm 755 "$(DESTDIR)$(DATA_DIR)/bin"
	@install -dm 755 "$(DESTDIR)$(DATA_DIR)/ui-plugins"
	@install -dm 755 "$(DESTDIR)$(DATA_DIR)/conf"
	@install -dm 755 "$(DESTDIR)$(DATA_DIR)/db-backups"
	@install -dm 755 "$(DESTDIR)$(DATA_DIR)/scripts/plugins"
	@install -dm 755 "$(DESTDIR)$(DATA_DIR)/scripts/dbutils"
	@install -dm 755 "$(DESTDIR)$(DATA_DIR)/firewalld/base"
	@install -dm 755 "$(DESTDIR)$(MAN_DIR)/man8"
	@install -dm 755 "$(DESTDIR)$(PKG_SYSCONF_DIR)/engine-config"
	@install -dm 755 "$(DESTDIR)$(PKG_SYSCONF_DIR)/engine-manage-domains"

install_artifacts:
	# we must exclude tmp.repos directory so we
	# won't get artifacts of older branches
	# we should use search MAVEN_OUTPUT_DIR as it may contain
	# pre-compiled artifacts at different hierarchy.
	find "$(MAVEN_OUTPUT_DIR)" -name '*-modules.zip' | grep -v tmp.repos | xargs -n 1 unzip -q -o -d "$(DESTDIR)$(PKG_JBOSS_MODULES)"
	find "$(MAVEN_OUTPUT_DIR)" -name '*.ear' -type f | grep -v tmp.repos | xargs -n 1 unzip -q -o -d "$(DESTDIR)$(PKG_EAR_DIR)"

	# TODO: remove some day
	sed -i "s/MYVERSION/$(DISPLAY_VERSION)/" "$(DESTDIR)$(PKG_EAR_DIR)/root.war/engineVersion.js"

install_poms:
	install -dm 755 "$(DESTDIR)$(MAVENPOM_DIR)"
	install -m 644 backend/manager/modules/bll/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-bll.pom"
	install -m 644 backend/manager/modules/common/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-common.pom"
	install -m 644 backend/manager/modules/compat/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-compat.pom"
	install -m 644 backend/manager/modules/dal/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-dal.pom"
	install -m 644 backend/manager/modules/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-manager-modules.pom"
	install -m 644 backend/manager/modules/restapi/interface/common/jaxrs/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-interface-common-jaxrs.pom"
	install -m 644 backend/manager/modules/restapi/interface/definition/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-restapi-definition.pom"
	install -m 644 backend/manager/modules/restapi/jaxrs/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-restapi-jaxrs.pom"
	install -m 644 backend/manager/modules/restapi/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-restapi-parent.pom"
	install -m 644 backend/manager/modules/restapi/types/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-restapi-types.pom"
	install -m 644 backend/manager/modules/scheduler/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-scheduler.pom"
	install -m 644 backend/manager/modules/searchbackend/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-searchbackend.pom"
	install -m 644 backend/manager/modules/utils/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-utils.pom"
	install -m 644 backend/manager/modules/vdsbroker/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-vdsbroker.pom"
	install -m 644 backend/manager/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-manager.pom"
	install -m 644 backend/manager/tools/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-tools.pom"
	install -m 644 backend/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-backend.pom"
	install -m 644 pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-root.pom"

install_setup:
	@echo "*** Deploying setup executables"

	# Configuration files:
	install -m 644 packaging/conf/engine-config-install.properties "$(DESTDIR)$(DATA_DIR)/conf"
	install -m 644 packaging/conf/iptables.default.in "$(DESTDIR)$(DATA_DIR)/conf"
	#FirewallD
	install -m 644 packaging/firewalld/base/ovirt-nfs.xml.in "$(DESTDIR)$(DATA_DIR)/firewalld/base/ovirt-nfs.xml.in"
	install -m 644 packaging/firewalld/base/ovirt-http.xml.in "$(DESTDIR)$(DATA_DIR)/firewalld/base/ovirt-http.xml.in"
	install -m 644 packaging/firewalld/base/ovirt-https.xml.in "$(DESTDIR)$(DATA_DIR)/firewalld/base/ovirt-https.xml.in"

	install -m 644 packaging/conf/nfs.sysconfig.in "$(DESTDIR)$(DATA_DIR)/conf"
	install -m 644 packaging/conf/ovirt-engine-proxy.conf.v2.in "$(DESTDIR)$(DATA_DIR)/conf"
	install -m 644 packaging/conf/ovirt-engine-root-redirect.conf.in "$(DESTDIR)$(DATA_DIR)/conf"

	# Shared python modules:
	install -m 644 packaging/fedora/setup/nfsutils.py "$(DESTDIR)$(DATA_DIR)/scripts"
	install -m 644 packaging/fedora/setup/basedefs.py "$(DESTDIR)$(DATA_DIR)/scripts"
	install -m 644 packaging/fedora/setup/engine_validators.py "$(DESTDIR)$(DATA_DIR)/scripts"
	install -m 644 packaging/fedora/setup/engine_firewalld.py "$(DESTDIR)$(DATA_DIR)/scripts"
	install -m 644 packaging/fedora/setup/setup_params.py "$(DESTDIR)$(DATA_DIR)/scripts"
	install -m 644 packaging/fedora/setup/setup_sequences.py "$(DESTDIR)$(DATA_DIR)/scripts"
	install -m 644 packaging/fedora/setup/setup_controller.py "$(DESTDIR)$(DATA_DIR)/scripts"
	install -m 644 packaging/fedora/setup/common_utils.py "$(DESTDIR)$(DATA_DIR)/scripts"
	install -m 644 packaging/fedora/setup/miniyum.py "$(DESTDIR)$(DATA_DIR)/scripts"
	install -m 644 packaging/fedora/setup/output_messages.py "$(DESTDIR)$(DATA_DIR)/scripts"
	install -m 644 packaging/fedora/setup/post_upgrade.py "$(DESTDIR)$(DATA_DIR)/scripts"

	# Install man pages
	install -m 644 packaging/man/man8/engine-setup.8 "$(DESTDIR)$(MAN_DIR)/man8"
	install -m 644 packaging/man/man8/engine-upgrade.8 "$(DESTDIR)$(MAN_DIR)/man8"

	# Example Plugin:
	install -m 644 packaging/fedora/setup/plugins/example_plugin_000.py "$(DESTDIR)$(DATA_DIR)/scripts/plugins"

	# Main programs and links:
	install -m 755 packaging/fedora/setup/engine-setup.py "$(DESTDIR)$(DATA_DIR)/scripts"
	ln -sf $(DATA_DIR)/scripts/engine-setup.py "$(DESTDIR)$(BIN_DIR)/engine-setup"
	install -m 755 packaging/fedora/setup/engine-cleanup.py "$(DESTDIR)$(DATA_DIR)/scripts"
	ln -sf $(DATA_DIR)/scripts/engine-cleanup.py "$(DESTDIR)$(BIN_DIR)/engine-cleanup"
	install -m 755 packaging/fedora/setup/engine-upgrade.py "$(DESTDIR)$(DATA_DIR)/scripts"
	ln -sf $(DATA_DIR)/scripts/engine-upgrade.py "$(DESTDIR)$(BIN_DIR)/engine-upgrade"
	install -m 755 packaging/fedora/setup/engine-check-update "$(DESTDIR)$(BIN_DIR)"

	# Backups folder
	install -dm 750 "$(DESTDIR)$(ENGINE_STATE)/backups"

	# Task cleaner
	install -dm 750 "$(DESTDIR)$(DATA_DIR)/scripts/dbutils"
	install -m 750 backend/manager/tools/dbutils/common.sh "$(DESTDIR)$(DATA_DIR)/scripts/dbutils"
	install -m 750 backend/manager/tools/dbutils/taskcleaner.sh "$(DESTDIR)$(DATA_DIR)/scripts/dbutils"
	install -m 640 backend/manager/tools/dbutils/taskcleaner_sp.sql "$(DESTDIR)$(DATA_DIR)/scripts/dbutils"
	install -m 750 backend/manager/tools/dbutils/fkvalidator.sh "$(DESTDIR)$(DATA_DIR)/scripts/dbutils"
	install -m 640 backend/manager/tools/dbutils/fkvalidator_sp.sql "$(DESTDIR)$(DATA_DIR)/scripts/dbutils"
	install -m 750 backend/manager/tools/dbutils/validatedb.sh "$(DESTDIR)$(DATA_DIR)/scripts/dbutils"

	( cd packaging && find setup -name '*.py' ) | while read f; do \
		install -dm 755 "$$(dirname "$(DESTDIR)$(DATA_DIR)/$$f")"; \
		install -m 644 "packaging/$$f" "$(DESTDIR)$(DATA_DIR)/$$f"; \
	done
	install -dm 755 "$(DESTDIR)$(DATA_DIR)/setup/bin"
	install -m 755 packaging/setup/bin/ovirt-engine-setup "$(DESTDIR)$(DATA_DIR)/setup/bin"
	install -m 644 packaging/setup/bin/ovirt-engine-setup.env "$(DESTDIR)$(DATA_DIR)/setup/bin"
	install -m 755 packaging/setup/bin/ovirt-engine-remove "$(DESTDIR)$(DATA_DIR)/setup/bin"
	ln -sf "$(DATA_DIR)/setup/bin/ovirt-engine-setup" "$(DESTDIR)$(BIN_DIR)/engine-setup-2"
	ln -sf "$(DATA_DIR)/setup/bin/ovirt-engine-remove" "$(DESTDIR)$(BIN_DIR)/engine-cleanup-2"

install_aio_plugin:
	install -m 644 packaging/fedora/setup/plugins/all_in_one_100.py "$(DESTDIR)$(DATA_DIR)/scripts/plugins"
	install -dm 755 "$(DESTDIR)$(DATA_DIR)/firewalld/aio"
	install -m 644 packaging/firewalld/aio/ovirt-aio.xml.in "$(DESTDIR)$(DATA_DIR)/firewalld/aio/ovirt-aio.xml.in"

install_sec:
	install -dm 755 "$(DESTDIR)$(PKG_PKI_DIR)/certs"
	install -dm 755 "$(DESTDIR)$(PKG_PKI_DIR)/keys"
	install -dm 750 "$(DESTDIR)$(PKG_PKI_DIR)/private"
	install -dm 755 "$(DESTDIR)$(PKG_PKI_DIR)/requests"

	# Configuration files:
	install -m 644 packaging/pki/openssl.conf "$(DESTDIR)$(PKG_PKI_DIR)"
	install -m 644 packaging/pki/cacert.template.in "$(DESTDIR)$(PKG_PKI_DIR)"
	install -m 644 packaging/pki/cert.template.in "$(DESTDIR)$(PKG_PKI_DIR)"

	# Scripts:
	install -m 644 packaging/bin/pki-common.sh "$(DESTDIR)$(DATA_DIR)/bin"
	install -m 755 packaging/bin/pki-create-ca.sh "$(DESTDIR)$(DATA_DIR)/bin"
	install -m 755 packaging/bin/pki-enroll-pkcs12.sh "$(DESTDIR)$(DATA_DIR)/bin"
	install -m 755 packaging/bin/pki-enroll-request.sh "$(DESTDIR)$(DATA_DIR)/bin"
	install -m 755 packaging/bin/ovirt-engine-log-setup-event.sh "$(DESTDIR)$(DATA_DIR)/bin"

install_config:
	@echo "*** Deploying engine-config & engine-manage-domains"

	# Configuration files for the configuration tool:
	install -m 644 packaging/etc/engine-config/engine-config.conf "$(DESTDIR)$(PKG_SYSCONF_DIR)/engine-config"
	install -m 644 packaging/etc/engine-config/engine-config*.properties "$(DESTDIR)$(PKG_SYSCONF_DIR)/engine-config"
	install -m 644 packaging/etc/engine-config/log4j.xml "$(DESTDIR)$(PKG_SYSCONF_DIR)/engine-config/log4j.xml"

	# Main program for the configuration tool:
	install -m 755 packaging/bin/engine-config.sh "$(DESTDIR)$(DATA_DIR)/bin"
	ln -sf "$(DATA_DIR)/bin/engine-config.sh" "$(DESTDIR)$(BIN_DIR)/engine-config"

	# Configuration files for the domain management tool:
	install -m 644 packaging/etc/engine-manage-domains/engine-manage-domains.conf "$(DESTDIR)$(PKG_SYSCONF_DIR)/engine-manage-domains"
	install -m 644 packaging/etc/engine-manage-domains/log4j.xml "$(DESTDIR)$(PKG_SYSCONF_DIR)/engine-manage-domains"

	# Main program for the domain management tool:
	install -m 755 packaging/bin/engine-manage-domains.sh "$(DESTDIR)$(DATA_DIR)/bin"
	ln -sf "$(DATA_DIR)/bin/engine-manage-domains.sh" "$(DESTDIR)$(BIN_DIR)/engine-manage-domains"

	# Install man pages
	install -m 644 packaging/man/man8/engine-manage-domains.8 "$(DESTDIR)$(MAN_DIR)/man8"
	install -m 644 packaging/man/man8/engine-config.8 "$(DESTDIR)$(MAN_DIR)/man8"

install_sysprep:
	@echo "*** Deploying sysperp"
	@install -dm 755 "$(DESTDIR)$(PKG_SYSCONF_DIR)/sysprep"
	install -m 644 packaging/etc/sysprep/* "$(DESTDIR)$(PKG_SYSCONF_DIR)/sysprep"

install_notification_service:
	@echo "*** Deploying notification service"

	install -dm 755 "$(DESTDIR)$(PKG_SYSCONF_DIR)/notifier"

	install -m 644 packaging/etc/notifier/log4j.xml "$(DESTDIR)$(PKG_SYSCONF_DIR)/notifier/log4j.xml"
	install -d -m 755 "$(DESTDIR)$(PKG_SYSCONF_DIR)/notifier/notifier.conf.d"
	install -m 644 packaging/conf/notifier.conf.defaults "$(DESTDIR)$(DATA_DIR)/conf/notifier.conf.defaults"
	install -m 755 packaging/services/ovirt-engine-notifier.py "$(DESTDIR)$(DATA_DIR)/services"
	install -m 644 packaging/services/ovirt-engine-notifier.systemd "$(DESTDIR)$(DATA_DIR)/services"
	install -m 755 packaging/services/ovirt-engine-notifier.sysv "$(DESTDIR)$(DATA_DIR)/services"

install_db_scripts:
	install -dm 755 "$(DESTDIR)$(DATA_DIR)/dbscripts"
	cp -r packaging/dbscripts/* "$(DESTDIR)$(DATA_DIR)/dbscripts"
	find "$(DESTDIR)$(DATA_DIR)/dbscripts" -type d -exec chmod 755 {} \;
	find "$(DESTDIR)$(DATA_DIR)/dbscripts" -type f -name '*.sql' -exec chmod 644 {} \;
	find "$(DESTDIR)$(DATA_DIR)/dbscripts" -type f -name '*.sh' -exec chmod 755 {} \;

install_misc:
	@echo "*** Copying additional files"

	# Shell scripts used by several programs:
	install -m 644 packaging/bin/engine-prolog.sh "$(DESTDIR)$(DATA_DIR)/bin"

	# Other misc things:
	install -m 644 packaging/conf/jaas.conf "$(DESTDIR)$(DATA_DIR)/conf"
	install -m 644 packaging/conf/engine.conf.defaults "$(DESTDIR)$(DATA_DIR)/conf"
	install -m 644 packaging/etc/engine.conf "$(DESTDIR)$(PKG_SYSCONF_DIR)"
	install -dm 755 "$(DESTDIR)$(PKG_SYSCONF_DIR)/engine.conf.d"
	install -dm 755 "$(DESTDIR)$(PKG_SYSCONF_DIR)/ovirt-websocket-proxy.conf.d"
	install -m 644 packaging/conf/ovirt-websocket-proxy.conf.defaults "$(DESTDIR)$(DATA_DIR)/conf"
	install -dm 755 "$(DESTDIR)$(PKG_SYSCONF_DIR)/osinfo.conf.d"
	install -m 644 packaging/conf/osinfo-defaults.properties "$(DESTDIR)$(DATA_DIR)/conf"
	ln -sf "$(DATA_DIR)/conf/osinfo-defaults.properties" "$(DESTDIR)$(PKG_SYSCONF_DIR)/osinfo.conf.d/00-defaults.properties"

	# Service common
	install -dm 755 "$(DESTDIR)$(DATA_DIR)/services"
	install -m 644 packaging/services/__init__.py "$(DESTDIR)$(DATA_DIR)/services"
	install -m 644 packaging/services/config.py "$(DESTDIR)$(DATA_DIR)/services"
	install -m 644 packaging/services/service.py "$(DESTDIR)$(DATA_DIR)/services"
	install -m 755 packaging/services/ovirt-websocket-proxy.py "$(DESTDIR)$(DATA_DIR)/services"
	install -m 644 packaging/services/ovirt-websocket-proxy.systemd "$(DESTDIR)$(DATA_DIR)/services"
	install -m 755 packaging/services/ovirt-websocket-proxy.sysv "$(DESTDIR)$(DATA_DIR)/services"

	# USB filter:
	install -m 644 packaging/etc/usbfilter.txt "$(DESTDIR)$(PKG_SYSCONF_DIR)"

	# branding
	( cd packaging/branding && find ovirt.brand -type f ) | while read f; do \
		install -dm 755 "$$(dirname "$(DESTDIR)$(DATA_DIR)/branding/$$f")"; \
		install -m 644 "packaging/branding/$$f" "$(DESTDIR)$(DATA_DIR)/branding/$$f"; \
	done
	install -dm 755 "$(DESTDIR)$(PKG_SYSCONF_DIR)/branding"
	-rm -f "$(DESTDIR)$(PKG_SYSCONF_DIR)/branding/00-ovirt.brand"
	ln -s "$(DATA_DIR)/branding/ovirt.brand" "$(DESTDIR)$(PKG_SYSCONF_DIR)/branding/00-ovirt.brand"

	# Create a version file
	echo "$(DISPLAY_VERSION)" > "$(DESTDIR)$(DATA_DIR)/conf/version"

install_service:
	@echo "*** Deploying service"

	# Install the files:
	install -m 644 packaging/services/ovirt-engine.xml.in "$(DESTDIR)$(DATA_DIR)/services"
	install -m 644 packaging/services/ovirt-engine-logging.properties.in "$(DESTDIR)$(DATA_DIR)/services"
	install -m 755 packaging/services/ovirt-engine.py "$(DESTDIR)$(DATA_DIR)/services"
	install -m 644 packaging/services/ovirt-engine.systemd "$(DESTDIR)$(DATA_DIR)/services"
	install -m 755 packaging/services/ovirt-engine.sysv "$(DESTDIR)$(DATA_DIR)/services"
	install -dm 755 "$(DESTDIR)$(SYSCONF_DIR)/logrotate.d"
	install -m 644 packaging/sys-etc/logrotate.d/ovirt-engine "$(DESTDIR)$(SYSCONF_DIR)/logrotate.d"

gwt-debug:
	[ -n "$(DEBUG_MODULE)" ] || ( echo "Please specify DEBUG_MODULE" && false )
	cd "frontend/webadmin/modules/$(DEBUG_MODULE)" && \
		$(MVN) \
			$(DEV_BUILD_FLAGS) \
			-Dgwt.noserver=true \
			-Pgwtdev,gwt-admin,gwt-user \
			gwt:debug

all-dev:
	[ "$(DEV_REBUILD)" != 0 ] && rm -f "$(BUILD_FILE)" || :
	rm -f $(GENERATED)
	$(MAKE) \
		all \
		EXTRA_BUILD_FLAGS="$(DEV_BUILD_FLAGS)" \
		$(NULL)

install-dev:	\
		all-dev \
		$(NULL)

	$(MAKE) \
		install \
		EXTRA_BUILD_FLAGS="$(DEV_BUILD_FLAGS)" \
		PYTHON_DIR="$(PREFIX)$(PYTHON_SYS_DIR)" \
		$(NULL)
	install -d "$(DESTDIR)$(LOCALSTATE_DIR)/tmp"
	install -d "$(DESTDIR)$(LOCALSTATE_DIR)/run/notifier"
	install -d "$(DESTDIR)$(LOCALSTATE_DIR)/cache/ovirt-engine"
	install -d "$(DESTDIR)$(LOCALSTATE_DIR)/lib/ovirt-engine/backups"
	install -d "$(DESTDIR)$(LOCALSTATE_DIR)/lib/ovirt-engine/deployments"
	install -d "$(DESTDIR)$(LOCALSTATE_DIR)/lib/ovirt-engine/content"
	install -d "$(DESTDIR)$(LOCALSTATE_DIR)/log/ovirt-engine/host-deploy"
	install -d "$(DESTDIR)$(LOCALSTATE_DIR)/log/ovirt-engine/notifier"
	install -d "$(DESTDIR)$(LOCALSTATE_DIR)/log/ovirt-engine/engine-manage-domains"
