#! /usr/bin/env bash

#
# Copy normalized translations to the repo if they have non-comment changes
#
if [[ $? -eq 0 ]]; then
  for localized in $(
    find target/normalized -type f -name '*.properties' | sort
  ); do
    base=${localized#target/normalized/}

    diff --ignore-matching-lines='^#' "${localized}" "../${base}" > /dev/null 2>&1
    if [[ $? -eq 1 ]]; then
      echo "Found changes! Copying \"${base}\" to the repo"
      cp ${localized} "../${base}"
    else
      echo "No changes to \"${base}\""
    fi
  done
fi
