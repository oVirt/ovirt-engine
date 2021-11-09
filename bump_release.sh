#!/bin/bash -xe

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

# commit
git add -u
git commit -s --message="build: ovirt-engine-${VERSION}"

# Restore the -SNAPSHOT preserving latest changelog
git show |patch -p 1 -R
git checkout -- ovirt-engine.spec.in

# Bump to next version
NEXT_VERSION="$(echo ${VERSION} | awk -F. -v OFS=. 'NF==1{print ++$NF}; NF>1{if(length($NF+1)>length($NF))$(NF-1)++; $NF=sprintf("%0*d", length($NF), ($NF+1)%(10^length($NF))); print}')"
export NEXT_VERSION
find . -name pom.xml -exec sed -i "s:${VERSION}-SNAPSHOT:${NEXT_VERSION}-SNAPSHOT:" {} +

# commit
git add -u
git commit -s --message="build: post ovirt-engine-${VERSION}"
