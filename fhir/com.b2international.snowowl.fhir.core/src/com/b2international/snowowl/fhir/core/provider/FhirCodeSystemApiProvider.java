/*
 * Copyright 2018-2021 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.fhir.core.provider;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import com.b2international.commons.StringUtils;
import com.b2international.commons.exceptions.BadRequestException;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.codesystem.CodeSystemVersion;
import com.b2international.snowowl.core.plugin.Component;
import com.b2international.snowowl.core.uri.CodeSystemURI;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.fhir.core.FhirCoreActivator;
import com.b2international.snowowl.fhir.core.ResourceNarrative;
import com.b2international.snowowl.fhir.core.codesystems.CodeSystemContentMode;
import com.b2international.snowowl.fhir.core.codesystems.FhirCodeSystem;
import com.b2international.snowowl.fhir.core.codesystems.NarrativeStatus;
import com.b2international.snowowl.fhir.core.codesystems.PublicationStatus;
import com.b2international.snowowl.fhir.core.model.ValidateCodeResult;
import com.b2international.snowowl.fhir.core.model.codesystem.*;
import com.b2international.snowowl.fhir.core.model.codesystem.CodeSystem.Builder;
import com.b2international.snowowl.fhir.core.model.dt.Narrative;
import com.b2international.snowowl.fhir.core.model.dt.Uri;
import com.b2international.snowowl.fhir.core.search.FhirParameter.PrefixedValue;
import com.b2international.snowowl.fhir.core.search.FhirSearchParameter;
import com.google.common.collect.Sets;

/**
 * Provider for the internal FHIR terminologies
 * 
 * @since 6.4
 */
public final class FhirCodeSystemApiProvider extends CodeSystemApiProvider {

	@Component
	public static final class Factory implements ICodeSystemApiProvider.Factory {
		@Override
		public ICodeSystemApiProvider create(IEventBus bus, List<ExtendedLocale> locales) {
			return new FhirCodeSystemApiProvider(bus, locales);
		}
	}
	
	/*
	 * No repository associated with the internal hard-coded FHIR terminologies
	 */
	public FhirCodeSystemApiProvider(IEventBus bus, List<ExtendedLocale> locales) {
		super(bus, locales, null);
	}
	
	public final Collection<CodeSystem> getCodeSystems() {
		
		Collection<CodeSystem> codeSystems = Sets.newHashSet();
		
		for (Class<?> codeSystemClass : getCodeSystemClasses()) {
			FhirCodeSystem fhirCodeSystem = createCodeSystemEnum(codeSystemClass);
			CodeSystem codeSystem = buildCodeSystem(fhirCodeSystem);
			codeSystems.add(codeSystem);
		}
		return codeSystems;
	}

	@Override
	public CodeSystem getCodeSystem(CodeSystemURI codeSystemURI) {
		
		return getCodeSystemClasses().stream()
				.map(cl-> createCodeSystemEnum(cl))
				.filter(fcs -> fcs.getCodeSystemUri().endsWith(codeSystemURI.getPath()))
				.map(this::buildCodeSystem)
				.findFirst()
				.get();
	}
	
	@Override
	public Collection<CodeSystem> getCodeSystems(Set<FhirSearchParameter> searchParameters) {
		
		Collection<CodeSystem> codeSystems = getCodeSystems();
		
		Optional<FhirSearchParameter> idParamOptional = getSearchParam(searchParameters, "_id");
		if (idParamOptional.isPresent()) {
			Collection<String> values = idParamOptional.get().getValues().stream()
					.map(PrefixedValue::getValue)
					.collect(Collectors.toSet());
			
			codeSystems = codeSystems.stream().filter(cs -> {
				return values.contains(cs.getId().getIdValue());
			}).collect(Collectors.toSet());
		}
		
		Optional<FhirSearchParameter> nameOptional = getSearchParam(searchParameters, "_name");

		if (nameOptional.isPresent()) {
			Collection<String> nameValues = nameOptional.get().getValues().stream()
					.map(PrefixedValue::getValue)
					.collect(Collectors.toSet());
			codeSystems = codeSystems.stream().filter(cs -> {
				return nameValues.contains(cs.getName());
			}).collect(Collectors.toSet());
		}
		
		return codeSystems;
	}

	@Override
	public LookupResult lookup(LookupRequest lookupRequest) {
		
		String system = lookupRequest.getSystem();
		String code = lookupRequest.getCode();
		
		if (StringUtils.isEmpty(system) || StringUtils.isEmpty(code)) {
			throw new BadRequestException("System or code parameters must not be null.");
		}
		validateRequestedProperties(lookupRequest);
		
		FhirCodeSystem fhirCodeSystem = findCodeSystemByUri(system);
		Optional<FhirCodeSystem> enumConstantOptional = getEnumConstant(fhirCodeSystem, code);
		
		FhirCodeSystem enumConstant = enumConstantOptional
				.orElseThrow(() -> new BadRequestException("Could not find code [%s] for the known code system [%s].", code, fhirCodeSystem.getCodeSystemUri()));
		
		LookupResult.Builder resultBuilder = LookupResult.builder();
		resultBuilder.name(enumConstant.getClass().getSimpleName());
		resultBuilder.display(enumConstant.getDisplayName());
		return resultBuilder.build();
	}
	
	@Override
	public SubsumptionResult subsumes(SubsumptionRequest subsumption) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ValidateCodeResult validateCode(final ValidateCodeRequest validationRequest) {
		
		String url = validationRequest.getUrl().getUriValue();
		String code = validationRequest.getCode();
		
		FhirCodeSystem fhirCodeSystem = findCodeSystemById(url);
		
		Optional<FhirCodeSystem> enumConstantOptional = getEnumConstant(fhirCodeSystem, code);
		
		if (enumConstantOptional.isPresent()) {
			return ValidateCodeResult.builder().result(true).build();
		} else {
			return ValidateCodeResult.builder().result(false)
					.message(String.format("Could not find code '%s'", code))
					.build();
		}

	}
	
	@Override
	protected Set<String> fetchAncestors(final CodeSystemURI codeSystemUri, String componentId) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Collection<String> getSupportedURIs() {

		Collection<String> codeSytemUris = Sets.newHashSet();

		Collection<Class<?>> codeSystemClasses = getCodeSystemClasses();
		
		for (Class<?> codeSystemPackageClass : codeSystemClasses) {
			FhirCodeSystem fhirCodeSystem = createCodeSystemEnum(codeSystemPackageClass);
			codeSytemUris.add(fhirCodeSystem.getCodeSystemUri());
		}
		return codeSytemUris;
	}
	
	@Override
	public boolean isSupported(CodeSystemURI codeSystemId) {
		
		Optional<String> supportedPath = getSupportedURIs().stream()
			.map(u -> u.substring(u.lastIndexOf("/") + 1))
			.filter(p -> p.equals(codeSystemId.getPath()))
			.findFirst();
		return supportedPath.isPresent();
	}
	
	private Optional<FhirCodeSystem> getEnumConstant(FhirCodeSystem fhirCodeSystem, String code) {
		
		return Sets.newHashSet(fhirCodeSystem.getClass().getDeclaredFields()).stream()
				.filter(Field::isEnumConstant)
				.map(f -> (FhirCodeSystem) createEnumInstance(f.getName(), fhirCodeSystem.getClass()))
				.filter(cs -> code.equals(cs.getCodeValue()))
				.findFirst();
	}
	
	private FhirCodeSystem findCodeSystemByUri(String systemUri) {
		
		Collection<Class<?>> codeSystemClasses = getCodeSystemClasses();
		
		return codeSystemClasses.stream()
			.map(csc -> createCodeSystemEnum(csc))
			.filter(fcs -> {
				return systemUri.equalsIgnoreCase(fcs.getCodeSystemUri());
			})
			.findFirst()
			.orElseThrow(() -> new BadRequestException("Could not find code system for ID [%s].", systemUri));
	}
	
	private FhirCodeSystem findCodeSystemById(String id) {
		
		Collection<Class<?>> codeSystemClasses = getCodeSystemClasses();
		
		return codeSystemClasses.stream()
			.map(csc -> createCodeSystemEnum(csc))
			.filter(fcs -> fcs.getCodeSystemUri().endsWith(id))
			.findFirst()
			.orElseThrow(() -> new BadRequestException("Could not find code system for ID [%s].", id));
	}
	
	/* private methods */
	private CodeSystem buildCodeSystem(FhirCodeSystem fhirCodeSystem) {
		
		String supportedUri = fhirCodeSystem.getCodeSystemUri();

		String id = getIdFromSystem(supportedUri);
		
		Builder builder = CodeSystem.builder("fhir/" + id)
			.language("en")
			.name(id)
			.publisher("www.hl7.org")
			.copyright("© 2011+ HL7")
			.version(fhirCodeSystem.getVersion())
			.caseSensitive(true)
			.status(PublicationStatus.ACTIVE)
			.url(new Uri(supportedUri))
			.content(CodeSystemContentMode.COMPLETE);
		
		//human-readable narrative
		ResourceNarrative resourceNarrative = fhirCodeSystem.getClass().getAnnotation(ResourceNarrative.class);
		if (resourceNarrative != null) {
			builder.text(Narrative.builder()
				.div("<div>" + resourceNarrative.value() + "</div>")
				.status(NarrativeStatus.GENERATED)
				.build());
		}
		
		int counter = 0;
		
		Field[] declaredFields = fhirCodeSystem.getClass().getDeclaredFields();
		for (int i = 0; i < declaredFields.length; i++) {
			if (declaredFields[i].isEnumConstant()) {
				FhirCodeSystem codeSystemEnumConstant = (FhirCodeSystem) createEnumInstance(declaredFields[i].getName(), fhirCodeSystem.getClass());
				
				Concept concept = Concept.builder()
					.code(codeSystemEnumConstant.getCode().getCodeValue())
					.display(codeSystemEnumConstant.getDisplayName())
					.build();
				
				builder.addConcept(concept);
				counter++;
			}
		}
				
		builder.count(counter);
		return builder.build();
	}
	
	/**
	 *  Example: "http://hl7.org/fhir/issue-type" -> issue-type 
	 * @param supportedUri
	 * @return
	 */
	private String getIdFromSystem(String supportedUri) {
		return supportedUri.substring(supportedUri.lastIndexOf("/") + 1, supportedUri.length());
	}

	/**
	 * @param codeSystemPackageClass
	 * @return
	 */
	private FhirCodeSystem createCodeSystemEnum(Class<?> codeSystemPackageClass) {
		
		Field[] declaredFields = codeSystemPackageClass.getDeclaredFields();
		
		//create the first enum constant if exists
		if (declaredFields.length > 0 && declaredFields[0].isEnumConstant()) {
			return (FhirCodeSystem) createEnumInstance(declaredFields[0].getName(), codeSystemPackageClass);
		}
		throw new NullPointerException("Could not create an enum for the class: " + codeSystemPackageClass);
	}

	/**
	 * @return
	 */
	private Collection<Class<?>> getCodeSystemClasses() {
		
		Collection<Class<?>> codeSystemClasses = Sets.newHashSet();
		
		Bundle bundle = FhirCoreActivator.getDefault().getBundle();
		BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
		Collection<String> listResources = bundleWiring.listResources("/com/b2international/snowowl/fhir/core/codesystems", "*", BundleWiring.FINDENTRIES_RECURSE);

		for (String codeSystemPackageClassName : listResources) {
			try {
				Class<?> codeSystemPackageClass = bundle.loadClass(codeSystemPackageClassName.replaceAll("/", ".").replaceAll(".class", ""));
				
				if (codeSystemPackageClass.isEnum()) {
					codeSystemClasses.add(codeSystemPackageClass);
				}
			} catch (Exception e) {
				//swallow the exception, only log
				e.printStackTrace();
			}
		}
		return codeSystemClasses;
				
	}

	@SuppressWarnings("unchecked")
	private <T extends Enum<T>> T createEnumInstance(String name, Type type) {
		return Enum.valueOf((Class<T>) type, name);
	}

	@Override
	protected Uri getFhirUri(com.b2international.snowowl.core.codesystem.CodeSystem codeSystem, CodeSystemVersion codeSystemVersion) {
		//handled on the per Core terminology basis (like LCS) 
		return null;
	}

	@Override
	protected int getCount(CodeSystemVersion codeSystemVersion) {
		//handled on the per Core terminology basis (like LCS) 
		return 0;
	}

	@Override
	protected String getCodeSystemShortName() {
		return null;
	}

	@Override
	protected String getRepositoryId() {
		return null;
	}

}
