package stroom.processor.impl.db;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.Record;
import stroom.db.util.GenericDao;
import stroom.db.util.JooqUtil;
import stroom.processor.impl.ProcessorFilterDao;
import stroom.processor.impl.db.jooq.tables.records.ProcessorFilterRecord;
import stroom.processor.impl.db.jooq.tables.records.ProcessorFilterTrackerRecord;
import stroom.processor.shared.FindProcessorFilterCriteria;
import stroom.processor.shared.Processor;
import stroom.processor.shared.ProcessorFilter;
import stroom.processor.shared.ProcessorFilterTracker;
import stroom.util.logging.LambdaLogUtil;
import stroom.util.logging.LambdaLogger;
import stroom.util.logging.LambdaLoggerFactory;
import stroom.util.shared.BaseResultList;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static stroom.processor.impl.db.jooq.tables.Processor.PROCESSOR;
import static stroom.processor.impl.db.jooq.tables.ProcessorFilter.PROCESSOR_FILTER;
import static stroom.processor.impl.db.jooq.tables.ProcessorFilterTracker.PROCESSOR_FILTER_TRACKER;

class ProcessorFilterDaoImpl implements ProcessorFilterDao {
    private static final LambdaLogger LAMBDA_LOGGER = LambdaLoggerFactory.getLogger(ProcessorFilterDaoImpl.class);

    private static final Map<String, Field> FIELD_MAP = Map.of(
            FindProcessorFilterCriteria.FIELD_ID, PROCESSOR_FILTER.ID);

    private static final Function<Record, Processor> RECORD_TO_PROCESSOR_MAPPER = new RecordToProcessorMapper();
    private static final Function<Record, ProcessorFilter> RECORD_TO_PROCESSOR_FILTER_MAPPER = new RecordToProcessorFilterMapper();
    private static final Function<Record, ProcessorFilterTracker> RECORD_TO_PROCESSOR_FILTER_TRACKER_MAPPER = new RecordToProcessorFilterTrackerMapper();

    private final ConnectionProvider connectionProvider;
    private final ProcessorFilterMarshaller marshaller;
    private final GenericDao<ProcessorFilterRecord, ProcessorFilter, Integer> genericDao;

    @Inject
    ProcessorFilterDaoImpl(final ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        this.marshaller = new ProcessorFilterMarshaller();
        this.genericDao = new GenericDao<>(PROCESSOR_FILTER, PROCESSOR_FILTER.ID, ProcessorFilter.class, connectionProvider);
    }

    @Override
    public ProcessorFilter create(final ProcessorFilter processorFilter) {
        LAMBDA_LOGGER.debug(LambdaLogUtil.message("Creating a {}", PROCESSOR_FILTER.getName()));

        final ProcessorFilter marshalled = marshaller.marshal(processorFilter);
        return marshaller.unmarshal(JooqUtil.transactionResult(connectionProvider, context -> {
            final ProcessorFilterTrackerRecord processorFilterTrackerRecord = context.newRecord(PROCESSOR_FILTER_TRACKER, new ProcessorFilterTracker());
            processorFilterTrackerRecord.store();
            final ProcessorFilterTracker processorFilterTracker = processorFilterTrackerRecord.into(ProcessorFilterTracker.class);

            marshalled.setProcessorFilterTracker(processorFilterTracker);

            final ProcessorFilterRecord processorFilterRecord = context.newRecord(PROCESSOR_FILTER, marshalled);

            processorFilterRecord.setFkProcessorFilterTrackerId(marshalled.getProcessorFilterTracker().getId());
            processorFilterRecord.setFkProcessorId(marshalled.getProcessor().getId());
            processorFilterRecord.store();

            final ProcessorFilter result = processorFilterRecord.into(ProcessorFilter.class);
            result.setProcessorFilterTracker(result.getProcessorFilterTracker());
            result.setProcessor(result.getProcessor());

            return result;
        }));
    }

    @Override
    public ProcessorFilter update(final ProcessorFilter processorFilter) {
        final ProcessorFilter marshalled = marshaller.marshal(processorFilter);
        return marshaller.unmarshal(JooqUtil.contextResultWithOptimisticLocking(connectionProvider, context -> {
            final ProcessorFilterRecord processorFilterRecord =
                    context.newRecord(PROCESSOR_FILTER, marshalled);

            processorFilterRecord.setFkProcessorFilterTrackerId(marshalled.getProcessorFilterTracker().getId());
            processorFilterRecord.setFkProcessorId(marshalled.getProcessor().getId());
            processorFilterRecord.store();

            final ProcessorFilter result = processorFilterRecord.into(ProcessorFilter.class);
            result.setProcessorFilterTracker(marshalled.getProcessorFilterTracker());
            result.setProcessor(marshalled.getProcessor());

            return result;
        }));
    }

    @Override
    public boolean delete(final int id) {
        return genericDao.delete(id);
    }

    @Override
    public Optional<ProcessorFilter> fetch(final int id) {
        return JooqUtil.contextResult(connectionProvider, context ->
                context
                        .select()
                        .from(PROCESSOR_FILTER)
                        .join(PROCESSOR_FILTER_TRACKER).on(PROCESSOR_FILTER.FK_PROCESSOR_FILTER_TRACKER_ID.eq(PROCESSOR_FILTER_TRACKER.ID))
                        .join(PROCESSOR).on(PROCESSOR_FILTER.FK_PROCESSOR_ID.eq(PROCESSOR.ID))
                        .where(PROCESSOR_FILTER.ID.eq(id))
                        .fetchOptional()
                        .map(record -> {
                            final Processor processor = RECORD_TO_PROCESSOR_MAPPER.apply(record);
                            final ProcessorFilter processorFilter = RECORD_TO_PROCESSOR_FILTER_MAPPER.apply(record);
                            final ProcessorFilterTracker processorFilterTracker = RECORD_TO_PROCESSOR_FILTER_TRACKER_MAPPER.apply(record);

                            processorFilter.setProcessor(processor);
                            processorFilter.setProcessorFilterTracker(processorFilterTracker);

                            return marshaller.unmarshal(processorFilter);
                        }));
    }

    @Override
    public BaseResultList<ProcessorFilter> find(final FindProcessorFilterCriteria criteria) {
        return JooqUtil.contextResult(connectionProvider, context -> find(context, criteria));
    }

    private BaseResultList<ProcessorFilter> find(final DSLContext context, final FindProcessorFilterCriteria criteria) {
        final Collection<Condition> conditions = convertCriteria(criteria);

        final OrderField[] orderFields = JooqUtil.getOrderFields(FIELD_MAP, criteria);

        final List<ProcessorFilter> list = context
                .select()
                .from(PROCESSOR_FILTER)
                .join(PROCESSOR_FILTER_TRACKER).on(PROCESSOR_FILTER.FK_PROCESSOR_FILTER_TRACKER_ID.eq(PROCESSOR_FILTER_TRACKER.ID))
                .join(PROCESSOR).on(PROCESSOR_FILTER.FK_PROCESSOR_ID.eq(PROCESSOR.ID))
                .where(conditions)
                .orderBy(orderFields)
                .fetch()
                .map(record -> {
                    final Processor processor = RECORD_TO_PROCESSOR_MAPPER.apply(record);
                    final ProcessorFilter processorFilter = RECORD_TO_PROCESSOR_FILTER_MAPPER.apply(record);
                    final ProcessorFilterTracker processorFilterTracker = RECORD_TO_PROCESSOR_FILTER_TRACKER_MAPPER.apply(record);

                    processorFilter.setProcessor(processor);
                    processorFilter.setProcessorFilterTracker(processorFilterTracker);

                    return marshaller.unmarshal(processorFilter);
                });

        return BaseResultList.createCriterialBasedList(list, criteria);
    }

    private Collection<Condition> convertCriteria(final FindProcessorFilterCriteria criteria) {
        return JooqUtil.conditions(
                JooqUtil.getRangeCondition(PROCESSOR_FILTER.PRIORITY, criteria.getPriorityRange()),
                JooqUtil.getRangeCondition(PROCESSOR_FILTER_TRACKER.LAST_POLL_MS, criteria.getLastPollPeriod()),
                JooqUtil.getSetCondition(PROCESSOR_FILTER.FK_PROCESSOR_ID, criteria.getProcessorIdSet()),
                JooqUtil.getStringCondition(PROCESSOR.PIPELINE_UUID, criteria.getPipelineUuidCriteria()),
                Optional.ofNullable(criteria.getProcessorEnabled()).map(PROCESSOR.ENABLED::eq),
                Optional.ofNullable(criteria.getProcessorFilterEnabled()).map(PROCESSOR_FILTER.ENABLED::eq),
                Optional.ofNullable(criteria.getCreateUser()).map(PROCESSOR_FILTER.CREATE_USER::eq));
    }
}
