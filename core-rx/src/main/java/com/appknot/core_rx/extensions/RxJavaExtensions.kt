package com.appknot.core_rx.extensions

import com.appknot.core_rx.rx.AutoClearedDisposable
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 *
 * @author Jin on 2019-06-10
 */

operator fun AutoClearedDisposable.plusAssign(disposable: Disposable)
        = this.add(disposable)

fun runOnIoScheduler(func: () -> Unit): Disposable
        = Completable.fromCallable(func).subscribeOn(Schedulers.io()).subscribe()
