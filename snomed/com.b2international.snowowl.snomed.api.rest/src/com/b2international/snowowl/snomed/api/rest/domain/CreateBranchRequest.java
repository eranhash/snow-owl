/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.rest.domain;

import com.b2international.snowowl.core.MetadataHolderImpl;
import com.b2international.snowowl.datastore.branch.Branch;
import com.b2international.snowowl.datastore.events.CreateBranchEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @since 4.1
 */
public abstract class CreateBranchRequest extends MetadataHolderImpl {

	@JsonProperty
	private String parent = "MAIN";
	
	@JsonProperty
	private String name;

	private String repository;
	
	public CreateBranchRequest(String repository) {
		this.repository = repository;
	}
	
	public CreateBranchEvent toEvent() {
		return new CreateBranchEvent(repository, parent, name, metadata());
	}

	public String path() {
		return parent + Branch.SEPARATOR + name;
	}
	
}
