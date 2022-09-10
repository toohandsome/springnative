package com.yxd.xiaomi2meidi.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.yxd.xiaomi2meidi.cache.Gcache;
import com.yxd.xiaomi2meidi.corn.RefreshToken;
import com.yxd.xiaomi2meidi.entity.MasRsp;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
public class Utils {


    public static Headers setHeaderParams(Map<String, String> headerParams) {
        Headers headers = null;
        Headers.Builder headersbuilder = new Headers.Builder();
        if (headerParams != null && headerParams.size() > 0) {
            for (String key : headerParams.keySet()) {
                if (headerParams.get(key) != null) {
                    headersbuilder.add(key, headerParams.get(key));
                }
            }
        }

        headers = headersbuilder.build();
        return headers;
    }

    public static String doExecute(OkHttpClient httpClient, Request request) {
        Call call = httpClient.newCall(request);
        try {
            String resp = call.execute().body().string();
            return resp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void checkResp(MasRsp resp) {
        if (resp.getCode() != 0) {
            if (resp.getCode() == 40002) {
                log.info("token 过期 , 尝试换取token");
                RefreshToken bean = SpringUtil.getBean(RefreshToken.class);
                bean.configureTasks();
            } else if (resp.getCode() == 1020) {
                throw new RuntimeException("请在手机上正常登录成功一次后再试");
            } else {
                throw new RuntimeException("非正常返回");
            }
        }
    }

    public static void writeConfig() {
        String pretty = JSON.toJSONString(Gcache.config, JSONWriter.Feature.PrettyFormat,
                JSONWriter.Feature.WriteMapNullValue,
                JSONWriter.Feature.WriteNullListAsEmpty);
        try {
            Files.writeString(Paths.get(Gcache.cache.get("configPath")), pretty);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
