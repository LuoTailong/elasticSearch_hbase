package cn.itcast.Tools;

import cn.itcast.Excel.ReadExcel;
import cn.itcast.bean.Article;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.List;

/**
 * Created by angel；
 */
public class HbaseUtils {

    //指定插入es的索引和类型信息
    public static final String ES_INDEX = "articles" ;
    public static final String ES_TYPE = "article" ;

    //hbase的表
    public static final String TABLE_NAME = "articles";
     //列族
    public static final String COLUMN_FAMILY = "info";
    //列
    /**
     *     private String title ;
     private String from ;
     private String readCounts ;
     private String content;
     private String times ;
     *
     */

    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_FROM = "from" ;
    public static final String COLUMN_READCOUNTS = "readCounts" ;
    public static final String COLUMN_CONTENT = "content" ;
    public static final String COLUMN_TIMES = "times" ;

    Configuration conf = null;
    HBaseAdmin admin = null;
    //通过构造方法进行连接hbase
    public HbaseUtils(){
        try {
            conf = new Configuration();
            conf.set("hbase.zookeeper.quorum" , "node1:2181");
            conf.set("hbase.rootdir" , "hdfs://node1:9000/hbase");
            admin = new HBaseAdmin(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //todo 在hbase中建表
    public void createTable(String tableName , String family){
        try {
            if(admin.tableExists(tableName)){
                System.out.println("当前的表已经存在了");
            }else {
                //表描述器
                HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);
                //添加列族
                hTableDescriptor.addFamily(new HColumnDescriptor(family));
                //将描述器放入客户端中，创建完毕
                admin.createTable(hTableDescriptor);
                System.out.println("===================表创建成功===================================");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //todo 插入数据
    public void put(String tableName , String family , String rowkey , String column , String data){
        try {
            HTable hTable = new HTable(conf , tableName);
            Put put = new Put(Bytes.toBytes(rowkey));
            put.add(Bytes.toBytes(family) , Bytes.toBytes(column) , Bytes.toBytes(data));
            hTable.put(put);
            System.out.println("========================数据插入成功==================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //todo 插入一条数据:获取页面传过来的参数（文章的id--rowkey），查询一条数据
    public Article get(String tableName , String rowkey){
        Article article = null;
        try {
            HTable hTable = new HTable(conf , tableName);
            Get get = new Get(Bytes.toBytes(rowkey));
            Result result = hTable.get(get);
            Cell[] cells = result.rawCells();

            if(cells.length == 5){
                article = new Article();
                article.setId(rowkey);
                article.setContent(new String(cells[0].getValue()));
                article.setFrom(new String(cells[1].getValue()));
                article.setReadCounts(new String(cells[2].getValue()));
                article.setTimes(new String(cells[3].getValue()));
                article.setTitle(new String(cells[4].getValue()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return article;
    }

    public static void main(String[] args) throws Exception {
        List<Article> list = ReadExcel.readXlsx("C:\\hadoop\\test\\baijia.xlsx");

        HbaseUtils hbaseUtils = new HbaseUtils();
        hbaseUtils.createTable(TABLE_NAME , COLUMN_FAMILY);

        for(int i=0;i<list.size();i++){
            Article article = list.get(i);
            hbaseUtils.put(TABLE_NAME , COLUMN_FAMILY , article.getId() , COLUMN_TITLE , article.getTitle());
            hbaseUtils.put(TABLE_NAME , COLUMN_FAMILY , article.getId() , COLUMN_CONTENT , article.getContent());
            hbaseUtils.put(TABLE_NAME , COLUMN_FAMILY , article.getId() , COLUMN_FROM , article.getFrom());
            hbaseUtils.put(TABLE_NAME , COLUMN_FAMILY , article.getId() , COLUMN_READCOUNTS , article.getReadCounts());
            hbaseUtils.put(TABLE_NAME , COLUMN_FAMILY , article.getId() , COLUMN_TIMES , article.getTimes());
        }
//        //创建es的索引及映射信息
        ESUtils.addIndex(ES_INDEX , ES_TYPE);
//        //将数据落地到elasticsearch中
        ESUtils.bulkIndex(ES_INDEX , ES_TYPE , list);
    }

}
