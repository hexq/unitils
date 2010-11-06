/*
 * Copyright Unitils.org
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
package org.unitils.database.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbmaintain.DbMaintainer;
import org.dbmaintain.MainFactory;
import org.dbmaintain.database.*;
import org.dbmaintain.database.impl.DefaultDatabaseConnectionManager;
import org.dbmaintain.database.impl.DefaultSQLHandler;
import org.dbmaintain.launch.task.UpdateDatabaseTask;
import org.springframework.context.ApplicationContext;
import org.unitils.database.datasource.DataSourceFactory;

import java.util.Map;
import java.util.Properties;

import static thirdparty.org.apache.commons.lang.StringUtils.isBlank;

/**
 * @author Tim Ducheyne
 * @author Filip Neven
 */
public class DbMaintainManager {

    /* The logger instance for this class */
    private static Log logger = LogFactory.getLog(DbMaintainManager.class);

    protected Properties configuration;
    /* Indicates if the DBMaintain should be invoked to update the database */
    protected boolean updateDatabaseSchemaEnabled;
    /* True if update database has already been called */
    protected boolean updateDatabaseCalled = false;

    protected Databases databases;
    protected DatabaseConnectionManager databaseConnectionManager;
    protected MainFactory mainFactory;


    public DbMaintainManager(Properties configuration, boolean updateDatabaseSchemaEnabled, DataSourceFactory dataSourceFactory) {
        this.configuration = configuration;
        this.updateDatabaseSchemaEnabled = updateDatabaseSchemaEnabled;
        this.databaseConnectionManager = createDatabaseConnectionManager(configuration, dataSourceFactory);
    }


    public DatabaseConnection getDatabaseConnection(String databaseName) {
        return databaseConnectionManager.getDatabaseConnection(databaseName);
    }


    public Database getDatabase(String databaseName) {
        Databases databases = getDatabases();
        if (isBlank(databaseName)) {
            return databases.getDefaultDatabase();
        }
        return databases.getDatabase(databaseName);
    }

    public Databases getDatabases() {
        if (databases == null) {
            DatabasesFactory databasesFactory = new DatabasesFactory(configuration, databaseConnectionManager);
            databases = databasesFactory.createDatabases();
        }
        return databases;
    }

    public MainFactory getDbMaintainMainFactory() {
        if (mainFactory == null) {
            mainFactory = new MainFactory(configuration, databaseConnectionManager);
        }
        return mainFactory;
    }


    /**
     * Determines whether the test database is outdated and, if that is the case, updates the database with the
     * latest changes.
     *
     * @param applicationContext The test application context, The spring application context, null if not defined
     * @return True if an update occurred, false if the database was up to date
     * @see {@link org.dbmaintain.DbMaintainer}
     */
    public boolean updateDatabaseIfNeeded(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            return updateDatabaseIfNeededFromProperties();
        }
        return updateDatabaseIfNeededFromApplicationContext(applicationContext);
    }


    protected boolean updateDatabaseIfNeededFromProperties() {
        if (!updateDatabaseSchemaEnabled || updateDatabaseCalled) {
            return false;
        }
        updateDatabaseCalled = true;

        logger.info("Checking if database(s) have to be updated.");

        MainFactory mainFactory = getDbMaintainMainFactory();
        DbMaintainer dbMaintainer = mainFactory.createDbMaintainer();
        return dbMaintainer.updateDatabase(false);
    }

    protected boolean updateDatabaseIfNeededFromApplicationContext(ApplicationContext applicationContext) {
        Map<String, UpdateDatabaseTask> updateDatabaseTasks = applicationContext.getBeansOfType(UpdateDatabaseTask.class);
        if (updateDatabaseTasks.isEmpty()) {
            return false;
        }

        logger.info("Checking if database(s) have to be updated.");
        boolean databaseUpdated = false;
        for (UpdateDatabaseTask updateDatabaseTask : updateDatabaseTasks.values()) {
            databaseUpdated = databaseUpdated || updateDatabaseTask.execute();
        }
        return updateDatabaseCalled;
    }


    protected DatabaseConnectionManager createDatabaseConnectionManager(Properties configuration, DataSourceFactory dataSourceFactory) {
        SQLHandler sqlHandler = new DefaultSQLHandler();
        return new DefaultDatabaseConnectionManager(configuration, sqlHandler, dataSourceFactory);
    }
}