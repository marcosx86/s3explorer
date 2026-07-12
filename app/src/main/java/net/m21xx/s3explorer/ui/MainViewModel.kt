package net.m21xx.s3explorer.ui

import androidx.lifecycle.ViewModel
import net.m21xx.s3explorer.domain.DummyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dummyUseCase: DummyUseCase
) : ViewModel() {
    fun testInjection(): String {
        return dummyUseCase.execute()
    }
}
