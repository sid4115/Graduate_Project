/*****************************************************************************
 * Copyright (c) 2016 CEA LIST and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   
 *****************************************************************************/

package ca.queensu.cs.rover.configuration;

import org.eclipse.emf.ecore.EObject;

import ca.queensu.cs.rover.configuration.internal.ConfigurationPage;

/**
 * @author sudharshan
 *
 */
public interface Configuration extends EObject {
	
	/**
	 * Returns the value of the '<em><b>Welcome Page</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Configuration Page</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Configuration Page</em>' containment reference.
	 * @see #setConfigurationPage(ConfigurationPage)
	 * @model containment="true" ordered="false"
	 * @generated
	 */
	ConfigurationPage getConfigurationPage();
	
	/**
	 * Sets the value of the '{@link ca.queensu.cs.rover.configuration.Configuration#getConfigurationPage <em>Welcome Page</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @param value
	 *            the new value of the '<em>Configuration Page</em>' containment reference.
	 * @see #getConfigurationPage()
	 * @generated
	 */
	void setConfigurationPage(ConfigurationPage value);
	
	/**
	 * Creates a new {@link ca.queensu.cs.rover.configuration.Configuration#getConfigurationPage} and sets the '<em><b>Configuration Page</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @return The new {@link a.queensu.cs.rover.configuration.ConfigurationPage}.
	 * @see #getConfigurationPage()
	 * @generated
	 */
	ConfigurationPage createConfigurationPage();

}
