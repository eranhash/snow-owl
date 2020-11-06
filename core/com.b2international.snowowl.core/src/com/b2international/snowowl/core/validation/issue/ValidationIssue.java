/*
 * Copyright 2017-2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.core.validation.issue;

import static com.google.common.collect.Maps.newHashMap;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.b2international.commons.collections.Collections3;
import com.b2international.index.Analyzers;
import com.b2international.index.Doc;
import com.b2international.index.Keyword;
import com.b2international.index.Script;
import com.b2international.index.Text;
import com.b2international.snowowl.core.ComponentIdentifier;
import com.b2international.snowowl.core.uri.ComponentURI;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

/**
 * @since 6.0
 */
@Doc
@Script(name = ValidationIssue.Scripts.WHITELIST, script="ctx._source.whitelisted = params.whitelisted")
@Script(name="normalizeWithOffset", script="(_score / (_score + 1.0f)) + params.offset")
public final class ValidationIssue implements Serializable {

	private static final long serialVersionUID = -230548830784505794L;

	public static class Fields {
		public static final String ID = "id";
		public static final String RULE_ID = "ruleId";
		public static final String BRANCH_PATH = "branchPath";
		public static final String COMPONENT_URI = "componentURI";
		public static final String AFFECTED_COMPONENT_ID = "affectedComponentId";
		public static final String AFFECTED_COMPONENT_TYPE = "affectedComponentType";
		public static final String AFFECTED_COMPONENT_LABELS = "affectedComponentLabels";
		public static final String AFFECTED_COMPONENT_LABELS_PREFIX = AFFECTED_COMPONENT_LABELS + ".prefix";
		public static final String AFFECTED_COMPONENT_LABELS_ORIGINAL= AFFECTED_COMPONENT_LABELS + ".original";
		public static final String WHITELISTED = "whitelisted";
		public static final String DETAILS = "details";
	}

	public static class Scripts {
		public static final String WHITELIST = "whitelist";
	}
	
	private final String id;
	private final String ruleId;
	
	@Deprecated
	private final String branchPath;
	@Deprecated
	private final String affectedComponentId;
	@Deprecated
	private final short affectedComponentType;
	
	private final ComponentURI componentURI;
	private final boolean whitelisted;
	
	@Text(analyzer = Analyzers.TOKENIZED)
	@Text(alias="prefix", analyzer = Analyzers.PREFIX, searchAnalyzer = Analyzers.TOKENIZED)
	@Keyword(alias="original")
	private List<String> affectedComponentLabels = Collections.emptyList();
	
	private Map<String, Object> details = null;
	
	private transient ComponentIdentifier affectedComponent;

	public ValidationIssue(
			final String id,
			final String ruleId, 
			final String branchPath, 
			final ComponentIdentifier affectedComponent,
			final boolean whitelisted) {
		this(id, ruleId, branchPath, ComponentURI.UNSPECIFIED, affectedComponent.getTerminologyComponentId(), affectedComponent.getComponentId(), whitelisted);
	}
	
	public ValidationIssue(
			final String id,
			final String ruleId, 
			final ComponentURI componentURI, 
			final boolean whitelisted) {
		this(id, ruleId, componentURI.codeSystemUri().getPath(), componentURI, componentURI.terminologyComponentId(), componentURI.identifier(), whitelisted);
	}
	
	@JsonCreator
	public ValidationIssue(
			@JsonProperty("id") final String id,
			@JsonProperty("ruleId") final String ruleId, 
			@JsonProperty("branchPath") final String branchPath, 
			@JsonProperty("componentURI") final ComponentURI componentURI,
			@JsonProperty("affectedComponentType") final short affectedComponentType,
			@JsonProperty("affectedComponentId") final String affectedComponentId,
			@JsonProperty("whitelisted") final boolean whitelisted) {
		this.id = id;
		this.ruleId = ruleId;
		this.branchPath = branchPath;
		this.affectedComponentId = affectedComponentId;
		this.affectedComponentType = affectedComponentType;
		this.componentURI = componentURI;
		this.whitelisted = whitelisted;
	}
	
	public String getId() {
		return id;
	}
	
	@JsonIgnore
	public ComponentIdentifier getAffectedComponent() {
		if (affectedComponent == null) {
			affectedComponent = ComponentIdentifier.of(affectedComponentType, affectedComponentId);
		}
		return affectedComponent;
	}
	
	@JsonProperty
	String getAffectedComponentId() {
		return affectedComponentId;
	}
	
	@JsonProperty
	short getAffectedComponentType() {
		return affectedComponentType;
	}
	
	public String getBranchPath() {
		return branchPath;
	}
	
	public ComponentURI getComponentURI() {
		return componentURI;
	}
	
	public String getRuleId() {
		return ruleId;
	}
	
	public boolean isWhitelisted() {
		return whitelisted;
	}
	
	public List<String> getAffectedComponentLabels() {
		return affectedComponentLabels;
	}
	
	public void setAffectedComponentLabels(List<String> affectedComponentLabels) {
		this.affectedComponentLabels = Collections3.toImmutableList(affectedComponentLabels);
	}
	
	@JsonAnyGetter
	public Map<String, Object> getDetails() {
		return details;
	}
	
	@JsonAnySetter
	public void setDetails(String key, Object value) {
		if (details == null) {
			this.details = newHashMap();
		}
		this.details.put(key, value);
	}
	
	public void setDetails(Map<String, Object> details) {
		this.details = details;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(getClass())
				.add("id", id)
				.add("ruleId", ruleId)
				.add("branchPath", branchPath)
				.add("affectedComponent", getAffectedComponent())
				.add("details", getDetails())
				.toString();
	}
	
}
