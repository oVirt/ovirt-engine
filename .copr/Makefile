srpm:
	dnf -y install git
	$(eval SUFFIX=$(shell sh -c "echo '.git$$(git rev-parse --short HEAD)'"))
	mkdir -p tmp.repos/SOURCES
	make dist
	rpmbuild \
		-D "_topdir tmp.repos" \
		-D "release_suffix ${SUFFIX}" \
		-D "ovirt_build_extra_flags -D gwt.userAgent=gecko1_8,safari" \
		-ts ./*.tar.gz
	cp tmp.repos/SRPMS/*.src.rpm $(outdir)
