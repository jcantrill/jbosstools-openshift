/*******************************************************************************
 * coright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.internal.ui.wizard.connection;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.widgets.Composite;
import org.jboss.tools.openshift.internal.common.ui.connection.IConnectionAdvancedPropertiesEditor;
import org.jboss.tools.openshift.internal.common.ui.detailviews.BaseDetailsView;

public class AdvancedConnectionEditor extends BaseDetailsView implements IConnectionAdvancedPropertiesEditor{

	@Override
	public Composite createControls(Composite parent, Object context, DataBindingContext dbc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isViewFor(Object object) {
		// TODO Auto-generated method stub
		return false;
	}

}
