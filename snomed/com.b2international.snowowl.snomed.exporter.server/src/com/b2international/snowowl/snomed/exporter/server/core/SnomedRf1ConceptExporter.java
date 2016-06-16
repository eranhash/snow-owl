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
package com.b2international.snowowl.snomed.exporter.server.core;

import static com.b2international.commons.StringUtils.valueOfOrEmptyString;

import java.io.IOException;

import com.b2international.index.Hits;
import com.b2international.index.query.Expression;
import com.b2international.index.query.Expressions;
import com.b2international.index.query.Query;
import com.b2international.index.query.Query.QueryBuilder;
import com.b2international.index.revision.RevisionSearcher;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.core.domain.ISnomedConcept;
import com.b2international.snowowl.snomed.core.lang.LanguageSetting;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedConceptDocument;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRefSetMemberIndexEntry;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.b2international.snowowl.snomed.exporter.server.ComponentExportType;
import com.b2international.snowowl.snomed.exporter.server.Id2Rf1PropertyMapper;
import com.b2international.snowowl.snomed.exporter.server.SnomedReleaseFileHeaders;
import com.b2international.snowowl.snomed.exporter.server.SnomedRfFileNameBuilder;
import com.b2international.snowowl.snomed.exporter.server.sandbox.SnomedExportConfiguration;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * RF1 exporter for SNOMED&nbsp;CT concepts.
 */
public class SnomedRf1ConceptExporter extends AbstractSnomedRf1Exporter<SnomedConceptDocument> {
	
	/**
	 * Line in the Concept RF1 file
	 */
	class Rf1Concept {
		
		public String id;
		public String status;
		public String fsn;
		public String ctv3;
		public String snomedRt;
		public String definitionStatus;
		
		@Override
		public String toString() {
			
			return new StringBuilder(valueOfOrEmptyString(id))
					.append(HT)
					.append(getConceptStatus(valueOfOrEmptyString(status)))
					.append(HT)
					.append(valueOfOrEmptyString(fsn))
					.append(HT)
					.append(valueOfOrEmptyString(ctv3))
					.append(HT)
					.append(valueOfOrEmptyString(snomedRt))
					.append(HT)
					.append(valueOfOrEmptyString(definitionStatus))
					.toString();
		}
	}
	
	/**
	 * Constructor
	 * @param configuration export configuration
	 * @param mapper RF2->RF1 mapper
	 */
	public SnomedRf1ConceptExporter(final SnomedExportConfiguration configuration, final Id2Rf1PropertyMapper mapper) {
		super(SnomedConceptDocument.class, configuration, mapper);
	}
	
	/**
	 * @param snomedConceptDocument
	 * @return
	 * @throws IOException 
	 */
	@Override
	protected String convertToRF1(SnomedConceptDocument revisionDocument) throws IOException {
		
		Rf1Concept concept = new Rf1Concept();
		
		concept.id = revisionDocument.getId();
		concept.status = revisionDocument.isActive() ? "1" : "0";
		concept.definitionStatus = revisionDocument.isPrimitive() ? "1" : "0";
		
		RevisionSearcher revisionSearcher = getConfiguration().getRevisionSearcher();
		QueryBuilder<SnomedRefSetMemberIndexEntry> refsetMemberQueryBuilder = Query.builder(SnomedRefSetMemberIndexEntry.class);

		final LanguageSetting languageSetting = ApplicationContext.getInstance().getService(LanguageSetting.class);
		IEventBus eventBus = ApplicationContext.getInstance().getService(IEventBus.class);
		
		//fsn
		ISnomedConcept snomedConcept = SnomedRequests.prepareGetConcept()
			.setComponentId(revisionDocument.getId())
			.setLocales(languageSetting.getLanguagePreference())
			.setExpand("fsn()").build(getConfiguration().getCurrentBranchPath().getPath()).executeSync(eventBus);
		
		concept.fsn = snomedConcept.getFsn().getTerm();
		
		//inactivation status
		if (!revisionDocument.isActive()) {
			
			Expression condition = Expressions.builder()
					.must(SnomedRefSetMemberIndexEntry.Expressions.referencedComponentIds(Sets.newHashSet(revisionDocument.getId())))
					.must(SnomedRefSetMemberIndexEntry.Expressions.referenceSetId(Sets.newHashSet(Concepts.REFSET_CONCEPT_INACTIVITY_INDICATOR)))
					.must(SnomedRefSetMemberIndexEntry.Expressions.active()).build();
			
			Query<SnomedRefSetMemberIndexEntry> query = refsetMemberQueryBuilder.selectAll().where(condition).build();
						
			Hits<SnomedRefSetMemberIndexEntry> snomedRefSetMemberIndexEntrys = revisionSearcher.search(query);
			
			//there should be only one max
			for (SnomedRefSetMemberIndexEntry snomedRefSetMemberIndexEntry : snomedRefSetMemberIndexEntrys) {
				concept.status = snomedRefSetMemberIndexEntry.getValueId();
			}
		}
		
		Expression condition = Expressions.builder()
				.must(SnomedRefSetMemberIndexEntry.Expressions.referencedComponentIds(Sets.newHashSet(revisionDocument.getId())))
				.must(SnomedRefSetMemberIndexEntry.Expressions.referenceSetId(Sets.newHashSet(Concepts.CTV3_SIMPLE_MAP_TYPE_REFERENCE_SET_ID)))
				.must(SnomedRefSetMemberIndexEntry.Expressions.active()).build();
		
		Query<SnomedRefSetMemberIndexEntry> query = refsetMemberQueryBuilder.selectAll().where(condition).build();
		Hits<SnomedRefSetMemberIndexEntry> snomedRefSetMemberIndexEntrys = revisionSearcher.search(query);
		
		//there should be only one max
		for (SnomedRefSetMemberIndexEntry snomedRefSetMemberIndexEntry : snomedRefSetMemberIndexEntrys) {
			concept.ctv3 = snomedRefSetMemberIndexEntry.getTargetComponentId();
		}
		
		condition = Expressions.builder()
				.must(SnomedRefSetMemberIndexEntry.Expressions.referencedComponentIds(Sets.newHashSet(revisionDocument.getId())))
				.must(SnomedRefSetMemberIndexEntry.Expressions.referenceSetId(Sets.newHashSet(Concepts.SNOMED_RT_SIMPLE_MAP_TYPE_REFERENCE_SET_ID)))
				.must(SnomedRefSetMemberIndexEntry.Expressions.active()).build();
		
		query = refsetMemberQueryBuilder.selectAll().where(condition).build();
		snomedRefSetMemberIndexEntrys = revisionSearcher.search(query);
		
		//there should be only one max
		for (SnomedRefSetMemberIndexEntry snomedRefSetMemberIndexEntry : snomedRefSetMemberIndexEntrys) {
			concept.snomedRt = snomedRefSetMemberIndexEntry.getTargetComponentId();
		}
		return concept.toString();
	}
	
	@Override
	public String getRelativeDirectory() {
		return RF1_CORE_RELATIVE_DIRECTORY;
	}

	@Override
	public String getFileName() {
		return SnomedRfFileNameBuilder.buildCoreRf1FileName(getType(), configuration);
	}

	@Override
	public ComponentExportType getType() {
		return ComponentExportType.CONCEPT;
	}

	@Override
	public String[] getColumnHeaders() {
		return SnomedReleaseFileHeaders.RF1_CONCEPT_HEADER;
	}

	@Override
	public SnomedExportConfiguration getConfiguration() {
		return configuration;
	}

	/*returns with a number indicating the status of a concept for RF1 publication.*/
	private String getConceptStatus(final String stringValue) {
		//magic mapping between RF1 and RF2 statuses
		if ("1".equals(stringValue)) {
			return "0";
		} else if ("0".equals(stringValue)) {
			return "1";
		} else {
			return Preconditions.checkNotNull(mapper.getConceptStatusProperty(stringValue));
		}
	}
	
}
