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

package stroom.streamtask;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import stroom.entity.FindService;
import stroom.jobsystem.DistributedTaskFactory;
import stroom.streamtask.shared.CreateProcessorAction;
import stroom.streamtask.shared.FetchProcessorAction;
import stroom.streamtask.shared.ReprocessDataAction;
import stroom.task.api.TaskHandlerBinder;
import stroom.util.lifecycle.LifecycleAwareBinder;

public class StreamTaskModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(StreamTaskCreator.class).to(StreamTaskCreatorImpl.class);
        bind(StreamProcessorFilterService.class).to(StreamProcessorFilterServiceImpl.class);
        bind(StreamProcessorService.class).to(StreamProcessorServiceImpl.class);
        bind(StreamTaskService.class).to(StreamTaskServiceImpl.class);
        bind(CachedStreamProcessorFilterService.class).to(CachedStreamProcessorFilterServiceImpl.class);
        bind(CachedStreamProcessorService.class).to(CachedStreamProcessorServiceImpl.class);

        TaskHandlerBinder.create(binder())
                .bind(CreateProcessorAction.class, stroom.streamtask.CreateProcessorHandler.class)
                .bind(CreateStreamTasksTask.class, stroom.streamtask.CreateStreamTasksTaskHandler.class)
                .bind(FetchProcessorAction.class, stroom.streamtask.FetchProcessorHandler.class)
                .bind(ReprocessDataAction.class, stroom.streamtask.ReprocessDataHandler.class)
                .bind(StreamProcessorTask.class, stroom.streamtask.StreamProcessorTaskHandler.class);

        final Multibinder<DistributedTaskFactory> distributedTaskFactoryBinder = Multibinder.newSetBinder(binder(), DistributedTaskFactory.class);
        distributedTaskFactoryBinder.addBinding().to(stroom.streamtask.StreamProcessorTaskFactory.class);

        final Multibinder<FindService> findServiceBinder = Multibinder.newSetBinder(binder(), FindService.class);
        findServiceBinder.addBinding().to(StreamTaskServiceImpl.class);

        LifecycleAwareBinder.create(binder()).bind(StreamTaskCreatorImpl.class);
    }
}