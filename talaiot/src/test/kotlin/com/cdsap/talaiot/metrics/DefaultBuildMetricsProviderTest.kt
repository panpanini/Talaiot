package com.cdsap.talaiot.metrics

import com.cdsap.talaiot.entities.*
import io.kotlintest.matchers.maps.shouldContainKeys
import io.kotlintest.specs.BehaviorSpec
import junit.framework.Assert.assertTrue

class DefaultBuildMetricsProviderTest : BehaviorSpec({
    given("DefaultBuildMetricsProvider instance") {
        `when`("Environment defines cpuCount and maxWorkers metrics") {
            val metrics = DefaultBuildMetricsProvider(executionReport()).get()
            then("cpuCount and maxWorker exist and they have the proper type") {
                assertTrue(metrics.filter {
                    it.key == "cpuCount" && it.value == 12
                }.count() == 1)
                assertTrue(metrics.filter {
                    it.key == "maxWorkers" && it.value == 4
                }.count() == 1)

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