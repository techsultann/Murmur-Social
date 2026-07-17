package com.sultlab.murmur.data.local.converters

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json.Default.decodeFromString
import kotlinx.serialization.json.Json.Default.encodeToString
import kotlinx.serialization.serializer

class ReactionConverters {

    @ColumnTypeConverter
    fun fromReactions(reactions: Map<String, List<String>>): String =
        encodeToString(
            MapSerializer(
                serializer(),
                ListSerializer(
                    serializer()
                )
            ),
            reactions,
        )


    @ColumnTypeConverter
    fun toReactions(json: String): Map<String, List<String>> =
        try {
            decodeFromString(
                MapSerializer(
                    serializer(),
                    ListSerializer(
                        serializer()
                    )
                ),
                json
            )
        } catch (e: Exception) {
            emptyMap()
        }

}