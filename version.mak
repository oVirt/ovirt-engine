# Version Information

# Major and minor are taken from pom.xml

# Fix release is manually specified,
# increment after releasing/branching
FIX_RELEASE=2.6

# Milestone is manually specified,
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
MILESTONE=

# RPM release is manually specified,
# For pre-release:
# RPM_RELEASE=0.N.$(MILESTONE).$(shell date -u +%Y%m%d%H%M%S)
# While N is incremented when milestone is changed.
#
# For release:
# RPM_RELEASE=N
# while N is incremented each re-release
#
RPM_RELEASE=1
