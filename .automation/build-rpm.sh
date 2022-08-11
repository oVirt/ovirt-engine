#!/bin/bash -xe

# Directory, where build artifacts will be stored, should be passed as '-a' parameter
ARTIFACTS_DIR=exported-artifacts

# Process options
while getopts ":a:" options; do
    case $options in
        a)  ARTIFACTS_DIR=$OPTARG;;
        \?)
            echo "Error: Invalid option"
            exit;;
    esac
done

# Build source packages
source $(dirname "$(readlink -f "$0")")/build-srpm.sh

# GWT build memory needs to be limited
EXTRA_BUILD_FLAGS="--no-transfer-progress"
EXTRA_BUILD_FLAGS="${EXTRA_BUILD_FLAGS} -Dgwt.compiler.localWorkers=1"
EXTRA_BUILD_FLAGS="${EXTRA_BUILD_FLAGS} -Dgwt.jvmArgs='-Xms1G -Xmx3G'"

# Maven memory needs to be limited
export MAVEN_OPTS="-Xms1G -Xmx2G"

# Set the location of the JDK that will be used for compilation
export JAVA_HOME="${JAVA_HOME:=/usr/lib/jvm/java-11}"

# Install build dependencies
if [[ $(id -u) -ne 0 ]]; then
    sudo dnf builddep -y ${top_dir}/SRPMS/*src.rpm
else
    dnf builddep -y ${top_dir}/SRPMS/*src.rpm
fi

# Build binary package with the minimal build. GH RPM builds
# will be used only for OST so Firefox and Chrome are enough.
rpmbuild \
    -D "_topdir ${top_dir}" \
    -D "release_suffix ${SNAPSHOT_SUFFIX}" \
    -D "ovirt_build_extra_flags ${EXTRA_BUILD_FLAGS}" \
    --with ovirt_build_minimal \
    --with ovirt_build_ut \
    --rebuild ${top_dir}/SRPMS/*src.rpm

# Move RPMs to exported artifacts
[[ -d ${ARTIFACTS_DIR} ]] || mkdir -p ${ARTIFACTS_DIR}
find ${top_dir} -iname \*rpm -print0 | xargs -0 mv -t ${ARTIFACTS_DIR}
