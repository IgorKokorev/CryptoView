package dev.kokorev.cmc_api

interface CmcProvider {
    fun provideBinance() : CmcApi
}