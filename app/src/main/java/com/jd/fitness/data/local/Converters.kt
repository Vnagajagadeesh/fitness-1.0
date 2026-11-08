package com.jd.fitness.data.local

import androidx.room.TypeConverter
import com.jd.fitness.data.local.entity.FastingState
import com.jd.fitness.data.local.entity.HabitCategory
import com.jd.fitness.data.local.entity.SkincareRoutineType

class Converters {
    @TypeConverter
    fun fromHabitCategory(value: HabitCategory?): String? = value?.name

    @TypeConverter
    fun toHabitCategory(value: String?): HabitCategory? =
        value?.let { runCatching { HabitCategory.valueOf(it) }.getOrDefault(HabitCategory.CUSTOM) }

    @TypeConverter
    fun fromSkincareRoutineType(value: SkincareRoutineType?): String? = value?.name

    @TypeConverter
    fun toSkincareRoutineType(value: String?): SkincareRoutineType? =
        value?.let { runCatching { SkincareRoutineType.valueOf(it) }.getOrDefault(SkincareRoutineType.MORNING) }

    @TypeConverter
    fun fromFastingState(value: FastingState?): String? = value?.name

    @TypeConverter
    fun toFastingState(value: String?): FastingState? =
        value?.let { runCatching { FastingState.valueOf(it) }.getOrDefault(FastingState.COMPLETED) }
}

