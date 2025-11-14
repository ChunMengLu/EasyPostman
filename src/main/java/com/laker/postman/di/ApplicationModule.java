package com.laker.postman.di;

import com.laker.postman.service.update.AutoUpdateManager;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger Application Module
 * 提供需要特殊构造逻辑的 Bean
 */
@Module
public class ApplicationModule {

    @Provides
    @Singleton
    AutoUpdateManager provideAutoUpdateManager() {
        return new AutoUpdateManager();
    }
}

