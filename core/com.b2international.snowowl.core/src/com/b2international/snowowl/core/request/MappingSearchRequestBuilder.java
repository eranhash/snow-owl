/*
 * Copyright 2020 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.core.request;

import com.b2international.snowowl.core.domain.BranchContext;
import com.b2international.snowowl.core.domain.ConceptMapMappings;
import com.b2international.snowowl.core.request.SetSearchRequestEvaluator.OptionKey;
import com.google.common.collect.ImmutableSet;

/**
* @since 7.8
*/
public final class MappingSearchRequestBuilder extends SearchResourceRequestBuilder<MappingSearchRequestBuilder, BranchContext, ConceptMapMappings> 
		implements RevisionIndexRequestBuilder<ConceptMapMappings> {
	
	public MappingSearchRequestBuilder filterBySet(String setId) {
		return filterBySets(ImmutableSet.of(setId));
	}
	
	public MappingSearchRequestBuilder filterBySets(Iterable<String> setIds) {
		return addOption(OptionKey.SET, setIds);
	}
	
	@Override
	protected SearchResourceRequest<BranchContext, ConceptMapMappings> createSearch() {
		return new MappingSearchRequest();
	}

}
