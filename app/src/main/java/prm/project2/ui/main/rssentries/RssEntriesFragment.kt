package prm.project2.ui.main.rssentries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import prm.project2.databinding.FragmentRssEntriesBinding

abstract class RssEntriesFragment : Fragment() {

    protected abstract val viewModel: RssEntriesViewModel
    private lateinit var binding: FragmentRssEntriesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRssEntriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rssEntriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = RssEntriesRecyclerViewAdapter(viewModel, viewLifecycleOwner)
        }
    }
}
