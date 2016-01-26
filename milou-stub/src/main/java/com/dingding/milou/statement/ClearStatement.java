package com.dingding.milou.statement;

import org.junit.runners.model.Statement;

import com.dingding.milou.proxy.MockAutoProxyCreator;
import com.dingding.milou.repo.StubInfoRepo;
import com.dingding.milou.repo.StubObjectRepo;

public class ClearStatement extends Statement {

    private final Statement next;

    public ClearStatement(Statement statement) {
        next = statement;
    }

    @Override
    public void evaluate() throws Throwable {
        try {
            next.evaluate();
        } finally {
            // 全局变量清理
            StubInfoRepo.clearStubInfoRepo();
            StubObjectRepo.clear();
            MockAutoProxyCreator.clear();
        }
    }

}
