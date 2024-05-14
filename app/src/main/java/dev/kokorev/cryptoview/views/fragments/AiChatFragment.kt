package dev.kokorev.cryptoview.views.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private var answers = StringBuilder("Hello, I'm your AI helper. You can make any question about crypto industry and I'll do my best to find an answer... \n")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAiChatBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        showAnswer(answers.toString())
        binding.buttonSend.setOnClickListener {
            val question: String = binding.question.text.toString()
            binding.question.text.clear()
            showQuestion(question)
            val aiQuestionMessage = AiQuestionMessage(question)
            val aiQuestion = AiQuestion(arrayListOf(aiQuestionMessage))
            viewModel.remoteApi.askAi(aiQuestion)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showAnswer(it.answer.toString())
                }, {
                    showAnswer("I have no answer to this question.")
                })
                .addTo(autoDisposable)
            hideKeyboard()
        }
        return binding.root
    }

    private fun showQuestion(text: String) {
        val outBinding = ChatOutItemBinding.inflate(layoutInflater)
        outBinding.incomingMessage.text = text
        binding.chatWindow.addView(outBinding.root)
    }

    private fun showAnswer(text: String) {
        val inBinding = ChatInItemBinding.inflate(layoutInflater)
        inBinding.incomingMessage.text = text
        binding.chatWindow.addView(inBinding.root)
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