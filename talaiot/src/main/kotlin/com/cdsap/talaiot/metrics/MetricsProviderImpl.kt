package com.cdsap.talaiot.metrics

import com.cdsap.talaiot.entities.ExecutionReport

class MetricsProviderImpl(val report: ExecutionReport) : MetricsProvider {

     fun get(): List<Pair<String, Any>> {
        val metrics = mutableListOf<Pair<String, Any>>()
        val buildMeta = report.flattenBuildEnv()
        buildMeta.forEach { (k, v) ->
            metrics.add(Pair(k, v))
        }
        metrics.add(Pair("duration", report.durationMs?.toLong() ?: 0L))
        metrics.add(Pair("configuration", report.configurationDurationMs?.toLong() ?: 0L))
        metrics.add(Pair("success", report.success))
        report.customProperties.buildProperties.forEach { (k, v) ->
            metrics.add(Pair(k, v))
        }
        report.environment.osVersion?.let { metrics.add(Pair("osVersion", it)) }
        report.environment.maxWorkers?.let { metrics.add(Pair("maxWorkers", it.toLong())) }
        report.environment.javaRuntime?.let { metrics.add(Pair("javaRuntime", it)) }
        report.environment.javaVmName?.let { metrics.add(Pair("javaVmName", it)) }
        report.environment.javaXmsBytes?.let { metrics.add(Pair("javaXmsBytes", it.toLong())) }
        report.environment.javaXmxBytes?.let { metrics.add(Pair("javaXmxBytes", it.toLong())) }
        report.environment.javaMaxPermSize?.let { metrics.add(Pair("javaMaxPermSize", it.toLong())) }
        report.environment.totalRamAvailableBytes?.let { metrics.add(Pair("totalRamAvailableBytes", it.toLong())) }
        report.environment.cpuCount?.let { metrics.add(Pair("cpuCount", it.toLong())) }
        report.environment.locale?.let { metrics.add(Pair("locale", it)) }
        report.environment.username?.let { metrics.add(Pair("username", it)) }
        report.environment.publicIp?.let { metrics.add(Pair("publicIp", it)) }
        report.environment.defaultChartset?.let { metrics.add(Pair("defaultCharset", it)) }
        report.environment.ideVersion?.let { metrics.add(Pair("ideVersion", it)) }
        report.environment.gradleVersion?.let { metrics.add(Pair("gradleVersion", it)) }
        report.environment.gitBranch?.let { metrics.add(Pair("gitBranch", it)) }
        report.environment.gitUser?.let { metrics.add(Pair("gitUser", it)) }
        report.environment.hostname?.let { metrics.add(Pair("hostname", it)) }
        report.environment.osManufacturer?.let { metrics.add(Pair("osManufacturer", it)) }
        report.environment.publicIp?.let { metrics.add(Pair("publicIp", it)) }
        report.cacheRatio?.let { metrics.add(Pair("cacheRatio", it.toDouble())) }
        report.beginMs?.let { metrics.add(Pair("start", it.toDouble())) }
        report.rootProject?.let { metrics.add(Pair("rootProject", it)) }
        report.requestedTasks?.let { metrics.add(Pair("requestedTasks", it)) }
        report.scanLink?.let { metrics.add(Pair("scanLink", it)) }
        return metrics
    }
}
