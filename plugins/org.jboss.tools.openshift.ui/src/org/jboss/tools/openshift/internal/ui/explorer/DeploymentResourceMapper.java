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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.tools.openshift.core.connection.Connection;
import org.jboss.tools.openshift.core.connection.ConnectionsRegistryUtil;

import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IPod;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IService;
import com.openshift.restclient.model.route.IRoute;

public class DeploymentResourceMapper {
	
	private IProject project;
	private Connection conn;

	public DeploymentResourceMapper(Connection conn, IProject project) {
		this.project = project;
		this.conn = conn;
	}
	
	public List<Deployment> getDeployments(){
		List<IService> services = conn.getResources(ResourceKind.SERVICE, project.getName());
		Collection<IRoute> routes = conn.getResources(ResourceKind.ROUTE, project.getName());
		
		List<Deployment> adapters = new ArrayList<>(services.size());
		for (IService service : services) {
			adapters.add(getDeployment(service, routes));
		}
		
		return adapters;
	}
	
	public static Deployment getDeployment(IService service) {
		List<IRoute> routes = ConnectionsRegistryUtil.getConnectionFor(service).getResources(ResourceKind.ROUTE, service.getNamespace());
		return getDeployment(service, routes);
	}
	
	private static Deployment getDeployment(IService service, Collection<IRoute> routes) {
		List<IPod> pods = service.getPods();
		List<IRoute> appRoutes = routes.stream()
				.filter(r->r.getServiceName().equals(service.getName())).collect(Collectors.toList());
		return new Deployment(service, appRoutes, pods);
	}
}
