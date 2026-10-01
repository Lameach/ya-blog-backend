package ru.yandex.practicum.controller; // Класс находится в пакете с контроллерами

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/home")
    @ResponseBody
    public String homePage() {
        return "<h1>Hello, world!</h1>";
    }

}