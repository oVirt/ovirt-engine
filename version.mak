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

# RPM_RELEASE_ON_RELEASE should be set to the rpm release to have on release (non-SNAPSHOT) builds.
RPM_RELEASE_ON_RELEASE=1

# AUTO_MILESTONE is set to MILESTONE_IF_NEEDED on SNAPSHOT builds, empty otherwise.
AUTO_MILESTONE=$(shell cat pom.xml | head -n 20 | grep '<version>' | head -n 1 | sed -e 's/.*>\(.*\)<.*/\1/' | grep -q 'SNAPSHOT$$' && echo $(MILESTONE_IF_NEEDED))

# AUTO_RPM_RELEASE is set to 0.0.something on SNAPSHOT builds, and to RPM_RELEASE_ON_RELEASE otherwise.
AUTO_RPM_RELEASE=$(shell if cat pom.xml | head -n 20 | grep '<version>' | head -n 1 | sed -e 's/.*>\(.*\)<.*/\1/' | grep -q 'SNAPSHOT$$'; then echo 0.2.$(MILESTONE).$$(date -u +%Y%m%d%H%M%S); else echo $(RPM_RELEASE_ON_RELEASE); fi)

MILESTONE=$(AUTO_MILESTONE)

# RPM release should be automatic. If needed to be set manually:
# For pre-release:
# RPM_RELEASE=0.N.$(MILESTONE).$(shell date -u +%Y%m%d%H%M%S)
# While N is incremented when milestone is changed.
#
# For release:
# RPM_RELEASE=N
# while N is incremented each re-release
#
RPM_RELEASE=$(AUTO_RPM_RELEASE)

#
# Downstream only release prefix
# Downstream (mead) does not use RPM_RELEASE but internal
# mead versioning.
# Increment the variable bellow after every milestone is
# released.
# Or leave empty to have only mead numbering.
#
DOWNSTREAM_RPM_RELEASE_PREFIX=0.
