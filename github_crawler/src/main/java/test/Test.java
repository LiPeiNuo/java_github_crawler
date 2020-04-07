package test;

import crawler.Crawler;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class Test {
    public static void main(String[] args) throws IOException {
//        Crawler crawler = new Crawler();
//        String html = crawler.getPage("https://github.com/doov-io/doov");
//        System.out.println(html);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        // new Date().getTime() 获取到当前系统时间, 毫秒级别的时间戳
        String result = simpleDateFormat.format(System.currentTimeMillis());
        System.out.println(result);

    }
}
