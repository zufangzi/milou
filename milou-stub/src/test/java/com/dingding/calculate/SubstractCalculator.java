package com.dingding.calculate;

import org.springframework.stereotype.Component;

@Component
public class SubstractCalculator {

    public int sub(int a, int b) {
        return a - b;
    }

    public int sub() {
        return 300;
    }
}
