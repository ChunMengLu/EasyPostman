package com.laker.postman;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.laker.postman.common.window.SplashWindow;
import com.laker.postman.di.BeanFactory;
import com.laker.postman.util.I18nUtil;
import com.laker.postman.util.MessageKeys;
import com.laker.postman.util.StyleUtils;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * 程序入口类。
 */
@Slf4j
public class App {

    public static void main(String[] args) {
        // 0. 初始化 BeanFactory（在 EDT 之前，避免阻塞 UI）
        BeanFactory.init();

        // Swing 推荐在事件分派线程（EDT）中运行所有 UI 相关操作
        SwingUtilities.invokeLater(() -> {
            // 1. 设置主题
            FlatIntelliJLaf.setup();
            // 2. FlatLaf 统一商务风格属性（圆角、阴影等）
            StyleUtils.apply();
            // 3. 注册图标字体，使用 FontAwesome 图标库
            IconFontSwing.register(FontAwesome.getIconFont());
            // 4. 从 BeanFactory 获取 SplashWindow
            SplashWindow splash = BeanFactory.getComponent().splashWindow();
            // 5. 调用初始化方法
            splash.init();
            // 6. 异步加载主窗口
            splash.initMainFrame();
        });

        // 7. 设置全局异常处理器，防止程序因未捕获异常崩溃
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            log.error("Uncaught exception in thread: {}", thread.getName(), throwable);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    null,
                    I18nUtil.getMessage(MessageKeys.GENERAL_ERROR_MESSAGE),
                    I18nUtil.getMessage(MessageKeys.GENERAL_ERROR), JOptionPane.ERROR_MESSAGE
            ));
        });

        // 8. 注册应用程序关闭钩子，确保优雅关闭
        registerShutdownHook();

        // 9. 启动后台版本检查
        BeanFactory.getComponent().updateService().checkUpdateOnStartup();
    }

    /**
     * 注册应用程序关闭钩子
     */
    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Application shutting down...");
            try {
                // 销毁 BeanFactory，调用清理方法
                BeanFactory.destroy();
                log.info("Application shutdown completed");
            } catch (Exception e) {
                log.error("Error during application shutdown", e);
            }
        }, "ShutdownHook"));
    }
}