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
package com.b2international.snowowl.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.b2international.snowowl.core.attachments.AttachmentRegistryTest;
import com.b2international.snowowl.core.branch.review.ReviewSerializationTest;
import com.b2international.snowowl.core.codesystem.CodeSystemTest;
import com.b2international.snowowl.core.events.NotificationsTest;
import com.b2international.snowowl.core.events.util.PromiseTest;
import com.b2international.snowowl.core.events.util.RequestTest;
import com.b2international.snowowl.core.identity.PermissionTest;
import com.b2international.snowowl.core.jobs.JobRequestsTest;
import com.b2international.snowowl.core.locks.DatastoreLockTests;
import com.b2international.snowowl.core.locks.LockIndexTests;
import com.b2international.snowowl.core.merge.MergeConflictSerializationTest;
import com.b2international.snowowl.core.monitoring.ConsoleProgressMonitorTest;
import com.b2international.snowowl.core.request.SearchResourceRequestTest;
import com.b2international.snowowl.core.request.SortParserTest;
import com.b2international.snowowl.core.scripts.GroovyScriptEngineTest;
import com.b2international.snowowl.core.uri.CodeSystemURITest;
import com.b2international.snowowl.core.uri.ComponentURITest;
import com.b2international.snowowl.core.validation.ValidationRuleApiTest;
import com.b2international.snowowl.core.validation.ValidationThreadPoolTest;
import com.b2international.snowowl.core.validation.ValidationWhiteListApiTest;
import com.b2international.snowowl.core.validation.issue.ValidationIssueApiTest;

/**
 * @since 7.1
 */
@RunWith(Suite.class)
@SuiteClasses({
	NotificationsTest.class,
	PromiseTest.class,
	RequestTest.class,
	AttachmentRegistryTest.class,
	SortParserTest.class,
	JobRequestsTest.class,
	MergeConflictSerializationTest.class,
	ReviewSerializationTest.class,
	PermissionTest.class,
	ComponentURITest.class,
	DatastoreLockTests.class,
	CodeSystemTest.class,
	LockIndexTests.class,
	ConsoleProgressMonitorTest.class,
	GroovyScriptEngineTest.class,
	CodeSystemURITest.class,
	SearchResourceRequestTest.class,
	ValidationIssueApiTest.class,
	ValidationRuleApiTest.class,
	ValidationThreadPoolTest.class,
	ValidationWhiteListApiTest.class,
})
public class AllCoreTests {

}
