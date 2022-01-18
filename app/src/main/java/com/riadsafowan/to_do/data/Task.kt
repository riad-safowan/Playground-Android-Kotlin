package com.riadsafowan.to_do.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey (autoGenerate = true)
    val id: Int = 0,
    val taskName: String,
    val created: Long = System.currentTimeMillis(),
    val isImportant: Boolean = false,
    val isCompleted: Boolean = false
)
