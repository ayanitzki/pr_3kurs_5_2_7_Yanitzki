package com.example.pr_3kurs_5_2_7_yanitzki

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie (
    @PrimaryKey val id: Int,
    val name: String,
    val year: Int,
    val rating: Double,
    val description: String?
)