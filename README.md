# What is AndroidModule?

Public Appknot Android Module
<br/><br/>


## Setup


### Gradle

Edit `root/app/build.gradle` like below.

#### AndroidX
```
dependencies {
    implementation 'com.github.appknot:AndroidModule:0.6'
}
```
<br/><br/>

## How to use

### AndroidX
```kotlin
val countDownTimer = object : AdvancedCountDownTimer(countDownMaxMills, countDownInterval) {
                                override fun onTick(millisUntilFinished: Long) {
                                    
                                }

                                override fun onFinish() {
                                
                                }
                            }
