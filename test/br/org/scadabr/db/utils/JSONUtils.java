package br.org.scadabr.db.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JSONUtils {
	public static final JSONObject ResultSet2JSONObject(ResultSet rs) {
		JSONObject element = null;
		JSONArray joa = new JSONArray();
		JSONObject jo = new JSONObject();
		int totalLength = 0;
		ResultSetMetaData rsmd = null;
		String columnName = null;
		String columnValue = null;
		try {
			rsmd = rs.getMetaData();
			while (rs.next()) {
				element = new JSONObject();
				for (int i = 0; i < rsmd.getColumnCount(); i++) {
					columnName = rsmd.getColumnName(i + 1);
					columnValue = rs.getString(columnName);
					element.accumulate(columnName, columnValue);
				}
				joa.add(element);
				totalLength++;
			}
			jo.accumulate("result", "success");
			jo.accumulate("rows", totalLength);
			jo.accumulate("data", joa);
		} catch (SQLException e) {
			jo.accumulate("result", "failure");
			jo.accumulate("error", e.getMessage());
		}
		return jo;
	}

	public static final String ResultSet2JSONString(ResultSet rs) {
		return ResultSet2JSONObject(rs).toString();
	}

	public static final String Object2JSONString(Object o) {
		return null;
	}

	public static final String String2JSONString(String str) {
		return "{result:'success',data:" + str + "}";
	}
}