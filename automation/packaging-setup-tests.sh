#!/bin/bash -xe

trap popd 0
pushd $(dirname "$(readlink -f "$0")")/..

export PYTHONPATH="packaging/pythonlib:packaging/setup:${PYTHONPATH}"
python -m pytest packaging/setup
