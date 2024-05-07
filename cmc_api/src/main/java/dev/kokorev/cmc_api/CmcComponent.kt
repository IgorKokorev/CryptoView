package dev.kokorev.cmc_api

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [CmcModule::class]
)
interface CmcComponent : CmcProvider