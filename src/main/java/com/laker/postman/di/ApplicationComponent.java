package com.laker.postman.di;

import com.laker.postman.common.window.SplashWindow;
import com.laker.postman.service.ClientCertificateService;
import com.laker.postman.service.ExitService;
import com.laker.postman.service.HistoryPersistenceService;
import com.laker.postman.service.UpdateService;
import com.laker.postman.service.update.AutoUpdateManager;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Dagger Application Component
 * 定义应用程序级别的依赖注入组件
 */
@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    // 提供各个服务的访问方法
    SplashWindow splashWindow();

    UpdateService updateService();

    HistoryPersistenceService historyPersistenceService();

    ClientCertificateService clientCertificateService();

    ExitService exitService();

    AutoUpdateManager autoUpdateManager();

}

