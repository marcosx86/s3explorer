package net.m21xx.s3explorer.ui.connection

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class NewConnectionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testConnectButtonIsDisabledInitially() {
        composeTestRule.setContent {
            NewConnectionScreen(
                viewModel = NewConnectionViewModel(),
                onConnectionSuccess = {}
            )
        }

        // Initially disabled
        composeTestRule.onNodeWithText("Connect").assertIsNotEnabled()

        // Fill all fields
        composeTestRule.onNodeWithText("Access Key ID / Username").performTextInput("myAccessKey")
        composeTestRule.onNodeWithText("Secret Access Key / Password").performTextInput("mySecretKey")
        composeTestRule.onNodeWithText("Endpoint URL").performTextInput("http://localhost:9000")
        
        // Accept terms
        composeTestRule.onNodeWithText("I agree to the Terms of Service and Privacy Policy.").performClick()

        // Now it should be enabled
        composeTestRule.onNodeWithText("Connect").assertIsEnabled()
    }
}
