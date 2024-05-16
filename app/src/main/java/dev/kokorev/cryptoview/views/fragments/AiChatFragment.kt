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


class AiChatFragment : Fragment() {
    private val viewModel: AiChatViewModel by viewModels()
    private lateinit var binding: FragmentAiChatBinding
    private val autoDisposable = AutoDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAiChatBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        showAnswer(getString(dev.kokorev.cryptoview.R.string.ai_greeting))

        binding.buttonSend.setOnClickListener {
            sendQuestion()
        }
        binding.question.setInputType(InputType.TYPE_CLASS_TEXT)
        binding.question.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendQuestion()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        return binding.root
    }

    private fun sendQuestion() {
        val question: String = binding.question.text.toString()
        binding.question.text?.clear()
        showQuestion(question)
        val aiQuestionMessage = AiQuestionMessage(question)
        val aiQuestion = AiQuestion(arrayListOf(aiQuestionMessage))
        viewModel.remoteApi.askAi(aiQuestion)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showAnswer(it.answer.toString())
            }
            .addTo(autoDisposable)
        hideKeyboard()
    }

    private fun showQuestion(text: String) {
        val outBinding = ChatOutItemBinding.inflate(layoutInflater)
        outBinding.incomingMessage.text = text
        viewModel.repository.saveQuestion(text)
        binding.chatWindow.addView(outBinding.root)
        binding.containerAnswer.post {
                binding.containerAnswer.fullScroll(View.FOCUS_DOWN)
            }
    }

    private fun showAnswer(text: String) {
        val inBinding = ChatInItemBinding.inflate(layoutInflater)
        inBinding.incomingMessage.text = text
        viewModel.repository.saveAnswer(text)
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
