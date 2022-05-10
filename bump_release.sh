#!/bin/bash -xe

if [ -z "${RPM_PACKAGER}" ] ; then
    echo 'Please export RPM_PACKAGER="Name Lastname <email@address.com>'
    exit 1
fi

# Set pom to build final release
find . -name pom.xml -exec gsed -i "s:-SNAPSHOT::" {} +

# Get current ovirt-engine version
VERSION="$(grep -E "<version"  pom.xml | head -n1 | awk -F '[<>]' '/version/{print $3}')"

# Prepare changelog
CHANGELOG="* $(LC_ALL=C date "+%a %b %d %Y") ${RPM_PACKAGER} - ${VERSION}\n- Bump version to ${VERSION}\n"
export CHANGELOG

# Add changelog to the spec file
gsed -i "/^%changelog/a ${CHANGELOG}" ovirt-engine.spec.in

# Adjust copr build config for releasing
patch -p0 --ignore-whitespace .copr/Makefile <<'__EOF__'
diff --git a/.copr/Makefile b/.copr/Makefile
index 51e3299e3e5..b2d4a195740 100644
--- a/.copr/Makefile
+++ b/.copr/Makefile
@@ -4,9 +4,9 @@ installdeps:
        dnf -y install git

 srpm: installdeps
-       $(eval SUFFIX=$(shell sh -c "echo '.git$$(git rev-parse --short HEAD)'"))
+       # $(eval SUFFIX=$(shell sh -c "echo '.git$$(git rev-parse --short HEAD)'"))
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

# Restore the -SNAPSHOT preserving latest changelog
git show |patch -p 1 -R
git checkout -- ovirt-engine.spec.in

# Bump to next version
NEXT_VERSION="$(echo ${VERSION} | awk -F. -v OFS=. 'NF==1{print ++$NF}; NF>1{if(length($NF+1)>length($NF))$(NF-1)++; $NF=sprintf("%0*d", length($NF), ($NF+1)%(10^length($NF))); print}')"
export NEXT_VERSION
find . -name pom.xml -exec gsed -i "s:${VERSION}-SNAPSHOT:${NEXT_VERSION}-SNAPSHOT:" {} +

# commit
git add -u
git commit -s --message="build: post ovirt-engine-${VERSION}"
