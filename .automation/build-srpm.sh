#!/bin/bash -xe

# Building srpm for copr?
copr_build=0

# Process options
while getopts ":c" options; do
    case $options in
        c)  copr_build=1;;
        \?)
            echo "Error: Invalid option"
            exit;;
    esac
done

# Ensure the build validation passes
make validations

# git hash of the HEAD commit (GH action may be the PR merge commit)
GIT_HASH=$(git rev-parse --short HEAD)

# Prepare the SNAPSHOT release suffix if a MILESTONE is available
SNAPSHOT_SUFFIX=
if [[ "$(make -f version.mak print-MILESTONE)" != "MILESTONE=" ]]; then
  SNAPSHOT_SUFFIX=".git${GIT_HASH}"
fi
export SNAPSHOT_SUFFIX

# For a copr snapshot build, burn in the release_suffix as -D options are not preserved when rebuilding from srpm
if [[ ${copr_build} -eq 1 && "${SNAPSHOT_SUFFIX}" != "" ]]; then
  sed "s:%{?release_suffix}:${SNAPSHOT_SUFFIX}:" -i ovirt-engine.spec.in
fi

# Create RPM build directories
export top_dir="${PWD}/rpmbuild"
test -d "${top_dir}" && rm -rf "${top_dir}" || :
mkdir -p "${top_dir}/SOURCES"

# Get the tarball
make dist
mv *.tar.gz ${top_dir}/SOURCES

# Create the src.rpm
rpmbuild \
    -D "_topdir ${top_dir}" \
    ${SNAPSHOT_SUFFIX:+-D "release_suffix ${SNAPSHOT_SUFFIX}"} \
    -ts ${top_dir}/SOURCES/*.gz
