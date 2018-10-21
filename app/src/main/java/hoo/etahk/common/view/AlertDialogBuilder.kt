package hoo.etahk.common.view

import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import hoo.etahk.R

class AlertDialogBuilder(context: Context, @StyleRes themeResId: Int = R.style.AppTheme_Dialog_Large) : AlertDialog.Builder(context, themeResId)