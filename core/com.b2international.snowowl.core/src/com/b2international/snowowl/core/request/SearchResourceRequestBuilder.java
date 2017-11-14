/*
 * Copyright 2011-2017 B2i Healthcare Pte Ltd, http://b2i.sg
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

import java.util.Collection;
import java.util.List;

import com.b2international.commons.CompareUtils;
import com.b2international.commons.options.OptionsBuilder;
import com.b2international.snowowl.core.ServiceProvider;
import com.b2international.snowowl.core.domain.PageableCollectionResource;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.core.request.SearchResourceRequest.OptionKey;
import com.b2international.snowowl.core.request.SearchResourceRequest.SortField;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * @since 5.2
 */
public abstract class SearchResourceRequestBuilder<B extends SearchResourceRequestBuilder<B, C, R>, C extends ServiceProvider, R> extends ResourceRequestBuilder<B, C, R> {
	
	private static final int MAX_LIMIT = Integer.MAX_VALUE - 1;
	
	private String scrollKeepAlive;
	private String scrollId;
	private Object[] searchAfter;
	
	private int limit = 50;
	
	private final OptionsBuilder optionsBuilder = OptionsBuilder.newBuilder();
	
	protected SearchResourceRequestBuilder() {
		super();
	}
	
	/**
	 * Sets the search after parameter to the specified array of scroll values.
	 * @param searchAfter
	 * @return
	 * @see PageableCollectionResource#getSearchAfter()
	 */
	public B setSearchAfter(Object[] scrollValues) {
		this.searchAfter = scrollValues;
		return getSelf();
	}
	
	/**
	 * Sets the scroll keep alive value to the specified value to start a scroll based on the query of this request. 
	 * @param scrollKeepAlive
	 * @return this builder instance
	 * @see PageableCollectionResource#getScrollId()
	 */
	public final B setScroll(String scrollKeepAlive) {
		this.scrollKeepAlive = scrollKeepAlive;
		return getSelf();
	}
	
	/**
	 * Sets the scroll Id to continue a previously started scroll.
	 * @param scrollId
	 * @return
	 * @see PageableCollectionResource#getScrollId()
	 */
	public final B setScrollId(String scrollId) {
		this.scrollId = scrollId;
		return getSelf();
	}
	
	/**
	 * Sets the limit of the result set returned
	 * @param limit of the result set
	 * @return this builder instance
	 */
	public final B setLimit(int limit) {
		this.limit = limit;
		return getSelf();
	}
	
	/**
	 * Filter by resource identifiers.
	 * @param id - a single identifier to match
	 * @return this builder instance
	 */
	public final B filterById(String id) {
		return filterByIds(ImmutableSet.of(id));
	}
	
	/**
	 * Filter by resource identifiers.
	 * @param ids - a {@link Collection} of identifiers to match
	 * @return this builder instance
	 */
	public final B filterByIds(Collection<String> ids) {
		addOption(OptionKey.COMPONENT_IDS, ImmutableSet.copyOf(ids));
		return getSelf();
	}
	
	/**
	 * Sorts the result set by the given sort fields.
	 * 
	 * @param first - the first sort field
	 * @param rest - any remaining sort fields (optional)
	 * @return this builder instance
	 */
	public final B sortBy(SortField first, SortField... rest) {
		return sortBy(Lists.asList(first, rest));
	}

	/**
	 * Sorts the result set by the given sort fields.
	 * 
	 * @param sortFields - the list of fields to sort by, in order
	 * @return this builder instance
	 */
	public final B sortBy(List<SortField> sortFields) {
		addOption(OptionKey.SORT_BY, ImmutableList.copyOf(sortFields));
		return getSelf();
	}
	
	/**
	 * Sets the request to return the entire results set as a single 'page'.
	 * @return this builder instance
	 */
	public final B all() {
		return setScroll(null).setLimit(MAX_LIMIT);
	}
	
	/**
	 * Returns a single hit from the result set.
	 * @return this builder instance
	 */
	public final B one() {
		return setScroll(null).setLimit(1);
	}
	
	// XXX: Does not allow empty-ish values
	protected final B addOption(String key, Object value) {
		if (value instanceof Iterable<?>) {
			for (final Object val : (Iterable<?>)value) {
				if (CompareUtils.isEmpty(val)) {
					throw new BadRequestException("%s filter cannot contain null or empty values", key);
				}
			}
			optionsBuilder.put(key, value);
		} else if (!CompareUtils.isEmpty(value)) {
			optionsBuilder.put(key, value);
		}
		return getSelf();
	}
	
	protected final B addOption(Enum<?> key, Object value) {
		return addOption(key.name(), value);
	}
	
	@Override
	protected ResourceRequest<C, R> create() {
		final SearchResourceRequest<C, R> req = createSearch();
		req.setScrollId(scrollId);
		req.setScrollKeepAlive(scrollKeepAlive);
		req.setSearchAfter(searchAfter);
		req.setLimit(Math.min(limit, MAX_LIMIT));
		req.setOptions(optionsBuilder.build());
		return req;
	}
	
	protected abstract SearchResourceRequest<C, R> createSearch();
}
