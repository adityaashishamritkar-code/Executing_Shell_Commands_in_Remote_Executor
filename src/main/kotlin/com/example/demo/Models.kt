package com.example.demo

import jakarta.persistence.*
import java.util.UUID

data class CommandRequest(
    val script: String,
    val cpuLimit: Double = 1.0,
    val memoryLimit: String = "512m"
)

enum class Status { QUEUED, IN_PROGRESS, FINISHED, FAILED, TIMEOUT }

@Entity
data class JobRecord(
    @Id val id: String = UUID.randomUUID().toString(),
    val script: String = "",
    val cpuLimit: Double = 1.0,
    val memoryLimit: String = "512m",
    @Enumerated(EnumType.STRING) var status: Status = Status.QUEUED,
    @Column(length = 10000) var output: String? = null
)