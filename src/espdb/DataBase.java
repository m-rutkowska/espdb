package espdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {
	Connection connection = null;
	
	public static class Result{
		private Statement statement;
		private boolean hasResult;
		private int updateCnt=-1;
		
		public ResultSet getResultSet() throws SQLException{
			return statement.getResultSet();
		}
		public boolean hasResult(){
			return hasResult;
		}
		public boolean hasMore(){
			return hasResult || updateCnt>=0;
		}
	}
	
	
	public DataBase(String dbName) throws SQLException{
		connection = DriverManager.getConnection(dbName);
	}
	public Result query (String q) throws SQLException{
		Result r = new Result();
		r.statement = connection.createStatement();
		r.hasResult = r.statement.execute(q);
		r.updateCnt = r.statement.getUpdateCount();
		return r;
	}

}
