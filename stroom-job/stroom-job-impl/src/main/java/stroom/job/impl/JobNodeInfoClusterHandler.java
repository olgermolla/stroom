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

package stroom.job.impl;

import stroom.job.shared.JobNode;
import stroom.job.shared.JobNodeInfo;
import stroom.security.api.Security;
import stroom.task.api.AbstractTaskHandler;
import stroom.util.scheduler.Scheduler;
import stroom.util.shared.SharedMap;

import javax.inject.Inject;
import java.util.Collection;


class JobNodeInfoClusterHandler
        extends AbstractTaskHandler<JobNodeInfoClusterTask, SharedMap<JobNode, JobNodeInfo>> {
    private final JobNodeTrackerCache jobNodeTrackerCache;
    private final Security security;

    @Inject
    JobNodeInfoClusterHandler(final JobNodeTrackerCache jobNodeTrackerCache,
                              final Security security) {
        this.jobNodeTrackerCache = jobNodeTrackerCache;
        this.security = security;
    }

    @Override
    public stroom.util.shared.SharedMap<JobNode, JobNodeInfo> exec(final JobNodeInfoClusterTask task) {
        return security.secureResult(() -> {
            final SharedMap<JobNode, JobNodeInfo> result = new SharedMap<>();
            final JobNodeTrackerCache.Trackers trackers = jobNodeTrackerCache.getTrackers();
            if (trackers != null) {
                final Collection<JobNodeTracker> trackerList = trackers.getTrackerList();
                if (trackerList != null) {
                    for (final JobNodeTracker tracker : trackerList) {
                        final JobNode jobNode = tracker.getJobNode();
                        final int currentTaskCount = tracker.getCurrentTaskCount();

                        Long scheduleReferenceTime = null;
                        final Scheduler scheduler = trackers.getScheduler(jobNode);
                        if (scheduler != null) {
                            scheduleReferenceTime = scheduler.getScheduleReferenceTime();
                        }

                        final JobNodeInfo info = new JobNodeInfo(currentTaskCount, scheduleReferenceTime,
                                tracker.getLastExecutedTime());
                        result.put(jobNode, info);
                    }
                }
            }

            return result;
        });
    }
}
