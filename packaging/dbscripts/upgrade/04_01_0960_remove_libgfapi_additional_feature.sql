/* Removing libgfapi as additional feature in 4.1 */
DELETE FROM cluster_features
WHERE feature_id = '00000020-0020-0020-0020-000000000300';
