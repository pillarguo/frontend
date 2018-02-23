package user.normal.dao;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.BeanParam;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import user.normal.bean.Information;
import user.normal.bean.Page;
import user.normal.bean.Posting;

public class Lucenes {
	// 创建分词器
	private static Analyzer analyzer = new SmartChineseAnalyzer();

	// String pathes = Lucenes.class.getResource("/").getPath().toString();
	// 创建索引
	public void createIndex(@BeanParam Posting posting) throws IOException {
		String realPath = Constant.getLucenePath();

		// 1.创建Directory
		// Store the index in memory:
		// Directory directory = new RAMDirectory();

		Path path = Paths.get(realPath + "index");
		Directory directory = FSDirectory.open(path);

		// 2.创建分词器
		// Analyzer analyzer = new SmartChineseAnalyzer();

		// 3.创建IndexWriterConfig
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		// 4.创建IndexWriter
		IndexWriter iw = null;
		try {
			/*
			 * String pathss = Lucenes.class.getResource("").toString();
			 * System.out.println("pasthss"+pathss);
			 */
			// 创建writer
			iw = new IndexWriter(directory, iwc);

			Document doc = new Document();
			doc.add(new StringField("postingId", posting.getPostingId().toString(), Field.Store.YES));
			Field field = new TextField("postingTitle", posting.getPostingTitle(), Field.Store.YES);
			doc.add(field);
			// doc.add(new StringField("userId", posting.getUserId().toString(),
			// Field.Store.YES));
			// 加权操作。qq邮箱2.0 新浪有限1.5 其他默认1.0 谷歌0.5
			// 1.权值越高，查询结果越靠前。
			// 2.lucene4.0以后不能对doc加权
			// 3.只能对TextField加权
			/*
			 * if (emails[i].indexOf("qq.com") != -1) { field.setBoost(2.0f); }
			 * else if (emails[i].indexOf("sina.com") != -1) {
			 * field.setBoost(1.5f); } else if (emails[i].indexOf("google") !=
			 * -1) { field.setBoost(3.5f); }
			 */

			// doc.add(new IntField("fileSize", fileSizes[i],
			// Field.Store.YES));
			// 对于内容只索引不存储
			// doc.add(new TextField("content", contents[i], Field.Store.NO));
			iw.addDocument(doc);
			iw.commit();
			iw.close();
		} catch (IOException e) {
			e.printStackTrace();
			iw.close();
		}
	}

	// 查询list
	public List<Posting> search(String keyword) throws IOException, ParseException {
		String realPath = Constant.getLucenePath();
		Path path = Paths.get(realPath + "index");
		Directory directory = FSDirectory.open(path);
		Analyzer analyzer = new SmartChineseAnalyzer();
		// DirectoryReader ireader = DirectoryReader.open(directory);
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher is = new IndexSearcher(reader);
		QueryParser parser = new QueryParser("postingTitle", analyzer);
		Query query = parser.parse(keyword);
		// Finds the top 1000 hits for query.
		TopDocs topDocs = is.search(query, 1000);
		System.out.println("总共匹配多少个：" + topDocs.totalHits);
		ScoreDoc[] hits = topDocs.scoreDocs;
		// 应该与topDocs.totalHits相同
		System.out.println("多少条数据：" + hits.length);

		List<Posting> list = new ArrayList<Posting>();

		for (ScoreDoc scoreDoc : hits) {
			Posting posting = new Posting();
			Document document = is.doc(scoreDoc.doc);
			posting.setPostingId(Integer.valueOf(document.get("postingId")));
			posting.setPostingTitle(document.get("postingTitle"));
			list.add(posting);
		}

		for (ScoreDoc scoreDoc : hits) {
			System.out.println("匹配得分：" + scoreDoc.score);
			System.out.println("文档索引ID：" + scoreDoc.doc);
			Document document = is.doc(scoreDoc.doc);
			System.out.println(document.get("postingTitle"));
			System.out.println(document.get("postingId"));
		}
		reader.close();
		directory.close();
		return list;
	}

	// 分页查询
	public Page<Posting> search_page(String keyword, String pageNum, String pageSize)
			throws IOException, ParseException {
		String realPath = Constant.getLucenePath();

		Path path = Paths.get(realPath + "index");
		Directory directory = FSDirectory.open(path);
		Analyzer analyzer = new SmartChineseAnalyzer();
		// DirectoryReader ireader = DirectoryReader.open(directory);
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher is = new IndexSearcher(reader);
		QueryParser parser = new QueryParser("postingTitle", analyzer);
		Query query = parser.parse(keyword);
		// Finds the top 1000 hits for query.
		TopDocs topDocs = is.search(query, 1000);
		System.out.println("总共匹配多少个：" + topDocs.totalHits);
		ScoreDoc[] hits = topDocs.scoreDocs;
		// 应该与topDocs.totalHits相同
		System.out.println("多少条数据：" + hits.length);

		Page<Posting> page = new Page<Posting>(Integer.valueOf(pageNum), Integer.valueOf(pageSize));
		page.setTotal(hits.length);// 总长度
		Integer pages = (int) Math.ceil(hits.length / Integer.valueOf(pageSize));// 总页数
		page.setPages(pages);
		List<Posting> list = new ArrayList<Posting>();

		for (Integer i = page.getStartRow(); i < page.getStartRow() + Integer.valueOf(pageSize); i++) {
			if (i >= hits.length)
				break;
			Posting posting = new Posting();
			ScoreDoc scoreDoc = hits[i];
			Document document = is.doc(scoreDoc.doc);
			posting.setPostingId(Integer.valueOf(document.get("postingId")));
			posting.setPostingTitle(document.get("postingTitle"));
			list.add(posting);

		}
		page.setResult(list);

		for (ScoreDoc scoreDoc : hits) {
			System.out.println("匹配得分：" + scoreDoc.score);
			System.out.println("文档索引ID：" + scoreDoc.doc);
			Document document = is.doc(scoreDoc.doc);
			System.out.println(document.get("postingTitle"));
			System.out.println(document.get("postingId"));
		}
		reader.close();
		directory.close();

		return page;
	}

	// 更新索引
	public void update(@BeanParam Posting posting) throws IOException, ParseException {
		String realPath = Constant.getLucenePath();
		IndexWriterConfig conf = new IndexWriterConfig(analyzer);
		Path path = Paths.get(realPath + "index");
		Directory directory = FSDirectory.open(path);
		IndexWriter iw = new IndexWriter(directory, conf);
		try {

			Term term = new Term("postingId", posting.getPostingId().toString());
			Document doc = new Document();
			doc.add(new StringField("postingId", posting.getPostingId().toString(), Field.Store.YES));
			Field field = new TextField("postingTitle", posting.getPostingTitle(), Field.Store.YES);
			doc.add(field);

			// doc.add(new StringField("userId", posting.getUserId().toString(),
			// Field.Store.YES));
			// 加权
			/*
			 * Field boostField = new TextField("content", contents[1],
			 * Field.Store.YES); doc.add(boostField);
			 * 
			 * boostField.setBoost(5f);
			 */
			// 更新的时候，会把原来那个索引删掉，重新生成一个索引
			iw.updateDocument(term, doc);

			iw.commit();
			iw.close();

		} catch (IOException e) {
			iw.close();
			e.printStackTrace();
		}

	}

}
