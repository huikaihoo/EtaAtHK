package hoo.etahk.view.dialog

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import com.mcxiaoke.koi.ext.onClick
import com.mcxiaoke.koi.ext.onTextChange
import hoo.etahk.R
import kotlinx.android.synthetic.main.dialog_input.view.*

class InputDialog (val activity: AppCompatActivity) {

    // https://stackoverflow.com/questions/10695103/creating-custom-alertdialog-what-is-the-root-view
    @SuppressLint("InflateParams")
    val view: View = activity.layoutInflater.inflate(R.layout.dialog_input, null)

    private val builder = AlertDialog.Builder(activity)
        .setView(view)
        .setNeutralButton(R.string.clear, null)
        .setNegativeButton(android.R.string.cancel, null)

    fun setTitle(textId: Int): InputDialog {
        builder.setTitle(textId)
        return this
    }

    fun setHint(textId: Int): InputDialog {
        view.input.setHint(textId)
        return this
    }

    fun setText(str: String): InputDialog {
        view.input.setText(str)
        view.input.setAdapter(ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, arrayOf(str)))
        return this
    }

    fun setPositiveButton(textId: Int = android.R.string.ok, listener: DialogInterface.OnClickListener): InputDialog {
        builder.setPositiveButton(textId, listener)
        return this
    }

    fun show() {
        val dialog = builder.show()

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.isEnabled = false

        val neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        neutralButton.onClick { view.input.setText("") }

        view.input.onTextChange { text, start, before, count ->
            positiveButton.isEnabled = text.isNotBlank()
        }
    }
}