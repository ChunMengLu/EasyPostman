package com.laker.postman.di;

import lombok.extern.slf4j.Slf4j;

/**
 * BeanFactory - Dagger Component 访问工具类
 * 提供 ApplicationComponent 的访问入口
 */
@Slf4j
public class BeanFactory {
    private static ApplicationComponent applicationComponent;

    /**
     * 初始化 Dagger Component
     * 应该在应用程序启动时调用
     */
    public static void init() {
        if (applicationComponent == null) {
            applicationComponent = DaggerApplicationComponent.create();
            log.info("Dagger ApplicationComponent initialized");
        }
    }

    /**
     * 获取 ApplicationComponent 实例
     * 使用方式：BeanFactory.getApplicationComponent().updateService()
     */
    public static ApplicationComponent getComponent() {
        if (applicationComponent == null) {
            throw new IllegalStateException("BeanFactory not initialized. Call BeanFactory.init() first.");
        }
        return applicationComponent;
    }

    /**
     * 销毁 Component（清理资源）
     */
    public static void destroy() {
        if (applicationComponent != null) {
            // 调用 UpdateService 的清理方法
            try {
                applicationComponent.updateService().shutdown();
            } catch (Exception e) {
                log.error("Error during component shutdown", e);
            }
            applicationComponent = null;
            log.info("Dagger ApplicationComponent destroyed");
        }
    }
}

