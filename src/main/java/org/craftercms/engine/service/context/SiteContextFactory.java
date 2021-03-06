/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.engine.service.context;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import groovy.lang.GroovyClassLoader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration.ConfigurationBuilder;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.store.impl.filesystem.FileSystemContentStoreAdapter;
import org.craftercms.core.url.UrlTransformationEngine;
import org.craftercms.engine.exception.SiteContextCreationException;
import org.craftercms.engine.macro.MacroResolver;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.ScriptJobResolver;
import org.craftercms.engine.scripting.impl.GroovyScriptFactory;
import org.craftercms.engine.service.PreviewOverlayCallback;
import org.craftercms.engine.util.SchedulingUtils;
import org.craftercms.engine.util.config.impl.MultiConfigurationBuilder;
import org.craftercms.engine.util.groovy.ContentStoreGroovyResourceLoader;
import org.craftercms.engine.util.groovy.ContentStoreResourceConnector;
import org.craftercms.engine.util.quartz.JobContext;
import org.craftercms.engine.util.spring.ApacheCommonsConfigPropertySource;
import org.craftercms.engine.util.spring.ContentStoreResourceLoader;
import org.quartz.Scheduler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

/**
 * Factory for creating {@link SiteContext} with common properties. It also uses the {@link MacroResolver} to resolve
 * any macros specified in the {@code rootFolderPath} before creating the context (remember that macros can vary
 * between requests).
 *
 * @author Alfonso Vásquez
 */
public class SiteContextFactory implements ApplicationContextAware {

    private static final Log logger = LogFactory.getLog(SiteContextFactory.class);

    protected String storeType;
    protected String storeServerUrl;
    protected String username;
    protected String password;
    protected String rootFolderPath;
    protected String staticAssetsPath;
    protected String templatesPath;
    protected String restScriptsPath;
    protected String controllerScriptsPath;
    protected String[] configPaths;
    protected String[] applicationContextPaths;
    protected String groovyClassesPath;
    protected Map<String, Object> groovyGlobalVars;
    protected boolean cacheOn;
    protected int maxAllowedItemsInCache;
    protected boolean ignoreHiddenFiles;
    protected ObjectFactory<FreeMarkerConfig> freeMarkerConfigFactory;
    protected UrlTransformationEngine urlTransformationEngine;
    protected PreviewOverlayCallback overlayCallback;
    protected ContentStoreService storeService;
    protected MacroResolver macroResolver;
    protected ApplicationContext mainApplicationContext;
    protected List<ScriptJobResolver> jobResolvers;

    public SiteContextFactory() {
        storeType = FileSystemContentStoreAdapter.STORE_TYPE;
        storeServerUrl = null;
        username = null;
        password = null;
        cacheOn = Context.DEFAULT_CACHE_ON;
        maxAllowedItemsInCache = Context.DEFAULT_MAX_ALLOWED_ITEMS_IN_CACHE;
        ignoreHiddenFiles = Context.DEFAULT_IGNORE_HIDDEN_FILES;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public void setStoreServerUrl(String storeServerUrl) {
        this.storeServerUrl = storeServerUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Required
    public void setRootFolderPath(String rootFolderPath) {
        this.rootFolderPath = rootFolderPath;
    }

    @Required
    public void setStaticAssetsPath(String staticAssetsPath) {
        this.staticAssetsPath = staticAssetsPath;
    }

    @Required
    public void setTemplatesPath(String templatesPath) {
        this.templatesPath = templatesPath;
    }

    @Required
    public void setRestScriptsPath(String restScriptsPath) {
        this.restScriptsPath = restScriptsPath;
    }

    @Required
    public void setControllerScriptsPath(final String controllerScriptsPath) {
        this.controllerScriptsPath = controllerScriptsPath;
    }

    @Required
    public void setConfigPaths(String[] configPaths) {
        this.configPaths = configPaths;
    }

    @Required
    public void setApplicationContextPaths(String[] applicationContextPaths) {
        this.applicationContextPaths = applicationContextPaths;
    }

    @Required
    public void setGroovyClassesPath(String groovyClassesPath) {
        this.groovyClassesPath = groovyClassesPath;
    }

    @Required
    public void setGroovyGlobalVars(Map<String, Object> groovyGlobalVars) {
        this.groovyGlobalVars = groovyGlobalVars;
    }

    public void setCacheOn(boolean cacheOn) {
        this.cacheOn = cacheOn;
    }

    public void setMaxAllowedItemsInCache(int maxAllowedItemsInCache) {
        this.maxAllowedItemsInCache = maxAllowedItemsInCache;
    }

    public void setIgnoreHiddenFiles(boolean ignoreHiddenFiles) {
        this.ignoreHiddenFiles = ignoreHiddenFiles;
    }

    @Required
    public void setFreeMarkerConfigFactory(ObjectFactory<FreeMarkerConfig> freeMarkerConfigFactory) {
        this.freeMarkerConfigFactory = freeMarkerConfigFactory;
    }

    @Required
    public void setUrlTransformationEngine(UrlTransformationEngine urlTransformationEngine) {
        this.urlTransformationEngine = urlTransformationEngine;
    }

    public void setOverlayCallback(PreviewOverlayCallback overlayCallback) {
        this.overlayCallback = overlayCallback;
    }

    @Required
    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    @Required
    public void setMacroResolver(MacroResolver macroResolver) {
        this.macroResolver = macroResolver;
    }

    @Required
    public void setJobResolvers(List<ScriptJobResolver> jobResolvers) {
        this.jobResolvers = jobResolvers;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.mainApplicationContext = applicationContext;
    }

    public SiteContext createContext(String siteName) {
        String resolvedRootFolderPath = macroResolver.resolveMacros(rootFolderPath);

        Context context = storeService.createContext(storeType, storeServerUrl, username, password,
                                                     resolvedRootFolderPath, cacheOn, maxAllowedItemsInCache,
                                                     ignoreHiddenFiles);
        try {
            SiteContext siteContext = new SiteContext();
            siteContext.setStoreService(storeService);
            siteContext.setSiteName(siteName);
            siteContext.setContext(context);
            siteContext.setStaticAssetsPath(staticAssetsPath);
            siteContext.setTemplatesPath(templatesPath);
            siteContext.setFreeMarkerConfig(freeMarkerConfigFactory.getObject());
            siteContext.setUrlTransformationEngine(urlTransformationEngine);
            siteContext.setOverlayCallback(overlayCallback);
            siteContext.setRestScriptsPath(restScriptsPath);
            siteContext.setControllerScriptsPath(controllerScriptsPath);

            String[] resolvedConfigPaths = new String[configPaths.length];
            for (int i = 0; i < configPaths.length; i++) {
                resolvedConfigPaths[i] = macroResolver.resolveMacros(configPaths[i]);
            }

            String[] resolvedAppContextPaths = new String[applicationContextPaths.length];
            for (int i = 0; i < applicationContextPaths.length; i++) {
                resolvedAppContextPaths[i] = macroResolver.resolveMacros(applicationContextPaths[i]);
            }

            ResourceLoader resourceLoader = new ContentStoreResourceLoader(siteContext);
            HierarchicalConfiguration config = getConfig(siteContext, resolvedConfigPaths, resourceLoader);
            URLClassLoader classLoader = getClassLoader(siteContext);
            ScriptFactory scriptFactory = getScriptFactory(siteContext, classLoader);
            ConfigurableApplicationContext appContext = getApplicationContext(siteContext, classLoader, config,
                                                                              resolvedAppContextPaths, resourceLoader);
        

            siteContext.setConfigPaths(resolvedConfigPaths);
            siteContext.setApplicationContextPaths(resolvedAppContextPaths);
            siteContext.setGroovyClassesPath(groovyClassesPath);
            siteContext.setScriptFactory(scriptFactory);
            siteContext.setConfig(config);
            siteContext.setApplicationContext(appContext);
            siteContext.setClassLoader(classLoader);

            Scheduler scheduler = scheduleJobs(siteContext);
            siteContext.setScheduler(scheduler);

            return siteContext;
        } catch (Exception e) {
            // Destroy context if the site context creation failed
            storeService.destroyContext(context);

            throw e;
        }
    }

    protected HierarchicalConfiguration getConfig(SiteContext siteContext, String[] configPaths,
                                                  ResourceLoader resourceLoader) {
        ConfigurationBuilder builder = new MultiConfigurationBuilder(configPaths, resourceLoader);

        try {
            return (HierarchicalConfiguration)builder.getConfiguration();
        } catch (ConfigurationException e) {
            throw new SiteContextCreationException("Unable to load configuration for site '" +
                                                   siteContext.getSiteName() + "'", e);
        }
    }

    protected URLClassLoader getClassLoader(SiteContext siteContext) {
        ContentStoreGroovyResourceLoader resourceLoader = new ContentStoreGroovyResourceLoader(siteContext,
                                                                                               groovyClassesPath);

        GroovyClassLoader classLoader = new GroovyClassLoader(getClass().getClassLoader());
        classLoader.setResourceLoader(resourceLoader);

        return classLoader;
    }

    protected ConfigurableApplicationContext getApplicationContext(SiteContext siteContext, URLClassLoader classLoader,
                                                                   HierarchicalConfiguration config,
                                                                   String[] applicationContextPaths,
                                                                   ResourceLoader resourceLoader) {
        try {
            List<Resource> resources = new ArrayList<>();

            for (String path : applicationContextPaths) {
                Resource resource = resourceLoader.getResource(path);
                if (resource.exists()) {
                    resources.add(resource);
                }
            }

            if (CollectionUtils.isNotEmpty(resources)) {
                GenericApplicationContext appContext = new GenericApplicationContext(mainApplicationContext);
                appContext.setClassLoader(classLoader);

                if (config != null) {
                    MutablePropertySources propertySources = appContext.getEnvironment().getPropertySources();
                    propertySources.addFirst(new ApacheCommonsConfigPropertySource("siteConfig", config));
                }

                XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
                reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);

                for (Resource resource : resources) {
                    reader.loadBeanDefinitions(resource);
                }

                appContext.refresh();

                logger.info("Application context loaded for site '" + siteContext.getSiteName() + "'");

                return appContext;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new SiteContextCreationException("Unable to load application context for site '" +
                                                   siteContext.getSiteName() + "'", e);
        }
    }

    protected ScriptFactory getScriptFactory(SiteContext siteContext, URLClassLoader classLoader) {
        return new GroovyScriptFactory(new ContentStoreResourceConnector(siteContext), classLoader, groovyGlobalVars);
    }

    protected Scheduler scheduleJobs(SiteContext siteContext) {
        String siteName = siteContext.getSiteName();

        try {
            List<JobContext> allJobContexts = new ArrayList<>();

            for (ScriptJobResolver jobResolver : jobResolvers) {
                List<JobContext> jobContexts = jobResolver.resolveJobs(siteContext);
                if (CollectionUtils.isNotEmpty(jobContexts)) {
                    allJobContexts.addAll(jobContexts);
                }
            }

            if (CollectionUtils.isNotEmpty(allJobContexts)) {
                Scheduler scheduler = SchedulingUtils.createScheduler(siteName + "_scheduler");

                for (JobContext jobContext : allJobContexts) {
                    scheduler.scheduleJob(jobContext.getDetail(), jobContext.getTrigger());

                    logger.info("Scheduled job: " + jobContext + " for site '" + siteName + "'");
                }

                scheduler.start();

                return scheduler;
            }
        } catch (Exception e) {
            throw new SiteContextCreationException("Unable to schedule jobs for site '" + siteName + "'", e);
        }

        return null;
    }
}
