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
package com.b2international.snowowl.snomed.datastore;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.b2international.snowowl.snomed.core.tree.TerminologyTreeTest;
import com.b2international.snowowl.snomed.datastore.id.memory.BulkInMemorySnomedIdentifierServiceTest;
import com.b2international.snowowl.snomed.datastore.id.memory.DefaultSnomedIdentifierServiceTest;
import com.b2international.snowowl.snomed.datastore.id.memory.InMemorySnomedIdentifierServiceTest;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedConceptIndexEntrySerializationTest;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntrySerializationTest;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRelationshipIndexEntrySerializationTest;
import com.b2international.snowowl.snomed.datastore.internal.id.SnomedIdentifierTest;
import com.b2international.snowowl.snomed.datastore.internal.id.reservations.ReservationImplTest;
import com.b2international.snowowl.snomed.datastore.internal.id.reservations.SnomedIdentifierReservationServiceImplTest;

/**
 * @since 4.0
 */
@RunWith(Suite.class)
@SuiteClasses({
	TerminologyTreeTest.class,
	ReservationImplTest.class,
	SnomedIdentifierTest.class,
	SnomedIdentifierReservationServiceImplTest.class,
	BulkInMemorySnomedIdentifierServiceTest.class,
	DefaultSnomedIdentifierServiceTest.class,
	InMemorySnomedIdentifierServiceTest.class,
	SnomedConceptIndexEntrySerializationTest.class,
	SnomedRelationshipIndexEntrySerializationTest.class,
	SnomedDescriptionIndexEntrySerializationTest.class
})
public class AllSnomedDatastoreTests {

}
