.PHONY: installdeps git_cfg_safe srpm

installdeps:
	dnf -y install git

git_cfg_safe:
	# From git 2.35.2 we need to mark temporary directory, where the project is cloned to, as safe, otherwise
	# git commands won't work because of the fix for CVE-2022-24765
	git config --global --add safe.directory "$(shell pwd)"

srpm: installdeps git_cfg_safe
	# Set SUFFIX to .git$HASH if we are on a -SNAPSHOT
	$(eval SUFFIX=$(shell grep -E "<version"  pom.xml | head -n1 | awk -F '[<>]' '/version/{print $3}' | grep -q -- -SNAPSHOT && echo .git$$(git rev-parse --short HEAD)))
	# changing the spec file as passing -D won't preserve the suffix when rebuilding in mock
	sed "s:%{?release_suffix}:${SUFFIX}:" -i ovirt-engine.spec.in
	mkdir -p tmp.repos/SOURCES
	make dist
	rpmbuild \
		-D "_topdir tmp.repos" \
		-ts ./*.tar.gz
	cp tmp.repos/SRPMS/*.src.rpm $(outdir)
