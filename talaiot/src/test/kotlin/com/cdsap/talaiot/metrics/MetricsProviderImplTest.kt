package com.cdsap.talaiot.metrics

import com.cdsap.talaiot.configuration.MetricsConfiguration
import com.cdsap.talaiot.entities.*
import io.kotlintest.inspectors.forAtLeastOne
import io.kotlintest.inspectors.forAtMostOne
import io.kotlintest.specs.BehaviorSpec

class MetricsProviderImplTest : BehaviorSpec({
    given("Metrics Metrics Provider") {
        `when`("defaults are used") {
            val metrics = MetricsProviderImpl(executionReport()).get()

            then("RootProject, GradleRequested and GradleVersion are included") {

               assert (metrics.find {
                   it.first == "cpuCount" && it.second == "12"
               } != null)
              }
        }
    }
})

private fun executionReport(): ExecutionReport {
    return ExecutionReport(
        requestedTasks = "assemble",
        environment = Environment(
            cpuCount = "12", maxWorkers = "4"
        ),
        customProperties = CustomProperties(taskProperties = getMetrics()),
        tasks = listOf(
            TaskLength(
                1, "assemble", ":assemble", TaskMessageState.EXECUTED, false,
                "app", emptyList()
            )
        )
    )
}

private fun getMetrics(): MutableMap<String, String> {
    return mutableMapOf(
        "metric1" to "value1",
        "metric2" to "value2"
    )
}