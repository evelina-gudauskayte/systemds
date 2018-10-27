/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysml.test.integration.functions.reorg;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.tugraz.sysds.api.DMLScript;
import org.tugraz.sysds.api.DMLScript.RUNTIME_PLATFORM;
import org.tugraz.sysds.lops.LopProperties.ExecType;
import org.tugraz.sysds.runtime.instructions.Instruction;
import org.tugraz.sysds.runtime.matrix.data.MatrixValue.CellIndex;
import org.tugraz.sysds.utils.Statistics;
import org.apache.sysml.test.integration.AutomatedTestBase;
import org.apache.sysml.test.integration.TestConfiguration;
import org.apache.sysml.test.utils.TestUtils;


public class FullReverseTest extends AutomatedTestBase 
{
	private final static String TEST_NAME1 = "Reverse1";
	private final static String TEST_NAME2 = "Reverse2";
	
	private final static String TEST_DIR = "functions/reorg/";
	private static final String TEST_CLASS_DIR = TEST_DIR + FullReverseTest.class.getSimpleName() + "/";
	
	private final static int rows1 = 2017;
	private final static int cols1 = 1001;	
	private final static double sparsity1 = 0.7;
	private final static double sparsity2 = 0.1;

	@Override
	public void setUp() {
		TestUtils.clearAssertionInformation();
		addTestConfiguration(TEST_NAME1, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME1, new String[]{"B"}));
		addTestConfiguration(TEST_NAME2, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME2, new String[]{"B"}));
	}

	@Test
	public void testReverseVectorDenseCP() {
		runReverseTest(TEST_NAME1, false, false, ExecType.CP);
	}
	
	@Test
	public void testReverseVectorSparseCP() {
		runReverseTest(TEST_NAME1, false, true, ExecType.CP);
	}
	
	@Test
	public void testReverseVectorDenseMR() {
		runReverseTest(TEST_NAME1, false, false, ExecType.MR);
	}
	
	@Test
	public void testReverseVectorSparseMR() {
		runReverseTest(TEST_NAME1, false, true, ExecType.MR);
	}
	
	@Test
	public void testReverseVectorDenseSP() {
		runReverseTest(TEST_NAME1, false, false, ExecType.SPARK);
	}
	
	@Test
	public void testReverseVectorSparseSP() {
		runReverseTest(TEST_NAME1, false, true, ExecType.SPARK);
	}
	
	@Test
	public void testReverseMatrixDenseCP() {
		runReverseTest(TEST_NAME1, true, false, ExecType.CP);
	}
	
	@Test
	public void testReverseMatrixSparseCP() {
		runReverseTest(TEST_NAME1, true, true, ExecType.CP);
	}
	
	@Test
	public void testReverseMatrixDenseMR() {
		runReverseTest(TEST_NAME1, true, false, ExecType.MR);
	}
	
	@Test
	public void testReverseMatrixSparseMR() {
		runReverseTest(TEST_NAME1, true, true, ExecType.MR);
	}
	
	@Test
	public void testReverseMatrixDenseSP() {
		runReverseTest(TEST_NAME1, true, false, ExecType.SPARK);
	}
	
	@Test
	public void testReverseMatrixSparseSP() {
		runReverseTest(TEST_NAME1, true, true, ExecType.SPARK);
	}	

	@Test
	public void testReverseVectorDenseRewriteCP() {
		runReverseTest(TEST_NAME2, false, false, ExecType.CP);
	}
	
	@Test
	public void testReverseMatrixDenseRewriteCP() {
		runReverseTest(TEST_NAME2, true, false, ExecType.CP);
	}	

	
	/**
	 * 
	 * @param sparseM1
	 * @param sparseM2
	 * @param instType
	 */
	private void runReverseTest(String testname, boolean matrix, boolean sparse, ExecType instType)
	{
		//rtplatform for MR
		RUNTIME_PLATFORM platformOld = rtplatform;
		switch( instType ){
			case MR: rtplatform = RUNTIME_PLATFORM.HADOOP; break;
			case SPARK: rtplatform = RUNTIME_PLATFORM.SPARK; break;
			default: rtplatform = RUNTIME_PLATFORM.HYBRID; break;
		}
		boolean sparkConfigOld = DMLScript.USE_LOCAL_SPARK_CONFIG;
		if( rtplatform == RUNTIME_PLATFORM.SPARK )
			DMLScript.USE_LOCAL_SPARK_CONFIG = true;
		
		String TEST_NAME = testname;
		
		try
		{
			int cols = matrix ? cols1 : 1;
			double sparsity = sparse ? sparsity2 : sparsity1;
			getAndLoadTestConfiguration(TEST_NAME);
			
			/* This is for running the junit test the new way, i.e., construct the arguments directly */
			String HOME = SCRIPT_DIR + TEST_DIR;
			fullDMLScriptName = HOME + TEST_NAME + ".dml";
			programArgs = new String[]{"-stats","-explain","-args", input("A"), output("B") };
			
			fullRScriptName = HOME + TEST_NAME + ".R";
			rCmd = "Rscript" + " " + fullRScriptName + " " + inputDir() + " " + expectedDir();
	
			//generate actual dataset 
			double[][] A = getRandomMatrix(rows1, cols, -1, 1, sparsity, 7); 
			writeInputMatrixWithMTD("A", A, true);
	
			runTest(true, false, null, -1); 		
			runRScript(true); 
		
			//compare matrices 
			HashMap<CellIndex, Double> dmlfile = readDMLMatrixFromHDFS("B");
			HashMap<CellIndex, Double> rfile  = readRMatrixFromFS("B");
			TestUtils.compareMatrices(dmlfile, rfile, 0, "Stat-DML", "Stat-R");
			
			//check generated opcode
			if( instType == ExecType.CP )
				Assert.assertTrue("Missing opcode: rev", Statistics.getCPHeavyHitterOpCodes().contains("rev"));
			else if ( instType == ExecType.SPARK )
				Assert.assertTrue("Missing opcode: "+Instruction.SP_INST_PREFIX+"rev", Statistics.getCPHeavyHitterOpCodes().contains(Instruction.SP_INST_PREFIX+"rev"));	
		}
		finally
		{
			//reset flags
			rtplatform = platformOld;
			DMLScript.USE_LOCAL_SPARK_CONFIG = sparkConfigOld;
		}
	}
	
		
}