#!/bin/bash -xe

export JVM_MEM_OPTS="-Xms1G -Xmx4G"

# set maven 3.5 for el7
if [[ "$STD_CI_DISTRO" = "el7" ]]; then
  source scl_source enable rh-maven35 || true
fi

