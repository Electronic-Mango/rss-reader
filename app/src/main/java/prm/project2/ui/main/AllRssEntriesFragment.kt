package prm.project2.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import prm.project2.databinding.FragmentAllRssEntriesBinding

class AllRssEntriesFragment : Fragment() {

    private val pageViewModel: AllRssEntriesViewModel by activityViewModels()
    private val binding get() = _binding!!
    private var _binding: FragmentAllRssEntriesBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllRssEntriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pageViewModel.entries.observe(viewLifecycleOwner, {
            binding.allRssEntriesRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = AllRssEntriesRecyclerViewAdapter(pageViewModel, it)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}