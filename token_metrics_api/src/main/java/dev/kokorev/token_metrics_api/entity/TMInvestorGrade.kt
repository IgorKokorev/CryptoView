package dev.kokorev.token_metrics_api.entity

import com.google.gson.annotations.SerializedName

data class TMInvestorGrade(
    @SerializedName("TOKEN_ID"                        ) var tokenId                    : Int?    = null,
    @SerializedName("TOKEN_NAME"                      ) var tokenName                  : String? = null,
    @SerializedName("TOKEN_SYMBOL"                    ) var tokenSymbol                : String? = null,
    @SerializedName("DATE"                            ) var date                       : String? = null,
    @SerializedName("TM_INVESTOR_GRADE"               ) var tmInvestorGrade            : Double? = null,
    @SerializedName("TM_INVESTOR_GRADE_7D_PCT_CHANGE" ) var tmInvestorGrade7dPctChange : Double? = null,
    @SerializedName("FUNDAMENTAL_GRADE"               ) var fundamentalGrade           : Double? = null,
    @SerializedName("TECHNOLOGY_GRADE"                ) var technologyGrade            : Double? = null,
    @SerializedName("VALUATION_GRADE"                 ) var valuationGrade             : Double? = null,
    @SerializedName("DEFI_USAGE_SCORE"                ) var defiUsageScore             : Int?    = null,
    @SerializedName("COMMUNITY_SCORE"                 ) var communityScore             : Double? = null,
    @SerializedName("EXCHANGE_SCORE"                  ) var exchangeScore              : Int?    = null,
    @SerializedName("VC_SCORE"                        ) var vcScore                    : Double? = null,
    @SerializedName("TOKENOMICS_SCORE"                ) var tokenomicsScore            : Int?    = null,
    @SerializedName("DEFI_SCANNER_SCORE"              ) var defiScannerScore           : Int?    = null,
    @SerializedName("ACTIVITY_SCORE"                  ) var activityScore              : Double? = null,
    @SerializedName("SECURITY_SCORE"                  ) var securityScore              : Double? = null,
    @SerializedName("REPOSITORY_SCORE"                ) var repositoryScore            : Double? = null,
    @SerializedName("COLLABORATION_SCORE"             ) var collaborationScore         : Double? = null
)
