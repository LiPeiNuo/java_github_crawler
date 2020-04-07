package crawler;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dao.Project;
//import jdk.nashorn.internal.objects.annotations.Setter;
import dao.ProjectDao;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Crawler {
    private HashSet<String> urlBlackList = new HashSet<>();
    private OkHttpClient okHttpClient = new OkHttpClient();
    private Gson gson = new GsonBuilder().create();


    {
        urlBlackList.add("https://github.com/events");
        urlBlackList.add("https://github.community");
        urlBlackList.add("https://github.com/about");
        urlBlackList.add("https://github.com/pricing");
        urlBlackList.add("https://github.com/contact");
    }

    public static void main(String[] args) throws IOException {
        Crawler crawler = new Crawler();
        long startTime = System.currentTimeMillis();

        // 1. 获取入口页面
        String html = crawler.getPage("https://github.com/akullpp/awesome-java/blob/master/README.md");
        // System.out.println(respBody);

        long finishTime = System.currentTimeMillis();
        // 大约 4s
        System.out.println("获取入口页面时间: " + (finishTime - startTime) + " ms");

        // 2. 解析入口页面, 获取项目列表
        List<Project> projects = crawler.parseProjectList(html);
        // System.out.println(projects);

        System.out.println("解析项目列表时间: " + (System.currentTimeMillis() - finishTime) + " ms");
        // 大约是 0.3 s
        finishTime = System.currentTimeMillis();

        for (int i = 0; i < projects.size(); i++) {
            try {
                Project project = projects.get(i);
                System.out.println("crawing " + project.getName() + " ...");
                String repoName = crawler.getRepoName(project.getUrl());
                String jsonString = crawler.getRepoInfo(repoName);
                // System.out.println(jsonString);
                // 4. 解析每个仓库获取到的 JSON 数据, 得到需要的信息
                crawler.parseRepoInfo(jsonString, project);
                System.out.println("crawing " + project.getName() + " done!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("解析所有项目的时间: " + (System.currentTimeMillis() - finishTime) + " ms");
        finishTime = System.currentTimeMillis();

        // 5. 在这个位置把 project 保存到数据库中
        ProjectDao projectDao = new ProjectDao();
        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);
            String repoName = crawler.getRepoName(project.getUrl());
            String jsonString = crawler.getRepoInfo(repoName);
            // System.out.println(jsonString);
            // 4. 解析每个仓库获取到的 JSON 数据, 得到需要的信息
            crawler.parseRepoInfo(jsonString, project);
            System.out.println(project);
            System.out.println("====================");
            projectDao.save(project);
        }

        // 这个时间 4s 多
        System.out.println("存储数据库的时间: " + (System.currentTimeMillis() - finishTime) + " ms");
        finishTime = System.currentTimeMillis();
        // 总时间: 147s
        System.out.println("整个项目的总时间: " + (finishTime - startTime) + " ms");
    }

    public String getPage(String url) throws IOException {
        // 1. 先创建一个 OkHttpClient 对象
        okHttpClient = new OkHttpClient();
        // 2. 创建一个 Request 对象
        //    此处的 Builder 这个类是一个辅助构造 Request 对象的类.
        //    Builder 中提供的 url 方法能够设定当前请求的url
        Request request = new Request.Builder().url(url).build();
        // 3. 创建一个 Call 对象(这个对象负责进行一次网络访问操作)
        Call call = okHttpClient.newCall(request);
        // 4. 发送请求给服务器, 获取到 response 对象
        Response response = call.execute();
        // 5. 判定响应是否成功
        if (!response.isSuccessful()) {
            System.out.println("请求失败!");
            return null;
        }
        return response.body().string();
    }

    public List<Project> parseProjectList(String html) {
        ArrayList<Project> result = new ArrayList<>();
        // 1. 创建 Document 对象
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByTag("li");
        for (Element li : elements) {
            // 再去获取里面的 a 标签.
            Elements allLink = li.getElementsByTag("a");
            if (allLink.size() == 0) {
                // 当前的这个 li 标签中没有包含 a 标签. 直接忽略掉这个 li
                continue;
            }
            // 一个项目的 li 标签里, 只有一个 a 标签.
            Element link = allLink.get(0);
            // 输出 a 标签中的内容
//            System.out.println(link.text());
//            System.out.println(link.attr("href"));
//            System.out.println(li.text());
//            System.out.println("===============================");
            String url = link.attr("href");
            if (!url.startsWith("https://github.com")) {
                // 如果当前这个项目的 url 不是以 https://github.com 开头的, 我们就直接丢弃掉
                continue;
            }
//            if (url.equals("https://github.com/events")
//                || url.equals("https://github.community")
//                || url.equals("https://github.com/about")) {
//                continue;
//            }
            if (urlBlackList.contains(url)) {
                continue;
            }
            Project project = new Project();
            project.setName(link.text());
            project.setUrl(link.attr("href"));
            project.setDescription(li.text());
            result.add(project);
        }
        return result;
    }
    //调用GitHub API 获取指定仓库信息
    public String getRepoInfo(String respName) throws IOException {
        String userName = "LiPeiNuo";
        String password = "15191712330p";
        String credential = Credentials.basic(userName,password);

        String url = "https://api.github.com/repos/" + respName;
        Request request = new Request.Builder().url(url).header("Authorization", credential).build();
        Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        if (!response.isSuccessful()) {
            System.out.println("访问 Github API 失败! url = " + url);
            return null;
        }
        return response.body().string();
    }

    //将项目得url提取出其中得仓库名字和作者名字
    public String getRepoName(String url){
        int lastOne = url.lastIndexOf("/");
        int lastTwo= url.lastIndexOf("/",lastOne-1);
            if(lastOne == -1 || lastTwo == -1){
                System.out.println("当前url不是一个标准得项目url！ url：" + url);
                return null;
            }
            return url.substring(lastTwo+1);
    }

    //通过这个方法获取信息
    //参数一jsonString 表示Github API 获取的数据
    //第二个参数project 表示解析出得star 数，fork数，opened_issue数据保存到project对象中
    //使用Gson这个库来进行解析
    public void parseRepoInfo(String jsonString,Project project){
        Type type = new TypeToken<HashMap<String,Object>>(){}.getType();
        HashMap<String,Object> hashMap = gson.fromJson(jsonString,type);
        Double starCount = (Double)hashMap.get("stargazers_count");
        project.setStarCount(starCount.intValue());
        Double forkCount = (Double)hashMap.get("forks_count");
        project.setForkCount(forkCount.intValue());
        Double openedIssueCount = (Double)hashMap.get("open_issues_count");
        project.setOpenedIssueCount(openedIssueCount.intValue());



    }

}