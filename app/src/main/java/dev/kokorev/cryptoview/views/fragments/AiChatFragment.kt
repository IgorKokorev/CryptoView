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

// Fragment to chat with TokenMetrics AI chat bot
class AiChatFragment : Fragment() {
    private val viewModel: AiChatViewModel by viewModels()
    private lateinit var binding: FragmentAiChatBinding
    private val autoDisposable = AutoDisposable()
    private var lastShownTime = 0L // the last shown message time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAiChatBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Show messages saved in db
        viewModel.messages
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.isEmpty()) {
                    viewModel.repository.saveAnswer(
                        getString(R.string.tokenmetrics_bot),
                        getString(dev.kokorev.cryptoview.R.string.ai_greeting)
                    )
                    lastShownTime = System.currentTimeMillis()
                } else {
                    it.sortedBy { it.time }
                        .forEach { message ->
                            if (message.time > lastShownTime) {
                                if (message.type == MessageType.OUT) showQuestion(message)
                                else showAnswer(message)
                                lastShownTime = message.time
                            }

                        }
                }
            }
            .addTo(autoDisposable)


        binding.buttonSend.setOnClickListener {
            sendQuestion()
        }

        binding.question.run {
            setImeOptions(EditorInfo.IME_ACTION_SEND);
            setRawInputType(InputType.TYPE_CLASS_TEXT);
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

    private fun sendQuestion() {
        hideKeyboard()
        val question = binding.question.text.toString()
        binding.question.text?.clear()

        // If the input is empty do nothing
        if (question.isNullOrBlank()) return

        viewModel.repository.saveQuestion(getString(R.string.user), question)

        val aiQuestionMessage = AiQuestionMessage(question)
        val aiQuestion = AiQuestion(arrayListOf(aiQuestionMessage))

        viewModel.progressBarState.onNext(true)

        viewModel.remoteApi.askAi(aiQuestion)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (!it.answer.isNullOrBlank()) {
                    viewModel.repository.saveAnswer(getString(R.string.tokenmetrics_bot), it.answer.toString())
                } else {
                    viewModel.repository.saveAnswer(getString(R.string.system), getString(R.string.no_answer))
                }
                viewModel.progressBarState.onNext(false)

            }
            .addTo(autoDisposable)
    }

    private fun showQuestion(message: MessageDB) {
        val outBinding = ChatOutItemBinding.inflate(layoutInflater)
        outBinding.message.text = message.message
        outBinding.name.text = message.name
        val format = SimpleDateFormat("dd MMMM yyyy hh:mm:ss")
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
        val format = SimpleDateFormat("dd MMMM yyyy hh:mm:ss")
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
