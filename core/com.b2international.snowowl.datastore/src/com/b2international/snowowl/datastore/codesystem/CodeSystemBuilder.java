/*
 * Copyright 2011-2016 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.b2international.snowowl.datastore.codesystem;

import java.util.Map;

import com.b2international.snowowl.terminologymetadata.CodeSystem;

/**
 * @since 4.7
 */
public interface CodeSystemBuilder<B extends CodeSystemBuilder<B, C>, C extends CodeSystem> {
	
	String EXTENSION_ID = "com.b2international.snowowl.datastore.codesystembuilder";

	String getRepositoryUuid();
	
	B init(Map<String, String> valueMap);
	
	B withCitation(String citation);

	B withCodeSystemOid(String codeSystemOid);

	B withIconPath(String iconPath);

	B withLanguage(String language);

	B withMaintainingOrganizationLink(String maintainingOrganiationLink);

	B withName(String name);

	B withShortName(String shortName);

	B withTerminologyComponentId(String terminologyComponentId);

	B withRepositoryUuid(String repositoryUuid);

	B withBranchPath(String branchPath);
	
	B withExtension(Map<String, String> extensionMap);
	
	C build();
	
	C create();

}
