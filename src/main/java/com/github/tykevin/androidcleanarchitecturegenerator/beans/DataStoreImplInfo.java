package com.github.tykevin.androidcleanarchitecturegenerator.beans;

import com.intellij.psi.PsiClass;

public class DataStoreImplInfo {
    public boolean isNeedGenerate;
    public PsiClass generateInterface;
    public GenerateType generateType;

    @Override
    public String toString() {
        return "DataStoreImplInfo{" +
                "isNeedGenerate=" + isNeedGenerate +
                ", generateInterface=" + generateInterface +
                ", generateType=" + generateType +
                '}';
    }

    public enum GenerateType {
        NET("从网络获取"),
        DATABASE_GET("从数据库获取"),
        DATABASE_SAVE("保存至数据库"),
        SHARED_PREFERENCES_GET("从SP中获取"),
        SHARED_PREFERENCES_SAVE("保存至SP");

        public String desc;
        GenerateType(String desc) {
            this.desc = desc;
        }
    }
}
