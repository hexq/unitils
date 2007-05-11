/*
 * Copyright 2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitils.dbmaintainer.clean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.Trigger;
import org.unitils.UnitilsJUnit3;
import org.unitils.core.ConfigurationLoader;
import org.unitils.core.dbsupport.DbSupport;
import org.unitils.core.dbsupport.DbSupportFactory;
import org.unitils.core.dbsupport.TestSQLUtils;
import org.unitils.core.util.SQLUtils;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.dbmaintainer.clean.impl.DefaultDBClearer;
import org.unitils.dbmaintainer.util.DatabaseModuleConfigUtils;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Test class for the {@link DBClearer} with configuratin to preserve all items.
 *
 * @author Tim Ducheyne
 * @author Filip Neven
 */
public class DBClearerPreserveTest extends UnitilsJUnit3 {

    /* The logger instance for this class */
    private static Log logger = LogFactory.getLog(DBClearerPreserveTest.class);

    /* DataSource for the test database, is injected */
    @TestDataSource
    private DataSource dataSource = null;

    /* Tested object */
    private DBClearer dbClearer;

    /* The DbSupport object */
    private DbSupport dbSupport;


    /**
     * Configures the tested object. Creates a test table, index, view and sequence
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Properties configuration = new ConfigurationLoader().loadConfiguration();
        dbSupport = DbSupportFactory.getDefaultDbSupport(configuration, dataSource);

        // configure items to preserve
        configuration.setProperty(DefaultDBClearer.PROPKEY_PRESERVE_TABLES, "test_table, " + dbSupport.quoted("Test_CASE_Table"));
        configuration.setProperty(DefaultDBClearer.PROPKEY_PRESERVE_VIEWS, "test_view, " + dbSupport.quoted("Test_CASE_View"));
        configuration.setProperty(DefaultDBClearer.PROPKEY_PRESERVE_SEQUENCES, "test_sequence, " + dbSupport.quoted("Test_CASE_Sequence"));
        configuration.setProperty(DefaultDBClearer.PROPKEY_PRESERVE_SYNONYMS, "test_table, " + dbSupport.quoted("Test_CASE_Table"));

        // todo Test_trigger_Preserve            Test_CASE_Trigger_Preserve
        dbClearer = DatabaseModuleConfigUtils.getConfiguredDatabaseTaskInstance(DBClearer.class, configuration, dataSource);

        cleanupTestDatabase();
        createTestDatabase();
    }


    /**
     * Removes all test tables.
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanupTestDatabase();
    }


    /**
     * Checks if the tables are correctly dropped.
     */
    public void testClearDatabase_tables() throws Exception {
        assertEquals(2, dbSupport.getTableNames().size());
        dbClearer.clearSchemas();
        assertEquals(2, dbSupport.getTableNames().size());
    }


    /**
     * Checks if the views are correctly dropped
     */
    public void testClearDatabase_views() throws Exception {
        assertEquals(2, dbSupport.getViewNames().size());
        dbClearer.clearSchemas();
        assertEquals(2, dbSupport.getViewNames().size());
    }


    /**
     * Checks if the synonyms are correctly dropped
     */
    public void testClearDatabase_synonyms() throws Exception {
        if (!dbSupport.supportsSynonyms()) {
            logger.warn("Current dialect does not support synonyms. Skipping test.");
            return;
        }
        assertEquals(2, dbSupport.getSynonymNames().size());
        dbClearer.clearSchemas();
        assertEquals(2, dbSupport.getSynonymNames().size());
    }


    /**
     * Tests if the triggers are correctly dropped
     */
    public void testClearDatabase_sequences() throws Exception {
        if (!dbSupport.supportsSequences()) {
            logger.warn("Current dialect does not support sequences. Skipping test.");
            return;
        }
        assertEquals(2, dbSupport.getSequenceNames().size());
        dbClearer.clearSchemas();
        assertEquals(2, dbSupport.getSequenceNames().size());
    }


    /**
     * Creates all test database structures (view, tables...)
     */
    private void createTestDatabase() throws Exception {
        String dialect = dbSupport.getDatabaseDialect();
        if ("hsqldb".equals(dialect)) {
            createTestDatabaseHsqlDb();
        } else if ("mysql".equals(dialect)) {
            createTestDatabaseMySql();
        } else if ("oracle".equals(dialect)) {
            createTestDatabaseOracle();
        } else if ("postgresql".equals(dialect)) {
            createTestDatabasePostgreSql();
        } else {
            fail("This test is not implemented for current dialect: " + dialect);
        }
    }


    /**
     * Drops all created test database structures (views, tables...)
     */
    private void cleanupTestDatabase() throws Exception {
        String dialect = dbSupport.getDatabaseDialect();
        if ("hsqldb".equals(dialect)) {
            cleanupTestDatabaseHsqlDb();
        } else if ("mysql".equals(dialect)) {
            cleanupTestDatabaseMySql();
        } else if ("oracle".equals(dialect)) {
            cleanupTestDatabaseOracle();
        } else if ("postgresql".equals(dialect)) {
            cleanupTestDatabasePostgreSql();
        }
    }

    //
    // Database setup for HsqlDb
    //

    /**
     * Creates all test database structures (view, tables...)
     */
    private void createTestDatabaseHsqlDb() throws Exception {
        // create tables
        SQLUtils.executeUpdate("create table test_table (col1 int not null identity, col2 varchar(12) not null)", dataSource);
        SQLUtils.executeUpdate("create table \"Test_CASE_Table\" (col1 int, foreign key (col1) references test_table(col1))", dataSource);
        // create views
        SQLUtils.executeUpdate("create view test_view as select col1 from test_table", dataSource);
        SQLUtils.executeUpdate("create view \"Test_CASE_View\" as select col1 from \"Test_CASE_Table\"", dataSource);
        // create sequences
        SQLUtils.executeUpdate("create sequence test_sequence", dataSource);
        SQLUtils.executeUpdate("create sequence \"Test_CASE_Sequence\"", dataSource);
        // create triggers
        // todo move to code clearer test
        SQLUtils.executeUpdate("create trigger test_trigger before insert on \"Test_CASE_Table\" call \"org.unitils.core.dbsupport.HsqldbDbSupportTest.TestTrigger\"", dataSource);
        SQLUtils.executeUpdate("create trigger \"Test_CASE_Trigger\" before insert on \"Test_CASE_Table\" call \"org.unitils.core.dbsupport.HsqldbDbSupportTest.TestTrigger\"", dataSource);
    }


    /**
     * Drops all created test database structures (views, tables...)
     */
    private void cleanupTestDatabaseHsqlDb() throws Exception {
        TestSQLUtils.dropTestTables(dbSupport, "test_table", "\"Test_CASE_Table\"");
        TestSQLUtils.dropTestViews(dbSupport, "test_view", "\"Test_CASE_View\"");
        TestSQLUtils.dropTestSequences(dbSupport, "test_sequence", "\"Test_CASE_Sequence\"");
        TestSQLUtils.dropTestTriggers(dbSupport, "test_trigger", "\"Test_CASE_Trigger\"");
    }


    /**
     * Test trigger for hypersonic.
     *
     * @author Filip Neven
     * @author Tim Ducheyne
     */
    public static class TestTrigger implements Trigger {

        public void fire(int i, String string, String string1, Object[] objects, Object[] objects1) {
        }
    }

    //
    // Database setup for MySql
    //

    /**
     * Creates all test database structures (view, tables...)
     */
    private void createTestDatabaseMySql() throws Exception {
        // create tables
        SQLUtils.executeUpdate("create table test_table (col1 int not null primary key AUTO_INCREMENT, col2 varchar(12) not null)", dataSource);
        SQLUtils.executeUpdate("create table `Test_CASE_Table` (col1 int, foreign key (col1) references test_table(col1))", dataSource);
        // create views
        SQLUtils.executeUpdate("create view test_view as select col1 from test_table", dataSource);
        SQLUtils.executeUpdate("create view `Test_CASE_View` as select col1 from `Test_CASE_Table`", dataSource);
        // create triggers
        SQLUtils.executeUpdate("create trigger test_trigger before insert on `Test_CASE_Table` FOR EACH ROW begin end", dataSource);
        SQLUtils.executeUpdate("create trigger `Test_CASE_Trigger` after insert on `Test_CASE_Table` FOR EACH ROW begin end", dataSource);
    }


    /**
     * Drops all created test database structures (views, tables...)
     */
    private void cleanupTestDatabaseMySql() throws Exception {
        TestSQLUtils.dropTestTables(dbSupport, "test_table", "`Test_CASE_Table`");
        TestSQLUtils.dropTestViews(dbSupport, "test_view", "`Test_CASE_View`");
        TestSQLUtils.dropTestTriggers(dbSupport, "test_trigger", "`Test_CASE_Trigger`");
    }

    //
    // Database setup for Oracle
    //

    /**
     * Creates all test database structures (view, tables...)
     */
    private void createTestDatabaseOracle() throws Exception {
        // create tables
        SQLUtils.executeUpdate("create table test_table (col1 varchar(10) not null primary key, col2 varchar(12) not null)", dataSource);
        SQLUtils.executeUpdate("create table \"Test_CASE_Table\" (col1 varchar(10), foreign key (col1) references test_table(col1))", dataSource);
        // create views
        SQLUtils.executeUpdate("create view test_view as select col1 from test_table", dataSource);
        SQLUtils.executeUpdate("create view \"Test_CASE_View\" as select col1 from \"Test_CASE_Table\"", dataSource);
        // create synonyms
        SQLUtils.executeUpdate("create synonym test_synonym for test_table", dataSource);
        SQLUtils.executeUpdate("create synonym \"Test_CASE_Synonym\" for \"Test_CASE_Table\"", dataSource);
        // create sequences
        SQLUtils.executeUpdate("create sequence test_sequence", dataSource);
        SQLUtils.executeUpdate("create sequence \"Test_CASE_Sequence\"", dataSource);
        // create triggers
        SQLUtils.executeUpdate("create or replace trigger test_trigger before insert on \"Test_CASE_Table\" begin dbms_output.put_line('test'); end test_trigger", dataSource);
        SQLUtils.executeUpdate("create or replace trigger \"Test_CASE_Trigger\" before insert on \"Test_CASE_Table\" begin dbms_output.put_line('test'); end \"Test_CASE_Trigger\"", dataSource);
        // create types
        SQLUtils.executeUpdate("create type test_type AS (col1 int)", dataSource);
        SQLUtils.executeUpdate("create type \"Test_CASE_Type\" AS (col1 int)", dataSource);
    }


    /**
     * Drops all created test database structures (views, tables...)
     */
    private void cleanupTestDatabaseOracle() throws Exception {
        TestSQLUtils.dropTestTables(dbSupport, "test_table", "\"Test_CASE_Table\"");
        TestSQLUtils.dropTestViews(dbSupport, "test_view", "\"Test_CASE_View\"");
        TestSQLUtils.dropTestSynonyms(dbSupport, "test_synonym", "\"Test_CASE_Synonym\"");
        TestSQLUtils.dropTestSequences(dbSupport, "test_sequence", "\"Test_CASE_Sequence\"");
        TestSQLUtils.dropTestTriggers(dbSupport, "test_trigger", "\"Test_CASE_Trigger\"");
        TestSQLUtils.dropTestTypes(dbSupport, "test_type", "\"Test_CASE_Type\"");
    }

    //
    // Database setup for PostgreSql
    //

    /**
     * Creates all test database structures (view, tables...)
     */
    private void createTestDatabasePostgreSql() throws Exception {
        // create tables
        SQLUtils.executeUpdate("create table test_table (col1 varchar(10) not null primary key, col2 varchar(12) not null)", dataSource);
        SQLUtils.executeUpdate("create table \"Test_CASE_Table\" (col1 varchar(10), foreign key (col1) references test_table(col1))", dataSource);
        // create views
        SQLUtils.executeUpdate("create view test_view as select col1 from test_table", dataSource);
        SQLUtils.executeUpdate("create view \"Test_CASE_View\" as select col1 from \"Test_CASE_Table\"", dataSource);
        // create sequences
        SQLUtils.executeUpdate("create sequence test_sequence", dataSource);
        SQLUtils.executeUpdate("create sequence \"Test_CASE_Sequence\"", dataSource);
        // create triggers
        try {
            SQLUtils.executeUpdate("create language plpgsql", dataSource);
        } catch (Exception e) {
            // ignore language already exists
        }
        SQLUtils.executeUpdate("create or replace function test() returns trigger as $$ declare begin end; $$ language plpgsql", dataSource);
        SQLUtils.executeUpdate("create trigger test_trigger before insert on \"Test_CASE_Table\" FOR EACH ROW EXECUTE PROCEDURE test()", dataSource);
        SQLUtils.executeUpdate("create trigger \"Test_CASE_Trigger\" before insert on \"Test_CASE_Table\" FOR EACH ROW EXECUTE PROCEDURE test()", dataSource);
        // create types
        SQLUtils.executeUpdate("create type test_type AS (col1 int)", dataSource);
        SQLUtils.executeUpdate("create type \"Test_CASE_Type\" AS (col1 int)", dataSource);
    }


    /**
     * Drops all created test database structures (views, tables...)
     */
    private void cleanupTestDatabasePostgreSql() throws Exception {
        TestSQLUtils.dropTestTables(dbSupport, "test_table", "\"Test_CASE_Table\"");
        TestSQLUtils.dropTestViews(dbSupport, "test_view", "\"Test_CASE_View\"");
        TestSQLUtils.dropTestSequences(dbSupport, "test_sequence", "\"Test_CASE_Sequence\"");
        TestSQLUtils.dropTestTriggers(dbSupport, "test_trigger", "\"Test_CASE_Trigger\"");
        TestSQLUtils.dropTestTypes(dbSupport, "test_type", "\"Test_CASE_Type\"");
    }


}
