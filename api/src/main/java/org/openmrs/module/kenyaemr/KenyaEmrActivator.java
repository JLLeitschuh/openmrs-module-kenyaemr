/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.kenyaemr;


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.metadatasharing.ImportedPackage;
import org.openmrs.module.metadatasharing.MetadataSharing;
import org.openmrs.module.metadatasharing.api.MetadataSharingService;
import org.openmrs.module.metadatasharing.wrapper.PackageImporter;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class KenyaEmrActivator implements ModuleActivator {
	
	protected Log log = LogFactory.getLog(getClass());
		
	/**
	 * @see ModuleActivator#willRefreshContext()
	 */
	public void willRefreshContext() {
		log.info("Refreshing Kenya OpenMRS EMR Module");
	}
	
	/**
	 * @see ModuleActivator#contextRefreshed()
	 */
	public void contextRefreshed() {
		log.info("Kenya OpenMRS EMR Module refreshed");
	}
	
	/**
	 * @see ModuleActivator#willStart()
	 */
	public void willStart() {
		log.info("Starting Kenya OpenMRS EMR Module");
	}
	
	/**
	 * @see ModuleActivator#started()
	 * @should install initial data only once
	 */
	public void started() {
		try {
			setupInitialData();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to setup initial data", ex);
		}
		log.info("Kenya OpenMRS EMR Module started");
	}
	
	/**
	 * @see ModuleActivator#willStop()
	 */
	public void willStop() {
		log.info("Stopping Kenya OpenMRS EMR Module");
	}
	
	/**
	 * @see ModuleActivator#stopped()
	 */
	public void stopped() {
		log.info("Kenya OpenMRS EMR Module stopped");
	}
		
    /**
     * Public for testing
     * 
     * @return whether any changes were made to the db
     * @throws Exception
     */
    public boolean setupInitialData() throws Exception {
    	boolean anyChanges = false;
    	anyChanges |= installMetadataPackageIfNecessary("fe64cb1e-5434-4c59-96f0-64db9bad1469", "KenyaEmrCoreMetadata-v3.zip");
    	return anyChanges;
    }

    /**
     * Checks whether the given version of the MDS package has been installed yet, and if not, install it
     * 
     * @param groupUuid
     * @param filename
     * @return whether any changes were made to the db
     * @throws IOException 
     */
    private boolean installMetadataPackageIfNecessary(String groupUuid, String filename) throws IOException {
    	//NameWithNoSpaces-v1.zip
    	Matcher matcher = Pattern.compile("\\w+-v(\\d+).zip").matcher(filename);
    	if (!matcher.matches())
    		throw new RuntimeException("Filename must match PackageNameWithNoSpaces-v1.zip");
    	Integer version = Integer.valueOf(matcher.group(1));
    	
    	ImportedPackage installed = Context.getService(MetadataSharingService.class).getImportedPackageByGroup(groupUuid);
    	if (installed != null && installed.getVersion() >= version) {
    		log.info("Metadata package " + filename + " is already installed with version " + installed.getVersion());
    		return false;
    	}
    	
    	if (getClass().getClassLoader().getResource(filename) == null) {
    		throw new RuntimeException("Cannot find " + filename + " for group " + groupUuid + ". Make sure it's in api/src/main/resources");
    	}
    	
    	PackageImporter metadataImporter = MetadataSharing.getInstance().newPackageImporter();
    	metadataImporter.loadSerializedPackageStream(getClass().getClassLoader().getResourceAsStream(filename));
    	metadataImporter.importPackage();
    	return true;
    }

}