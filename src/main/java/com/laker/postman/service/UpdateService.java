package com.laker.postman.service;

import javax.inject.Inject;
import javax.inject.Singleton;
import com.laker.postman.service.update.AutoUpdateManager;
import lombok.extern.slf4j.Slf4j;

/**
 * 版本更新服务
 */
@Slf4j
@Singleton
public class UpdateService {

    private final AutoUpdateManager autoUpdateManager;

    @Inject
    public UpdateService(AutoUpdateManager autoUpdateManager) {
        this.autoUpdateManager = autoUpdateManager;
    }

    /**
     * 启动时异步检查更新
     */
    public void checkUpdateOnStartup() {
        // 启动后台更新检查
        autoUpdateManager.startBackgroundCheck();
    }

    /**
     * 手动检查更新（用于菜单调用）
     */
    public void checkUpdateManually() {
        autoUpdateManager.checkForUpdateManually()
                .thenAccept(updateInfo ->
                        autoUpdateManager.handleUpdateCheckResult(updateInfo, true))
                .exceptionally(throwable -> {
                    log.error("Manual update check failed", throwable);
                    return null;
                });
    }

    /**
     * 停止自动更新服务
     */
    public void shutdown() {
        autoUpdateManager.stopBackgroundCheck();
    }
}