package cn.itcast.Tools;

import cn.itcast.bean.Article;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by angel；
 */
public class ESUtils {
    private static TransportClient client;
    static{
        try{
           Settings settings = Settings.builder()
                   .put("cluster.name" , "cluster_es").build();
            PreBuiltTransportClient preBuiltTransportClient = new PreBuiltTransportClient(settings);
            client =  preBuiltTransportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("node1") , 9300));

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取客户端
     * */
    public static synchronized TransportClient getClient(){
        return client;
    }


    /**
     *
     * 建立索引
     *
     *
     *"settings":{
            "number_of_shards":3,
            "number_of_replicas":1
     },
     "mappings":{
        "article":{
            "dynamic":"strict",
            "properties":{
                "id":{"type": "string", "store": true},
                "title":{"type": "string","store": true,"index" : "analyzed","analyzer": "ik_max_word"},
                "from":{"type": "string","store": true},
                "readCounts":{"type": "integer","store": true},
                "content":{"type": "string","store": false,"index": "not_analyzed"},
                "times": {"type": "string", "index": "not_analyzed"}
            }
     }
     * */

    public static void addIndex(String index , String type){
        Map<String  , Integer> map = new HashMap<String, Integer>();
        map.put("number_of_shards" , 3);
        map.put("number_of_replicas" , 1);
        //创建索引
        CreateIndexResponse indexResponse = client.admin().indices().prepareCreate(index).setSettings(map).get();
        System.out.println("=============================索引创建完毕=========================================");
        //创建索引的映射信息
        XContentBuilder builder = null;
        try{
            builder = jsonBuilder()
                    .startObject()
                    .startObject(type).field("dynamic" , "strict")
                    .startObject("properties")
                        .startObject("id").field("type" , "string").field("store" , "yes").endObject()
                        .startObject("title").field("type" , "string").field("store" , "yes").field("index" , "analyzed").field("analyzer" ,"ik_max_word").endObject()
                        .startObject("from").field("type" , "string").field("store" , "yes").endObject()
                        .startObject("readCounts").field("type" , "integer").field("store" , "yes").endObject()
                        .startObject("content").field("type" , "string").field("store" , "no").field("index" , "not_analyzed").endObject()
                        .startObject("times").field("type" , "string").field("index" , "not_analyzed").endObject()
                    .endObject()
                    .endObject()
                    .endObject();
            PutMappingRequest source = Requests.putMappingRequest(index).type(type).source(builder);
            client.admin().indices().putMapping(source).get();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
//        addIndex("articles" , "article");
//        List<Article> articles = ReadExcel.readXlsx("/Users/niutao/Desktop/baijia.xlsx");
//        bulkIndex("articles" , "article" , articles);
    }

    /**
     * 批量插入数据：excel里面的数据
     * */
    public static void bulkIndex(String index , String type , List<Article> list){
        BulkRequestBuilder prepareBulk = getClient().prepareBulk();
        for(int i=0;i<list.size();i++){
            Article article = list.get(i);
            IndexRequest indexRequest = new IndexRequest(index , type);
            HashMap<String , Object> hashMap = new HashMap<String, Object>();
            hashMap.put("id" , article.getId());
            hashMap.put("title" , article.getTitle());
            hashMap.put("from" , article.getFrom());
            hashMap.put("readCounts" , article.getReadCounts());
            hashMap.put("times" , article.getTimes());
            indexRequest.source(hashMap);
            indexRequest.id(article.getId());
            prepareBulk.add(indexRequest);
            BulkResponse bulkItemResponses = prepareBulk.get();
            check(bulkItemResponses);

        }
    }


    //检测数据是否插入成功
    private static void check(BulkResponse bulkItemResponses){
        if(bulkItemResponses.hasFailures()){
            BulkItemResponse[] items = bulkItemResponses.getItems();
            for(BulkItemResponse bulkItemResponse:items){
                System.out.println("当前插入数据的时候出错的是："+bulkItemResponse.getFailureMessage());
            }
        }else {
            System.out.println("批量入库成功了！");
        }
    }

    //todo 接收keyword，然后做查询，并且高亮显示
    public static ArrayList<Article> search(String keyword , String index , String type , int start , int row){
        SearchRequestBuilder builder = getClient().prepareSearch(index).setTypes(type)
                .setFrom(start)
                .setSize(row);
        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<font color='red'>");
        highlightBuilder.postTags("</font>");
        builder.highlighter(highlightBuilder);
        if(StringUtils.isNotBlank(keyword)){
            builder.setQuery(QueryBuilders.multiMatchQuery(keyword , "title"));
        }
        SearchResponse searchResponse = builder.get();
        //开始迭代,在把结果封装在Article
        SearchHits hits = searchResponse.getHits();
        ArrayList<Article> articleArrayList = new ArrayList<Article>();
        for(SearchHit searchHit:hits){
            Map<String, Object> source = searchHit.getSource();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            String title = source.get("title").toString();
            String from = source.get("from").toString();
            String readCounts = source.get("readCounts").toString();
            String times = source.get("times").toString();
            String id = searchHit.getId();
            HighlightField highlightField = highlightFields.get("title");
            if(highlightField != null){
                Text[] fragments = highlightField.getFragments();
                title = "";
                for(Text text:fragments){
                    title += text;
                }
            }
            //将结果封装到Article对象中
            Article article = new Article();
            article.setTimes(times);
            article.setTitle(title);
            article.setReadCounts(readCounts);
            article.setFrom(from);
            article.setId(id);
            articleArrayList.add(article);

        }
       return articleArrayList;


    }
}
