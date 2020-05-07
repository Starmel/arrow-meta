package arrow.meta.ide.plugins.initial.tooltip

import com.intellij.ide.IdeTooltipManager
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.HintHint
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.Html
import com.intellij.util.ui.JBHtmlEditorKit
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.NonNls
import java.awt.Dimension
import javax.swing.JEditorPane
import javax.swing.text.AbstractDocument
import javax.swing.text.Element
import javax.swing.text.StyleConstants
import javax.swing.text.View
import javax.swing.text.ViewFactory
import javax.swing.text.html.HTML
import javax.swing.text.html.HTMLEditorKit
import kotlin.math.max

object IdeTooltipPaneUtils {

  fun initPane(
    @NonNls html: Html,
    hintHint: HintHint,
    preferredWidth: Int
  ): JEditorPane {
    val prefSize = Ref<Dimension>(null)
    @NonNls var text = prepareHintText(html)
    var isSizeComputed = false
    val pane: JEditorPane = object : JEditorPane() {
      override fun getPreferredSize(): Dimension {
        if (!isSizeComputed && hintHint.isAwtTooltip) {
          isSizeComputed = true

          val fitWidth: Int = (preferredWidth * 0.8).toInt()
          val prefSizeOriginal: Dimension = super.getPreferredSize()
          if (prefSizeOriginal.width > fitWidth) {
            size = Dimension(fitWidth, Integer.MAX_VALUE)
            val fixedWidthSize: Dimension = super.getPreferredSize()
            val minSize: Dimension = super.getMinimumSize()
            prefSize.set(Dimension(max(fitWidth, minSize.width), fixedWidthSize.height))
          } else {
            prefSize.set(Dimension(prefSizeOriginal))
          }
        }
        val s: Dimension = if (prefSize.get() != null) Dimension(prefSize.get()) else super.getPreferredSize()
        if (border != null) {
          JBInsets.addTo(s, border.getBorderInsets(this))
        }
        return s
      }

      override fun setPreferredSize(preferredSize: Dimension) {
        super.setPreferredSize(preferredSize)
        prefSize.set(preferredSize)
      }
    }

    val kit: HTMLEditorKit = object : JBHtmlEditorKit() {
      val factory: HTMLFactory = object : HTMLFactory() {
        override fun create(elem: Element): View {
          val attrs = elem.attributes
          val elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute)
          val o = if (elementName != null) null else attrs.getAttribute(StyleConstants.NameAttribute)
          if (o is HTML.Tag) {
            if (o === HTML.Tag.HR) {
              val view = super.create(elem)
              try {
                val field = view.javaClass.getDeclaredField("size")
                field.isAccessible = true
                field[view] = JBUIScale.scale(1)
                return view
              } catch (ignored: Exception) { //ignore
              }
            }
          }
          return super.create(elem)
        }
      }

      override fun getViewFactory(): ViewFactory {
        return factory
      }
    }
    val editorFontName = EditorColorsManager.getInstance().globalScheme.editorFontName
    if (editorFontName != null) {
      val style = "font-family:\"" + StringUtil.escapeQuotes(editorFontName) + "\";font-size:95%;"
      kit.styleSheet.addRule("pre {$style}")
      text = text.replace("<code>", "<code style='$style'>")
    }
    pane.editorKit = kit
    pane.text = text
    pane.caretPosition = 0
    pane.isEditable = false
    if (hintHint.isOwnBorderAllowed) {
      IdeTooltipManager.setBorder(pane)
      IdeTooltipManager.setColors(pane)
    } else {
      pane.border = null
    }
    if (!hintHint.isAwtTooltip) {
      isSizeComputed = true
    }
    val opaque = hintHint.isOpaqueAllowed
    pane.isOpaque = opaque
    pane.background = hintHint.textBackground

    return pane
  }

  private fun prepareHintText(text: Html): String {
    val htmlBody = UIUtil.getHtmlBody(text)
    return """
      <html>
        <head>
          <link rel="stylesheet" type="text/css" href="https://47deg-academy.s3.amazonaws.com/css/edutools-style-light.css">
        </head>
        <body>
          $htmlBody
        </body>
      </html>
    """.trimIndent()
  }
}