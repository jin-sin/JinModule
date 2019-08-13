# What is AndroidModule?

Public Appknot Android Module
<br/><br/>


## Setup


### Gradle

Edit `root/app/build.gradle` like below.

#### AndroidX
```gradle
dependencies {
    implementation 'com.github.appknot:AndroidModule:0.8'
}
```
<br/><br/>

## How to use

### AndroidX
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