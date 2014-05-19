--
-- Table is used to exchange data with external systems
--
CREATE TABLE external_variable (
  var_name VARCHAR(100) NOT NULL,
  var_value VARCHAR(4000),
  _update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
  CONSTRAINT pk_external_variable PRIMARY KEY(var_name)
) WITH OIDS;

