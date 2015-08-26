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
package org.jboss.tools.openshift.internal.ui.wizard.deployimage;

import java.util.List;
import java.util.Set;

import org.jboss.tools.openshift.internal.ui.wizard.common.IResourceLabelsPageModel;

/**
 * Page model for the deployment config page
 * @author jeff.cantrill
 *
 */
public interface IDeploymentConfigPageModel {
	String PROPERTY_ENVIRONMENT_VARIABLES = "environmentVariables";
	String PROPERTY_SELECTED_ENVIRONMENT_VARIABLE = "selectedEnvironmentVariable";

	String PROPERTY_VOLUMES = "volumes";
	String PROPERTY_SELECTED_VOLUME = "selectedVolume";

	String PROPERTY_PORT_SPECS = "portSpecs";

	String PROPERTY_REPLICAS = "replicas";


	List<IResourceLabelsPageModel.Label> getEnvironmentVariables();
	
	void setEnvironmentVariables(List<IResourceLabelsPageModel.Label> envVars);
	
	void setVolumes(Set<String> volumes);
	
	Set<String> getVolumes();
	
	void setPortSpecs(Set<String> portSpecs);
	
	Set<String> getPortSpecs();
	
	/**
	 * The number of replicas to define in the deployment config.
	 * This is scalability factor;
	 * @return
	 */
	int getReplicas();
	
	/**
	 * The number of replicas to define in the deployment config.
	 * This is scalability factor;

	 * @param replicas  a number of 1 or more replicas
	 */
	void setReplicas(int replicas);

	void setSelectedEnvironmentVariable(IResourceLabelsPageModel.Label envVar);

	IResourceLabelsPageModel.Label getSelectedEnvironmentVariable();

	void removeEnvironmentVariable(IResourceLabelsPageModel.Label envVar);
	
	void updateEnvironmentVariable(IResourceLabelsPageModel.Label envVar, String key, String value);

	void addEnvironmentVariable(String key, String value);

	void setSelectedVolume(String volume);

	String getSelectedVolume();

	void updateVolume(String volume, String value);


}
