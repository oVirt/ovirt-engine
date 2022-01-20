#!/bin/bash -xe

# git hash of current commit should be passed as the 1st paraameter
if [ "${GITHUB_SHA}" == "" ]; then
  GIT_HASH=$(git rev-list HEAD | wc -l)
else
  GIT_HASH=$(git rev-parse --short $GITHUB_SHA)
fi

# Directory, where build artifacts will be stored, should be passed as the 1st parameter
ARTIFACTS_DIR=${1:-exported-artifacts}

# Get the MILESTONE from version.mak, so that we know if we build for a
# non-release milestone, such as master, alpha, beta, etc., or for a
# release (and then MILESTONE is empty).
# Do this methodically, by generating a shell script snippet using make
# and writing it there, instead of parsing version.mak ourselves.
make generated-files
. source $(dirname "$(readlink -f "$0")")/../automation/milestone-config.sh

# Set SUFFIX only for MILESTONEs
SUFFIX=
[ -n "${MILESTONE}" ] && SUFFIX=".git${GIT_HASH}"

source automation/jvm-opts.sh

MAVEN_OPTS="$MAVEN_OPTS $JVM_MEM_OPTS"
export MAVEN_OPTS


export BUILD_JAVA_OPTS_MAVEN="\
    -Dgwt.compiler.localWorkers=1 \
    -Dovirt.surefire.reportsDirectory=${ARTIFACTS_DIR}/tests \
"

# For milestone (non-release) master builds, build permutations for chrome and firefox
if [ -n "${MILESTONE}" ] && [ "${MILESTONE}" == "master" ]; then
	export EXTRA_BUILD_FLAGS="-D gwt.userAgent=gecko1_8,safari"
else
	export EXTRA_BUILD_FLAGS=""
fi

export BUILD_JAVA_OPTS_GWT="$JVM_MEM_OPTS"

# Set the location of the JDK that will be used for compilation:
export JAVA_HOME="${JAVA_HOME:=/usr/lib/jvm/java-11}"

[ -d ${ARTIFACTS_DIR}/tests ] || mkdir -p ${ARTIFACTS_DIR}/tests

make clean \
    "EXTRA_BUILD_FLAGS=$EXTRA_BUILD_FLAGS"

# Get the tarball
make dist

# create the src.rpm
rpmbuild \
    -D "_topmdir rpmbuild" \
    ${SUFFIX:+-D "release_suffix ${SUFFIX}"} \
    -D "ovirt_build_extra_flags $EXTRA_BUILD_FLAGS" \
    -ts ./*.gz
