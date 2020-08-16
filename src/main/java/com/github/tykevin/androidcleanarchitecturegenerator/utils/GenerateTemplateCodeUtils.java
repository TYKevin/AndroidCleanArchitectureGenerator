package com.github.tykevin.androidcleanarchitecturegenerator.utils;

import com.github.tykevin.androidcleanarchitecturegenerator.beans.BaseInfo;
import com.github.tykevin.androidcleanarchitecturegenerator.beans.DataStoreImplInfo;
import com.intellij.psi.PsiClass;

public class GenerateTemplateCodeUtils {

    public static String getTemplateCode(PsiClass dataSourceImplClass, DataStoreImplInfo dataStoreImplInfo, BaseInfo baseInfo) {
        String templateCode = null;
        DataStoreImplInfo.GenerateType generateType = dataStoreImplInfo.generateType;
        switch (generateType) {
            case NET:
                templateCode = getNetTemplateCode(dataSourceImplClass, dataStoreImplInfo, baseInfo);
                break;
            case DATABASE_GET:
                templateCode = getDatabaseGetTemplateCode(dataSourceImplClass, dataStoreImplInfo, baseInfo);
                break;
            case SHARED_PREFERENCES_GET:
                break;
        }

        return templateCode;
    }

    public static String getNetTemplateCode(PsiClass dataSourceImplClass, DataStoreImplInfo dataStoreImplInfo, BaseInfo baseInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" try\n");
        stringBuilder.append("{\n");
        stringBuilder.append("\t// TODO: 在 ").append(dataSourceImplClass.getName()).append("中添加URL链接\n");
        stringBuilder.append("    String responseJson = requestFromApi(").append(dataStoreImplInfo.generateDataSourceInterface.getQualifiedName()).append(".").append(baseInfo.getApiUrlName());
        if (!baseInfo.isVoidParam()) {
            stringBuilder.append(",");
            stringBuilder.append("com.qianmi.arch.util.GsonHelper.toJson(").append(baseInfo.getParamFieldName()).append(")");
        }
        stringBuilder.append(");\n");
        stringBuilder.append("      if (responseJson != null)\n");
        stringBuilder.append("      {\n");
        stringBuilder.append("\t// TODO: 替换 BaseResponseEntity 为 实际业务类  <Class extends BaseResponseEntity> \n");
        stringBuilder.append("         BaseResponseEntity response = GsonHelper.toType(response, BaseResponseEntity.class);\n");
        stringBuilder.append("          assert response != null;\n");
        stringBuilder.append("         if (com.qianmi.arch.util.GeneralUtils.isNotNullOrZeroLength(response.status)\n");
        stringBuilder.append("                 && response.status.equals(com.qianmi.arch.config.Config.RESPONSE_SUCCESS))\n");
        stringBuilder.append("         {\n");
        stringBuilder.append("             emitter.onNext(response.data);\n");
        stringBuilder.append("             emitter.onComplete();\n");
        stringBuilder.append("         }\n");
        stringBuilder.append("         else\n");
        stringBuilder.append("         {\n");
        stringBuilder.append("             emitter.onError(new com.qianmi.arch.domain.exception.DefaultErrorBundle(response.status, response.message));\n");
        stringBuilder.append("         }\n");
        stringBuilder.append("     }\n");
        stringBuilder.append("     else\n");
        stringBuilder.append("     {\n");
        stringBuilder.append("         emitter.onError(new com.qianmi.arch.domain.exception.DefaultErrorBundle());\n");
        stringBuilder.append("     }\n");
        stringBuilder.append(" } catch (Exception e)\n");
        stringBuilder.append(" {\n");
        stringBuilder.append("     emitter.onError(new com.qianmi.arch.domain.exception.DefaultErrorBundle());\n");
        stringBuilder.append(" }");
        return stringBuilder.toString();
    }

    public static String getDatabaseGetTemplateCode(PsiClass dataSourceImplClass, DataStoreImplInfo dataStoreImplInfo, BaseInfo baseInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("  try (io.realm.Realm mRealm = io.realm.Realm.getDefaultInstance())\n");
        stringBuilder.append("   {\n");
        stringBuilder.append("       // 查询\n");
        stringBuilder.append("\t// TODO：替换 RealmResultsType 为实际业务类\n");
        stringBuilder.append("       io.realm.RealmResults<RealmResultsType> results = mRealm.where(RealmResultsType.class).findAll();\n");
        stringBuilder.append("       emitter.onNext(mRealm.copyFromRealm(results));\n");
        stringBuilder.append("       emitter.onComplete();\n");
        stringBuilder.append("   } catch (Exception e)\n");
        stringBuilder.append("   {\n");
        stringBuilder.append("       com.qianmi.arch.util.ExceptionUtil.uploadRealmException(e);\n");
        stringBuilder.append("       com.qianmi.arch.util.QMLog.i(TAG, e.getCause().toString());\n");
        stringBuilder.append("       emitter.onError(new com.qianmi.arch.domain.exception.DefaultErrorBundle());\n");
        stringBuilder.append("   }\n" );
        return stringBuilder.toString();
    }

}
