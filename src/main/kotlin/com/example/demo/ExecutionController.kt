package com.example.demo

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/jobs")
class ExecutionController(val service: JobManagerService) {

    @PostMapping
    fun submit(@RequestBody req: CommandRequest): String {
        return service.submit(req)
    }

    @GetMapping("/{id}")
    fun check(@PathVariable id: String): JobRecord? {
        return service.getJob(id)
    }

    @GetMapping
    fun listAll(): List<JobRecord> {
        return service.getAll()
    }
}
