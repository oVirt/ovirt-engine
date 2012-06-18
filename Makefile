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
RPMBUILD=$(shell bash -c "pwd -P")/rpmbuild
SRCRPMBUILD=$(shell bash -c "pwd -P")/srcrpmbuild
OUTPUT_DIR=$(shell bash -c "pwd -P")/output
TARBALL=ovirt-engine-$(RPM_VERSION).tar.gz
SRPM=$(OUTPUT_DIR)/ovirt-engine-$(RPM_VERSION)*.src.rpm
ARCH=noarch
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
	@install -dm 755 $(PREFIX)/usr/share/ovirt-engine/{kerberos,scripts,engine.ear,conf,dbscripts,resources,ovirt-isos,db-backups,engine.ear}
	@install -dm 755 $(PREFIX)/usr/share/ovirt-engine/engine-config/lib
	@install -dm 755 $(PREFIX)/usr/share/ovirt-engine/notifier/lib
	@install -dm 755 $(PREFIX)/usr/share/ovirt-engine/engine-manage-domains/lib
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

	# Jar files for the configuration tool:
	# XXX: Should replace with links to the actual locations
	# of the jar files, or fix the scripts to use that.
	install -m 644 $(SOURCE_DIR)/engine-config-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/engine-config/lib/
	ln -s /usr/share/ovirt-engine/engine-config/lib/engine-config-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/engine-config/lib/engine-config.jar
	install -m 644 $(SOURCE_DIR)/engineencryptutils-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/engine-config/lib/engine-encryptutils.jar
	install -m 644 $(SOURCE_DIR)/compat-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/engine-config/lib/engine-compat.jar

	# Main program for the configuration tool:
	install -m 750 backend/manager/tools/engine-config/src/main/resources/engine-config $(PREFIX)/usr/share/ovirt-engine/engine-config/
	ln -s /usr/share/ovirt-engine/engine-config/engine-config $(PREFIX)/usr/bin/engine-config

	# Configuration files for the domain management tool:
	install -m 644 backend/manager/modules/utils/src/main/resources/engine-manage-domains.conf $(PREFIX)/etc/ovirt-engine/engine-manage-domains/
	install -m 644 backend/manager/modules/utils/src/main/resources/engine-manage-domains/log4j.xml $(PREFIX)/etc/ovirt-engine/engine-manage-domains/

	# Jar files for the domain management tool:
	# XXX: Should replace with links to the actual locations
	# of the jar files, or fix the scripts to use that.
	install -m 644 $(SOURCE_DIR)/compat-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/engine-manage-domains/lib/engine-compat.jar

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

	# Jar files:
	# XXX: Should replace with links to the actual locations
	# of the jar files, or fix the scripts to use that.
	install -m 644 $(SOURCE_DIR)/engine-notifier-service-$(APP_VERSION).jar $(PREFIX)/usr/share/ovirt-engine/notifier/engine-notifier.jar

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
	install -m 644 packaging/fedora/setup/resources/postgres/postgres-ds.xml $(PREFIX)/usr/share/ovirt-engine/conf
	install -m 755 packaging/resources/ovirtlogrot.sh ${PREFIX}/usr/share/ovirt-engine/scripts/
	install -m 755 packaging/resources/ovirt-cron ${PREFIX}/etc/cron.daily/
	install -m 644 packaging/resources/ovirt-tmpfilesd ${PREFIX}/etc/tmpfiles.d/ovirt-engine.conf

install_jboss_modules:
	@echo "*** Deploying JBoss modules"

	# Copy the module definitions:
	install -dm 755 $(PREFIX)/usr/share/ovirt-engine/modules
	cp -r deployment/modules/* $(PREFIX)/usr/share/ovirt-engine/modules
	find $(PREFIX)/usr/share/ovirt-engine/modules -type d -exec chmod 755 {} \;
	find $(PREFIX)/usr/share/ovirt-engine/modules -type f -exec chmod 644 {} \;

	# PostgreSQL driver:
	ln -s /usr/share/java/postgresql-jdbc.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/postgresql/main/.

	# Apache commons-codec module:
	ln -s /usr/share/java/commons-codec.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/apache/commons/codec/main/.

	# Apache HTTP components module:
	ln -s /usr/share/java/httpcomponents/httpcore.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/apache/httpcomponents/main/.
	ln -s /usr/share/java/httpcomponents/httpclient.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/apache/httpcomponents/main/.
	ln -s /usr/share/java/httpcomponents/httpmime.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/apache/httpcomponents/main/.

	# Scannotation module:
	ln -s /usr/share/java/scannotation.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/scannotation/scannotation/main/.

	# JAXB module:
	ln -s /usr/share/java/glassfish-jaxb/jaxb-impl.jar $(PREFIX)/usr/share/ovirt-engine/modules/com/sun/xml/bind/main/.
	ln -s /usr/share/java/glassfish-jaxb/jaxb-xjc.jar $(PREFIX)/usr/share/ovirt-engine/modules/com/sun/xml/bind/main/.
	ln -s /usr/share/java/istack-commons-runtime.jar $(PREFIX)/usr/share/ovirt-engine/modules/com/sun/xml/bind/main/.

	# JAX-RS API modules:
	ln -s /usr/share/java/resteasy/jaxrs-api.jar $(PREFIX)/usr/share/ovirt-engine/modules/javax/ws/rs/api/main/.

	# Resteasy modules:
	ln -s /usr/share/java/resteasy/resteasy-cdi.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/jboss/resteasy/resteasy-cdi/main/.
	ln -s /usr/share/java/resteasy/resteasy-jettison-provider.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/jboss/resteasy/resteasy-jettison-provider/main/.
	ln -s /usr/share/java/resteasy/resteasy-atom-provider.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/jboss/resteasy/resteasy-atom-provider/main/.
	ln -s /usr/share/java/resteasy/resteasy-yaml-provider.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/jboss/resteasy/resteasy-yaml-provider/main/.
	ln -s /usr/share/java/resteasy/resteasy-multipart-provider.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/jboss/resteasy/resteasy-multipart-provider/main/.
	ln -s /usr/share/java/resteasy/resteasy-jackson-provider.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/jboss/resteasy/resteasy-jackson-provider/main/.
	ln -s /usr/share/java/resteasy/resteasy-jaxb-provider.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/jboss/resteasy/resteasy-jaxb-provider/main/.
	ln -s /usr/share/java/resteasy/resteasy-jaxrs.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/jboss/resteasy/resteasy-jaxrs/main/.
	ln -s /usr/share/java/resteasy/async-http-servlet-3.0.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/jboss/resteasy/resteasy-jaxrs/main/.
	ln -s /usr/share/java/resteasy/resteasy-jsapi.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/jboss/resteasy/resteasy-jsapi/main/.

	# Jackson modules:
	ln -s /usr/share/java/jackson/jackson-jaxrs.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/codehaus/jackson/jackson-jaxrs/main/.
	ln -s /usr/share/java/jackson/jackson-core-asl.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/codehaus/jackson/jackson-core-asl/main/.
	ln -s /usr/share/java/jackson/jackson-mapper-asl.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/codehaus/jackson/jackson-mapper-asl/main/.
	ln -s /usr/share/java/jackson/jackson-xc.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/codehaus/jackson/jackson-xc/main/.

	# Hibernate validator module:
	ln -s /usr/share/java/hibernate-validator.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/hibernate/validator/main/.
	ln -s /usr/share/java/jtype.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/hibernate/validator/main/.

	# Jettison:
	ln -s /usr/share/java/jettison.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/codehaus/jettison/main

	# Apache MIME4J:
	ln -s /usr/share/java/apache-mime4j/core.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/apache/james/mime4j/main/.
	ln -s /usr/share/java/apache-mime4j/dom.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/apache/james/mime4j/main/.
	ln -s /usr/share/java/apache-mime4j/storage.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/apache/james/mime4j/main/.

	# Snakeyaml:
	ln -s /usr/share/java/snakeyaml.jar $(PREFIX)/usr/share/ovirt-engine/modules/org/yaml/snakeyaml/main/.

install_service:
	@echo "*** Deploying service"

	# Install the files:
	install -m 644 packaging/fedora/engine-service.xml $(PREFIX)/etc/ovirt-engine
	install -m 644 packaging/fedora/engine-service-logging.properties $(PREFIX)/usr/share/ovirt-engine/service
	install -m 644 packaging/fedora/engine-service.sysconfig $(PREFIX)/etc/sysconfig/ovirt-engine
	install -m 644 packaging/fedora/engine-service.limits $(PREFIX)/etc/security/limits.d/10-ovirt-engine.conf
	install -m 755 packaging/fedora/engine-service.py $(PREFIX)/usr/share/ovirt-engine/service
	install -m 644 packaging/fedora/engine-service.systemd $(PREFIX)/usr/lib/systemd/system/ovirt-engine.service

	# Install the links:
	ln -s /usr/share/ovirt-engine/service/engine-service.py $(PREFIX)/usr/bin/engine-service

