package com.babeeta.hudee.test.parallel;

public abstract class AbstractTunnelState
  implements TunnelState
{
  protected AbstractTunnelState nextState = null;

  public final AbstractTunnelState setNextState(AbstractTunnelState nextState)
  {
    this.nextState = nextState;
    return this.nextState;
  }
}