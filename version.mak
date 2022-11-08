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
MILESTONE_IF_NEEDED=zstream

# RPM_RELEASE_ON_RELEASE should be set to the rpm release to have on release (non-SNAPSHOT) builds.
RPM_RELEASE_ON_RELEASE=1

# MILESTONE is set to MILESTONE_IF_NEEDED on SNAPSHOT builds, empty otherwise.
ifndef MILESTONE
MILESTONE=$(shell cat pom.xml | head -n 20 | grep '<version>' | head -n 1 | sed -e 's/.*>\(.*\)<.*/\1/' | grep -q 'SNAPSHOT$$' && echo $(MILESTONE_IF_NEEDED))
endif

# RPM_VERSION is set to pom version without -SNAPSHOT
ifndef RPM_VERSION
RPM_VERSION:=$(shell cat pom.xml | head -n 20 | grep '<version>' | head -n 1 | sed -e 's/.*>\(.*\)<.*/\1/' -e 's/-SNAPSHOT//')
endif


# RPM release should be automatic.
# Set to 0.something on SNAPSHOT builds, and to RPM_RELEASE_ON_RELEASE otherwise.
# If needed to be set manually:
# For pre-release:
# RPM_RELEASE=0.$(MILESTONE).$(shell date -u +%Y%m%d%H%M%S)
#
ifndef RPM_RELEASE
RPM_RELEASE=$(shell if cat pom.xml | head -n 20 | grep '<version>' | head -n 1 | sed -e 's/.*>\(.*\)<.*/\1/' | grep -q 'SNAPSHOT$$'; then echo 0.$(MILESTONE).$$(date -u +%Y%m%d%H%M%S); else echo $(RPM_RELEASE_ON_RELEASE); fi)
endif
