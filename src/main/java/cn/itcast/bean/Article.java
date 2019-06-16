package cn.itcast.bean;

/**
 * Created by angel；
 */
public class Article {
    private String id ;
    private String title ;
    private String from ;
    private String readCounts ;
    private String content;
    private String times ;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setReadCounts(String readCounts) {
        this.readCounts = readCounts;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getFrom() {
        return from;
    }

    public String getId() {
        return id;
    }

    public String getReadCounts() {
        return readCounts;
    }

    public String getTimes() {
        return times;
    }

    @Override
    public String toString() {
        return "{'title':'"+title+"' , 'from':'"+from+"' , 'id':'"+id+"','readCounts':'"+readCounts+"','time':'"+times+"','content':'"+content+"'}";
    }
}
