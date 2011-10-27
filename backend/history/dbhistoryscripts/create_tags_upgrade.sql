declare @lastSync datetime,
		@thisSync datetime
SET @thisSync = getdate()
SET @lastSync = Convert(datetime ,'01/01/2010', 103)
EXEC [dbo].[update_tags_tables] @lastSync, @thisSync