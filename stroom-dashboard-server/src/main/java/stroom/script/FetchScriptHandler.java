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

package stroom.script;

import stroom.query.api.v2.DocRef;
import stroom.script.shared.FetchScriptAction;
import stroom.script.shared.Script;
import stroom.security.Security;
import stroom.task.AbstractTaskHandler;
import stroom.task.TaskHandlerBean;
import stroom.util.shared.SharedList;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@TaskHandlerBean(task = FetchScriptAction.class)
class FetchScriptHandler extends AbstractTaskHandler<FetchScriptAction, SharedList<Script>> {
    private final ScriptService scriptService;
    private final Security security;

    @Inject
    FetchScriptHandler(final ScriptService scriptService,
                       final Security security) {
        this.scriptService = scriptService;
        this.security = security;
    }

    @Override
    public SharedList<Script> exec(final FetchScriptAction action) {
        return security.secureResult(() -> {
            // Elevate the users permissions for the duration of this task so they can read the script if they have 'use' permission.
            return security.useAsReadResult(() -> {
                final List<Script> scripts = new ArrayList<>();

                Set<DocRef> uiLoadedScripts = action.getLoadedScripts();
                if (uiLoadedScripts == null) {
                    uiLoadedScripts = new HashSet<>();
                }

                // Load the script and it's dependencies.
                loadScripts(action.getScript(), uiLoadedScripts, new HashSet<>(), scripts);

                return new SharedList<>(scripts);
            });
        });
    }

    private void loadScripts(final DocRef docRef, final Set<DocRef> uiLoadedScripts, final Set<DocRef> loadedScripts,
                             final List<Script> scripts) {
        // Prevent circular reference loading with this set.
        if (!loadedScripts.contains(docRef)) {
            loadedScripts.add(docRef);

            // Load the script.
            final Script loadedScript = scriptService.loadByUuid(docRef.getUuid());
            if (loadedScript != null) {
                // Add required dependencies first.
                if (loadedScript.getDependencies() != null) {
                    for (final DocRef dep : loadedScript.getDependencies()) {
                        loadScripts(dep, uiLoadedScripts, loadedScripts, scripts);
                    }
                }

                // Add this script.
                if (!uiLoadedScripts.contains(docRef)) {
                    uiLoadedScripts.add(docRef);
                    scripts.add(loadedScript);
                }
            }
        }
    }
}