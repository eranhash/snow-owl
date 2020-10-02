/*
 * Copyright 2018-2020 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.fhir.tests.serialization.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.b2international.snowowl.core.date.Dates;
import com.b2international.snowowl.fhir.core.FhirConstants;
import com.b2international.snowowl.fhir.core.codesystems.*;
import com.b2international.snowowl.fhir.core.exceptions.ValidationException;
import com.b2international.snowowl.fhir.core.model.Designation;
import com.b2international.snowowl.fhir.core.model.Issue;
import com.b2international.snowowl.fhir.core.model.codesystem.CodeSystem;
import com.b2international.snowowl.fhir.core.model.codesystem.Concept;
import com.b2international.snowowl.fhir.core.model.codesystem.Filter;
import com.b2international.snowowl.fhir.core.model.codesystem.SupportedConceptProperty;
import com.b2international.snowowl.fhir.core.model.dt.Code;
import com.b2international.snowowl.fhir.core.model.dt.Coding;
import com.b2international.snowowl.fhir.core.model.dt.Identifier;
import com.b2international.snowowl.fhir.core.model.dt.Uri;
import com.b2international.snowowl.fhir.core.model.property.BooleanConceptProperty;
import com.b2international.snowowl.fhir.core.model.property.CodeConceptProperty;
import com.b2international.snowowl.fhir.core.model.property.CodingConceptProperty;
import com.b2international.snowowl.fhir.core.model.property.DateTimeConceptProperty;
import com.b2international.snowowl.fhir.core.model.property.DecimalConceptProperty;
import com.b2international.snowowl.fhir.core.model.property.IntegerConceptProperty;
import com.b2international.snowowl.fhir.core.model.property.StringConceptProperty;
import com.b2international.snowowl.fhir.tests.FhirExceptionIssueMatcher;
import com.b2international.snowowl.fhir.tests.FhirTest;

import io.restassured.path.json.JsonPath;

/**
 * Test for checking the serialization from model->JSON.
 * @since 6.3
 */
public class CodeSystemSerializationTest extends FhirTest {
	
	//Supported property in the code system
	@Test
	public void supportedConceptPropertyTest() throws Exception {
		
		SupportedConceptProperty conceptProperty = SupportedConceptProperty.builder(CommonConceptProperties.INACTIVE).build();
		
		String expectedJson = "{\"code\":\"inactive\","
				+ "\"uri\":\"http://hl7.org/fhir/concept-properties/inactive\","
				+ "\"description\":\"Inactive\","
				+ "\"type\":\"boolean\"}";
		
		assertEquals(expectedJson, objectMapper.writeValueAsString(conceptProperty));
	}
	
	@Test
	public void returnedBooleanConceptPropertyTest() throws Exception {
		
		BooleanConceptProperty conceptProperty = BooleanConceptProperty.builder()
			.code("childConcept")
			.value(true)
			.build();
		
		String expected = "{\"code\":\"childConcept\",\"valueBoolean\":true}";
		Assert.assertEquals(expected, objectMapper.writeValueAsString(conceptProperty));
	}
	
	@Test
	public void returnedStringConceptPropertyTest() throws Exception {
		
		StringConceptProperty conceptProperty = StringConceptProperty.builder()
			.code("childConcept")
			.value("string")
			.build();
		
		String expected = "{\"code\":\"childConcept\",\"valueString\":\"string\"}";
		Assert.assertEquals(expected, objectMapper.writeValueAsString(conceptProperty));
	}
	
	@Test
	public void returnedIntegerConceptPropertyTest() throws Exception {
		
		IntegerConceptProperty conceptProperty = IntegerConceptProperty.builder()
			.code("childConcept")
			.value(1)
			.build();
		
		String expected = "{\"code\":\"childConcept\",\"valueInteger\":1}";
		Assert.assertEquals(expected, objectMapper.writeValueAsString(conceptProperty));
	}

	@Test
	public void returnedDecimalConceptPropertyTest() throws Exception {
		
		DecimalConceptProperty conceptProperty = DecimalConceptProperty.builder()
				.code("childConcept")
				.value(1.12f)
				.build();
		
		String expected = "{\"code\":\"childConcept\",\"valueDecimal\":1.12}";
		Assert.assertEquals(expected, objectMapper.writeValueAsString(conceptProperty));
	}
	
	@Test
	public void returnedDateTimeConceptPropertyTest() throws Exception {
		
		Date date = Dates.parse("2018-03-23T08:49:40.000+0100", FhirConstants.DATE_TIME_FORMAT);
		
		DateTimeConceptProperty conceptProperty = DateTimeConceptProperty.builder()
			.code("childConcept")
			.value(date)
			.build();
		
		String expected = "{\"code\":\"childConcept\",\"valueDateTime\":\"2018-03-23T07:49:40.000+00:00\"}";
		Assert.assertEquals(expected, objectMapper.writeValueAsString(conceptProperty));
	}
	
	@Test
	public void returnedCodeConceptPropertyTest() throws Exception {
		
		CodeConceptProperty conceptProperty = CodeConceptProperty.builder()
			.code("childConcept")
			.value(new Code("code"))
			.build();
		
		String expected = "{\"code\":\"childConcept\",\"valueCode\":\"code\"}";
		Assert.assertEquals(expected, objectMapper.writeValueAsString(conceptProperty));
	}
	
	@Test
	public void returnedCodingConceptPropertyTest() throws Exception {
		
		CodingConceptProperty conceptProperty = CodingConceptProperty.builder()
				.code(CommonConceptProperties.CHILD.getCode())
				.value(new Coding.Builder()
					.code("codingCode")
					.system("uri")
					.build())
				.build();
		
		JsonPath jsonPath = JsonPath.from(objectMapper.writeValueAsString(conceptProperty));
		
		assertThat(jsonPath.getString("code")).isEqualTo("child");
		jsonPath.setRoot("valueCoding");
		assertThat(jsonPath.getString("code")).isEqualTo("codingCode");
		assertThat(jsonPath.getString("system")).isEqualTo("uri");
	}
	
	@Test
	public void filterMissingFieldsTest() throws Exception {
		exception.expect(ValidationException.class);
		exception.expectMessage("3 validation errors");
		Filter.builder().build();
	}

	@Test
	public void filterMissingCodeTest() throws Exception {
		
		Issue expectedIssue = Issue.builder()
			.code(IssueType.INVALID)
			.severity(IssueSeverity.ERROR)
			.diagnostics("1 validation error")
			.addLocation("Filter.code")
			.codeableConceptWithDisplay(OperationOutcomeCode.MSG_PARAM_INVALID, "Parameter 'code' content is invalid [null]. Violation: may not be null.")
			.build();
		
		exception.expect(ValidationException.class);
		exception.expectMessage("1 validation error");
		exception.expect(FhirExceptionIssueMatcher.issue(expectedIssue));
		
		Filter.builder()
			.value("A SNOMED CT code")
			.addOperator(FilterOperator.EQUALS)
			.build();
	}
	
	//status(code)/content(code)
	@Test
	public void incompleteCodeSystemTest() throws Exception {
		
		exception.expect(ValidationException.class);
		exception.expectMessage("2 validation errors");
		
		CodeSystem.builder().build();
	}
	
	@Test 
	public void minimalCodeSystemTest() throws Exception {
		
		CodeSystem codeSystem = CodeSystem.builder()
			.status(PublicationStatus.ACTIVE)
			.content(CodeSystemContentMode.COMPLETE)
			.build();
		
		applyFilter(codeSystem);
		
		JsonPath jsonPath = JsonPath.from(objectMapper.writeValueAsString(codeSystem));
		
		assertThat(jsonPath.getString("resourceType")).isEqualTo("CodeSystem");
		assertThat(jsonPath.getString("status")).isEqualTo("active");
		assertThat(jsonPath.getString("content")).isEqualTo("complete");
		
		String expectedJson = "{\"resourceType\":\"CodeSystem\","
				+ "\"status\":\"active\","
				+ "\"content\":\"complete\"}";
		
		assertEquals(expectedJson, objectMapper.writeValueAsString(codeSystem));
	}
	
	@Test 
	public void negativeCountCodeSystemTest() throws Exception {
		
		Issue expectedIssue = Issue.builder()
				.code(IssueType.INVALID)
				.severity(IssueSeverity.ERROR)
				.diagnostics("1 validation error")
				.addLocation("CodeSystem.count")
				.codeableConceptWithDisplay(OperationOutcomeCode.MSG_PARAM_INVALID, "Parameter 'count' content is invalid [-1]. Violation: Count must be equal to or larger than 0.")
				.build();
			
			exception.expect(ValidationException.class);
			exception.expectMessage("1 validation error");
			exception.expect(FhirExceptionIssueMatcher.issue(expectedIssue));
		
		CodeSystem.builder()
			.status(PublicationStatus.ACTIVE)
			.content(CodeSystemContentMode.COMPLETE)
			.count(-1)
			.build();
	}
	
	@Test 
	public void codeSystemTest() throws Exception {
		
		Identifier identifier = Identifier.builder()
			.use(IdentifierUse.OFFICIAL)
			.system(new Uri("www.hl7.org"))
			.value("OID:1234.1234")
			.build();
		
		CodeSystem codeSystem = CodeSystem.builder("repo/shortName")
			.addProperty(SupportedConceptProperty.builder(CommonConceptProperties.CHILD).build())
			.description("Code system description")
			.date("2018-03-09T19:50:21.000+00:00")
			.hierarchyMeaning(CodeSystemHierarchyMeaning.IS_A)
			.identifier(identifier)
			.language("en")
			.name("Local code system")
			.narrative(NarrativeStatus.ADDITIONAL, "<div>Some html text</div>")
			.title("title")
			.publisher("B2i")
			.status(PublicationStatus.ACTIVE)
			.content(CodeSystemContentMode.COMPLETE)
			.supplements(new Uri("http://b2i.sg/supplement"))
			.url(new Uri("code system uri"))
			.version("2018.01.01")
			.addConcept(Concept.builder()
				.code("conceptCode")
				.definition("This is a code definition")
				.display("Label")
				.addDesignation(Designation.builder()
					.languageCode("uk_en")
					.use(Coding.builder()
						.code("internal")
						.system("http://b2i.sg/test")
						.build()
						)
					.value("conceptLabel_uk")
					.build())
				.addProperties(CodeConceptProperty.builder()
						.code("childConcept")
						.value(new Code("childId"))
						.build())
				.build())
			.build();
		
		applyFilter(codeSystem);
		
		JsonPath jsonPath = getJsonPath(codeSystem);
		
		assertThat(jsonPath.getString("resourceType")).isEqualTo("CodeSystem");
		assertThat(jsonPath.getString("id")).isEqualTo("repo/shortName");
		assertThat(jsonPath.getString("language")).isEqualTo("en");
		assertThat(jsonPath.getString("text.status")).isEqualTo("additional");
		assertThat(jsonPath.getString("text.div")).isEqualTo("<div>Some html text</div>");
		assertThat(jsonPath.getString("url")).isEqualTo("code system uri");

		assertThat(jsonPath.getString("identifier.use")).isEqualTo("official");
		assertThat(jsonPath.getString("identifier.system")).isEqualTo("www.hl7.org");
		assertThat(jsonPath.getString("identifier.value")).isEqualTo("OID:1234.1234");
		
		assertThat(jsonPath.getString("version")).isEqualTo("2018.01.01");
		assertThat(jsonPath.getString("name")).isEqualTo("Local code system");
		assertThat(jsonPath.getString("title")).isEqualTo("title");
		assertThat(jsonPath.getString("status")).isEqualTo("active");
		assertThat(jsonPath.getString("publisher")).isEqualTo("B2i");
		assertThat(jsonPath.getString("description")).isEqualTo("Code system description");
		assertThat(jsonPath.getString("hierarchyMeaning")).isEqualTo("is-a");
		assertThat(jsonPath.getString("content")).isEqualTo("complete");
		assertThat(jsonPath.getString("supplements")).isEqualTo("http://b2i.sg/supplement");
		
		assertThat(jsonPath.getString("property[0].code")).isEqualTo("child");
		assertThat(jsonPath.getString("property[0].uri")).isEqualTo("http://hl7.org/fhir/concept-properties/child");
		assertThat(jsonPath.getString("property[0].description")).isEqualTo("Child");
		assertThat(jsonPath.getString("property[0].type")).isEqualTo("code");
		
		jsonPath.setRoot("concept[0]");
		
		assertThat(jsonPath.getString("code")).isEqualTo("conceptCode");
		assertThat(jsonPath.getString("display")).isEqualTo("Label");
		assertThat(jsonPath.getString("definition")).isEqualTo("This is a code definition");
		assertThat(jsonPath.getString("designation[0].language")).isEqualTo("uk_en");
		assertThat(jsonPath.getString("designation[0].use.code")).isEqualTo("internal");
		assertThat(jsonPath.getString("designation[0].use.system")).isEqualTo("http://b2i.sg/test");
		assertThat(jsonPath.getString("designation[0].value")).isEqualTo("conceptLabel_uk");
		assertThat(jsonPath.getString("designation[0].languageCode")).isEqualTo("uk_en");

		assertThat(jsonPath.getString("property[0].code")).isEqualTo("childConcept");
		assertThat(jsonPath.getString("property[0].valueCode")).isEqualTo("childId");
	}

}
