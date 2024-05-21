package dev.kokorev.cryptoview.domain

import dev.kokorev.cryptoview.data.PreferenceProvider

// Interactor to communicate with shared preferences
class Preferences(private val preferenceProvider: PreferenceProvider) {
    fun getLastTopMoversCallTime() = preferenceProvider.getLastTopMoversCallTime()
    fun saveLastTopMoversCallTime() = preferenceProvider.saveLastTopMoversCallTime()
    fun getLastCpTickersCallTime() = preferenceProvider.getLastCpTickersCallTime()
    fun saveLastCpTickersCallTime() = preferenceProvider.saveLastCpTickersCallTime()
    fun getLastAppUpdateTime() = preferenceProvider.getLastAppUpdateTime()
    fun saveLastAppUpdateTime() = preferenceProvider.saveLastAppUpdateTime()
    fun getCPTickersUpdateTime() = preferenceProvider.getCPTickersUpdateTime()
    fun saveCPTickersUpdateTime() = preferenceProvider.saveCPTickersUpdateTime()
    fun getMinMcap() = preferenceProvider.getMinMcap()
    fun saveMinMcap(mcap: Long) = preferenceProvider.saveMinMcap(mcap)
    fun getMinVol() = preferenceProvider.getMinVol()
    fun saveMinVol(vol: Long) = preferenceProvider.saveMinVol(vol)

}