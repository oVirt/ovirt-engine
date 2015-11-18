--
-- PostgreSQL database dump
--


--
-- Data for Name: storage_pool; Type: TABLE DATA; Schema: public; Owner: engine
--

INSERT INTO storage_pool (
    id,
    name,
    description,
    storage_pool_type,
    storage_pool_format_type,
    status,
    master_domain_version,
    spm_vds_id,
    compatibility_version,
    _create_date,
    _update_date,
    quota_enforcement_type,
    free_text_comment
    )
VALUES (
    '00000002-0002-0002-0002-00000000021c',
    'Default',
    'The default Data Center',
    1,
    NULL,
    0,
    0,
    NULL,
    '4.0',
    '2015-01-21 15:14:26.872109+02',
    NULL,
    NULL,
    NULL
    );

--
-- PostgreSQL database dump complete
--

