package com.github.tykevin.androidcleanarchitecturegenerator.beans;

import com.intellij.psi.PsiClass;

public class DataStoreImplInfo {
    public boolean isNeedGenerate;
    public PsiClass generateDataSourceInterface;

    public String generateInterfaceFieldName;
    public GenerateType generateType;

    @Override
    public String toString() {
        return "DataStoreImplInfo{" +
                "isNeedGenerate=" + isNeedGenerate +
                ", generateInterface=" + generateDataSourceInterface +
                ", generateType=" + generateType +
                '}';
    }

    public enum GenerateType {
        NET("从网络获取"),
        DATABASE_GET("从数据库获取"),
        SHARED_PREFERENCES_GET("从SP中获取");

        public String desc;
        GenerateType(String desc) {
            this.desc = desc;
        }
    }
}
