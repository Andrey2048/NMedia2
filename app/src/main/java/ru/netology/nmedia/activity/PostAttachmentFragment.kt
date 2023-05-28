package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentPostAttachmentBinding
import ru.netology.nmedia.util.StringArg

class PostAttachmentFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostAttachmentBinding.inflate(
            inflater,
            container,
            false
        )

        binding.backArrow.setOnClickListener {
            findNavController().navigateUp()
        }

        Glide.with(binding.attachmentImage)
            .load(BuildConfig.BASE_URL + "/media/${arguments?.textArg}")
            .centerCrop()
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.ic_error_100dp)
            .timeout(10_000)
            .into(binding.attachmentImage)


        return binding.root
    }
}