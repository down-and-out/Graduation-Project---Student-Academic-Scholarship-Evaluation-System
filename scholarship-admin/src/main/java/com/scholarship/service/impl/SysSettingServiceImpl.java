package com.scholarship.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.dto.*;
import com.scholarship.entity.SysSetting;
import com.scholarship.mapper.SysSettingMapper;
import com.scholarship.service.SysSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "sys:settings")
public class SysSettingServiceImpl implements SysSettingService {

    private final SysSettingMapper sysSettingMapper;

    @Override
    @Cacheable(key = "#key")
    public <T> T getSetting(String key, Class<T> clazz) {
        SysSetting setting = getByKey(key);
        if (setting == null || setting.getSettingValue() == null) {
            return null;
        }
        try {
            return JSON.parseObject(setting.getSettingValue(), clazz);
        } catch (Exception e) {
            log.error("解析设置失败，key={}，value={}", key, setting.getSettingValue(), e);
            return null;
        }
    }

    @Override
    @Cacheable(key = "#key + '_typed'")
    public <T> T getSetting(String key, TypeReference<T> typeReference) {
        SysSetting setting = getByKey(key);
        if (setting == null || setting.getSettingValue() == null) {
            return null;
        }
        try {
            return JSON.parseObject(setting.getSettingValue(), typeReference);
        } catch (Exception e) {
            log.error("解析设置失败，key={}，value={}", key, setting.getSettingValue(), e);
            return null;
        }
    }

    @Override
    public <T> T getActiveSetting(String key, Class<T> clazz) {
        SysSetting setting = sysSettingMapper.selectActiveByKey(key);
        if (setting == null || setting.getSettingValue() == null) {
            return null;
        }
        try {
            return JSON.parseObject(setting.getSettingValue(), clazz);
        } catch (Exception e) {
            log.error("解析设置失败，key={}，value={}", key, setting.getSettingValue(), e);
            return null;
        }
    }

    @Override
    @CacheEvict(allEntries = true)
    public boolean updateSetting(String key, Object data) {
        String jsonString = JSON.toJSONString(data);

        SysSetting exist = getLatestActiveByKey(key);

        SysSetting setting = new SysSetting();
        setting.setSettingKey(key);
        setting.setSettingValue(jsonString);
        setting.setVersion(1);
        setting.setIsActive(1);

        boolean success;
        if (exist == null) {
            // 当前按单生效版本模型运行，缺失时直接新增一条 active 记录。
            log.info("新增系统设置，key={}", key);
            success = sysSettingMapper.insert(setting) > 0;
        } else {
            // 当前不启用版本切换，直接覆盖当前生效记录。
            log.info("更新系统设置，key={}，id={}", key, exist.getId());
            setting.setId(exist.getId());
            setting.setDescription(exist.getDescription());
            success = sysSettingMapper.updateById(setting) > 0;
        }

        return success;
    }

    @Override
    public Map<String, String> getAllSettings() {
        List<SysSetting> list = sysSettingMapper.selectList(new LambdaQueryWrapper<SysSetting>()
                .eq(SysSetting::getIsActive, 1));
        Map<String, String> result = new HashMap<>();
        for (SysSetting setting : list) {
            result.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return result;
    }

    @Override
    public SysSetting getByKey(String key) {
        return getLatestActiveByKey(key);
    }

    @Override
    public BasicSetting getBasicSetting() {
        return getSetting("basic", BasicSetting.class);
    }

    @Override
    public WeightSetting getWeightSetting() {
        return getSetting("weight", WeightSetting.class);
    }

    @Override
    public AwardConfig getAwardConfig() {
        return getSetting("awards", AwardConfig.class);
    }

    @Override
    public List<AwardRule> getAwardRules() {
        AwardConfig config = getAwardConfig();
        if (config == null || config.getRules() == null) {
            return List.of();
        }
        return config.getRules();
    }

    private SysSetting getLatestActiveByKey(String key) {
        List<SysSetting> activeSettings = sysSettingMapper.selectList(new LambdaQueryWrapper<SysSetting>()
                .eq(SysSetting::getSettingKey, key)
                .eq(SysSetting::getIsActive, 1)
                .orderByDesc(SysSetting::getUpdateTime)
                .orderByDesc(SysSetting::getId));
        if (activeSettings.isEmpty()) {
            return null;
        }
        if (activeSettings.size() > 1) {
            log.warn("检测到重复生效系统设置，key={}，activeCount={}，将使用最新一条", key, activeSettings.size());
            Long keepId = activeSettings.get(0).getId();
            for (int i = 1; i < activeSettings.size(); i++) {
                SysSetting duplicate = new SysSetting();
                duplicate.setId(activeSettings.get(i).getId());
                duplicate.setIsActive(0);
                sysSettingMapper.updateById(duplicate);
                log.warn("已自动停用重复系统设置，key={}，id={}，keepId={}", key, duplicate.getId(), keepId);
            }
        }
        return activeSettings.get(0);
    }
}
