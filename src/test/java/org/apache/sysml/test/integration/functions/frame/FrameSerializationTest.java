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

package org.apache.sysml.test.integration.functions.frame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.sysml.test.integration.AutomatedTestBase;
import org.apache.sysml.test.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.tugraz.sysds.parser.Expression.ValueType;
import org.tugraz.sysds.runtime.matrix.data.FrameBlock;
import org.tugraz.sysds.runtime.util.UtilFunctions;

public class FrameSerializationTest extends AutomatedTestBase
{
	private final static int rows = 2791;
	private final static ValueType[] schemaStrings = new ValueType[]{ValueType.STRING, ValueType.STRING, ValueType.STRING};	
	private final static ValueType[] schemaMixed = new ValueType[]{ValueType.STRING, ValueType.DOUBLE, ValueType.INT, ValueType.BOOLEAN};	
	
	private enum SerType {
		WRITABLE_SER,
		JAVA_SER,
	}
	
	@Override
	public void setUp() {
		TestUtils.clearAssertionInformation();
	}

	@Test
	public void testFrameStringsWritable()  {
		runFrameSerializeTest(schemaStrings, SerType.WRITABLE_SER);
	}
	
	@Test
	public void testFrameMixedWritable()  {
		runFrameSerializeTest(schemaMixed, SerType.WRITABLE_SER);
	}
	
	@Test
	public void testFrameStringsJava()  {
		runFrameSerializeTest(schemaStrings, SerType.JAVA_SER);
	}
	
	@Test
	public void testFrameMixedJava()  {
		runFrameSerializeTest(schemaMixed, SerType.JAVA_SER);
	}

	
	/**
	 * 
	 * @param sparseM1
	 * @param sparseM2
	 * @param instType
	 */
	private void runFrameSerializeTest( ValueType[] schema, SerType stype)
	{
		try
		{
			//data generation
			double[][] A = getRandomMatrix(rows, schema.length, -10, 10, 0.9, 8234); 
			
			//init data frame
			FrameBlock frame = new FrameBlock(schema);
			
			//init data frame 
			Object[] row = new Object[schema.length];
			for( int i=0; i<rows; i++ ) {
				for( int j=0; j<schema.length; j++ )
					A[i][j] = UtilFunctions.objectToDouble(schema[j], 
							row[j] = UtilFunctions.doubleToObject(schema[j], A[i][j]));
				frame.appendRow(row);
			}			
			
			//core serialization and deserialization
			if( stype == SerType.WRITABLE_SER ) {
				//serialization
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				frame.write(dos);
				
				//deserialization
				ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
				DataInputStream dis = new DataInputStream(bis);
				frame = new FrameBlock();
				frame.readFields(dis);
			}
			else if( stype == SerType.JAVA_SER ) {
				//serialization
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(frame);
				
				//deserialization
				ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
				ObjectInputStream ois = new ObjectInputStream(bis);
				frame = (FrameBlock) ois.readObject();
			}
			
			//check basic meta data
			if( frame.getNumRows() != rows )
				Assert.fail("Wrong number of rows: "+frame.getNumRows()+", expected: "+rows);
		
			//check correct values			
			for( int i=0; i<rows; i++ ) 
				for( int j=0; j<schema.length; j++ )	{
					double tmp = UtilFunctions.objectToDouble(schema[j], frame.get(i, j));
					if( tmp != A[i][j] )
						Assert.fail("Wrong get value for cell ("+i+","+j+"): "+tmp+", expected: "+A[i][j]);
				}		
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
}
