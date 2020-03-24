package com.cdsap.talaiot.publisher.rethinkdb

import com.cdsap.talaiot.configuration.RethinkDbPublisherConfiguration
import com.cdsap.talaiot.entities.*
import com.cdsap.talaiot.logger.LogTracker
import com.cdsap.talaiot.metrics.MetricsProviderImpl
import com.cdsap.talaiot.publisher.Publisher
import com.rethinkdb.RethinkDB
import com.rethinkdb.net.Connection
import org.influxdb.dto.Point
import java.net.URL
import java.util.concurrent.Executor

/**
 * Publisher using RethinkDb format to send the metrics
 */
class RethinkDbPublisher(
    /**
     * General configuration for the publisher
     */
    private val rethinkDbPublisherConfiguration: RethinkDbPublisherConfiguration,
    /**
     * LogTracker to print in console depending on the Mode
     */
    private val logTracker: LogTracker,
    /**
     * Executor to schedule a task in Background
     */
    private val executor: Executor
) : Publisher {

    private val TAG = "RethinkDbPublisher"

    val r = RethinkDB.r

    override fun publish(report: ExecutionReport) {
        if (rethinkDbPublisherConfiguration.url.isEmpty() ||
            rethinkDbPublisherConfiguration.dbName.isEmpty() ||
            rethinkDbPublisherConfiguration.taskTableName.isEmpty() ||
            rethinkDbPublisherConfiguration.buildTableName.isEmpty()
        ) {
            error(
                "RethinkDbPublisher not executed. Configuration requires url, dbName, taskTableName and buildTableName: \n" +
                        "rethinkDbPublisher {\n" +
                        "            dbName = \"tracking\"\n" +
                        "            url = \"http://localhost:8086\"\n" +
                        "            buildTableName = \"build\"\n" +
                        "            taskTableName = \"task\"\n" +
                        "}\n" +
                        "Please update your configuration"
            )
        }



        executor.execute {
            logTracker.log(TAG, "================")
            logTracker.log(TAG, "RethinkDbPublisher")
            logTracker.log(TAG, "publishBuildMetrics: ${rethinkDbPublisherConfiguration.publishBuildMetrics}")
            logTracker.log(TAG, "publishTaskMetrics: ${rethinkDbPublisherConfiguration.publishTaskMetrics}")
            logTracker.log(TAG, "================")

            try {
                val url = URL(rethinkDbPublisherConfiguration.url)
                val conn: Connection = if (rethinkDbPublisherConfiguration.username.isBlank() &&
                    rethinkDbPublisherConfiguration.password.isBlank()
                ) {
                    r.connection()
                        .hostname(url.host)
                        .port(url.port)
                        .connect()
                } else {
                    r.connection()
                        .hostname(url.host)
                        .port(url.port)
                        .user(rethinkDbPublisherConfiguration.username, rethinkDbPublisherConfiguration.password)
                        .connect()
                }

                checkDb(conn, rethinkDbPublisherConfiguration.dbName)

                if (rethinkDbPublisherConfiguration.publishTaskMetrics) {
                    val entries = createTaskEntries(report)
                    if (entries.isNotEmpty()) {
                        checkTable(
                            conn,
                            rethinkDbPublisherConfiguration.dbName,
                            rethinkDbPublisherConfiguration.taskTableName
                        )
                        insertEntries(
                            conn,
                            rethinkDbPublisherConfiguration.dbName,
                            rethinkDbPublisherConfiguration.taskTableName,
                            entries
                        )
                    }
                }

                if (rethinkDbPublisherConfiguration.publishBuildMetrics) {
                    val entries = createBuildEntry(report)
                    if (entries != null && entries.isNotEmpty()) {
                        checkTable(
                            conn,
                            rethinkDbPublisherConfiguration.dbName,
                            rethinkDbPublisherConfiguration.buildTableName
                        )
                        insertEntries(
                            conn,
                            rethinkDbPublisherConfiguration.dbName,
                            rethinkDbPublisherConfiguration.buildTableName,
                            entries
                        )
                    }
                }

            } catch (e: Exception) {
                logTracker.error("RethinkDbPublisher- Error executing the Runnable: ${e.message}")
            }
        }
    }

    private fun insertEntries(
        conn: Connection,
        db: String,
        table: String,
        entries: List<Pair<String, Any>>?
    ) {
        r.db(db).table(table).insert(entries).run<Any>(conn)
    }

    private fun checkDb(conn: Connection, db: String) {
        val exist = r.dbList().contains(db).run<Boolean>(conn)
        if (!exist) {
            r.dbCreate(db).run<Any>(conn)
        }
    }

    private fun checkTable(conn: Connection, db: String, table: String) {
        val exist = r.db(db).tableList().contains(table).run<Boolean>(conn)
        if (!exist) {
            r.db(db).tableCreate(table).run<Any>(conn)
        }
    }

    private fun createTaskEntries(report: ExecutionReport): List<Pair<String, Any>> {
        val taskCustomProperties = getCustomProperties(report.customProperties.taskProperties)

        val list = mutableListOf<Pair<String, Any>>()
        taskCustomProperties.forEach {
            it.entries.forEach {
                list.add(Pair(it.key, it.value))
            }
        }
        report.tasks?.forEach { task ->

            list.add(Pair("state", task.state.name))
            list.add(Pair("module", task.module))
            list.add(Pair("time", System.currentTimeMillis()))
            list.add(Pair("rootNode", task.rootNode.toString()))
            list.add(Pair("task", task.taskPath))
            list.add(Pair("workerId", task.workerId))
            list.add(Pair("value", task.ms))
            list.add(Pair("critical", task.critical.toString()))


        }
        return list
    }

    private fun getCustomProperties(taskProperties: MutableMap<String, String>): List<Map<String, Any>> {
        return taskProperties.flatMap {
            listOf(mapOf(it.key to it.value))
        }
    }

    private fun createBuildEntry(report: ExecutionReport): List<Pair<String, Any>>? {
        val metricsProvider = MetricsProviderImpl<Point>(report)
        return metricsProvider.get()
    }
}
