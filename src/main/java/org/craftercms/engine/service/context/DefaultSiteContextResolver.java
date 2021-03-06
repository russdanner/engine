package org.craftercms.engine.service.context;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Required;

/**
 * {@link org.craftercms.engine.service.context.SiteContextResolver} that resolves always the current site name
 * to a default site name.
 *
 * @author avasquez
 */
public class DefaultSiteContextResolver extends AbstractSiteContextResolver {

    private String defaultSiteName;

    @Required
    public void setDefaultSiteName(String defaultSiteName) {
        this.defaultSiteName = defaultSiteName;
    }

    @Override
    protected String getSiteName(HttpServletRequest request) {
        return defaultSiteName;
    }

}
