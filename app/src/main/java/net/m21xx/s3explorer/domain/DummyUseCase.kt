package net.m21xx.s3explorer.domain

import javax.inject.Inject

class DummyUseCase @Inject constructor() {
    fun execute(): String {
        return "Injected successfully"
    }
}
