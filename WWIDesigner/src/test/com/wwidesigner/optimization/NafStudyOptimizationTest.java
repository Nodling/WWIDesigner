package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.wwidesigner.gui.NafStudyModel;

public class NafStudyOptimizationTest extends PerturbedInstrumentOptimization
{
	protected String instrumentFile = "com/wwidesigner/optimization/example/Optimized-D-NAF.xml";
	protected String tuningFile = "com/wwidesigner/optimization/example/D_NAF_Tuning.xml";
	protected double initialNorm = 0.0;

	@Test
	public final void optimizationTest() throws Exception
	{
		com.jidesoft.utils.Lm.verifyLicense("Edward Kort", "WWIDesigner",
				"DfuwPRAUR5KQYgePf:CH0LWIp63V8cs2");
		
		// Set up the study model to be tested.

		NafStudyModel myStudy = new NafStudyModel();
		myStudy.getParams().setProperties(22.2, 101.0, 45, 0.000400);
		myStudy.setMinTopHoleRatio(0.4);
		myStudy.setCategorySelection(NafStudyModel.OPTIMIZER_CATEGORY_ID,
				NafStudyModel.GROUP_OPT_SUB_CATEGORY_ID);
		myStudy.setCategorySelection(NafStudyModel.CONSTRAINT_CATEGORY_ID,
				NafStudyModel.HOLE_6_1_25_SPACING_CONS_SUB_CATEGORY_ID);

		setStudyModel(myStudy);
		setTuning(tuningFile);
		setInstrument(instrumentFile, 1.0, 1.0, 1.0);
		testOptimization("Re-optimize the un-perturbed instrument...", 0.01);
		initialNorm = study.getInitialNorm();
		assertEquals("Residual error incorrect", 1.0, study.getResidualErrorRatio(), 0.02);

		perturbInstrument(1.05,1.05,0.95);
		testOptimization("Optimize instrument after 5% stretch...", 0.01);
		assertEquals("Residual error incorrect", 1.0, study.getFinalNorm()/initialNorm, 0.02);

		perturbInstrument(0.95,0.95,1.05);
		testOptimization("Optimize instrument after 5% shrink...", 0.01);
		assertEquals("Residual error incorrect", 1.0, study.getFinalNorm()/initialNorm, 0.02);
	}
}