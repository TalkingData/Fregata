package fregata.optimize

/**
 * Created by takun on 16/9/19.
 */
trait Minimizer extends Optimizer {
  private[this] var _target : Target = _
  def minimize(target: Target) : this.type = {
    this._target = target
    this
  }
  def target = _target
}
