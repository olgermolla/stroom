/*
 * Copyright 2016 Crown Copyright
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

package stroom.core.servlet;

import stroom.util.shared.ResultList;
import stroom.security.api.Security;
import stroom.task.api.AbstractTaskHandler;

import javax.inject.Inject;

class SessionListClusterHandler extends AbstractTaskHandler<SessionListClusterTask, ResultList<SessionDetails>> {
    private final SessionListService sessionListService;
    private final Security security;

    @Inject
    SessionListClusterHandler(final SessionListService sessionListService,
                              final Security security) {
        this.sessionListService = sessionListService;
        this.security = security;
    }

    @Override
    public ResultList<SessionDetails> exec(final SessionListClusterTask task) {
        return security.insecureResult(() -> sessionListService.find(null));
    }
}
