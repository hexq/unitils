/*
 * Copyright 2012,  Unitils.org
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

package org.unitils.database.core;

import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.unitils.core.UnitilsException;
import org.unitils.database.transaction.TransactionProvider;
import org.unitils.database.transaction.TransactionProviderManager;
import org.unitils.mock.Mock;
import org.unitils.mock.annotation.Dummy;
import org.unitilsnew.UnitilsJUnit4;

import javax.sql.DataSource;

import static org.junit.Assert.*;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRED;

/**
 * @author Tim Ducheyne
 */
public class TransactionManagerRegisterDataSourceTest extends UnitilsJUnit4 {

    /* Tested object */
    private TransactionManager transactionManager;

    private Mock<TransactionProviderManager> transactionProviderManagerMock;
    private Mock<TransactionProvider> transactionProviderMock;
    private Mock<PlatformTransactionManager> platformTransactionManagerMock;
    @Dummy
    private TransactionStatus transactionStatus;
    @Dummy
    private DataSource dataSource;
    @Dummy
    private DataSource otherDataSource;


    @Before
    public void initialize() {
        transactionManager = new TransactionManager(transactionProviderManagerMock.getMock());

        transactionProviderManagerMock.returns(transactionProviderMock).getTransactionProvider();
        transactionProviderMock.returns(platformTransactionManagerMock).getPlatformTransactionManager(null, null);
        platformTransactionManagerMock.returns(transactionStatus).getTransaction(null);
    }


    @Test
    public void registerDataSourceWhenNoTransactionStarted() throws Exception {
        transactionManager.registerDataSource(dataSource);

        assertSame(dataSource, transactionManager.dataSource);
        assertFalse(transactionManager.isTransactionActive());
    }

    @Test
    public void startTransactionWhenTransactionMarkedAsStarted() throws Exception {
        transactionManager.startTransaction("myTransactionManager");
        transactionManager.registerDataSource(dataSource);

        assertTrue(transactionManager.isTransactionActive());
        transactionProviderMock.assertInvoked().getPlatformTransactionManager("myTransactionManager", dataSource);
        platformTransactionManagerMock.assertInvoked().getTransaction(new DefaultTransactionDefinition(PROPAGATION_REQUIRED));
    }

    @Test
    public void doNotStartNewTransactionWhenRegisteringSameDataSourceMoreThanOnceWhileTransactionIsRunning() throws Exception {
        transactionManager.startTransaction("myTransactionManager");
        transactionManager.registerDataSource(dataSource);
        platformTransactionManagerMock.assertInvoked().getTransaction(null);

        transactionManager.registerDataSource(dataSource);
        platformTransactionManagerMock.assertNotInvoked().getTransaction(null);
    }

    @Test
    public void noProblemRegisteringOtherDataSourceWhenNoTransactionIsRunning() throws Exception {
        transactionManager.registerDataSource(dataSource);
        transactionManager.registerDataSource(otherDataSource);
        assertSame(otherDataSource, transactionManager.dataSource);
    }

    @Test
    public void exceptionWhenTransactionAlreadyStartedForOtherDataSource() throws Exception {
        transactionManager.startTransaction("myTransactionManager");
        transactionManager.registerDataSource(dataSource);
        try {
            transactionManager.registerDataSource(otherDataSource);
            fail("UnitilsException expected");
        } catch (UnitilsException e) {
            assertEquals("Unable to register data source. A transaction for another data source is already active for this test.\n" +
                    "A transaction can only be started for 1 data source at the same time. If you want a transaction spanning multiple data sources, you will need to set up an XA-transaction manager in a spring context.", e.getMessage());
        }
    }
}
