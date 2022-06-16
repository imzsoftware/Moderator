package com.traq.core.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;


public class ServiceFactoryImpl implements ServiceFactory {

    private static BeanFactory beanFactory;

    public ServiceFactoryImpl() {
    }

    public static BeanFactory getBeanFactory() {
        return ServiceFactoryImpl.beanFactory;
    }

    public synchronized <T extends Service> T getService(String name)
            throws BeansException {
        if (name == null || name.length() == 0) {
            throw new NoSuchBeanDefinitionException("- invalid entity name!");
        }
        T service = (T) getBeanFactory().getBean(name);
        return service;
    }

    public void setBeanFactory(BeanFactory bFactory)
            throws BeansException {

        ServiceFactoryImpl.beanFactory = bFactory;
    }
}

