/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.internal.ui.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.jboss.tools.common.ui.databinding.ObservableUIPojo;
import org.jboss.tools.openshift.common.core.utils.StringUtils;

import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IBuild;
import com.openshift.restclient.model.IPod;
import com.openshift.restclient.model.IResource;

public abstract class ResourcesUIModel extends ObservableUIPojo implements IResourcesUIModel{

	private Map<String, List<IResourceUIModel>> resources = new ConcurrentHashMap<>();
	
	protected ResourcesUIModel() {
		Arrays.asList(KINDS).forEach(k->resources.put(k, Collections.synchronizedList(new ArrayList<>())));
	}
	
	private <T extends IResource> List<IResourceUIModel> init(Collection<T> resources) {
		if(resources != null) {
			return resources.stream().map(r->new OpenShiftResourceUIModel(r)).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}
	
	@Override
	public Collection<IResourceUIModel> getImageStreams() {
		return resources.get(ResourceKind.IMAGE_STREAM);	
	}
	
	@Override
	public void setImageStreams(Collection<IResourceUIModel> models) {
		firePropertyChange(PROP_IMAGE_STREAMS, resources.get(ResourceKind.IMAGE_STREAM), resources.put(ResourceKind.IMAGE_STREAM, new ArrayList<>(models)));
	}
	
	@Override
	public void setImageStreamResources(Collection<IResource> streams) {
		firePropertyChange(PROP_IMAGE_STREAMS, resources.get(ResourceKind.IMAGE_STREAM), resources.put(ResourceKind.IMAGE_STREAM, init(streams)));
	}

	@Override
	public Collection<IResourceUIModel> getDeploymentConfigs() {
		return resources.get(ResourceKind.DEPLOYMENT_CONFIG);	
	}
	
	@Override
	public void setDeploymentConfigs(Collection<IResourceUIModel> models) {
		firePropertyChange(PROP_DEPLOYMENT_CONFIGS, resources.get(ResourceKind.DEPLOYMENT_CONFIG), resources.put(ResourceKind.DEPLOYMENT_CONFIG, new ArrayList<>(models)));
	}
	
	@Override
	public void setDeploymentConfigResources(Collection<IResource> dcs) {
		firePropertyChange(PROP_DEPLOYMENT_CONFIGS, resources.get(ResourceKind.DEPLOYMENT_CONFIG), resources.put(ResourceKind.DEPLOYMENT_CONFIG, init(dcs)));
	}
	
	@Override
	public Collection<IResourceUIModel> getBuilds() {
		return resources.get(ResourceKind.BUILD);	
	}
	
	@Override
	public void setBuilds(Collection<IResourceUIModel> builds) {
		firePropertyChange(PROP_BUILDS, resources.get(ResourceKind.BUILD), resources.put(ResourceKind.BUILD, new ArrayList<>(builds)));
	}
	
	public void setBuildResources(Collection<IBuild> builds) {
		firePropertyChange(PROP_BUILDS, resources.get(ResourceKind.BUILD), resources.put(ResourceKind.BUILD, init(builds)));
	}
	
	@Override
	public Collection<IResourceUIModel> getPods() {
		return resources.get(ResourceKind.POD);
	}
	
	@Override
	public void setPods(Collection<IResourceUIModel> pods) {
		firePropertyChange(PROP_PODS, resources.get(ResourceKind.POD), resources.put(ResourceKind.POD, new ArrayList<>(pods)));
	}

	@Override
	public void setPodResources(Collection<IPod> pods) {
		firePropertyChange(PROP_PODS, resources.get(ResourceKind.POD), resources.put(ResourceKind.POD, init(pods)));
	}
	
	@Override
	public Collection<IResourceUIModel> getRoutes() {
		return resources.get(ResourceKind.ROUTE);
	}

	@Override
	public void setRoutes(Collection<IResourceUIModel> routes) {
		firePropertyChange(PROP_ROUTES, resources.get(ResourceKind.ROUTE), resources.put(ResourceKind.ROUTE, new ArrayList<>(routes)));
	}
	
	@Override
	public void setRouteResources(Collection<IResource> routes) {
		firePropertyChange(PROP_ROUTES, resources.get(ResourceKind.ROUTE), resources.put(ResourceKind.ROUTE, init(routes)));
	}

	@Override
	public Collection<IResourceUIModel> getReplicationControllers() {
		return resources.get(ResourceKind.REPLICATION_CONTROLLER);
	}
	
	@Override
	public void setReplicationControllers(Collection<IResourceUIModel> rcs) {
		firePropertyChange(PROP_REPLICATION_CONTROLLERS, resources.get(ResourceKind.REPLICATION_CONTROLLER), resources.put(ResourceKind.REPLICATION_CONTROLLER, new ArrayList<>(rcs)));
	}

	@Override
	public void setReplicationControllerResources(Collection<IResource> rcs) {
		firePropertyChange(PROP_REPLICATION_CONTROLLERS, resources.get(ResourceKind.REPLICATION_CONTROLLER), resources.put(ResourceKind.REPLICATION_CONTROLLER, init(rcs)));
	}

	@Override
	public Collection<IResourceUIModel> getBuildConfigs(){
		return resources.get(ResourceKind.BUILD_CONFIG);
	}
	
	@Override
	public void setBuildConfigs(Collection<IResourceUIModel> buildConfigs) {
		firePropertyChange(PROP_BUILD_CONFIGS, resources.get(ResourceKind.BUILD_CONFIG), resources.put(ResourceKind.BUILD_CONFIG, new ArrayList<>(buildConfigs)));
	}
	
	@Override
	public void setBuildConfigResources(Collection<IResource> buildConfigs) {
		firePropertyChange(PROP_BUILD_CONFIGS, resources.get(ResourceKind.BUILD_CONFIG), resources.put(ResourceKind.BUILD_CONFIG, init(buildConfigs)));
	}
	
	@Override
	public Collection<IResourceUIModel> getServices() {
		return resources.get(ResourceKind.SERVICE);
	}

	@Override
	public void setServices(Collection<IResourceUIModel> services) {
		firePropertyChange(PROP_SERVICES, resources.get(ResourceKind.SERVICE), resources.put(ResourceKind.SERVICE, new ArrayList<>(services)));
	}

	@Override
	public void setServiceResources(Collection<IResource> services) {
		firePropertyChange(PROP_SERVICES, resources.get(ResourceKind.SERVICE), resources.put(ResourceKind.SERVICE, init(services)));
	}

	@Override
	public void add(IResource resource) {
		final String property = getProperty(resource.getKind());
		List<IResourceUIModel> models = resources.get(resource.getKind());
		if(models != null) {
			models.add(new OpenShiftResourceUIModel(resource));
			int index = models.size();
			fireIndexedPropertyChange(property, index, null, Collections.unmodifiableList(models));
		}
	}

	@Override
	public void remove(IResource resource) {
		final String property = getProperty(resource.getKind());
		List<IResourceUIModel> models = resources.get(resource.getKind());
		if(models != null) {
			int index = indexOf(models, resource);
			if(index > -1) {
				models.remove(index);
				fireIndexedPropertyChange(property, index, Collections.unmodifiableList(models), null);
			}
		}
	}

	@Override
	public void update(IResource resource) {
		final String property = getProperty(resource.getKind());
		List<IResourceUIModel> models = resources.get(resource.getKind());
		if(models != null) {
			int index = indexOf(models, resource);
			if(index > -1) {
				List<IResourceUIModel> old = new ArrayList<>(models);
				models.set(index, new OpenShiftResourceUIModel(resource));
				fireIndexedPropertyChange(property, index, old, Collections.unmodifiableList(models));
			}
		}
	}
	
	private int indexOf(List<IResourceUIModel> models, IResource resource) {
		for (int i = 0; i < models.size(); i++) {
			IResourceUIModel model = models.get(i);
			if(model != null) {
				IResource old = model.getResource();
				if(old.equals(resource)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	private static String getProperty(String kind) {
		return StringUtils.pluralize(kind.toLowerCase());
	}

}
