package hoo.etahk.view.test

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mcxiaoke.koi.ext.find
import hoo.etahk.R
import kotlinx.android.synthetic.main.fragment_simple.view.*


class SimpleFragment : Fragment() {

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(sectionNumber: Int): SimpleFragment {
            val fragment = SimpleFragment()
            val args = Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_simple, container, false)
        rootView.section_label.text = getString(R.string.section_format, arguments!!.getInt(ARG_SECTION_NUMBER))
        if (arguments!!.getInt(ARG_SECTION_NUMBER) == 1) {
            //(activity as BusSearchActivity).animateAppAndStatusBar()
        }
        return rootView
    }

    fun t1() {
        val colorFrom = Color.parseColor("#FF0000")
        val colorTo = Color.parseColor("#00FF00")
        val colorStatusFrom = Color.parseColor("#FF0000")
        val colorStatusTo = Color.parseColor("#00FF00")
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        val colorStatusAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorStatusFrom, colorStatusTo)

        colorAnimation.addUpdateListener { animator -> activity!!.find<Toolbar>(R.id.toolbar).setBackgroundColor(animator.animatedValue as Int) }

        colorStatusAnimation.addUpdateListener { animator ->
            activity!!.window.statusBarColor = animator.animatedValue as Int
        }

        colorAnimation.duration = 1300
        colorAnimation.startDelay = 0
        colorAnimation.start()
        colorStatusAnimation.duration = 1300
        colorStatusAnimation.startDelay = 0
        colorStatusAnimation.start()
    }

}