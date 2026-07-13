# Version Information are taken from pom.xml

# MILESTONE_IF_NEEDED is manually specified,
# example for ordering:
# - master
# - alpha
# - master
# - beta
# - master
# - beta2
# - master
# - rc
# - master
# - rc2
# - master
# - <none>
#
MILESTONE_IF_NEEDED=master

# MILESTONE is set to MILESTONE_IF_NEEDED on SNAPSHOT builds, empty otherwise.
ifndef MILESTONE
MILESTONE=$(shell cat pom.xml | head -n 20 | grep '<version>' | head -n 1 | sed -e 's/.*>\(.*\)<.*/\1/' | grep -q 'SNAPSHOT$$' && echo $(MILESTONE_IF_NEEDED))
endif

# RPM_VERSION is set to pom version without -SNAPSHOT.
# For release builds it is overridden by the CI environment from the tag.
ifndef RPM_VERSION
RPM_VERSION:=$(shell cat pom.xml | head -n 20 | grep '<version>' | head -n 1 | sed -e 's/.*>\(.*\)<.*/\1/' -e 's/-SNAPSHOT//')
endif

# Default RPM release for development builds.
# For release builds, RPM_RELEASE is overridden by the CI environment from the tag.
# A timestamp/git suffix is appended via the release_suffix RPM macro in CI.
ifndef RPM_RELEASE
RPM_RELEASE=0.master
endif
