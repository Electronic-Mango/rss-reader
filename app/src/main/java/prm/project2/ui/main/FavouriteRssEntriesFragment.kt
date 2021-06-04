package prm.project2.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import prm.project2.databinding.FragmentAllRssEntriesBinding

class FavouriteRssEntriesFragment : Fragment() {

    private val pageViewModel: FavouriteRssEntriesViewModel by activityViewModels()
    private val binding get() = _binding!!
    private var _binding: FragmentAllRssEntriesBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllRssEntriesBinding.inflate(inflater, container, false)
        val root = binding.root
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}