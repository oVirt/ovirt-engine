#!/bin/bash -xe

trap popd 0
pushd $(dirname "$(readlink -f "$0")")/..

export PYTHONPATH="packaging/pythonlib:${PYTHONPATH}"
python3 -m pytest --verbose packaging/pythonlib

