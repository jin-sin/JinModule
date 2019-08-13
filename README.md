# What is AndroidModule?

Public Appknot Android Module
<br/><br/>


## Setup


### Gradle
<b>Step1.</b> Add the token to $HOME/.gradle/gradle.properties
```gradle
authToken=jp_lcn1h8uu0hvi33rv6sejqshqj7
```

<b>Step2.</b> Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
		repositories {
			...
			maven {
                        url "https://jitpack.io"
                        credentials { username authToken }
            }
		}
	}
```

<b>Step3.</b> Add the dependency
```gradle
dependencies {
	        implementation 'com.github.appknot:AndroidModule:Tag'
	}
```
<br/><br/>

## How to use

### AndroidX
#### RetrofitUtil
````kotlin
setRetrofit("http://appknot.com/api/")
        RetrofitUtil().run {

            call = create(SeotDaApi::class.java).registerToken(
                "jin",
                fbToken
            )

            onSuccess { it ->
                val userIdx = com.appknot.seotda.extensions.toMap(it)["user_idx"].toString()
                viewModel.requestEnterRoom(userIdx)
            }

            onError { _, msg -> showSnackbar(msg) }
            onFailure { showToast("fail") }
            executeWithProgress(this@UserActivity)
        }
````
#### AKCountDownTimer
```kotlin
val countDownTimer = object : AKCountDownTimer(countDownMaxMills, countDownInterval) {
                                override fun onTick(millisUntilFinished: Long) {
                                    
                                }

                                override fun onFinish() {
                                
                                }
                            }
```

#### DialogExtensions
```kotlin
if (isLoading) showLoadingDialog()
else hideLoadingDialog()
```

#### IntentExtensions
```kotlin
startActivity(
                    intentFor<MainActivity>(
                        KEY_USER_LIST to userList
                    )
                        .clearTask()
                        .newTask()
                )
                
                
startActivity<MainActivity>(
                    KEY_USER_LIST to userList
                    )
```

#### SnackbarExtensions
```kotlin
showSnackbar(message)
```

#### ToastExtensions
```kotlin
showToast(message)
```

#### Utils
```kotlin
1000.convertCurrency()
```