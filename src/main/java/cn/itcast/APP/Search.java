package cn.itcast.APP;

import cn.itcast.Tools.ESUtils;
import cn.itcast.bean.Article;
import flexjson.JSONSerializer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by angel；
 */
public class Search extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
//        byte[] keywords = request.getParameter("keyword").getBytes("UTF-8");
//        String keyword = new String(keywords , "UTF-8");
//        System.out.println(keyword);
        String keyword = request.getParameter("keyword");
        keyword = URLEncoder.encode(keyword , "ISO-8859-1");
        keyword = URLDecoder.decode(keyword , "UTF-8");
        System.out.println(keyword);
        //有了关键词，然后根据keyword--->es
        ArrayList<Article> list = ESUtils.search(keyword, "articles", "article", 1, 10);
        System.out.println(list);
        String json = new JSONSerializer().exclude("*.class").serialize(list);
        response.getWriter().print(json);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    public static void main(String[] args) {
        String keyword = "ofo";
        ArrayList<Article> list = ESUtils.search(keyword, "articles", "article", 1, 10);
        System.out.println(list);
    }
}
