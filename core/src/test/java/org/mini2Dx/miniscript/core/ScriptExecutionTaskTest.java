/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2016 Thomas Cashman
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.mini2Dx.miniscript.core;

import org.junit.Assert;
import org.junit.Test;
import org.mini2Dx.miniscript.core.dummy.DummyGameScriptingEngine;
import org.mini2Dx.miniscript.core.dummy.DummyScript;
import org.mini2Dx.miniscript.core.dummy.DummyScriptExecutor;

/**
 * Unit tests for {@link ScriptExecutionTask}
 */
public class ScriptExecutionTaskTest {

	@Test
	public void testIdGeneration() {
		ScriptExecutionTask<DummyScript> task1 = new ScriptExecutionTask<DummyScript>(new DummyGameScriptingEngine(),
				new DummyScriptExecutor(null), 0, new GlobalGameScript<DummyScript>(new DummyScript("")),
				new ScriptBindings(), null);
		ScriptExecutionTask<DummyScript> task2 = new ScriptExecutionTask<DummyScript>(new DummyGameScriptingEngine(),
				new DummyScriptExecutor(null), 0, new GlobalGameScript<DummyScript>(new DummyScript("")),
				new ScriptBindings(), null);
		Assert.assertEquals(true, task1.getTaskId() != task2.getTaskId());
	}

	@Test
	public void testRunExecutesScript() {
		DummyScript script = new DummyScript("");
		ScriptExecutionTask<DummyScript> task = new ScriptExecutionTask<DummyScript>(new DummyGameScriptingEngine(),
				new DummyScriptExecutor(null), 0, new GlobalGameScript<DummyScript>(script), new ScriptBindings(), null);
		task.run();
		Assert.assertEquals(true, script.isExecuted());
	}
}
