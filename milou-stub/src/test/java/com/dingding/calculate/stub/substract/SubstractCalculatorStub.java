package com.dingding.calculate.stub.substract;

import com.dingding.milou.stub.Stub;

public class SubstractCalculatorStub {
    /**
     * 测试case1
     */
    @Stub("SubstractCalculatorStub_sub_normal_noArgs")
    public int sub_normal() {
        return 200;
    }

    /**
     * 测试case2
     */
    @Stub("SubstractCalculatorStub_sub_normal_withArgs")
    public long sub_normal(int a, long b) {
        if (a < 10) {
            return a;
        }
        if (b > 10) {
            return b;
        }
        return a * b;
    }

}
