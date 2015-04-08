/* ----------------------------------------------------------------
 Stored procedures for database operations on Cluster Features
 related tables: cluster_features, supported_cluster_features, supported_host_features
----------------------------------------------------------------*/
Create or replace function InsertClusterFeature(v_feature_id UUID,
                                                v_feature_name VARCHAR(256),
                                                v_version VARCHAR(40),
                                                v_category INTEGER,
                                                v_description TEXT)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO cluster_features(feature_id, feature_name, version, category, description)
    VALUES(v_feature_id, v_feature_name, v_version, v_category, v_description);
END; $procedure$
LANGUAGE plpgsql;

Create or replace function UpdateClusterFeature(v_feature_id UUID,
                                                v_feature_name VARCHAR(256),
                                                v_version VARCHAR(40),
                                                v_category INTEGER,
                                                v_description TEXT)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE cluster_features
    SET feature_name = v_feature_name,
    version = v_version,
    description = v_description,
    category = v_category
    where feature_id = v_feature_id;
END; $procedure$
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

Create or replace FUNCTION GetClusterFeaturesByVersionAndCategory(v_version VARCHAR(256), v_category INTEGER)
RETURNS SETOF cluster_features STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  cluster_features
    WHERE cluster_features.version = v_version and (cluster_features.category & v_category) > 0;
END; $procedure$
LANGUAGE plpgsql;


Create or replace function InsertSupportedClusterFeature(v_feature_id UUID,
                                                         v_cluster_id UUID,
                                                         v_is_enabled BOOLEAN)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO supported_cluster_features(cluster_id, feature_id, is_enabled)
    VALUES(v_cluster_id, v_feature_id, v_is_enabled);
END; $procedure$
LANGUAGE plpgsql;

Create or replace function UpdateSupportedClusterFeature(v_feature_id UUID,
                                                         v_cluster_id UUID,
                                                         v_is_enabled BOOLEAN)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE supported_cluster_features
    SET is_enabled = v_is_enabled
    where cluster_id = v_cluster_id and feature_id = v_feature_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetSupportedClusterFeaturesByClusterId(v_cluster_id UUID)
RETURNS SETOF supported_cluster_features_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  supported_cluster_features_view
    WHERE cluster_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace function InsertSupportedHostFeature(v_host_id UUID,
                                                      v_feature_name VARCHAR(256))
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO supported_host_features(host_id, feature_name)
    VALUES(v_host_id, v_feature_name);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetSupportedHostFeaturesByHostId(v_host_id UUID)
RETURNS SETOF supported_host_features STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  supported_host_features
    WHERE host_id = v_host_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace function RemoveSupportedHostFeature(v_host_id UUID,
                                                      v_feature_name VARCHAR(256))
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM supported_host_features WHERE host_id = v_host_id and feature_name = v_feature_name;
END; $procedure$
LANGUAGE plpgsql;
