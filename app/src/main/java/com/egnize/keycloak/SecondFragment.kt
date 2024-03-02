package com.egnize.keycloak

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.egnize.keycloak.auth.AuthAuthenticator
import com.egnize.keycloak.databinding.FragmentSecondBinding

/**
 * Created by Vinay on 02/03/2024.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var openIdAuth: AuthAuthenticator? = null

    private var resultLauncherLogout =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            openIdAuth?.onActivityResultLogout(result)
        }
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            openIdAuth?.onActivityResult(result)
        }

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { openIdAuth?.attachActivity(activity = it) }
        openIdAuth = activity?.let {
            AuthAuthenticator(
                it,
                lifecycleScope,
                resultLauncher = resultLauncher,
                resultLauncherLogout = resultLauncherLogout
            )
        }

        binding.buttonSecond.setOnClickListener {
            clearAuth(openIdAuth)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}