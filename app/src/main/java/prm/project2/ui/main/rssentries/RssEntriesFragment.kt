package prm.project2.ui.main.rssentries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import prm.project2.databinding.FragmentRssEntriesBinding

abstract class RssEntriesFragment : Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentRssEntriesBinding? = null

    protected abstract fun viewModel(): RssEntriesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRssEntriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rssEntriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
        viewModel().entries.observe(viewLifecycleOwner, {
            binding.rssEntriesRecyclerView.apply {
                this.
                adapter = RssEntriesRecyclerViewAdapter(viewModel(), it)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}