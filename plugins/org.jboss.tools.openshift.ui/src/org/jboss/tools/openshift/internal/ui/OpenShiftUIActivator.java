/*******************************************************************************
 * Copyright (c) 2014-2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.internal.ui;

import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.jboss.tools.foundation.core.plugin.log.IPluginLog;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;
import org.jboss.tools.foundation.ui.plugin.BaseUIPlugin;
import org.jboss.tools.openshift.core.OpenShiftCoreUIIntegration;
import org.jboss.tools.openshift.internal.common.ui.connection.CredentialsPrompter;
import org.jboss.tools.openshift.internal.core.OpenShiftCoreActivator;
import org.jboss.tools.openshift.internal.ui.wizard.connection.SSLCertificateCallback;
import org.osgi.framework.BundleContext;

import com.openshift.restclient.OpenShiftException;

public class OpenShiftUIActivator extends BaseUIPlugin{

	public static final String PLUGIN_ID = "org.jboss.tools.openshift.ui"; //$NON-NLS-1$

	private static OpenShiftUIActivator plugin;

	private IPreferenceStore corePreferenceStore;
	
	public OpenShiftUIActivator() {
	}
	
	public IPluginLog getLogger(){
		return pluginLogInternal();
	}
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		OpenShiftCoreUIIntegration.getInstance().setSSLCertificateAuthorization(new SSLCertificateCallback());
		OpenShiftCoreUIIntegration.getInstance().setCredentialPrompter(new CredentialsPrompter());
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static OpenShiftUIActivator getDefault() {
		return plugin;
	}
	
	public static StatusFactory statusFactory() {
		return getDefault().statusFactoryInternal();
	}
	
	/**
	 * Get an inputstream for a file
	 * @param file
	 * @return
	 * @throws OpenShiftException if unable to read the file;
	 */
	public InputStream getPluginFile(String file) {
		URL url;
		try {
			url = new URL(plugin.getBundle().getEntry("/"), file);
			return url.openStream();
		} catch (Exception e) {
			getLogger().logError(e);
			throw new OpenShiftException(e,"Exception trying to load plugin file: {0}", file) ;
		}
	}
	
	/**
	 * Retrieve the preferencestore
	 * @return 
	 */
    public IPreferenceStore getCorePreferenceStore() {
        // Create the preference store lazily.
        if (corePreferenceStore == null) {
        	this.corePreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, OpenShiftCoreActivator.PLUGIN_ID);

        }
        return corePreferenceStore;
    }
}
