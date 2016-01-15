package com.dingding.milou.dbunit.loader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.IDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.dingding.milou.dbunit.DatabaseConnections;
import com.dingding.milou.dbunit.DbUnitTestContext;
import com.github.springtestdbunit.dataset.DataSetLoader;
import com.github.springtestdbunit.dataset.FlatXmlDataSetLoader;

/**
 * 自定义的DataSetLoader，维护数据库初始化时候全部的表数据，每个case执行之后，利用此镜像数据还原数据库。 注解@DatabaseSetup或者@DatabaseTearDown的属性value值，如果是.xml结尾
 * 则读取xml文件的数据装载入数据库；如果是.sql结尾则读取.sql文件执行sql操作； 如果是数据库表的表名，则根据镜像实现该表的数据还原。
 * 
 * @author al
 * 
 */
@Component("dbUnitDataSetLoader")
public class MilouDataSetLoader implements DataSetLoader {

    private static final Logger logger = LoggerFactory.getLogger(MilouDataSetLoader.class);

    private final static String XML_FILE = ".xml";

    private final static String SQL_FILE = ".sql";

    private Map<String, IDataSet> bufferMap = new HashMap<String, IDataSet>();

    private DataSetLoader dataSetLoaderDelegate = null;

    private DbUnitTestContext testContext;

    private String conntion;

    public MilouDataSetLoader() {
        this.dataSetLoaderDelegate = new FlatXmlDataSetLoader();
    }

    /**
     * 根据注解@DatabaseSetup 或者@DatabaseTearDown的属性value值，进行xml文件、sql文件、数据库表的操作
     */
    @Override
    public IDataSet loadDataSet(Class<?> testClass, String location) throws Exception {
        if (isXMLFile(location)) {
            return doWithXMLFile(testClass, location);
        }
        if (isSQLFile(location)) {
            return doWithSQLFile(testClass, location);
        }
        if (isTable(location)) {
            return doWithTable(testClass, location);
        }
        throw new Exception("no such xml or sql or table named " + location);
    }

    /**
     * 如果注解@DatabaseSetup 或者@DatabaseTearDown的属性value值是.sql文件,则加载执行其中的sql语句，更新数据库。
     * 
     * @param testClass 测试类
     * @param sqlFile 注解@DatabaseSetup 或者@DatabaseTearDown 中的属性value之一
     * @return IDataSet 数据集
     * @throws IOException
     * @throws SQLException
     */
    private IDataSet doWithSQLFile(Class<?> testClass, String sqlFile) throws IOException, SQLException {
        Assert.notNull(testContext, "DBUnittestContext must not null !");
        Assert.notNull(conntion, "conntion from MilouDataSetLoader must not null !");
        DatabaseConnections connections = testContext.getConnections();
        IDatabaseConnection iDatabaseConn = connections.get(conntion);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(sqlFile);
        Connection connection = iDatabaseConn.getConnection();
        for (Resource res : resources) {
            ScriptUtils.executeSqlScript(connection, res);
        }
        // return some not important just avoid throwing exception
        return new DefaultDataSet();
    }

    /**
     * 如果注解@DatabaseSetup 或者@DatabaseTearDown的属性value值是.xml文件,则使用DBUnit的原本功能处理。
     * 
     * @param testClass 测试类
     * @param location 注解@DatabaseSetup 或者@DatabaseTearDown 中的属性value之一
     * @return IDataSet 数据集
     * @throws Exception
     */
    private IDataSet doWithXMLFile(Class<?> testClass, String location) throws Exception {
        return this.dataSetLoaderDelegate.loadDataSet(testClass, location);
    }

    /**
     * 如果注解@DatabaseSetup 或者@DatabaseTearDown的属性value值是数据库中某张表的表名,则利用镜像还原该表的数据。
     * 
     * @param testClass 测试类
     * @param location 注解@DatabaseSetup 或者@DatabaseTearDown 中的属性value之一
     * @return IDataSet 数据集
     * @throws DataSetException
     */
    private IDataSet doWithTable(Class<?> testClass, String tablename) throws DataSetException {
        logger.info("表[{}]数据还原！", tablename);
        return bufferMap.get(tablename);
    }

    private boolean isXMLFile(String location) {
        return location.endsWith(XML_FILE);
    }

    private boolean isSQLFile(String location) {
        return location.endsWith(SQL_FILE);
    }

    private boolean isTable(String location) {
        return bufferMap.containsKey(location);
    }

    public void put(String tablename, IDataSet dataSet) {
        this.bufferMap.put(tablename, dataSet);
    }

    public void clear() {
        this.bufferMap.clear();
    }

    public void setDbUnitTestContext(DbUnitTestContext testContext) {
        this.testContext = testContext;
    }

    public void setConnection(String conn) {
        this.conntion = conn;
    }

}
