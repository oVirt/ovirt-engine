/*--------------------------------------------------------------
Stored procedures for database operations on storage_domain_dr table
--------------------------------------------------------------*/
CREATE OR REPLACE FUNCTION InsertStorageDomainDR (
    v_storage_domain_id UUID,
    v_georep_session_id UUID,
    v_sync_schedule VARCHAR(256),
    v_gluster_scheduler_job_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO storage_domain_dr (
        storage_domain_id,
        georep_session_id,
        sync_schedule,
        gluster_scheduler_job_id
        )
    VALUES (
        v_storage_domain_id,
        v_georep_session_id,
        v_sync_schedule,
        v_gluster_scheduler_job_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateStorageDomainDR (
    v_storage_domain_id UUID,
    v_georep_session_id UUID,
    v_sync_schedule VARCHAR(256),
    v_gluster_scheduler_job_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE storage_domain_dr
    set sync_schedule = v_sync_schedule,
        gluster_scheduler_job_id = v_gluster_scheduler_job_id
    WHERE storage_domain_id = v_storage_domain_id
    AND georep_session_id = v_georep_session_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetStorageDomainDR (
    v_storage_domain_id UUID,
    v_georep_session_id UUID
    )
RETURNS SETOF storage_domain_dr STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM storage_domain_dr
    WHERE storage_domain_id = v_storage_domain_id
    AND georep_session_id = v_georep_session_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetStorageDomainDRList (
    v_storage_domain_id UUID
    )
RETURNS SETOF storage_domain_dr STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM storage_domain_dr
    WHERE storage_domain_id = v_storage_domain_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetStorageDomainDRWithGeoRep (
    v_georep_session_id UUID
    )
RETURNS SETOF storage_domain_dr STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM storage_domain_dr
    WHERE georep_session_id = v_georep_session_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteStorageDomainDR (
    v_storage_domain_id UUID,
    v_georep_session_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM storage_domain_dr
    WHERE storage_domain_id = v_storage_domain_id
    AND georep_session_id = v_georep_session_id;
END;$PROCEDURE$
LANGUAGE plpgsql;
