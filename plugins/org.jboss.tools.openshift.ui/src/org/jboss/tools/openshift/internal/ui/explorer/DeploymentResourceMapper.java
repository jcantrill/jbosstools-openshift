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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jboss.tools.openshift.core.OpenShiftAPIAnnotations;
import org.jboss.tools.openshift.core.connection.Connection;

import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.images.DockerImageURI;
import com.openshift.restclient.model.IBuild;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IPod;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IReplicationController;
import com.openshift.restclient.model.IResource;
import com.openshift.restclient.model.IService;
import com.openshift.restclient.model.deploy.DeploymentTriggerType;
import com.openshift.restclient.model.deploy.IDeploymentImageChangeTrigger;
import com.openshift.restclient.model.deploy.IDeploymentTrigger;
import com.openshift.restclient.model.route.IRoute;

/**
 * Figures out the resources in a project associated
 * with a deployment
 * 
 * @author jeff.cantrill
 *
 */
public class DeploymentResourceMapper {
	
	private IProject project;
	private Connection conn;

	public DeploymentResourceMapper(Connection conn, IProject project) {
		this.project = project;
		this.conn = conn;
	}

	public Collection<Deployment> getDeployments(){
		List<IReplicationController> rcs = conn.getResources(ResourceKind.REPLICATION_CONTROLLER, project.getName());
		List<IService> services = conn.getResources(ResourceKind.SERVICE, project.getName());
		Collection<IRoute> routes = conn.getResources(ResourceKind.ROUTE, project.getName());
		List<IBuild> builds = conn.getResources(ResourceKind.BUILD, project.getName());
		Map<String, IDeploymentConfig> deployConfigs = 
				conn.<IDeploymentConfig>getResources(ResourceKind.DEPLOYMENT_CONFIG, project.getName())
				.stream().collect(Collectors.toMap(IResource::getName, resource->resource));
		
		Collection<Deployment> deployments = new ArrayList<>(services.size());
		
		Map<IService, List<IReplicationController>> rcsByService = mapServicesToRepControllers(services, rcs);
		//serice by name->deployment  join selector
		//service by name ->dc->deployment
		for (IService service : services) {
			List<IPod> pods = service.getPods();
			List<IBuild> appBuilds = getBuildsForDeployment(pods, builds, deployConfigs);
			deployments.add(getDeployment(service, routes, appBuilds));
		}
		
		return deployments;
	}
	
	public static Deployment getDeployment(IService service) {
		List<IRoute> routes = ConnectionsRegistryUtil.getConnectionFor(service).getResources(ResourceKind.ROUTE, service.getNamespace());
		return getDeployment(service, routes, null);
	}
	
	private static Deployment getDeployment(IService service, Collection<IRoute> routes, Collection<IBuild> builds) {
		List<IPod> pods = service.getPods();
		List<IRoute> appRoutes = routes.stream()
				.filter(r->r.getServiceName().equals(service.getName())).collect(Collectors.toList());
		return new Deployment(service, appRoutes, builds, pods, null);
    }
	private Map<IService, List<IReplicationController>> mapServicesToRepControllers(Collection<IService> services, Collection<IReplicationController> rcs){
		Map<IService, List<IReplicationController>> map = new HashMap<>(services.size());
		for (IReplicationController rc : rcs) {
			Map<String, String> deploymentSelector = rc.getReplicaSelector();
			for (IService service : services) {
				Map<String, String> serviceSelector = service.getSelector();
				if(selectorsJoin(serviceSelector, deploymentSelector)) {
					if(!map.containsKey(service)) {
						map.put(service, new ArrayList<IReplicationController>());
					}
					map.get(service).add(rc);
				}
			}		
		}
		return map;
	}
	
	/**
	 * 
	 * @param source
	 * @param target
	 * @return true if target includes all source keys and values; false otherwise
	 */
	private boolean selectorsJoin(Map<String, String> source, Map<String, String> target) {
		if(!target.keySet().containsAll(source.keySet())) {
			return false;
		}
		for (String key : source.keySet()) {
			if(!target.get(key).equals(source.get(key))) {
				return false;
			}
		}
		return true;
	}
	
	private List<IBuild> getBuildsForDeployment(List<IPod> pods, List<IBuild> builds, Map<String, IDeploymentConfig> deployConfigs) {
		List<IBuild> buildsForDeployment = new ArrayList<IBuild>();
		for (IPod pod : pods) {
			String dcName = pod.getAnnotation(OpenShiftAPIAnnotations.DEPLOYMENT_CONFIG_NAME);
			if(StringUtils.isNotEmpty(dcName) && deployConfigs.containsKey(dcName)) {
				IDeploymentConfig dc = deployConfigs.get(dcName);
				List<IDeploymentTrigger> imageChangeTriggers = filterImageChangeTriggers(dc);
				for (IDeploymentTrigger trigger : imageChangeTriggers) {
					String triggerImageRef = imageRef((IDeploymentImageChangeTrigger) trigger, this.project);
					for (IBuild build : builds) {
						String buildImageRef = imageRef(build, this.project);
						if(triggerImageRef.equals(buildImageRef)) {
							buildsForDeployment.add(build);
						}
					}
					
				}
			}
		}
		return buildsForDeployment;
	}
	private String imageRef(IBuild build, IProject project) {
		final String kind = build.getOutputKind();
		if("ImageStreamTag".equals(kind) || "ImageStreamImage".equals(kind)) {
			return new DockerImageURI("", project.getName(),build.getOutputTo().getNameAndTag()).toString();
		}
		if("DockerImage".equals(kind)) {
			return build.getOutputTo().getNameAndTag().toString();
		}
		return "";
		
	}
	
	private String imageRef(IDeploymentImageChangeTrigger trigger, IProject project) {
		final String kind = trigger.getKind();
		if("ImageStreamTag".equals(kind) || "ImageStreamImage".equals(kind)) {
			return new DockerImageURI("", project.getName(),trigger.getFrom().getNameAndTag()).toString();
		}
		if("DockerImage".equals(kind)) {
			return trigger.getFrom().getNameAndTag().toString();
		}
		return "";
	}

	private List<IDeploymentTrigger>  filterImageChangeTriggers(IDeploymentConfig dc){
		return dc.getTriggers()
				.stream().
				filter(t->t.getType().equals(DeploymentTriggerType.IMAGE_CHANGE)).collect(Collectors.toList());
	}
}
