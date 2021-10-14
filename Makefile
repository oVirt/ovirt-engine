# ====================================================================
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
# ====================================================================

#
# CUSTOMIZATION-BEGIN
#
BUILD_GWT=1
BUILD_ALL_USER_AGENTS=0
BUILD_LOCALES=0
BUILD_DEV=0
BUILD_UT=1
SKIP_CHECKS=0
EXTRA_BUILD_FLAGS=
BUILD_VALIDATION=1
BUILD_ENV_VALIDATION=1
BUILD_JAVA_OPTS_MAVEN?=
BUILD_JAVA_OPTS_GWT?=
DEV_REBUILD=1
DEV_BUILD_GWT_DRAFT=0
DEV_BUILD_GWT_SUPER_DEV_MODE=1
DEV_EXTRA_BUILD_FLAGS=
DEV_EXTRA_BUILD_FLAGS_GWT_DEFAULTS=
IMAGEIO_SYSCONF_DIR=/etc/ovirt-imageio
VMCONSOLE_SYSCONF_DIR=/etc/ovirt-vmconsole
VMCONSOLE_PKI_DIR=/etc/pki/ovirt-vmconsole

PACKAGE_NAME=ovirt-engine
ENGINE_NAME=$(PACKAGE_NAME)
MVN=mvn
PYTHON=$(shell which python3 2> /dev/null)
PYFLAKES=$(shell which pyflakes 2> /dev/null)
PY_VERSION=3
PEP8=$(shell which pycodestyle-3 2> /dev/null)
ISORT=isort
PREFIX=/usr/local
LOCALSTATE_DIR=$(PREFIX)/var
BIN_DIR=$(PREFIX)/bin
PID_DIR=$(LOCALSTATE_DIR)/run
SYSCONF_DIR=$(PREFIX)/etc
DATAROOT_DIR=$(PREFIX)/share
LIBEXEC_DIR=$(PREFIX)/libexec
MAN_DIR=$(DATAROOT_DIR)/man
DOC_DIR=$(DATAROOT_DIR)/doc
DATA_DIR=$(DATAROOT_DIR)/$(ENGINE_NAME)
MAVENPOM_DIR=$(DATAROOT_DIR)/maven-poms
JAVA_DIR=$(DATAROOT_DIR)/java
PKG_SYSCONF_DIR=$(SYSCONF_DIR)/$(ENGINE_NAME)
PKG_PKI_DIR=$(SYSCONF_DIR)/pki/$(ENGINE_NAME)
PKG_DOC_DIR=$(DOC_DIR)/$(ENGINE_NAME)
PKG_HTML_DIR=$(PKG_DOC_DIR)
PKG_EAR_DIR=$(DATA_DIR)/engine.ear
PKG_JBOSS_MODULES=$(DATA_DIR)/modules
PKG_LOGUTILS=$(DATA_DIR)/logutils
PKG_CACHE_DIR=$(LOCALSTATE_DIR)/cache/$(ENGINE_NAME)
PKG_LOG_DIR=$(LOCALSTATE_DIR)/log/$(ENGINE_NAME)
PKG_BACKUP_DEFAULT_DIR=$(LOCALSTATE_DIR)/lib/$(ENGINE_NAME)-backup
PKG_BACKUP_LOG_DEFAULT_DIR=$(LOCALSTATE_DIR)/log/$(ENGINE_NAME)-backup
PKG_STATE_DIR=$(LOCALSTATE_DIR)/lib/$(ENGINE_NAME)
PKG_TMP_DIR=$(LOCALSTATE_DIR)/tmp/$(ENGINE_NAME)
JBOSS_HOME=/usr/share/ovirt-engine-wildfly
JBOSS_RUNTIME=$(PKG_STATE_DIR)/jboss_runtime
PYTHON_DIR=$(PYTHON_SYS_DIR)
DEV_PYTHON_DIR=
DEV_SETUP_ENV_DIR=
PKG_USER=ovirt
PKG_GROUP=ovirt
WILDFLY_OVERLAY_MODULES=/usr/share/ovirt-engine-wildfly-overlay/modules
#
# CUSTOMIZATION-END
#

include version.mak
# major, minor, seq
ENGINE_VERSION:=$(shell cat pom.xml | head -n 20 | grep '<version>' | head -n 1 | sed -e 's/.*>\(.*\)<.*/\1/' -e 's/-SNAPSHOT//')
PACKAGE_VERSION=$(ENGINE_VERSION)$(if $(MILESTONE),_$(MILESTONE))
DISPLAY_VERSION=$(PACKAGE_VERSION)

DEV_BUILD_FLAGS:=-P brand-source-maps
ifneq ($(DEV_BUILD_GWT_DRAFT),0)
DEV_BUILD_FLAGS:=$(DEV_BUILD_FLAGS) -P gwtdraft
endif

GWT_DEBUG_PROFILES:=gwtdraft,gwtdev
GWT_DEBUG_TARGET:=gwt:debug
ifneq ($(DEV_BUILD_GWT_SUPER_DEV_MODE),0)
GWT_DEBUG_PROFILES:=$(GWT_DEBUG_PROFILES),gwtsuperdev
endif

BUILD_FLAGS:=
ifneq ($(BUILD_GWT),0)
BUILD_FLAGS:=$(BUILD_FLAGS) -P gwt-admin
endif
ifneq ($(BUILD_ALL_USER_AGENTS),0)
BUILD_FLAGS:=$(BUILD_FLAGS) -P all-user-agents
endif
ifneq ($(BUILD_LOCALES),0)
BUILD_FLAGS:=$(BUILD_FLAGS) -P all-langs
endif

ifneq ($(SKIP_CHECKS),0)
BUILD_FLAGS:=$(BUILD_FLAGS) -D skipTests
DEV_EXTRA_BUILD_FLAGS:=$(DEV_EXTRA_BUILD_FLAGS) -Danimal.sniffer.skip -Dcheckstyle.skip
endif

DEV_BUILD_FLAGS:=$(DEV_BUILD_FLAGS) $(DEV_EXTRA_BUILD_FLAGS_GWT_DEFAULTS)
DEV_BUILD_FLAGS:=$(DEV_BUILD_FLAGS) $(DEV_EXTRA_BUILD_FLAGS)

ifeq ($(BUILD_UT),0)
BUILD_FLAGS:=$(BUILD_FLAGS) -D skipTests
endif
ifneq ($(BUILD_DEV),0)
BUILD_FLAGS:=$(BUILD_FLAGS) $(DEV_BUILD_FLAGS)
endif
ifneq ($(BUILD_JAVA_OPTS_GWT),)
BUILD_FLAGS:=$(BUILD_FLAGS) -D gwt.jvmArgs="$(BUILD_JAVA_OPTS_GWT)"
endif
BUILD_FLAGS:=$(BUILD_FLAGS) $(EXTRA_BUILD_FLAGS)

PYTHON_SYS_DIR:=$(shell $(PYTHON) -c "from distutils.sysconfig import get_python_lib as f;print(f())")
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
	GENERATED_FILE_LIST=$$(echo "$(GENERATED)" | sed -e "s/.gitignore\s//g" -e "s/ /\\\n/g"); \
	sed \
	-e "s|@ENGINE_DEFAULTS@|$(DATA_DIR)/services/ovirt-engine/ovirt-engine.conf|g" \
	-e "s|@ENGINE_VARS@|$(PKG_SYSCONF_DIR)/engine.conf|g" \
	-e "s|@ENGINE_SETUP_VARS@|$(SYSCONF_DIR)/ovirt-engine-setup.conf|g" \
	-e "s|@ENGINE_NOTIFIER_VARS@|$(PKG_SYSCONF_DIR)/notifier/notifier.conf|g" \
	-e "s|@ENGINE_FKLSNR_VARS@|$(PKG_SYSCONF_DIR)/ovirt-fence-kdump-listener.conf|g" \
	-e "s|@ENGINE_WSPROXY_VARS@|$(PKG_SYSCONF_DIR)/ovirt-websocket-proxy.conf|g" \
	-e "s|@ENGINE_USER@|$(PKG_USER)|g" \
	-e "s|@ENGINE_GROUP@|$(PKG_GROUP)|g" \
	-e "s|@ENGINE_ETC@|$(PKG_SYSCONF_DIR)|g" \
	-e "s|@ENGINE_PKI@|$(PKG_PKI_DIR)|g" \
	-e "s|@ENGINE_LOG@|$(PKG_LOG_DIR)|g" \
	-e "s|@ENGINE_TMP@|$(PKG_TMP_DIR)|g" \
	-e "s|@ENGINE_USR@|$(DATA_DIR)|g" \
	-e "s|@ENGINE_LIBEXEC@|$(LIBEXEC_DIR)|g" \
	-e "s|@ENGINE_DOC@|$(PKG_DOC_DIR)|g" \
	-e "s|@ENGINE_VAR@|$(PKG_STATE_DIR)|g" \
	-e "s|@ENGINE_BACKUP_DEFAULT_DIR@|$(PKG_BACKUP_DEFAULT_DIR)|g" \
	-e "s|@ENGINE_BACKUP_LOG_DEFAULT_DIR@|$(PKG_BACKUP_LOG_DEFAULT_DIR)|g" \
	-e "s|@ENGINE_CACHE@|$(PKG_CACHE_DIR)|g" \
	-e "s|@ENGINE_PID@|$(PID_DIR)/$(ENGINE_NAME).pid|g" \
	-e "s|@ENGINE_COMMON_USR@|$(DATA_DIR)|g" \
	-e "s|@SETUP_LOG@|$(PKG_LOG_DIR)|g" \
	-e "s|@SETUP_USR@|$(DATA_DIR)|g" \
	-e "s|@SETUP_VAR@|$(PKG_STATE_DIR)|g" \
	-e "s|@DEV_PYTHON_DIR@|$(DEV_PYTHON_DIR)|g" \
	-e "s|@DEV_SETUP_ENV_DIR@|$(DEV_SETUP_ENV_DIR)|g" \
	-e "s|@RPM_VERSION@|$(ENGINE_VERSION)|g" \
	-e "s|@RPM_RELEASE@|$(RPM_RELEASE)|g" \
	-e "s|@MILESTONE@|$(MILESTONE)|g" \
	-e "s|@PACKAGE_NAME@|$(PACKAGE_NAME)|g" \
	-e "s|@PACKAGE_VERSION@|$(PACKAGE_VERSION)|g" \
	-e "s|@POSTGRESQL_SYSTEMD_SERVICE@|$(POSTGRESQL_SYSTEMD_SERVICE)|g" \
	-e "s|@DISPLAY_VERSION@|$(DISPLAY_VERSION)|g" \
	-e "s|@JBOSS_HOME@|$(JBOSS_HOME)|g" \
	-e "s|@JBOSS_RUNTIME@|$(JBOSS_RUNTIME)|g" \
	-e "s|@PEP8@|$(PEP8)|g" \
	-e "s|@PYFLAKES@|$(PYFLAKES)|g" \
	-e "s|@ISORT@|$(ISORT)|g" \
	-e "s|@DEVMODE@|$(BUILD_DEV)|g" \
	-e "s|@VMCONSOLE_SYSCONF_DIR@|$(VMCONSOLE_SYSCONF_DIR)|g" \
	-e "s|@VMCONSOLE_PKI_DIR@|$(VMCONSOLE_PKI_DIR)|g" \
	-e "s|@PY_VERSION@|$(PY_VERSION)|g" \
	-e "s|@VMCONSOLE_PROXY_HELPER_PATH@|$(LIBEXEC_DIR)/ovirt-vmconsole-proxy-helper/ovirt-vmconsole-list.py|g" \
	-e "s|@VMCONSOLE_PROXY_HELPER_VARS@|$(PKG_SYSCONF_DIR)/ovirt-vmconsole-proxy-helper.conf|g" \
	-e "s|@VMCONSOLE_PROXY_HELPER_DEFAULTS@|$(DATA_DIR)/conf/ovirt-vmconsole-proxy-helper.conf|g" \
	-e "s|@BIN_DIR@|$(BIN_DIR)|g" \
	-e "s|@AAA_JDBC_USR@|$(DATAROOT_DIR)/ovirt-engine-extension-aaa-jdbc|g" \
	-e "s|@IMAGEIO_SYSCONF_DIR@|$(IMAGEIO_SYSCONF_DIR)|g" \
	-e "s|@GENERATED_FILE_LIST@|$${GENERATED_FILE_LIST}|g" \
	$< > $@

# List of files that will be generated from templates:
# Once add new template file, if required chmod, add it in generated-files target.
GENERATED = \
	.gitignore \
	automation/milestone-config.sh \
	ovirt-engine.spec \
	build/helptag.py \
	build/helptag_checker.py \
	build/helptag-oneline-check.py \
	build/python-check.sh \
	build/shell-check.sh \
	packaging/bin/engine-backup.sh \
	packaging/bin/engine-host-update.py \
	packaging/bin/engine-migrate-he.py \
	packaging/bin/engine-prolog.sh \
	packaging/bin/pki-common.sh \
	packaging/bin/vdsm_to_network_name_map \
	packaging/conf/notifier-logging.properties \
	packaging/conf/ovirt-vmconsole-proxy-helper.conf \
	packaging/conf/ovirt-vmconsole-proxy.conf \
	packaging/etc/engine.conf.d/README \
	packaging/etc/notifier/notifier.conf.d/README \
	packaging/etc/ovirt-fence-kdump-listener.conf.d/README \
	packaging/etc/ovirt-vmconsole-proxy-helper.conf.d/README \
	packaging/etc/ovirt-websocket-proxy.conf.d/README \
	packaging/libexec/ovirt-vmconsole-proxy-helper/ovirt_vmconsole_conf.py \
	packaging/libexec/ovirt-vmconsole-proxy-helper/ovirt-vmconsole-list.py \
	packaging/pythonlib/ovirt_engine/config.py \
	packaging/services/ovirt-engine-notifier/config.py \
	packaging/services/ovirt-engine-notifier/ovirt-engine-notifier.conf \
	packaging/services/ovirt-engine-notifier/ovirt-engine-notifier.py \
	packaging/services/ovirt-engine-notifier/ovirt-engine-notifier.systemd \
	packaging/services/ovirt-engine-notifier/ovirt-engine-notifier.sysv \
	packaging/services/ovirt-engine/config.py \
	packaging/services/ovirt-engine/ovirt-engine.conf \
	packaging/services/ovirt-engine/ovirt-engine.py \
	packaging/services/ovirt-engine/ovirt-engine.systemd \
	packaging/services/ovirt-engine/ovirt-engine.sysv \
	packaging/services/ovirt-fence-kdump-listener/config.py \
	packaging/services/ovirt-fence-kdump-listener/ovirt-fence-kdump-listener.conf \
	packaging/services/ovirt-fence-kdump-listener/ovirt-fence-kdump-listener.py \
	packaging/services/ovirt-fence-kdump-listener/ovirt-fence-kdump-listener.systemd \
	packaging/services/ovirt-fence-kdump-listener/ovirt-fence-kdump-listener.sysv \
	packaging/services/ovirt-websocket-proxy/config.py \
	packaging/services/ovirt-websocket-proxy/ovirt-websocket-proxy.conf \
	packaging/services/ovirt-websocket-proxy/ovirt-websocket-proxy.py \
	packaging/services/ovirt-websocket-proxy/ovirt-websocket-proxy.systemd \
	packaging/services/ovirt-websocket-proxy/ovirt-websocket-proxy.sysv \
	packaging/setup/bin/bundle-ovirt-engine-health \
	packaging/setup/bin/ovirt-engine-health \
	packaging/setup/bin/ovirt-engine-setup.env \
	packaging/setup/bin/ovirt-engine-upgrade-check \
	packaging/setup/ovirt_engine_setup/config.py \
	packaging/setup/ovirt_engine_setup/engine/config.py \
	packaging/setup/ovirt_engine_setup/engine_common/config.py \
	packaging/setup/ovirt_engine_setup/ovirt_imageio/config.py \
	packaging/setup/ovirt_engine_setup/vmconsole_proxy_helper/config.py \
	packaging/setup/ovirt_engine_setup/websocket_proxy/config.py \
	packaging/sys-etc/logrotate.d/ovirt-engine \
	packaging/sys-etc/logrotate.d/ovirt-engine-notifier \
	packaging/sys-etc/logrotate.d/ovirt-engine-setup \
	packaging/cinderlib/config.py \
	packaging/cinderlib/cinderlib-client.py \
	$(NULL)

all: \
	generated-files \
	validations \
	$(BUILD_FILE) \
	$(NULL)

generated-files:	$(GENERATED)
	chmod a+x build/helptag.py
	chmod a+x build/helptag_checker.py
	chmod a+x build/helptag-oneline-check.py
	chmod a+x build/python-check.sh
	chmod a+x build/shell-check.sh
	chmod a+x packaging/bin/engine-backup.sh
	chmod a+x packaging/bin/engine-host-update.py
	chmod a+x packaging/bin/engine-migrate-he.py
	chmod a+x packaging/bin/vdsm_to_network_name_map
	chmod a+x packaging/libexec/ovirt-vmconsole-proxy-helper/ovirt-vmconsole-list.py
	chmod a+x packaging/ansible-runner-service-project/project/roles/ovirt-ova-query/files/query_ova.py
	chmod a+x packaging/ansible-runner-service-project/project/roles/ovirt-ova-pack/files/pack_ova.py
	chmod a+x packaging/ansible-runner-service-project/project/roles/ovirt-ova-extract/files/extract_ova.py
	chmod a+x packaging/services/ovirt-engine/ovirt-engine.sysv
	chmod a+x packaging/services/ovirt-engine-notifier/ovirt-engine-notifier.py
	chmod a+x packaging/services/ovirt-engine-notifier/ovirt-engine-notifier.sysv
	chmod a+x packaging/services/ovirt-engine/ovirt-engine.py
	chmod a+x packaging/services/ovirt-fence-kdump-listener/ovirt-fence-kdump-listener.py
	chmod a+x packaging/services/ovirt-fence-kdump-listener/ovirt-fence-kdump-listener.sysv
	chmod a+x packaging/services/ovirt-websocket-proxy/ovirt-websocket-proxy.py
	chmod a+x packaging/services/ovirt-websocket-proxy/ovirt-websocket-proxy.sysv
	chmod a+x packaging/cinderlib/cinderlib-client.py
	chmod a+x packaging/setup/bin/ovirt-engine-health
	chmod a+x packaging/setup/bin/ovirt-engine-upgrade-check
	chmod a+x packaging/cinderlib/cinderlib-client.py

# support force run of maven
maven:
	MAVEN_OPTS="${MAVEN_OPTS} "'$(BUILD_JAVA_OPTS_MAVEN)' \
		$(MVN) \
			$(BUILD_FLAGS) \
			$(BUILD_TARGET)
	touch "$(BUILD_FILE)"

$(BUILD_FILE):
	$(MAKE) maven

clean:
	# Clean maven generated stuff:
	$(MVN) clean $(EXTRA_BUILD_FLAGS)
	rm -rf $(BUILD_FILE) tmp.dev.flist

	# Clean files generated from templates:
	rm -rf $$(echo $(GENERATED) | grep -v .gitignore)

test:
	$(MVN) install $(BUILD_FLAGS) $(EXTRA_BUILD_FLAGS)

install: \
	all \
	install-layout \
	install_artifacts \
	install_poms \
	$(NULL)

.PHONY: ovirt-engine.spec.in

ovirt-engine.spec: version.mak
.gitignore: Makefile

dist:	ovirt-engine.spec
	git ls-files | tar --files-from /proc/self/fd/0 -czf "$(TARBALL)" ovirt-engine.spec
	@echo
	@echo You can use rpmbuild -tb $(TARBALL) to produce rpms
	@echo

# copy SOURCEDIR to TARGETDIR
# exclude EXCLUDEGEN a list of files to exclude with .in
# exclude EXCLUDE a list of files.
copy-recursive:
	( cd "$(SOURCEDIR)" && find . -type d -printf '%P\n' ) | while read d; do \
		install -d -m 755 "$(TARGETDIR)/$${d}"; \
	done
	( \
		cd "$(SOURCEDIR)" && find . -type f -printf '%P\n' | \
		while read f; do \
			exclude=false; \
			for x in $(EXCLUDE_GEN); do \
				if [ "$(SOURCEDIR)/$${f}" = "$${x}.in" ]; then \
					exclude=true; \
					break; \
				fi; \
			done; \
			for x in $(EXCLUDE); do \
				if [ "$(SOURCEDIR)/$${f}" = "$${x}" ]; then \
					exclude=true; \
					break; \
				fi; \
			done; \
			$${exclude} || echo "$${f}"; \
		done \
	) | while read f; do \
		src="$(SOURCEDIR)/$${f}"; \
		dst="$(TARGETDIR)/$${f}"; \
		[ -x "$${src}" ] && MASK=0755 || MASK=0644; \
		[ -n "$(DEV_FLIST)" ] && echo "$${dst}" | sed 's#^$(PREFIX)/##' >> "$(DEV_FLIST)"; \
		install -T -m "$${MASK}" "$${src}" "$${dst}"; \
	done


validations:	generated-files
	if [ "$(BUILD_ENV_VALIDATION)" != 0 ]; then \
		if [ "$(BUILD_LOCALES)" != 0 ]; then \
			require="10240"; \
			current="$$(ulimit -n)"; \
			if [ "$${current}" -lt "$${require}" ]; then \
				echo "Building locales requires more than $${require} available file descriptors, currently $${current}" >&2; \
				echo "Refer to README.adoc for further instructions" >&2; \
				false; \
			fi; \
		fi; \
	fi
	if [ "$(BUILD_VALIDATION)" != 0 ]; then \
                echo "Performing build validation." &&\
		build/shell-check.sh && \
		build/python-check.sh && \
		build/ansible-check.sh && \
		build/image-check.sh && \
		build/helptag-oneline-check.py && \
		build/dbscripts-duplicate_upgrade_scripts.sh; \
	fi

install_artifacts:
	# we must exclude tmp.repos directory so we
	# won't get artifacts of older branches
	# we should use search MAVEN_OUTPUT_DIR as it may contain
	# pre-compiled artifacts at different hierarchy.
	install -dm 0755 "$(DESTDIR)$(PKG_JBOSS_MODULES)"
	for category in common tools; do \
		install -dm 0755 "$(DESTDIR)$(PKG_JBOSS_MODULES)/$${category}"; \
		find "$(MAVEN_OUTPUT_DIR)" -name '*'"-$${category}-modules.zip" | grep -v tmp.repos | xargs -r -n 1 unzip -q -o -d "$(DESTDIR)$(PKG_JBOSS_MODULES)/$${category}"; \
	done
	install -dm 0755 "$(DESTDIR)$(PKG_EAR_DIR)"
	find "$(MAVEN_OUTPUT_DIR)" -name '*.ear' -type f | grep -v tmp.repos | xargs -n 1 unzip -q -o -d "$(DESTDIR)$(PKG_EAR_DIR)"
	install -dm 0755 "$(DESTDIR)$(DATA_DIR)/restapi.war"
	find "$(MAVEN_OUTPUT_DIR)" -name 'restapi-webapp-*.war' -type f | grep -v tmp.repos | xargs -n 1 unzip -q -o -d "$(DESTDIR)$(DATA_DIR)/restapi.war"
	install -dm 0755 "$(DESTDIR)$(DATA_DIR)/apidoc.war"
	find "$(MAVEN_OUTPUT_DIR)" -name 'restapi-apidoc-*.war' -type f | grep -v tmp.repos | xargs -n 1 unzip -q -o -d "$(DESTDIR)$(DATA_DIR)/apidoc.war"
	install -dm 0755 "$(DESTDIR)$(PKG_HTML_DIR)"
	find "$(MAVEN_OUTPUT_DIR)" -name '*-javadoc.jar' -type f | grep -v tmp.repos | while read f; do \
		comp="$$(basename "$${f}" | sed 's/-[0-9].*//')"; \
		unzip -q -o -d "$(DESTDIR)$(PKG_HTML_DIR)/$${comp}" "$${f}"; \
	done

	# logutils.jar is need only for tools and it's loaded from boot
	# classpath -> we need to handle it differently than other jars
	install -dm 0755 "$(DESTDIR)$(PKG_LOGUTILS)"
	find "$(MAVEN_OUTPUT_DIR)" -name 'logutils-*.jar' -not -name 'logutils-*-tests.jar' -type f -exec install -m 644 {} "$(DESTDIR)$(PKG_LOGUTILS)/logutils.jar" \;

	# extract embedded artifacts as doc
	# no need to relay on source tree for these
	install -d -m 755 "$(DESTDIR)$(PKG_DOC_DIR)"
	unzip -q -c "$(DESTDIR)$(PKG_JBOSS_MODULES)/common/org/ovirt/engine/core/dal/main/dal.jar" bundles/AuditLogMessages.properties > \
		"$(DESTDIR)$(PKG_DOC_DIR)/AuditLogMessages.properties"
	chmod 0644 "$(DESTDIR)$(PKG_DOC_DIR)/AuditLogMessages.properties"

install_poms:
	install -dm 755 "$(DESTDIR)$(MAVENPOM_DIR)"
	install -m 644 backend/manager/extensions-tool/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-extensions-tool.pom"
	install -m 644 backend/manager/sso-client-registration-tool/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-sso-client-registration-tool.pom"
	install -m 644 backend/manager/logutils/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-logutils.pom"
	install -m 644 backend/manager/modules/aaa/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-aaa.pom"
	install -m 644 backend/manager/modules/bll/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-bll.pom"
	install -m 644 backend/manager/modules/branding/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-branding.pom"
	install -m 644 backend/manager/modules/builtin-extensions/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-builtin.pom"
	install -m 644 backend/manager/modules/common/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-common.pom"
	install -m 644 backend/manager/modules/compat/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-compat.pom"
	install -m 644 backend/manager/modules/dal/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-dal.pom"
	install -m 644 backend/manager/modules/extensions-manager/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-extensions-manager.pom"
	install -m 644 backend/manager/modules/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-manager-modules.pom"
	install -m 644 backend/manager/modules/restapi/interface/common/jaxrs/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-interface-common-jaxrs.pom"
	install -m 644 backend/manager/modules/restapi/interface/definition/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-restapi-definition.pom"
	install -m 644 backend/manager/modules/restapi/jaxrs/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-restapi-jaxrs.pom"
	install -m 644 backend/manager/modules/restapi/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-restapi-parent.pom"
	install -m 644 backend/manager/modules/restapi/types/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-restapi-types.pom"
	install -m 644 backend/manager/modules/scheduler/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-scheduler.pom"
	install -m 644 backend/manager/modules/searchbackend/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-searchbackend.pom"
	install -m 644 backend/manager/modules/utils/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-utils.pom"
	install -m 644 backend/manager/modules/uutils/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-uutils.pom"
	install -m 644 backend/manager/modules/vdsbroker/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-vdsbroker.pom"
	install -m 644 backend/manager/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-manager.pom"
	install -m 644 backend/manager/tools/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-tools.pom"
	install -m 644 backend/pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-backend.pom"
	install -m 644 pom.xml "$(DESTDIR)$(MAVENPOM_DIR)/$(PACKAGE_NAME)-root.pom"

install-packaging-files: \
		$(GENERATED) \
		$(NULL)
	$(MAKE) copy-recursive SOURCEDIR=packaging/sys-etc TARGETDIR="$(DESTDIR)$(SYSCONF_DIR)" EXCLUDE_GEN="$(GENERATED)"
	$(MAKE) copy-recursive SOURCEDIR=packaging/etc TARGETDIR="$(DESTDIR)$(PKG_SYSCONF_DIR)" EXCLUDE_GEN="$(GENERATED)"
	$(MAKE) copy-recursive SOURCEDIR=packaging/pki TARGETDIR="$(DESTDIR)$(PKG_PKI_DIR)" EXCLUDE_GEN="$(GENERATED)"
	for d in bin conf files firewalld services cinderlib ansible-runner-service-project selinux; do \
		$(MAKE) copy-recursive SOURCEDIR="packaging/$${d}" TARGETDIR="$(DESTDIR)$(DATA_DIR)/$${d}" EXCLUDE_GEN="$(GENERATED)" EXCLUDE="packaging/ansible-runner-service-project/env/.gitkeep"; \
	done
	$(MAKE) copy-recursive SOURCEDIR=packaging/doc TARGETDIR="$(DESTDIR)$(PKG_DOC_DIR)" EXCLUDE_GEN="$(GENERATED)"
	$(MAKE) copy-recursive SOURCEDIR=packaging/man TARGETDIR="$(DESTDIR)$(MAN_DIR)" EXCLUDE_GEN="$(GENERATED)"
	$(MAKE) copy-recursive SOURCEDIR=packaging/libexec TARGETDIR="$(DESTDIR)$(LIBEXEC_DIR)" EXCLUDE_GEN="$(GENERATED)"
	# `cp` instead of `install` to copy symlinks
	cp -dRT packaging/icons $(DESTDIR)$(DATA_DIR)/icons
	find $(DESTDIR)$(DATA_DIR)/icons -type f -exec chmod 0644 {} \;
	find $(DESTDIR)$(DATA_DIR)/icons -type d -exec chmod 0755 {} \;

	# copy all of packaging/setup but exclude all tests.
	$(MAKE) copy-recursive SOURCEDIR=packaging/setup TARGETDIR="$(DESTDIR)$(DATA_DIR)/setup" \
		EXCLUDE_GEN="$(GENERATED)" \
		EXCLUDE="$$(echo $$(find packaging/setup/tests)) packaging/setup/plugins/README"

	$(MAKE) copy-recursive SOURCEDIR=packaging/pythonlib TARGETDIR="$(DESTDIR)$(PYTHON_DIR)" EXCLUDE="$$(echo $$(find packaging/pythonlib/tests))" EXCLUDE_GEN="$(GENERATED)"

	# we should avoid make these directories dirty
	$(MAKE) copy-recursive SOURCEDIR=packaging/dbscripts TARGETDIR="$(DESTDIR)$(DATA_DIR)/dbscripts" \
		EXCLUDE_GEN="$(GENERATED)" \
		EXCLUDE="$$(echo $$(find packaging/dbscripts \( -name '*.scripts.md5' -or -name '*.schema' -or -name '*.log' \)))"

	if [ -n "$(WILDFLY_OVERLAY_MODULES)" ]; then \
		echo "ENGINE_JAVA_MODULEPATH=\"$(WILDFLY_OVERLAY_MODULES):\$${ENGINE_JAVA_MODULEPATH}\"" > "$(DESTDIR)$(PKG_SYSCONF_DIR)/engine.conf.d/20-setup-jboss-overlay.conf"; \
	fi

install-brands:
	install -dm 0755 "$(DESTDIR)$(DATA_DIR)/brands/ovirt.brand"; \
	find "$(MAVEN_OUTPUT_DIR)" -name 'ovirt-*-brand.zip' | grep -v tmp.repos | xargs -r -n 1 unzip -q -o -d "$(DESTDIR)$(DATA_DIR)/brands/ovirt.brand"

install-gwt-symbols:
	install -d -m 0755 "$(DESTDIR)$(DATA_DIR)/gwt-symbols/webadmin"; \
	find "$(MAVEN_OUTPUT_DIR)" -name "webadmin-*-gwt-symbols.zip" -type f | grep -v tmp.repos | head -n 1 | xargs -I{} cp {} "$(DESTDIR)$(DATA_DIR)/gwt-symbols/webadmin/symbolMaps.zip"; \

install-layout: \
		install-packaging-files \
		install-brands \
		install-gwt-symbols \
		$(NULL)

	install -d -m 755 "$(DESTDIR)$(BIN_DIR)"
	ln -sf "$(DATA_DIR)/setup/bin/ovirt-engine-setup" "$(DESTDIR)$(BIN_DIR)/engine-setup"
	ln -sf "$(DATA_DIR)/setup/bin/ovirt-engine-remove" "$(DESTDIR)$(BIN_DIR)/engine-cleanup"
	ln -sf "$(DATA_DIR)/setup/bin/ovirt-engine-upgrade-check" "$(DESTDIR)$(BIN_DIR)/engine-upgrade-check"
	ln -sf "$(DATA_DIR)/bin/engine-config.sh" "$(DESTDIR)$(BIN_DIR)/engine-config"
	ln -sf "$(DATA_DIR)/bin/engine-backup.sh" "$(DESTDIR)$(BIN_DIR)/engine-backup"
	ln -sf "$(DATA_DIR)/bin/engine-vacuum.sh" "$(DESTDIR)$(BIN_DIR)/engine-vacuum"
	ln -sf "$(DATA_DIR)/bin/ovirt-engine-extensions-tool.sh" "$(DESTDIR)$(BIN_DIR)/ovirt-engine-extensions-tool"
	ln -sf "$(DATA_DIR)/bin/ovirt-register-sso-client-tool.sh" "$(DESTDIR)$(BIN_DIR)/ovirt-register-sso-client-tool"

	install -d -m 755 "$(DESTDIR)$(PKG_PKI_DIR)/certs"
	install -d -m 755 "$(DESTDIR)$(PKG_PKI_DIR)/certs-qemu"
	install -d -m 755 "$(DESTDIR)$(PKG_PKI_DIR)/keys"
	install -d -m 750 "$(DESTDIR)$(PKG_PKI_DIR)/private"
	install -d -m 755 "$(DESTDIR)$(PKG_PKI_DIR)/requests"
	install -d -m 755 "$(DESTDIR)$(PKG_PKI_DIR)/requests-qemu"
	install -d -m 755 "$(DESTDIR)$(DATA_DIR)/ui-plugins"
	install -d -m 755 "$(DESTDIR)$(PKG_SYSCONF_DIR)/branding"
	install -d -m 750 "$(DESTDIR)$(PKG_STATE_DIR)"
	install -d -m 750 "$(DESTDIR)$(PKG_STATE_DIR)/backups"
	install -d -m 750 "$(DESTDIR)$(PKG_BACKUP_DEFAULT_DIR)"
	install -d -m 750 "$(DESTDIR)$(PKG_BACKUP_LOG_DEFAULT_DIR)"
	install -d -m 755 "$(DESTDIR)$(PKG_SYSCONF_DIR)/extensions.d"
	install -d -m 755 "$(DESTDIR)$(DATA_DIR)/extensions.d"

	install -d -m 0755 "$(DESTDIR)$(DATA_DIR)/files"
	-rm -f "$(DESTDIR)$(DATA_DIR)/files/usbfilter.txt"
	ln -s "$(PKG_SYSCONF_DIR)/usbfilter.txt" "$(DESTDIR)$(DATA_DIR)/files/usbfilter.txt"
	-rm -f "$(DESTDIR)$(DATA_DIR)/files/novnc"
	ln -s "/usr/share/novnc" "$(DESTDIR)$(DATA_DIR)/files/novnc"

	install -d -m 755 "$(DESTDIR)$(PKG_SYSCONF_DIR)/branding"
	-rm -f "$(DESTDIR)$(PKG_SYSCONF_DIR)/branding/00-ovirt.brand"
	ln -s "$(DATA_DIR)/brands/ovirt.brand" "$(DESTDIR)$(PKG_SYSCONF_DIR)/branding/00-ovirt.brand"

	ln -sf "$(DATA_DIR)/conf/osinfo-defaults.properties" "$(DESTDIR)$(PKG_SYSCONF_DIR)/osinfo.conf.d/00-defaults.properties"

	ln -sf "$(DATA_DIR)/conf/timezones-defaults.properties" "$(DESTDIR)$(PKG_SYSCONF_DIR)/timezones/00-defaults.properties"

gwt-debug:
	$(MVN) -pl "frontend/webadmin/modules/webadmin" \
		$(DEV_EXTRA_BUILD_FLAGS_GWT_DEFAULTS) \
		$(DEV_EXTRA_BUILD_FLAGS) \
		-Dgwt.noserver=true \
		-P $(GWT_DEBUG_PROFILES) \
		-P gwt-admin \
			$(GWT_DEBUG_TARGET)

all-dev:
	[ "$(DEV_REBUILD)" != 0 ] && rm -f "$(BUILD_FILE)" || :
	rm -f $(GENERATED)
	$(MAKE) \
		all \
		BUILD_DEV=1 \
		DEV_PYTHON_DIR="$(PREFIX)$(PYTHON_SYS_DIR)" \
		DEV_SETUP_ENV_DIR="$(PREFIX)/etc/ovirt-engine-setup.env.d" \
		IMAGEIO_SYSCONF_DIR="$(PREFIX)/etc/ovirt-imageio" \
		VMCONSOLE_SYSCONF_DIR="$(PREFIX)/etc/ovirt-vmconsole" \
		VMCONSOLE_PKI_DIR="$(PREFIX)/etc/pki/ovirt-vmconsole" \
		$(NULL)

install-dev:	\
		all-dev \
		$(NULL)

	# remove dbscripts to avoid dups
	rm -fr "$(DESTDIR)$(DATA_DIR)/dbscripts"

	if [ -f "$(DESTDIR)$(PREFIX)/dev.$(PACKAGE_NAME).flist" ]; then \
		cat "$(DESTDIR)$(PREFIX)/dev.$(PACKAGE_NAME).flist" | while read f; do \
			rm -f "$(DESTDIR)$(PREFIX)/$${f}"; \
		done; \
		rm -f "$(DESTDIR)$(PREFIX)/dev.$(PACKAGE_NAME).flist"; \
	fi

	rm -f tmp.dev.flist
	$(MAKE) \
		install \
		BUILD_DEV=1 \
		BUILD_VALIDATION=0 \
		PYTHON_DIR="$(PREFIX)$(PYTHON_SYS_DIR)" \
		DEV_FLIST=tmp.dev.flist \
		$(NULL)
	cp tmp.dev.flist "$(DESTDIR)$(PREFIX)/dev.$(PACKAGE_NAME).flist"

	install -d "$(DESTDIR)$(PKG_TMP_DIR)"
	install -d "$(DESTDIR)$(PKG_CACHE_DIR)"
	install -d "$(DESTDIR)$(PKG_STATE_DIR)/.ssh"
	install -d "$(DESTDIR)$(PKG_STATE_DIR)/content"
	install -d "$(DESTDIR)$(PKG_STATE_DIR)/setup/answers"
	install -d "$(DESTDIR)$(PKG_LOG_DIR)/host-deploy"
	install -d "$(DESTDIR)$(PKG_LOG_DIR)/setup"
	install -d "$(DESTDIR)$(PKG_LOG_DIR)/notifier"
	install -d "$(DESTDIR)$(PKG_LOG_DIR)/dump"
	install -d "$(DESTDIR)$(PKG_LOG_DIR)/ansible"
	install -d "$(DESTDIR)$(PKG_LOG_DIR)/ova"
	install -d "$(DESTDIR)$(PKG_LOG_DIR)/cinderlib"

	touch "$(DESTDIR)$(PKG_STATE_DIR)/.ssh/config"

	if [ -e "$(DESTDIR)$(PKG_STATE_DIR)/jboss_runtime/deployments" ]; then \
		touch "$(DESTDIR)$(PKG_STATE_DIR)/jboss_runtime/deployments/engine.ear.deployed"; \
	fi

