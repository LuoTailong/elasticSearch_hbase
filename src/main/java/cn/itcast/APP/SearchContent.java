package cn.itcast.APP;

import cn.itcast.Tools.HbaseUtils;
import cn.itcast.bean.Article;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by angel；
 */
public class SearchContent extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
        String id = request.getParameter("id");
        Article article = new HbaseUtils().get("articles", id);
        response.getWriter().print("<table broder='2'>");
        response.getWriter().print("<td>");
        response.getWriter().print(article.getContent());
        response.getWriter().print("</td></table>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
