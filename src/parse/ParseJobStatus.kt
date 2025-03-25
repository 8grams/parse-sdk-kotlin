package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * JobStatus class for handling background job status
 */
@Serializable
class ParseJobStatus : ParseObject("_JobStatus") {
    var jobName: String? = null
        private set
        get() {
            return get("jobName") as? String
        }

    var source: String? = null
        private set
        get() {
            return get("source") as? String
        }

    var status: String? = null
        private set
        get() {
            return get("status") as? String
        }

    var message: String? = null
        private set
        get() {
            return get("message") as? String
        }

    var params: Map<String, Any?>? = null
        private set
        get() {
            @Suppress("UNCHECKED_CAST")
            return get("params") as? Map<String, Any?>
        }

    var finishedAt: Date? = null
        private set
        get() {
            return get("finishedAt") as? Date
        }

    /**
     * Check if the job is completed
     */
    fun isCompleted(): Boolean {
        return status == "completed"
    }

    /**
     * Check if the job failed
     */
    fun isFailed(): Boolean {
        return status == "failed"
    }

    /**
     * Check if the job is running
     */
    fun isRunning(): Boolean {
        return status == "running"
    }

    override suspend fun save(): ParseJobStatus {
        throw UnsupportedOperationException("Job status cannot be directly saved")
    }

    override suspend fun delete() {
        throw UnsupportedOperationException("Job status cannot be directly deleted")
    }

    companion object {
        /**
         * Get a query for job status
         */
        fun query(): ParseQuery<ParseJobStatus> {
            return ParseQuery("_JobStatus")
        }

        /**
         * Get job status by ID
         */
        suspend fun getJobStatus(jobId: String): ParseJobStatus? {
            val query = query()
            query.where["objectId"] = jobId
            val results = query.find()
            return results.firstOrNull()
        }

        /**
         * Get job status by name
         */
        suspend fun getJobStatusByName(jobName: String): List<ParseJobStatus> {
            val query = query()
            query.where["jobName"] = jobName
            return query.find()
        }

        /**
         * Create a job status from a map
         */
        fun fromMap(map: Map<String, Any?>): ParseJobStatus {
            val jobStatus = ParseJobStatus()
            
            map.forEach { (key, value) ->
                when (key) {
                    "objectId" -> jobStatus.objectId = value as String
                    "createdAt" -> jobStatus.createdAt = Date(value as String)
                    "updatedAt" -> jobStatus.updatedAt = Date(value as String)
                    "ACL" -> jobStatus.acl = ParseACL.fromMap(value as Map<String, Any?>)
                    "jobName" -> jobStatus["jobName"] = value
                    "source" -> jobStatus["source"] = value
                    "status" -> jobStatus["status"] = value
                    "message" -> jobStatus["message"] = value
                    "params" -> jobStatus["params"] = value
                    "finishedAt" -> jobStatus["finishedAt"] = Date(value as String)
                    else -> jobStatus[key] = value
                }
            }

            return jobStatus
        }
    }
} 