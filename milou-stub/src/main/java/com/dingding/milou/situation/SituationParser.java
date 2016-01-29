package com.dingding.milou.situation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.dingding.milou.situation.SituationInfo.SituationMethod;
import com.dingding.milou.util.ArrayUtils;

public class SituationParser {

    private static final String CLASS_EXP_REG = "^beanId:.+$";

    private static final String METHOD_REG = "^name:.+;paramType:(.+,)*.*$";

    private static final String BEAN_ID = "beanId";

    private static final String KV_SEPARATOR = ":";

    private static final String METHOD_EXP_SEPARATOR = ";";

    private static final String METHOD_PARAMS_SEPARATOR = ",";

    private static final String METHOD_NAME = "name";

    private static final String METHOD_PARAMTYPE = "paramType";

    private static final String CONNECTOR_CLASS_METHOD = "_";

    private SituationParser() {
    }

    /**
     * 解析@Situation中属性Class的值
     * 
     * @param classExp
     * @return
     */
    public static String parseClassExp(String classExp) {
        Pattern r = Pattern.compile(CLASS_EXP_REG);
        Matcher m = r.matcher(classExp);
        if (m.matches()) {
            return classExp.split(KV_SEPARATOR)[1].trim();
        }
        return classExp;
    }

    /**
     * 解析@Situation中属性Method的值
     * 
     * @param methodExp
     * @return
     */
    public static SituationMethod parseMethodExp(String methodExp) {
        SituationMethod method = new SituationMethod();
        Pattern r = Pattern.compile(METHOD_REG);
        Matcher m = r.matcher(methodExp);
        if (m.matches()) {
            // overLoaded
            String[] nameString = methodExp.split(METHOD_EXP_SEPARATOR);
            method.setMethodName(nameString[0].split(KV_SEPARATOR)[1]);
            method.setParamType(nameString[1].split(METHOD_PARAMS_SEPARATOR));
            method.setMethodExp(methodExp);
            method.setOverLoaded(true);
        } else {
            // not overLoaded
            String[] methodSplit = methodExp.split(KV_SEPARATOR);
            String methodName = null;
            if (methodSplit.length == 1) {
                methodName = methodSplit[0];
                method.setMethodExp(createMethodExp(methodName, null));
            } else {
                methodName = methodSplit[methodSplit.length - 1];
                method.setMethodExp(methodExp);
            }
            method.setMethodName(methodName);
            method.setOverLoaded(false);
        }
        return method;
    }

    /**
     * 创建类的表达式，如：beanId:substractCalculator
     * 
     * @param beanId
     * @return
     */
    public static String createClassExp(String beanId) {
        if (!StringUtils.hasLength(beanId)) {
            return null;
        }
        StringBuilder builder = new StringBuilder(BEAN_ID);
        builder.append(KV_SEPARATOR).append(beanId);
        return builder.toString();
    }

    /**
     * 创建方法的表达式，如：method:sub或者是：method:sub;paramType:int,int
     * 
     * @param methodName
     * @param paramType
     * @return
     */
    public static String createMethodExp(String methodName, Class<?>[] paramType) {
        if (!StringUtils.hasLength(methodName)) {
            return null;
        }
        StringBuilder builder = new StringBuilder(METHOD_NAME).append(KV_SEPARATOR)
                .append(methodName);
        if (ArrayUtils.isEmpty(paramType)) {
            return builder.toString();
        }
        builder.append(METHOD_EXP_SEPARATOR).append(METHOD_PARAMTYPE).append(KV_SEPARATOR);
        for (Class<?> param : paramType) {
            builder.append(param.toString()).append(METHOD_PARAMS_SEPARATOR);
        }
        // 去除逗号
        return builder.substring(0, builder.length() - 1);
    }

    /**
     * 根据当前调用的方法名和类的全限定名构建situation复杂表达式，如beanId:substractCalculator_method:sub;paramType:int,int
     * 
     * @param beanId
     * @param methodName
     * @param paramType
     * @return
     */
    public static String createClassMethodExp(String beanId, String methodName, Class<?>[] paramType) {
        String classExp = createClassExp(beanId);
        String methodExp = createMethodExp(methodName, paramType);
        return joinClassMethod(classExp, methodExp);
    }

    /**
     * 根据当前调用的方法名和类的全限定名构建situation简单表达式，如beanId:substractCalculator_method:sub
     * 
     * @param clsExp
     * @param methodExp
     * @return
     */
    public static String createClassMethodExp(String beanId, String methodName) {
        return createClassMethodExp(beanId, methodName, null);
    }

    /**
     * 连接class表达式和method表达式
     * 
     * @param classExp
     * @param methodExp
     * @return
     */
    public static String joinClassMethod(String classExp, String methodExp) {
        return classExp + CONNECTOR_CLASS_METHOD + methodExp;
    }

}
