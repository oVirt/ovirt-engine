#!/bin/bash
javac FieldsInVDCConfigXmlToSQL.java
java -cp /usr/share/java/postgresql-jdbc.jar:. FieldsInVDCConfigXmlToSQL ../dbscripts/FieldsInVDCConfig.xml >fill_config.sql
rm -rf FieldsInVDCConfigXmlToSQL*class*

