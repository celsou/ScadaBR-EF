/*
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.rt.dataSource.meta;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.IntValuePair;
import com.serotonin.io.StreamUtils;
import com.serotonin.mango.Common;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.IDataPoint;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.AlphanumericValue;
import com.serotonin.mango.rt.dataImage.types.BinaryValue;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataImage.types.MultistateValue;
import com.serotonin.mango.rt.dataImage.types.NumericValue;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 * @author Matthew Lohbihler
 */
public class ScriptExecutor {
	private static final String SCRIPT_PREFIX = "function __scriptExecutor__() {";
	private static final String SCRIPT_SUFFIX = "\r\n}\r\n__scriptExecutor__();";
	private static String SCRIPT_FUNCTION_PATH;
	private static String FUNCTIONS;
	private Log LOG = LogFactory.getLog(ScriptExecutor.class);

	public static void setScriptFunctionPath(String path) {
		SCRIPT_FUNCTION_PATH = path;
	}

	public Map<String, IDataPoint> convertContext(List<IntValuePair> context) throws DataPointStateException {
		RuntimeManager rtm = Common.ctx.getRuntimeManager();

		Map<String, IDataPoint> converted = new HashMap<String, IDataPoint>();
		for (IntValuePair contextEntry : context) {
			DataPointRT point = rtm.getDataPoint(contextEntry.getKey());
			if (point == null)
				throw new DataPointStateException(contextEntry.getKey(),
						new LocalizableMessage("event.meta.pointMissing"));
			converted.put(contextEntry.getValue(), point);
		}

		return converted;
	}

	public PointValueTime execute(String script, Map<String, IDataPoint> context, long runtime, int dataTypeId,
			long timestamp) throws ScriptException, ResultTypeException, UndefinedResultException {

		ensureFunctions();

		Context cx = Context.enter();

		Scriptable scope = null;
		Object result = null;

		scope = cx.initStandardObjects();
		WrapperContext wrapperContext = new WrapperContext(runtime);

		LOG.debug("Got here... ");
		// Add constants to the context.
		scope.put("SECOND", scope, Common.TimePeriods.SECONDS);
		scope.put("MINUTE", scope, Common.TimePeriods.MINUTES);
		scope.put("HOUR", scope, Common.TimePeriods.HOURS);
		scope.put("DAY", scope, Common.TimePeriods.DAYS);
		scope.put("WEEK", scope, Common.TimePeriods.WEEKS);
		scope.put("MONTH", scope, Common.TimePeriods.MONTHS);
		scope.put("YEAR", scope, Common.TimePeriods.YEARS);
		scope.put("CONTEXT", scope, wrapperContext);

		for (String varName : context.keySet()) {
			IDataPoint point = context.get(varName);
			int dt = point.getDataTypeId();

			LOG.debug("Var: " + varName + ", value: "
					+ (point.getPointValue() == null ? "null" : point.getPointValue().toString()));

			if (dt == DataTypes.BINARY) {
				scope.put(varName, scope, new BinaryPointWrapper(point, wrapperContext));
			} else if (dt == DataTypes.MULTISTATE) {
				scope.put(varName, scope, new MultistatePointWrapper(point, wrapperContext));
			} else if (dt == DataTypes.NUMERIC) {
				scope.put(varName, scope, new NumericPointWrapper(point, wrapperContext));
			} else if (dt == DataTypes.ALPHANUMERIC) {
				scope.put(varName, scope, new AlphanumericPointWrapper(point, wrapperContext));
			} else
				throw new ShouldNeverHappenException("Unknown data type id: " + point.getDataTypeId());
		}

		script = SCRIPT_PREFIX + script + SCRIPT_SUFFIX + FUNCTIONS;

		try {
			result = cx.evaluateString(scope, script, "<cmd>", 1, null);
		} catch (Exception e) {
			LOG.error("Error executing script: " + script + "\n" + e.getMessage() + "\n" + e.getStackTrace());
			throw prettyScriptMessage(e);

		} finally {
			Context.exit();
		}

		Object ts = scope.get("TIMESTAMP", scope);
		if (ts != null) {
			if (ts instanceof Number)
				timestamp = ((Number) ts).longValue();
		}

		MangoValue value;
		if (result == null) {
			if (dataTypeId == DataTypes.BINARY)
				value = new BinaryValue(false);
			else if (dataTypeId == DataTypes.MULTISTATE)
				value = new MultistateValue(0);
			else if (dataTypeId == DataTypes.NUMERIC)
				value = new NumericValue(0);
			else if (dataTypeId == DataTypes.ALPHANUMERIC)
				value = new AlphanumericValue("");
			else
				value = null;
		} else if (result instanceof AbstractPointWrapper) {
			value = ((AbstractPointWrapper) result).getValueImpl();
		} else if (dataTypeId == DataTypes.BINARY && result instanceof Boolean) {
			value = new BinaryValue((Boolean) result);
			LOG.debug("Result: " + value.getBooleanValue());
		} else if (dataTypeId == DataTypes.MULTISTATE && result instanceof Number) {
			value = new MultistateValue(((Number) result).intValue());
			LOG.debug("Result: " + value.getIntegerValue());
		} else if (dataTypeId == DataTypes.NUMERIC && result instanceof Number) {
			value = new NumericValue(((Number) result).doubleValue());
			LOG.debug("Result: " + value.getDoubleValue());
		} else if (dataTypeId == DataTypes.ALPHANUMERIC && result instanceof String) {
			value = new AlphanumericValue((String) result);
			LOG.debug("Result: " + value.getStringValue());
		} else if (result instanceof Undefined) {
			throw new UndefinedResultException(new LocalizableMessage("event.script.convertError", result,
					DataTypes.getDataTypeMessage(dataTypeId)));
		} else
			// If not, ditch it.
			throw new ResultTypeException(new LocalizableMessage("event.script.convertError", result,
					DataTypes.getDataTypeMessage(dataTypeId)));

		LOG.debug("Returning: " + value.toString() + "@" + timestamp);
		return new PointValueTime(value, timestamp);
	}

	public static ScriptException prettyScriptMessage(Exception e) {
		String message = e.getMessage();
		int line = -1;

		try {
			line = Integer.parseInt(message.split("#")[1].split(")")[0]);
		} catch (Exception ex) {

		}

		return new ScriptException(message, "script", line, -1);
	}

	private static void ensureFunctions() {
		if (FUNCTIONS == null) {
			if (SCRIPT_FUNCTION_PATH == null)
				SCRIPT_FUNCTION_PATH = Common.ctx.getServletContext()
						.getRealPath("/WEB-INF/scripts/scriptFunctions.js");
			StringWriter sw = new StringWriter();
			FileReader fr = null;
			try {
				fr = new FileReader(SCRIPT_FUNCTION_PATH);
				StreamUtils.transfer(fr, sw);
			} catch (Exception e) {
				throw new ShouldNeverHappenException(e);
			} finally {
				try {
					if (fr != null)
						fr.close();
				} catch (IOException e) {
					// no op
				}
			}
			FUNCTIONS = sw.toString();
		}
	}
}
