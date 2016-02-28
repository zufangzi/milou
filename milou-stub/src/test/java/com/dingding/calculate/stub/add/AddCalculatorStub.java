package com.dingding.calculate.stub.add;

import com.dingding.milou.stub.Stub;

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

    /**
     * 测试case3
     */
    @Stub("AddCalculatorStub_add_abnormal")
    public int add_abnormal() {
        return 100;
    }

}
