/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package stroom.meta.impl.db;

import stroom.db.util.JooqUtil;
import stroom.meta.impl.MetaFeedDao;
import stroom.meta.impl.db.jooq.tables.records.MetaFeedRecord;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static stroom.meta.impl.db.jooq.tables.MetaFeed.META_FEED;

@Singleton
class MetaFeedDaoImpl implements MetaFeedDao {
    // TODO : @66 Replace with a proper cache.
    private final Map<String, Integer> cache = new ConcurrentHashMap<>();

    private final ConnectionProvider connectionProvider;

    @Inject
    MetaFeedDaoImpl(final ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Integer getOrCreate(final String name) {
        // Try and get the id from the cache.
        return Optional.ofNullable(cache.get(name))
                .or(() -> {
                    // Try and get the existing id from the DB.
                    return get(name)
                            .or(() -> {
                                // The id isn't in the DB so create it.
                                return create(name)
                                        .or(() -> {
                                            // If the id is still null then this may be because the create method failed
                                            // due to the name having been inserted into the DB by another thread prior
                                            // to us calling create and the DB preventing duplicate names.
                                            // Assuming this is the case, try and get the id from the DB one last time.
                                            return get(name);
                                        });
                            })
                            .map(i -> {
                                // Cache for next time.
                                cache.put(name, i);
                                return i;
                            });
                }).orElseThrow();
    }

    Optional<Integer> get(final String name) {
        return JooqUtil.contextResult(connectionProvider, context -> context
                .select(META_FEED.ID)
                .from(META_FEED)
                .where(META_FEED.NAME.eq(name))
                .fetchOptional(META_FEED.ID));
    }

    Optional<Integer> create(final String name) {
        return JooqUtil.contextResult(connectionProvider, context -> context
                .insertInto(META_FEED, META_FEED.NAME)
                .values(name)
                .onDuplicateKeyIgnore()
                .returning(META_FEED.ID)
                .fetchOptional()
                .map(MetaFeedRecord::getId));
    }

    @Override
    public List<String> list() {
        return JooqUtil.contextResult(connectionProvider, context -> context
                .select(META_FEED.NAME)
                .from(META_FEED)
                .fetch(META_FEED.NAME));
    }

    @Override
    public void clear() {
        deleteAll();
        cache.clear();
    }

    private int deleteAll() {
        return JooqUtil.contextResult(connectionProvider, context -> context
                .delete(META_FEED)
                .execute());
    }
}
