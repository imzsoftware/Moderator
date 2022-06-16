package com.traq.core.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * Class: ServiceFactory.java
 */
public interface ServiceFactory extends BeanFactoryAware {

    public <T extends Service> T getService(String name)
            throws BeansException;

}
