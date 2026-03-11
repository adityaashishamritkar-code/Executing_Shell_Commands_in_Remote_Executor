package com.example.demo

import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class JobManagerService(private val repository: JobRepository) {
    // Only allow 2 jobs to run at the same time (Queueing nuance)
    private val threadPool = Executors.newFixedThreadPool(2)

    fun submit(req: CommandRequest): String {
        val job = JobRecord(script = req.script, cpuLimit = req.cpuLimit, memoryLimit = req.memoryLimit)
        repository.save(job) // Saved to DB as QUEUED

        threadPool.submit {
            runTask(job)
        }
        return job.id
    }

    private fun runTask(job: JobRecord) {
        job.status = Status.IN_PROGRESS
        repository.save(job)

        println("Executing command: docker run --cpus ${job.cpuLimit} --memory ${job.memoryLimit} alpine sh -c \"${job.script}\"")

        try {
            val process = ProcessBuilder(
                "docker", "run", "--rm", "--cpus", job.cpuLimit.toString(),
                "--memory", job.memoryLimit, "alpine", "sh", "-c", job.script
            ).redirectErrorStream(true).start()

            // Timeout nuance: Wait 30 seconds max
            val finished = process.waitFor(30, TimeUnit.SECONDS)

            if (finished) {
                job.output = process.inputStream.bufferedReader().readText()
                job.status = Status.FINISHED
            } else {
                process.destroyForcibly() // Kill the infinite loop
                job.status = Status.TIMEOUT
                job.output = "Error: Execution exceeded 30s limit."
            }
        } catch (e: Exception) {
            job.status = Status.FAILED
            job.output = e.message
        }
        repository.save(job)
    }

    fun getJob(id: String) = repository.findById(id).orElse(null)
    fun getAll() = repository.findAll()
}