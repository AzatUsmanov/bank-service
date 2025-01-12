package com.example.demo.controller;

import com.example.demo.domain.dto.authentication.SignUpRequest;
import com.example.demo.service.authentication.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Контроллер, представляющий основную страницу
 */
@Controller
public class HomeController {

    /**
     * Метод, возвращающий предоставление основной страницы
     * @return {@link ModelAndView} - представление основной страницы
     */
    @GetMapping("/")
    public ModelAndView home() {
        return new ModelAndView("home.html");
    }


}
