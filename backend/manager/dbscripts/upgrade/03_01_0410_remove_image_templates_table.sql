insert into images (image_guid,creation_date,size,description,boot,it_guid,internal_drive_mapping) select '00000000-0000-0000-0000-000000000000','2008/04/01 00:00:00',85899345920,'Blank Image Template',false,'00000000-0000-0000-0000-000000000000','1';
select fn_db_drop_constraint('images','image_templates_images');
select fn_db_create_constraint('images', 'image_templates_images', 'FOREIGN KEY(it_guid) REFERENCES images(image_guid)');
drop table image_templates cascade;
