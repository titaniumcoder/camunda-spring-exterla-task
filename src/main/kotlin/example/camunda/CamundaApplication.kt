package example.camunda

import jakarta.inject.Inject
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID


@SpringBootApplication
@EnableProcessApplication
@EnableScheduling
class CamundaApplication(
    @Inject val runtimeService: RuntimeService,
    val externalTaskService: ExternalTaskService
) {
    private val log = LoggerFactory.getLogger(CamundaApplication::class.java)

    @Scheduled(fixedRate = 1000L, initialDelay = 1000L)
    fun createTasks() {
        // start 100x
        (1..100).forEach {
            runtimeService.startProcessInstanceByKey("external_process")
        }
        log.info("obs started")}
    @Scheduled(fixedRate = 1000L, initialDelay = 5000L)
    fun executeExternalTasks() {
        log.info("ExternalTaskService Schedule")

        val tasks = externalTaskService.fetchAndLock(1000, "spring")
            .topic("funny", 60 * 1000L)
            .execute()

        tasks.forEach { task ->
            log.info("Executing ${task.id}")
            externalTaskService.complete(
                task.id,
                "spring",
                mapOf("global" to Instant.now()),
                mapOf("local" to LocalDateTime.now())
            )
        }

    }
}

fun main(args: Array<String>) {
    runApplication<CamundaApplication>(*args)
}
