package stroom.jobsystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.task.api.job.ScheduledJob;
import stroom.task.api.job.TaskConsumer;
import stroom.task.shared.Task;

import java.util.concurrent.atomic.AtomicBoolean;

class ScheduledJobFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledJobFunction.class);

    private final AtomicBoolean running;
    private final ScheduledJob scheduledJob;
    private final TaskConsumer consumer;

    public ScheduledJobFunction(final ScheduledJob scheduledJob, final TaskConsumer consumer, final AtomicBoolean running) {
        this.scheduledJob = scheduledJob;
        this.running = running;
        this.consumer = consumer;
    }

    public void exec(final Task<?> task) {
        try {
            //TODO: debug logging
//            LOGGER.debug(message + " " + methodReference.getClazz().getName() + "." + methodReference.getMethod().getName());

            consumer.accept(task);
        } catch (final RuntimeException e) {
            LOGGER.error("Error calling {}", scheduledJob.getName(), e);
        } finally {
            running.set(false);
        }
    }

    @Override
    public String toString() {
        return scheduledJob.toString();
    }
}
