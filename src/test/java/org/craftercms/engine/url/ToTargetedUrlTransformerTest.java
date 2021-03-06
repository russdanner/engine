package org.craftercms.engine.url;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.targeting.TargetedUrlStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.craftercms.engine.util.config.TargetingProperties.*;
import static org.mockito.Mockito.*;

/**
 * Created by alfonsovasquez on 21/8/15.
 */
public class ToTargetedUrlTransformerTest {

    private static final String[] ROOT_FOLDERS = { "/site/website" };
    private static final String NON_TARGETED_URL = "/products/index.xml";
    private static final String TARGETED_URL = "/products/index_es_CR.xml";
    private static final String NON_TARGETED_FULL_URL = ROOT_FOLDERS[0] + NON_TARGETED_URL;
    private static final String TARGETED_FULL_URL = ROOT_FOLDERS[0] + TARGETED_URL;

    private ToTargetedUrlTransformer urlTransformer;

    @Before
    public void setUp() throws Exception {
        urlTransformer = new ToTargetedUrlTransformer();
        urlTransformer.setTargetedUrlStrategy(createTargetedUrlStrategy());

        setUpCurrentSiteContext();
    }

    @After
    public void tearDown() throws Exception {
        clearCurrentSiteContext();
    }

    @Test
    public void testTransformUrl() throws Exception {
        String url = urlTransformer.transformUrl(null, null, NON_TARGETED_FULL_URL);

        assertNotNull(url);
        assertEquals(TARGETED_FULL_URL, url);
    }

    private TargetedUrlStrategy createTargetedUrlStrategy() {
        TargetedUrlStrategy urlStrategy = mock(TargetedUrlStrategy.class);

        when(urlStrategy.toTargetedUrl(NON_TARGETED_URL)).thenReturn(TARGETED_URL);

        return urlStrategy;
    }

    private void setUpCurrentSiteContext() {
        HierarchicalConfiguration config = mock(HierarchicalConfiguration.class);
        when(config.getBoolean(TARGETING_ENABLED_CONFIG_KEY, false)).thenReturn(true);
        when(config.getStringArray(ROOT_FOLDERS_CONFIG_KEY)).thenReturn(ROOT_FOLDERS);

        SiteContext siteContext = new SiteContext();
        siteContext.setConfig(config);

        SiteContext.setCurrent(siteContext);
    }

    private void clearCurrentSiteContext() {
        SiteContext.clear();
    }

}
