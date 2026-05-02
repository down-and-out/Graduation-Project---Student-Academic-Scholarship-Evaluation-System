package com.scholarship.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.support.CacheConstants;
import com.scholarship.dto.*;
import com.scholarship.entity.SysSetting;
import com.scholarship.mapper.SysSettingMapper;
import com.scholarship.service.SysSettingService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置服务实现类
 *
 * 功能说明：
 * - 获取设置（支持 Class 和 TypeReference 两种类型反序列化方式）
 * - 获取当前生效设置（从 active 表查询）
 * - 更新设置（自动序列化为 JSON 并存储）
 * - 获取所有设置（key-value 形式）
 * - 获取快捷方法：getBasicSetting、getWeightSetting、getAwardConfig
 *
 * 设置类型：
 * - basic：基本设置（系统名称、学期、公告等）
 * - weight：评分权重（课程成绩、科研成果、综合素质权重）
 * - awards：奖项配置
 *
 * 缓存说明：
 * - 使用 Spring Cache 进行缓存
 * - 更新设置时清除所有相关缓存
 * - 检测并自动清理重复生效的设置记录
 *
 * 数据模型：
 * - 支持单条 active 记录模式
 * - 如果存在多条 active 记录，自动清理重复项
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "sys:settings")
public class SysSettingServiceImpl implements SysSettingService {

    private final SysSettingMapper sysSettingMapper;
    private final Validator validator;

    /**
     * 获取设置（使用 Class 类型反序列化）
     *
     * @param key   设置键
     * @param clazz 目标类型
     * @return 设置值（反序列化后的对象）
     */
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

    /**
     * 获取设置（使用 TypeReference 类型反序列化）
     * 用于复杂的泛型类型
     *
     * @param key          设置键
     * @param typeReference 包含目标类型的 TypeReference
     * @return 设置值（反序列化后的对象）
     */
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

    /**
     * 获取当前生效设置（从 active 表查询）
     *
     * @param key   设置键
     * @param clazz 目标类型
     * @return 设置值（反序列化后的对象）
     */
    @Override
    @Cacheable(value = CacheConstants.SYS_SETTING_ACTIVE, key = "#key")
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

    /**
     * 更新设置
     * 自动将数据序列化为 JSON 并存储
     * 更新前进行数据校验（如 weight 必须满足特定约束）
     *
     * @param key  设置键
     * @param data 设置值（对象）
     * @return 操作结果
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.SYS_SETTING, allEntries = true),
            @CacheEvict(value = CacheConstants.SYS_SETTING_ACTIVE, allEntries = true),
            @CacheEvict(value = CacheConstants.SYS_SETTINGS_ALL, allEntries = true)
    })
    public boolean updateSetting(String key, Object data) {
        // 1. 归一化和校验设置数据
        Object normalizedData = normalizeAndValidateSetting(key, data);
        // 2. 序列化为 JSON 字符串
        String jsonString = JSON.toJSONString(normalizedData);

        // 3. 查询当前生效的设置
        SysSetting exist = getLatestActiveByKey(key);

        SysSetting setting = new SysSetting();
        setting.setSettingKey(key);
        setting.setSettingValue(jsonString);
        setting.setVersion(1);
        setting.setIsActive(1);

        boolean success;
        if (exist == null) {
            // 当前按单生效版本模型运行，缺失时直接新增一条 active 记录
            log.info("新增系统设置，key={}", key);
            success = sysSettingMapper.insert(setting) > 0;
        } else {
            // 当前不启用版本切换，直接覆盖当前生效记录
            log.info("更新系统设置，key={}，id={}", key, exist.getId());
            setting.setId(exist.getId());
            setting.setDescription(exist.getDescription());
            success = sysSettingMapper.updateById(setting) > 0;
        }

        return success;
    }

    /**
     * 获取所有系统设置
     * 返回当前生效的所有设置（key-value 形式）
     */
    @Override
    @Cacheable(value = CacheConstants.SYS_SETTINGS_ALL, key = "'all'")
    public Map<String, String> getAllSettings() {
        List<SysSetting> list = sysSettingMapper.selectList(new LambdaQueryWrapper<SysSetting>()
                .eq(SysSetting::getIsActive, 1));
        Map<String, String> result = new HashMap<>();
        for (SysSetting setting : list) {
            result.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return result;
    }

    /**
     * 根据 key 获取设置
     */
    @Override
    public SysSetting getByKey(String key) {
        return getLatestActiveByKey(key);
    }

    /**
     * 获取基本设置（快捷方法）
     */
    @Override
    public BasicSetting getBasicSetting() {
        return getSetting("basic", BasicSetting.class);
    }

    /**
     * 获取评分权重设置（快捷方法）
     */
    @Override
    public WeightSetting getWeightSetting() {
        return getSetting("weight", WeightSetting.class);
    }

    /**
     * 获取奖项配置（快捷方法）
     */
    @Override
    public AwardConfig getAwardConfig() {
        return getSetting("awards", AwardConfig.class);
    }

    /**
     * 获取奖项规则列表（快捷方法）
     */
    @Override
    public List<AwardRule> getAwardRules() {
        AwardConfig config = getAwardConfig();
        if (config == null || config.getRules() == null) {
            return List.of();
        }
        return config.getRules();
    }

    /**
     * 获取最新的生效设置
     * 如果存在多条 active 记录，自动清理重复项
     */
    private SysSetting getLatestActiveByKey(String key) {
        List<SysSetting> activeSettings = sysSettingMapper.selectList(new LambdaQueryWrapper<SysSetting>()
                .eq(SysSetting::getSettingKey, key)
                .eq(SysSetting::getIsActive, 1)
                .orderByDesc(SysSetting::getUpdateTime)
                .orderByDesc(SysSetting::getId));
        if (activeSettings.isEmpty()) {
            return null;
        }
        // 如果存在多条生效记录，自动清理
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

    /**
     * 归一化和校验设置数据
     * - weight 设置需要进行约束校验
     * - 其他设置直接返回原数据
     */
    private Object normalizeAndValidateSetting(String key, Object data) {
        if (!"weight".equals(key)) {
            return data;
        }

        // weight 设置需要进行校验
        WeightSetting weightSetting = JSON.parseObject(JSON.toJSONString(data), WeightSetting.class);
        if (weightSetting == null) {
            throw new BusinessException("权重配置不能为空");
        }

        // 使用 Bean Validation 校验约束
        List<String> errors = validator.validate(weightSetting).stream()
                .map(ConstraintViolation::getMessage)
                .distinct()
                .toList();
        if (!errors.isEmpty()) {
            throw new BusinessException(String.join("; ", errors));
        }
        return weightSetting;
    }
}
