package click.yinsb.icmtracing.temporal.client;

import click.yinsb.icmtracing.temporal.model.Constants;
import click.yinsb.icmtracing.temporal.model.EventMessage;
import click.yinsb.icmtracing.temporal.workflow.MainWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WorkflowClientService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowClientService.class);

    private final WorkflowClient client;

    public WorkflowClientService(WorkflowClient client) {
        this.client = client;
    }

    public void start(EventMessage eventMessage) {
        try {
            log.info("starting workflow");
            String workflowId = UUID.randomUUID().toString();

            WorkflowOptions options = WorkflowOptions.newBuilder().setTaskQueue(Constants.ICM_TASK_QUEUE)
                    .setWorkflowId(workflowId).build();

            MainWorkflow stub1 = client.newWorkflowStub(MainWorkflow.class, options);
            WorkflowClient.start(stub1::runAsync, eventMessage); // this call do not block

            log.info("workflow started: {}", workflowId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
}
