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

package stroom.explorer.impl;

import stroom.explorer.api.ExplorerService;
import stroom.explorer.shared.BulkActionResult;
import stroom.explorer.shared.ExplorerServiceMoveAction;
import stroom.security.api.Security;
import stroom.task.api.AbstractTaskHandler;

import javax.inject.Inject;


class ExplorerServiceMoveHandler
        extends AbstractTaskHandler<ExplorerServiceMoveAction, BulkActionResult> {
    private final ExplorerService explorerService;
    private final Security security;

    @Inject
    ExplorerServiceMoveHandler(final ExplorerService explorerService,
                               final Security security) {
        this.explorerService = explorerService;
        this.security = security;
    }

    @Override
    public BulkActionResult exec(final ExplorerServiceMoveAction action) {
        return security.secureResult(() -> explorerService.move(action.getDocRefs(), action.getDestinationFolderRef(), action.getPermissionInheritance()));
    }
}