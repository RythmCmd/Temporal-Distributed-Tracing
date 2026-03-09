
package click.yinsb.icmtracing.temporal.workflow;

import click.yinsb.icmtracing.temporal.model.Constants;
import click.yinsb.icmtracing.temporal.model.EventMessage;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WorkflowImpl(taskQueues = { Constants.ICM_TASK_QUEUE })
public class MainWorkflowImpl implements MainWorkflow {
	private static final Logger log = LoggerFactory.getLogger(MainWorkflowImpl.class);

	@Override
	public void runAsync(EventMessage eventMessage) {
		log.info("run main workflow");

		// create Workflow Stubs
		ChildWorkflow001 stub1 = Workflow.newChildWorkflowStub(ChildWorkflow001.class);

		// start workflows
		stub1.run(eventMessage);

	}
}
