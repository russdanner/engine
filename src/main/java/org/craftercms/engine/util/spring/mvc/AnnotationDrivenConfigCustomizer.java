/*
 * Copyright (C) 2007-2014 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.engine.util.spring.mvc;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean post processor used to customize some of the beans defined by the Spring tag &lt;mvc:annotation-driven/&gt;,
 * which can't be overwritten.
 *
 * @author avasquez
 */
public class AnnotationDrivenConfigCustomizer implements BeanPostProcessor {

    private ContentNegotiationManager contentNegotiationManager;
    private List<HttpMessageConverter<?>> messageConverters;
    private boolean replaceMessageConverters;

    public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
        this.contentNegotiationManager = contentNegotiationManager;
    }

    public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
    }

    public void setReplaceMessageConverters(boolean replaceMessageConverters) {
        this.replaceMessageConverters = replaceMessageConverters;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RequestMappingHandlerAdapter) {
            RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter) bean;
            if (contentNegotiationManager != null) {
                adapter.setContentNegotiationManager(contentNegotiationManager);
            }
            if (CollectionUtils.isNotEmpty(messageConverters)) {
                adapter.setMessageConverters(getMessageConverters(adapter.getMessageConverters()));
            }
        } else if (bean instanceof ExceptionHandlerExceptionResolver) {
            ExceptionHandlerExceptionResolver exceptionResolver = (ExceptionHandlerExceptionResolver) bean;
            if (contentNegotiationManager != null) {
                exceptionResolver.setContentNegotiationManager(contentNegotiationManager);
            }
            if (CollectionUtils.isNotEmpty(messageConverters)) {
                exceptionResolver.setMessageConverters(getMessageConverters(exceptionResolver.getMessageConverters()));
            }
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private List<HttpMessageConverter<?>> getMessageConverters(List<HttpMessageConverter<?>> originalMessageConverters) {
        if (replaceMessageConverters) {
            return messageConverters;
        } else {
            List<HttpMessageConverter<?>> mergedConverters = new ArrayList<>(messageConverters);
            mergedConverters.addAll(originalMessageConverters);

            return mergedConverters;
        }
    }

}
