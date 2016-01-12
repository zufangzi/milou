package com.dingding.milou.statement;

import java.util.List;

import org.junit.runners.model.Statement;
import org.springframework.util.CollectionUtils;

import com.dingding.milou.repo.StubInfoRepo;
import com.dingding.milou.situation.SituationInfo;

/**
 * 在@Test方法执行之前获取场景信息，执行后清理；
 * 
 * @author al
 * 
 */
public class RunSituations extends Statement {

    private final Statement next;

    private List<SituationInfo> list;

    public RunSituations(Statement statement, List<SituationInfo> list) {
        this.next = statement;
        this.list = list;
    }

    @Override
    public void evaluate() throws Throwable {
        try {
            convertListToStubIdMap();
            next.evaluate();
        } finally {
            StubInfoRepo.clearStubIdMap();
        }
    }

    /**
     * 将situation信息处理后转入stubIdMap
     */
    private void convertListToStubIdMap() {
        if (!CollectionUtils.isEmpty(list)) {
            for (SituationInfo info : list) {
                String key = info.getFullExp();
                String value = info.getStubId();
                StubInfoRepo.setIntoStubIdMap(key, value);
            }
        }
    }
}
