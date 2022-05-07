#!/bin/bash -xe

# git hash of current commit should be passed as the 1st paraameter
if [ "${GITHUB_SHA}" == "" ]; then
  GIT_HASH=$(git rev-list HEAD | wc -l)
else
  GIT_HASH=$(git rev-parse --short $GITHUB_SHA)
fi

# Directory, where build artifacts will be stored, should be passed as the 1st parameter
ARTIFACTS_DIR=${1:-exported-artifacts}

# Prepare the version string (with support for SNAPSHOT versioning)
VERSION=$(mvn help:evaluate  -q -DforceStdout -Dexpression=project.version)
VERSION=${VERSION/-SNAPSHOT/-0.${GIT_HASH}.$(date +%04Y%02m%02d%02H%02M)}
IFS='-' read -ra VERSION <<< "$VERSION"
RELEASE=${VERSION[1]-1}
MILESTONE=master

# GH RPM builds will be used only for OST so Firefox and Chrome are enough
# GWT build memory needs to be limited
EXTRA_BUILD_FLAGS=""
EXTRA_BUILD_FLAGS="${EXTRA_BUILD_FLAGS} --no-transfer-progress"
EXTRA_BUILD_FLAGS="${EXTRA_BUILD_FLAGS} -Dgwt.userAgent=gecko1_8,safari"
EXTRA_BUILD_FLAGS="${EXTRA_BUILD_FLAGS} -Dgwt.compiler.localWorkers=1"
EXTRA_BUILD_FLAGS="${EXTRA_BUILD_FLAGS} -Dgwt.jvmArgs='-Xms1G -Xmx3G'"

export MAVEN_OPTS="-Xms1G -Xmx2G"

# Set the location of the JDK that will be used for compilation:
export JAVA_HOME="${JAVA_HOME:=/usr/lib/jvm/java-11}"

[ -d ${ARTIFACTS_DIR} ] || mkdir -p ${ARTIFACTS_DIR}
[ -d rpmbuild/SOURCES ] || mkdir -p rpmbuild/SOURCES

make validations

# Get the tarball
make dist
mv *.tar.gz rpmbuild/SOURCES

# create the src.rpm
rpmbuild \
    -D "_topdir rpmbuild" \
    -ts rpmbuild/SOURCES/*.gz
