SELECT fn_db_create_index('idx_images_it_guid', 'images', 'it_guid', '', FALSE);

DELETE FROM images WHERE it_guid NOT IN (SELECT image_guid FROM images);

SELECT fn_db_create_constraint('images',
                               'fk_image_templates_images',
                               'FOREIGN KEY (it_guid) REFERENCES images(image_guid)');
