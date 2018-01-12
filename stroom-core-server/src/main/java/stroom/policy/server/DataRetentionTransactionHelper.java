/*
 * Copyright 2018 Crown Copyright
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

package stroom.policy.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import stroom.dictionary.shared.DictionaryService;
import stroom.entity.server.util.PreparedStatementUtil;
import stroom.entity.server.util.SqlBuilder;
import stroom.entity.shared.Period;
import stroom.entity.shared.Range;
import stroom.feed.shared.Feed;
import stroom.pipeline.shared.PipelineEntity;
import stroom.policy.server.DataRetentionExecutor.ActiveRules;
import stroom.policy.server.DataRetentionExecutor.Progress;
import stroom.policy.shared.DataRetentionRule;
import stroom.query.shared.IndexField;
import stroom.streamstore.server.ExpressionMatcher;
import stroom.streamstore.server.StreamFields;
import stroom.streamstore.server.fs.FileSystemStreamStore;
import stroom.streamstore.shared.Stream;
import stroom.streamstore.shared.StreamType;
import stroom.streamtask.shared.StreamProcessor;
import stroom.util.task.TaskMonitor;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class DataRetentionTransactionHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataRetentionTransactionHelper.class);

    private final DataSource dataSource;
    private final FileSystemStreamStore fileSystemStreamStore;
    private final DictionaryService dictionaryService;

    @Inject
    public DataRetentionTransactionHelper(final DataSource dataSource, final FileSystemStreamStore fileSystemStreamStore, final DictionaryService dictionaryService) {
        this.dataSource = dataSource;
        this.fileSystemStreamStore = fileSystemStreamStore;
        this.dictionaryService = dictionaryService;
    }

    @Transactional(propagation = Propagation.NEVER, readOnly = true)
    public long getRowCount(final Period ageRange, final Set<String> fieldSet) {
        long rowCount = 0;
        final SqlBuilder sql = getSql(ageRange, null, fieldSet, true, null);
        try (final Connection connection = dataSource.getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
                PreparedStatementUtil.setArguments(preparedStatement, sql.getArgs());

                try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        rowCount = resultSet.getLong(1);
                    }
                }
            }
        } catch (final SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return rowCount;
    }

    @Transactional(propagation = Propagation.NEVER, readOnly = true)
    public boolean deleteMatching(final Period ageRange, final Range<Long> streamIdRange, final long batchSize, final ActiveRules activeRules, final Map<DataRetentionRule, Optional<Long>> ageMap, final TaskMonitor taskMonitor, final Progress progress) {
        boolean more = false;

        final ExpressionMatcher expressionMatcher = new ExpressionMatcher(StreamFields.getFieldMap(), dictionaryService);

        final SqlBuilder sql = getSql(ageRange, streamIdRange, activeRules.getFieldSet(), false, batchSize);
        try (final Connection connection = dataSource.getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
                PreparedStatementUtil.setArguments(preparedStatement, sql.getArgs());

                try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        final Map<String, Object> attributeMap = createAttributeMap(resultSet, activeRules.getFieldSet());
                        final Long streamId = (Long) attributeMap.get(StreamFields.STREAM_ID);
                        final Long createMs = (Long) attributeMap.get(StreamFields.CREATED_ON);
                        try {
                            more = true;
                            progress.nextStream(streamId, createMs);
                            final String streamInfo = progress.toString();
                            info(taskMonitor, "Examining stream " + streamInfo);

                            final DataRetentionRule matchingRule = findMatchingRule(expressionMatcher, attributeMap, activeRules.getActiveRules());
                            if (matchingRule != null) {
                                ageMap.get(matchingRule).ifPresent(age -> {
                                    if (createMs < age) {
                                        info(taskMonitor, "Deleting stream " + streamInfo);
                                        fileSystemStreamStore.deleteStream(Stream.createStub(streamId));
                                    }
                                });
                            }
                        } catch (final Exception e) {
                            LOGGER.error("An error occurred processing stream " + streamId, e);
                        }
                    }
                }
            }
        } catch (final SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return more;
    }

    private void info(final TaskMonitor taskMonitor, final String message) {
        LOGGER.debug(message);
        taskMonitor.info(message);
    }

    private SqlBuilder getSql(final Period ageRange, final Range<Long> streamIdRange, final Set<String> fieldSet, final boolean count, final Long limit) {
        final SqlBuilder sql = new SqlBuilder();
        sql.append("SELECT");

        final boolean includeStream = addFieldsToQuery(StreamFields.getStreamFields(), fieldSet, sql, "S");
        final boolean includeFeed = addFieldsToQuery(StreamFields.getFeedFields(), fieldSet, sql, "F");
        final boolean includeStreamType = addFieldsToQuery(StreamFields.getStreamTypeFields(), fieldSet, sql, "ST");
        final boolean includePipeline = addFieldsToQuery(StreamFields.getPipelineFields(), fieldSet, sql, "P");

        if (count) {
            sql.setLength(0);
            sql.append("SELECT COUNT(*)");
        } else {
            // Remove last comma from field list.
            sql.setLength(sql.length() - 1);
        }

        sql.append(" FROM ");
        sql.append(Stream.TABLE_NAME);
        sql.append(" S");

        if (includeFeed) {
            sql.join(Feed.TABLE_NAME, "F", "S", Feed.FOREIGN_KEY, "F", Feed.ID);
        }
        if (includeStreamType) {
            sql.join(StreamType.TABLE_NAME, "ST", "S", StreamType.FOREIGN_KEY, "ST", StreamType.ID);
        }
        if (includePipeline) {
            sql.leftOuterJoin(StreamProcessor.TABLE_NAME, "SP", "S", StreamProcessor.FOREIGN_KEY, "SP", StreamProcessor.ID);
            sql.leftOuterJoin(PipelineEntity.TABLE_NAME, "p", "SP", PipelineEntity.FOREIGN_KEY, "p", PipelineEntity.ID);
        }

        sql.append(" WHERE 1=1");
        sql.appendRangeQuery("S." + Stream.CREATE_MS, ageRange);
        sql.appendRangeQuery("S." + Stream.ID, streamIdRange);
        sql.append(" ORDER BY S." + Stream.ID);
        if (limit != null) {
            sql.append(" LIMIT " + limit);
        }

        return sql;
    }

    private DataRetentionRule findMatchingRule(final ExpressionMatcher expressionMatcher, final Map<String, Object> attributeMap, final List<DataRetentionRule> activeRules) {
        for (final DataRetentionRule rule : activeRules) {
            if (expressionMatcher.match(attributeMap, rule.getExpression())) {
                return rule;
            }
        }
        return null;
    }

    private Map<String, Object> createAttributeMap(final ResultSet resultSet, final Set<String> fieldSet) {
        final Map<String, Object> attributeMap = new HashMap<>();
        fieldSet.forEach(fieldName -> {
            try {
                final IndexField indexField = StreamFields.getFieldMap().get(fieldName);
                switch (indexField.getFieldType()) {
                    case FIELD:
                        final String string = resultSet.getString(fieldName);
                        attributeMap.put(fieldName, string);
                        break;
                    default:
                        final long number = resultSet.getLong(fieldName);
                        attributeMap.put(fieldName, number);
                        break;

                }
            } catch (final SQLException e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
        return attributeMap;
    }

    private boolean addFieldsToQuery(final Map<String, String> fieldMap, final Set<String> fieldSet, final SqlBuilder sql, final String alias) {
        final AtomicBoolean used = new AtomicBoolean();

        fieldMap.forEach((k, v) -> {
            if (fieldSet.contains(k)) {
                sql.append(" ");
                sql.append(alias);
                sql.append(".");
                sql.append(v);
                sql.append(" AS ");
                sql.append("'" + k + "'");
                sql.append(",");

                used.set(true);
            }
        });

        return used.get();
    }


}