#!/bin/bash -xe

source automation/jvm-opts.sh

MAVEN_OPTS="$MAVEN_OPTS $JVM_MEM_OPTS"
export MAVEN_OPTS

BUILD_UT=0
RUN_DAO_TESTS=0

dao_tests_path1=backend/manager/modules/dal
dao_tests_path2=("dao_tests_path2=backend/manager/modules/common/src/main/"
                 "java/org/ovirt/engine/core/common/businessentities")

if git show --pretty="format:" --name-only | egrep \
    "(sql|$dao_tests_path1|${dao_tests_path2[0]}${dao_tests_path2[1]})" | \
    egrep -v -q "backend/manager/modules/dal/src/main/resources/bundles"; then
    RUN_DAO_TESTS=1
fi

SUFFIX=".git$(git rev-parse --short HEAD)"

if [ -d /root/.m2/repository/org/ovirt ]; then
    echo "Deleting ovirt folder from maven cache"
    rm -rf /root/.m2/repository/org/ovirt
fi

MAVEN_SETTINGS="/etc/maven/settings.xml"
export BUILD_JAVA_OPTS_MAVEN="\
    -Dgwt.compiler.localWorkers=1 \
"
export EXTRA_BUILD_FLAGS="-gs $MAVEN_SETTINGS"
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

# Run Dao tests
if [[ $RUN_DAO_TESTS -eq 1 ]]; then
    automation/dao-tests.sh "$EXTRA_BUILD_FLAGS"
fi

# remove any previous artifacts
rm -rf output
rm -f ./*tar.gz
make clean \
    "EXTRA_BUILD_FLAGS=$EXTRA_BUILD_FLAGS"

# CentOS 8 contains maven 3.5.4, which is known to work, but on earlier maven
# releases we got false positives.
source automation/spotbugs.sh

# Get the tarball
make dist

# create the src.rpm
rpmbuild \
    -D "_srcrpmdir $PWD/output" \
    -D "_topmdir $PWD/rpmbuild" \
    -D "release_suffix ${SUFFIX}" \
    -D "ovirt_build_extra_flags $EXTRA_BUILD_FLAGS" \
    -D "ovirt_build_draft 1" \
    -ts ./*.gz

# install any build requirements
yum-builddep output/*src.rpm

# create the rpms
rpmbuild \
    -D "_rpmdir $PWD/output" \
    -D "_topmdir $PWD/rpmbuild" \
    -D "release_suffix ${SUFFIX}" \
    -D "ovirt_build_ut $BUILD_UT" \
    -D "ovirt_build_extra_flags $EXTRA_BUILD_FLAGS" \
    -D "ovirt_build_draft 1" \
    --rebuild output/*.src.rpm

# Store any relevant artifacts in exported-artifacts for the ci system to
# archive
[[ -d exported-artifacts ]] || mkdir -p exported-artifacts

# Move find bugs to a dedicated directory under exported-artifacts
mkdir -p exported-artifacts/find-bugs
find * -name "*spotbugs.xml" -o -name "spotbugsXml.xml" | \
    while read source_file; do
        destination_file=$(
            sed -e 's#/#-#g' -e 's#\(.*\)-#\1.#' <<< "$source_file"
        )
        mv $source_file exported-artifacts/find-bugs/"$destination_file"
    done
