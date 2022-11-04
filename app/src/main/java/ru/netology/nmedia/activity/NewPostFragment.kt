package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
//import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel
import com.github.dhaval2404.imagepicker.ImagePicker

class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private val photoLauncher = registerForActivityResult(ActivityResultContracts
            .StartActivityForResult()
    ) {
        when (it.resultCode) {

            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), "Image pick error", Toast.LENGTH_SHORT)
            }
            else -> {
                val uri = it.data?.data ?: return@registerForActivityResult
                viewModel.changePhoto(uri)
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.create_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        viewModel.changeContent(binding.edit.text.toString())
                        viewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
                        true
                    }
                    else -> false
                }


        }, viewLifecycleOwner)

        binding.takePhoto.setOnClickListener {

            ImagePicker.Builder(this)
                .cameraOnly()
                .maxResultSize(2048, 2048)
                .createIntent {
                    photoLauncher.launch(it)

                }
        }

        binding.pickPhoto.setOnClickListener {

            ImagePicker.Builder(this)
                .galleryOnly()
                .maxResultSize(2048, 2048)
                .createIntent {
                    photoLauncher.launch(it)

                }
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            binding.photoLayout.isVisible = it != null
            binding.photo.setImageURI(it?.uri)

        }
        binding.deletePhoto.setOnClickListener {
            viewModel.changePhoto(null)

        }




        arguments?.textArg
            ?.let(binding.edit::setText)

//        binding.ok.setOnClickListener {
//перенесли в меню
//        }
        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }
        return binding.root
    }
}