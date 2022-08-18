#!/bin/bash -xe

# Usage:
#
# RPM_PACKAGER='Name Lastname <email@address.com>' ./bump_release.sh
#
# The built version is always taken from current pom.xml, only removing -SNAPSHOT
#
# You can override the _next_ version. E.g. if you are on 4.6.8-SNAPSHOT and
# intend to release 4.6.8 and then branch it to ovirt-engine-4.6, bumping master
# to 4.7, you can do:
#
# NEXT_VERSION=4.7 RPM_PACKAGER='Name Lastname <email@address.com>' ./bump_release.sh

if [ -z "${RPM_PACKAGER}" ] ; then
    echo 'Please export RPM_PACKAGER="Name Lastname <email@address.com>'
    exit 1
fi

# Set pom to build final release
find . -name pom.xml -exec sed -i "s:-SNAPSHOT::" {} +

# Get current ovirt-engine version
VERSION="$(grep -E "<version"  pom.xml | head -n1 | awk -F '[<>]' '/version/{print $3}')"

# Prepare changelog
CHANGELOG="* $(LC_ALL=C date "+%a %b %d %Y") ${RPM_PACKAGER} - ${VERSION}\n- Bump version to ${VERSION}\n"
export CHANGELOG

# Add changelog to the spec file
sed -i "/^%changelog/a ${CHANGELOG}" ovirt-engine.spec.in

# Adjust copr build config for releasing
patch -p0 --ignore-whitespace .copr/Makefile <<'__EOF__'
diff --git a/.copr/Makefile b/.copr/Makefile
index 51e3299e3e5..b2d4a195740 100644
--- a/.copr/Makefile
+++ b/.copr/Makefile
@@ -9,9 +9,9 @@ git_cfg_safe:
        git config --global --add safe.directory "$(shell pwd)"

 srpm: installdeps git_cfg_safe
-       $(eval SUFFIX=.git$(shell git rev-parse --short HEAD))
+       # $(eval SUFFIX=.git$(shell git rev-parse --short HEAD))
        # changing the spec file as passing -D won't preserve the suffix when rebuilding in mock
-       sed "s:%{?release_suffix}:${SUFFIX}:" -i ovirt-engine.spec.in
+       # sed "s:%{?release_suffix}:${SUFFIX}:" -i ovirt-engine.spec.in
        mkdir -p tmp.repos/SOURCES
        make dist
        rpmbuild \
__EOF__

# commit
git add -u
git commit -s --message="build: ovirt-engine-${VERSION}"

TAG="ovirt-engine-${VERSION}"
git tag "${TAG}"

# Restore the -SNAPSHOT preserving latest changelog
git show |patch -p 1 -R
git checkout -- ovirt-engine.spec.in

# Bump to next version
if [ -z "${NEXT_VERSION}" ]; then
	NEXT_VERSION="$(echo ${VERSION} | awk -F. '{for (i=1; i<NF; i++) res=res $i "."; res=res $NF+1; print res}')"
	MSG="build: post ovirt-engine-${VERSION}"
else
	# If we did get NEXT_VERSION, we want the commit message to be
	# post PREVIOUS
	# where PREVIOUS only has the number of components as the NEXT VERSION.
	# e.g. bumping from 4.6.8 to 4.7 should say "post 4.6", not "post 4.6.8".
	# This isn't just cosmetic, it's also significant - because we are quite
	# likely to branch a 4.6 branch from the 4.6.8 tag, and there we'll want
	# a commit 'post 4.6.8'.
	PREVIOUS_VERSION="$(echo ${NEXT_VERSION} | awk -F. '{for (i=1; i<NF; i++) res=res $i "."; res=res $NF-1; print res}')"
	MSG="build: post ovirt-engine-${PREVIOUS_VERSION}"
fi

export NEXT_VERSION
find . -name pom.xml -exec sed -i "s:${VERSION}-SNAPSHOT:${NEXT_VERSION}-SNAPSHOT:" {} +

# commit
git add -u
git commit -s --message="${MSG}"
