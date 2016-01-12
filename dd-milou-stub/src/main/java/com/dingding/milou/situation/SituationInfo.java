package com.dingding.milou.situation;

/**
 * 场景信息
 * 
 * @author al
 * 
 */
public class SituationInfo {
    // 桩的方法id
    private String stubId;
    // 被mock的目标类
    private String beanId;
    // 被mock的目标方法
    private SituationMethod method;

    private String fullExp;

    public String getStubId() {
        return stubId;
    }

    public void setStubId(String stubId) {
        this.stubId = stubId;
    }

    public String getBeanId() {
        return beanId;
    }

    public void setBeanId(String classExp) {
        this.beanId = SituationParser.parseClassExp(classExp);
    }

    public SituationMethod getMethod() {
        return method;
    }

    public void setMethod(String methodExp) {
        this.method = SituationParser.parseMethodExp(methodExp);
    }

    public void setMethod(SituationMethod method) {
        this.method = method;
    }

    public String getFullExp() {
        if (fullExp == null) {
            String classExp = SituationParser.createClassExp(beanId);
            String methodExp = method.getMethodExp();
            fullExp = SituationParser.joinClassMethod(classExp, methodExp);
        }
        return fullExp;
    }

    public void setFullExp(String fullExp) {
        this.fullExp = fullExp;
    }

    // 对应@Situation中Method属性
    public static class SituationMethod {

        private String methodName;

        private String[] paramType;

        private boolean isOverLoaded;

        private String methodExp;// method:add;paramType:int,int 或者 method:add 或者 add

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String[] getParamType() {
            return paramType;
        }

        public void setParamType(String[] paramType) {
            this.paramType = paramType;
        }

        public boolean isOverLoaded() {
            return isOverLoaded;
        }

        public void setOverLoaded(boolean isOverLoaded) {
            this.isOverLoaded = isOverLoaded;
        }

        public String getMethodExp() {
            return methodExp;
        }

        public void setMethodExp(String methodExp) {
            this.methodExp = methodExp;
        }

        @Override
        public String toString() {
            return methodExp;
        }

    }

}
