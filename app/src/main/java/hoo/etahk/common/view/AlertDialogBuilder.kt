package hoo.etahk.common.view

import android.content.Context
import android.support.annotation.StyleRes
import android.support.v7.app.AlertDialog
import hoo.etahk.R

class AlertDialogBuilder(context: Context, @StyleRes themeResId: Int = R.style.AppTheme_Dialog_Large) : AlertDialog.Builder(context, themeResId)