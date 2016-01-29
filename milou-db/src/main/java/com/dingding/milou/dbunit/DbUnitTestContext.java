package com.dingding.milou.dbunit;

import java.lang.reflect.Method;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;

import com.github.springtestdbunit.dataset.DataSetLoader;
import com.github.springtestdbunit.operation.DatabaseOperationLookup;

public interface DbUnitTestContext {

    /**
     * Returns the {@link IDatabaseConnection} that should be used when performing database setup and teardown.
     * 
     * @return The connection
     */
    DatabaseConnections getConnections();

    /**
     * Returns the {@link DataSetLoader} that should be used to load {@link IDataSet}s.
     * 
     * @return The dataset loader
     */
    DataSetLoader getDataSetLoader();

    /**
     * Returns the {@link DatabaseOperationLookup} that should be used to lookup database operations.
     * 
     * @return the database operation lookup
     */
    DatabaseOperationLookup getDatbaseOperationLookup();

    /**
     * Returns the class that is under test.
     * 
     * @return The class under test
     */
    Class<?> getTestClass();

    /**
     * Returns the instance that is under test.
     * 
     * @return The instance under test
     */
    Object getTestInstance();

    /**
     * Returns the method that is under test.
     * 
     * @return The method under test
     */
    Method getTestMethod();

    /**
     * Returns any exception that was thrown during the test or <tt>null</tt> if no test exception occurred.
     * 
     * @return the test exception or <tt>null</tt>
     */
    Throwable getTestException();

}
