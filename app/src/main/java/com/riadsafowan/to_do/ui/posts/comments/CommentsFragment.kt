package com.riadsafowan.to_do.ui.posts.comments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.riadsafowan.to_do.R
import com.riadsafowan.to_do.data.model.posts.comment.CommentRequest
import com.riadsafowan.to_do.data.model.posts.comment.CommentResponse
import com.riadsafowan.to_do.databinding.FragmentCommentsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommentsFragment : Fragment(R.layout.fragment_comments),
    CommentsAdapter.OnItemClickedListener {
    private lateinit var binding: FragmentCommentsBinding
    private val viewModel: CommentsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val postId = arguments?.getInt("postId")

        postId.let {
            viewModel.getComments(postId!!)
        }

        val commentAdapter = CommentsAdapter(this)
        binding.apply {
            recyclerViewPosts.apply {
                adapter = commentAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
//            swipe.setOnRefreshListener {
//                viewModel.getComments(postId!!)
//            }
            send.setOnClickListener {
                val text = textBox.text.toString().trim()
                if (text.isNotEmpty()) {
                    viewModel.createComment(postId!!, CommentRequest(text))
                    binding.textBox.text = null
                } else
                    Toast.makeText(requireContext(), "Comment box is empty", Toast.LENGTH_SHORT)
                        .show()
            }
        }
        viewModel.comments.observe(viewLifecycleOwner) {
            commentAdapter.submitList(it)
//            binding.swipe.isRefreshing = false
        }
    }

    override fun onItemClicked(comment: CommentResponse) {

    }

    override fun onLikeBtnClicked(postId: Int) {
    }

}