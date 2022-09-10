package com.yxd.xiaomi2meidi.start;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.yxd.xiaomi2meidi.cache.Gcache;
import com.yxd.xiaomi2meidi.config.MqttProviderConfig;
import com.yxd.xiaomi2meidi.corn.RefreshToken;
import com.yxd.xiaomi2meidi.entity.Config;
import com.yxd.xiaomi2meidi.util.Utils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@Slf4j
public class AppStart implements CommandLineRunner {

    @Autowired
    private OkHttpClient httpClient;

    @Autowired
    RefreshToken refreshToken;

    @Override
    public void run(String... args) {
        try {
            File config = new File("config.json");
            Path configPath = Paths.get(config.getAbsolutePath());
            log.info("configPath: " + config.getAbsolutePath());
            Gcache.cache.put("configPath", config.getAbsolutePath());
            if (config.exists()) {
                String s = Files.readString(configPath);
                Config config1 = null;

                try {
                    config1 = JSON.parseObject(s, Config.class);
                } catch (Exception e) {
                    log.error("配置文件格式错误,请按照json 格式进行配置");
                    return;
                }
                if (config1 == null) {
                    log.error("配置文件为空");
                    return;
                }
                Gcache.config = config1;

                if (!StringUtils.hasText(config1.getPhone()) || !StringUtils.hasText(config1.getPassword()) || !StringUtils.hasText(config1.getAcNameList()) || !StringUtils.hasText(config1.getBlinkerKeyList())) {
                    log.error("配置不完整,请检查 phone,password,acNameList,blinkerKeyList 均已配置");
                } else {
                    String[] authKeyArr = config1.getBlinkerKeyList().replace("，", ",").split(",");
                    String[] nameArr = config1.getAcNameList().replace("，", ",").split(",");
                    if (authKeyArr.length != nameArr.length) {
                        log.error("配置不正确: acNameList,blinkerKeyList 数量是否相等");
                        return;
                    }

                    for (int i = 0; i < authKeyArr.length; i++) {
                        String authKey = authKeyArr[i].trim();
                        try {
                            Request request1 = new Request.Builder().url("https://iot.diandeng.tech/api/v1/user/device/diy/auth?authKey=" + authKey + "&protocol=mqtt")
                                    .get()
                                    .build();
                            String connectInfoStr = doExecute(request1).body().string();
                            log.info(connectInfoStr);
                            JSONObject parse = (JSONObject) JSON.parse(connectInfoStr);
                            JSONObject detail = (JSONObject) parse.get("detail");
                            MqttProviderConfig mqttProviderConfig = new MqttProviderConfig(httpClient);
                            mqttProviderConfig.deviceName = detail.getString("deviceName");
                            mqttProviderConfig.productKey = detail.getString("productKey");
                            mqttProviderConfig.authKey = authKey;

                            String host = detail.getString("host");
                            if (host.contains("aliyuncs")) {
                                mqttProviderConfig.isAliyun = true;
                            }
                            mqttProviderConfig.hostUrl = host
                                    .replaceFirst("mqtts:", "tcp:")
                                    .replaceFirst("mqtt:", "tcp:")
                                    + ":"
                                    + detail.getString("port");

                            mqttProviderConfig.iotId = detail.getString("iotId");
                            mqttProviderConfig.iotToken = detail.getString("iotToken");
                            mqttProviderConfig.uuid = detail.getString("uuid");
                            mqttProviderConfig.chineseName = nameArr[i].trim();

                            mqttProviderConfig.nomalMqttConnect();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    refreshToken.configureTasks();
                }

            } else {

                Config object = new Config();

                object.setDeviceId(getDeviceId());
                object.setAppVersion(getAppVersion());
                object.setDeviceName(getDeviceName());
                object.setOsVersion(getOsVer());
                Gcache.config = object;
                log.warn("config not exists , create new");
                config.createNewFile();
                Utils.writeConfig();
                log.warn("请关闭程序重新运行,在配置文件: " + config.getAbsolutePath() + " 中输入 phone: 手机号, password: 密码,acNameList: 空调名称(多个用逗号隔开), blinkerKeyList: 点灯的authkey(多个用逗号隔开,需要与空调名称一一对应)  ");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private Response doExecute(Request request) {
        Call call = httpClient.newCall(request);
        try {
            Response resp = call.execute();
            return resp;
        } catch (IOException e) {
            log.error("第三方请求失败，Body: {}", e);
            e.printStackTrace();
        }
        return null;
    }

    public String getDeviceId() {
        String uuid = UUID.randomUUID().toString();
        byte[] bytes = ConcurrentMessageDigest.digestMD5(uuid.getBytes(StandardCharsets.UTF_8));
        return MD5Encoder.encode(bytes).substring(0, 16);
    }


    private String getAppVersion() {
        List<String> verList = new ArrayList<>();
        verList.add("8.10.0.91");
        verList.add("8.9.0.87");
        verList.add("8.9.0.86");
        verList.add("8.9.0.85");
        verList.add("8.8.0.78");
        verList.add("8.7.0.71");
        verList.add("8.6.0.63");
        verList.add("8.5.0.58");
        verList.add("8.4.0.52");
        verList.add("8.3.0.45");
        verList.add("8.2.0.34");
        verList.add("8.1.1.23");
        verList.add("8.1.0.16");
        verList.add("8.0.1.7");
        verList.add("8.0.0.3");
        verList.add("7.13.1.130");
        verList.add("7.12.0.117");
        verList.add("7.12.0.115");
        verList.add("7.11.1.1");
        verList.add("7.11.0.116");
        verList.add("7.11.0.115");
        verList.add("7.11.0.107");
        verList.add("7.10.1.1");
        verList.add("7.10.0.38");
        verList.add("7.9.1.2");
        verList.add("7.9.0.56");
        verList.add("7.9.0.53");
        verList.add("7.9.0.42");
        verList.add("7.8.0.59");
        verList.add("7.7.1.1");
        verList.add("7.7.0.129");
        verList.add("7.6.1.1");
        verList.add("7.6.0.60");
        int ran = new Random().nextInt(32);
        return verList.get(ran);
    }

    private String getDeviceName() {
        List<String> devList = new ArrayList<>();
        devList.add("Samsung Galaxy S10");
        devList.add("Samsung Galaxy S20");
        devList.add("Samsung Galaxy Note 10");
        devList.add("Oppo R11 Plus");
        devList.add("Redmi Note 9");
        devList.add("Huawei Mate 30");
        devList.add("Huawei P30 Pro");
        devList.add("HUAWEI Mate S");
        devList.add("HUAWEI Mate 8");
        devList.add("HUAWEI Mate 9");
        devList.add("HUAWEI Mate 10");
        devList.add("HUAWEI Mate 20");
        devList.add("HUAWEI nova 2 ");
        devList.add("HUAWEI nova 2s");
        devList.add("HUAWEI nova 3e");
        devList.add("HUAWEI nova 3 ");
        devList.add("HUAWEI nova 3i");
        devList.add("HUAWEI nova 4 ");
        devList.add("Xiaomi MI 6");
        devList.add("Xiaomi MI 7");
        devList.add("Xiaomi MI 8");
        devList.add("Xiaomi MI 9");
        devList.add("Xiaomi MI 10");
        devList.add("Xiaomi MI 11");
        devList.add("Xiaomi MI 12");
        int ran = new Random().nextInt(24);
        return devList.get(ran);
    }

    private String getOsVer() {
        List<String> osList = new ArrayList<>();
        osList.add("7");
        osList.add("8");
        osList.add("9");
        osList.add("10");
        osList.add("11");
        osList.add("12");
        int ran = new Random().nextInt(6);
        return osList.get(ran);
    }


}
