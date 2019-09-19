#!/bin/bash -xe

trap popd 0
pushd $(dirname "$(readlink -f "$0")")/..

make generated-files
export PYTHONPATH="packaging/pythonlib:packaging/setup:${PYTHONPATH}"
if command -v python 2>/dev/null; then
    python -m pytest packaging/setup
else
    python3 -m pytest --verbose packaging/setup
fi
