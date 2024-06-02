package dev.kokorev.cryptoview.views.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.coinpaprika.apiclient.entity.MessageDB
import com.coinpaprika.apiclient.entity.MessageType
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.ChatInItemBinding
import dev.kokorev.cryptoview.databinding.ChatOutItemBinding
import dev.kokorev.cryptoview.databinding.FragmentAiChatBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.AiChatViewModel
import dev.kokorev.token_metrics_api.entity.AiQuestion
import dev.kokorev.token_metrics_api.entity.AiQuestionMessage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Locale

// Fragment to chat with TokenMetrics AI chat bot
class AiChatFragment : Fragment() {
    private val viewModel: AiChatViewModel by viewModels()
    private lateinit var binding: FragmentAiChatBinding
    private val autoDisposable = AutoDisposable()
    private var lastShownTime = 0L // the last shown message time
    private lateinit var greeting: MessageDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAiChatBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
        // initializing greeting message from resources
        greeting = MessageDB(
            time = System.currentTimeMillis(),
            type = MessageType.IN,
            name = getString(R.string.tokenmetrics_bot),
            message = getString(R.string.ai_greeting),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Show messages saved in db
        viewModel.messages
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { list ->
                // If there's no messages saved the greeting is showed
                if (list.isEmpty()) {
                    viewModel.repository.saveMessage(greeting)
                    showMessage(greeting)
                } else {
                    list.sortedBy { it.time }
                        .filter { it.time > lastShownTime }
                        .forEach { message ->
                            showMessage(message)
                        }
                }
            }
            .addTo(autoDisposable)

        binding.buttonSend.setOnClickListener {
            sendQuestion()
        }

        // Setup question EditText view params
        binding.question.run {
            setImeOptions(EditorInfo.IME_ACTION_SEND); // show Send instead of Enter
            setRawInputType(InputType.TYPE_CLASS_TEXT); // otherwise view isn't multiline
            // listen to "Send" soft key
            setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendQuestion()
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
        }

        return binding.root
    }

    private fun showMessage(message: MessageDB) {
            if (message.type == MessageType.OUT) showQuestion(message)
            else showAnswer(message)
            lastShownTime = message.time
    }

    private fun sendQuestion() {
        hideKeyboard()
        val question = binding.question.text.toString()
        binding.question.text?.clear()

        // If the input is empty do nothing
        if (question.isBlank()) return

        viewModel.repository.saveQuestion(getString(R.string.user), question)

        val aiQuestionMessage = AiQuestionMessage(question)
        val aiQuestion = AiQuestion(arrayListOf(aiQuestionMessage))

        viewModel.remoteApi.askAi(aiQuestion)
            .subscribe {
                if (!it.answer.isNullOrBlank()) {
                    viewModel.repository.saveAnswer(
                        getString(R.string.tokenmetrics_bot),
                        it.answer.toString()
                    )
                } else {
                    viewModel.repository.saveAnswer(
                        getString(R.string.system),
                        getString(R.string.no_answer)
                    )
                }
            }
            .addTo(autoDisposable)
    }

    private fun showQuestion(message: MessageDB) {
        val outBinding = ChatOutItemBinding.inflate(layoutInflater)
        outBinding.message.text = message.message
        outBinding.name.text = message.name
        val format = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.ENGLISH)
        outBinding.time.text = format.format(message.time)

        binding.chatWindow.addView(outBinding.root)
        binding.containerAnswer.post {
            binding.containerAnswer.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun showAnswer(message: MessageDB) {
        val inBinding = ChatInItemBinding.inflate(layoutInflater)
        inBinding.message.text = message.message
        inBinding.name.text = message.name
        val format = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.ENGLISH)
        inBinding.time.text = format.format(message.time)

        binding.chatWindow.addView(inBinding.root)
        binding.containerAnswer.post {
            binding.containerAnswer.fullScroll(View.FOCUS_DOWN)
        }
    }
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}
