#!/bin/bash -xe

source automation/jvm-opts.sh

MAVEN_OPTS="$MAVEN_OPTS $JVM_MEM_OPTS"
export MAVEN_OPTS

SUFFIX=".git$(git rev-parse --short HEAD)"

if [ -d /root/.m2/repository/org/ovirt ]; then
    echo "Deleting ovirt folder from maven cache"
    rm -rf /root/.m2/repository/org/ovirt
fi

MAVEN_SETTINGS="/etc/maven/settings.xml"

export BUILD_JAVA_OPTS_MAVEN="\
    -Dgwt.compiler.localWorkers=1 \
    -Dovirt.surefire.reportsDirectory=${PWD}/exported-artifacts/tests \
"

# build permutations for chrome and firefox
export EXTRA_BUILD_FLAGS="-gs $MAVEN_SETTINGS \
    -D gwt.userAgent=gecko1_8,safari \
"

export BUILD_JAVA_OPTS_GWT="$JVM_MEM_OPTS"

# Set the location of the JDK that will be used for compilation:
export JAVA_HOME="${JAVA_HOME:=/usr/lib/jvm/java-1.8.0}"

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
# remove any previous artifacts
rm -rf output
rm -f ./*tar.gz
rm -rf exported-artifacts
mkdir -p exported-artifacts/tests
make clean \
    "EXTRA_BUILD_FLAGS=$EXTRA_BUILD_FLAGS"

# Get the tarball
make dist

# create the src.rpm
rpmbuild \
    -D "_srcrpmdir $PWD/output" \
    -D "_topmdir $PWD/rpmbuild" \
    -D "release_suffix ${SUFFIX}" \
    -D "ovirt_build_extra_flags $EXTRA_BUILD_FLAGS" \
    -ts ./*.gz

# install any build requirements
yum-builddep output/*src.rpm

# build minimal rpms for CI
rpmbuild \
    -D "_rpmdir $PWD/output" \
    -D "_topmdir $PWD/rpmbuild" \
    -D "release_suffix ${SUFFIX}" \
    -D "ovirt_build_ut 0" \
    -D "ovirt_build_all_user_agents 0" \
    -D "ovirt_build_locales 0" \
    -D "ovirt_build_extra_flags $EXTRA_BUILD_FLAGS" \
    --rebuild output/*.src.rpm

# Store any relevant artifacts in exported-artifacts for the ci system to
# archive
find output -iname \*rpm -exec mv "{}" exported-artifacts/ \;
mv ./*tar.gz exported-artifacts/

# Rename junit surefire reports to match jenkins report plugin
# Error code 4 means nothing changed, ignore it
rename .xml .junit.xml exported-artifacts/tests/* ||  [[ $? -eq 4 ]]
