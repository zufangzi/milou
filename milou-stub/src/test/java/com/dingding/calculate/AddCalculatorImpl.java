package com.dingding.calculate;

import org.springframework.stereotype.Component;

@Component
public class AddCalculatorImpl implements AddCalculator {

    @Override
    public int add(int a, int b) {
        return a + b;
    }

}
