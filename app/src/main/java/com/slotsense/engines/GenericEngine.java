package com.slotsense.engines;

import com.slotsense.core.BaseEngine;
import com.slotsense.steps.*;

import java.util.List;

public class GenericEngine extends BaseEngine {
    public GenericEngine(){
        super(List.of(
                new com.slotsense.steps.PreSpinSetupStep(),
                new com.slotsense.steps.BuildReelBoardStep(),
                new com.slotsense.steps.CountBonusStep(),
                new com.slotsense.steps.EvaluateWinStep()

        ));
    }

    public GenericEngine(List<com.slotsense.core.Step> steps){
        super(steps);
    }
}
