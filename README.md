![Release](https://jitpack.io/v/appknot/AndroidModule.svg)

# What is AndroidModule?

Public Appknot Android Module
<br/><br/>


## Setup


### Gradle
<b>Step1.</b> Add the token to $HOME/.gradle/gradle.properties
```gradle
authToken=jp_6hv7j20dp8s7arjs05c5pivbdl
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
Core
```gradle
dependencies {
	        implementation 'com.github.appknot:AndroidModule:Tag'
	}
```
AkVideoView
```gradle
dependencies {
	        implementation 'com.github.appknot:AndroidModule:akvideoview:Tag'
	}
```
<br/><br/>

## How to use

### AndroidX
#### RetrofitUtil
```kotlin
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
```
#### AKCountDownTimer
```kotlin
val countDownTimer = object : AKCountDownTimer(countDownMaxMills, countDownInterval) {
                                override fun onTick(millisUntilFinished: Long) {
                                    
                                }

                                override fun onFinish() {
                                
                                }
                            }
```

#### AKVideoView
#### AKMediaController

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
```
Int.convertCurrency() - Int 형 숫자를 받아 1000 단위로 , 를 표시한다
Int.stringForTime() - Int 형 milliseconds 를 받아 00:00 형식으로 표시한다
```
