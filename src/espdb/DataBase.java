package espdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {
	private String lastq = null;
	private Connection connection = null;
	private final Object[] emptyArray = {};
	
	public static class Result{
		private Statement statement;
		private boolean hasResult;
		private int updateCnt=-1;
		private int genKey = -1;
		
		public int getgenKey(){
			return genKey;
		}
		
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
	
	public String getLastq (){
		return lastq;
	}
	
	
	
	public DataBase(String dbName) throws SQLException{
		connection = DriverManager.getConnection(dbName);
	}
	public Result query (String q) throws SQLException{
		if(q.isEmpty())return null;
		return query(q, emptyArray);
	}
	public Result query(String q, Object... args) throws SQLException{
		if(q.isEmpty()) return null;
		lastq = q;
		Result r = new Result();
		int opt = Statement.NO_GENERATED_KEYS;
		if(q.toUpperCase().contains("INSERT"))
			opt = Statement.RETURN_GENERATED_KEYS;
		
		PreparedStatement pstmt = connection.prepareStatement(q, opt);
		r.statement = pstmt;
		int i = 1;
		for(Object a:args){
			pstmt.setObject(i++, a);
		}
		r.hasResult = pstmt.execute();
		r.updateCnt = r.statement.getUpdateCount();
		
		if(r.updateCnt > 0){
			ResultSet k = r.statement.getGeneratedKeys();
			if(k.next()){
				r.genKey = k.getInt(1);
			}
		}
		return r;
	}

}
