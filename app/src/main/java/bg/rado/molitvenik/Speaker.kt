package bg.rado.molitvenik

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/** Четене на глас на български чрез вградения в телефона TextToSpeech. */
class Speaker(context: Context) : TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = TextToSpeech(appContext, this)
    private var ready = false
    private var bulgarianOk = false

    /** id на молитвата, която се чете в момента (или null). */
    var speakingId by mutableStateOf<String?>(null)
        private set

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val engine = tts ?: return
        val r = engine.setLanguage(Locale("bg", "BG"))
        bulgarianOk = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
        engine.setSpeechRate(0.9f)
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) = finished(utteranceId)
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = finished(utteranceId)
            override fun onError(utteranceId: String?, errorCode: Int) = finished(utteranceId)
        })
        ready = true
    }

    private fun finished(utteranceId: String?) {
        main.post { if (speakingId == utteranceId) speakingId = null }
    }

    fun toggle(id: String, text: String) {
        if (speakingId == id) {
            stop()
            return
        }
        if (!ready) {
            Toast.makeText(appContext, "Гласът се зарежда, опитай след секунда", Toast.LENGTH_SHORT).show()
            return
        }
        if (!bulgarianOk) {
            Toast.makeText(
                appContext,
                "Няма инсталиран български глас. Изтегли го от настройките за преобразуване на текст в говор.",
                Toast.LENGTH_LONG,
            ).show()
            try {
                appContext.startActivity(
                    Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            } catch (_: ActivityNotFoundException) {
            }
            return
        }
        val clean = text.replace("(име)", "").replace("(имена)", "")
        speakingId = id
        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    fun stop() {
        tts?.stop()
        speakingId = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
