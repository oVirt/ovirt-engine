.PHONY: installdeps git-safe patchspec srpm

installdeps:
	dnf -y install git

# explicity mark the copr generated git repo directory (which is done prior to the mock
# call to the make_srpm and will be the current pwd) as safe for git commands
git-safe:
	git config --global --add safe.directory "$(shell pwd)"

# patch the spec file directly as passing -D won't be preserved in chroot rebuilds
patchspec: git-safe
	sed "s:%{?release_suffix}:.git$(shell git rev-parse --short HEAD):" -i ovirt-engine.spec.in

srpm: installdeps git-safe patchspec
	mkdir -p tmp.repos/SOURCES
	make dist
	rpmbuild \
		-D "_topdir tmp.repos" \
		-ts ./*.tar.gz
	cp tmp.repos/SRPMS/*.src.rpm $(outdir)
