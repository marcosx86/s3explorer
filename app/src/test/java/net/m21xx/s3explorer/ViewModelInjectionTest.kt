package net.m21xx.s3explorer

import net.m21xx.s3explorer.domain.DummyUseCase
import net.m21xx.s3explorer.ui.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class ViewModelInjectionTest {
    @Test
    fun testViewModelInjection() {
        val useCase = DummyUseCase()
        val viewModel = MainViewModel(useCase)
        assertEquals("Injected successfully", viewModel.testInjection())
    }
}
