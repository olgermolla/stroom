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
 */

package stroom.streamstore.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import stroom.feed.shared.Feed;
import stroom.feed.shared.FeedService;
import stroom.feed.shared.FindFeedCriteria;
import stroom.security.SecurityContext;
import stroom.util.zip.HeaderMap;
import stroom.util.zip.StroomHeaderArguments;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
class DataPermissionServiceFactoryImpl implements DataPermissionServiceFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataPermissionServiceFactoryImpl.class);

    private final FeedService feedService;
    private final SecurityContext securityContext;

    private final Map<String, DataPermissionService> userPermissionCache = new ConcurrentHashMap<>();

    private volatile long lastClear = System.currentTimeMillis();

    @Inject
    DataPermissionServiceFactoryImpl(final FeedService feedService, final SecurityContext securityContext) {
        this.feedService = feedService;
        this.securityContext = securityContext;
    }

    @Override
    public DataPermissionService get() {
        final long now = System.currentTimeMillis();
        if (lastClear < now - 10000) {
            userPermissionCache.clear();
        }

        final String userId = securityContext.getUserId();
        return userPermissionCache.computeIfAbsent(userId, k -> new DataPermissionServiceImpl(feedService));
    }

    // TODO : REPLACE WITH DATA ACCESS POLICY DECISIONS
    private static class DataPermissionServiceImpl implements DataPermissionService {
        private final FeedService feedService;
        private final Map<String, Set<String>> feedPermissionMap = new ConcurrentHashMap<>();

        private DataPermissionServiceImpl(final FeedService feedService) {
            this.feedService = feedService;
        }

        @Override
        public boolean hasPermission(final HeaderMap metaMap, final String permission) {
            final String feed = metaMap.get(StroomHeaderArguments.FEED);
            final Set<String> idSet = feedPermissionMap
                    .computeIfAbsent(permission, k -> getFeedNameSet(permission));
            return idSet.contains(feed);
        }

        private Set<String> getFeedNameSet(final String permission) {
            try {
                final FindFeedCriteria findFeedCriteria = new FindFeedCriteria();
                findFeedCriteria.setRequiredPermission(permission);
                findFeedCriteria.setPageRequest(null);
                final List<Feed> feeds = feedService.find(findFeedCriteria);
                final Set<String> nameSet = feeds.stream().map(Feed::getName).collect(Collectors.toSet());
                return nameSet;
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            return Collections.emptySet();
        }
    }
}
