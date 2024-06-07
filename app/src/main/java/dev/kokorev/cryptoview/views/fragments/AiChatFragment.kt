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
import dev.kokorev.cryptoview.databinding.FragmentAiChatBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.AiChatViewModel
import dev.kokorev.cryptoview.views.rvadapters.ChatAdapter
import dev.kokorev.token_metrics_api.entity.AiQuestion
import dev.kokorev.token_metrics_api.entity.AiQuestionMessage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

// Fragment to chat with TokenMetrics AI chat bot
class AiChatFragment : Fragment() {
    private val viewModel: AiChatViewModel by viewModels()
    private lateinit var binding: FragmentAiChatBinding
    private val autoDisposable = AutoDisposable()
    private lateinit var greeting: MessageDB
    private lateinit var chatAdapter: ChatAdapter
    private var messages: List<MessageDB> = listOf()
        set(value) {
            if (field == value) return
            field = value
            chatAdapter.addItems(field)
        }
    
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
        getMessagesFromDB()
        initRecycler()
        // Setup question EditText view params
        setupQuestionEditText()
        binding.buttonSend.setOnClickListener {
            sendQuestion()
        }
        
        return binding.root
    }
    
    private fun initRecycler() {
        chatAdapter = ChatAdapter(
            object : ChatAdapter.OnItemClickListener {
                override fun click(messageDB: MessageDB) { // On item click
                
                }
            }).apply {
            addItems(messages)
        }
        binding.chatRv.adapter = chatAdapter
    }
    
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
    
    private fun getMessagesFromDB() {
        viewModel.messages
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { list ->
                // If there's no messages saved the greeting is showed
                if (list.isEmpty()) {
                    viewModel.repository.saveMessage(greeting)
                } else {
                    messages = list.sortedByDescending { it.time }
                    binding.chatRv.layoutManager?.scrollToPosition(0)
                }
            }
            .addTo(autoDisposable)
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
            .subscribe({
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
//                binding.chatRv.layoutManager?.scrollToPosition(0)
            },
                {
                    viewModel.repository.saveAnswer("Error", "Something went wrong, TM answer: ${it.localizedMessage}")
                }
            )
            .addTo(autoDisposable)
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
