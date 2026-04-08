package com.scholarship.service;

import com.scholarship.entity.SysSetting;

import java.util.List;
import java.util.Map;

/**
 * 系统设置服务接口
 */
public interface SysSettingService {

    /**
     * 根据 key 获取设置值（自动反序列化）
     *
     * @param key   设置键
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 解析后的对象
     */
    <T> T getSetting(String key, Class<T> clazz);

    /**
     * 根据 key 获取设置值（支持复杂泛型，如 List<T>）
     *
     * @param key           设置键
     * @param typeReference 类型引用
     * @param <T>           泛型类型
     * @return 解析后的对象
     */
    <T> T getSetting(String key, com.alibaba.fastjson2.TypeReference<T> typeReference);

    /**
     * 获取当前生效的设置
     *
     * @param key   设置键
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 解析后的对象
     */
    <T> T getActiveSetting(String key, Class<T> clazz);

    /**
     * 更新设置（自动序列化为 JSON）
     *
     * @param key  设置键
     * @param data 设置数据对象
     * @return 是否成功
     */
    boolean updateSetting(String key, Object data);

    /**
     * 获取所有设置
     *
     * @return key-value 映射
     */
    Map<String, String> getAllSettings();

    /**
     * 根据 key 查询实体（原始方法）
     *
     * @param key 设置键
     * @return SysSetting 实体
     */
    SysSetting getByKey(String key);

    /**
     * 获取基本设置（便捷方法）
     *
     * @return BasicSetting
     */
    com.scholarship.dto.BasicSetting getBasicSetting();

    /**
     * 获取权重设置（便捷方法）
     *
     * @return WeightSetting
     */
    com.scholarship.dto.WeightSetting getWeightSetting();

    /**
     * 获取奖项配置（便捷方法）
     *
     * @return AwardConfig
     */
    com.scholarship.dto.AwardConfig getAwardConfig();

    /**
     * 获取所有奖项规则列表（便捷方法）
     *
     * @return List<AwardRule>
     */
    List<com.scholarship.dto.AwardRule> getAwardRules();
}
