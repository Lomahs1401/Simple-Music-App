package com.example.simplemusicapp.data.repo.source

interface OnResultListener<T> {
    fun onSuccess(data: T)
    fun onError(exception: Exception?)
}