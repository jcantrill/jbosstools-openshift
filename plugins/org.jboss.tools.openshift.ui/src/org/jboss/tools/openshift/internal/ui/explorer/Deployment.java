/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.internal.ui.explorer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.tools.common.ui.databinding.ObservableUIPojo;

import com.openshift.restclient.model.IPod;

import com.openshift.restclient.model.IBuild;
import com.openshift.restclient.model.IReplicationController;
import com.openshift.restclient.model.IService;
import com.openshift.restclient.model.route.IRoute;

/**
 * A deployment is the collection of resources
 * that makes up an 'application'
 * 
 * @author jeff.cantrill
 *
 */
public class Deployment extends ObservableUIPojo {

	private IService service;
	private Set<IRoute> routes;
	private Set<IPod> pods;
	private Set<IBuild> builds;
	private Set<IReplicationController> rcs;
	
	public Deployment(IService service, Collection<IRoute> routes, Collection<IBuild> builds, Collection<IPod> pods, Collection<IReplicationController> rcs) {
		this.service = service;
		this.builds = builds == null ? new HashSet<>() :new HashSet<>(builds);
		this.pods = new HashSet<>(pods);
		this.routes = new HashSet<>(routes);
//		this.rcs = new HashSet<>(rcs);
	}
	public Collection<IBuild> getBuilds() {
		return Collections.unmodifiableSet(builds);
	}
	
	public Collection<IPod> getPods() {
		return Collections.unmodifiableSet(pods);
	}
	public Collection<IRoute> getRoutes() {
		return Collections.unmodifiableSet(this.routes);
	}
	public IService getService() {
		return this.service;
	}

	public Collection<IReplicationController> getReplicationController() {
		return this.rcs;
	}

	public void add(IPod pod) {
		this.pods.add(pod);
	}

	public void remove(IPod pod) {
		this.pods.remove(pod);
	}
	
}
