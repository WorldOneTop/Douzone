package com.worldonetop.portfolio.util

import androidx.lifecycle.*
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit


// 중복 클릭 방지
class DoubleClick(lifecycle: Lifecycle, private val delay: Long = 1000L) : DefaultLifecycleObserver {
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var clickSubject: BehaviorSubject<(() -> Unit)>

    init {
        lifecycle.addObserver(this)
    }


    fun run(hof: () -> Unit) {
        clickSubject.onNext(hof)
    }

    // 처음 누른 시점부터 delay 동안 클릭이벤트를 무시하며 run 이벤트 발생시킴
    override fun onCreate(owner: LifecycleOwner) {
        compositeDisposable = CompositeDisposable()
        clickSubject = BehaviorSubject.create()
        compositeDisposable.add(
            clickSubject.throttleFirst(delay, TimeUnit.MILLISECONDS)
                .subscribe {
                    it.invoke()
                })
    }

    // 할당 해제
    override fun onDestroy(owner: LifecycleOwner) {
        compositeDisposable.dispose()
    }
}