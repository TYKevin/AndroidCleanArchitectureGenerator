package com.github.tykevin.androidcleanarchitecturegenerator.utils;

public class ClassNameUtils {

    /**
     * 切换类名（全路径）到方法名
     *
     * @param className
     * @return
     */
    public static String subClassNameToFuncName(String className) {
        if (className == null || className.length() <= 1) {
            return "";
        }

        // 不是全称，或者.是最后一个
        if (className.indexOf(".") <= 0 || className.lastIndexOf(".") == className.length() - 1) {
            return classNameToFuncName(className);
        }

        // 从全类名中截取类名
        className = className.substring(className.lastIndexOf(".") + 1, className.length());
        return classNameToFuncName(className);
    }

    /**
     * 切换类名（SimpleName）到方法名
     *
     * @param className
     * @return
     */
    private static String classNameToFuncName(String className) {
        if (className == null || className.length() <= 1) {
            return "";
        }

        return (change(className.charAt(0))) + className.substring(1, className.length());
    }

    /**
     * 大小写转换
     *
     * @param c
     * @return
     */
    private static char change(char c) {
        //如果输入的是大写，+32即可得到小写
        if (c >= 'A' && c <= 'Z') {
            return c += 32;
        } else if (c >= 'a' && c <= 'z') {    //如果输入的是小写，-32即可得大小写
            return c -= 32;
        } else {
            return c;
        }
    }

    /***
     * 驼峰命名转为下划线命名
     *
     * @param para 驼峰命名的字符串
     */
    public static String humpToUnderline(String para) {
        if (para == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(para);
        int temp = 0;//定位
        if (!para.contains("_")) {
            for (int i = 0; i < para.length(); i++) {
                if (Character.isUpperCase(para.charAt(i))) {
                    sb.insert(i + temp, "_");
                    temp += 1;
                }
            }
        }
        return sb.toString().toUpperCase();
    }
}
