#!/bin/bash -xe

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

if [ -d /root/.m2/repository/org/ovirt ]; then
    echo "Deleting ovirt folder from maven cache"
    rm -rf /root/.m2/repository/org/ovirt
fi

MAVEN_SETTINGS="/etc/maven/settings.xml"

export BUILD_JAVA_OPTS_MAVEN="\
    -Dgwt.compiler.localWorkers=1 \
    -Dovirt.surefire.reportsDirectory=${PWD}/exported-artifacts/tests \
"

# For milestone (non-release) master builds, build permutations for chrome and firefox
if [ -n "${MILESTONE}" ] && [ "${MILESTONE}" == "master" ]; then
	export EXTRA_BUILD_FLAGS="-gs $MAVEN_SETTINGS \
	    -D gwt.userAgent=gecko1_8,safari \
	"
else
	export EXTRA_BUILD_FLAGS="-gs $MAVEN_SETTINGS"
fi

export BUILD_JAVA_OPTS_GWT="$JVM_MEM_OPTS"

# Set the location of the JDK that will be used for compilation:
export JAVA_HOME="${JAVA_HOME:=/usr/lib/jvm/java-11}"

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
# remove any previous artifacts
rm -rf output
rm -f ./*tar.gz
rm -rf exported-artifacts
mkdir -p exported-artifacts/tests
make clean \
    "EXTRA_BUILD_FLAGS=$EXTRA_BUILD_FLAGS"

# Get the tarball
make dist

# create the src.rpm
rpmbuild \
    -D "_srcrpmdir $PWD/output" \
    -D "_topmdir $PWD/rpmbuild" \
    ${SUFFIX:+-D "release_suffix ${SUFFIX}"} \
    -D "ovirt_build_extra_flags $EXTRA_BUILD_FLAGS" \
    -ts ./*.gz

# install any build requirements
yum-builddep output/*src.rpm

# build minimal rpms for CI, fuller ones for releases
BUILD_UT=0
BUILD_ALL_USER_AGENTS=0
BUILD_LOCALES=0

if [ -z "${MILESTONE}" ] || { [ -n "${MILESTONE}" ] && [ "${MILESTONE}" != "master" ]; }; then
	BUILD_UT=1
	BUILD_ALL_USER_AGENTS=1
	BUILD_LOCALES=1
fi

rpmbuild \
    -D "_rpmdir $PWD/output" \
    -D "_topmdir $PWD/rpmbuild" \
    ${SUFFIX:+-D "release_suffix ${SUFFIX}"} \
    -D "ovirt_build_ut ${BUILD_UT}" \
    -D "ovirt_build_all_user_agents ${BUILD_ALL_USER_AGENTS}" \
    -D "ovirt_build_locales ${BUILD_LOCALES}" \
    -D "ovirt_build_extra_flags $EXTRA_BUILD_FLAGS" \
    --rebuild output/*.src.rpm

# Store any relevant artifacts in exported-artifacts for the ci system to
# archive
find output -iname \*rpm -exec mv "{}" exported-artifacts/ \;
mv ./*tar.gz exported-artifacts/

# Rename junit surefire reports to match jenkins report plugin
# Error code 4 means nothing changed, ignore it
rename .xml .junit.xml exported-artifacts/tests/* ||  [[ $? -eq 4 ]]
