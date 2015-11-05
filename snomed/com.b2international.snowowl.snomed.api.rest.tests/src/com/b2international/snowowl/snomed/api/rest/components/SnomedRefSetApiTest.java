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
package com.b2international.snowowl.snomed.api.rest.components;

import static com.b2international.snowowl.snomed.api.rest.SnomedBranchingApiAssert.givenBranchWithPath;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentApiAssert.assertComponentCreated;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentApiAssert.*;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentApiAssert.assertComponentReadWithStatus;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentApiAssert.givenConceptRequestBody;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.rest.AbstractSnomedApiTest;
import com.b2international.snowowl.snomed.api.rest.SnomedApiTestConstants;
import com.b2international.snowowl.snomed.api.rest.SnomedComponentType;
import com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * @since 4.5
 */
public class SnomedRefSetApiTest extends AbstractSnomedApiTest {

	@Test
	public void getReferenceSetFromNonExistingBranch() throws Exception {
		assertComponentReadWithStatus(BranchPathUtils.createPath("MAIN/nonexistent"), SnomedComponentType.REFSET, "fake", 404);
	}
	
	@Test
	public void getNonExistingReferenceSet() throws Exception {
		assertComponentReadWithStatus(BranchPathUtils.createMainPath(), SnomedComponentType.REFSET, "123456789", 404);
	}
	
	@Test
	public void createSimpleTypeConceptRefSet() throws Exception {
		givenBranchWithPath(testBranchPath);
		final Map<String,Object> requestBody = createRefSetRequestBody(SnomedRefSetType.SIMPLE, SnomedTerminologyComponentConstants.CONCEPT, Concepts.REFSET_SIMPLE_TYPE);
		final String refSetId = assertComponentCreated(testBranchPath, SnomedComponentType.REFSET, requestBody);
		assertComponentExists(testBranchPath, SnomedComponentType.REFSET, refSetId);
	}
	
	@Test
	public void createSimpleTypeRefSetWithWrongReferencedComponentType() throws Exception {
		givenBranchWithPath(testBranchPath);
		final Map<String,Object> requestBody = createRefSetRequestBody(SnomedRefSetType.SIMPLE, SnomedTerminologyComponentConstants.REFSET, Concepts.REFSET_SIMPLE_TYPE);
		assertComponentNotCreated(testBranchPath, SnomedComponentType.REFSET, requestBody);
	}
	
	@Test
	public void createQueryTypeRefSetWithWrongReferencedComponentType() throws Exception {
		final Map<String,Object> requestBody = createRefSetRequestBody(SnomedRefSetType.QUERY, SnomedTerminologyComponentConstants.CONCEPT, Concepts.REFSET_QUERY_SPECIFICATION_TYPE);
		assertComponentNotCreated(BranchPathUtils.createMainPath(), SnomedComponentType.REFSET, requestBody);
	}
	
	@Test
	public void createQueryTypeRefSet() throws Exception {
		givenBranchWithPath(testBranchPath);
		final Map<String,Object> requestBody = createRefSetRequestBody(SnomedRefSetType.QUERY, SnomedTerminologyComponentConstants.REFSET, Concepts.REFSET_QUERY_SPECIFICATION_TYPE);
		final String refSetId = assertComponentCreated(testBranchPath, SnomedComponentType.REFSET, requestBody);
		assertComponentExists(testBranchPath, SnomedComponentType.REFSET, refSetId);
	}
	
	private ImmutableMap<String, Object> createRefSetRequestBody(SnomedRefSetType type, String referencedComponentType, String parent) {
		final Map<String, Object> conceptBody = (Map<String, Object>) givenConceptRequestBody(null, parent, Concepts.MODULE_SCT_CORE, SnomedApiTestConstants.PREFERRED_ACCEPTABILITY_MAP, true);
		final Builder<String, Object> requestBody = ImmutableMap.builder();
		requestBody.putAll(conceptBody);
		requestBody.put("commitComment", String.format("New %s type reference set with %s members", type, referencedComponentType));
		requestBody.put("type", type);
		requestBody.put("referencedComponentType", referencedComponentType);
		return requestBody.build();
	}
	
}
