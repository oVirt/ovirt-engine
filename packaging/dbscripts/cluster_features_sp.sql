

/* ----------------------------------------------------------------
 Stored procedures for database operations on Cluster Features
 related tables: cluster_features, supported_cluster_features, supported_host_features
----------------------------------------------------------------*/
CREATE OR REPLACE FUNCTION InsertClusterFeature (
    v_feature_id UUID,
    v_feature_name VARCHAR(256),
    v_version VARCHAR(40),
    v_category INT,
    v_description TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO cluster_features (
        feature_id,
        feature_name,
        version,
        category,
        description
        )
    VALUES (
        v_feature_id,
        v_feature_name,
        v_version,
        v_category,
        v_description
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateClusterFeature (
    v_feature_id UUID,
    v_feature_name VARCHAR(256),
    v_version VARCHAR(40),
    v_category INT,
    v_description TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE cluster_features
    SET feature_name = v_feature_name,
        version = v_version,
        description = v_description,
        category = v_category
    WHERE feature_id = v_feature_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- The logic to get the applicable set of features for a given category is as below-
-- Calculate the bitwise AND of inpuyt v_category  and category value of current role from roles table.
-- If the calculated value is greater than 0, the role is applicable.
--
-- To explain with an example-
-- Currently supported category values  which can be set for a feature-
-- 1. Virt     0000 0001
-- 2. Gluster  0000 0010
-- 3. All      1111 1111
--
-- Now suppose the value of input category is Gluster - 2 (0000 0010), then
-- set of features would include all the roles with category values either 2 or 255
-- Now start doing bitwise AND for valid category values 1, 2 and 255 with input category.
-- Only bitwise AND with 2 and 255 would result in a value greater
-- than ZERO (0) and applicable set of features are identified.
--
-- 1 & 2 (0000 0001 & 0000 0010) = 0000 0000 = 0            Features with this category would NOT be listed
-- 2 & 2 (0000 0010 & 0000 0010) = 0000 0010 = 2 > 0        Features with this category would be listed
-- 255 & 2 (1111 1111 & 0000 0010) = 0000 0010 = 2 > 0      Features with this category would be listed
CREATE OR REPLACE FUNCTION GetClusterFeaturesByVersionAndCategory (
    v_version VARCHAR(256),
    v_category INT
    )
RETURNS SETOF cluster_features STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM cluster_features
    WHERE cluster_features.version = v_version
        AND (cluster_features.category & v_category) > 0;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetSupportedClusterFeature (
    v_feature_id UUID,
    v_cluster_id UUID
    )
RETURNS SETOF supported_cluster_features_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM supported_cluster_features_view
    WHERE feature_id = v_feature_id
        AND cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllSupportedClusterFeatures ()
RETURNS SETOF supported_cluster_features_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM supported_cluster_features_view;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertSupportedClusterFeature (
    v_feature_id UUID,
    v_cluster_id UUID,
    v_is_enabled BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO supported_cluster_features (
        cluster_id,
        feature_id,
        is_enabled
        )
    VALUES (
        v_cluster_id,
        v_feature_id,
        v_is_enabled
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateSupportedClusterFeature (
    v_feature_id UUID,
    v_cluster_id UUID,
    v_is_enabled BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE supported_cluster_features
    SET is_enabled = v_is_enabled
    WHERE cluster_id = v_cluster_id
        AND feature_id = v_feature_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteSupportedClusterFeature (
    v_feature_id UUID,
    v_cluster_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM supported_cluster_features
    WHERE cluster_id = v_cluster_id
        AND feature_id = v_feature_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetSupportedClusterFeaturesByClusterId (v_cluster_id UUID)
RETURNS SETOF supported_cluster_features_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM supported_cluster_features_view
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertSupportedHostFeature (
    v_host_id UUID,
    v_feature_name VARCHAR(256)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO supported_host_features (
        host_id,
        feature_name
        )
    VALUES (
        v_host_id,
        v_feature_name
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetSupportedHostFeaturesByHostId (v_host_id UUID)
RETURNS SETOF supported_host_features STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM supported_host_features
    WHERE host_id = v_host_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION RemoveSupportedHostFeature (
    v_host_id UUID,
    v_feature_name VARCHAR(256)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM supported_host_features
    WHERE host_id = v_host_id
        AND feature_name = v_feature_name;
END;$PROCEDURE$
LANGUAGE plpgsql;


