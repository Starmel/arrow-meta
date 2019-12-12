package arrow.meta.quotes.expression

import arrow.meta.quotes.Scope
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression

/**
 * <code>"""if ($condition) $then else $`else`""".`if`</code>
 *
 * A template destructuring [Scope] for a [KtIfExpression].
 *
 * ```kotlin:ank:silent
 * import arrow.meta.Meta
 * import arrow.meta.Plugin
 * import arrow.meta.invoke
 * import arrow.meta.quotes.Transform
 * import arrow.meta.quotes.ifExpression
 *
 * val Meta.reformatIf: Plugin
 *    get() =
 *      "Reformat If Expression" {
 *        meta(
 *          ifExpression({ true }) { expression ->
 *            Transform.replace(
 *              newDeclaration = when {
 *                `else`.value == null -> """if ($condition) $then""".`if`
 *                else -> """if ($condition) $then else $`else`""".`if`
 *              }
 *            )
 *          }
 *        )
 *      }
 *```
 */
class IfExpression(
  override val value: KtIfExpression,
  val condition: Scope<KtExpression> = Scope(value.condition),
  val then: Scope<KtExpression> = Scope(value.then),
  val `else`: Scope<KtExpression> = Scope(value.`else`)
) : Scope<KtIfExpression>(value)