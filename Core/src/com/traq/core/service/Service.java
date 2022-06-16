package com.traq.core.service;


import com.traq.common.data.entity.Entity;
import com.traq.common.data.factory.EntityFactory;


public interface Service {

    public <T extends Entity> T getEntity();

    public EntityFactory getEntityFactory();

    public ServiceFactory getServiceFactory();


    public void setEntityFactory(EntityFactory entityFactory);

    public void setServiceFactory(ServiceFactory serviceFactory);

}
