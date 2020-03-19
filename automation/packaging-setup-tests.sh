#!/bin/bash -xe

trap popd 0
pushd $(dirname "$(readlink -f "$0")")/..

make generated-files
export PYTHONPATH="packaging/pythonlib:packaging/setup:${PYTHONPATH}"
python3 -m pytest --verbose packaging/setup

