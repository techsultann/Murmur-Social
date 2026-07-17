package com.sultlab.murmur.data.local

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.ColumnTypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.sultlab.murmur.data.local.converters.ReactionConverters
import com.sultlab.murmur.data.local.dao.GroupDao
import com.sultlab.murmur.data.local.dao.GroupMessageDao
import com.sultlab.murmur.data.local.dao.PostDao
import com.sultlab.murmur.data.local.model.GroupEntity
import com.sultlab.murmur.data.local.model.GroupMessageEntity
import com.sultlab.murmur.data.local.model.PostEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [PostEntity::class, GroupMessageEntity::class, GroupEntity::class], version = 5)
@ColumnTypeConverters(ReactionConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun groupMessageDao(): GroupMessageDao
    abstract fun groupDao(): GroupDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(true)
        .build()
}
