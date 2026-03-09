package click.yinsb.icmtracing.temporal.workflow;

import click.yinsb.icmtracing.temporal.model.EventMessage;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MainWorkflow {

	@WorkflowMethod(name = "MainWorkflow")
	void runAsync(EventMessage eventMessage);
}