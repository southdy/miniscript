/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2017 Thomas Cashman
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
package org.mini2Dx.miniscript.kotlin;

import org.jetbrains.kotlin.cli.common.repl.KotlinJsr223JvmScriptEngineBase.CompiledKotlinScript;
import org.jetbrains.kotlin.ir.expressions.IrConstKind.Char;
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine;
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory;
import org.jetbrains.kotlin.util.KotlinFrontEndException;
import org.mini2Dx.miniscript.core.*;
import org.mini2Dx.miniscript.core.exception.ScriptSkippedException;

/**
 * An implementation of {@link ScriptExecutor} for Kotlin-based scripts
 */
public class KotlinScriptExecutor implements ScriptExecutor<CompiledKotlinScript> {
	private final KotlinScriptExecutorPool executorPool;
	private final KotlinJsr223JvmLocalScriptEngine engine;

	public KotlinScriptExecutor(KotlinScriptExecutorPool executorPool) {
		this.executorPool = executorPool;

		engine = (KotlinJsr223JvmLocalScriptEngine) new KotlinJsr223JvmLocalScriptEngineFactory().getScriptEngine();
	}

	@Override
	public GameScript<CompiledKotlinScript> compile(String script) {
		return new PerThreadGameScript<>(script);
	}

	@Override
	public ScriptExecutionResult execute(int scriptId, GameScript<CompiledKotlinScript> script, ScriptBindings bindings,
			boolean returnResult) throws Exception {
		final PerThreadGameScript<CompiledKotlinScript> threadScript = (PerThreadGameScript<CompiledKotlinScript>) script;

		final KotlinEmbeddedScriptInvoker embeddedScriptInvoker = executorPool.getEmbeddedScriptInvokerPool().allocate();
		embeddedScriptInvoker.setScriptBindings(bindings);
		embeddedScriptInvoker.setScriptExecutor(this);
		embeddedScriptInvoker.setParentScriptId(scriptId);

		for (String variableName : bindings.keySet()) {
			engine.put(variableName, bindings.get(variableName));
		}
		engine.put(ScriptBindings.SCRIPT_PARENT_ID_VAR, -1);
		engine.put(ScriptBindings.SCRIPT_ID_VAR, scriptId);
		engine.put(ScriptBindings.SCRIPT_INVOKE_VAR, embeddedScriptInvoker);

		try {
			engine.eval(threadScript.getContent());
		} catch (Exception e) {
			if(e.getMessage().contains(ScriptSkippedException.class.getName())) {
				throw new ScriptSkippedException();
			} else if(e.getCause() instanceof ScriptSkippedException) {
				throw new ScriptSkippedException();
			} else {
				throw e;
			}
		}

		executorPool.getEmbeddedScriptInvokerPool().release(embeddedScriptInvoker);

		if (!returnResult) {
			return null;
		}

		ScriptExecutionResult executionResult = new ScriptExecutionResult(null);
		for (String variableName : bindings.keySet()) {
			putResult(executionResult, variableName);
		}
		putResult(executionResult, ScriptBindings.SCRIPT_ID_VAR);

		return executionResult;
	}

	@Override
	public void executeEmbedded(int parentScriptId, int scriptId, GameScript<CompiledKotlinScript> script,
								EmbeddedScriptInvoker embeddedScriptInvoker, ScriptBindings bindings) throws Exception {
		throw new RuntimeException("Embedded synchronous script invokes not supported in Kotlin.");
/*		PerThreadGameScript<CompiledKotlinScript> threadScript = (PerThreadGameScript<CompiledKotlinScript>) script;
		engine.put(ScriptBindings.SCRIPT_PARENT_ID_VAR, parentScriptId);
		engine.put(ScriptBindings.SCRIPT_ID_VAR, scriptId);
		embeddedScriptInvoker.setParentScriptId(scriptId);

		try {
			engine.eval(threadScript.getContent());
		} catch (Exception e) {
			if(e.getMessage().contains(ScriptSkippedException.class.getName())) {
				throw new ScriptSkippedException();
			} else if(e.getCause() instanceof ScriptSkippedException) {
				throw new ScriptSkippedException();
			} else {
				throw e;
			}
		}

		for (String variableName : bindings.keySet()) {
			try {
				bindings.put(variableName, engine.eval(variableName));
			} catch (Exception e) {
				if(e.getMessage().contains("unresolved reference")) {
					bindings.remove(variableName);
				} else {
					e.printStackTrace();
				}
			}
		}

		engine.put(ScriptBindings.SCRIPT_ID_VAR, parentScriptId);
		embeddedScriptInvoker.setParentScriptId(parentScriptId);*/
	}

	private void putResult(ScriptExecutionResult executionResult, String variableName) {
		try {
			executionResult.put(variableName, engine.eval(variableName));
		} catch (Exception e) {
			if(e.getMessage().contains("unresolved reference")) {
				executionResult.remove(variableName);
			} else {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void release() {
		executorPool.release(this);
	}

	private String getExpectedType(String variableName, ScriptBindings bindings) {
		Object result = bindings.get(variableName);
		if (result instanceof Boolean) {
			return "Boolean";
		} else if (result instanceof Character) {
			return "Char";
		} else if (result instanceof Double) {
			return "Double";
		} else if (result instanceof Float) {
			return "Float";
		} else if (result instanceof Integer) {
			return "Int";
		} else if (result instanceof Long) {
			return "Long";
		} else if (result instanceof Short) {
			return "Short";
		} else if (result instanceof String) {
			return "String";
		} else {
			return result.getClass().getName();
		}
	}
}
