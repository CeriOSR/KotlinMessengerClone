package com.example.reycerio.kotlinmessenger.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//add this after you add the androidExtension {} to your gradle.app
@Parcelize
//turn this into parcelable so we can pass this data around.
class User(val uid: String, val username: String, val profileImageUrl: String) : Parcelable{
    //need the constructor if youre gonna plug in data into empty class
    constructor() : this("", "", "")
}