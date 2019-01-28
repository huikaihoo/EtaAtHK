package hoo.etahk.common.view

import android.content.Context
import hoo.etahk.R
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.spans.SpannableTheme
import ru.noties.markwon.view.IMarkwonView

class MarkwonViewConfigurationProvider: IMarkwonView.ConfigurationProvider {

    override fun provide(context: Context): SpannableConfiguration {
        val theme = SpannableTheme.builderWithDefaults(context)
            .tableBorderWidth(0)
            .tableCellPadding(4)
            .thematicBreakColor(context.getColor(R.color.colorBlack))
            .headingTextSizeMultipliers(floatArrayOf(1.7F, 1.4F, 1.1F, 1.0F, .83F, .67F))
            .build()

        return SpannableConfiguration.builder(context)
            .softBreakAddsNewLine(true)
            .theme(theme)
            .build()
    }

}
