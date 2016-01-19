package com.dingding.milou.dbunit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.IDataSet;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Conventions;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.dingding.milou.dbunit.loader.MilouDataSetLoader;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import com.github.springtestdbunit.dataset.DataSetLoader;
import com.github.springtestdbunit.dataset.FlatXmlDataSetLoader;
import com.github.springtestdbunit.operation.DatabaseOperationLookup;
import com.github.springtestdbunit.operation.DefaultDatabaseOperationLookup;

/**
 * 封装DbUnitTestExecutionListener,保证原本功能下，建立内存数据库的镜像，{@code MilouDataSetLoader}持有该镜像map。
 * 
 * @author al
 * 
 */
public class MilouDbUnitTestExecutionListener extends AbstractTestExecutionListener {

    private static final Log logger = LogFactory.getLog(MilouDbUnitTestExecutionListener.class);

    private static final String[] COMMON_DATABASE_CONNECTION_BEAN_NAMES = { "dbUnitDatabaseConnection", "dataSource" };

    private static final String DATA_SET_LOADER_BEAN_NAME = "dbUnitDataSetLoader";

    protected static final String CONNECTION_ATTRIBUTE = Conventions.getQualifiedAttributeName(
            DbUnitTestExecutionListener.class, "connection");

    protected static final String DATA_SET_LOADER_ATTRIBUTE = Conventions.getQualifiedAttributeName(
            DbUnitTestExecutionListener.class, "dataSetLoader");

    protected static final String DATABASE_OPERATION_LOOKUP_ATTRIBUTE = Conventions.getQualifiedAttributeName(
            DbUnitTestExecutionListener.class, "databseOperationLookup");

    private boolean hasMirrorData = false;
    // 默认是不执行此Listener
    public static boolean executeListenerOrNot = false;

    private static MilouDBUnitRunner runner = new MilouDBUnitRunner();

    private DbUnitTestContextAdapter contextAdapter = null;

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        // 判断是否需要此listener
        if (!executeListenerOrNot) {
            return;
        }
        if (contextAdapter == null) {
            contextAdapter = new DbUnitTestContextAdapter(testContext);
        }
        prepareTestInstance(contextAdapter);
        if (!hasMirrorData) {
            prepareTestInstancePostAspect(contextAdapter);
            hasMirrorData = true;
        }

    }

    /**
     * 在TestClass实例化前，从内存数据库获取数据集DataSet内存数据库初始化后，获取数据库中全部表的数据，建立镜像置入内存。
     * 
     * @param contextAdapter DBUnit上下文
     */
    private void prepareTestInstancePostAspect(DbUnitTestContextAdapter contextAdapter) {
        DatabaseDataSourceConnection connection =
                (DatabaseDataSourceConnection) contextAdapter.getConnections().get("dbUnitDatabaseConnection");
        Assert.notNull(connection, "Spring context must has bean named 'dbUnitDatabaseConnection' !!!");
        MilouDataSetLoader dataSetLoader = (MilouDataSetLoader) contextAdapter.getDataSetLoader();
        try {
            IDataSet dataSet = connection.createDataSet();
            if (dataSet != null) {
                String[] tables = dataSet.getTableNames();
                for (String tableName : tables) {
                    this.writeTableIntoBuffer(tableName, connection, dataSetLoader);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("获取表名失败", e.getCause());
        } finally {

        }
    }

    /**
     * 按表名从内存数据库中获取全表数据，放入{@code MilouDataSetLoader}
     * 
     * @param tableName
     * @param iconn
     * @throws Exception
     */
    private void writeTableIntoBuffer(String tableName, IDatabaseConnection iconn, MilouDataSetLoader dataSetLoader)
            throws Exception {
        CachedDataSet dataSet = new CachedDataSet(iconn.createDataSet(new String[] { tableName }));
        if (dataSetLoader != null) {
            dataSetLoader.put(tableName, dataSet);
        }

    }

    public void prepareTestInstance(DbUnitTestContextAdapter testContext) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Preparing test instance " + testContext.getTestClass() + " for DBUnit");
        }
        String[] databaseConnectionBeanNames = null;
        String dataSetLoaderBeanName = null;
        Class<? extends DataSetLoader> dataSetLoaderClass = FlatXmlDataSetLoader.class;
        Class<? extends DatabaseOperationLookup> databaseOperationLookupClass = DefaultDatabaseOperationLookup.class;
        DbUnitConfiguration configuration = testContext.getTestClass().getAnnotation(DbUnitConfiguration.class);
        if (configuration != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Using @DbUnitConfiguration configuration");
            }
            databaseConnectionBeanNames = configuration.databaseConnection();
            dataSetLoaderClass = configuration.dataSetLoader();
            dataSetLoaderBeanName = configuration.dataSetLoaderBean();
            databaseOperationLookupClass = configuration.databaseOperationLookup();
        }

        if (ObjectUtils.isEmpty(databaseConnectionBeanNames)
                || ((databaseConnectionBeanNames.length == 1) && StringUtils.isEmpty(databaseConnectionBeanNames[0]))) {
            // modify dbunit origin code,just for supporting multi dataSource,not just "dbUnitDatabaseConnection";
            List<String> list = new ArrayList<String>();
            String commonBeanName = getDatabaseConnectionUsingCommonBeanNames(testContext);
            if (!StringUtils.isEmpty(commonBeanName)) {
                list.add(commonBeanName);
            }
            // other dataSource
            String[] dataSourceNotDBUnit = testContext.getApplicationContext().getBeanNamesForType(DataSource.class);
            if (!ObjectUtils.isEmpty(dataSourceNotDBUnit)) {
                list.addAll(Arrays.asList(dataSourceNotDBUnit));
            }
            databaseConnectionBeanNames = list.toArray(new String[list.size()]);
        }

        if (!StringUtils.hasLength(dataSetLoaderBeanName)) {
            if (testContext.getApplicationContext().containsBean(DATA_SET_LOADER_BEAN_NAME)) {
                dataSetLoaderBeanName = DATA_SET_LOADER_BEAN_NAME;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("DBUnit tests will run using databaseConnection \""
                    + StringUtils.arrayToCommaDelimitedString(databaseConnectionBeanNames)
                    + "\", datasets will be loaded using "
                    + (StringUtils.hasLength(dataSetLoaderBeanName) ? "'" + dataSetLoaderBeanName + "'"
                            : dataSetLoaderClass));
        }
        prepareDatabaseConnection(testContext, databaseConnectionBeanNames);
        prepareDataSetLoader(testContext, dataSetLoaderBeanName, dataSetLoaderClass);
        prepareDatabaseOperationLookup(testContext, databaseOperationLookupClass);
    }

    private String getDatabaseConnectionUsingCommonBeanNames(DbUnitTestContextAdapter testContext) {
        for (String beanName : COMMON_DATABASE_CONNECTION_BEAN_NAMES) {
            if (testContext.getApplicationContext().containsBean(beanName)) {
                return beanName;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Unable to find a DB Unit database connection, missing one the following beans: "
                            + Arrays.asList(COMMON_DATABASE_CONNECTION_BEAN_NAMES));
        }
        return null;
    }

    private void prepareDatabaseConnection(DbUnitTestContextAdapter testContext, String[] connectionBeanNames)
            throws Exception {
        IDatabaseConnection[] connections = new IDatabaseConnection[connectionBeanNames.length];
        for (int i = 0; i < connectionBeanNames.length; i++) {
            Object databaseConnection = testContext.getApplicationContext().getBean(connectionBeanNames[i]);
            if (databaseConnection instanceof DataSource) {
                databaseConnection = DatabaseDataSourceConnectionFactoryBean
                        .newConnection((DataSource) databaseConnection);
            }
            Assert.isInstanceOf(IDatabaseConnection.class, databaseConnection);
            connections[i] = (IDatabaseConnection) databaseConnection;
        }
        testContext.setAttribute(CONNECTION_ATTRIBUTE, new DatabaseConnections(connectionBeanNames, connections));
    }

    private void prepareDataSetLoader(DbUnitTestContextAdapter testContext, String beanName,
            Class<? extends DataSetLoader> dataSetLoaderClass) {
        if (StringUtils.hasLength(beanName)) {
            testContext.setAttribute(DATA_SET_LOADER_ATTRIBUTE,
                    testContext.getApplicationContext().getBean(beanName, DataSetLoader.class));
        } else {
            try {
                testContext.setAttribute(DATA_SET_LOADER_ATTRIBUTE, dataSetLoaderClass.newInstance());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unable to create data set loader instance for "
                        + dataSetLoaderClass, ex);
            }
        }
    }

    private void prepareDatabaseOperationLookup(DbUnitTestContextAdapter testContext,
            Class<? extends DatabaseOperationLookup> databaseOperationLookupClass) {
        try {
            testContext.setAttribute(DATABASE_OPERATION_LOOKUP_ATTRIBUTE, databaseOperationLookupClass.newInstance());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to create database operation lookup instance for "
                    + databaseOperationLookupClass, ex);
        }
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        // 判断是否需要此listener
        if (!executeListenerOrNot) {
            return;
        }
        runner.beforeTestMethod(new DbUnitTestContextAdapter(testContext));
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        // 判断是否需要此listener
        if (!executeListenerOrNot) {
            return;
        }
        runner.afterTestMethod(new DbUnitTestContextAdapter(testContext));
    }

    /**
     * Adapter class to convert Spring's {@link TestContext} to a {@link DbUnitTestContext}. Since Spring 4.0 change the
     * TestContext class from a class to an interface this method uses reflection.
     */
    private static class DbUnitTestContextAdapter implements DbUnitTestContext {

        private static final Method GET_TEST_CLASS;
        private static final Method GET_TEST_INSTANCE;
        private static final Method GET_TEST_METHOD;
        private static final Method GET_TEST_EXCEPTION;
        private static final Method GET_APPLICATION_CONTEXT;
        private static final Method GET_ATTRIBUTE;
        private static final Method SET_ATTRIBUTE;
        static {
            try {
                GET_TEST_CLASS = TestContext.class.getMethod("getTestClass");
                GET_TEST_INSTANCE = TestContext.class.getMethod("getTestInstance");
                GET_TEST_METHOD = TestContext.class.getMethod("getTestMethod");
                GET_TEST_EXCEPTION = TestContext.class.getMethod("getTestException");
                GET_APPLICATION_CONTEXT = TestContext.class.getMethod("getApplicationContext");
                GET_ATTRIBUTE = TestContext.class.getMethod("getAttribute", String.class);
                SET_ATTRIBUTE = TestContext.class.getMethod("setAttribute", String.class, Object.class);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        private TestContext testContext;

        public DbUnitTestContextAdapter(TestContext testContext) {
            this.testContext = testContext;
        }

        public DatabaseConnections getConnections() {
            return (DatabaseConnections) getAttribute(CONNECTION_ATTRIBUTE);
        }

        public DataSetLoader getDataSetLoader() {
            return (DataSetLoader) getAttribute(DATA_SET_LOADER_ATTRIBUTE);
        }

        public DatabaseOperationLookup getDatbaseOperationLookup() {
            return (DatabaseOperationLookup) getAttribute(DATABASE_OPERATION_LOOKUP_ATTRIBUTE);
        }

        public Class<?> getTestClass() {
            return (Class<?>) ReflectionUtils.invokeMethod(GET_TEST_CLASS, this.testContext);
        }

        public Method getTestMethod() {
            return (Method) ReflectionUtils.invokeMethod(GET_TEST_METHOD, this.testContext);
        }

        public Object getTestInstance() {
            return ReflectionUtils.invokeMethod(GET_TEST_INSTANCE, this.testContext);
        }

        public Throwable getTestException() {
            return (Throwable) ReflectionUtils.invokeMethod(GET_TEST_EXCEPTION, this.testContext);
        }

        public ApplicationContext getApplicationContext() {
            return (ApplicationContext) ReflectionUtils.invokeMethod(GET_APPLICATION_CONTEXT, this.testContext);
        }

        public Object getAttribute(String name) {
            return ReflectionUtils.invokeMethod(GET_ATTRIBUTE, this.testContext, name);
        }

        public void setAttribute(String name, Object value) {
            ReflectionUtils.invokeMethod(SET_ATTRIBUTE, this.testContext, name, value);
        }

    }

}
