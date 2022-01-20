#!/bin/bash -xe

source $(dirname "$(readlink -f "$0")")/build-srpm.sh

# Install build dependencies
dnf builddep -y rpmbuild/SRPMS/*src.rpm

# build minimal rpms for CI, fuller ones for releases
BUILD_UT=0
BUILD_ALL_USER_AGENTS=0
BUILD_LOCALES=0

if [ -z "${MILESTONE}" ] || { [ -n "${MILESTONE}" ] && [ "${MILESTONE}" != "master" ]; }; then
	BUILD_UT=1
	BUILD_ALL_USER_AGENTS=1
	BUILD_LOCALES=1
fi

# Build binary package
rpmbuild \
    --define "_topmdir rpmbuild" \
    --define "_rpmdir rpmbuild" \
    ${SUFFIX:+-D "release_suffix ${SUFFIX}"} \
    -D "ovirt_build_ut ${BUILD_UT}" \
    -D "ovirt_build_all_user_agents ${BUILD_ALL_USER_AGENTS}" \
    -D "ovirt_build_locales ${BUILD_LOCALES}" \
    -D "ovirt_build_extra_flags $EXTRA_BUILD_FLAGS" \
    --rebuild rpmbuild/SRPMS/*src.rpm

# Move RPMs to exported artifacts
[[ -d $ARTIFACTS_DIR ]] || mkdir -p $ARTIFACTS_DIR
find rpmbuild -iname \*rpm | xargs mv -t $ARTIFACTS_DIR
