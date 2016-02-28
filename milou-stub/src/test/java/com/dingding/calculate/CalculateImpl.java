package com.dingding.calculate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CalculateImpl implements Calculate {

    @Autowired
    private AddCalculator add;

    @Autowired
    private SubstractCalculator substract;

    @Autowired
    private MultiplyCalculator multiply;

    @Override
    public int add(int a, int b) {
        return add.add(a, b);
    }

    @Override
    public int substract(int a, int b) {
        return substract.sub(a, b);
    }

    @Override
    public int multiply(int a, int b) {
        return multiply.multiply(a, b);
    }
}
