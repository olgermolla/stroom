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

package stroom.activity.impl;

import stroom.activity.shared.Activity;
import stroom.activity.api.ActivityService;
import stroom.activity.shared.FetchActivityAction;
import stroom.event.logging.api.DocumentEventLog;
import stroom.security.api.Security;
import stroom.task.api.AbstractTaskHandler;

import javax.inject.Inject;

public class FetchActivityHandler extends AbstractTaskHandler<FetchActivityAction, Activity> {
    private final ActivityService activityService;
    private final DocumentEventLog entityEventLog;
    private final Security security;

    @Inject
    FetchActivityHandler(final ActivityService activityService,
                         final DocumentEventLog entityEventLog,
                         final Security security) {
        this.activityService = activityService;
        this.entityEventLog = entityEventLog;
        this.security = security;
    }

    @Override
    public Activity exec(final FetchActivityAction action) {
        final Activity activity = action.getActivity();
        return security.secureResult(() -> {
            Activity result;
            try {
                result = activityService.fetch(activity.getId());
                entityEventLog.view(result, null);
            } catch (final RuntimeException e) {
                entityEventLog.view(activity, e);
                throw e;
            }

            return result;
        });
    }
}
