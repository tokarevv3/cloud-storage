package ru.tokarev.cloudstorage.http;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DemoController {

//    @RequestMapping
//    public String hello() {
//        return "hello";
//    }
//
    @GetMapping("/cloud/{folderPath")
    public String hello(@PathVariable("folderPath") String folderPath) {
        System.out.println(folderPath);
        return "hello";
    }
}
