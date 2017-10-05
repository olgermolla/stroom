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

package stroom.streamstore.server.fs;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import stroom.entity.server.util.EntityServiceLogUtil;
import stroom.entity.server.util.FieldMap;
import stroom.entity.server.util.PreparedStatementUtil;
import stroom.entity.server.util.SqlBuilder;
import stroom.entity.shared.BaseEntity;
import stroom.entity.shared.EntityIdSet;
import stroom.entity.shared.FolderIdSet;
import stroom.entity.shared.PageRequest;
import stroom.feed.shared.Feed;
import stroom.feed.shared.FeedService;
import stroom.feed.shared.FindFeedCriteria;
import stroom.pipeline.shared.PipelineEntity;
import stroom.security.shared.DocumentPermissionNames;
import stroom.streamstore.server.DataPermissionService;
import stroom.streamstore.server.DataPermissionServiceFactory;
import stroom.streamstore.shared.FindStreamCriteria;
import stroom.streamstore.shared.Stream;
import stroom.streamstore.shared.StreamAttributeCondition;
import stroom.streamstore.shared.StreamAttributeConstants;
import stroom.streamstore.shared.StreamAttributeFieldUse;
import stroom.streamstore.shared.StreamAttributeValue;
import stroom.streamstore.shared.StreamType;
import stroom.streamtask.shared.StreamProcessor;
import stroom.util.logging.LogExecutionTime;
import stroom.util.logging.StroomLogger;
import stroom.util.zip.HeaderMap;
import stroom.util.zip.StroomHeaderArguments;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * A file system stream store.
 * </p>
 * <p>
 * <p>
 * Stores streams in the stream store indexed by some meta data.
 * </p>
 */
@Transactional
@Component
public class FileSystemStreamStore2Impl implements FileSystemStreamStore2 {
    private static final String MYSQL_INDEX_STRM_CRT_MS_IDX = "STRM_CRT_MS_IDX";
    private static final String MYSQL_INDEX_STRM_FK_FD_ID_CRT_MS_IDX = "STRM_FK_FD_ID_CRT_MS_IDX";
    private static final String MYSQL_INDEX_STRM_EFFECT_MS_IDX = "STRM_EFFECT_MS_IDX";
    private static final String MYSQL_INDEX_STRM_PARNT_STRM_ID_IDX = "STRM_PARNT_STRM_ID_IDX";
    private static final String MYSQL_INDEX_STRM_FK_STRM_PROC_ID_CRT_MS_IDX = "STRM_FK_STRM_PROC_ID_CRT_MS_IDX";
    private static final StroomLogger LOGGER = StroomLogger.getLogger(FileSystemStreamStore2Impl.class);


    private static final Field ID_FIELD = new LongField(Stream.ID, "S." + Stream.ID, Stream.TABLE_NAME);
    private static final Field FEED_FIELD = new StringField(StroomHeaderArguments.FEED, "F." + Feed.NAME, Feed.TABLE_NAME);
    private static final Field STATUS_FIELD = new StringField(Stream.STATUS, "S." + Stream.STATUS, Stream.TABLE_NAME);
    private static final List<Field> FIELDS = Arrays.asList(ID_FIELD, FEED_FIELD, STATUS_FIELD);

    private static final FieldMap FIELD_MAP = new FieldMap()
            .add(FindStreamCriteria.FIELD_ID, BaseEntity.ID, "id")
            .add(FindStreamCriteria.FIELD_CREATE_MS, Stream.CREATE_MS, "createMs");

    private final FeedService feedService;


    private final DataSource dataSource;
    private final DataPermissionServiceFactory dataPermissionServiceFactory;

    @Inject
    FileSystemStreamStore2Impl(@Named("cachedFeedService") final FeedService feedService,
                               final DataSource dataSource,
                               final DataPermissionServiceFactory dataPermissionServiceFactory) {
        this.feedService = feedService;
        this.dataSource = dataSource;
        this.dataPermissionServiceFactory = dataPermissionServiceFactory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HeaderMap> findMeta(final FindStreamCriteria criteria) {
        return find(criteria, FIELDS);
    }

    private List<HeaderMap> find(final FindStreamCriteria originalCriteria, final List<Field> fields) {
        final boolean relationshipQuery = originalCriteria.getFetchSet().contains(Stream.ENTITY_TYPE);
        final PageRequest pageRequest = originalCriteria.getPageRequest();
        if (relationshipQuery) {
            originalCriteria.setPageRequest(null);
        }

        final LogExecutionTime logExecutionTime = new LogExecutionTime();

        final FindStreamCriteria queryCriteria = new FindStreamCriteria();
        queryCriteria.copyFrom(originalCriteria);

        // Turn all folders in the criteria into feeds.
        convertFoldersToFeeds(queryCriteria, DocumentPermissionNames.READ);

        final DataPermissionService dataPermissionService = dataPermissionServiceFactory.get();

        final SqlBuilder sql = new SqlBuilder();
        rawBuildSQL(queryCriteria, sql, fields);

        final List<HeaderMap> list = new ArrayList<>();

        long index = 0;
        long offset = 0;
        int length = Integer.MAX_VALUE;

        if (pageRequest != null && pageRequest.getOffset() != null && pageRequest.getLength() != null) {
            offset = pageRequest.getOffset();
            length = pageRequest.getLength();
        }

        try (final Connection connection = dataSource.getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
                PreparedStatementUtil.setArguments(preparedStatement, sql.getArgs());
                try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next() && list.size() <= length) {
                        final HeaderMap metaMap = createMetaMap(resultSet, fields);
                        final boolean include = dataPermissionService.hasPermission(metaMap, DocumentPermissionNames.READ);
                        if (include) {
                            if (index >= offset) {
                                list.add(metaMap);
                            }

                            index++;
                        }
                    }
                }
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        EntityServiceLogUtil.logQuery(LOGGER, "findMeta()", logExecutionTime, list, sql);

        return list;
    }

    private HeaderMap createMetaMap(final ResultSet resultSet, final List<Field> fields) throws SQLException {
        final HeaderMap metaMap = new HeaderMap();
        int index = 1;
        for (final Field field : fields) {
            metaMap.put(field.getName(), field.getValue(resultSet, index));
            index++;
        }
        return metaMap;
    }

    private void convertFoldersToFeeds(final FindStreamCriteria findStreamCriteria, final String requiredPermission) {
        final FolderIdSet folderIdSet = findStreamCriteria.getFolderIdSet();
        if (folderIdSet != null) {
            if (folderIdSet.isConstrained()) {
                final FindFeedCriteria findFeedCriteria = new FindFeedCriteria();
                findFeedCriteria.setRequiredPermission(requiredPermission);
                findFeedCriteria.setPageRequest(null);
                findFeedCriteria.getFolderIdSet().copyFrom(folderIdSet);
                final List<Feed> folderFeeds = feedService.find(findFeedCriteria);

                // If the user is filtering by feed then make sure they can read all of the feeds that they are filtering by.
                final EntityIdSet<Feed> feeds = findStreamCriteria.obtainFeeds().obtainInclude();

                // Ensure a user cannot match all feeds.
                feeds.setMatchAll(Boolean.FALSE);
                folderFeeds.forEach(feed -> feeds.add(feed.getId()));
            }

            findStreamCriteria.setFolderIdSet(null);
        }
    }

    @SuppressWarnings("incomplete-switch")
    private void rawBuildSQL(final FindStreamCriteria criteria, final SqlBuilder sql, final List<Field> fields) {
        sql.append("SELECT ");
        sql.append(fields.stream().map(Field::getSql).collect(Collectors.joining(",")));
        sql.append(" FROM ");
        sql.append(Stream.TABLE_NAME);
        sql.append(" S");

        appendJoin(criteria, sql);

        sql.append(" WHERE 1=1");

        appendStreamCriteria(criteria, sql);

        // Append order by criteria.
        sql.appendOrderBy(FIELD_MAP.getSqlFieldMap(), criteria, "S");
    }

    private void appendJoin(final FindStreamCriteria criteria, final SqlBuilder sql) {
        String indexToUse = null;

        // Here we try and better second guess a index to use for MYSQL
        boolean chooseIndex = true;

        // Any Key by stream id MySQL will pick the stream id index
        if (criteria.getStreamIdSet() != null && criteria.getStreamIdSet().isConstrained()) {
            chooseIndex = false;
        }
        if (criteria.getStreamIdRange() != null && criteria.getStreamIdRange().isConstrained()) {
            chooseIndex = false;
        }
        if (criteria.getParentStreamIdSet() != null && criteria.getParentStreamIdSet().isConstrained()) {
            chooseIndex = false;
        }

        if (chooseIndex && criteria.getPipelineIdSet() != null && criteria.getPipelineIdSet().getSet().size() == 1) {
            chooseIndex = false;
            indexToUse = MYSQL_INDEX_STRM_FK_STRM_PROC_ID_CRT_MS_IDX;
        }

        if (chooseIndex && criteria.getFeeds() != null && criteria.getFeeds().getInclude() != null
                && criteria.getFeeds().getInclude().getSet().size() == 1) {
            chooseIndex = false;
            indexToUse = MYSQL_INDEX_STRM_FK_FD_ID_CRT_MS_IDX;
        }

        if (chooseIndex && criteria.getFeeds() != null && criteria.getFeeds().getExclude() != null
                && criteria.getFeeds().getExclude().getSet().size() == 1) {
            chooseIndex = false;
            indexToUse = MYSQL_INDEX_STRM_FK_FD_ID_CRT_MS_IDX;
        }

        if (chooseIndex) {
            chooseIndex = false;
            indexToUse = MYSQL_INDEX_STRM_CRT_MS_IDX;
        }

        if (indexToUse != null) {
            sql.append(" USE INDEX (");
            sql.append(indexToUse);
            sql.append(")");
        }

//        if (criteria.getAttributeConditionList() != null) {
//            for (int i = 0; i < criteria.getAttributeConditionList().size(); i++) {
//                sql.append(" JOIN ");
//                sql.append(StreamAttributeValue.TABLE_NAME);
//                sql.append(" SAV");
//                sql.append(i, false);
//                sql.append(" ON (S.");
//                sql.append(Stream.ID);
//                sql.append(" = SAV");
//                sql.append(i, false);
//                sql.append(".");
//                sql.append(StreamAttributeValue.STREAM_ID);
//                sql.append(" AND SAV");
//                sql.append(i, false);
//                sql.append(".");
//                sql.append(StreamAttributeValue.STREAM_ATTRIBUTE_KEY_ID);
//                sql.append(" = ");
//                sql.arg(criteria.getAttributeConditionList().get(i).getStreamAttributeKey().getId());
//                sql.append(")");
//            }
//        }

        appendFeedJoin(criteria, sql);
        appendStreamProcessorJoin(criteria, sql);
    }

    private void appendFeedJoin(final FindStreamCriteria queryCriteria, final SqlBuilder sql) {
        sql.append(" JOIN ");
        sql.append(Feed.TABLE_NAME);
        sql.append(" F ON (F.");
        sql.append(Feed.ID);
        sql.append(" = S.");
        sql.append(Feed.FOREIGN_KEY);
        sql.append(")");
    }

    private void appendStreamProcessorJoin(final FindStreamCriteria queryCriteria, final SqlBuilder sql) {
        if (queryCriteria.getPipelineIdSet() != null && queryCriteria.getPipelineIdSet().isConstrained()) {
            sql.append(" JOIN ");
            sql.append(StreamProcessor.TABLE_NAME);
            sql.append(" SP ON (SP.");
            sql.append(StreamProcessor.ID);
            sql.append(" = S.");
            sql.append(StreamProcessor.FOREIGN_KEY);
            sql.append(")");
        }
    }

    private void appendStreamCriteria(final FindStreamCriteria criteria, final SqlBuilder sql) {
        if (criteria.getAttributeConditionList() != null) {
            for (int i = 0; i < criteria.getAttributeConditionList().size(); i++) {
                final StreamAttributeCondition condition = criteria.getAttributeConditionList().get(i);
                final StreamAttributeFieldUse use = StreamAttributeConstants.SYSTEM_ATTRIBUTE_FIELD_TYPE_MAP
                        .get(condition.getStreamAttributeKey().getName());
                if (use != null) {
                    final Object[] values = getValues(use, condition);

                    if (values != null && values.length > 0) {
                        final boolean toLong = use.isNumeric();
                        String field;
                        if (toLong) {
                            field = "SAV" + i + "." + StreamAttributeValue.VALUE_NUMBER;
                        } else {
                            field = "SAV" + i + "." + StreamAttributeValue.VALUE_STRING;
                        }

                        sql.append(" AND ");
                        switch (condition.getCondition()) {
                            case CONTAINS:
                                sql.append(field);
                                sql.append(" LIKE ");
                                sql.arg(values[0]);
                                break;
                            case EQUALS:
                                sql.append(field);
                                sql.append(" = ");
                                sql.arg(values[0]);
                                break;
                            case GREATER_THAN:
                                sql.append(field);
                                sql.append(" > ");
                                sql.arg(values[0]);
                                break;
                            case GREATER_THAN_OR_EQUAL_TO:
                                sql.append(field);
                                sql.append(" >= ");
                                sql.arg(values[0]);
                                break;

                            case LESS_THAN:
                                sql.append(field);
                                sql.append(" < ");
                                sql.arg(values[0]);
                                break;

                            case LESS_THAN_OR_EQUAL_TO:
                                sql.append(field);
                                sql.append(" <= ");
                                sql.arg(values[0]);
                                break;

                            case BETWEEN:
                                sql.append(field);
                                sql.append(" >= ");
                                sql.arg(values[0]);

                                if (values.length > 1) {
                                    sql.append(" AND ");
                                    sql.append(field);
                                    sql.append(" <= ");
                                    sql.arg(values[1]);
                                }
                                break;
                        }
                    }
                }
            }
        }

        sql.appendRangeQuery("S." + Stream.CREATE_MS, criteria.getCreatePeriod());

        sql.appendRangeQuery("S." + Stream.EFFECTIVE_MS, criteria.getEffectivePeriod());

        sql.appendRangeQuery("S." + Stream.STATUS_MS, criteria.getStatusPeriod());

        sql.appendRangeQuery("S." + Stream.ID, criteria.getStreamIdRange());

        sql.appendEntityIdSetQuery("S." + Stream.ID, criteria.getStreamIdSet());

        sql.appendPrimitiveValueSetQuery("S." + Stream.STATUS, criteria.getStatusSet());

        sql.appendEntityIdSetQuery("S." + Stream.PARENT_STREAM_ID, criteria.getParentStreamIdSet());
        sql.appendEntityIdSetQuery("S." + StreamType.FOREIGN_KEY, criteria.getStreamTypeIdSet());
        sql.appendIncludeExcludeSetQuery("S." + Feed.FOREIGN_KEY, criteria.getFeeds());

        sql.appendEntityIdSetQuery("SP." + PipelineEntity.FOREIGN_KEY, criteria.getPipelineIdSet());
        sql.appendEntityIdSetQuery("S." + StreamProcessor.FOREIGN_KEY, criteria.getStreamProcessorIdSet());
    }

    private Object[] getValues(final StreamAttributeFieldUse use, final StreamAttributeCondition condition) {
        Object[] values = null;

        final boolean toLong = use.isNumeric();
        if (condition.getFieldValue() != null) {
            final String[] parts = condition.getFieldValue().split(",");
            values = new Object[parts.length];
            for (int i = 0; i < parts.length; i++) {
                if (toLong) {
                    try {
                        values[i] = Long.parseLong(parts[i]);
                    } catch (final NumberFormatException e) {
                        // Ignore
                    }
                } else {
                    values[i] = parts[i];
                }
            }
        }
        return values;
    }

    private static abstract class Field {
        private final String name;
        private final String sql;
        private final String table;

        Field(final String name, final String sql, final String table) {
            this.name = name;
            this.sql = sql;
            this.table = table;
        }

        String getName() {
            return name;
        }

        String getSql() {
            return sql;
        }

        String getTable() {
            return table;
        }

        abstract String getValue(ResultSet resultSet, int index) throws SQLException;
    }

    private static class StringField extends Field {
        StringField(final String name, final String sql, final String table) {
            super(name, sql, table);
        }

        @Override
        String getValue(final ResultSet resultSet, final int index) throws SQLException {
            return resultSet.getString(index);
        }
    }

    private static class LongField extends Field {
        LongField(final String name, final String sql, final String table) {
            super(name, sql, table);
        }

        @Override
        String getValue(final ResultSet resultSet, final int index) throws SQLException {
            return String.valueOf(resultSet.getLong(index));
        }
    }

    private static class IntegerField extends Field {
        IntegerField(final String name, final String sql, final String table) {
            super(name, sql, table);
        }

        @Override
        String getValue(final ResultSet resultSet, final int index) throws SQLException {
            return String.valueOf(resultSet.getInt(index));
        }
    }

    private static class BooleanField extends Field {
        BooleanField(final String name, final String sql, final String table) {
            super(name, sql, table);
        }

        @Override
        String getValue(final ResultSet resultSet, final int index) throws SQLException {
            return String.valueOf(resultSet.getBoolean(index));
        }
    }
}
