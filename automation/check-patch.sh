#!/bin/bash -xe

PACKAGER=dnf
export PACKAGER
ARCH="$(rpm --eval "%_arch")"

on_exit() {
    ${PACKAGER} --verbose clean all
}

trap on_exit EXIT

${PACKAGER} update -y

# Get the MILESTONE from version.mak, so that we know if we build for a
# non-release milestone, such as master, alpha, beta, etc., or for a
# release (and then MILESTONE is empty).
# Do this methodically, by generating a shell script snippet using make
# and writing it there, instead of parsing version.mak ourselves.
make generated-files
. automation/milestone-config.sh

# Set SUFFIX only for MILESTONEs
SUFFIX=
[ -n "${MILESTONE}" ] && SUFFIX=".git$(git rev-parse --short HEAD)"

source automation/jvm-opts.sh

MAVEN_OPTS="$MAVEN_OPTS $JVM_MEM_OPTS"
export MAVEN_OPTS

BUILD_UT=1
RUN_DAO_TESTS=0

if [ -z "${MILESTONE}" ] || { [ -n "${MILESTONE}" ] && [ "${MILESTONE}" != "master" ]; }; then
	RUN_DAO_TESTS=1
fi

# Check asciidoc documentation
for doc in $(find . -name "*.adoc"); do
	echo "Checking asciidoc ${doc}"
	asciidoc "${doc}"
done

# Check for copyright notices in files that do not also include an SPDX tag.
non_removed_files=$( \
	git show --pretty="format:" --name-status | \
	awk '/^[^D]/ {print $2}' \
)

copyright_notices_files=
[ -n "${non_removed_files}" ] && copyright_notices_files=$( \
	echo "${non_removed_files}" | \
	xargs grep -il 'Copyright.*Red Hat' \
) || true

copyright_notices_no_spdx_files=
[ -n "${copyright_notices_files}" ] && copyright_notices_no_spdx_files=$( \
	echo "${copyright_notices_files}" | \
	xargs grep -iL 'SPDX' \
) || true

if [ -n "${copyright_notices_no_spdx_files}" ]; then
	cat << __EOF__
[ERROR] : The following file(s) contain copyright/license notices, and do not contain an SPDX tag:
============================================================
${copyright_notices_no_spdx_files}
============================================================
Please replace the notices with an SPDX tag. How exactly to do this is language/syntax specific. You should include the following two lines in a comment:
============================================================
Copyright oVirt Authors
SPDX-License-Identifier: Apache-2.0
============================================================
__EOF__
	exit 1
fi

# Check for DB upgrade scripts modifications without the
# "Allow-db-upgrade-script-changes:Yes" in the patch header

UPGRADE_DIR="packaging/dbscripts/upgrade/"
SCRIPT_EXPR="[0-9]{2}_[0-9]{2}_[0-9]{4}[a-zA-Z0-9_-]*\.sql"
KEYWORD="Allow-db-upgrade-script-changes: Yes"

if git show --pretty="format:" --name-status | egrep -q "^M.${UPGRADE_DIR}${SCRIPT_EXPR}" ; then
    if ! git log -1 --pretty=format:%b | grep -E -iq "${KEYWORD}"; then
        echo "[ERROR] : Changing an existing upgrade script is dangerous and can cause upgrade problems,
            if you are doing that in purpose and sure that there is no other way to fix the issue you
            are working on, please add the key '${KEYWORD}' to your patch commit message."
        exit 1
    fi
fi

common_modules_paths=("backend/manager/modules/searchbackend/" \
                      "backend/manager/modules/common/" \
                      "backend/manager/modules/compat/" )

#create a search string with OR on all paths
common_modules_search_string=$(IFS='|'; echo "${common_modules_paths[*]}")



dao_tests_paths=("backend/manager/modules/dal" \
                 "backend/manager/modules/searchbackend/" \
                 "java/org/ovirt/engine/core/common/businessentities" )

#create a search string with OR on all paths
dao_tests_paths_search_string=$(IFS='|'; echo "${dao_tests_paths[*]}")

if git show --pretty="format:" --name-only | egrep \
    "(sql|${dao_tests_paths_search_string})" | \
    egrep -v -q "backend/manager/modules/dal/src/main/resources/bundles"; then
    RUN_DAO_TESTS=1
fi

if [ -d /root/.m2/repository/org/ovirt ]; then
    echo "Deleting ovirt folder from maven cache"
    rm -rf /root/.m2/repository/org/ovirt
fi

MAVEN_SETTINGS="/etc/maven/settings.xml"
export BUILD_JAVA_OPTS_MAVEN="\
    -Dgwt.compiler.localWorkers=1 \
"
export EXTRA_BUILD_FLAGS="-gs $MAVEN_SETTINGS \
    -Dovirt.surefire.reportsDirectory=${PWD}/exported-artifacts/tests \
"
export BUILD_JAVA_OPTS_GWT="$JVM_MEM_OPTS"

# Set the location of the JDK that will be used for compilation:
export JAVA_HOME="${JAVA_HOME:=/usr/lib/jvm/java-11-openjdk}"

# Use ovirt mirror if able, fall back to central maven
mkdir -p "${MAVEN_SETTINGS%/*}"
cat >"$MAVEN_SETTINGS" <<EOS
<?xml version="1.0"?>
<settings xmlns="http://maven.apache.org/POM/4.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
          http://maven.apache.org/xsd/settings-1.0.0.xsd">

<mirrors>
        <mirror>
                <id>ovirt-maven-repository</id>
                <name>oVirt artifactory proxy</name>
                <url>http://artifactory.ovirt.org/artifactory/ovirt-mirror</url>
                <mirrorOf>*</mirrorOf>
        </mirror>
        <mirror>
                <id>root-maven-repository</id>
                <name>Official maven repo</name>
                <url>http://repo.maven.apache.org/maven2</url>
                <mirrorOf>*</mirrorOf>
        </mirror>
</mirrors>
</settings>
EOS

# Run Dao tests
if [[ $RUN_DAO_TESTS -eq 1 ]]; then
    automation/dao-tests.sh "$EXTRA_BUILD_FLAGS"
fi

# remove any previous artifacts
rm -rf output
rm -f ./*tar.gz
rm -rf exported-artifacts
mkdir -p exported-artifacts/tests

make clean \
    "EXTRA_BUILD_FLAGS=$EXTRA_BUILD_FLAGS"

# execute packaging tests
automation/packaging-setup-tests.sh
automation/packaging-pythonlib-tests.sh

# perform quick validations
make validations

# CentOS 8 contains maven 3.5.4, which is known to work, but on earlier maven
# releases we got false positives.
source automation/spotbugs.sh

if [ -n "${MILESTONE}" ] && [ "${MILESTONE}" == "master" ]; then
    OVIRT_BUILD_QUICK=1
fi

# Get the tarball
make dist

# create the src.rpm
rpmbuild \
    -D "_srcrpmdir $PWD/output" \
    -D "_topmdir $PWD/rpmbuild" \
    ${SUFFIX:+-D "release_suffix ${SUFFIX}"} \
    -D "ovirt_build_extra_flags $EXTRA_BUILD_FLAGS" \
    ${OVIRT_BUILD_QUICK:+-D "ovirt_build_quick 1"} \
    -ts ./*.gz

# install any build requirements
yum-builddep output/*src.rpm

# create the rpms
rpmbuild \
    -D "_rpmdir $PWD/output" \
    -D "_topmdir $PWD/rpmbuild" \
    ${SUFFIX:+-D "release_suffix ${SUFFIX}"} \
    -D "ovirt_build_ut $BUILD_UT" \
    -D "ovirt_build_extra_flags $EXTRA_BUILD_FLAGS" \
    ${OVIRT_BUILD_QUICK:+-D "ovirt_build_draft 1"} \
    --rebuild output/*.src.rpm

# Store any relevant artifacts in exported-artifacts for the ci system to
# archive
[[ -d exported-artifacts ]] || mkdir -p exported-artifacts

# Move any relevant artifacts to exported-artifacts for the ci system to
# archive
find output -iname \*rpm -exec mv "{}" exported-artifacts/ \;


# Collect any mvn spotbugs artifacts
mkdir -p exported-artifacts/find-bugs
find * -name "*spotbugs.xml" -o -name "spotbugsXml.xml" | \
    while read source_file; do
        destination_file=$(
            sed -e 's#/#-#g' -e 's#\(.*\)-#\1.#' <<< "$source_file"
        )
        mv $source_file exported-artifacts/find-bugs/"$destination_file"
    done
mv ./*tar.gz exported-artifacts/

# Rename junit surefire reports to match jenkins report plugin
# Error code 4 means nothing changed, ignore it
rename .xml .junit.xml exported-artifacts/tests/* ||  [[ $? -eq 4 ]]

if git show --name-only | grep ovirt-engine.spec.in; then
pushd exported-artifacts
    #Restoring sane yum environment
    rm -f /etc/yum.conf
    ${PACKAGER} reinstall -y system-release ${PACKAGER}
    [[ -d /etc/dnf ]] && [[ -x /usr/bin/dnf ]] && dnf -y reinstall dnf-conf
    [[ -d /etc/dnf ]] && sed -i -re 's#^(reposdir *= *).*$#\1/etc/yum.repos.d#' '/etc/dnf/dnf.conf'
    [[ -e /etc/dnf/dnf.conf ]] && echo "deltarpm=False" >> /etc/dnf/dnf.conf
    rm -f /etc/yum/yum.conf

    ${PACKAGER} repolist enabled
    ${PACKAGER} clean all
    ${PACKAGER} install -y http://resources.ovirt.org/pub/yum-repo/ovirt-release-master.rpm

    if [[ "$(rpm --eval "%dist")" == ".el8" ]]; then
        ${PACKAGER} module reset postgresql
        ${PACKAGER} module enable pki-deps javapackages-tools postgresql:12
        ${PACKAGER} --downloadonly install *noarch.rpm
        if [[ "${ARCH}" == "x86_64" ]]; then
            echo "Reference installation from ovirt-release repo."
            ${PACKAGER} --downloadonly install ovirt-engine ovirt-engine-setup-plugin-websocket-proxy || true
        fi
    else
        if [[ $(${PACKAGER} repolist enabled|grep -v ovirt|grep epel) ]] ; then
            ${PACKAGER} --downloadonly --disablerepo=epel install *noarch.rpm
            if [[ "${ARCH}" == "x86_64" ]]; then
                ${PACKAGER} --downloadonly --disablerepo=epel install ovirt-engine ovirt-engine-setup-plugin-websocket-proxy
            fi
        else
            ${PACKAGER} --downloadonly install *noarch.rpm
            if [[ "${ARCH}" == "x86_64" ]]; then
                ${PACKAGER} --downloadonly install ovirt-engine ovirt-engine-setup-plugin-websocket-proxy
            fi
        fi
    fi
popd
fi
