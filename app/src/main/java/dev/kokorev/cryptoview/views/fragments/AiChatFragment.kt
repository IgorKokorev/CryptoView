package dev.kokorev.cryptoview.views.fragments

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.coinpaprika.apiclient.entity.MessageDB
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.vertexai.type.GenerateContentResponse
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.FragmentAiChatBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.AiChatViewModel
import dev.kokorev.cryptoview.views.rvadapters.ChatAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job


// Fragment to chat with TokenMetrics AI chat bot
class AiChatFragment : Fragment() {
    private val viewModel: AiChatViewModel by viewModels()
    private lateinit var binding: FragmentAiChatBinding
    private val autoDisposable = AutoDisposable()
    private lateinit var chatAdapter: ChatAdapter
    private var messages: List<MessageDB> = listOf()
        set(value) {
            if (field == value) return
            field = value
            chatAdapter.addItems(field)
        }
    private val scope = CoroutineScope(Job())
    private var clipboard: ClipboardManager? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAiChatBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
        
        clipboard = getSystemService(binding.root.context, ClipboardManager::class.java)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        getMessagesFromDB()
        initRecycler()
        setupQuestionEditText()
        binding.buttonSend.setOnClickListener {
            sendQuestion()
        }
        
        return binding.root
    }
    
    // Initialize chat RV
    private fun initRecycler() {
        chatAdapter = ChatAdapter(
            object : ChatAdapter.OnItemClickListener {
                override fun click(messageDB: MessageDB) { // On item click
                    val clip = ClipData.newPlainText(getString(R.string.app_name), messageDB.message)
                    clipboard?.setPrimaryClip(clip)
                    /*Snackbar.make(binding.root, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(resources.getColor(R.color.base3, null))
                        .setTextColor(resources.getColor(R.color.textColor, null))
                        .show()*/
                }
            }).apply {
            addItems(messages)
        }
        binding.chatRv.adapter = chatAdapter
    }
    
    // Setup question edit text view
    private fun setupQuestionEditText() {
        binding.question.run {
            setImeOptions(EditorInfo.IME_ACTION_SEND) // show Send instead of Enter
            setRawInputType(InputType.TYPE_CLASS_TEXT) // otherwise view isn't multiline
            // listen to "Send" soft key
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendQuestion()
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
        }
    }
    
    // get messages from local db and send them to the RV adapter
    private fun getMessagesFromDB() {
        viewModel.messages
            .subscribe(
                { list ->
                    // If there's no messages saved the greeting is showed
                    if (list.isEmpty()) {
                        viewModel.saveAnswer(getString(R.string.tokenmetrics_bot), getString(R.string.ai_greeting))
                    } else {
                        messages = list.sortedByDescending { it.time }
                        binding.chatRv.layoutManager?.scrollToPosition(0) // on every new message
                    }
                },
                {
                    Log.d(this.javaClass.simpleName, "Error getting messages from local db: ${it.localizedMessage}, ${it.stackTrace}")
                }
            )
            .addTo(autoDisposable)
    }
    
    private fun sendQuestion() {
        hideKeyboard()
        val question = binding.question.text.toString()
        binding.question.text?.clear()
        // If the input is empty do nothing
        if (question.isBlank()) return
        
        viewModel.saveQuestion(getString(R.string.user), question)
        
        var toAskGemini = true
        viewModel.askTokenMetricsAi(question)
            .doOnSuccess {
                if (!it.answer.isNullOrBlank()) {
                    if (it.success ?: false) {
                        viewModel.saveAnswer(
                            getString(R.string.tokenmetrics_bot),
                            it.answer.toString()
                        )
                        toAskGemini = false
                    }
                }
            }
            .doOnError(::errorAnswer)
            .doAfterTerminate {
                if (toAskGemini) {
                    letsAskGemini()
                    viewModel.askGemini(question)
                        .doOnSuccess(::saveAnswer)
                        .doOnError(::errorAnswer)
                        .doOnComplete(::emptyAnswer)
                        .subscribe()
                        .addTo(autoDisposable)
                }
            }
            .subscribe()
            .addTo(autoDisposable)
    }
    
    private fun letsAskGemini() {
        viewModel.saveAnswer(
            getString(R.string.tokenmetrics_bot),
            getString(R.string.lets_ask_gemini)
        )
    }
    
    private fun saveAnswer(response: GenerateContentResponse) {
        viewModel.saveAnswer(
            getString(R.string.gemini),
            response.text ?: getString(R.string.no_answer)
        )
    }
    
    private fun errorAnswer(it: Throwable) {
        viewModel.saveAnswer(
            getString(R.string.error),
            getString(R.string.something_went_wrong_tm_answer, it.localizedMessage)
        )
    }
    
    private fun emptyAnswer() {
        viewModel.saveAnswer(
            getString(R.string.error),
            getString(R.string.empty_ai_response)
        )
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
