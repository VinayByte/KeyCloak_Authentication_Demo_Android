package com.egnize.keycloak

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.egnize.keycloak.auth.AuthAuthenticator
import com.egnize.keycloak.databinding.FragmentFirstBinding
import com.egnize.keycloak.repository.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Created by Vinay on 02/03/2024.
 *
 */
@AndroidEntryPoint
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var openIdAuth: AuthAuthenticator? = null
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            openIdAuth?.onActivityResult(result)
        }
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        openIdAuth = AuthAuthenticator(
            context = context,
            scope = lifecycleScope,
            resultLauncher = resultLauncher,
            resultLauncherLogout = null
        )
        activity?.let { openIdAuth?.attachActivity(activity = it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUserInfo()
        binding.buttonFirst.setOnClickListener {
            doAuth(openIdAuth, viewModel)
        }
    }

    private fun observeUserInfo() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userInfo.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                        }

                        is UiState.Success -> {
                            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                        }

                        is UiState.Error -> {
                        }
                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}