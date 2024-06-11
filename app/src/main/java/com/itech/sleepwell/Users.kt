package com.itech.sleepwell

data class Users(
    var email: String? = null,
    var firstname: String? = null,
    var lastname: String? = null,
    var mobilenum: String? = null,
    var password: String? = null,
    var imageUrl: String? = "https://firebasestorage.googleapis.com/v0/b/sleepwell-4ee66.appspot.com/o/images%2Fprofile_icon.png?alt=media&token=df70acba-770c-4094-8055-c8964b2eadff",
    var uid: String? = null
)
