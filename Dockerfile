ARG BASE_VERSION=9
FROM quay.io/ovirt/buildcontainer:el${BASE_VERSION}stream

ARG USERNAME=build
ENV USERNAME=$USERNAME

# Install required PyPI packages using pip3
RUN dnf -y --nobest update && \
    dnf install -y sudo && \
    pip3 install "ansible-lint>=6.0.0,<7.0.0" && \
    echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME

# Explicitly copy the current directory and the .git directory to /src in the container
COPY . /workspace

# Set the working directory to /workspace
WORKDIR /workspace

# Build Spec
RUN make ovirt-engine.spec

# Install builds deps
RUN dnf -y builddep ovirt-engine.spec

# Install run deps
RUN dnf -y install python3-daemon python3-otopi python3-psycopg2 python3-ovirt-setup-lib otopi-common initscripts-service bind-utils postgresql ovirt-engine-wildfly-overlay mailcap ansible-runner ansible-collection-ansible-posix ovirt-imageio-daemon novnc ovirt-engine-websocket-proxy

# engine-setup needs the link to initctl
RUN ln -s /usr/sbin/service /usr/bin/initctl

# Set default User
USER $USERNAME

# Set oVirt development environment variables
ENV PREFIX=/home/$USERNAME/ovirt
ENV PATH="$PREFIX/bin:$PATH"
ENV PATH="$PREFIX/ovirt-engine/services/ovirt-engine:$PATH"
ENV PATH="$PREFIX/share/ovirt-engine/services/ovirt-websocket-proxy:$PATH"

# Expose oVirt-Engine, Java and ovirt imageio ports
EXPOSE 8080 8443 8787 54323 9696 6642 35357 2222 6100 7410
