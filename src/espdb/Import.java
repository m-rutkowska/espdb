package espdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import espdb.DataBase.Result;

public class Import {
	
	static DataBase db;
	public static int getId (String word, String lang) throws SQLException{
		int id=-1;
		Result r = db.query("SELECT id FROM word_"+lang+" WHERE word='" + word+"'");
		ResultSet rs = r.getResultSet();
		if (rs.next()) {
			id = rs.getInt("id");
		}
		return id;
	}
	
	public static List<Integer> addWords(String[] words, String lang) throws SQLException {
		List<Integer> ids = new ArrayList<Integer>();
		for (String s : words) {
			s = s.trim();
			int id = getId(s, lang) ;
			if (id == -1) {
				try {
					db.query("INSERT INTO word_"+lang+" (word) VALUES ('" + s
							+ "')");
					id = getId(s, lang);
				} catch (Exception e) {
					System.out.println("Failed to insert " + s);
				}
			}
			ids.add(id);
		}
		return ids;
	}

	public static void main(String[] args) throws Exception {
		Class.forName("org.sqlite.JDBC");
		// temporary delete database
		File fdb = new File("res/espdb.db");
		if (fdb.exists())
			fdb.delete();

		db = new DataBase("jdbc:sqlite:res/espdb.db");
		db.query("CREATE TABLE IF NOT EXISTS word_es ( id INTEGER PRIMARY KEY, word VARCHAR(255), UNIQUE (word));");
		db.query("CREATE TABLE IF NOT EXISTS word_en ( id INTEGER PRIMARY KEY, word VARCHAR(255), UNIQUE (word));");
		db.query("CREATE TABLE IF NOT EXISTS word_pl ( id INTEGER PRIMARY KEY, word VARCHAR(255), UNIQUE (word));");
		
		/*
		 * Open txt file Read line by line for each line parse split after
		 * tab(split after coma)create SQL request (insert into table "name" ()
		 * values....)
		 */
		FileReader fr = new FileReader("esp-verbs.csv");
		BufferedReader br = new BufferedReader(fr);
		String ln;
		while ((ln = br.readLine()) != null) {
			String[] cols = ln.split(";");
			if (cols.length < 2)
				continue;
			String[] words_es = {};
			String[] words_pl = {};
			String[] words_en = {};

			if (cols.length > 1)
				words_es = cols[1].split(",");
			if (cols.length > 2)
				words_pl = cols[2].split(",");
			if (cols.length > 3)
				words_en = cols[3].split(",");

			List<Integer> id_es = addWords(words_es, "es");
			List<Integer> id_en = addWords(words_en, "en");
			List<Integer> id_pl = addWords(words_pl, "pl");
			
			
			
		}
		br.close();
	}

}
