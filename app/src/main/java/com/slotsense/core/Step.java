package com.slotsense.core;

import com.slotsense.core.SpinContext;

public interface Step {
    void run(SpinContext ctx);
}
