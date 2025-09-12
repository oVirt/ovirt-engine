#!/bin/bash -xe

# Mark current directory as safe for git to be able to parse git hash
git config --global --add safe.directory $(pwd)

# git hash of current commit passed from GitHub or HEAD
GIT_HASH=$(git rev-parse --short ${GITHUB_SHA:-HEAD})
SUFFIX=$(grep -E "<version"  pom.xml | head -n1 | awk -F '[<>]' '/version/{print $3}' | grep -q -- -SNAPSHOT && echo .git${GIT_HASH} || :)

# Directory, where build artifacts will be stored, should be passed as the 1st parameter
ARTIFACTS_DIR=${1:-exported-artifacts}

# GH RPM builds will be used only for OST so Firefox and Chrome are enough
# GWT build memory needs to be limited
EXTRA_BUILD_FLAGS=""
EXTRA_BUILD_FLAGS="${EXTRA_BUILD_FLAGS} --no-transfer-progress"
EXTRA_BUILD_FLAGS="${EXTRA_BUILD_FLAGS} -Dgwt.userAgent=gecko1_8,safari"
EXTRA_BUILD_FLAGS="${EXTRA_BUILD_FLAGS} -Dgwt.compiler.localWorkers=1"
EXTRA_BUILD_FLAGS="${EXTRA_BUILD_FLAGS} -Dgwt.jvmArgs='-Xms1G -Xmx3G'"

export MAVEN_OPTS="-Xms1G -Xmx2G"

[ -d ${ARTIFACTS_DIR} ] || mkdir -p ${ARTIFACTS_DIR}
[ -d rpmbuild/SOURCES ] || mkdir -p rpmbuild/SOURCES

make validations

# Get the tarball
make dist
mv *.tar.gz rpmbuild/SOURCES

# create the src.rpm
rpmbuild \
    -D "_topdir rpmbuild" \
    ${SUFFIX:+-D "release_suffix ${SUFFIX}"} \
    -ts rpmbuild/SOURCES/*.gz
