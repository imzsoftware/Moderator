package com.traq.core.service;

import com.traq.common.base.BaseInitializer;
import com.traq.common.data.entity.Entity;
import com.traq.common.data.factory.EntityFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class ServiceImpl extends BaseInitializer implements Service, ApplicationContextAware {

    private EntityFactory entityFactory;
    private ServiceFactory serviceFactory;
    private ApplicationContext applicationContext;
    private Integer retry;

    public ServiceImpl() {
        super("CORE_LOGGER");
        //  this.entityFactory = EntityFactoryImpl.getInstance();
    }

    public <T extends Entity> T getEntity() {
        return null;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    public ServiceFactory getServiceFactory() {
        return serviceFactory;
    }

    public void setServiceFactory(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public Integer getRetry() {
        Integer result = retry;
        if (result == null) {
            result = 1;
        }
        return result;
    }

    public void setRetry(Integer retry) {
        this.retry = retry;
    }
}
