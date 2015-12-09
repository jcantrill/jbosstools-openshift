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

package org.jboss.tools.openshift.internal.ui.property.tabbed;

import java.text.ParseException;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.databinding.viewers.ObservableSetContentProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.jboss.tools.openshift.core.OpenShiftAPIAnnotations;
import org.jboss.tools.openshift.internal.common.ui.utils.DateTimeUtils;
import org.jboss.tools.openshift.internal.common.ui.utils.TableViewerBuilder;
import org.jboss.tools.openshift.internal.common.ui.utils.TableViewerBuilder.IColumnLabelProvider;
import org.jboss.tools.openshift.internal.common.ui.utils.UIUtils;
import org.jboss.tools.openshift.internal.ui.explorer.Deployment;

import com.openshift.restclient.model.IBuild;

public class BuildsPropertySection extends AbstractPropertySection implements OpenShiftAPIAnnotations {

	private static final String CONTEXT_MENU_ID = "org.jboss.tools.openshift.ui.properties.tab.BuildsTab";
	private TableViewer table;
	private DataBindingContext dbc;
	private PropertySheetPage buildDetails;

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		dbc = new DataBindingContext(); 
		
		SashForm container = new SashForm(parent, SWT.VERTICAL);
		Composite tableContainer = new Composite(container, SWT.NONE);
		
		tableContainer.setLayout(new FillLayout());
		this.table = createTable(tableContainer);
		table.setContentProvider(new ObservableSetContentProvider());

		buildDetails = new PropertySheetPage();
		buildDetails.createControl(container);
		
	}

	protected TableViewer createTable(Composite tableContainer) {
		Table table = new Table(tableContainer, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		TableViewer viewer = new TableViewerBuilder(table, tableContainer).contentProvider(new ArrayContentProvider())
				.column(new IColumnLabelProvider<IBuild>() {
					@Override
					public String getValue(IBuild build) {
						return build.getName();
					}
				}).name("Name").align(SWT.LEFT).weight(1).minWidth(10).buildColumn()
				.column(new IColumnLabelProvider<IBuild>() {
					@Override
					public String getValue(IBuild build) {
						return build.getAnnotation(BUILD_NUMBER);
					}
				}).name("Build").align(SWT.LEFT).weight(1).minWidth(5).buildColumn()
				.column(new IColumnLabelProvider<IBuild>() {
					@Override
					public String getValue(IBuild build) {
						return build.getStatus();
					}
				}).name("Status").align(SWT.LEFT).weight(1).minWidth(25).buildColumn()
				.column(new IColumnLabelProvider<IBuild>() {
					@Override
					public String getValue(IBuild build) {
						return build.getCreationTimeStamp();
					}
				}).name("Started").align(SWT.LEFT).weight(1).buildColumn()
				.buildViewer();
		viewer.setComparator(new ViewerComparator() {

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				IBuild build1 = (IBuild)e1;
				IBuild build2 = (IBuild)e2;
				try {
					return -1 * DateTimeUtils.parse(build1.getCreationTimeStamp())
							.compareTo(DateTimeUtils.parse(build2.getCreationTimeStamp()));
				} catch (ParseException e) {
				}
				return 0;
			}
			
			
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				buildDetails.selectionChanged(null, event.getSelection());
			}
		}); 
		addContextMenu(table);
		return viewer;
	}
	
	private void addContextMenu(Table table) {
		IMenuManager contextMenu = UIUtils.createContextMenu(table);
		UIUtils.registerContributionManager(CONTEXT_MENU_ID, contextMenu, table);
    }
	
	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		Deployment deployment = UIUtils.getFirstElement(selection, Deployment.class);
		if(deployment == null) return;
		table.setInput(BeanProperties.set("builds").observe(deployment));

	}

	@Override
	public void dispose() {
		this.dbc.dispose();
		this.buildDetails.dispose();
	}

	@Override
	public boolean shouldUseExtraSpace() {
		return true;
	}

	@Override
	public void refresh() {
		this.table.refresh();
		this.buildDetails.refresh();
	}

	
	
}
