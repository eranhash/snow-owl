/*
 * Copyright 2011-2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.datastore.request;

import static com.b2international.index.revision.Commit.Expressions.allCommentPrefixesPresent;
import static com.b2international.index.revision.Commit.Expressions.branch;
import static com.b2international.index.revision.Commit.Expressions.exactComment;
import static com.b2international.index.revision.Commit.Expressions.timestampRange;
import static com.b2international.index.revision.Commit.Expressions.userId;

import java.util.List;

import com.b2international.index.Hits;
import com.b2international.index.query.Expression;
import com.b2international.index.query.Expressions;
import com.b2international.index.query.Expressions.ExpressionBuilder;
import com.b2international.index.revision.Commit;
import com.b2international.snowowl.core.domain.RepositoryContext;
import com.b2international.snowowl.datastore.commitinfo.CommitInfoConverter;
import com.b2international.snowowl.datastore.commitinfo.CommitInfos;
import com.google.common.collect.Lists;

/**
 * @since 5.2
 */
final class CommitInfoSearchRequest extends SearchIndexResourceRequest<RepositoryContext, CommitInfos, Commit> {

	private static final long serialVersionUID = 1L;
	
	enum OptionKey {
		
		BRANCH,
		USER_ID,
		COMMENT,
		TIME_STAMP_FROM,
		TIME_STAMP_TO
		
	}
	
	CommitInfoSearchRequest() {}

	@Override
	protected Class<Commit> getDocumentType() {
		return Commit.class;
	}
	
	@Override
	protected Expression prepareQuery(RepositoryContext context) {
		ExpressionBuilder queryBuilder = Expressions.builder();
		addIdFilter(queryBuilder, Commit.Expressions::ids);
		addBranchClause(queryBuilder);
		addUserIdClause(queryBuilder);
		addCommentClause(queryBuilder);
		addTimeStampClause(queryBuilder);
		return queryBuilder.build();
	}
	
	@Override
	protected boolean trackScores() {
		return containsKey(OptionKey.COMMENT);
	}

	@Override
	protected CommitInfos toCollectionResource(RepositoryContext context, Hits<Commit> hits) {
		if (limit() < 1 || hits.getTotal() < 1) {
			return new CommitInfos(context.id(), limit(), hits.getTotal());
		} else {
			return new CommitInfoConverter(context, expand(), locales()).convert(hits.getHits(), hits.getScrollId(), hits.getSearchAfter(), limit(), hits.getTotal());
		}
	}
	
	@Override
	protected CommitInfos createEmptyResult(int limit) {
		throw new UnsupportedOperationException("Missing repositoryId parameter at this point");
	}
	
	private void addBranchClause(final ExpressionBuilder builder) {
		if (containsKey(OptionKey.BRANCH)) {
			final String branch = getString(OptionKey.BRANCH);
			builder.filter(branch(branch));
		}
	}

	private void addUserIdClause(final ExpressionBuilder builder) {
		if (containsKey(OptionKey.USER_ID)) {
			final String userId = getString(OptionKey.USER_ID);
			builder.filter(userId(userId));
		}
	}

	private void addCommentClause(final ExpressionBuilder builder) {
		if (containsKey(OptionKey.COMMENT)) {
			final String comment = getString(OptionKey.COMMENT);
			final List<Expression> disjuncts = Lists.newArrayList();
			
			disjuncts.add(exactComment(comment));
			disjuncts.add(allCommentPrefixesPresent(comment));
			
			builder.must(Expressions.dismax(disjuncts));
		}
	}

	private void addTimeStampClause(final ExpressionBuilder builder) {
		if (containsKey(OptionKey.TIME_STAMP_FROM) || containsKey(OptionKey.TIME_STAMP_TO)) {
			final Long timestampFrom = containsKey(OptionKey.TIME_STAMP_FROM) ? get(OptionKey.TIME_STAMP_FROM, Long.class) : 0L;
			final Long timestampTo = containsKey(OptionKey.TIME_STAMP_TO) ? get(OptionKey.TIME_STAMP_TO, Long.class) : Long.MAX_VALUE;
			builder.filter(timestampRange(timestampFrom, timestampTo));
		}
	}

}
