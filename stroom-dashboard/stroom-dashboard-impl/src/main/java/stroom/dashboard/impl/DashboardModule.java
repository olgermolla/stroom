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

package stroom.dashboard.impl;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import stroom.dashboard.shared.DashboardDoc;
import stroom.dashboard.shared.DownloadQueryAction;
import stroom.dashboard.shared.DownloadSearchResultsAction;
import stroom.dashboard.shared.FetchTimeZonesAction;
import stroom.dashboard.shared.FetchVisualisationAction;
import stroom.dashboard.shared.SearchBusPollAction;
import stroom.dashboard.shared.ValidateExpressionAction;
import stroom.explorer.api.ExplorerActionHandler;
import stroom.importexport.api.ImportExportActionHandler;
import stroom.task.api.TaskHandlerBinder;
import stroom.util.guice.GuiceUtil;
import stroom.docstore.api.DocumentActionHandlerBinder;
import stroom.util.shared.Clearable;

public class DashboardModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DashboardStore.class).to(DashboardStoreImpl.class);

        GuiceUtil.buildMultiBinder(binder(), Clearable.class).addBinding(ActiveQueriesManager.class);

        TaskHandlerBinder.create(binder())
                .bind(DownloadQueryAction.class, DownloadQueryActionHandler.class)
                .bind(DownloadSearchResultsAction.class, DownloadSearchResultsHandler.class)
                .bind(FetchTimeZonesAction.class, FetchTimeZonesHandler.class)
                .bind(FetchVisualisationAction.class, FetchVisualisationHandler.class)
                .bind(SearchBusPollAction.class, SearchBusPollActionHandler.class)
                .bind(ValidateExpressionAction.class, ValidateExpressionHandler.class);

        final Multibinder<ExplorerActionHandler> explorerActionHandlerBinder = Multibinder.newSetBinder(binder(), ExplorerActionHandler.class);
        explorerActionHandlerBinder.addBinding().to(DashboardStoreImpl.class);

        final Multibinder<ImportExportActionHandler> importExportActionHandlerBinder = Multibinder.newSetBinder(binder(), ImportExportActionHandler.class);
        importExportActionHandlerBinder.addBinding().to(DashboardStoreImpl.class);

        DocumentActionHandlerBinder.create(binder())
                .bind(DashboardDoc.DOCUMENT_TYPE, DashboardStoreImpl.class);
    }
}