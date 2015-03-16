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
package com.b2international.snowowl.snomed.datastore.internal.id;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;

import com.b2international.snowowl.snomed.datastore.ComponentNature;
import com.b2international.snowowl.snomed.datastore.id.SnomedIdentifier;
import com.b2international.snowowl.snomed.datastore.id.SnomedIdentifiers;
import com.b2international.snowowl.snomed.datastore.id.reservations.Reservation;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Range;

/**
 * @since 4.0
 */
public class ReservationRangeImpl implements Reservation {

	private Range<Long> itemIdRange;
	private String namespace;
	private Collection<ComponentNature> components;
	
	public ReservationRangeImpl(long itemIdMin, long itemIdMax, String namespace, Collection<ComponentNature> components) {
		checkArgument(components.size() >= 1, "At least one ComponentNature must be defined");
		final int minItemIdMin = Strings.isNullOrEmpty(namespace) ? 100 : 1;
		checkArgument(itemIdMin >= minItemIdMin, "ItemIdMin should be greater than or equal to %s", minItemIdMin);
		checkArgument(itemIdMax >= itemIdMin, "ItemIdMax should be greater than or equal to ItemIdMin");
		this.itemIdRange = Range.closed(itemIdMin, itemIdMax);
		this.namespace = namespace;
		this.components = components;
	}
	
	@Override
	public long getItemIdMin() {
		return this.itemIdRange.lowerEndpoint();
	}

	@Override
	public long getItemIdMax() {
		return this.itemIdRange.upperEndpoint();
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public Collection<ComponentNature> getComponents() {
		return components;
	}

	@Override
	public boolean conflicts(String componentId) {
		final SnomedIdentifier id = SnomedIdentifiers.of(componentId);
		return itemIdRange.contains(id.getItemId()) && Objects.equal(id.getNamespace(), getNamespace()) && getComponents().contains(id.getComponentNature());
	}

}
