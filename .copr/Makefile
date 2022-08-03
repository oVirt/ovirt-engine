.PHONY: installdeps git_cfg_safe srpm

installdeps:
	dnf -y install git

git_cfg_safe:
	# From git 2.35.2 we need to mark temporary directory, where the project is cloned to, as safe, otherwise
	# git commands won't work because of the fix for CVE-2022-24765
	git config --global --add safe.directory "$(shell pwd)"

srpm: installdeps git_cfg_safe
       # $(eval SUFFIX=.git$(shell git rev-parse --short HEAD))
	# changing the spec file as passing -D won't preserve the suffix when rebuilding in mock
       # sed "s:%{?release_suffix}:${SUFFIX}:" -i ovirt-engine.spec.in
	mkdir -p tmp.repos/SOURCES
	make dist
	rpmbuild \
		-D "_topdir tmp.repos" \
		-ts ./*.tar.gz
	cp tmp.repos/SRPMS/*.src.rpm $(outdir)
