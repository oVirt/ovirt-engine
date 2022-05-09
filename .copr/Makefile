.PHONY: installdeps srpm git_config_pre create_srpm git_config_post

installdeps:
	dnf -y install git

git_config_pre:
	# From git 2.35.2 we need to mark temporary directory, where the project is cloned to, as safe, otherwise
	# git commands won't work, and the remove afterwards to make git user config clean
	$(eval REPO_DIR=$(shell pwd))
	git config --global --add safe.directory ${REPO_DIR}

create_srpm:
	$(eval SUFFIX=.git$(shell git rev-parse --short HEAD))
	echo ${SUFFIX}
	# changing the spec file as passing -D won't preserve the suffix when rebuilding in mock
	sed "s:%{?release_suffix}:${SUFFIX}:" -i ovirt-engine.spec.in
	mkdir -p tmp.repos/SOURCES
	make dist
	rpmbuild \
		-D "_topdir tmp.repos" \
		-ts ./*.tar.gz

git_config_post:
	git config --global --unset safe.directory ${REPO_DIR}

srpm: installdeps git_config_pre create_srpm git_config_post
	cp tmp.repos/SRPMS/*.src.rpm $(outdir)
