#Milou
##Introdution
- Milou是基于Spring、JUnit和DBUnit的单元测试框架，实现桩数据的复用，降低业务开发人员单测构造桩的开发成本。同时避免各个测试case因为数据库的数据出现耦合。
- 单元测试要求有很强的独立性，不论测试环境还是测试case的方法执行顺序怎样变化，都要保证测试结果的一致性。纯粹的单测要求对环境对依赖有很强的隔离性。
- 针对外部api的依赖，采用基于Spring对依赖进行mock的处理，mock切面会根据场景注解路由到开发人员已经写好的桩方法上，避免外部依赖的真正执行。桩方法可以做成动态，根据入参返回不同结果，可以实现桩数据复用。
- 针对外部数据库的依赖，采用Spring+H2+DBUnit数据库。同时利用注解@DBSituations实现测试方法执行前后的数据库的数据清理。避免case之间出现执行顺序的耦合。

##Modules
- milou-stub 通过注解@Situation实现基于Spring的单元测试中桩数据的路由的功能
- milou-db 通过注解@DBSituations实现基于DBUnit的单元测试中各个案例之间对数据库依赖的数据隔离的功能
- milou-assert 提供一些更加丰富的断言工具方法

##QUICKSTART
以下将通过一个常见的使用case来简单说明如何使用Milou框架，在Milou的单测中有该示例
1. **添加pom依赖**
```
<milou.stub.version>1.0.0-SNAPSHOT</milou.stub.version>    
<dependency>
    <groupId>com.dingding</groupId>
    <artifactId>milou-stub</artifactId>
    <version>${milou.stub.version}</version>
</dependency>
```
2. **修改数据库配置文件ctx-virtual_db.xml**
在Milou test中有此配置文件，关于dbunit的配置可自行google
```
<!-- H2 dataSource -->
<bean id="virtual_db" class="org.h2.jdbcx.JdbcDataSource">
    <property name="url" value="jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MYSQL;IGNORECASE=TRUE;" />
    <property name="user" value="sa" />
    <property name="password" value="" />
</bean>
 
<!-- dataSource init -->
<jdbc:initialize-database data-source="virtual_db">
    <jdbc:script location="classpath:database/virtual_db-schema.sql" />
    <jdbc:script location="classpath:database/virtual_db-data.sql" />
</jdbc:initialize-database>
 
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="virtual_db" />
    <property name="configLocation" value="classpath:META-INF/spring/domain/mybatis-config.xml" />
</bean>
<!-- 配置dbUnit框架的特有的数据库连接，注入属性，dataSource是实际的H2数据源， databaseConfig是H2数据库连接的配置信息-->
<bean id="dbUnitDatabaseConnection"
    class="com.dingding.milou.dbunit.bean.DatabaseDataSourceConnectionFactoryBean">
    <property name="databaseConfig" ref="dbUnitDatabaseConfig" />
    <property name="dataSource" ref="virtual_db" />
</bean>
  
<!-- 数据库连接配置信息，注入H2数据类型工厂，如果不注入，本人在做demo的时候报warning 数据类型有不兼容 -->
<bean id="dbUnitDatabaseConfig" class="com.dingding.milou.dbunit.bean.DatabaseConfigBean">
    <property name="datatypeFactory" ref="h2DataTypeFactory" />
    <property name="allowEmptyFields" value="true"/>
</bean>
  
<!-- datatype config for H2 type compatible-->
<bean id="h2DataTypeFactory" class="org.dbunit.ext.h2.H2DataTypeFactory" />
<!--dbUnitDataSetLoader -->
<bean id="dbUnitDataSetLoader" class="com.dingding.milou.dbunit.loader.MilouDataSetLoader" />
```
3. **桩数据**
Situation  描述了桩数据路由的场景和路由信息，属性Class值为Spring中某个bean的id值，属性Method值是该bean的某个方法签名，属性Stub值是调用该方法时候路由到的桩方法的stubId值，这个值就是@Stub注解的value值
@Situations 描述了桩数据复用的多场景信息，value为数组，对应上面的@Situation注解
@StubLocation 描述了桩数据的位置信息
3.1 **测试类示例**
```
@RunWith(MilouSpringJunitRunner.class)
@ContextConfiguration(locations = { "classpath:ctx-test.xml" })
@StubLocation("com.dingding.calculate.stub")
// 指定stub类的仓库位置
public class CalculateImplTest extends StubRepo {
    @Autowired
    private Calculate calculateInterface;
    /**
     * 测试case1,id为"AddCalculatorStub_add_normal_noArgs"的桩的返回值是500。
     * calculateInterface.add(int,int)方法依赖beanId为"addCalculator"的add方法
     * calculateInterface.substract(int,int)方法依赖beanId为"substractCalculator"的sub方法 多个mock场景写在@Situations中
     */
    @Test
    @Situations({
            @Situation(StubId = "AddCalculatorStub_add_normal_noArgs",
                    Class = "beanId:addCalculatorImpl",
                    Method = "name:add"),
            @Situation(StubId = "SubstractCalculatorStub_sub_normal_withArgs",
                    Class = "substractCalculator",
                    Method = "sub")
    })
    public void test_add1() {
        int expect1 = 500;
        int expect2 = 100;
        int addActual = calculateInterface.add(10, 10);
        int subActual = calculateInterface.substract(10, 10);
        Assert.assertEquals(addActual, expect1);
        Assert.assertEquals(subActual, expect2);
    }
    /**
     * 测试case2
     */
    @Test
    @Situations({
            @Situation(StubId = "AddCalculatorStub_add_normal_withArgs", Class = "beanId:addCalculatorImpl",
                    Method = "name:add;paramType:int,int"),
            @Situation(StubId = "SubstractCalculatorStub_sub_normal_noArgs",
                    Class = "substractCalculator",
                    Method = "sub")
    })
    public void test_add2() {
        int expect1 = 200;
        int expect2 = 100;
        int addActual = calculateInterface.add(10, 10);
        int subActual = calculateInterface.substract(10, 10);
        Assert.assertEquals(addActual, expect2);
        Assert.assertEquals(subActual, expect1);
    }
```
3.2**桩示例**
```
public class AddCalculatorStub {
    /**
     * 测试case1，实际测试中如果返回只是简单的数据，不是构造代码冗长的返回结果的情况下，可以使用其他mock框架。 此只为实例，桩数据复用是为了减少重复造轮子。
     */
    @Stub("AddCalculatorStub_add_normal_noArgs")
    public int add_normal() {
        return 500;
    }
    /**
     * 测试case2，带响应逻辑的桩
     */
    @Stub("AddCalculatorStub_add_normal_withArgs")
    public int add_normal(int a, int b) {
        if (a < 10) {
            return a;
        }
        if (b > 10) {
            return b;
        }
        return a * b;
    }
}
```
3.3 关于dubbo暴露的api服务，额外添加配置，如下：
```
<bean id="blacklistAPI" class="org.mockito.Mockito" factory-method="mock">
        <constructor-arg value="com.zufangzi.xxx.api.xxxAPI"/>
 </bean>
```
3.4 @Situation的用法
```
public @interface Situation {
    /**
     * id 指明依赖的方法需要路由到哪一个桩的id
     */
    String StubId();
    /**
     * 被mock的目标对象 格式写法： 1、Class="beanId:xxxx" beanId是被mock对象在spring中的beanId； 默认可以不写"beanId"
     */
    String Class();
    /**
     * 被mock的目标方法 格式写法： Method="name:xxxx",xxx是方法名，默认可以不写"name";如果方法名无法确定唯一性，即有重载情况，采用：
     * Method="name:xxxx;paramType=aaa,bbbb", xxx是方法名，aaa和bbb分别是方法参数的类型的getSimpleName()的返回值；按照参数顺序，有多少个参数，有多少个类型。
     */
    String Method();
}
```
4. **数据隔离**
@DBSetupSituation 描述了当前case在执行之前对数据库的操作，目前支持三种情况：1、对部分表的数据还原；2、执行sql文件（sql文件中插入的数据不应该和数据库的数据冲突）；3、xml格式的数据文件。
@DBTeardownSituation 描述了当前case在执行之后对数据库的操作，目前支持三种情况：1、对部分表的数据还原；2、执行sql文件（sql文件中插入的数据不应该和数据库的数据冲突）；3、xml格式的数据文件。
@DBSituations 属性setup对应着@DBSetupSituation 属性teardown对应着@DBTeardownSituation，均为数组。
```
@RunWith(MilouSpringJunitRunner.class)
@ContextConfiguration({ "classpath:ctx-virtual_db.xml", "classpath:ctx.xml" })
public class StaffDaoTest {
    // 待测试类1
    @Autowired
    private StaffDao staffDao;
    // 待测试类2，为了演示在每个@test执行完或者执行前实现嵌入式数据库（采用H2）的多张表的数据回归，增加的待测试类，实际每个单测只有一个带测试类。
    @Autowired
    private StaffDeptDao staffDeptDao;
    @Test
    // 建议在修改、删除数据库相关表的数据后进行数据回归的时候采用此标签。保证污染数据的case能够自己在case结束后去清理。
    // 还原staff和staff_dept两张表的数据。
    @DBSituations(
            teardown = { @DBTeardownSituation({ "staff", "staff_dept" }) })
    public void test_deleteById() {
        // case 1:
        // 按照staffId为1L进行删除
        StaffDept staffDept = new StaffDept();
        staffDept.setStaffId(1L);
        this.staffDeptDao.deleteByStaffId(staffDept);
        List<StaffDeptVo> list = staffDeptDao.selectByStaffIds(Arrays.asList(new Long[] { 1L }));
        // 断言list为empty
        Assert.assertTrue(list.isEmpty());
        // case 2
        // 删除id为1L的staff
        this.staffDao.deleteById(1L);
        Staff staff = staffDao.selectById(1L);
        // 断言staff为null
        Assert.assertNull(staff);
    }
    @Test
    // 上一个case执行完删除了id为1L的staff，如果数据库相关表的数据状态不回归，则会影响此case的断言。但是上一个case使用@DatabaseTearDown进行了相关表的回归
    @DBSetupSituation({ "staff", "staff_dept" })
    public void test_selectById() {
        // case1
        // 按照staffId为1L进行查找员工部门
        List<StaffDeptVo> list = staffDeptDao.selectByStaffIds(Arrays.asList(new Long[] { 1L }));
        // 断言list不为empty
        Assert.assertTrue("测试类: " + StaffDaoTest.class.getName() + ";  测试方法: test_selectById assert error",
                list.size() == 2);
        // case2
        // 查找id为1L的staff
        Staff staff = staffDao.selectById(1L);
        // 断言staff不为null
        Assert.assertNotNull(staff);
    }
    @Test
    @DBSituations(
            setup = { @DBSetupSituation(value = {
                    "classpath:database/virtual_db-data_staff_dept.sql",
                    "classpath:database/virtual_db-data_staff_dept2.sql" }) })
    public void test_insert_sql() {
        // case3 添加sql文件，测试完后回归，如果不回归，会影响下一个测试。
        // 按照staffId为1L进行查找员工部门
        List<StaffDeptVo> list = staffDeptDao.selectByStaffIds(Arrays.asList(new Long[] { 1L }));
        // 断言list不为empty
        Assert.assertTrue("测试类: " + StaffDaoTest.class.getName() + ";  测试方法: test_insert_sql assert error",
                list.size() == 8);
    }
```

##使用规范
桩的写法规范
1. 桩所在的类名：com.dingding.{module}.stub.**.{被替换的类名称}Stub
module是业务模块名称；**表示可以存在子级多层目录；
2. 桩方法：方法名与被替换的方法名尽可能保持一致，考虑到一个方法对应的stub可能有多个，都写在一个类中，可以添加下划线拼后缀来区分；
                    参数可有可无，有则必须和原本的方法的参数保持一致。
3. @Stub的value值，即stubId：{类的简单名称}_{被mock的方法名称}_{是否带有参数}_{模拟的状态}_{**}
是否带有参数：noArgs--可以没有参数、anyArgs--有参数，可以任意、withArgs--有参数，有具体要求，具体参数因为数据结构复杂，宜在注释中说明。 
模拟状态：normal–正常 exception–异常等，如描述不够清晰，可以添加"_"进一步描述状态，目的是表明用途，降低代码维护和别人复用的难度。

##Contact Us
inf@zufangit.cn

##Changelog
**v0.1 —— 2016-02-28**
+ 实现单测桩数据复用；
+ 实现单测db数据隔离

