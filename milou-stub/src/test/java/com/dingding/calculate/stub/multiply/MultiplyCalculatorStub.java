package com.dingding.calculate.stub.multiply;

public class MultiplyCalculatorStub {
    public static void main(String[] args) {
        String string = System.getProperty("java.class.path");
        String[] strings = string.split(";");
        for (int i = 0; i < strings.length; i++) {
            System.out.println(strings[i]);
        }
    }
}
