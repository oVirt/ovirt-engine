----------------------------------
-- 		create functions		--
----------------------------------

IF EXISTS (SELECT NAME FROM sys.objects WHERE TYPE = 'FN' AND NAME = 'tag_path_ids_to_names')
BEGIN
	EXEC('Drop Function tag_path_ids_to_names')
END
GO

CREATE FUNCTION [dbo].[tag_path_ids_to_names] (@path nvarchar(max))
RETURNS nvarchar(max)
AS
Begin
	DECLARE @id uniqueidentifier
	DECLARE @path_names nvarchar(max)
	SET @path_names = '';
	IF (@path != '/')
	BEGIN
		SET @path = RIGHT(@path, LEN(@path)-1);
		WHILE (LEN(@path) > 0)
			BEGIN
				SET @id = cast(LEFT(@path,36) as uniqueidentifier);
				SET @path = RIGHT(@path, LEN(@path)-37);
				SET @path_names = @path_names + '/' + (select [tag_name]
													   FROM [dbo].[tag_details]
													   where [tag_id] = @id)
			END
	END
	IF (@path_names = '')
	SET @path_names = '/'
	RETURN @path_names;
END
GO