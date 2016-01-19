package com.dingding.milou.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.runner.Version;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;

import com.dingding.milou.dbunit.MilouDbUnitTestExecutionListener;
import com.dingding.milou.dbunit.annotation.DBSetupSituation;
import com.dingding.milou.dbunit.annotation.DBSituations;
import com.dingding.milou.dbunit.annotation.DBTeardownSituation;
import com.dingding.milou.proxy.MockAutoProxyCreator;
import com.dingding.milou.repo.StubInfoRepo;
import com.dingding.milou.scanner.PackageScanner;
import com.dingding.milou.scanner.StubInfoScanner;
import com.dingding.milou.situation.Situation;
import com.dingding.milou.situation.SituationInfo;
import com.dingding.milou.situation.Situations;
import com.dingding.milou.statement.RunSituations;
import com.dingding.milou.stub.Stub;
import com.dingding.milou.stub.StubInfo;
import com.dingding.milou.stub.StubLocation;

public class MilouSpringJunitRunner extends SpringJUnit4ClassRunner {

    // 根据Method 确定其场景信息
    private static Map<FrameworkMethod, List<SituationInfo>> situationInfos =
            new HashMap<FrameworkMethod, List<SituationInfo>>();

    public MilouSpringJunitRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        String version = Version.id();
        // 在其他工程下添加的junit的依赖可能不是4.12，因此会绕过createTestClass()方法，打补丁
        if (!"4.12".equals(version)) {
            TestClass testClassWrapper = super.getTestClass();
            doWithMilouAnnotation(testClassWrapper);
        }
    }

    /**
     * 4.12版本特有方法
     */
    @Override
    protected TestClass createTestClass(Class<?> testClass) {
        TestClass testClassWrapper = super.createTestClass(testClass);
        doWithMilouAnnotation(testClassWrapper);
        return testClassWrapper;
    }

    /**
     * 添加@Situations、@Situation和@StubLocation注解的扫描
     */
    protected void doWithMilouAnnotation(TestClass testClassWrapper) {
        // 获取test方法-场景信息的map
        setAllSituationIntoMap(testClassWrapper);
        // stubInfo仓库初始化
        stubInfoRepoInit(testClassWrapper);
        // 是否需要MilouDBUnitExecutionListener
        switchMilouDBUnitTestExecutionListener(testClassWrapper);
    }

    /**
     * 判断是否需要执行MilouDbUnitTestExecutionListener
     * 
     * @param testClass
     */
    private void switchMilouDBUnitTestExecutionListener(TestClass testClass) {
        List<FrameworkMethod> DBSituations = testClass.getAnnotatedMethods(DBSituations.class);
        List<FrameworkMethod> DBSetupSituation = testClass.getAnnotatedMethods(DBSetupSituation.class);
        List<FrameworkMethod> DBTeardownSituation = testClass.getAnnotatedMethods(DBTeardownSituation.class);
        if (!CollectionUtils.isEmpty(DBSituations) || !CollectionUtils.isEmpty(DBSetupSituation)
                || !CollectionUtils.isEmpty(DBTeardownSituation)) {
            MilouDbUnitTestExecutionListener.executeListenerOrNot = true;
        }
    }

    private void setAllSituationIntoMap(TestClass testClass) {
        List<FrameworkMethod> frameworkMethods = testClass.getAnnotatedMethods(Test.class);
        for (FrameworkMethod method : frameworkMethods) {
            List<SituationInfo> list = getSituationInfos(method);
            if (!CollectionUtils.isEmpty(list)) {
                situationInfos.put(method, list);
            }
        }
    }

    /**
     * 获取@Test注解的Method的全部场景信息
     * 
     * @param method 测试类的@Test 方法
     * @return List<SituationInfo>
     */
    private List<SituationInfo> getSituationInfos(FrameworkMethod method) {
        List<SituationInfo> infos = new ArrayList<SituationInfo>();
        Situations multiSituation = method.getMethod().getAnnotation(Situations.class);
        if (multiSituation != null) {
            Situation[] value = multiSituation.value();
            for (Situation situation : value) {
                infos.add(getSituationInfo(situation));
            }
        }
        Situation singleSituation = method.getMethod().getAnnotation(Situation.class);
        if (singleSituation != null) {
            infos.add(getSituationInfo(singleSituation));
        }
        return infos;
    }

    /**
     * 获取指定的{@code situation}对象的属性信息
     * 
     * @param situation {@code situation}对象
     * @return SituationInfo 场景信息
     */
    private SituationInfo getSituationInfo(Situation situation) {
        SituationInfo info = null;
        if (situation != null) {
            info = new SituationInfo();
            info.setStubId(situation.StubId());
            info.setBeanId(situation.Class());
            info.setMethod(situation.Method());
            // 需要mock的对象beanId
            MockAutoProxyCreator.putIntoBeanIdSet(info.getBeanId());
        }
        return info;
    }

    /**
     * 完成Stub的元数据扫描,构建stub元数据仓库
     */
    private void stubInfoRepoInit(TestClass testClass) {
        // 加载本地stub仓库
        List<FrameworkMethod> stubMethods = testClass.getAnnotatedMethods(Stub.class);
        if (!CollectionUtils.isEmpty(stubMethods)) {
            for (FrameworkMethod fm : stubMethods) {
                StubInfo info = new StubInfo();
                String stubId = fm.getAnnotation(Stub.class).value();
                info.setStubId(stubId);
                info.setStubClass(testClass.getJavaClass());
                info.setStubMethod(fm.getMethod());
                StubInfoRepo.setIntoStubInfoRepo(stubId, info);
            }
        }
        StubLocation locationAnno = testClass.getJavaClass().getAnnotation(StubLocation.class);
        if (locationAnno != null && !locationAnno.value().equals(StubLocation.LOCAL)) {
            // 根据注解加载指定路径下的stub仓库
            List<Class<?>> scanList = PackageScanner.getStubClass(locationAnno.value());
            StubInfoScanner.scanAllStubClass(scanList, StubInfoRepo.getStubInfoRepo());
        }
    }

    @Override
    protected Statement methodBlock(FrameworkMethod frameworkMethod) {
        Statement statement = super.methodBlock(frameworkMethod);
        return withSituations(frameworkMethod, statement);
    }

    protected Statement withSituations(FrameworkMethod method, Statement statement) {
        List<SituationInfo> list = situationInfos.get(method);
        return list == null ? statement : new RunSituations(statement, list);
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        Statement last = super.withBeforeClasses(statement);
        // 全局变量清理
        // StubInfoRepo.clearStubIdMap();
        // StubInfoRepo.clearStubInfoRepo();
        // StubObjectRepo.clear();
        // situationInfos.clear();
        return last;
    }

}
