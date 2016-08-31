# TODO: log setup log and boot.log to stdout only
# TODO: fix gpg key warnings
# TODO: make sure unicode is used
# TODO: give ovirt user a well known gid and uid
# TODO: fix redirect from http to https (port is missing)

FROM centos:7

MAINTAINER Lev Veyde <lev@redhat.com>

# Build
ENV OVIRT_HOME=/home/ovirt/ovirt-engine
ENV VERSION=ovirt-engine-4.0

# Database
ENV POSTGRES_USER engine
ENV POSTGRES_PASSWORD engine
ENV POSTGRES_DB engine
ENV POSTGRES_HOST postgres
ENV POSTGRES_PORT 5432

# oVirt
ENV OVIRT_FQDN localhost

EXPOSE 8080 8443

RUN useradd -ms /bin/bash ovirt

RUN yum -y install http://resources.ovirt.org/pub/yum-repo/ovirt-release40.rpm \
    && curl  https://copr.fedorainfracloud.org/coprs/patternfly/patternfly1/repo/fedora-23/patternfly-patternfly1-fedora-23.repo > /etc/yum.repos.d/patternfly.repo \
    && yum -y install git java-devel maven openssl nss \
       m2crypto python-psycopg2 python-cheetah python-daemon libxml2-python \
       unzip patternfly1 pyflakes python-pep8 python-docker-py mailcap python-jinja2 \
    && yum -y install ovirt-engine-wildfly ovirt-engine-wildfly-overlay java-1.8.0-openjdk java-1.8.0-openjdk-devel \
    && curl -LO https://github.com/kubevirt/ovirt-engine/archive/$VERSION.tar.gz#/ovirt-engine-$VERSION.tar.gz \
    && tar xf ovirt-engine-$VERSION.tar.gz && chown ovirt:ovirt ovirt-engine-$VERSION -R && cd ovirt-engine-$VERSION \
    && su -m -s /bin/bash ovirt -c "make install-dev PREFIX=\"$OVIRT_HOME\" BUILD_UT=0 DEV_EXTRA_BUILD_FLAGS=\"-Dgwt.compiler.localWorkers=1 -Dgwt.jjs.maxThreads=1\"" \
    && cd .. && rm -rf ovirt-engine-$VERSION* \
    && yum -y remove maven pyflakes python-pep8 git \
    && yum -y install ovirt-host-deploy ovirt-setup-lib patch postgresql bind-utils iproute procps-ng openssh java-1.8.0-openjdk-headless \
    && yum -y clean all \
    && rm -rf /home/ovirt/.m2

USER ovirt

#oVirt
ENV OVIRT_PASSWORD engine
ENV OVIRT_PKI_ORGANIZATION oVirt

COPY docker/entrypoint.sh docker/answers.conf.in docker/setup.patch /home/ovirt/ 

# patch engine-setup to enable developer mode without asking
RUN patch -p0 < /home/ovirt/setup.patch

# Persist this folder to keep the generated TLS certificates on the first start
VOLUME $OVIRT_HOME/etc/pki/ovirt-engine
# Persist this folder to keep the database backups
VOLUME $OVIRT_HOME/var/lib/ovirt-engine/backups

# Encrypt host communication
ENV HOST_ENCRYPT=true
# Try to provision hosts when they are added
ENV HOST_INSTALL=true

CMD bash /home/ovirt/entrypoint.sh
