package com.example.models

data class SubscriptionRequest(val boardName: String, val author: String?, val keyword: String?)

data class SubscriptionRequestWithUserId(val subUid: String, val subInfo: SubscriptionRequest)

// 各個訂閱的列表
val subscriptionListStorage = mutableListOf<SubscriptionRequestWithUserId>()
