package my.id.phyton06.markuscell.commons

import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable

open class RxBaseFragment : Fragment() {

    protected var subscriptions = CompositeDisposable()

    override fun    onResume() {
        super.onResume()
        subscriptions = CompositeDisposable()
    }


    override fun onPause() {
        super.onPause()
        subscriptions.clear()
    }

}