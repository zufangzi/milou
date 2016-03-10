package com.dingding.calculate.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.dingding.calculate.Calculate;
import com.dingding.milou.runner.MilouSpringJunitRunner;
import com.dingding.milou.situation.Situation;
import com.dingding.milou.situation.Situations;
import com.dingding.milou.stub.StubLocation;

@RunWith(MilouSpringJunitRunner.class)
@ContextConfiguration(locations = { "classpath:ctx-test.xml" })
@StubLocation("com.dingding.calculate.stub")
// 指定stub类的仓库位置
public class CalculateImplTest2 extends StubRepo {

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

    @Test
    @Situations({
            @Situation(StubId = "AddCalculatorStub_add_normal_withArgs",
                    Class = "beanId:addCalculatorImpl",
                    Method = "name:add"),
            @Situation(StubId = "SubstractCalculatorStub_sub_normal_withArgs",
                    Class = "substractCalculator",
                    Method = "sub")
    })
    public void test_add3() {
        int expect1 = 100;
        int expect2 = 100;
        int addActual = calculateInterface.add(10, 10);
        int subActual = calculateInterface.substract(10, 10);
        Assert.assertEquals(addActual, expect1);
        Assert.assertEquals(subActual, expect2);
    }

}
