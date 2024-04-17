package dev.kokorev.cryptoview.views.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.kokorev.cryptoview.viewModel.ChartViewModel
import dev.kokorev.cryptoview.R

class ChartFragment : Fragment() {

    companion object {
        fun newInstance() = ChartFragment()
    }

    private val viewModel: ChartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chart, container, false)
    }
}