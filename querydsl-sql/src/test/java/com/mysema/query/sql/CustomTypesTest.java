package com.mysema.query.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.query.AbstractJDBCTest;
import com.mysema.query.alias.AliasTest.Gender;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.types.EnumByNameType;
import com.mysema.query.sql.types.StringType;

public class CustomTypesTest extends AbstractJDBCTest{
    
    private Configuration configuration;
    
    @Before
    public void setUp() throws ClassNotFoundException, SQLException{
        super.setUp();
        // create schema
        statement.execute("drop table person if exists");
        statement.execute("create table person("
            + "id INT, "
            + "firstname VARCHAR(50), "
            + "gender VARCHAR(50), "
            + "securedId VARCHAR(50), "
            + "CONSTRAINT PK_person PRIMARY KEY (id) "
            + ")");
        
        // create configuration
        configuration = new Configuration(new HSQLDBTemplates());
        configuration.setType(Types.DATE, java.util.Date.class);
        configuration.setType("person", "secureId", new EncryptedString());
        configuration.setType("person", "gender",  new EnumByNameType<Gender>(Gender.class));
        configuration.register(new StringType());

    }

    @Test
    public void export() throws SQLException, IOException{
        // create exporter
        String namePrefix = "Q";
        NamingStrategy namingStrategy = new DefaultNamingStrategy();
        MetaDataSerializer serializer = new MetaDataSerializer(namePrefix, namingStrategy);
        MetaDataExporter exporter = new MetaDataExporter(namePrefix, "test", new File("target/customExport"), namingStrategy, serializer);
        exporter.setConfiguration(configuration);
        
        // export
        exporter.export(connection.getMetaData());
        String person = FileUtils.readFileToString(new File("target/customExport/test/QPerson.java"));
        assertTrue(person.contains("createEnum(\"GENDER\""));              
    }
    
    @Test
    @Ignore
    public void insert_and_query(){
        // FIXME
        QPerson person = QPerson.person;
        
        // insert
        SQLInsertClause insert = new SQLInsertClause(connection, configuration, person);
        insert.set(person.firstname, "Bob");
        insert.set(person.gender, Gender.MALE);
        assertEquals(1l, insert.execute());
    }
    
}