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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.common.ui.databinding.ValueBindingBuilder;
import org.jboss.tools.openshift.core.connection.Connection;
import org.jboss.tools.openshift.internal.common.ui.databinding.RequiredControlDecorationUpdater;
import org.jboss.tools.openshift.internal.common.ui.databinding.RequiredStringValidator;
import org.jboss.tools.openshift.internal.common.ui.wizard.AbstractOpenShiftWizardPage;
import org.jboss.tools.openshift.internal.ui.treeitem.ObservableTreeItem2ModelConverter;
import org.jboss.tools.openshift.internal.ui.treeitem.ObservableTreeItemLabelProvider;

import com.openshift.restclient.model.IProject;

/**
 * Page to (mostly) edit the config items for a page
 * 
 * @author jeff.cantrill
 */
public class DeployImagePage extends AbstractOpenShiftWizardPage {

	private static final String PAGE_DESCRIPTION = "";

	private IDeployImagePageModel model;

	protected DeployImagePage(IWizard wizard, IDeployImagePageModel model) {
		super("Deploy an Image", PAGE_DESCRIPTION, "Deployment Config Settings Page", wizard);
		this.model = model;
	}

	@Override
	protected void doCreateControls(Composite parent, DataBindingContext dbc) {
		GridLayoutFactory.fillDefaults()
			.numColumns(2)
			.margins(10, 10)
			.applyTo(parent);

		createConnectionControl(parent, dbc);
		createProjectControl(parent, dbc);
		
		//Image
		Label lblImage = new Label(parent, SWT.NONE);
		lblImage.setText("Image: ");
		GridDataFactory.fillDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.applyTo(lblImage);
		Text txtImage = new Text(parent, SWT.BORDER);
		GridDataFactory.fillDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false)
			.applyTo(txtImage);
		IObservableValue imageTextObservable = 
				WidgetProperties.text(SWT.Modify).observe(txtImage);
		Binding imageBinding = ValueBindingBuilder
			.bind(imageTextObservable)
			.validatingAfterConvert(new RequiredStringValidator("Image"))
			.to(BeanProperties.value(IDeployImagePageModel.PROPERTY_IMAGE).observe(model))
			.in(dbc);
		ControlDecorationSupport.create(
				imageBinding, SWT.LEFT | SWT.TOP, null, new RequiredControlDecorationUpdater(true));

		//Resource Name
		Label lblName = new Label(parent, SWT.NONE);
		lblName.setText("Resource Name: ");
		GridDataFactory.fillDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.applyTo(lblName);
		Text txtName = new Text(parent, SWT.BORDER);
		GridDataFactory.fillDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false)
			.applyTo(txtName);
		IObservableValue nameTextObservable = 
				WidgetProperties.text(SWT.Modify).observe(txtName);
		Binding nameBinding = ValueBindingBuilder
				.bind(nameTextObservable)
				.validatingAfterConvert(new RequiredStringValidator("Resource Name"))
				.to(BeanProperties.value(IDeployImagePageModel.PROPERTY_NAME).observe(model))
				.in(dbc);
		ControlDecorationSupport.create(
				nameBinding, SWT.LEFT | SWT.TOP, null, new RequiredControlDecorationUpdater(true));
		
	}
	
	private void createConnectionControl(Composite parent, DataBindingContext dbc) {
		Label lblConnection = new Label(parent, SWT.NONE);
		lblConnection.setText("Connection: ");
		GridDataFactory.fillDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.applyTo(lblConnection);
		
		StructuredViewer connectionViewer = new ComboViewer(parent);
		GridDataFactory.fillDefaults()
			.align(SWT.FILL, SWT.CENTER).grab(true, false)
			.applyTo(connectionViewer.getControl());
		
		connectionViewer.setContentProvider(new ObservableListContentProvider());
		connectionViewer.setLabelProvider(new ObservableTreeItemLabelProvider());
		connectionViewer.setInput(
				BeanProperties.list(IDeployImagePageModel.PROPERTY_CONNECTIONS).observe(model));
		
		IObservableValue selectedConnectionObservable = ViewerProperties.singleSelection().observe(connectionViewer);
		Binding selectedConnectionBinding = 
			ValueBindingBuilder.bind(selectedConnectionObservable)
			.converting(new ObservableTreeItem2ModelConverter(Connection.class))
			.validatingAfterConvert(new IValidator() {
				
				@Override
				public IStatus validate(Object value) {
					if (value instanceof Connection) {
						return ValidationStatus.ok();
					}
					return ValidationStatus.cancel("Please choose an OpenShift connection.");
				}
			})
			.to(BeanProperties.value(IDeployImagePageModel.PROPERTY_CONNECTION)
			.observe(model))
			.in(dbc);
		ControlDecorationSupport.create(
			selectedConnectionBinding, SWT.LEFT | SWT.TOP, null, new RequiredControlDecorationUpdater(true));
		
	}

	private void createProjectControl(Composite parent, DataBindingContext dbc) {
		Label lblProject = new Label(parent, SWT.NONE);
		lblProject.setText("OpenShift Project: ");
		GridDataFactory.fillDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.applyTo(lblProject);
		
		StructuredViewer cmboProject = new ComboViewer(parent);
		GridDataFactory.fillDefaults()
			.align(SWT.FILL, SWT.CENTER).grab(true, false)
			.applyTo(cmboProject.getControl());
		
		cmboProject.setContentProvider(new ObservableListContentProvider());
		cmboProject.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if(element instanceof IProject) {
					return ((IProject)element).getName();
				}
				return super.getText(element);
			}
			
		});
		cmboProject.setInput(
				BeanProperties.list(IDeployImagePageModel.PROPERTY_PROJECTS).observe(model));
		
		IObservableValue selectedProjectObservable = ViewerProperties.singleSelection().observe(cmboProject);
		Binding selectedProjectBinding = 
			ValueBindingBuilder.bind(selectedProjectObservable)
			.converting(new ObservableTreeItem2ModelConverter(IProject.class))
			.validatingAfterConvert(new IValidator() {
				
				@Override
				public IStatus validate(Object value) {
					if (value instanceof IProject) {
						return ValidationStatus.ok();
					}
					return ValidationStatus.cancel("Please choose an OpenShift project.");
				}
			})
			.to(BeanProperties.value(IDeployImagePageModel.PROPERTY_PROJECT)
			.observe(model))
			.in(dbc);
		ControlDecorationSupport.create(
				selectedProjectBinding, SWT.LEFT | SWT.TOP, null, new RequiredControlDecorationUpdater(true));
	}

}
