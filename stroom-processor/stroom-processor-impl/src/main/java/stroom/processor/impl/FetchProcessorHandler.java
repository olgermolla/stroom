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

package stroom.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.dictionary.api.DictionaryStore;
import stroom.dictionary.shared.DictionaryDoc;
import stroom.docref.DocRef;
import stroom.docref.SharedObject;
import stroom.docstore.shared.DocRefUtil;
import stroom.pipeline.PipelineStore;
import stroom.pipeline.shared.PipelineDoc;
import stroom.processor.api.ProcessorFilterService;
import stroom.processor.api.ProcessorService;
import stroom.processor.shared.FetchProcessorAction;
import stroom.processor.shared.FindProcessorCriteria;
import stroom.processor.shared.FindProcessorFilterCriteria;
import stroom.processor.shared.Processor;
import stroom.processor.shared.ProcessorFilter;
import stroom.processor.shared.ProcessorFilterRow;
import stroom.processor.shared.ProcessorRow;
import stroom.processor.shared.QueryData;
import stroom.query.api.v2.ExpressionItem;
import stroom.query.api.v2.ExpressionOperator;
import stroom.query.api.v2.ExpressionOperator.Builder;
import stroom.query.api.v2.ExpressionTerm;
import stroom.security.api.Security;
import stroom.security.api.SecurityContext;
import stroom.security.shared.PermissionNames;
import stroom.task.api.AbstractTaskHandler;
import stroom.util.shared.BaseResultList;
import stroom.util.shared.Expander;
import stroom.util.shared.ResultList;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FetchProcessorHandler extends AbstractTaskHandler<FetchProcessorAction, ResultList<SharedObject>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchProcessorHandler.class);

    private final ProcessorFilterService processorFilterService;
    private final ProcessorService streamProcessorService;
    private final SecurityContext securityContext;
    private final DictionaryStore dictionaryStore;
    private final PipelineStore pipelineStore;
    private final Security security;

    @Inject
    FetchProcessorHandler(final ProcessorFilterService processorFilterService,
                          final ProcessorService streamProcessorService,
                          final SecurityContext securityContext,
                          final DictionaryStore dictionaryStore,
                          final PipelineStore pipelineStore,
                          final Security security) {
        this.processorFilterService = processorFilterService;
        this.streamProcessorService = streamProcessorService;
        this.securityContext = securityContext;
        this.dictionaryStore = dictionaryStore;
        this.pipelineStore = pipelineStore;
        this.security = security;
    }

    @Override
    public ResultList<SharedObject> exec(final FetchProcessorAction action) {
        return security.secureResult(PermissionNames.MANAGE_PROCESSORS_PERMISSION, () -> {
            final List<SharedObject> values = new ArrayList<>();

            final FindProcessorFilterCriteria criteria = new FindProcessorFilterCriteria();
            final FindProcessorCriteria criteriaRoot = new FindProcessorCriteria();
            if (action.getPipeline() != null) {
                criteria.obtainPipelineUuidCriteria().setString(action.getPipeline().getUuid());
                criteriaRoot.obtainPipelineUuidCriteria().setString(action.getPipeline().getUuid());
            }

            // If the user is not an admin then only show them filters that were created by them.
            if (!securityContext.isAdmin()) {
                criteria.setCreateUser(securityContext.getUserId());
            }

            final BaseResultList<Processor> streamProcessors = streamProcessorService.find(criteriaRoot);

            final BaseResultList<ProcessorFilter> processorFilters = processorFilterService
                    .find(criteria);

            // Get unique processors.
            final Set<Processor> processors = new HashSet<>(streamProcessors);

            final List<Processor> sorted = new ArrayList<>(processors);
            sorted.sort((o1, o2) -> {
                if (o1.getPipelineUuid() != null && o2.getPipelineUuid() != null) {
                    return o1.getPipelineUuid().compareTo(o2.getPipelineUuid());
                }
                if (o1.getPipelineUuid() != null) {
                    return -1;
                }
                if (o2.getPipelineUuid() != null) {
                    return 1;
                }
                return o1.getId().compareTo(o2.getId());
            });

            for (final Processor processor : sorted) {
                final Expander processorExpander = new Expander(0, false, false);
                final ProcessorRow processorRow = new ProcessorRow(processorExpander,
                        processor);
                values.add(processorRow);

                // If the job row is open then add child rows.
                if (action.getExpandedRows() == null || action.isRowExpanded(processorRow)) {
                    processorExpander.setExpanded(true);

                    // Add filters.
                    for (final ProcessorFilter processorFilter : processorFilters) {
                        if (processor.equals(processorFilter.getProcessor())) {
                            // Decorate the expression with resolved dictionaries etc.
                            final QueryData queryData = processorFilter.getQueryData();
                            if (queryData != null && queryData.getExpression() != null) {
                                queryData.setExpression(decorate(queryData.getExpression()));
                            }

                            final ProcessorFilterRow processorFilterRow = new ProcessorFilterRow(processorFilter);
                            values.add(processorFilterRow);
                        }
                    }
                }
            }

            return BaseResultList.createUnboundedList(values);
        });
    }

    private ExpressionOperator decorate(final ExpressionOperator operator) {
        final ExpressionOperator.Builder builder = new Builder()
                .op(operator.getOp())
                .enabled(operator.getEnabled());

        if (operator.getChildren() != null) {
            for (final ExpressionItem child : operator.getChildren()) {
                if (child instanceof ExpressionOperator) {
                    builder.addOperator(decorate((ExpressionOperator) child));

                } else if (child instanceof ExpressionTerm) {
                    ExpressionTerm term = (ExpressionTerm) child;
                    DocRef dictionary = term.getDictionary();
                    DocRef docRef = term.getDocRef();

                    if (dictionary != null) {
                        try {
                            final DictionaryDoc dictionaryDoc = dictionaryStore.readDocument(term.getDictionary());
                            dictionary = DocRefUtil.create(dictionaryDoc);
                        } catch (final RuntimeException e) {
                            LOGGER.debug(e.getMessage(), e);
                        }

                        term = new ExpressionTerm.Builder()
                                .enabled(term.getEnabled())
                                .field(term.getField())
                                .condition(term.getCondition())
                                .value(term.getValue())
                                .dictionary(dictionary)
                                .build();
                    } else if (docRef != null && PipelineDoc.DOCUMENT_TYPE.equals(docRef.getType())) {
                        try {
                            final PipelineDoc pipelineDoc = pipelineStore.readDocument(docRef);
                            docRef = DocRefUtil.create(pipelineDoc);
                        } catch (final RuntimeException e) {
                            LOGGER.debug(e.getMessage(), e);
                        }

                        term = new ExpressionTerm.Builder()
                                .enabled(term.getEnabled())
                                .field(term.getField())
                                .condition(term.getCondition())
                                .value(term.getValue())
                                .docRef(docRef)
                                .build();
                    }

                    builder.addTerm(term);
                }
            }
        }

        return builder.build();
    }
}
