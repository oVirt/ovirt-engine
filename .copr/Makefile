.PHONY: installdeps git_cfg_safe srpm

installdeps:
	dnf -y install git

git_cfg_safe:
	# From git 2.35.2 we need to mark temporary directory, where the project is cloned to, as safe, otherwise
	# git commands won't work because of the fix for CVE-2022-24765
	git config --global --add safe.directory "$(shell pwd)"

srpm: installdeps git_cfg_safe
	./.automation/build-srpm.sh -c
	cp rpmbuild/SRPMS/*.src.rpm $(outdir)
