package com.example.callyourmother

import android.graphics.Bitmap
import java.util.*

class Contacts {
    var image: String? = null
    var phone: String? = null
    var name: String? = null
    var notification: String? = null
    var lastCallDate: Date? = null

    constructor(image: String?, phone: String?, name:String?, notification: String?, lastCallDate: Date) {
        this.image = image
        this.phone = phone
        this.name = name
        this.notification = notification
        this.lastCallDate = lastCallDate
    }

}