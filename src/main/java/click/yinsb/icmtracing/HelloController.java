package click.yinsb.icmtracing;

import click.yinsb.icmtracing.temporal.client.WorkflowClientService;
import click.yinsb.icmtracing.temporal.model.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class HelloController {
    private final WorkflowClientService workflowClientService;

    public HelloController(WorkflowClientService workflowClientService) {
        this.workflowClientService = workflowClientService;
    }

    @PostMapping("/hello")
    public void hello(EventMessage eventMessage) {
        workflowClientService.start(eventMessage);
    }
}
