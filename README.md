# PolicyDialog [![](https://jitpack.io/v/alterok/PolicyDialog.svg)](https://jitpack.io/#alterok/PolicyDialog)
This library is based on [Android-Privacy-Policy](https://github.com/khirr/Android-Privacy-Policy) by [khirr](https://github.com/khirr).

## Dependency
#### Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
#### Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.alterok:PolicyDialog:2.0.0'
	}
  
## Create PolicyDialogBuilder
  
    val policyDialogBuilder = PolicyDialog.Builder(appCompatActivity)
            .apply {
                //Dialog Background
                backgroundColor = Color.WHITE

                //Dialog Title
                dialogTitleTextColor = Color.BLACK

                //Button Style
                isOutlineButtonStyle = false

                //Cancel Button
                cancelTextColor = Color.DKGRAY
                cancelButtonText = getString(android.R.string.cancel)

                //Accept Button
                acceptTextColor = Color.WHITE
                acceptButtonColor = Color.BLACK

                //Terms and Polices
                policyLineTextColor = Color.DKGRAY
                termsSubTextColor = Color.DKGRAY

                //URL highlight
                linkTextColor = Color.BLUE

                //option style
                optionTextColor = Color.DKGRAY
                optionCheckMarkColor = Color.DKGRAY

                //EU GDPR, show PolicyDialog in EU only
                showInEUOnly = false
            }
            
            //Policy Preview Lines
            .addPolicyLine("This application uses a unique user identifier for advertising purposes, it is shared with third-party companies.")
            .addPolicyLine("This application sends error reports, installation and send it to crashlytics service to analyze and process it.")
            .addPolicyLine("All details about the use of data are available in our Privacy Policies, as well as all Terms of Service links below.")

            // Show additional checkable options to ask permission
            .addOption(
                DialogOption(
                    "Allow app to collect analytics to improve the app. (Option for EU Only)",
                    defaultValue = true,
                    forEUOnly = true
                )
            )
            .addOption(
                DialogOption(
                    "Allow app to collect analytics to improve the app.",
                    defaultValue = true,
                    forEUOnly = false
                )
            )

## Create PolicyDialog

       val policyDialog = policyDialogBuilder.create()
       
       //Policy Dialog Listener
       policyDialog.addPolicyDialogListener(object : PolicyDialog.OnDialogButtonListener {
                    override fun onAccept(fromUser: Boolean) {
                        Log.d(
                            TAG, "onAccept() called with: fromUser = $fromUser"
                        )
                    }

                    override fun onCancel() {
                        Log.d(TAG, "onCancel() called")

                        //Exit app if declined to accept the terms
                        //activity?.finish()
                    }

                    override fun onOptionClicked(optionIndex: Int, isChecked: Boolean) {
                        Log.i(TAG, "onOptionClicked: optionIndex=$optionIndex isChecked=$isChecked")
                    }
                })
                
        //Reset PolicyDialog accepted state
        //policyDialog.reset()
                
       policyDialog.show()
       
   ## PolicyDialog States
    policyDialog.hasAccepted    //true if the PolicyDialog has been accepted via Accepted Button click or if GDPR compliance requested in Non EU countries.
    policyDialog.isDestroyed    //check if the PolicyDialog has been destroyed already, if it is then we need to create a new dialog using the policyDialogBuilder to show again.
