package arrow.meta.phases

import arrow.meta.phases.analysis.ElementScope
import arrow.meta.plugins.proofs.phases.Proof
import arrow.meta.quotes.QuoteDefinition
import arrow.meta.quotes.Scope
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import java.util.concurrent.atomic.AtomicBoolean
import arrow.meta.plugins.proofs.phases.proofs as tp

/**
 * The Compiler Context represents the environment received by all plugins.
 * The Compiler Context will get more services as they become relevant overtime to the development of compiler plugins.
 */
open class CompilerContext(
  open val project: Project,
  val messageCollector: MessageCollector? = null,
  val scope: ElementScope = ElementScope.default(project),
  val ktPsiElementFactory: KtPsiFactory = KtPsiFactory(project, false),
  val eval: (String) -> Any? = {
    KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine.eval(it)
  }
) : ElementScope by scope {
  private var md: ModuleDescriptor? = null
  private var cp: ComponentProvider? = null

  var configuration: CompilerConfiguration? = null

  val quotes: MutableList<QuoteDefinition<out KtElement, out KtElement, out Scope<KtElement>>> =
    mutableListOf()

  val analysedDescriptors: MutableList<DeclarationDescriptor> = mutableListOf()

  val analysisPhaseWasRewind: AtomicBoolean = AtomicBoolean(false)

  val analysisPhaseCanBeRewind: AtomicBoolean = AtomicBoolean(false)

  val ModuleDescriptor?.proofs: List<Proof>
    get() = this?.tp ?: emptyList()

  var module: ModuleDescriptor?
    get() = md
    set(value) {
      md = value
    }

  var componentProvider: ComponentProvider?
    get() = cp
    set(value) {
      cp = value
    }

  val ctx: CompilerContext = this
}

fun <T> CompilerContext.evaluateDependsOn(
  noRewindablePhase: () -> T,
  rewindablePhase: (Boolean) -> T
): T {
  if (!analysisPhaseCanBeRewind.get()) return noRewindablePhase()
  return rewindablePhase(analysisPhaseWasRewind.get())
}
