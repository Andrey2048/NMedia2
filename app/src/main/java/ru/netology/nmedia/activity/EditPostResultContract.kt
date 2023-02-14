package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class EditPostResultContract : ActivityResultContract<String, String>() {


    override fun createIntent(context: Context, input: String): Intent {

        val intent = Intent(context, EditPostActivity::class.java).apply {
            putExtra(Intent.EXTRA_TEXT, input)
        }
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String {
        val resultNullable: String?
        val result: String
        if (resultCode == Activity.RESULT_OK) {
            resultNullable = intent?.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            resultNullable = null
        }
        result = resultNullable ?: ""
        return result
    }
}