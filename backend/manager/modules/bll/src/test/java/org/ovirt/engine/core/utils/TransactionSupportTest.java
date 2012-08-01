package org.ovirt.engine.core.utils;

/**
 * TODO:
 * Commented out test class in order to cancel dependency on PowerMock
 * This should be revisited.
 */

//import javax.transaction.Status;
//import javax.transaction.SystemException;
//import javax.transaction.Transaction;
//import javax.transaction.TransactionManager;
//
//import junit.framework.Assert;
//
//import org.jboss.embedded.Bootstrap;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.datasource.SingleConnectionDataSource;
//
//import org.ovirt.engine.core.common.businessentities.bookmarks;
//import org.ovirt.engine.core.compat.Guid;
//import org.ovirt.engine.core.compat.TransactionScopeOption;
//import org.ovirt.engine.core.dal.dbbroker.DbFacade;
//import org.ovirt.engine.core.utils.ejb.ContainerManagedResourceType;
//import org.ovirt.engine.core.utils.ejb.EjbUtils;
//import org.ovirt.engine.core.utils.ejb.JBossEmbeddedEJBUtilsStrategy;
//import org.ovirt.engine.core.utils.transaction.TransactionMethod;
//import org.ovirt.engine.core.utils.transaction.TransactionSupport;
//
//@Ignore("Running these tests requires manual configuration of timeouts. They should be revisited when TransactionSupport is changed")
//public class TransactionSupportTest {
//
//    private static TransactionManager tm;
//    private static Bootstrap bootstrap;
//    private Transaction outerTransaction;
//    private Transaction innerTransaction;
//
//    /**
//     * Initializing the embadded Jboss and looking-up the transaction manager.
//     *
//     * @throws Exception
//     */
//    @BeforeClass
//    public static void initState() throws Exception {
//        try {
//            bootstrap = Bootstrap.getInstance();
//            if (!bootstrap.isStarted()) {
//                bootstrap.bootstrap();
//                EjbUtils.setStrategy(new JBossEmbeddedEJBUtilsStrategy());
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw ex;
//        }
//
//        tm = EjbUtils.findResource(ContainerManagedResourceType.TRANSACTION_MANAGER);
//    }
//
//    @Before
//    public void cleanState() {
//        outerTransaction = null;
//        innerTransaction = null;
//    }
//
//    /**
//     * Testing that opening a transaction within another transaction indeed opens a new transaction. Testing that the
//     * transactions status are as expected during the whole test.
//     */
//    @Test
//    public void runInNewTransaction() {
//        TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew, new TransactionMethod<Object>() {
//            @Override
//            public Object runInTransaction() {
//                try {
//                    outerTransaction = tm.getTransaction();
//                } catch (SystemException e) {
//                    e.printStackTrace();
//                }
//                validateStatus(Status.STATUS_ACTIVE, outerTransaction);
//                // open a new transaction
//                TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
//                    @Override
//                    public Object runInTransaction() {
//                        try {
//                            innerTransaction = tm.getTransaction();
//                        } catch (SystemException e) {
//                            e.printStackTrace();
//                        }
//                        // validate that the inner and outer
//                        // transactions are not equals
//                        Assert.assertFalse(outerTransaction.equals(innerTransaction));
//                        validateStatus(Status.STATUS_ACTIVE, outerTransaction);
//                        validateStatus(Status.STATUS_ACTIVE, innerTransaction);
//                        return null;
//                    }
//                });
//                validateStatus(Status.STATUS_ACTIVE, outerTransaction);
//                validateStatus(Status.STATUS_COMMITTED, innerTransaction);
//                return null;
//            }
//        });
//        validateStatus(Status.STATUS_COMMITTED, outerTransaction);
//    }
//
//    /**
//     * This test validates that changing inner transaction status to STATUS_MARKED_ROLLBACK The inner transaction is
//     * rolled back even if no exception was thrown.
//     */
//    @Test
//    public void runInNewTransactionAndRollBack() {
//        TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew, new TransactionMethod<Object>() {
//            @Override
//            public Object runInTransaction() {
//                try {
//                    outerTransaction = tm.getTransaction();
//                } catch (SystemException e) {
//                    e.printStackTrace();
//                }
//                validateStatus(Status.STATUS_ACTIVE, outerTransaction);
//                // open a new transaction
//                TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
//                    @Override
//                    public Object runInTransaction() {
//                        try {
//                            innerTransaction = tm.getTransaction();
//                            innerTransaction.setRollbackOnly();
//                        } catch (SystemException e) {
//                            e.printStackTrace();
//                        }
//                        // validate that the inner and outer
//                        // transactions are not equals
//                        Assert.assertFalse(outerTransaction.equals(innerTransaction));
//                        validateStatus(Status.STATUS_ACTIVE, outerTransaction);
//                        validateStatus(Status.STATUS_MARKED_ROLLBACK, innerTransaction);
//                        return null;
//
//                    }
//                });
//                validateStatus(Status.STATUS_ACTIVE, outerTransaction);
//                validateStatus(Status.STATUS_ROLLEDBACK, innerTransaction);
//                return null;
//            }
//        });
//        validateStatus(Status.STATUS_COMMITTED, outerTransaction);
//    }
//
//    /**
//     * Testing that if inner transaction fails - the outer transaction still commits.
//     */
//    @Test
//    public void runInNewTransactionWithInnerErrors() {
//        TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew, new TransactionMethod<Object>() {
//            @Override
//            public Object runInTransaction() {
//                try {
//                    outerTransaction = tm.getTransaction();
//                } catch (SystemException e) {
//                    e.printStackTrace();
//                }
//                validateStatus(Status.STATUS_ACTIVE, outerTransaction);
//                try {
//                    // open a new transaction
//                    TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
//                        @Override
//                        public Object runInTransaction() {
//                            try {
//                                innerTransaction = tm.getTransaction();
//                            } catch (SystemException e) {
//                                e.printStackTrace();
//                            }
//                            // validate that the inner and outer
//                            // transactions are not equals
//                            Assert.assertFalse(outerTransaction.equals(innerTransaction));
//                            validateStatus(Status.STATUS_ACTIVE, outerTransaction);
//                            validateStatus(Status.STATUS_ACTIVE, innerTransaction);
//                            throw new RuntimeException("failing inner transaction");
//                        }
//                    });
//                } catch (Exception e) {
//                    System.out.println("Exception for inner transaction with msg: " + e.getMessage());
//                } finally {
//                    validateStatus(Status.STATUS_ACTIVE, outerTransaction);
//                    validateStatus(Status.STATUS_ROLLEDBACK, innerTransaction);
//                }
//                return null;
//            }
//        });
//        validateStatus(Status.STATUS_COMMITTED, outerTransaction);
//
//    }
//
//    /**
//     * Testing that if inner transaction throws exception and fails - the outer transaction fails as well.
//     */
//    @Test
//    public void runInNewTransactionWithErrors() {
//        try {
//            TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew, new TransactionMethod<Object>() {
//                @Override
//                public Object runInTransaction() {
//                    try {
//                        outerTransaction = tm.getTransaction();
//                    } catch (SystemException e) {
//                        e.printStackTrace();
//                    }
//                    validateStatus(Status.STATUS_ACTIVE, outerTransaction);
//                    try {
//                        // open a new transaction
//                        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
//                            @Override
//                            public Object runInTransaction() {
//                                try {
//                                    innerTransaction = tm.getTransaction();
//                                } catch (SystemException e) {
//                                    e.printStackTrace();
//                                }
//                                // validate that the inner and
//                                // outer transactions are not
//                                // equals
//                                Assert.assertFalse(outerTransaction.equals(innerTransaction));
//                                validateStatus(Status.STATUS_ACTIVE, outerTransaction);
//                                validateStatus(Status.STATUS_ACTIVE, innerTransaction);
//                                throw new RuntimeException("failing inner transaction");
//                            }
//                        });
//                    } finally {
//                        validateStatus(Status.STATUS_ACTIVE, outerTransaction);
//                        validateStatus(Status.STATUS_ROLLEDBACK, innerTransaction);
//                    }
//                    return null;
//                }
//            });
//
//        } catch (Exception e) {
//            validateStatus(Status.STATUS_ROLLEDBACK, outerTransaction);
//        }
//    }
//
//    private DbFacade dbFacade;
//    private DbFacade dbFacade2;
//
//    private void createMsFacade() throws ClassNotFoundException {
//        this.getClass().getClassLoader().loadClass("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//        SingleConnectionDataSource dataSource = new SingleConnectionDataSource(
//                "jdbc:sqlserver://10.35.113.39\\SQLEXPRESS:1433;databaseName=engine", "sa", "ENGINEadmin2009!", true);
//        dbFacade = new DbFacade();
//                dbFacade.setTemplate(new JdbcTemplate(dataSource));
//        SingleConnectionDataSource dataSource2 = new SingleConnectionDataSource(
//                "jdbc:sqlserver://10.35.113.39\\SQLEXPRESS:1433;databaseName=engine", "sa", "ENGINEadmin2009!", true);
//        dbFacade2 = new DbFacade();
//                dbFacade2.setTemplate(new JdbcTemplate(dataSource2));
//        // org.springframework.jdbc.datasource.DataSourceTransactionManager
//    }
//
//    /**
//     * Testing that opening a transaction within another transaction indeed opens a new transaction. Testing that the
//     * transactions status are as expected during the whole test.
//     */
//    // @Test
//    public void runInNewDBTransaction() throws Exception {
//
//        createMsFacade();
//
//        TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew, new TransactionMethod<Object>() {
//            @Override
//            public Object runInTransaction() {
//                try {
//                    outerTransaction = tm.getTransaction();
//                } catch (SystemException e) {
//                    e.printStackTrace();
//                }
//                // validateStatus(Status.STATUS_ACTIVE, outerTransaction);
//                // TAKE DB lOCK
//                bookmarks b = new bookmarks();
//                b.setbookmark_id(Guid.NewGuid());
//                b.setbookmark_name("MyBookmark");
//                b.setbookmark_value("test");
//                dbFacade.getBookmarkDAO().save(b);
//                // open a new transaction
//                TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
//                    @Override
//                    public Object runInTransaction() {
//                        try {
//                            innerTransaction = tm.getTransaction();
//                        } catch (SystemException e) {
//                            e.printStackTrace();
//                        }
//                        // TAKE DB lOCK
//                        // open a new transaction
//                        bookmarks c = dbFacade2.getBookmarkDAO().getByName("MyBookmark");
//                        c.setbookmark_value("test2");
//                        dbFacade2.getBookmarkDAO().update(c);
//                        throw new RuntimeException();
//                        // validate that the inner and outer
//                        // transactions are not equals
//                        // Assert.assertFalse(outerTransaction.equals(innerTransaction));
//                        // validateStatus(Status.STATUS_ACTIVE,
//                        // outerTransaction);
//                        // validateStatus(Status.STATUS_ACTIVE,
//                        // innerTransaction);
//                        // return null;
//                    }
//                });
//                // validateStatus(Status.STATUS_ACTIVE, outerTransaction);
//                // validateStatus(Status.STATUS_COMMITTED, innerTransaction);
//                return null;
//            }
//        });
//        // validateStatus(Status.STATUS_COMMITTED, outerTransaction);
//    }
//
//    private static void validateStatus(int expected, Transaction t) {
//        try {
//            Assert.assertEquals(expected, t.getStatus());
//        } catch (SystemException e) {
//            e.printStackTrace();
//        }
//    }
// }
