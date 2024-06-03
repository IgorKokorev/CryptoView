package dev.kokorev.room_db.core_api.db

import android.content.Context

interface ContextProviderDB {
    fun provideContext(): Context
}