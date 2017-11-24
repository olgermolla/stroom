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

package stroom.pipeline.client.presenter;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.View;
import stroom.entity.client.presenter.DocumentSettingsPresenter;
import stroom.pipeline.client.presenter.PipelineSettingsPresenter.PipelineSettingsView;
import stroom.pipeline.shared.PipelineDocument;
import stroom.pipeline.shared.PipelineDocument.PipelineType;
import stroom.security.client.ClientSecurityContext;

public class PipelineSettingsPresenter
        extends DocumentSettingsPresenter<PipelineSettingsView, PipelineDocument>
        implements PipelineSettingsUiHandlers {
    @Inject
    public PipelineSettingsPresenter(final EventBus eventBus, final PipelineSettingsView view,
                                     final ClientSecurityContext securityContext) {
        super(eventBus, view, securityContext);
        view.setUiHandlers(this);
    }

    @Override
    public String getType() {
        return PipelineDocument.DOCUMENT_TYPE;
    }

    @Override
    protected void onRead(final PipelineDocument pipelineDocument) {
        getView().setDescription(pipelineDocument.getDescription());
        getView().clearTypes();
        for (final PipelineDocument.PipelineType type : PipelineDocument.PipelineType.values()) {
            getView().addType(type);
        }

        PipelineDocument.PipelineType type = null;
        if (pipelineDocument.getPipelineType() != null) {
            for (final PipelineDocument.PipelineType t : PipelineDocument.PipelineType.values()) {
                if (t.getDisplayValue().equals(pipelineDocument.getPipelineType())) {
                    type = t;
                }
            }
        }

        getView().setType(type);
    }

    @Override
    protected void onWrite(final PipelineDocument pipelineDocument) {
        final PipelineType pipelineType = getView().getType();
        if (pipelineType != null && !pipelineType.getDisplayValue().equals(pipelineDocument.getPipelineType())) {
            pipelineDocument.setPipelineType(pipelineType.getDisplayValue());
            setDirty(true);
        }
        if (!getView().getDescription().trim().equals(pipelineDocument.getDescription())) {
            pipelineDocument.setDescription(getView().getDescription().trim());
            setDirty(true);
        }
    }

    public interface PipelineSettingsView extends View, HasUiHandlers<PipelineSettingsUiHandlers> {
        String getDescription();

        void setDescription(String description);

        void clearTypes();

        void addType(PipelineDocument.PipelineType type);

        PipelineDocument.PipelineType getType();

        void setType(PipelineDocument.PipelineType type);
    }
}
