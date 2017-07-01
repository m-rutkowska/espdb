package espdb;


import java.io.BufferedReader;
//import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import espdb.DataBase.Result;

public class JsoupTest {

	static DataBase db;

	public static int getId(String word, String lang) throws SQLException {
		int id = -1;
		Result r = db.query("SELECT id FROM word_" + lang + " WHERE word=?", word);
		ResultSet rs = r.getResultSet();
		if (rs.next()) {
			id = rs.getInt("id");
		}
		if (lang.equals("es"))
			System.out.println("id("+word+") = "+id);
		return id;
	}
	
	public static List<Integer> addWords(String[] words, String lang)
			throws SQLException {
		List<Integer> ids = new ArrayList<Integer>();
		for (String s : words) {
			s = s.trim();
			int id = getId(s, lang);
			if (id == -1) {
				try {
					db.query("INSERT INTO word_" + lang + " (word) VALUES (?)",s);
					id = getId(s, lang);
				} catch (Exception e) {
					System.out.println("Failed to insert " + s);
				}
			}
			ids.add(id);
		}
		return ids;
	}

	public static boolean checkRel(int a, int b, String lang) throws SQLException {
		String tabname = "rel_es_"+lang;
		Result r = db.query("SELECT 1 FROM " + tabname + " WHERE id_es=? and id_"+lang+"=?",a,b);
		ResultSet rs = r.getResultSet();
		return rs.next();
	}

	public static void createRel(List<Integer> id_A, List<Integer> id_B,
			String lang) throws SQLException {
		for (int i = 0; i < id_A.size(); i++) {
			for (int j = 0; j < id_B.size(); j++) {
				
				if (checkRel(id_A.get(i), id_B.get(j), lang)) continue;
				
				//insert into tab (id_es,id_pl) VALUES('v1','v2');
				String q = "INSERT INTO rel_es_" + lang + " (id_es, id_" + lang + ") VALUES ('"
						+ id_A.get(i) + "','" + id_B.get(j) + "'); ";
				//System.out.println("q="+q);
				db.query(q);
			}
		}
	}

	public static String[] getTranslation(String esp, String targetLang) throws Exception{
		Connection connect = Jsoup.connect("https://glosbe.com/es/"+targetLang+"/"+esp);
		Response resp = connect.execute();
		Document doc = resp.parse();
		Elements els = doc.getElementsByClass("phr");
		String[] r = new String[els.size()];
		for(int i = 0; i <els.size(); i++){
			r[i] = els.get(i).text();
		}
		return r;	
	}
	
	public static void main(String[] args) throws Exception {
		
		Class.forName("org.sqlite.JDBC");
		// temporary delete database
		//File fdb = new File("db/espdb.db");
		//if (fdb.exists())fdb.delete();

		db = new DataBase("jdbc:sqlite:db/espdb.db");
		db.query("CREATE TABLE IF NOT EXISTS word_es ( id INTEGER PRIMARY KEY, word VARCHAR(255), UNIQUE (word));");
		db.query("CREATE TABLE IF NOT EXISTS word_en ( id INTEGER PRIMARY KEY, word VARCHAR(255), UNIQUE (word));");
		db.query("CREATE TABLE IF NOT EXISTS word_pl ( id INTEGER PRIMARY KEY, word VARCHAR(255), UNIQUE (word));");
		db.query("CREATE TABLE IF NOT EXISTS rel_es_pl ( id_es INTEGER, id_pl INTEGER, UNIQUE(id_es, id_pl));");
		db.query("CREATE TABLE IF NOT EXISTS rel_es_en ( id_es INTEGER, id_en INTEGER, UNIQUE(id_es, id_en));");
		List<String> noExists = new ArrayList<String>();
		
		FileReader fr = new FileReader("res/esp-verbs.csv");
		BufferedReader br = new BufferedReader(fr);
		String ln;
		int trcnt=0;
		while ((ln = br.readLine()) != null) {
			String[] cols = ln.split(";");
			if (cols.length < 1)
				continue;
			String[] words_es = {};
			String[] words_pl = {};
			String[] words_en = {};
			
			words_es = cols[1].split(",");
			words_es = new String[] {words_es[0].trim()}; 
			if (getId(words_es[0], "es") >= 0) {
				System.out.println("** Have " + words_es[0]);
				continue;
			}
			if (trcnt++==50) {
				
				break;
			}
			System.out.println("** Translating '" + words_es[0]+"'");
			try{
			words_en = getTranslation(words_es[0], "en");
			words_pl = getTranslation(words_es[0], "pl");
			}catch(org.jsoup.HttpStatusException e){
				//addWords(words_es, "es");
				if (e.getStatusCode()==404) {
					noExists.add(words_es[0]);
					continue;
				}else{
					br.close();
					throw e;
				}
			}
		
			List<Integer> id_es = addWords(words_es, "es");
			List<Integer> id_en = addWords(words_en, "en");
			List<Integer> id_pl = addWords(words_pl, "pl");
			String pl = "pl";
			createRel(id_es, id_pl, pl);
			String en = "en";
			createRel(id_es, id_en, en);
		}
		for(int i =0; i <noExists.size(); i++){
			System.out.println("Anknown: "+noExists.get(i));
		}
		br.close();
	}
}
