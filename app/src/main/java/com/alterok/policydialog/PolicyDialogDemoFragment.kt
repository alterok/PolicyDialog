package com.alterok.policydialog

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.alterok.policydialog.databinding.FragmentPolicyDialogDemoBinding
import com.alterok.policydialoglib.PolicyDialog

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
private const val TAG = "PolicyDialogDemoFrag"

class PolicyDialogDemoFragment : Fragment() {

    private var _binding: FragmentPolicyDialogDemoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var policyDialogBuilder: PolicyDialog.Builder
    private lateinit var policyDialog: PolicyDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPolicyDialogDemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        policyDialogBuilder = PolicyDialog.Builder(requireActivity() as AppCompatActivity)
            .apply {
                //Dialog Background
                backgroundColor = Color.WHITE

                //Dialog Title
                dialogTitleTextColor = Color.BLACK

                //Button Style
                isOutlineButtonStyle = false

                //Cancel Button
                cancelTextColor = Color.GRAY

                //Accept Button
                acceptTextColor = Color.WHITE
                acceptButtonColor = Color.DKGRAY


                //Terms and Polices
                policyLineTextColor = Color.DKGRAY
                termsSubTextColor = Color.DKGRAY

                //URL highlight
                linkTextColor = Color.BLUE
            }
            .addPolicyLine("This application uses a unique user identifier for advertising purposes, it is shared with third-party companies.")
            .addPolicyLine("This application sends error reports, installation and send it to a server of the Fabric.io company to analyze and process it.")
            .addPolicyLine("This application requires internet access and must collect the following information: Installed applications and history of installed applications, ip address, unique installation id, token to send notifications, version of the application, time zone and information about the language of the device.")
            .addPolicyLine("All details about the use of data are available in our Privacy Policies, as well as all Terms of Service links below.")

        policyDialog = createNewDialogFromBuilder(policyDialogBuilder)
        policyDialog.show()

        binding.buttonFirst.setOnClickListener {
            if (policyDialog.hasAccepted) {
                policyDialog.reset()
            } else {
                if (policyDialog.isDestroyed)
                    policyDialog = createNewDialogFromBuilder(policyDialogBuilder)

                policyDialog.show()
            }

            setPolicyStatusText()
        }

        setPolicyStatusText()
    }

    private fun createNewDialogFromBuilder(policyDialogBuilder: PolicyDialog.Builder): PolicyDialog {
        return policyDialogBuilder.create()
            .apply {
                addPolicyDialogListener(object : PolicyDialog.OnDialogButtonListener {
                    override fun onAccept(fromUser: Boolean) {
                        Log.d(
                            TAG, "onAccept() called with: fromUser = $fromUser"
                        )
                        setPolicyStatusText()
                    }

                    override fun onCancel() {
                        Log.d(TAG, "onCancel() called")
                        setPolicyStatusText()

                        //Exit app if declined to accept the terms
//                        activity?.finish()
                    }
                })
            }
    }

    private fun setPolicyStatusText() {
        binding.textviewFirst.text = if (policyDialog.hasAccepted) {
            binding.buttonFirst.text = getString(R.string.reset)
            "Terms Accepted"
        } else {
            binding.buttonFirst.text = getString(R.string.ask_again)
            "Terms not accepted yet."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        policyDialog.destroy()
    }
}